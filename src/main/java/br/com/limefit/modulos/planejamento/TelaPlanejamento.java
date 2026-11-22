package br.com.limefit.modulos.planejamento;

import br.com.limefit.desktop.ContextoAplicacao;
import br.com.limefit.desktop.PainelModulo;
import br.com.limefit.desktop.Tela;

/** Responsável: Gustavo. Menu, sessão e navegação já vêm da base compartilhada. */
public final class TelaPlanejamento extends PainelModulo {
    private final PlanejamentoDao dao;

    public TelaPlanejamento(ContextoAplicacao contexto) {
        super(contexto, Tela.PLANEJAMENTO);
        dao = new PlanejamentoDao(contexto.conexao(), contexto.sessao());
        // Monte a interface do seu módulo no JPanel conteudo, usando layouts Swing.
    }

    @Override public void atualizarTextos() {
        super.atualizarTextos();
        // Traduza os componentes com idioma.escolher("Português", "English").
    }

    @Override public void limparDados() {
        // Limpe campos, tabelas, seleções e caches da conta anterior aqui.
    }
}
