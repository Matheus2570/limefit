package br.com.limefit.modulos.alimentos;

import br.com.limefit.autenticacao.Sessao;
import br.com.limefit.desktop.ContextoAplicacao;
import br.com.limefit.desktop.PainelModulo;
import br.com.limefit.desktop.Tela;
import br.com.limefit.desktop.Visual;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

public final class TelaAlimentos extends PainelModulo {
    private final JTextField filtro = Visual.campo(new JTextField(), "alimentos.filtro", 100);
    private final JTextField nome = Visual.campo(new JTextField(27), "alimentos.nome", 100);
    private final JTextField calorias = Visual.campo(new JTextField(), "alimentos.calorias", 16);
    private final JTextField proteinas = Visual.campo(new JTextField(), "alimentos.proteinas", 16);
    private final JTextField carboidratos = Visual.campo(new JTextField(), "alimentos.carboidratos", 16);
    private final JTextField gorduras = Visual.campo(new JTextField(), "alimentos.gorduras", 16);
    private final JTextField[] campos = {nome, calorias, proteinas, carboidratos, gorduras};
    private final JLabel[] rotulos = new JLabel[campos.length];
    private final JLabel rotuloFiltro = Visual.rotulo();
    private final JLabel tituloFormulario = Visual.titulo(19);
    private final JLabel unidades = Visual.rotulo();
    private final JLabel mensagem = Visual.rotulo();
    private final JButton buscar = Visual.botao("alimentos.buscar", false);
    private final JButton novo = Visual.botao("alimentos.novo", false);
    private final JButton salvar = Visual.botao("alimentos.salvar", true);
    private final JButton excluir = Visual.botao("alimentos.excluir", false);

    private final DefaultTableModel modelo = new DefaultTableModel(0, 5) {
        @Override public boolean isCellEditable(int linha, int coluna) { return false; }
        @Override public Class<?> getColumnClass(int coluna) {
            return coluna == 0 ? String.class : BigDecimal.class;
        }
    };
    private final JTable tabela = new JTable(modelo);
    private List<Alimento> alimentos = List.of();
    private Alimento selecionado;
    private SwingWorker<?, ?> trabalho;
    private Mensagem aviso = new Mensagem("", "", false);

