package br.com.limefit.modulos.login;

import br.com.limefit.autenticacao.Autenticacao;
import br.com.limefit.web.Idioma;
import com.sun.net.httpserver.HttpExchange;
import j2html.tags.DomContent;
import j2html.tags.ContainerTag;
import static j2html.TagCreator.*;

final class FormulariosLogin {
    private FormulariosLogin() {}

    static DomContent montar(HttpExchange pedido, Idioma idioma, boolean cadastro, String erro, String nome, String email) {
        var formulario = form(input().withType("hidden").withName("csrf_token")
                .withValue(Autenticacao.sessao(pedido).csrf())).withMethod("post")
                .withAction(idioma.url(cadastro ? "/cadastro" : "/login"));
        if (cadastro) formulario.with(campo(idioma.escolher("Nome", "Name"), "nome", "text", "name", nome, 120));
        formulario.with(campo(idioma.escolher("E-mail", "Email"), "email", "email", "email", email, 190),
                senha(idioma, "password", idioma.escolher("Senha", "Password"), cadastro));
        if (cadastro) formulario.with(
                p(idioma.escolher("Use de 8 a 128 caracteres.", "Use 8 to 128 characters.")).withId("senha-ajuda").withClass("ajuda"),
                senha(idioma, "confirm_password", idioma.escolher("Confirmar senha", "Confirm password"), true));
        formulario.with(button(cadastro ? idioma.escolher("Criar conta", "Create account") : idioma.escolher("Entrar", "Log in"))
                .withType("submit").withClass("botao largura-total"));
        var painel = section().withClass("login-card").attr("aria-labelledby", "form-titulo");
        painel.with(h2(cadastro ? idioma.escolher("Criar sua conta", "Create your account") : idioma.escolher("Entrar na conta", "Log in to your account"))
                        .withId("form-titulo"),
                p(cadastro ? idioma.escolher("Preencha seus dados para começar.", "A few details to get started.") : idioma.escolher(
                        "Informe seus dados para continuar no LimeFit.", "Enter your details to continue to LimeFit.")).withClass("subtitulo"));
        if (!erro.isEmpty()) painel.with(div(erro).withClass("erro").attr("role", "alert"));
        painel.with(formulario,
                a(cadastro ? idioma.escolher("Já tenho uma conta", "I already have an account") : idioma.escolher("Criar conta", "Create account"))
                        .withHref(idioma.url(cadastro ? "/login" : "/cadastro")).withClass("botao botao-contorno largura-total"),
                a(idioma.escolher("← Voltar ao início", "← Back to home")).withHref(idioma.url("/")).withClass("voltar"));
        return div(section(p(idioma.escolher("SUA ALIMENTAÇÃO, DO SEU JEITO", "YOUR NUTRITION, YOUR WAY")).withClass("sobretitulo"),
                        h1(text(cadastro ? idioma.escolher("Seu próximo", "Your next") : idioma.escolher("Bom ter você", "Good to have you")), br(),
                                span(cadastro ? idioma.escolher("passo começa aqui.", "step starts here.") : idioma.escolher("de volta.", "back.")).withClass("verde")),
                        p(idioma.escolher("Um espaço seu para organizar a alimentação e planejar o dia a dia.",
                                "Your own space to organize your nutrition and plan your everyday meals.")).withClass("login-intro-texto"))
                        .withClass("login-intro"), painel).withClass("container login-layout");
    }

    private static DomContent campo(String rotulo, String nome, String tipo, String autocomplete, String valor, int limite) {
        return div(label(rotulo).attr("for", nome), input().withType(tipo).withId(nome).withName(nome).withValue(valor)
                .attr("autocomplete", autocomplete).attr("maxlength", limite).attr("required")).withClass("campo");
    }

    private static DomContent senha(Idioma idioma, String nome, String rotulo, boolean nova) {
        var entrada = input().withType("password").withId(nome).withName(nome).attr("maxlength", 128)
                .attr("autocomplete", nova ? "new-password" : "current-password").attr("required");
        if (nova) entrada.attr("minlength", 8);
        if (nome.equals("password") && nova) entrada.attr("aria-describedby", "senha-ajuda");
        ContainerTag<?> mostrar = button(idioma.escolher("Mostrar", "Show")).withType("button").withClass("mostrar-senha")
                .attr("data-senha", nome).attr("data-mostrar", idioma.escolher("Mostrar", "Show"))
                .attr("data-ocultar", idioma.escolher("Ocultar", "Hide"))
                .attr("aria-controls", nome).attr("aria-pressed", "false").attr("hidden");
        return div(label(rotulo).attr("for", nome), div(entrada, mostrar).withClass("senha-wrap")).withClass("campo");
    }
}
