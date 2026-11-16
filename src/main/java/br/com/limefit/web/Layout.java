package br.com.limefit.web;

import br.com.limefit.autenticacao.Sessao;
import j2html.tags.DomContent;
import j2html.tags.ContainerTag;

import static j2html.TagCreator.*;

final class Layout {
    private Layout() {}

    static String montar(Tela ativa, Idioma idioma, Sessao sessao, DomContent conteudo) {
        ContainerTag<?> menu = nav().attr("aria-label", idioma.escolher("Navegação principal", "Main navigation"));
        for (Tela tela : Tela.values()) {
            if (tela.noMenu()) {
                var link = a(tela.titulo(idioma)).withHref(idioma.url(tela.rota()));
                if (tela == ativa) link.attr("aria-current", "page");
                menu.with(link);
            }
        }
        var idiomas = div().withClass("idiomas").attr("aria-label", idioma.escolher("Idioma", "Language"));
        for (Idioma opcao : new Idioma[]{Idioma.PORTUGUES, Idioma.INGLES}) {
            var link = a(opcao == Idioma.INGLES ? "EN" : "PT").withHref(opcao.url(ativa.rota()))
                    .attr("hreflang", opcao.tag()).attr("lang", opcao.tag())
                    .attr("aria-label", opcao == Idioma.INGLES ? "English" : "Português");
            if (opcao == idioma) link.attr("aria-current", "true");
            idiomas.with(link);
        }
        DomContent conta = sessao.usuario() == null
                ? a(idioma.escolher("Entrar", "Log in")).withHref(idioma.url("/login")).withClass("botao botao-pequeno")
                : div(span(sessao.usuario().nome()).withClass("nome-conta"),
                    form(input().withType("hidden").withName("csrf_token").withValue(sessao.csrf()),
                            button(idioma.escolher("Sair", "Log out")).withType("submit").withClass("sair"))
                            .withMethod("post").withAction(idioma.url("/sair"))).withClass("conta");
        String classe = ativa == Tela.INICIO ? "inicio" : ativa.noMenu() ? "pagina-modulo" : "autenticacao";
        return "<!doctype html>" + html(
                head(meta().withCharset("utf-8"), meta().withName("viewport").withContent("width=device-width, initial-scale=1"),
                        meta().withName("theme-color").withContent("#0b100d"),
                        meta().withName("description").withContent(idioma.escolher(
                                "LimeFit: sua alimentação, do seu jeito.", "LimeFit: your nutrition, your way.")),
                        title(ativa.titulo(idioma) + " | LimeFit"),
                        link().withRel("icon").withHref("/assets/logo.png").withType("image/png"),
                        link().withRel("stylesheet").withHref("/assets/limefit.css"),
                        link().withRel("stylesheet").withHref(ativa.css()),
                        script().withSrc("/assets/autenticacao.js").attr("defer")),
                body(a(idioma.escolher("Ir para o conteúdo", "Skip to content")).withHref("#conteudo").withClass("pular"),
                        header(div(
                                a(img().withSrc("/assets/logo.png").withAlt("").withWidth("44").withHeight("44"),
                                        span("Lime"), span("Fit").withClass("verde"))
                                        .withHref(idioma.url("/")).withClass("marca").attr("aria-label", "LimeFit"),
                                menu, div(idiomas, conta).withClass("topo-acoes")).withClass("container topo-inner")).withClass("topo"),
                        main(conteudo).withId("conteudo"),
                        footer(div(span("LimeFit"), span(idioma.escolher(
                                "Projeto acadêmico · POO e Inglês Técnico", "Academic project · OOP & Technical English")))
                                .withClass("container rodape-inner")).withClass("rodape"))
                        .withClass(classe + " pagina-" + ativa.modulo())).attr("lang", idioma.tag()).render();
    }
}
