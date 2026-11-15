package br.com.limefit.modulos.inicio;

import br.com.limefit.web.Pagina;
import br.com.limefit.web.Tela;
import br.com.limefit.web.Idioma;
import br.com.limefit.autenticacao.Autenticacao;
import com.sun.net.httpserver.HttpExchange;
import j2html.tags.DomContent;
import static j2html.TagCreator.*;

public final class InicioPagina extends Pagina {
    public InicioPagina() {
        super(Tela.INICIO);
    }

    @Override
    protected DomContent conteudo(HttpExchange pedido, Idioma idioma) {
        boolean conectado = Autenticacao.sessao(pedido).usuario() != null;
        return div(
                section(div(div(
                        p(idioma.escolher("COMER BEM COMEÇA COM VOCÊ", "EATING WELL STARTS WITH YOU")).withClass("sobretitulo"),
                        h1(text(idioma.escolher("Sua alimentação,", "Your nutrition,")), br(),
                                span(idioma.escolher("do seu jeito.", "your way.")).withClass("verde")),
                        p(idioma.escolher("Um espaço para organizar sua alimentação, conhecer seus nutrientes e planejar refeições com mais clareza.",
                                "A place to organize your nutrition, understand your nutrients and plan meals with more clarity.")).withClass("hero-texto"),
                        a(text(conectado ? idioma.escolher("Acessar meu perfil", "Go to my profile") : idioma.escolher("Começar agora", "Get started")),
                                span(" →").attr("aria-hidden", "true")).withClass("botao")
                                .withHref(idioma.url(conectado ? "/perfil" : "/login")),
                        p(idioma.escolher("Mais organização. Um dia de cada vez.", "More clarity. One day at a time.")).withClass("hero-nota")
                ).withClass("hero-conteudo"),
                        img().withSrc("/assets/capa.png").withAlt(idioma.escolher(
                                "Prato com frango grelhado, arroz, salada, abacate e limão",
                                "A bowl of grilled chicken, rice, salad, avocado and lime"))
                                .withWidth("1536").withHeight("1024").attr("fetchpriority", "high").withClass("hero-foto"))
                        .withClass("hero-grid container")).withClass("hero"),
                section(div(
                        div(p(idioma.escolher("A IDEIA É SIMPLES", "A SIMPLE IDEA")).withClass("sobretitulo"),
                                h2(idioma.escolher("Um pouco de planejamento.\nMais espaço para viver.", "A little planning.\nMore room for life."))),
                        div(p(idioma.escolher("O LimeFit é um projeto acadêmico que conecta perfil, alimentos, refeições e planejamento em um só lugar.",
                                        "LimeFit is an academic project that brings profile, foods, meals and planning together in one place.")),
                                p(idioma.escolher("Comece criando sua conta. Os módulos estão sendo desenvolvidos pela nossa equipe.",
                                        "Start by creating your account. Our team is building the modules.")),
                                a(conectado ? idioma.escolher("Meu perfil →", "My profile →") : idioma.escolher("Criar minha conta →", "Create my account →"))
                                        .withHref(idioma.url(conectado ? "/perfil" : "/cadastro"))))
                        .withClass("container apresentacao-grid")).withClass("apresentacao")
        );
    }
}
