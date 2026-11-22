package br.com.limefit.modulos.inicio;

import br.com.limefit.desktop.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public final class TelaInicio extends PainelTela {
    private final JLabel chamada = Visual.rotulo();
    private final JLabel titulo = Visual.titulo(44);
    private final JLabel destaque = Visual.titulo(44);
    private final JTextArea descricao = Visual.texto(18);
    private final JLabel nota = Visual.rotulo();
    private final JLabel boasVindas = Visual.rotulo();
    private final JButton comecar = Visual.botao("home.comecar", true);
    private final JLabel ideia = Visual.titulo(25);
    private final JTextArea apresentacao = Visual.texto(16);
    private final JButton criar = Visual.botao("home.criar", false);

    public TelaInicio(ContextoAplicacao contexto) {
        super(contexto);
        JPanel pagina = Visual.painelRolavel(new BorderLayout(0, 32));
        pagina.setBorder(new EmptyBorder(36, 40, 26, 40));
        JPanel hero = Visual.painel(new GridLayout(1, 2, 32, 0));
        hero.setName("home.hero");
        JPanel texto = Visual.painel(new GridBagLayout());
        GridBagConstraints linha = new GridBagConstraints();
        linha.gridx = 0; linha.gridy = 0; linha.weightx = 1; linha.fill = GridBagConstraints.HORIZONTAL;
        linha.insets = new Insets(0, 0, 18, 0);
        chamada.setForeground(Visual.LIMA);
        chamada.setFont(Visual.fonte(13, Font.BOLD));
        texto.add(chamada, linha);
        linha.gridy++; linha.insets.bottom = 0;
        texto.add(titulo, linha);
        linha.gridy++; linha.insets.bottom = 22;
        destaque.setForeground(Visual.LIMA);
        texto.add(destaque, linha);
        linha.gridy++;
        descricao.setRows(4);
        texto.add(descricao, linha);
        linha.gridy++; linha.insets.bottom = 10;
        boasVindas.setForeground(Visual.LIMA);
        boasVindas.setName("home.boasVindas");
        texto.add(boasVindas, linha);
        linha.gridy++;
        JPanel acao = Visual.painel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        acao.add(comecar);
        texto.add(acao, linha);
        linha.gridy++; linha.insets.bottom = 0;
        texto.add(nota, linha);
        hero.add(texto);
        hero.add(new Visual.Foto());
        pagina.add(hero, BorderLayout.CENTER);
        JPanel explicacao = Visual.painel(new BorderLayout(0, 14));
        explicacao.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Visual.LINHA), new EmptyBorder(26, 0, 0, 0)));
        explicacao.add(ideia, BorderLayout.NORTH);
        apresentacao.setRows(3);
        explicacao.add(apresentacao, BorderLayout.CENTER);
        JPanel convite = Visual.painel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        convite.add(criar);
        explicacao.add(convite, BorderLayout.SOUTH);
        pagina.add(explicacao, BorderLayout.SOUTH);
        JScrollPane scroll = Visual.rolagem(pagina);
        scroll.setName("home.rolagem");
        scroll.getViewport().addComponentListener(new ComponentAdapter() {
            private boolean empilhado;
            @Override public void componentResized(ComponentEvent evento) {
                boolean estreito = scroll.getViewport().getWidth() < 1000;
                if (estreito != empilhado) {
                    empilhado = estreito;
                    hero.setLayout(estreito ? new GridLayout(2, 1, 0, 24) : new GridLayout(1, 2, 32, 0));
                    pagina.revalidate();
                }
            }
        });
        add(scroll, BorderLayout.CENTER);
        comecar.addActionListener(e -> contexto.navegar(contexto.sessao().autenticada() ? Tela.PERFIL : Tela.LOGIN));
        criar.addActionListener(e -> contexto.navegar(contexto.sessao().autenticada() ? Tela.PERFIL : Tela.CADASTRO));
        atualizarTextos();
    }
    @Override public void atualizarTextos() {
        chamada.setText(idioma.escolher("COMER BEM COMEÇA COM VOCÊ", "EATING WELL STARTS WITH YOU"));
        titulo.setText(idioma.escolher("Sua alimentação,", "Your nutrition,"));
        destaque.setText(idioma.escolher("do seu jeito.", "your way."));
        descricao.setText(idioma.escolher(
                "Um espaço para organizar sua alimentação, conhecer seus nutrientes e planejar refeições com mais clareza.",
                "A place to organize your nutrition, understand your nutrients and plan meals with more clarity."));
        nota.setText(idioma.escolher("Mais organização. Um dia de cada vez.", "More clarity. One day at a time."));
        boolean conectada = contexto.sessao().autenticada();
        boasVindas.setText(conectada ? idioma.escolher("Olá, ", "Hello, ") + contexto.sessao().usuario().nome() : "");
        boasVindas.setToolTipText(boasVindas.getText());
        comecar.setText(conectada ? idioma.escolher("Acessar meu perfil", "Go to my profile") : idioma.escolher("Começar agora", "Get started"));
        ideia.setText(idioma.escolher("Um pouco de planejamento. Mais espaço para viver.", "A little planning. More room for life."));
        apresentacao.setText(idioma.escolher(
                "O LimeFit é um projeto acadêmico de POO e Inglês que conecta perfil, alimentos, refeições e planejamento em um só lugar.\nComece criando sua conta. Os módulos estão sendo desenvolvidos pela nossa equipe.",
                "LimeFit is an academic project in OOP and English that brings profile, foods, meals and planning together in one place.\nStart by creating your account. Our team is building the modules."));
        criar.setText(conectada ? Tela.PERFIL.titulo(idioma) : idioma.escolher("Criar minha conta", "Create my account"));
    }
    @Override public void limparDados() { boasVindas.setText(""); }
}
