package br.com.limefit.modulos.login;

import br.com.limefit.desktop.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public final class TelaCadastroSucesso extends PainelTela {
    private final JLabel titulo = Visual.titulo(32);
    private final JTextArea mensagem = Visual.texto(17);
    private final JButton entrar = Visual.botao("sucesso.entrar", true);
    private final JButton inicio = Visual.botao("sucesso.inicio", false);

    public TelaCadastroSucesso(ContextoAplicacao contexto) {
        super(contexto);
        JPanel cartao = new JPanel(new GridBagLayout());
        cartao.setBackground(Visual.PAINEL);
        cartao.setBorder(new EmptyBorder(40, 40, 40, 40));
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0; c.gridy = 0; c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 1;
        c.insets = new Insets(0, 0, 24, 0);
        titulo.setForeground(Visual.LIMA); cartao.add(titulo, c);
        c.gridy++; mensagem.setColumns(32); mensagem.setRows(3); cartao.add(mensagem, c);
        c.gridy++; cartao.add(entrar, c);
        c.gridy++; c.insets.bottom = 0; cartao.add(inicio, c);
        JPanel centro = Visual.painel(new GridBagLayout());
        centro.setBorder(new EmptyBorder(30, 30, 30, 30)); centro.add(cartao);
        add(Visual.rolagem(centro), BorderLayout.CENTER);
        entrar.addActionListener(e -> contexto.navegar(Tela.LOGIN));
        inicio.addActionListener(e -> contexto.navegar(Tela.INICIO));
        atualizarTextos();
    }
    @Override public void atualizarTextos() {
        titulo.setText(idioma.escolher("Sua conta foi criada!", "Your account is ready!"));
        mensagem.setText(idioma.escolher("Cadastro concluído. Entre com seu e-mail e senha para começar.", "Registration complete. Log in with your email and password to get started."));
        entrar.setText(Tela.LOGIN.titulo(idioma));
        inicio.setText(idioma.escolher("Voltar ao início", "Back to home"));
    }
}
