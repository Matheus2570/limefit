package br.com.limefit.modulos.planejamento;

import br.com.limefit.desktop.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.text.NumberFormat;
import java.util.Locale;

/** Consulta diária com navegação por data e totais de nutrientes. */
public final class TelaPlanejamento extends PainelModulo {
    private final JLabel rotuloData=Visual.rotulo(), tituloTabela=Visual.titulo(20), status=Visual.rotulo();
    private final JTextField campoData=Visual.campo(new JTextField(10),"planejamento.data",10);
    private final JButton anterior=Visual.botao("planejamento.anterior",false);
    private final JButton hoje=Visual.botao("planejamento.hoje",false);
    private final JButton proximo=Visual.botao("planejamento.proximo",false);
    private final JButton consultar=Visual.botao("planejamento.consultar",true);
    private final JLabel[] nomes=new JLabel[4], valores=new JLabel[4];
    private final DefaultTableModel modelo=new DefaultTableModel(new Object[]{"#",""},0) {
        @Override public boolean isCellEditable(int linha,int coluna) { return false; }
    };
    private final JTable tabela=new JTable(modelo);
    private LocalDate dataAtual=LocalDate.now();
    private PlanoDiario plano;
    private SwingWorker<?,?> trabalho;
    private String avisoPt="",avisoEn="";
    private boolean erro;

