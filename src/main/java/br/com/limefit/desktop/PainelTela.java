package br.com.limefit.desktop;

import javax.swing.JPanel;
import java.awt.BorderLayout;

/** Ciclo de vida das telas dentro do CardLayout. Métodos chamados na EDT. */
public abstract class PainelTela extends JPanel {
    protected final ContextoAplicacao contexto;
    protected final Idioma idioma;

    protected PainelTela(ContextoAplicacao contexto) {
        super(new BorderLayout());
        this.contexto = contexto;
        this.idioma = contexto.idioma();
        setBackground(Visual.FUNDO);
    }

    public abstract void atualizarTextos();
    public void aoExibir() {}
    public void aoOcultar() {}
    /** Sobrescreva para esvaziar campos, tabelas, seleção e caches da conta. */
    public void limparDados() {}
}
