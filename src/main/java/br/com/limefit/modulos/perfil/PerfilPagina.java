package br.com.limefit.modulos.perfil;

import br.com.limefit.web.Idioma;
import br.com.limefit.web.Pagina;
import br.com.limefit.web.Tela;
import com.sun.net.httpserver.HttpExchange;
import j2html.tags.DomContent;
import static j2html.TagCreator.*;

public final class PerfilPagina extends Pagina {
    public PerfilPagina() {
        super(Tela.PERFIL);
    }

    @Override
    protected DomContent conteudo(HttpExchange pedido, Idioma idioma) {
        // Implemente a tela deste m?dulo aqui com j2html.
        // usuarioId(pedido) retorna o dono autenticado. Veja docs/CONTAS-E-DADOS.md.
        return section(h1(Tela.PERFIL.titulo(idioma))).withClass("container pagina-simples");
    }
}
