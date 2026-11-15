package br.com.limefit.autenticacao;

import br.com.limefit.web.Formulario;
import br.com.limefit.web.Idioma;
import br.com.limefit.web.Resposta;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;

public final class Sair implements HttpHandler {
    @Override
    public void handle(HttpExchange pedido) throws IOException {
        if (!pedido.getRequestMethod().equals("POST")) {
            Resposta.metodoInvalido(pedido, "POST");
            return;
        }
        Idioma idioma = Idioma.daRequisicao(pedido.getRequestURI());
        pedido.getResponseHeaders().set("Cache-Control", "no-store");
        try {
            if (!Autenticacao.csrfValido(pedido, Formulario.ler(pedido).get("csrf_token"))) {
                Resposta.texto(pedido, 403, "text/plain", idioma.escolher("Atualize a página e tente novamente.", "Refresh the page and try again."));
                return;
            }
            Autenticacao.sair(pedido);
            Resposta.redirecionar(pedido, idioma.url("/"));
        } catch (Formulario.Invalido erro) {
            Resposta.texto(pedido, erro.codigo(), "text/plain", idioma.escolher("Formulário inválido.", "Invalid form."));
        }
    }
}
