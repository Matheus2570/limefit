package br.com.limefit.desktop;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

/** Componentes e cores comuns. A interface é feita com Swing, sem HTML ou CSS. */
public final class Visual {
    public static final Color FUNDO = new Color(0x0B100D);
    public static final Color PAINEL = new Color(0x151E18);
    public static final Color LINHA = new Color(0x354639);
    public static final Color LIMA = new Color(0xA8EB20);
    public static final Color TEXTO = new Color(0xF3F6EF);
    public static final Color SUAVE = new Color(0xB0BFB3);
    public static final Color ERRO = new Color(0xFFB3A7);
    private Visual() {}

    public static void configurar() {
        try { UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName()); }
        catch (Exception erro) { throw new IllegalStateException(erro); }
        UIManager.put("Button.font", fonte(14, Font.BOLD));
        UIManager.put("Label.font", fonte(15, Font.PLAIN));
        UIManager.put("TextField.font", fonte(16, Font.PLAIN));
        UIManager.put("PasswordField.font", fonte(16, Font.PLAIN));
        UIManager.put("ToolTip.background", PAINEL);
        UIManager.put("ToolTip.foreground", TEXTO);
    }

    public static Font fonte(int tamanho, int estilo) { return new Font("Segoe UI", estilo, tamanho); }
    public static JPanel painel(LayoutManager layout) {
        JPanel painel = new JPanel(layout);
        painel.setOpaque(false);
        return painel;
    }
    /** Mantém a largura da janela; textos se ajustam e a rolagem fica na vertical. */
    public static JPanel painelRolavel(LayoutManager layout) {
        return new PainelRolavel(layout);
    }
    private static final class PainelRolavel extends JPanel implements Scrollable {
        PainelRolavel(LayoutManager layout) { super(layout); setOpaque(false); }
        @Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
        @Override public int getScrollableUnitIncrement(Rectangle r, int eixo, int direcao) { return 22; }
        @Override public int getScrollableBlockIncrement(Rectangle r, int eixo, int direcao) { return Math.max(22, r.height - 22); }
        @Override public boolean getScrollableTracksViewportWidth() { return true; }
        @Override public boolean getScrollableTracksViewportHeight() {
            return getParent() instanceof JViewport viewport && getPreferredSize().height < viewport.getHeight();
        }
    }
    public static JLabel titulo(int tamanho) {
        JLabel rotulo = new JLabel();
        rotulo.setForeground(TEXTO);
        rotulo.setFont(fonte(tamanho, Font.BOLD));
        return rotulo;
    }
    public static JLabel rotulo() {
        JLabel rotulo = new JLabel();
        rotulo.setForeground(SUAVE);
        return rotulo;
    }
    public static JTextArea texto(int tamanho) {
        JTextArea texto = new JTextArea();
        texto.setLineWrap(true);
        texto.setWrapStyleWord(true);
        texto.setEditable(false);
        texto.setFocusable(false);
        texto.setOpaque(false);
        texto.setForeground(SUAVE);
        texto.setFont(fonte(tamanho, Font.PLAIN));
        return texto;
    }
    public static JButton botao(String nome, boolean destaque) {
        JButton botao = new JButton();
        botao.setName(nome);
        botao.setForeground(destaque ? FUNDO : TEXTO);
        botao.setBackground(destaque ? LIMA : PAINEL);
        botao.setFocusPainted(true);
        botao.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        botao.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(destaque ? LIMA : LINHA), new EmptyBorder(11, 18, 11, 18)));
        return botao;
    }
    public static <T extends JTextField> T campo(T campo, String nome, int limite) {
        campo.setName(nome);
        campo.setBackground(new Color(0x202B23));
        campo.setForeground(TEXTO);
        campo.setCaretColor(LIMA);
        campo.setSelectionColor(new Color(0x527A29));
        campo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINHA), new EmptyBorder(10, 12, 10, 12)));
        ((javax.swing.text.AbstractDocument) campo.getDocument()).setDocumentFilter(new javax.swing.text.DocumentFilter() {
            @Override public void insertString(FilterBypass fb, int offset, String texto, javax.swing.text.AttributeSet attr)
                    throws javax.swing.text.BadLocationException { replace(fb, offset, 0, texto, attr); }
            @Override public void replace(FilterBypass fb, int offset, int length, String texto, javax.swing.text.AttributeSet attr)
                    throws javax.swing.text.BadLocationException {
                if (texto == null || fb.getDocument().getLength() - length + texto.length() <= limite)
                    super.replace(fb, offset, length, texto, attr);
            }
        });
        return campo;
    }
    public static JScrollPane rolagem(JComponent conteudo) {
        JScrollPane scroll = new JScrollPane(conteudo);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(FUNDO);
        scroll.getVerticalScrollBar().setUnitIncrement(22);
        return scroll;
    }
    public static BufferedImage imagem(String nome) {
        try (var recurso = Visual.class.getResourceAsStream("/assets/" + nome)) {
            if (recurso == null) throw new IllegalStateException("Missing asset: " + nome);
            return ImageIO.read(recurso);
        } catch (IOException erro) { throw new IllegalStateException("Image: " + nome, erro); }
    }
    public static ImageIcon logo(int tamanho) {
        return new ImageIcon(imagem("logo.png").getScaledInstance(tamanho, tamanho, Image.SCALE_SMOOTH));
    }

    /** Ajusta a foto ao painel e recorta pelo centro, mantendo a proporção original. */
    public static final class Foto extends JPanel {
        private final BufferedImage imagem = imagem("capa.png");
        public Foto() {
            setPreferredSize(new Dimension(480, 340));
            setMinimumSize(new Dimension(200, 200));
        }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D desenho = (Graphics2D) g.create();
            desenho.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            double escala = Math.max(getWidth() / (double) imagem.getWidth(), getHeight() / (double) imagem.getHeight());
            int largura = (int) Math.ceil(imagem.getWidth() * escala);
            int altura = (int) Math.ceil(imagem.getHeight() * escala);
            desenho.drawImage(imagem, (getWidth() - largura) / 2, (getHeight() - altura) / 2, largura, altura, null);
            desenho.dispose();
        }
    }
}
