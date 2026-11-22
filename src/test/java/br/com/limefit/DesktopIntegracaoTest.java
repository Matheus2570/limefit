package br.com.limefit;

import br.com.limefit.autenticacao.*;
import br.com.limefit.desktop.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.function.BooleanSupplier;
import static org.junit.jupiter.api.Assertions.*;

/** Janela real + MariaDB real, exclusivamente em banco descartável criado pelo script. */
@EnabledIfEnvironmentVariable(named = "LIMEFIT_TEST_DB", matches = "limefit_validacao_[0-9]+")
class DesktopIntegracaoTest {
    ContextoAplicacao contexto;
    JanelaPrincipal janela;
    static final String SENHA = "SenhaTeste-123";

    @BeforeEach void abrir() throws Exception {
        assertEquals(System.getenv("LIMEFIT_TEST_DB"), System.getenv("LIMEFIT_DB_NAME"));
        assertFalse(GraphicsEnvironment.isHeadless(), "É necessária uma sessão gráfica para testar Swing.");
        edt(() -> {
            Visual.configurar();
            contexto = new ContextoAplicacao();
            janela = new JanelaPrincipal(contexto);
            janela.setVisible(true);
            return null;
        });
    }
    @AfterEach void fechar() throws Exception {
        if (janela != null) edt(() -> { janela.dispose(); return null; });
    }
    @Test void fluxoCompletoComDuasContasNaMesmaJanela() throws Exception {
        assertTrue(edt(janela::isShowing));
        assertEquals(Tela.INICIO, edt(janela::telaAtual));
        assertEquals("Get started", textoBotao("home.comecar"));
        foto("home-en");
        clicar("idioma.pt");
        assertEquals("Começar agora", textoBotao("home.comecar"));
        clicar("menu.ALIMENTOS");
        assertEquals(Tela.LOGIN, edt(janela::telaAtual));
        for (Tela tela : new Tela[]{Tela.PERFIL, Tela.ALIMENTOS, Tela.REFEICOES, Tela.PLANEJAMENTO, Tela.PROJETO}) {
            clicar("menu." + tela.name());
            assertEquals(Tela.LOGIN, edt(janela::telaAtual));
            assertFalse(contexto.sessao().autenticada());
        }
        clicar("login.voltar");
        cadastrar("Ana Teste", "Ana.Validacao@Example.test");
        cadastrar("Bruno Teste", "bruno.validacao@example.test");
        clicar("home.criar");
        preencherCadastro("Duplicada", "ana.validacao@example.test", SENHA, SENHA);
        clicar("cadastro.enviar");
        aguardar(() -> aviso("cadastro").contains("já está cadastrado"));
        clicar("cadastro.voltar");
        clicar("home.comecar");
        campo("login.email", "ana.validacao@example.test");
        campo("login.senha", "Errada-123");
        clicar("login.enviar");
        aguardar(() -> aviso("login").contains("incorretos"));
        assertFalse(contexto.sessao().autenticada());
        clicar("idioma.en");
        assertEquals("Incorrect email or password.", aviso("login"));
        clicar("idioma.pt");
        foto("login-pt-erro");
        entrar("ANA.VALIDACAO@EXAMPLE.TEST");
        long anaId = contexto.usuarioId();
        assertEquals("Ana Teste", contexto.sessao().usuario().nome());
        assertEquals(Tela.INICIO, edt(janela::telaAtual));
        assertEquals("Acessar meu perfil", textoBotao("home.comecar"));
        assertEquals("Ana Teste", edt(() -> ((JLabel) buscar("identidade")).getText()));
        navegarModulos();
        clicar("sair");
        assertFalse(contexto.sessao().autenticada());
        assertThrows(IllegalStateException.class, contexto::usuarioId);
        assertEquals("", edt(() -> ((JLabel) buscar("identidade")).getText()));
        assertEquals("", edt(() -> ((JLabel) buscar("home.boasVindas")).getText()));
        clicar("menu.REFEICOES"); // Login deve retornar à tela privada originalmente solicitada.
        assertEquals("", edt(() -> ((JTextField) buscar("login.email")).getText()));
        entrar("bruno.validacao@example.test");
        assertNotEquals(anaId, contexto.usuarioId());
        assertEquals("Bruno Teste", contexto.sessao().usuario().nome());
        assertEquals(Tela.REFEICOES, edt(janela::telaAtual));
        clicar("idioma.en");
        assertEquals("Meals", textoBotao("menu.REFEICOES"));
        clicar("menu.INICIO");
        assertEquals("Hello, Bruno Teste", edt(() -> ((JLabel) buscar("home.boasVindas")).getText()));
        assertEquals("Go to my profile", textoBotao("home.comecar"));
        foto("home-bruno-en");
        clicar("sair");
        assertEquals(Tela.INICIO, edt(janela::telaAtual));
        assertEquals("Get started", textoBotao("home.comecar"));
        assertFalse(edt(() -> buscar("sair").isVisible()));
        // A janela menor continua navegável e os campos se mantêm alinhados.
        edt(() -> { janela.setSize(900, 700); return null; });
        aguardar(() -> ((GridLayout) ((JPanel) buscar("home.hero")).getLayout()).getRows() == 2);
        assertFalse(edt(() -> ((JScrollPane) buscar("home.rolagem")).getHorizontalScrollBar().isVisible()));
        foto("home-menor");
        clicar("home.criar");
        foto("cadastro-menor");
    }
    private void cadastrar(String nome, String email) throws Exception {
        clicar("home.criar");
        preencherCadastro(nome, email, "curta", "curta");
        clicar("cadastro.enviar");
        aguardar(() -> aviso("cadastro").contains("8 a 128"));
        preencherCadastro(nome, email, SENHA, "diferente");
        clicar("cadastro.enviar");
        aguardar(() -> aviso("cadastro").contains("não coincidem"));
        clicar("idioma.en");
        assertEquals("The passwords do not match.", aviso("cadastro"));
        clicar("idioma.pt");
        preencherCadastro(nome, email, SENHA, SENHA);
        clicar("cadastro.enviar");
        aguardar(() -> janela.telaAtual() == Tela.CADASTRO_SUCESSO);
        assertFalse(contexto.sessao().autenticada(), "Cadastro não autentica automaticamente.");
        foto("sucesso-" + nome.split(" ")[0]);
        clicar("sucesso.inicio");
    }
    private void preencherCadastro(String nome, String email, String senha, String confirmacao) throws Exception {
        campo("cadastro.nome", nome); campo("cadastro.email", email);
        campo("cadastro.senha", senha); campo("cadastro.confirmar", confirmacao);
    }
    private void entrar(String email) throws Exception {
        campo("login.email", email); campo("login.senha", SENHA);
        clicar("login.enviar");
        aguardar(() -> contexto.sessao().autenticada() && janela.telaAtual() != Tela.LOGIN);
    }
    private void navegarModulos() throws Exception {
        for (Tela tela : new Tela[]{Tela.PERFIL, Tela.ALIMENTOS, Tela.REFEICOES, Tela.PLANEJAMENTO, Tela.PROJETO}) {
            clicar("menu." + tela.name());
            assertEquals(tela, edt(janela::telaAtual));
        }
    }
    private void campo(String nome, String valor) throws Exception {
        edt(() -> { ((JTextField) buscar(nome)).setText(valor); return null; });
    }
    private void clicar(String nome) throws Exception {
        edt(() -> {
            AbstractButton botao = (AbstractButton) buscar(nome);
            assertTrue(botao.isShowing(), nome + " precisa estar na tela");
            assertTrue(botao.isEnabled(), nome + " precisa estar habilitado");
            botao.doClick(0);
            return null;
        });
    }
    private String textoBotao(String nome) throws Exception {
        return edt(() -> ((AbstractButton) buscar(nome)).getText());
    }
    private String aviso(String prefixo) {
        try { return edt(() -> ((JTextArea) buscar(prefixo + ".aviso")).getText()); }
        catch (Exception erro) { throw new AssertionError(erro); }
    }
    private Component buscar(String nome) {
        Component encontrado = buscarEm(janela, nome);
        assertNotNull(encontrado, nome);
        return encontrado;
    }
    private Component buscarEm(Component componente, String nome) {
        if (nome.equals(componente.getName())) return componente;
        if (componente instanceof Container container)
            for (Component filho : container.getComponents()) {
                Component resultado = buscarEm(filho, nome);
                if (resultado != null) return resultado;
            }
        return null;
    }
    private void aguardar(BooleanSupplier condicao) throws Exception {
        long fim = System.nanoTime() + 15_000_000_000L;
        while (!edt(condicao::getAsBoolean)) {
            if (System.nanoTime() > fim) fail("Tempo esgotado aguardando resposta da interface.");
            Thread.sleep(50);
        }
    }
    private void foto(String nome) throws Exception {
        edt(() -> {
            janela.validate();
            BufferedImage imagem = new BufferedImage(janela.getWidth(), janela.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D desenho = imagem.createGraphics();
            janela.paint(desenho); desenho.dispose();
            Path pasta = Path.of("target", "validacao");
            Files.createDirectories(pasta);
            ImageIO.write(imagem, "png", pasta.resolve(nome + ".png").toFile());
            return null;
        });
    }
    static <T> T edt(Callable<T> tarefa) throws Exception {
        if (SwingUtilities.isEventDispatchThread()) return tarefa.call();
        FutureTask<T> futuro = new FutureTask<>(tarefa);
        SwingUtilities.invokeAndWait(futuro);
        return futuro.get();
    }
}
