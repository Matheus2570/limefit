package br.com.limefit.modulos.perfil;

import br.com.limefit.desktop.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.util.function.Consumer;

/** Cadastro, consulta e exclusão do perfil corporal da conta. */
public final class TelaPerfil extends PainelModulo {
    private final JTextField peso = Visual.campo(new JTextField(), "perfil.peso", 8);
    private final JTextField altura = Visual.campo(new JTextField(), "perfil.altura", 8);
    private final JComboBox<Objetivo> objetivo = new JComboBox<>(Objetivo.values());
    private final JLabel nomeUsuario = Visual.titulo(18);
    private final JLabel subtitulo = Visual.rotulo();
    private final JLabel[] rotulos = {Visual.rotulo(), Visual.rotulo(), Visual.rotulo()};
    private final JLabel tituloImc = Visual.titulo(20);
    private final JLabel imc = Visual.titulo(30);
    private final JLabel legendaImc = Visual.rotulo();
    private final JLabel status = Visual.rotulo();
    private final JButton salvar = Visual.botao("perfil.salvar", true);
    private final JButton limpar = Visual.botao("perfil.limpar", false);
    private final JButton excluir = Visual.botao("perfil.excluir", false);
    private SwingWorker<?, ?> trabalho;
    private boolean existe;
    private String avisoPt = "", avisoEn = "";
    private boolean erro;

