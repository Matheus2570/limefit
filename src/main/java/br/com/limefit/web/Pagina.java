package br.com.limefit.web;

import br.com.limefit.autenticacao.Autenticacao;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import j2html.tags.DomContent;

import java.io.IOException;
import java.util.Map;
import static j2html.TagCreator.*;

public abstract class Pagina implements HttpHandler {
    private final Tela tela;

    protected Pagina(Tela tela) { this.tela = tela; }
    public final String rota() { return tela.rota(); }

    @Override
    public final void handle(HttpExchange pedido) throws IOException {
        Idioma idioma = Idioma.daRequisicao(pedido.getRequestURI());
        pedido.getResponseHeaders().set("Cache-Control", "no-store");
        if (tela.privada() && Autenticacao.sessao(pedido).usuario() == null) {
            Resposta.redirecionar(pedido, idioma.url("/login"));
            return;
        }
        switch (pedido.getRequestMethod()) {
            case "GET", "HEAD" -> exibir(pedido, idioma);
            case "POST" -> {
                try {
                    var campos = Formulario.ler(pedido);
                    if (!Autenticacao.csrfValido(pedido, campos.get("csrf_token"))) {
                        enviarHtml(pedido, idioma, 403, section(h1(idioma.escolher("Sessão expirada", "Session expired")),
                                p(idioma.escolher("Atualize a página e tente novamente.", "Refresh the page and try again.")),
                                a(idioma.escolher("Voltar", "Go back")).withHref(idioma.url(rota())))
                                .withClass("container pagina-simples"));
                        return;
                    }
                    enviarFormulario(pedido, idioma, campos);
                } catch (Formulario.Invalido erro) {
                    Resposta.texto(pedido, erro.codigo(), "text/plain", idioma.escolher(
                            "Formulário inválido ou muito grande.", "Invalid or oversized form."));
                }
            }
            default -> Resposta.metodoInvalido(pedido, "GET, HEAD, POST");
        }
    }

    protected void exibir(HttpExchange pedido, Idioma idioma) throws IOException {
        enviarHtml(pedido, idioma, conteudo(pedido, idioma));
    }

    // As subclasses implementam a interface com j2html; o menu vem do Layout.
    protected DomContent conteudo(HttpExchange pedido, Idioma idioma) {
        return section(h1(tela.titulo(idioma))).withClass("container pagina-simples");
    }

    protected void enviarFormulario(HttpExchange pedido, Idioma idioma, Map<String, String> campos) throws IOException {
        Resposta.metodoInvalido(pedido, "GET, HEAD");
    }

    protected final long usuarioId(HttpExchange pedido) { return Autenticacao.exigirUsuario(pedido).id(); }

    protected final void enviarHtml(HttpExchange pedido, Idioma idioma, DomContent conteudo) throws IOException {
        enviarHtml(pedido, idioma, 200, conteudo);
    }

    protected final void enviarHtml(HttpExchange pedido, Idioma idioma, int codigo, DomContent conteudo) throws IOException {
        Resposta.texto(pedido, codigo, "text/html", Layout.montar(tela, idioma, Autenticacao.sessao(pedido), conteudo));
    }
}
