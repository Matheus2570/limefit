package br.com.limefit.modulos.projeto;

import br.com.limefit.desktop.ContextoAplicacao;
import br.com.limefit.desktop.PainelModulo;
import br.com.limefit.desktop.Tela;

/** Responsável: Francisco. Menu, sessão e navegação já vêm da base compartilhada. */
public final class TelaProjeto extends PainelModulo {

    public TelaProjeto(ContextoAplicacao contexto) {
        super(contexto, Tela.PROJETO);
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