    public TelaPlanejamento(ContextoAplicacao contexto) {
        super(contexto,Tela.PLANEJAMENTO);
        JPanel raiz=Visual.painel(new BorderLayout(0,18));
        JPanel barra=Visual.painel(new FlowLayout(FlowLayout.LEFT,8,0));
        rotuloData.setLabelFor(campoData);
        barra.add(rotuloData); barra.add(anterior); barra.add(campoData); barra.add(proximo); barra.add(hoje); barra.add(consultar);
        raiz.add(barra,BorderLayout.NORTH);
        JPanel centro=Visual.painel(new BorderLayout(0,10));
        centro.add(tituloTabela,BorderLayout.NORTH);
        tabela.setName("planejamento.tabela"); tabela.setRowHeight(34); tabela.setFillsViewportHeight(true);
        tabela.setBackground(Visual.PAINEL); tabela.setForeground(Visual.TEXTO); tabela.setGridColor(Visual.LINHA);
        tabela.getTableHeader().setBackground(Visual.PAINEL); tabela.getTableHeader().setForeground(Visual.SUAVE);
        tabela.getTableHeader().setReorderingAllowed(false);
        tabela.getColumnModel().getColumn(0).setMaxWidth(60);
        JScrollPane lista=Visual.rolagem(tabela); lista.setPreferredSize(new Dimension(560,240));
        centro.add(lista,BorderLayout.CENTER); raiz.add(centro,BorderLayout.CENTER);
        JPanel sul=Visual.painel(new BorderLayout(0,14));
        JPanel grade=Visual.painel(new GridLayout(1,4,12,0));
        for(int i=0;i<4;i++) {
            JPanel cartao=Visual.painel(new GridLayout(2,1,0,8));
            nomes[i]=Visual.rotulo(); valores[i]=Visual.titulo(24); valores[i].setForeground(Visual.LIMA);
            valores[i].setName("planejamento.total."+i);
            cartao.add(nomes[i]); cartao.add(valores[i]); grade.add(cartao);
        }
        sul.add(grade,BorderLayout.NORTH); status.setName("planejamento.status"); sul.add(status,BorderLayout.SOUTH);
        raiz.add(sul,BorderLayout.SOUTH); conteudo.add(raiz,BorderLayout.NORTH);
        anterior.addActionListener(e -> mudarDia(-1)); proximo.addActionListener(e -> mudarDia(1));
        hoje.addActionListener(e -> { dataAtual=LocalDate.now(); campoData.setText(formato().format(dataAtual)); carregar(); });
        consultar.addActionListener(e -> carregar()); campoData.addActionListener(e -> carregar());
        atualizarTextos(); limparValores();
    }
    private DateTimeFormatter formato() {
        return DateTimeFormatter.ofPattern(idioma.escolher("dd/MM/uuuu","MM/dd/uuuu")).withResolverStyle(ResolverStyle.STRICT);
    }
    private LocalDate lerData() { return LocalDate.parse(campoData.getText().strip(),formato()); }
    private void mudarDia(int delta) {
        try { dataAtual=lerData().plusDays(delta); campoData.setText(formato().format(dataAtual)); carregar(); }
        catch(java.time.DateTimeException e) { dataInvalida(); }
    }
    private void dataInvalida() {
        if(trabalho!=null) trabalho.cancel(false);
        trabalho=null; plano=null; limparValores();
        informar("Data inválida. Use dd/mm/aaaa.","Invalid date. Use mm/dd/yyyy.",true);
    }
    private void carregar() {
        final LocalDate data;
        try { data=lerData(); if(data.getYear()<1000||data.getYear()>9999) { dataInvalida(); return; } }
        catch(DateTimeParseException e) { dataInvalida(); return; }
        dataAtual=data;
        if(trabalho!=null) trabalho.cancel(false);
        plano=null; limparValores(); informar("Carregando…","Loading…",false);
        trabalho=executarNaConta(s -> new PlanejamentoDao(contexto.conexao(),s).buscarPorData(data), resultado -> {
            trabalho=null; plano=resultado; mostrar();
            informar(plano.refeicoes().isEmpty()?"Nenhuma refeição planejada para esta data.":"Planejamento carregado.",
                    plano.refeicoes().isEmpty()?"No meals planned for this date.":"Daily plan loaded.",false);
        }, falha -> {
            trabalho=null; plano=null; limparValores();
            informar("Não foi possível consultar o planejamento. Confira o MySQL e tente novamente.",
                    "Could not load the daily plan. Check MySQL and try again.",true);
        });
    }
    private void mostrar() {
        modelo.setRowCount(0);
        if(plano==null) return;
        int n=1;
        for(var refeicao:plano.refeicoes()) modelo.addRow(new Object[]{n++,refeicao.nome()});
        NumberFormat nf=NumberFormat.getNumberInstance(Locale.forLanguageTag(idioma.escolher("pt-BR","en-US")));
        nf.setMinimumFractionDigits(2); nf.setMaximumFractionDigits(2); nf.setRoundingMode(java.math.RoundingMode.HALF_UP);
        var t=plano.totais();
        java.math.BigDecimal[] totais={t.calorias(),t.proteinas(),t.carboidratos(),t.gorduras()};
        for(int i=0;i<4;i++) valores[i].setText(nf.format(totais[i]));
    }
    private void limparValores() { modelo.setRowCount(0); for(JLabel v:valores) v.setText("—"); }
    private void informar(String pt,String en,boolean falha) {
        avisoPt=pt; avisoEn=en; erro=falha; status.setText(idioma.escolher(pt,en)); status.setForeground(falha?Visual.ERRO:Visual.SUAVE);
    }
    @Override public void atualizarTextos() {
        super.atualizarTextos();
        rotuloData.setText(idioma.escolher("Data","Date")); anterior.setText("<"); proximo.setText(">");
        anterior.setToolTipText(idioma.escolher("Dia anterior","Previous day")); proximo.setToolTipText(idioma.escolher("Próximo dia","Next day"));
        hoje.setText(idioma.escolher("Hoje","Today")); consultar.setText(idioma.escolher("Consultar","Search"));
        campoData.setToolTipText(idioma.escolher("dd/mm/aaaa","mm/dd/yyyy"));
        if(!campoData.getText().isBlank()) campoData.setText(formato().format(dataAtual));
        tituloTabela.setText(idioma.escolher("Refeições do dia","Meals of the day"));
        tabela.getColumnModel().getColumn(1).setHeaderValue(idioma.escolher("Refeição","Meal")); tabela.getTableHeader().repaint();
        nomes[0].setText(idioma.escolher("Calorias (kcal)","Calories (kcal)"));
        nomes[1].setText(idioma.escolher("Proteínas (g)","Protein (g)"));
        nomes[2].setText(idioma.escolher("Carboidratos (g)","Carbohydrates (g)"));
        nomes[3].setText(idioma.escolher("Gorduras (g)","Fat (g)"));
        mostrar(); informar(avisoPt,avisoEn,erro);
    }
    @Override public void aoExibir() { super.aoExibir(); campoData.setText(formato().format(dataAtual)); carregar(); }
    @Override public void limparDados() {
        if(trabalho!=null) trabalho.cancel(false);
        trabalho=null; plano=null; dataAtual=LocalDate.now(); campoData.setText(""); limparValores(); informar("","",false);
    }
}
