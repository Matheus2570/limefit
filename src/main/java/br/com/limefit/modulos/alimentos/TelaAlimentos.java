package br.com.limefit.modulos.alimentos;

import br.com.limefit.desktop.ContextoAplicacao;
import br.com.limefit.desktop.PainelModulo;
import br.com.limefit.desktop.Tela;

/** Responsável: Lucas. Menu, sessão e navegação já vêm da base compartilhada. */
public final class TelaAlimentos extends PainelModulo {
    private final AlimentoDao dao;

    public TelaAlimentos(ContextoAplicacao contexto) {
        super(contexto, Tela.ALIMENTOS);
        dao = new AlimentoDao(contexto.conexao(), contexto.sessao());
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
