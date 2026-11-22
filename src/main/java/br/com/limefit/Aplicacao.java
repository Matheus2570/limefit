package br.com.limefit;

import br.com.limefit.desktop.ContextoAplicacao;
import br.com.limefit.desktop.JanelaPrincipal;
import br.com.limefit.desktop.Visual;
import javax.swing.SwingUtilities;

public final class Aplicacao {
    private Aplicacao() {}
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Visual.configurar();
            new JanelaPrincipal(new ContextoAplicacao()).setVisible(true);
        });
    }
}
