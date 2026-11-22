package br.com.limefit.desktop;

import br.com.limefit.modulos.inicio.TelaInicio;
import br.com.limefit.modulos.login.TelaLogin;
import br.com.limefit.modulos.login.TelaCadastro;
import br.com.limefit.modulos.login.TelaCadastroSucesso;
import br.com.limefit.modulos.perfil.TelaPerfil;
import br.com.limefit.modulos.alimentos.TelaAlimentos;
import br.com.limefit.modulos.refeicoes.TelaRefeicoes;
import br.com.limefit.modulos.planejamento.TelaPlanejamento;
import br.com.limefit.modulos.projeto.TelaProjeto;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.EnumMap;
import java.util.Map;

public final class JanelaPrincipal extends JFrame {
    private final ContextoAplicacao contexto;
    private final CardLayout cards = new CardLayout();
    private final JPanel paginas = new JPanel(cards);
    private final Map<Tela, PainelTela> telas = new EnumMap<>(Tela.class);
    private final Map<Tela, JButton> menu = new EnumMap<>(Tela.class);
    private final JLabel identidade = Visual.rotulo();
    private final JLabel rodape = Visual.rotulo();
    private final JButton conta = Visual.botao("conta", true);
    private final JButton sair = Visual.botao("sair", false);
    private final JButton pt = Visual.botao("idioma.pt", false);
    private final JButton en = Visual.botao("idioma.en", false);
    private Tela atual;
    private Tela aposLogin = Tela.INICIO;

    public JanelaPrincipal(ContextoAplicacao contexto) {
        super("LimeFit");
        this.contexto = contexto;
        setName("limefit");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setIconImage(Visual.imagem("logo.png"));
        setMinimumSize(new Dimension(900, 700));
        setSize(1200, 820);
        setLocationRelativeTo(null);
        getContentPane().setBackground(Visual.FUNDO);
        setLayout(new BorderLayout());
        add(cabecalho(), BorderLayout.NORTH);
        add(paginas, BorderLayout.CENTER);
        rodape.setBorder(new EmptyBorder(14, 32, 14, 32));
        add(rodape, BorderLayout.SOUTH);
        registrar(Tela.INICIO, new TelaInicio(contexto));
        registrar(Tela.LOGIN, new TelaLogin(contexto, this::concluirLogin));
        registrar(Tela.CADASTRO, new TelaCadastro(contexto));
        registrar(Tela.CADASTRO_SUCESSO, new TelaCadastroSucesso(contexto));
        registrar(Tela.PERFIL, new TelaPerfil(contexto));
        registrar(Tela.ALIMENTOS, new TelaAlimentos(contexto));
        registrar(Tela.REFEICOES, new TelaRefeicoes(contexto));
        registrar(Tela.PLANEJAMENTO, new TelaPlanejamento(contexto));
        registrar(Tela.PROJETO, new TelaProjeto(contexto));
        contexto.conectar(this::navegar, this::atualizarTextos, this::limparDados);
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosed(WindowEvent evento) {
                contexto.sessao().sair();
                limparDados();
            }
        });
        atualizarTextos();
        navegar(Tela.INICIO);
    }

    private JPanel cabecalho() {
        JPanel topo = Visual.painel(new BorderLayout(24, 12));
        topo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Visual.LINHA), new EmptyBorder(10, 28, 10, 28)));
        JLabel marca = Visual.titulo(26);
        marca.setText("LimeFit");
        marca.setIcon(Visual.logo(56));
        topo.add(marca, BorderLayout.WEST);
        JPanel acoes = Visual.painel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        identidade.setName("identidade");
        identidade.setPreferredSize(new Dimension(170, 40));
        acoes.add(identidade);
        conta.addActionListener(e -> contexto.navegar(contexto.sessao().autenticada() ? Tela.PERFIL : Tela.LOGIN));
        acoes.add(conta);
        sair.addActionListener(e -> contexto.sair());
        acoes.add(sair);
        pt.setText("PT"); en.setText("EN");
        pt.addActionListener(e -> contexto.mudarIdioma(false));
        en.addActionListener(e -> contexto.mudarIdioma(true));
        acoes.add(pt); acoes.add(en);
        topo.add(acoes, BorderLayout.CENTER);
        JPanel links = Visual.painel(new GridLayout(1, 6, 8, 0));
        for (Tela tela : new Tela[]{Tela.INICIO, Tela.PERFIL, Tela.ALIMENTOS, Tela.REFEICOES, Tela.PLANEJAMENTO, Tela.PROJETO}) {
            JButton botao = Visual.botao("menu." + tela.name(), false);
            botao.addActionListener(e -> contexto.navegar(tela));
            menu.put(tela, botao);
            links.add(botao);
        }
        topo.add(links, BorderLayout.SOUTH);
        return topo;
    }
    private void registrar(Tela tela, PainelTela painel) {
        telas.put(tela, painel);
        paginas.add(painel, tela.name());
    }
    private void navegar(Tela destino) {
        if (destino == Tela.INICIO && !contexto.sessao().autenticada()) aposLogin = Tela.INICIO;
        if (destino.privada() && !contexto.sessao().autenticada()) {
            aposLogin = destino;
            destino = Tela.LOGIN;
        }
        if (contexto.sessao().autenticada() && (destino == Tela.LOGIN || destino == Tela.CADASTRO))
            destino = Tela.PERFIL;
        if (atual != null && atual != destino) telas.get(atual).aoOcultar();
        atual = destino;
        cards.show(paginas, destino.name());
        telas.get(destino).aoExibir();
        atualizarTextos();
    }
    private void concluirLogin() {
        Tela destino = aposLogin;
        aposLogin = Tela.INICIO;
        navegar(destino);
    }
    private void limparDados() {
        telas.values().forEach(PainelTela::limparDados);
    }
    private void atualizarTextos() {
        var idioma = contexto.idioma();
        telas.values().forEach(PainelTela::atualizarTextos);
        menu.forEach((tela, botao) -> {
            botao.setText(tela.titulo(idioma));
            botao.setForeground(atual == tela ? Visual.LIMA : Visual.SUAVE);
        });
        boolean conectada = contexto.sessao().autenticada();
        identidade.setText(conectada ? contexto.sessao().usuario().nome() : "");
        identidade.setToolTipText(conectada ? contexto.sessao().usuario().nome() : null);
        conta.setText(conectada ? Tela.PERFIL.titulo(idioma) : Tela.LOGIN.titulo(idioma));
        sair.setVisible(conectada);
        sair.setText(idioma.escolher("Sair", "Log out"));
        pt.setForeground(idioma.ingles() ? Visual.SUAVE : Visual.LIMA);
        en.setForeground(idioma.ingles() ? Visual.LIMA : Visual.SUAVE);
        rodape.setText(idioma.escolher("LimeFit  •  Projeto acadêmico de POO e Inglês", "LimeFit  •  An academic project in OOP and English"));
        setTitle("LimeFit — " + (atual == null ? "" : atual.titulo(idioma)));
        revalidate();
        repaint();
    }
    public Tela telaAtual() { return atual; }
}
