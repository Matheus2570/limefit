package br.com.limefit.modulos.refeicoes;

import br.com.limefit.desktop.ContextoAplicacao;
import br.com.limefit.desktop.PainelModulo;
import br.com.limefit.desktop.Tela;
import br.com.limefit.desktop.Visual;
import br.com.limefit.modulos.alimentos.Alimento;
import br.com.limefit.modulos.alimentos.AlimentoDao;
import br.com.limefit.modulos.perfil.Perfil;
import br.com.limefit.modulos.perfil.PerfilDao;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

/** Tela de organização das refeições diárias da conta autenticada. */
public final class TelaRefeicoes extends PainelModulo {
    private final RefeicaoDao dao;
    private final AlimentoDao alimentos;
    private final PerfilDao perfis;

    private final JTextField campoData = Visual.campo(new JTextField(10), "refeicoes.data", 10);
    private final JTextField campoNome = Visual.campo(new JTextField(18), "refeicoes.nome", 60);
    private final JButton adicionar = Visual.botao("refeicoes.adicionar", true);
    private final JButton excluir = Visual.botao("refeicoes.excluir", false);
    private final JButton atualizar = Visual.botao("refeicoes.atualizar", false);
    private final JLabel mensagem = Visual.rotulo();
    private final JLabel resumo = Visual.titulo(17);
    private final DefaultListModel<Refeicao> modeloRefeicoes = new DefaultListModel<>();
    private final JList<Refeicao> listaRefeicoes = new JList<>(modeloRefeicoes);
    private final DefaultTableModel modeloItens = new DefaultTableModel(new Object[]{"Alimento", "Porção", "Calorias", "Proteínas"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable tabelaItens = new JTable(modeloItens);
    private final JComboBox<Alimento> seletorAlimento = new JComboBox<>();
    private final JTextField campoGramas = Visual.campo(new JTextField("100", 6), "refeicoes.gramas", 8);
    private final JButton incluirItem = Visual.botao("refeicoes.incluirItem", true);
    private final JButton removerItem = Visual.botao("refeicoes.removerItem", false);
    private Refeicao selecionada;
    private long trabalhoAtual;

    public TelaRefeicoes(ContextoAplicacao contexto) {
        super(contexto, Tela.REFEICOES);
        dao = new RefeicaoDao(contexto.conexao(), contexto.sessao());
        alimentos = new AlimentoDao(contexto.conexao(), contexto.sessao());
        perfis = new PerfilDao(contexto.conexao(), contexto.sessao());
        montarInterface();
        atualizarTextos();
    }

    private void montarInterface() {
        JPanel raiz = Visual.painel(new BorderLayout(0, 18));
        raiz.setBorder(new EmptyBorder(0, 0, 12, 0));

        JPanel topo = Visual.painel(new BorderLayout(12, 8));
        JPanel filtros = Visual.painel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filtros.add(rotuloForte("Data / Date"));
        campoData.setText(LocalDate.now().toString());
        filtros.add(campoData);
        atualizar.setText("Atualizar");
        filtros.add(atualizar);
        topo.add(filtros, BorderLayout.WEST);
        resumo.setText("0 refeições");
        topo.add(resumo, BorderLayout.EAST);
        raiz.add(topo, BorderLayout.NORTH);

        JPanel corpo = Visual.painel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(0, 0, 0, 14);
        c.fill = GridBagConstraints.BOTH;
        c.weighty = 1;
        c.gridx = 0; c.gridy = 0; c.weightx = .38;
        corpo.add(painelLista(), c);
        c.gridx = 1; c.weightx = .62; c.insets = new Insets(0, 0, 0, 0);
        corpo.add(painelDetalhes(), c);
        raiz.add(corpo, BorderLayout.CENTER);
        mensagem.setBorder(new EmptyBorder(4, 2, 0, 2));
        raiz.add(mensagem, BorderLayout.SOUTH);
        conteudo.add(raiz, BorderLayout.CENTER);

        adicionar.addActionListener(e -> criarRefeicao());
        excluir.addActionListener(e -> excluirRefeicao());
        atualizar.addActionListener(e -> carregarRefeicoes());
        listaRefeicoes.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) selecionarRefeicao(listaRefeicoes.getSelectedValue());
        });
        incluirItem.addActionListener(e -> incluirAlimento());
        removerItem.addActionListener(e -> removerAlimento());
        campoData.addActionListener(e -> carregarRefeicoes());
    }

    private JPanel painelLista() {
        JPanel painel = cartao(new BorderLayout(8, 12));
        JPanel cabecalho = Visual.painel(new BorderLayout(4, 8));
        cabecalho.add(tituloSecao("Refeições do dia", 19), BorderLayout.NORTH);
        JPanel novo = Visual.painel(new BorderLayout(6, 0));
        novo.add(campoNome, BorderLayout.CENTER);
        novo.add(adicionar, BorderLayout.EAST);
        cabecalho.add(novo, BorderLayout.SOUTH);
        painel.add(cabecalho, BorderLayout.NORTH);
        listaRefeicoes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listaRefeicoes.setBackground(Visual.PAINEL);
        listaRefeicoes.setForeground(Visual.TEXTO);
        listaRefeicoes.setSelectionBackground(new Color(0x34472A));
        listaRefeicoes.setSelectionForeground(Visual.LIMA);
        listaRefeicoes.setFixedCellHeight(48);
        listaRefeicoes.setCellRenderer((list, value, index, selected, focus) -> {
            JLabel label = new JLabel(value == null ? "" : value.nome());
            label.setOpaque(true);
            label.setBorder(new EmptyBorder(8, 12, 8, 12));
            label.setFont(Visual.fonte(15, Font.BOLD));
            label.setBackground(selected ? new Color(0x34472A) : Visual.PAINEL);
            label.setForeground(selected ? Visual.LIMA : Visual.TEXTO);
            return label;
        });
        painel.add(Visual.rolagem(listaRefeicoes), BorderLayout.CENTER);
        JPanel rodape = Visual.painel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        rodape.add(excluir);
        painel.add(rodape, BorderLayout.SOUTH);
        return painel;
    }

    private JPanel painelDetalhes() {
        JPanel painel = cartao(new BorderLayout(8, 12));
        JPanel cabecalho = Visual.painel(new BorderLayout());
        cabecalho.add(tituloSecao("Alimentos da refeição", 19), BorderLayout.WEST);
        painel.add(cabecalho, BorderLayout.NORTH);
        tabelaItens.setRowHeight(34);
        tabelaItens.setFillsViewportHeight(true);
        tabelaItens.setBackground(Visual.PAINEL);
        tabelaItens.setForeground(Visual.TEXTO);
        tabelaItens.setGridColor(Visual.LINHA);
        tabelaItens.setSelectionBackground(new Color(0x34472A));
        tabelaItens.getTableHeader().setBackground(new Color(0x202B23));
        tabelaItens.getTableHeader().setForeground(Visual.SUAVE);
        painel.add(Visual.rolagem(tabelaItens), BorderLayout.CENTER);

        JPanel adicionarItem = Visual.painel(new FlowLayout(FlowLayout.LEFT, 7, 0));
        seletorAlimento.setPreferredSize(new Dimension(190, 38));
        adicionarItem.add(seletorAlimento);
        adicionarItem.add(campoGramas);
        adicionarItem.add(new JLabel("g"));
        adicionarItem.add(incluirItem);
        adicionarItem.add(removerItem);
        painel.add(adicionarItem, BorderLayout.SOUTH);
        return painel;
    }

    private JPanel cartao(LayoutManager layout) {
        JPanel p = Visual.painel(layout);
        p.setBackground(Visual.PAINEL);
        p.setOpaque(true);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Visual.LINHA), new EmptyBorder(16, 16, 16, 16)));
        return p;
    }
    private JLabel rotuloForte(String texto) { JLabel l = new JLabel(texto); l.setForeground(Visual.TEXTO); l.setFont(Visual.fonte(14, Font.BOLD)); return l; }
    private JLabel tituloSecao(String texto, int tamanho) { JLabel l = Visual.titulo(tamanho); l.setText(texto); return l; }

    @Override public void aoExibir() {
        super.aoExibir();
        carregarRefeicoes();
        carregarAlimentos();
    }

    private LocalDate dataSelecionada() {
        try { return LocalDate.parse(campoData.getText().trim()); }
        catch (DateTimeParseException e) { throw new IllegalArgumentException(idioma.escolher("Informe a data no formato AAAA-MM-DD.", "Enter the date as YYYY-MM-DD.")); }
    }

    private void carregarRefeicoes() {
        final LocalDate data;
        try { data = dataSelecionada(); }
        catch (IllegalArgumentException e) { mostrarMensagem(e.getMessage(), true); return; }
        final long revisao = contexto.sessao().revisao();
        final long idTrabalho = ++trabalhoAtual;
        mostrarMensagem(idioma.escolher("Carregando refeições...", "Loading meals..."), false);
        new SwingWorker<List<Refeicao>, Void>() {
            @Override protected List<Refeicao> doInBackground() throws Exception { return dao.listarPorData(data); }
            @Override protected void done() {
                if (idTrabalho != trabalhoAtual || revisao != contexto.sessao().revisao() || !contexto.sessao().autenticada()) return;
                try {
                    List<Refeicao> dados = get();
                    modeloRefeicoes.clear();
                    dados.forEach(modeloRefeicoes::addElement);
                    selecionada = null;
                    limparItens();
                    resumo.setText(dados.size() + " " + idioma.escolher("refeições", "meals"));
                    mostrarMensagem("", false);
                } catch (Exception e) { tratarErro(e); }
            }
        }.execute();
    }

    private void carregarAlimentos() {
        final long revisao = contexto.sessao().revisao();
        new SwingWorker<List<Alimento>, Void>() {
            @Override protected List<Alimento> doInBackground() throws Exception { return alimentos.listar(""); }
            @Override protected void done() {
                if (revisao != contexto.sessao().revisao() || !contexto.sessao().autenticada()) return;
                try {
                    seletorAlimento.removeAllItems();
                    for (Alimento alimento : get()) seletorAlimento.addItem(alimento);
                } catch (Exception e) { tratarErro(e); }
            }
        }.execute();
    }

    private void selecionarRefeicao(Refeicao refeicao) {
        selecionada = refeicao;
        limparItens();
        if (refeicao == null) return;
        final long revisao = contexto.sessao().revisao();
        new SwingWorker<List<ItemRefeicao>, Void>() {
            @Override protected List<ItemRefeicao> doInBackground() throws Exception { return dao.listarItens(refeicao.id()); }
            @Override protected void done() {
                if (revisao != contexto.sessao().revisao() || selecionada != refeicao || !contexto.sessao().autenticada()) return;
                try {
                    for (ItemRefeicao item : get()) {
                        Optional<Alimento> alimento = alimentos.buscarPorId(item.alimentoId());
                        if (alimento.isPresent()) {
                            Alimento a = alimento.get();
                            BigDecimal fator = item.quantidadeG().divide(new BigDecimal("100"), 4, java.math.RoundingMode.HALF_UP);
                            modeloItens.addRow(new Object[]{a.nome(), item.quantidadeG() + " g", formatar(a.calorias().multiply(fator)), formatar(a.proteinas().multiply(fator)) + " g"});
                        }
                    }
                } catch (Exception e) { tratarErro(e); }
            }
        }.execute();
    }

    private void criarRefeicao() {
        String nome = campoNome.getText().trim();
        if (nome.isEmpty()) { mostrarMensagem(idioma.escolher("Digite o nome da refeição.", "Enter a meal name."), true); return; }
        final LocalDate data;
        try { data = dataSelecionada(); } catch (IllegalArgumentException e) { mostrarMensagem(e.getMessage(), true); return; }
        final long revisao = contexto.sessao().revisao();
        new SwingWorker<Refeicao, Void>() {
            @Override protected Refeicao doInBackground() throws Exception {
                Perfil perfil = perfis.buscarAtual().orElseThrow(() -> new IllegalStateException(idioma.escolher("Complete seu perfil antes de cadastrar refeições.", "Complete your profile before adding meals.")));
                return dao.cadastrar(perfil.id(), data, nome);
            }
            @Override protected void done() {
                if (revisao != contexto.sessao().revisao() || !contexto.sessao().autenticada()) return;
                try { get(); campoNome.setText(""); carregarRefeicoes(); }
                catch (Exception e) { tratarErro(e); }
            }
        }.execute();
    }

    private void excluirRefeicao() {
        if (selecionada == null) { mostrarMensagem(idioma.escolher("Selecione uma refeição.", "Select a meal."), true); return; }
        int resposta = JOptionPane.showConfirmDialog(this,
                idioma.escolher("Excluir a refeição selecionada?", "Delete the selected meal?"),
                idioma.escolher("Confirmar exclusão", "Confirm deletion"), JOptionPane.YES_NO_OPTION);
        if (resposta != JOptionPane.YES_OPTION) return;
        Refeicao alvo = selecionada;
        long revisao = contexto.sessao().revisao();
        new SwingWorker<Boolean, Void>() {
            @Override protected Boolean doInBackground() throws Exception { return dao.excluir(alvo.id()); }
            @Override protected void done() {
                if (revisao != contexto.sessao().revisao() || !contexto.sessao().autenticada()) return;
                try { get(); carregarRefeicoes(); } catch (Exception e) { tratarErro(e); }
            }
        }.execute();
    }

    private void incluirAlimento() {
        if (selecionada == null) { mostrarMensagem(idioma.escolher("Selecione uma refeição primeiro.", "Select a meal first."), true); return; }
        Alimento alimento = (Alimento) seletorAlimento.getSelectedItem();
        if (alimento == null) { mostrarMensagem(idioma.escolher("Cadastre alimentos na tela Alimentos.", "Add foods on the Foods screen."), true); return; }
        try {
            BigDecimal gramas = new BigDecimal(campoGramas.getText().trim());
            if (gramas.signum() <= 0 || gramas.compareTo(new BigDecimal("10000")) > 0) throw new NumberFormatException();
            Refeicao alvo = selecionada;
            long revisao = contexto.sessao().revisao();
            new SwingWorker<Void, Void>() {
                @Override protected Void doInBackground() throws Exception { dao.definirPorcao(alvo.id(), alimento.id(), gramas); return null; }
                @Override protected void done() { if (revisao == contexto.sessao().revisao() && contexto.sessao().autenticada()) try { get(); selecionarRefeicao(alvo); } catch (Exception e) { tratarErro(e); } }
            }.execute();
        } catch (NumberFormatException e) { mostrarMensagem(idioma.escolher("Use uma quantidade entre 0 e 10000 gramas.", "Enter an amount between 0 and 10000 grams."), true); }
    }

    private void removerAlimento() {
        if (selecionada == null || tabelaItens.getSelectedRow() < 0) { mostrarMensagem(idioma.escolher("Selecione um alimento da lista.", "Select a food from the list."), true); return; }
        int linha = tabelaItens.convertRowIndexToModel(tabelaItens.getSelectedRow());
        String nome = String.valueOf(modeloItens.getValueAt(linha, 0));
        Alimento alimento = (Alimento) seletorAlimento.getSelectedItem();
        if (alimento == null || !alimento.nome().equals(nome)) {
            try { alimento = alimentos.listar("").stream().filter(a -> a.nome().equals(nome)).findFirst().orElse(null); }
            catch (SQLException e) { mostrarMensagem(e.getMessage(), true); return; }
        }
        if (alimento == null) return;
        Refeicao alvo = selecionada;
        Alimento escolhido = alimento;
        long revisao = contexto.sessao().revisao();
        new SwingWorker<Boolean, Void>() {
            @Override protected Boolean doInBackground() throws Exception { return dao.removerItem(alvo.id(), escolhido.id()); }
            @Override protected void done() { if (revisao == contexto.sessao().revisao() && contexto.sessao().autenticada()) try { get(); selecionarRefeicao(alvo); } catch (Exception e) { tratarErro(e); } }
        }.execute();
    }

    private void limparItens() { modeloItens.setRowCount(0); }
    private String formatar(BigDecimal valor) { return valor.setScale(1, java.math.RoundingMode.HALF_UP).stripTrailingZeros().toPlainString(); }
    private void mostrarMensagem(String texto, boolean erro) { mensagem.setText(texto); mensagem.setForeground(erro ? Visual.ERRO : Visual.SUAVE); }
    private void tratarErro(Exception erro) {
        Throwable causa = erro;
        if (erro instanceof ExecutionException && erro.getCause() != null) causa = erro.getCause();
        String detalhe = causa.getMessage() == null ? causa.getClass().getSimpleName() : causa.getMessage();
        mostrarMensagem(detalhe, true);
    }

    @Override public void atualizarTextos() {
        super.atualizarTextos();
        adicionar.setText(idioma.escolher("+ Criar", "+ Add meal"));
        excluir.setText(idioma.escolher("Excluir refeição", "Delete meal"));
        atualizar.setText(idioma.escolher("Atualizar", "Refresh"));
        incluirItem.setText(idioma.escolher("Adicionar", "Add food"));
        removerItem.setText(idioma.escolher("Remover", "Remove"));
        if (resumo.getText().matches("\\d+ .*")) {
            String quantidade = resumo.getText().split(" ")[0];
            resumo.setText(quantidade + " " + idioma.escolher("refeições", "meals"));
        }
        repaint();
    }

    @Override public void limparDados() {
        trabalhoAtual++;
        modeloRefeicoes.clear();
        limparItens();
        seletorAlimento.removeAllItems();
        selecionada = null;
        campoNome.setText("");
        resumo.setText("0 " + idioma.escolher("refeições", "meals"));
        mostrarMensagem("", false);
    }
}
