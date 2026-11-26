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
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/** Tela de organização das refeições diárias da conta autenticada. */
public final class TelaRefeicoes extends PainelModulo {
    private final JLabel rotuloData=Visual.rotulo();
    private final JLabel tituloLista=Visual.titulo(19), tituloDetalhes=Visual.titulo(19);
    private final JButton editar=Visual.botao("refeicoes.editar",false);
    private final JButton novo=Visual.botao("refeicoes.novo",false);
    private SwingWorker<?,?> trabalho;
    private java.util.List<DetalheItem> itens=java.util.List.of();
    private String avisoPt="",avisoEn="";
    private boolean erro;
    private boolean temPerfil;

    private final JTextField campoData = Visual.campo(new JTextField(10), "refeicoes.data", 10);
    private final JTextField campoNome = Visual.campo(new JTextField(18), "refeicoes.nome", 100);
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


    public TelaRefeicoes(ContextoAplicacao contexto) {
        super(contexto, Tela.REFEICOES);
        montarInterface();
        atualizarTextos(); controles();
    }

    private void montarInterface() {
        JPanel raiz = Visual.painel(new BorderLayout(0, 18));
        raiz.setBorder(new EmptyBorder(0, 0, 12, 0));

        JPanel topo = Visual.painel(new BorderLayout(12, 8));
        JPanel filtros = Visual.painel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        rotuloData.setLabelFor(campoData); filtros.add(rotuloData);
        campoData.setText(LocalDate.now().toString());
        filtros.add(campoData);
        atualizar.setText("Atualizar");
        filtros.add(atualizar);
        topo.add(filtros, BorderLayout.WEST);
        resumo.setText("0 refeições");
        topo.add(resumo, BorderLayout.EAST);
        raiz.add(topo, BorderLayout.NORTH);

        JPanel corpo = Visual.painel(new GridLayout(1,2,14,14));
        corpo.add(painelLista()); corpo.add(painelDetalhes());
        corpo.setPreferredSize(new Dimension(800,380));
        corpo.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override public void componentResized(java.awt.event.ComponentEvent e) {
                GridLayout layout=(GridLayout)corpo.getLayout();
                int linhas=corpo.getWidth()<1000?2:1;
                if(layout.getRows()!=linhas) {
                    layout.setRows(linhas); layout.setColumns(linhas==2?1:2);
                    corpo.setPreferredSize(new Dimension(800,linhas==2?740:380)); corpo.revalidate();
                }
            }
        });
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
        editar.addActionListener(e -> editarRefeicao());
        novo.addActionListener(e -> { listaRefeicoes.clearSelection(); campoNome.setText(""); });
        tabelaItens.getSelectionModel().addListSelectionListener(e -> controles());
    }

    private JPanel painelLista() {
        JPanel painel = cartao(new BorderLayout(8, 12));
        JPanel cabecalho = Visual.painel(new BorderLayout(4, 8));
        cabecalho.add(tituloLista, BorderLayout.NORTH);
        JPanel formularioNovo = Visual.painel(new BorderLayout(6, 0));
        formularioNovo.add(campoNome, BorderLayout.CENTER);
        formularioNovo.add(adicionar, BorderLayout.EAST);
        cabecalho.add(formularioNovo, BorderLayout.SOUTH);
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
        rodape.setLayout(new GridLayout(0,1,0,6));
        rodape.add(novo); rodape.add(editar); rodape.add(excluir);
        painel.add(rodape, BorderLayout.SOUTH);
        return painel;
    }

    private JPanel painelDetalhes() {
        JPanel painel = cartao(new BorderLayout(8, 12));
        JPanel cabecalho = Visual.painel(new BorderLayout());
        cabecalho.add(tituloDetalhes, BorderLayout.WEST);
        painel.add(cabecalho, BorderLayout.NORTH);
        listaRefeicoes.setName("refeicoes.lista");
        seletorAlimento.setName("refeicoes.alimento");
        tabelaItens.setName("refeicoes.itens");
        mensagem.setName("refeicoes.status");
        tabelaItens.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabelaItens.getTableHeader().setReorderingAllowed(false);
        tabelaItens.setRowHeight(34);
        tabelaItens.setFillsViewportHeight(true);
        tabelaItens.setBackground(Visual.PAINEL);
        tabelaItens.setForeground(Visual.TEXTO);
        tabelaItens.setGridColor(Visual.LINHA);
        tabelaItens.setSelectionBackground(new Color(0x34472A));
        tabelaItens.getTableHeader().setBackground(new Color(0x202B23));
        tabelaItens.getTableHeader().setForeground(Visual.SUAVE);
        painel.add(Visual.rolagem(tabelaItens), BorderLayout.CENTER);

        JPanel adicionarItem = Visual.painel(new GridLayout(0,1,0,8));
        seletorAlimento.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> lista, Object valor, int indice,
                                                                     boolean selecionado, boolean foco) {
                String nome = valor instanceof Alimento alimento ? alimento.nome() : "";
                Component componente=super.getListCellRendererComponent(lista, nome, indice, selecionado, foco);
                componente.setBackground(selecionado?new Color(0x34472A):Visual.PAINEL);
                componente.setForeground(Visual.TEXTO);
                return componente;
            }
        });
        seletorAlimento.setPreferredSize(new Dimension(190, 38));
        seletorAlimento.setBackground(Visual.PAINEL); seletorAlimento.setForeground(Visual.TEXTO);
        adicionarItem.add(seletorAlimento);
        JPanel porcao=Visual.painel(new BorderLayout(8,0));
        porcao.add(campoGramas,BorderLayout.CENTER);
        JLabel unidade=Visual.rotulo(); unidade.setText("g"); porcao.add(unidade,BorderLayout.EAST);
        adicionarItem.add(porcao);
        JPanel acoes=Visual.painel(new GridLayout(1,2,8,0)); acoes.add(incluirItem); acoes.add(removerItem);
        adicionarItem.add(acoes);
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

    private record DadosDia(List<Refeicao> refeicoes,List<Alimento> alimentos,boolean temPerfil) {}
    private record DetalheItem(ItemRefeicao item,Alimento alimento) {}

    @Override public void aoExibir() { super.aoExibir(); carregarRefeicoes(); }

    private LocalDate dataSelecionada() {
        LocalDate data=LocalDate.parse(campoData.getText().strip());
        if(data.getYear()<1000||data.getYear()>9999) throw new IllegalArgumentException();
        return data;
    }
    private void carregarRefeicoes() {
        if(trabalho!=null) return;
        final LocalDate data;
        try { data=dataSelecionada(); }
        catch(RuntimeException e) { informar("Informe a data no formato AAAA-MM-DD.","Enter the date as YYYY-MM-DD.",true); return; }
        executar(sessao -> {
            RefeicaoDao dao=new RefeicaoDao(contexto.conexao(),sessao);
            return new DadosDia(dao.listarPorData(data),new AlimentoDao(contexto.conexao(),sessao).listar(""),
                    new PerfilDao(contexto.conexao(),sessao).buscarAtual().isPresent());
        }, dados -> {
            listaRefeicoes.clearSelection(); modeloRefeicoes.clear(); dados.refeicoes().forEach(modeloRefeicoes::addElement);
            seletorAlimento.removeAllItems(); dados.alimentos().forEach(seletorAlimento::addItem);
            temPerfil=dados.temPerfil(); selecionada=null; limparItens(); campoNome.setText(""); atualizarResumo();
            if(!temPerfil) informar("Complete seu perfil na tela Perfil antes de criar refeições.","Complete your profile on the Profile screen before adding meals.",false);
            else if(dados.alimentos().isEmpty()) informar("Cadastre alimentos na tela Alimentos para montar suas refeições.","Add foods on the Foods screen to build your meals.",false);
            else informar("","",false);
        });
    }
    private void selecionarRefeicao(Refeicao refeicao) {
        if(trabalho!=null) return;
        selecionada=refeicao; limparItens();
        if(refeicao==null) { campoNome.setText(""); controles(); return; }
        campoNome.setText(refeicao.nome());
        executar(sessao -> {
            RefeicaoDao dao=new RefeicaoDao(contexto.conexao(),sessao);
            AlimentoDao alimentos=new AlimentoDao(contexto.conexao(),sessao);
            List<DetalheItem> resultado=new java.util.ArrayList<>();
            for(ItemRefeicao item:dao.listarItens(refeicao.id())) {
                Optional<Alimento> alimento=alimentos.buscarPorId(item.alimentoId());
                if(alimento.isPresent()) resultado.add(new DetalheItem(item,alimento.orElseThrow()));
            }
            return resultado;
        }, dados -> { itens=List.copyOf(dados); mostrarItens(); informar("","",false); });
    }
    private void criarRefeicao() { gravarRefeicao(false); }
    private void editarRefeicao() { gravarRefeicao(true); }
    private void gravarRefeicao(boolean edicao) {
        if(trabalho!=null || (edicao&&selecionada==null)) return;
        String nome=campoNome.getText().strip();
        if(nome.isEmpty()||nome.length()>100) { informar("Informe um nome de 1 a 100 caracteres.","Enter a name from 1 to 100 characters.",true); return; }
        final LocalDate data;
        try { data=dataSelecionada(); }
        catch(RuntimeException e) { informar("Informe a data no formato AAAA-MM-DD.","Enter the date as YYYY-MM-DD.",true); return; }
        Refeicao alvo=selecionada;
        executar(sessao -> {
            RefeicaoDao dao=new RefeicaoDao(contexto.conexao(),sessao);
            if(edicao) return dao.atualizar(alvo.id(),data,nome);
            Perfil perfil=new PerfilDao(contexto.conexao(),sessao).buscarAtual().orElseThrow(() -> new IllegalArgumentException("profile"));
            dao.cadastrar(perfil.id(),data,nome); return true;
        }, salvo -> carregarRefeicoes());
    }
    private void excluirRefeicao() {
        if(selecionada==null||trabalho!=null) return;
        if(JOptionPane.showConfirmDialog(this,idioma.escolher("Excluir a refeição e todos os seus itens?","Delete the meal and all its items?"),
                excluir.getText(),JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION) return;
        Refeicao alvo=selecionada;
        executar(s -> new RefeicaoDao(contexto.conexao(),s).excluir(alvo.id()), removido -> carregarRefeicoes());
    }
    private void incluirAlimento() {
        if(trabalho!=null||selecionada==null) return;
        Alimento alimento=(Alimento)seletorAlimento.getSelectedItem();
        if(alimento==null) return;
        final BigDecimal gramas;
        try {
            String texto=campoGramas.getText().strip().replace(',','.');
            if(!texto.matches("[0-9]+(?:[.][0-9]{1,2})?")) throw new NumberFormatException();
            gramas=new BigDecimal(texto);
            if(gramas.signum()<=0||gramas.compareTo(new BigDecimal("10000"))>0) throw new NumberFormatException();
        } catch(NumberFormatException e) {
            informar("Informe uma porção maior que 0 e até 10000 g, com até duas casas decimais.",
                    "Enter a portion greater than 0 and up to 10000 g, with up to two decimal places.",true); return;
        }
        Refeicao alvo=selecionada;
        executar(s -> { new RefeicaoDao(contexto.conexao(),s).definirPorcao(alvo.id(),alimento.id(),gramas); return true; },
                salvo -> selecionarRefeicao(alvo));
    }
    private void removerAlimento() {
        int linha=tabelaItens.getSelectedRow();
        if(trabalho!=null||selecionada==null||linha<0) return;
        ItemRefeicao item=itens.get(tabelaItens.convertRowIndexToModel(linha)).item();
        Refeicao alvo=selecionada;
        executar(s -> new RefeicaoDao(contexto.conexao(),s).removerItem(alvo.id(),item.alimentoId()),
                removido -> selecionarRefeicao(alvo));
    }
    private <T> void executar(OperacaoDaConta<T> op,java.util.function.Consumer<T> concluir) {
        if(trabalho!=null) return;
        informar("Carregando…","Loading…",false);
        trabalho=executarNaConta(op,valor -> { trabalho=null; concluir.accept(valor); controles(); },falha -> {
            trabalho=null;
            if(falha instanceof IllegalArgumentException) informar("Complete seu perfil antes de criar refeições.","Complete your profile before adding meals.",true);
            else informar("Não foi possível concluir a operação. Confira o MySQL e atualize os dados.",
                    "Could not complete the operation. Check MySQL and refresh the data.",true);
            controles();
        });
        controles();
    }
    private void controles() {
        boolean livre=trabalho==null;
        campoData.setEnabled(livre); campoNome.setEnabled(livre); atualizar.setEnabled(livre);
        listaRefeicoes.setEnabled(livre); tabelaItens.setEnabled(livre); novo.setEnabled(livre);
        adicionar.setEnabled(livre&&temPerfil); editar.setEnabled(livre&&selecionada!=null); excluir.setEnabled(livre&&selecionada!=null);
        seletorAlimento.setEnabled(livre); campoGramas.setEnabled(livre);
        incluirItem.setEnabled(livre&&selecionada!=null&&seletorAlimento.getItemCount()>0);
        removerItem.setEnabled(livre&&selecionada!=null&&tabelaItens.getSelectedRow()>=0);
    }
    private void limparItens() { itens=List.of(); modeloItens.setRowCount(0); }
    private void mostrarItens() {
        modeloItens.setRowCount(0);
        for(DetalheItem detalhe:itens) {
            Alimento a=detalhe.alimento(); BigDecimal g=detalhe.item().quantidadeG();
            modeloItens.addRow(new Object[]{a.nome(),formatar(g)+" g",formatar(a.calorias().multiply(g).movePointLeft(2)),
                    formatar(a.proteinas().multiply(g).movePointLeft(2))+" g"});
        }
    }
    private String formatar(BigDecimal valor) {
        String n=valor.setScale(2,java.math.RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
        return idioma.escolher(n.replace('.',','),n);
    }
    private void informar(String pt,String en,boolean falha) {
        avisoPt=pt; avisoEn=en; erro=falha; mensagem.setText(idioma.escolher(pt,en)); mensagem.setForeground(falha?Visual.ERRO:Visual.SUAVE);
    }
    private void atualizarResumo() { resumo.setText(modeloRefeicoes.size()+" "+idioma.escolher("refeições","meals")); }
    @Override public void atualizarTextos() {
        super.atualizarTextos(); rotuloData.setText(idioma.escolher("Data (AAAA-MM-DD)","Date (YYYY-MM-DD)"));
        tituloLista.setText(idioma.escolher("Refeições do dia","Meals of the day"));
        tituloDetalhes.setText(idioma.escolher("Alimentos da refeição","Foods in the meal"));
        adicionar.setText(idioma.escolher("+ Criar","+ Add meal")); editar.setText(idioma.escolher("Salvar nome e data","Save name and date"));
        novo.setText(idioma.escolher("Nova refeição","New meal")); excluir.setText(idioma.escolher("Excluir refeição","Delete meal"));
        atualizar.setText(idioma.escolher("Atualizar","Refresh")); incluirItem.setText(idioma.escolher("Definir porção","Set portion"));
        removerItem.setText(idioma.escolher("Remover","Remove"));
        String[] colunas={idioma.escolher("Alimento","Food"),idioma.escolher("Porção","Portion"),
                idioma.escolher("Calorias (kcal)","Calories (kcal)"),idioma.escolher("Proteínas","Protein")};
        for(int i=0;i<4;i++) tabelaItens.getColumnModel().getColumn(i).setHeaderValue(colunas[i]);
        tabelaItens.getTableHeader().repaint(); mostrarItens(); atualizarResumo(); informar(avisoPt,avisoEn,erro);
    }
    @Override public void limparDados() {
        if(trabalho!=null) trabalho.cancel(false);
        trabalho=null; selecionada=null; temPerfil=false; modeloRefeicoes.clear(); limparItens(); seletorAlimento.removeAllItems();
        campoNome.setText(""); campoData.setText(LocalDate.now().toString()); campoGramas.setText("100");
        atualizarResumo(); informar("","",false); controles();
    }
}
