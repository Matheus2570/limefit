package br.com.limefit.modulos.login;

import br.com.limefit.web.*;
import com.sun.net.httpserver.HttpExchange;
import j2html.tags.DomContent;
import static j2html.TagCreator.*;

public final class CadastroSucessoPagina extends Pagina {
    public CadastroSucessoPagina() { super(Tela.CADASTRO_SUCESSO); }

    @Override
    protected DomContent conteudo(HttpExchange pedido, Idioma idioma) {
        return div(section(p(idioma.escolher("TUDO CERTO", "YOU'RE ALL SET")).withClass("sobretitulo"),
                h1(idioma.escolher("Conta criada!", "Account created!")),
                p(idioma.escolher("Sua conta no LimeFit está pronta. Entre para começar.", "Your LimeFit account is ready. Log in to get started.")),
                a(idioma.escolher("Ir para o login", "Go to login")).withHref(idioma.url("/login")).withClass("botao largura-total"))
                .withClass("login-card sucesso")).withClass("container sucesso-layout");
    }
}
