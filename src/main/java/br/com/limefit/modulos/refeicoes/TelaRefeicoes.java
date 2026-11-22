package br.com.limefit.modulos.refeicoes;

import br.com.limefit.desktop.ContextoAplicacao;
import br.com.limefit.desktop.PainelModulo;
import br.com.limefit.desktop.Tela;

/** Responsável: Guilherme. Menu, sessão e navegação já vêm da base compartilhada. */
public final class TelaRefeicoes extends PainelModulo {
    private final RefeicaoDao dao;

    public TelaRefeicoes(ContextoAplicacao contexto) {
        super(contexto, Tela.REFEICOES);
        dao = new RefeicaoDao(contexto.conexao(), contexto.sessao());
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