    public TelaPerfil(ContextoAplicacao contexto) {
        super(contexto, Tela.PERFIL);
        JPanel raiz = Visual.painel(new BorderLayout(0, 18));
        JPanel topo = Visual.painel(new GridLayout(0, 1, 0, 8));
        topo.add(subtitulo); topo.add(nomeUsuario);
        raiz.add(topo, BorderLayout.NORTH);
        JPanel formulario = Visual.painel(new GridBagLayout());
        formulario.setBorder(new EmptyBorder(16, 0, 16, 0));
        GridBagConstraints c = new GridBagConstraints();
        c.gridx=0; c.weightx=1; c.fill=GridBagConstraints.HORIZONTAL;
        c.insets=new Insets(0,0,14,12);
        JComponent[] campos = {peso, altura, objetivo};
        objetivo.setName("perfil.objetivo");
        objetivo.setBackground(Visual.PAINEL); objetivo.setForeground(Visual.TEXTO);
        objetivo.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> lista, Object valor, int indice, boolean selecionado, boolean foco) {
                return super.getListCellRendererComponent(lista, valor instanceof Objetivo o ? nomeObjetivo(o) : "", indice, selecionado, foco);
            }
        });
        for (int i=0;i<3;i++) {
            c.gridx=i; c.gridy=0; rotulos[i].setLabelFor(campos[i]); formulario.add(rotulos[i],c);
            c.gridy=1; formulario.add(campos[i],c);
        }
        objetivo.setPreferredSize(new Dimension(220,peso.getPreferredSize().height));
        c.gridx=0; c.gridwidth=3;
        c.gridy=2; formulario.add(tituloImc,c);
        c.gridy++; imc.setName("perfil.imc"); imc.setForeground(Visual.LIMA); formulario.add(imc,c);
        c.gridy++; formulario.add(legendaImc,c);
        JPanel acoes=Visual.painel(new FlowLayout(FlowLayout.LEFT,8,0));
        acoes.add(salvar); acoes.add(limpar); acoes.add(excluir);
        c.gridy++; formulario.add(acoes,c);
        c.gridy++; status.setName("perfil.status"); formulario.add(status,c);
        raiz.add(formulario,BorderLayout.CENTER);
        conteudo.add(raiz,BorderLayout.NORTH);
        javax.swing.event.DocumentListener preview=new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { atualizarImc(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { atualizarImc(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { atualizarImc(); }
        };
        peso.getDocument().addDocumentListener(preview); altura.getDocument().addDocumentListener(preview);
        salvar.addActionListener(e -> salvar());
        limpar.addActionListener(e -> { limparFormulario(); informar("", "", false); });
        excluir.addActionListener(e -> excluir());
        atualizarTextos(); controles();
    }

    private String nomeObjetivo(Objetivo o) {
        return switch(o) {
            case GANHAR_MASSA -> idioma.escolher("Ganhar massa muscular", "Gain muscle");
            case PERDER_PESO -> idioma.escolher("Perder peso", "Lose weight");
            case EQUILIBRAR -> idioma.escolher("Equilibrar alimentação", "Balance nutrition");
            case MANTER -> idioma.escolher("Manter peso", "Maintain weight");
        };
    }
    @Override public void atualizarTextos() {
        super.atualizarTextos();
        nomeUsuario.setText(contexto.sessao().autenticada()?contexto.sessao().usuario().nome():"");
        subtitulo.setText(idioma.escolher("Configure seu peso, altura e objetivo.", "Set your weight, height and goal."));
        rotulos[0].setText(idioma.escolher("Peso (kg)", "Weight (kg)"));
        rotulos[1].setText(idioma.escolher("Altura (cm)", "Height (cm)"));
        rotulos[2].setText(idioma.escolher("Objetivo", "Goal"));
        tituloImc.setText(idioma.escolher("Índice de Massa Corporal (IMC)", "Body Mass Index (BMI)"));
        salvar.setText(idioma.escolher("Salvar perfil", "Save profile"));
        limpar.setText(idioma.escolher("Limpar campos", "Clear fields"));
        excluir.setText(idioma.escolher("Excluir perfil", "Delete profile"));
        objetivo.repaint(); atualizarImc(); informar(avisoPt,avisoEn,erro);
    }
    @Override public void aoExibir() {
        super.aoExibir();
        if (trabalho!=null) return;
        executar(s -> new PerfilDao(contexto.conexao(),s).buscarAtual(), perfil -> {
            existe=perfil.isPresent();
            if (existe) { preencher(perfil.orElseThrow()); informar("Perfil carregado.","Profile loaded.",false); }
            else { limparFormulario(); informar("Nenhum perfil cadastrado. Preencha e salve.","No profile yet. Fill in the form and save.",false); }
        });
    }
    private void salvar() {
        final BigDecimal p,a;
        try {
            p=decimal(peso.getText()); a=decimal(altura.getText());
            PerfilDao.validarMedida(p,25,300); PerfilDao.validarMedida(a,100,230);
        } catch (IllegalArgumentException e) {
            informar("Informe peso de 25 a 300 kg e altura de 100 a 230 cm, com até duas casas decimais.",
                    "Enter weight from 25 to 300 kg and height from 100 to 230 cm, with up to two decimal places.",true); return;
        }
        Objetivo o=(Objetivo)objetivo.getSelectedItem();
        executar(s -> new PerfilDao(contexto.conexao(),s).salvar(p,a,o), perfil -> {
            existe=true; preencher(perfil); informar("Perfil salvo com sucesso.","Profile saved successfully.",false);
        });
    }
    private void excluir() {
        if (!existe || trabalho!=null) return;
        if (JOptionPane.showConfirmDialog(this,idioma.escolher(
                "Excluir seu perfil? Refeições e itens associados também serão excluídos.",
                "Delete your profile? Related meals and meal items will also be deleted."),
                excluir.getText(),JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE)!=JOptionPane.YES_OPTION) return;
        executar(s -> new PerfilDao(contexto.conexao(),s).excluirAtual(), removido -> {
            existe=false; limparFormulario(); informar("Perfil excluído.","Profile deleted.",false);
        });
    }
    private <T> void executar(OperacaoDaConta<T> op, Consumer<T> concluir) {
        if (trabalho!=null) return;
        informar("Carregando…","Loading…",false);
        trabalho=executarNaConta(op, valor -> {
            trabalho=null; concluir.accept(valor); controles();
        }, falha -> {
            trabalho=null;
            informar("Não foi possível acessar o banco. Confira o MySQL do XAMPP e tente novamente.",
                    "Could not access the database. Check XAMPP MySQL and try again.",true);
            controles();
        });
        controles();
    }
    private void controles() {
        boolean livre=trabalho==null;
        peso.setEnabled(livre); altura.setEnabled(livre); objetivo.setEnabled(livre);
        salvar.setEnabled(livre); limpar.setEnabled(livre); excluir.setEnabled(livre&&existe);
    }
    private void preencher(Perfil p) {
        peso.setText(p.pesoKg().toPlainString()); altura.setText(p.alturaCm().toPlainString()); objetivo.setSelectedItem(p.objetivo());
    }
    private static BigDecimal decimal(String texto) {
        String n=texto.strip().replace(',','.');
        if (!n.matches("[0-9]+(?:[.][0-9]{1,2})?")) throw new NumberFormatException();
        return new BigDecimal(n);
    }
    private void atualizarImc() {
        try {
            BigDecimal p=decimal(peso.getText()),a=decimal(altura.getText());
            PerfilDao.validarMedida(p,25,300); PerfilDao.validarMedida(a,100,230);
            String valor=new Perfil(0,0,"",p,a,Objetivo.MANTER).calcularImc().toPlainString();
            imc.setText(idioma.escolher(valor.replace('.',','),valor));
            legendaImc.setText(idioma.escolher("Valor calculado com as medidas informadas.","Calculated from the entered measurements."));
        } catch (RuntimeException e) {
            imc.setText("—"); legendaImc.setText(idioma.escolher("Informe peso e altura válidos para calcular.","Enter valid weight and height to calculate."));
        }
    }
    private void informar(String pt,String en,boolean falha) {
        avisoPt=pt; avisoEn=en; erro=falha;
        status.setText(idioma.escolher(pt,en)); status.setForeground(falha?Visual.ERRO:Visual.SUAVE);
    }
    private void limparFormulario() { peso.setText(""); altura.setText(""); objetivo.setSelectedItem(Objetivo.MANTER); }
    @Override public void limparDados() {
        if (trabalho!=null) trabalho.cancel(false);
        trabalho=null; existe=false; limparFormulario(); nomeUsuario.setText(""); informar("","",false); controles();
    }
}
