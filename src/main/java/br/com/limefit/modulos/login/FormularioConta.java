package br.com.limefit.modulos.login;

import br.com.limefit.autenticacao.*;
import br.com.limefit.desktop.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

/** Composição do formulário e execução JDBC fora da EDT, compartilhadas por login/cadastro. */
abstract class FormularioConta extends PainelTela {
    private final boolean cadastro;
    private final JLabel titulo = Visual.titulo(30);
    private final JTextArea subtitulo = Visual.texto(15);
    private final JLabel nomeRotulo = Visual.rotulo(), emailRotulo = Visual.rotulo();
    private final JLabel senhaRotulo = Visual.rotulo(), confirmarRotulo = Visual.rotulo();
    private final JTextField nome, email;
    private final JPasswordField senha, confirmar;
    private final JCheckBox mostrar = new JCheckBox();
    private final JButton enviar, alternar, voltar;
    private final JTextArea aviso = Visual.texto(14);
    private final JLabel ajuda = Visual.rotulo();
    private final ServicoContas contas;
    private MensagemConta mensagem;
    private boolean ocupada;
    private long operacao;
    private final char mascara;

    protected FormularioConta(ContextoAplicacao contexto, boolean cadastro) {
        super(contexto);
        this.cadastro = cadastro;
        contas = new ServicoContas(contexto.usuarios());
        String prefixo = cadastro ? "cadastro." : "login.";
        nome = Visual.campo(new JTextField(25), prefixo + "nome", 120);
        email = Visual.campo(new JTextField(25), prefixo + "email", 190);
        senha = Visual.campo(new JPasswordField(25), prefixo + "senha", 128);
        confirmar = Visual.campo(new JPasswordField(25), prefixo + "confirmar", 128);
        mascara = senha.getEchoChar();
        enviar = Visual.botao(prefixo + "enviar", true);
        alternar = Visual.botao(prefixo + "alternar", false);
        voltar = Visual.botao(prefixo + "voltar", false);
        aviso.setName(prefixo + "aviso");
        aviso.setForeground(Visual.ERRO);
        aviso.setRows(2);
        JPanel formulario = new JPanel(new GridBagLayout());
        formulario.setBackground(Visual.PAINEL);
        formulario.setBorder(new EmptyBorder(26, 32, 26, 32));
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0; c.gridy = 0; c.weightx = 1; c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 0, 12, 0);
        formulario.add(titulo, c);
        c.gridy++; subtitulo.setRows(2); formulario.add(subtitulo, c);
        if (cadastro) campo(formulario, c, nomeRotulo, nome);
        campo(formulario, c, emailRotulo, email);
        campo(formulario, c, senhaRotulo, senha);
        if (cadastro) {
            c.gridy++; formulario.add(ajuda, c);
            campo(formulario, c, confirmarRotulo, confirmar);
        }
        c.gridy++;
        mostrar.setName(prefixo + "mostrar");
        mostrar.setOpaque(false); mostrar.setForeground(Visual.SUAVE);
        mostrar.setFont(Visual.fonte(14, Font.PLAIN));
        formulario.add(mostrar, c);
        c.gridy++; formulario.add(aviso, c);
        c.gridy++; formulario.add(enviar, c);
        c.gridy++; formulario.add(alternar, c);
        c.gridy++; c.insets.bottom = 0; formulario.add(voltar, c);
        JPanel centro = Visual.painel(new GridBagLayout());
        centro.setBorder(new EmptyBorder(24, 24, 24, 24));
        centro.add(formulario);
        add(Visual.rolagem(centro), BorderLayout.CENTER);
        mostrar.addActionListener(e -> {
            senha.setEchoChar(mostrar.isSelected() ? (char) 0 : mascara);
            confirmar.setEchoChar(senha.getEchoChar());
        });
        enviar.addActionListener(e -> enviar());
        senha.addActionListener(e -> enviar());
        confirmar.addActionListener(e -> enviar());
        email.addActionListener(e -> { if (cadastro) senha.requestFocusInWindow(); else enviar(); });
        alternar.addActionListener(e -> contexto.navegar(cadastro ? Tela.LOGIN : Tela.CADASTRO));
        voltar.addActionListener(e -> contexto.navegar(Tela.INICIO));
        atualizarTextos();
    }
    private void campo(JPanel painel, GridBagConstraints c, JLabel rotulo, JTextField entrada) {
        c.gridy++; c.insets.bottom = 5; painel.add(rotulo, c);
        rotulo.setLabelFor(entrada);
        c.gridy++; c.insets.bottom = 12; painel.add(entrada, c);
    }
    private void enviar() {
        if (ocupada) return;
        String nomeInformado = nome.getText(), emailInformado = email.getText();
        char[] segredo = senha.getPassword(), repeticao = confirmar.getPassword();
        String senhaInformada = new String(segredo), confirmacao = new String(repeticao);
        Arrays.fill(segredo, '\0'); Arrays.fill(repeticao, '\0');
        senha.setText(""); confirmar.setText("");
        mensagem = null;
        definirOcupada(true);
        long pedido = ++operacao, revisao = contexto.sessao().revisao();
        new SwingWorker<Usuario, Void>() {
            @Override protected Usuario doInBackground() throws Exception {
                if (cadastro) { contas.cadastrar(nomeInformado, emailInformado, senhaInformada, confirmacao); return null; }
                return contas.autenticar(emailInformado, senhaInformada);
            }
            @Override protected void done() {
                // Ignora respostas antigas se houve navegação, saída ou troca de conta.
                if (pedido != operacao || revisao != contexto.sessao().revisao() || !isDisplayable()) return;
                definirOcupada(false);
                try { concluir(get()); }
                catch (InterruptedException erro) { Thread.currentThread().interrupt(); }
                catch (ExecutionException erro) {
                    Throwable causa = erro.getCause();
                    mensagem = causa instanceof ValidacaoConta validacao ? validacao.mensagem()
                            : causa instanceof SQLException ? MensagemConta.BANCO : MensagemConta.FALHA;
                    if (causa instanceof SQLException sql)
                        java.util.logging.Logger.getLogger(getClass().getName()).warning("Database SQLState=" + sql.getSQLState());
                    atualizarTextos();
                    senha.requestFocusInWindow();
                }
            }
        }.execute();
    }
    protected abstract void concluir(Usuario usuario);
    private void definirOcupada(boolean valor) {
        ocupada = valor;
        nome.setEnabled(!valor); email.setEnabled(!valor);
        senha.setEnabled(!valor); confirmar.setEnabled(!valor);
        mostrar.setEnabled(!valor); enviar.setEnabled(!valor);
        atualizarTextos();
    }
    @Override public void atualizarTextos() {
        titulo.setText(cadastro ? idioma.escolher("Criar sua conta", "Create your account") : idioma.escolher("Bom ter você de volta.", "Good to have you back."));
        subtitulo.setText(cadastro ? idioma.escolher("Preencha seus dados para começar.", "A few details to get started.") : idioma.escolher("Entre na sua conta para continuar no LimeFit.", "Log in to your account to continue to LimeFit."));
        nomeRotulo.setText(idioma.escolher("Nome", "Name"));
        emailRotulo.setText(idioma.escolher("E-mail", "Email"));
        senhaRotulo.setText(idioma.escolher("Senha", "Password"));
        confirmarRotulo.setText(idioma.escolher("Confirmar senha", "Confirm password"));
        mostrar.setText(idioma.escolher("Mostrar senha", "Show password"));
        ajuda.setText(idioma.escolher("Use de 8 a 128 caracteres.", "Use 8 to 128 characters."));
        enviar.setText(ocupada ? idioma.escolher("Aguarde…", "Please wait…") : cadastro ? Tela.CADASTRO.titulo(idioma) : Tela.LOGIN.titulo(idioma));
        alternar.setText(cadastro ? idioma.escolher("Já tenho uma conta", "I already have an account") : Tela.CADASTRO.titulo(idioma));
        voltar.setText(idioma.escolher("Voltar ao início", "Back to home"));
        aviso.setText(mensagem == null ? "" : mensagem.texto(idioma));
    }
    @Override public void aoExibir() { (cadastro ? nome : email).requestFocusInWindow(); }
    @Override public void aoOcultar() {
        operacao++;
        senha.setText(""); confirmar.setText("");
        mostrar.setSelected(false);
        senha.setEchoChar(mascara); confirmar.setEchoChar(mascara);
        mensagem = null;
        definirOcupada(false);
    }
    @Override public void limparDados() {
        aoOcultar();
        nome.setText(""); email.setText("");
    }
}
