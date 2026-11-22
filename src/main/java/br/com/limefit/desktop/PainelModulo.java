package br.com.limefit.desktop;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;

/** Menu e navegação ficam na janela; cada colega preenche apenas o conteúdo. */
public abstract class PainelModulo extends PainelTela {
    protected final JPanel conteudo = Visual.painel(new BorderLayout());
    private final JLabel titulo = Visual.titulo(36);
    private final Tela tela;

    protected PainelModulo(ContextoAplicacao contexto, Tela tela) {
        super(contexto);
        this.tela = tela;
        setBorder(new EmptyBorder(32, 40, 32, 40));
        titulo.setBorder(new EmptyBorder(0, 0, 26, 0));
        add(titulo, BorderLayout.NORTH);
        add(Visual.rolagem(conteudo), BorderLayout.CENTER);
        titulo.setText(tela.titulo(idioma));
    }
    @Override public void atualizarTextos() { titulo.setText(tela.titulo(idioma)); }
    @Override public void aoExibir() { contexto.usuarioId(); }
}