    public TelaAlimentos(ContextoAplicacao contexto) {
        super(contexto, Tela.ALIMENTOS);
        JPanel raiz = Visual.painel(new BorderLayout(16, 16));
        JPanel formulario = montarFormulario();
        raiz.add(montarLista(), BorderLayout.CENTER);
        raiz.add(formulario, BorderLayout.SOUTH);
        conteudo.add(raiz, BorderLayout.NORTH);
        raiz.addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent evento) {
                String posicao = raiz.getWidth() >= 960 ? BorderLayout.EAST : BorderLayout.SOUTH;
                if (posicao.equals(((BorderLayout) raiz.getLayout()).getConstraints(formulario))) return;
                raiz.remove(formulario);
                raiz.add(formulario, posicao);
                raiz.revalidate();
            }
        });

        filtro.addActionListener(e -> carregar(null));
        buscar.addActionListener(e -> carregar(null));
        novo.addActionListener(e -> {
            limparFormulario();
            informar(new Mensagem("", "", false));
            nome.requestFocusInWindow();
        });
        salvar.addActionListener(e -> salvar());
        excluir.addActionListener(e -> excluir());
        tabela.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) selecionar();
        });
        atualizarTextos();
        atualizarControles();
    }

    private JPanel montarLista() {
        JPanel painel = cartao();
        JPanel busca = Visual.painel(new BorderLayout(12, 0));
        rotuloFiltro.setLabelFor(filtro);
        busca.add(rotuloFiltro, BorderLayout.WEST);
        busca.add(filtro, BorderLayout.CENTER);
        busca.add(buscar, BorderLayout.EAST);
        painel.add(busca, BorderLayout.NORTH);

        tabela.setName("alimentos.tabela");
        tabela.setRowHeight(34);
        tabela.setFillsViewportHeight(true);
        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabela.setBackground(Visual.PAINEL);
        tabela.setForeground(Visual.TEXTO);
        tabela.setGridColor(Visual.LINHA);
        tabela.setSelectionBackground(new Color(0x34472A));
        tabela.setSelectionForeground(Visual.LIMA);
        tabela.getTableHeader().setBackground(new Color(0x202B23));
        tabela.getTableHeader().setForeground(Visual.SUAVE);
        tabela.getTableHeader().setReorderingAllowed(false);
        var numeros = new DefaultTableCellRenderer() {
            @Override protected void setValue(Object valor) {
                setText(valor instanceof BigDecimal decimal ? formatar(decimal) : "");
            }
        };
        numeros.setHorizontalAlignment(SwingConstants.RIGHT);
        tabela.setDefaultRenderer(BigDecimal.class, numeros);
        JScrollPane rolagem = Visual.rolagem(tabela);
        rolagem.setPreferredSize(new Dimension(460, 230));
        painel.add(rolagem, BorderLayout.CENTER);
        return painel;
    }

    private JPanel montarFormulario() {
        JPanel painel = cartao();
        JPanel cabecalho = Visual.painel(new GridLayout(0, 1, 0, 5));
        cabecalho.add(tituloFormulario);
        cabecalho.add(unidades);
        painel.add(cabecalho, BorderLayout.NORTH);

        JPanel formulario = Visual.painel(new BorderLayout(0, 10));
        formulario.add(campoRotulado(0), BorderLayout.NORTH);
        JPanel nutrientes = Visual.painel(new GridLayout(2, 2, 10, 10));
        for (int i = 1; i < campos.length; i++) nutrientes.add(campoRotulado(i));
        formulario.add(nutrientes, BorderLayout.CENTER);
        painel.add(formulario, BorderLayout.CENTER);

        JPanel rodape = Visual.painel(new BorderLayout(0, 8));
        JPanel acoes = Visual.painel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        acoes.add(salvar);
        acoes.add(novo);
        acoes.add(excluir);
        rodape.add(acoes, BorderLayout.NORTH);
        mensagem.setName("alimentos.mensagem");
        rodape.add(mensagem, BorderLayout.SOUTH);
        painel.add(rodape, BorderLayout.SOUTH);
        return painel;
    }

    private JPanel campoRotulado(int indice) {
        JPanel painel = Visual.painel(new BorderLayout(0, 5));
        JLabel rotulo = Visual.rotulo();
        rotulo.setLabelFor(campos[indice]);
        rotulos[indice] = rotulo;
        painel.add(rotulo, BorderLayout.NORTH);
        painel.add(campos[indice], BorderLayout.CENTER);
        return painel;
    }

    private static JPanel cartao() {
        JPanel painel = new JPanel(new BorderLayout(0, 12));
        painel.setBackground(Visual.PAINEL);
        painel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Visual.LINHA), new EmptyBorder(16, 16, 16, 16)));
        return painel;
    }

    @Override public void atualizarTextos() {
        super.atualizarTextos();
        String[] colunas = {
                idioma.escolher("Nome", "Name"), idioma.escolher("Energia (kcal)", "Energy (kcal)"),
                idioma.escolher("Proteínas (g)", "Protein (g)"), idioma.escolher("Carboidratos (g)", "Carbs (g)"),
                idioma.escolher("Gorduras (g)", "Fat (g)")
        };
        for (int i = 0; i < colunas.length; i++) {
            tabela.getColumnModel().getColumn(i).setHeaderValue(colunas[i]);
            rotulos[i].setText(colunas[i]);
        }
        tabela.getColumnModel().getColumn(0).setPreferredWidth(200);
        tabela.getTableHeader().repaint();
        tabela.repaint();
        rotuloFiltro.setText(idioma.escolher("Filtrar por nome", "Filter by name"));
        unidades.setText(idioma.escolher("Valores nutricionais por 100 g", "Nutrition per 100 g"));
        buscar.setText(idioma.escolher("Buscar", "Search"));
        novo.setText(idioma.escolher("Novo", "New"));
        excluir.setText(idioma.escolher("Excluir", "Delete"));
        atualizarTituloFormulario();
        informar(aviso);
    }

    @Override public void aoExibir() {
        super.aoExibir();
        carregar(null);
    }

    @Override public void limparDados() {
        if (trabalho != null) trabalho.cancel(false);
        trabalho = null;
        filtro.setText("");
        substituirLista(List.of());
        informar(new Mensagem("", "", false));
        atualizarControles();
    }

    private void selecionar() {
        int linha = tabela.getSelectedRow();
        selecionado = linha < 0 ? null : alimentos.get(linha);
        if (selecionado != null) {
            nome.setText(selecionado.nome());
            calorias.setText(formatar(selecionado.calorias()));
            proteinas.setText(formatar(selecionado.proteinas()));
            carboidratos.setText(formatar(selecionado.carboidratos()));
            gorduras.setText(formatar(selecionado.gorduras()));
        } else {
            for (JTextField campo : campos) campo.setText("");
        }
        atualizarTituloFormulario();
        atualizarControles();
    }

    private String formatar(BigDecimal valor) {
        String numero = valor.toPlainString();
        return idioma.escolher(numero.replace('.', ','), numero);
    }

    private void limparFormulario() {
        tabela.clearSelection();
        selecionar();
    }

    private void atualizarTituloFormulario() {
        tituloFormulario.setText(selecionado == null ? idioma.escolher("Novo alimento", "New food")
                : idioma.escolher("Editar alimento", "Edit food"));
        salvar.setText(selecionado == null ? idioma.escolher("Cadastrar", "Add food")
                : idioma.escolher("Salvar alterações", "Save changes"));
    }

    private void atualizarControles() {
        boolean livre = trabalho == null;
        filtro.setEnabled(livre);
        buscar.setEnabled(livre);
        tabela.setEnabled(livre);
        novo.setEnabled(livre);
        salvar.setEnabled(livre);
        excluir.setEnabled(livre && selecionado != null);
        for (JTextField campo : campos) campo.setEnabled(livre);
    }

    private void substituirLista(List<Alimento> lista) {
        limparFormulario();
        alimentos = lista;
        modelo.setRowCount(0);
        for (Alimento alimento : lista) {
            modelo.addRow(new Object[]{alimento.nome(), alimento.calorias(), alimento.proteinas(),
                    alimento.carboidratos(), alimento.gorduras()});
        }
    }

    private void carregar(Mensagem resultado) {
        String termo = filtro.getText();
        executar(dao -> dao.listar(termo), lista -> {
            substituirLista(lista);
            if (resultado != null) informar(resultado);
            else informar(lista.isEmpty() ? new Mensagem("Nenhum alimento encontrado.", "No foods found.", false)
                    : new Mensagem("", "", false));
        });
    }

    private void salvar() {
        String textoNome = nome.getText().strip();
        if (textoNome.isEmpty()) {
            informar(new Mensagem("Informe o nome do alimento.", "Enter the food name.", true));
            nome.requestFocusInWindow();
            return;
        }

        BigDecimal[] valores = new BigDecimal[4];
        for (int i = 0; i < valores.length; i++) {
            JTextField campo = campos[i + 1];
            int maximo = i == 0 ? 1000 : 100;
            try {
                String texto = campo.getText().strip().replace(',', '.');
                if (!texto.matches("[+-]?(?:[0-9]+(?:\\.[0-9]*)?|\\.[0-9]+)")) throw new NumberFormatException();
                valores[i] = AlimentoDao.validar(new BigDecimal(texto), maximo);
            } catch (IllegalArgumentException erro) {
                informar(new Mensagem("Informe um valor de 0 a " + maximo + " com até duas casas decimais.",
                        "Enter a value from 0 to " + maximo + " with up to two decimal places.", true));
                campo.requestFocusInWindow();
                return;
            }
        }

        Alimento alvo = selecionado;
        executar(dao -> {
            if (alvo == null) {
                dao.cadastrar(textoNome, valores[0], valores[1], valores[2], valores[3]);
                return true;
            }
            return dao.atualizar(alvo.id(), textoNome, valores[0], valores[1], valores[2], valores[3]);
        }, salvo -> {
            limparFormulario();
            carregar(salvo ? new Mensagem("Alimento salvo.", "Food saved.", false) : ausente());
        });
    }

    private void excluir() {
        Alimento alvo = selecionado;
        if (alvo == null || trabalho != null) return;
        long revisao = contexto.sessao().revisao();
        int resposta = JOptionPane.showOptionDialog(this,
                idioma.escolher("Excluir o alimento “" + alvo.nome() + "”?", "Delete “" + alvo.nome() + "”?"),
                excluir.getText(), JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null,
                new Object[]{idioma.escolher("Cancelar", "Cancel"), excluir.getText()}, null);
        if (resposta != 1 || revisao != contexto.sessao().revisao()) return;
        executar(dao -> dao.excluir(alvo.id()), removido -> {
            limparFormulario();
            carregar(removido ? new Mensagem("Alimento excluído.", "Food deleted.", false) : ausente());
        });
    }

    private static Mensagem ausente() {
        return new Mensagem("O alimento não está mais disponível.", "The food is no longer available.", true);
    }

    private void informar(Mensagem aviso) {
        this.aviso = aviso;
        mensagem.setForeground(aviso.erro() ? Visual.ERRO : Visual.SUAVE);
        mensagem.setText(aviso.pt().isEmpty() ? " " : idioma.escolher(aviso.pt(), aviso.en()));
    }

    private <T> void executar(Operacao<T> operacao, Consumer<T> concluir) {
        Sessao sessao = contexto.sessao();
        if (trabalho != null || !sessao.autenticada()) return;
        long revisao = sessao.revisao();
        informar(new Mensagem("Carregando…", "Loading…", false));
        trabalho = new SwingWorker<T, Void>() {
            @Override protected T doInBackground() throws SQLException {
                // Vincula o DAO à conta verificada sem manter o bloqueio durante o JDBC.
                Sessao sessaoDoTrabalho = new Sessao();
                synchronized (sessao) {
                    if (revisao != sessao.revisao() || !sessao.autenticada()) return null;
                    sessaoDoTrabalho.entrar(sessao.usuario());
                }
                return operacao.executar(new AlimentoDao(contexto.conexao(), sessaoDoTrabalho));
            }

            @Override protected void done() {
                if (trabalho != this || revisao != sessao.revisao()) return;
                trabalho = null;
                atualizarControles();
                try {
                    concluir.accept(get());
                } catch (InterruptedException erro) {
                    Thread.currentThread().interrupt();
                    informar(falhaBanco());
                } catch (ExecutionException erro) {
                    informar(mensagemErro(erro.getCause()));
                }
            }
        };
        atualizarControles();
        trabalho.execute();
    }

    private static Mensagem mensagemErro(Throwable erro) {
        if (erro instanceof SQLException sql) {
            if (sql.getErrorCode() == 1062) {
                return new Mensagem("Já existe um alimento com esse nome.", "A food with this name already exists.", true);
            }
            if (sql.getErrorCode() == 1451) {
                return new Mensagem("Este alimento está em uso em uma refeição.",
                        "This food is used in a meal.", true);
            }
        }
        return falhaBanco();
    }

    private static Mensagem falhaBanco() {
        return new Mensagem("Não foi possível concluir a operação no banco de dados.",
                "The database operation could not be completed.", true);
    }

    @FunctionalInterface
    private interface Operacao<T> {
        T executar(AlimentoDao dao) throws SQLException;
    }

    private record Mensagem(String pt, String en, boolean erro) {}
}