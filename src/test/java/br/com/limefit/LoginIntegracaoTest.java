package br.com.limefit;

import br.com.limefit.autenticacao.Autenticacao;
import br.com.limefit.autenticacao.Senhas;
import br.com.limefit.banco.Conexao;
import br.com.limefit.banco.DadosDaConta;
import br.com.limefit.web.Resposta;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.net.*;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

/** scripts/validar.py prepara um banco descartável; este teste não usa o banco pessoal. */
@EnabledIfEnvironmentVariable(named = "LIMEFIT_TEST_DB", matches = "limefit_validacao_[0-9]+")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
final class LoginIntegracaoTest {
    static HttpServer servidor;
    static ExecutorService executor;
    static URI base;
    static final Conexao CONEXAO = new Conexao();
    static Navegador ana, bruno;
    static long anaId, brunoId, alimentoAna, alimentoBruno, perfilAna, refeicaoAna;

    @BeforeAll
    static void iniciar() throws Exception {
        assertEquals(System.getenv("LIMEFIT_TEST_DB"), System.getenv("LIMEFIT_DB_NAME"));
        var roteador = Aplicacao.criarRoteador(CONEXAO);
        // Instrumentação só deste teste. A aplicação entregue não registra esta rota.
        roteador.registrar("/teste/identidade", pedido -> {
            if (Autenticacao.sessao(pedido).usuario() == null) {
                Resposta.texto(pedido, 401, "text/plain", "anonymous");
                return;
            }
            var conta = new DadosDaConta(CONEXAO, pedido);
            String query = pedido.getRequestURI().getQuery();
            try {
                boolean pertence = query != null && query.matches("[0-9]+") && conta.pertence(DadosDaConta.Registro.ALIMENTO, Long.parseLong(query));
                Resposta.texto(pedido, 200, "text/plain", conta.usuarioId() + ":" + pertence);
            } catch (SQLException erro) { throw new java.io.IOException(erro); }
        });
        servidor = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        servidor.createContext("/", roteador);
        executor = Executors.newFixedThreadPool(4);
        servidor.setExecutor(executor);
        servidor.start();
        base = URI.create("http://127.0.0.1:" + servidor.getAddress().getPort());
        ana = new Navegador(); bruno = new Navegador();
    }

    @AfterAll
    static void encerrar() {
        if (servidor != null) servidor.stop(0);
        if (executor != null) executor.shutdownNow();
    }

    @Test @Order(1)
    void homeIdiomasRotasPrivadasEStatus() throws Exception {
        var home = ana.get("/");
        assertEquals(200, home.statusCode());
        assertTrue(home.body().contains("lang=\"en\""));
        assertTrue(home.body().contains("Your nutrition,"));
        assertTrue(home.body().contains("/login?lang=en"));
        assertTrue(ana.get("/?lang=pt").body().contains("Sua alimentação,"));
        assertTrue(ana.get("/login?lang=pt").body().contains("Entrar na conta"));
        assertTrue(ana.get("/cadastro?lang=pt").body().contains("Confirmar senha"));
        for (String rota : List.of("/perfil", "/alimentos", "/refeicoes", "/planejamento", "/projeto")) {
            var resposta = bruno.get(rota + "?lang=pt");
            assertEquals(303, resposta.statusCode());
            assertEquals("/login?lang=pt", resposta.headers().firstValue("Location").orElseThrow());
        }
        var banco = ana.get("/status/banco");
        assertEquals(200, banco.statusCode());
        assertTrue(banco.body().contains("\"status\":\"ok\""));
        assertEquals(200, ana.get("/assets/capa.png").statusCode());
        assertEquals(200, ana.get("/assets/logo.png").statusCode());
        assertEquals(200, ana.head("/").statusCode());
        assertEquals(404, ana.get("/views/inicio/pagina.html").statusCode());
    }

    @Test @Order(2)
    void cadastroValidacaoCsrfDuplicidadeEHash() throws Exception {
        assertEquals(403, ana.post("/cadastro", Map.of("nome", "Sem CSRF")).statusCode());
        var campos = cadastro("Ana Teste", "ana.validacao@example.test");
        campos.put("csrf_token", ana.csrf("/cadastro"));
        campos.put("confirm_password", "diferente");
        var erro = ana.post("/cadastro?lang=pt", campos);
        assertEquals(422, erro.statusCode());
        assertTrue(erro.body().contains("As senhas não coincidem."));
        campos.put("confirm_password", "SenhaTeste-123");
        var sucesso = ana.post("/cadastro?lang=pt", campos);
        assertEquals(303, sucesso.statusCode());
        assertEquals("/cadastro/sucesso?lang=pt", sucesso.headers().firstValue("Location").orElseThrow());
        assertTrue(ana.get("/cadastro/sucesso?lang=pt").body().contains("Conta criada!"));
        var duplicado = ana.post("/cadastro", campos);
        assertEquals(409, duplicado.statusCode());
        assertTrue(duplicado.body().contains("This email is already registered."));
        var segundo = cadastro("Bruno Teste", "bruno.validacao@example.test");
        segundo.put("csrf_token", bruno.csrf("/cadastro"));
        assertEquals(303, bruno.post("/cadastro", segundo).statusCode());
        anaId = usuario("ana.validacao@example.test"); brunoId = usuario("bruno.validacao@example.test");
        assertNotEquals(anaId, brunoId);
        try (var banco = CONEXAO.abrir(); var ps = banco.prepareStatement("SELECT senha_hash FROM usuarios WHERE id = ?")) {
            ps.setLong(1, anaId);
            try (var rs = ps.executeQuery()) {
                assertTrue(rs.next());
                String hash = rs.getString(1);
                assertTrue(hash.startsWith("pbkdf2-sha256$600000$"));
                assertFalse(hash.contains("SenhaTeste-123"));
                assertTrue(Senhas.verificar("SenhaTeste-123", hash));
                assertFalse(Senhas.verificar("senha-errada", hash));
            }
        }
        assertEquals(303, ana.get("/perfil").statusCode()); // Cadastro não autentica automaticamente.
    }

    @Test @Order(3)
    void loginRotacaoSessoesConcorrentesESemConfiarNoFormulario() throws Exception {
        var campos = new HashMap<String, String>();
        campos.put("email", "' OR 1=1 --"); campos.put("password", "SenhaTeste-123");
        campos.put("csrf_token", ana.csrf("/login"));
        assertEquals(401, ana.post("/login", campos).statusCode());
        campos.put("email", "ana.validacao@example.test"); campos.put("password", "errada");
        assertTrue(ana.post("/login?lang=pt", campos).body().contains("E-mail ou senha incorretos."));
        String cookieAntigo = ana.cookie();
        campos.put("password", "SenhaTeste-123");
        campos.put("usuario_id", String.valueOf(brunoId)); // Deve ser ignorado.
        var login = ana.post("/login?lang=pt", campos);
        assertEquals(303, login.statusCode());
        assertNotEquals(cookieAntigo, ana.cookie());
        assertTrue(login.headers().firstValue("Set-Cookie").orElseThrow().contains("HttpOnly; SameSite=Lax"));
        assertEquals(303, ana.comCookie("/perfil", cookieAntigo).statusCode());
        campos.put("email", "bruno.validacao@example.test");
        campos.put("csrf_token", bruno.csrf("/login"));
        assertEquals(303, bruno.post("/login", campos).statusCode());
        assertEquals(anaId + ":false", ana.get("/teste/identidade?usuario_id=" + brunoId).body());
    }

    @Test @Order(4)
    void propriedadePreservacaoLegadoEChavesCompostas() throws Exception {
        try (var banco = CONEXAO.abrir(); var stmt = banco.createStatement()) {
            var legado = stmt.executeQuery("SELECT COUNT(*) FROM alimentos WHERE usuario_id IS NULL");
            assertTrue(legado.next()); assertEquals(2, legado.getInt(1)); legado.close();
            try (var valores = stmt.executeQuery("""
                    SELECT SUM(a.calorias * i.quantidade_g / 100), SUM(a.proteinas * i.quantidade_g / 100),
                           SUM(a.carboidratos * i.quantidade_g / 100), SUM(a.gorduras * i.quantidade_g / 100)
                    FROM perfis p JOIN refeicoes r ON r.perfil_id = p.id
                    JOIN itens_refeicao i ON i.refeicao_id = r.id JOIN alimentos a ON a.id = i.alimento_id
                    WHERE p.usuario_id IS NULL AND r.usuario_id IS NULL AND i.usuario_id IS NULL AND a.usuario_id IS NULL
                    """)) {
                assertTrue(valores.next());
                assertEquals(340, valores.getDouble(1)); assertEquals(15.5, valores.getDouble(2));
                assertEquals(55, valores.getDouble(3)); assertEquals(6, valores.getDouble(4));
            }
            alimentoAna = inserir(banco, "INSERT INTO alimentos (nome, calorias, proteinas, carboidratos, gorduras, usuario_id) VALUES ('Mesmo alimento', 100, 5, 10, 2, " + anaId + ")");
            alimentoBruno = inserir(banco, "INSERT INTO alimentos (nome, calorias, proteinas, carboidratos, gorduras, usuario_id) VALUES ('Mesmo alimento', 100, 5, 10, 2, " + brunoId + ")");
            perfilAna = inserir(banco, "INSERT INTO perfis (identificador, peso_kg, altura_cm, objetivo, usuario_id) VALUES (SHA2('perfil-ana', 256), 70, 170, 'maintain', " + anaId + ")");
            refeicaoAna = inserir(banco, "INSERT INTO refeicoes (perfil_id, data_plano, nome, usuario_id) VALUES (" + perfilAna + ", '2026-01-15', 'Almoço', " + anaId + ")");
            assertThrows(SQLException.class, () -> stmt.executeUpdate("INSERT INTO itens_refeicao (refeicao_id, alimento_id, quantidade_g, usuario_id) VALUES (" + refeicaoAna + ", " + alimentoBruno + ", 100, " + anaId + ")"));
            assertThrows(SQLException.class, () -> stmt.executeUpdate("INSERT INTO refeicoes (perfil_id, data_plano, nome, usuario_id) VALUES (" + perfilAna + ", '2026-01-15', 'Intrusa', " + brunoId + ")"));
            assertThrows(SQLException.class, () -> stmt.executeUpdate("INSERT INTO alimentos (nome, calorias, proteinas, carboidratos, gorduras) VALUES ('Sem dono', 10, 1, 1, 1)"));
            assertThrows(SQLException.class, () -> stmt.executeUpdate("UPDATE alimentos SET usuario_id = " + brunoId + " WHERE id = " + alimentoAna));
            assertEquals(1, stmt.executeUpdate("INSERT INTO itens_refeicao (refeicao_id, alimento_id, quantidade_g, usuario_id) VALUES (" + refeicaoAna + ", " + alimentoAna + ", 100, " + anaId + ")"));
            try (var atualizar = banco.prepareStatement("UPDATE alimentos SET calorias = ? WHERE id = ? AND usuario_id = ?");
                 var excluir = banco.prepareStatement("DELETE FROM alimentos WHERE id = ? AND usuario_id = ?")) {
                atualizar.setInt(1, 200); atualizar.setLong(2, alimentoAna); atualizar.setLong(3, brunoId);
                assertEquals(0, atualizar.executeUpdate());
                excluir.setLong(1, alimentoAna); excluir.setLong(2, brunoId);
                assertEquals(0, excluir.executeUpdate());
            }
        }
        assertEquals(anaId + ":true", ana.get("/teste/identidade?" + alimentoAna).body());
        assertEquals(anaId + ":false", ana.get("/teste/identidade?" + alimentoBruno).body());
        assertEquals(brunoId + ":false", bruno.get("/teste/identidade?" + alimentoAna).body());
        assertEquals(brunoId + ":true", bruno.get("/teste/identidade?" + alimentoBruno).body());
        var chamadas = new ArrayList<java.util.concurrent.CompletableFuture<Void>>();
        for (int i = 0; i < 12; i++) {
            Navegador navegador = i % 2 == 0 ? ana : bruno;
            long esperado = i % 2 == 0 ? anaId : brunoId;
            chamadas.add(navegador.cliente.sendAsync(HttpRequest.newBuilder(base.resolve("/teste/identidade")).build(), HttpResponse.BodyHandlers.ofString())
                    .thenAccept(r -> assertEquals(esperado + ":false", r.body())));
        }
        java.util.concurrent.CompletableFuture.allOf(chamadas.toArray(java.util.concurrent.CompletableFuture[]::new)).join();
    }

    @Test @Order(5)
    void placeholdersESaidaInvalidamSoAProriaSessao() throws Exception {
        for (String rota : List.of("/perfil", "/alimentos", "/refeicoes", "/planejamento", "/projeto")) {
            var pagina = ana.get(rota);
            assertEquals(200, pagina.statusCode());
            assertEquals(1, pagina.body().split("<h1", -1).length - 1);
            assertTrue(Pattern.compile("<main[^>]*><section[^>]*><h1>[^<]+</h1></section></main>").matcher(pagina.body()).find());
        }
        assertEquals(405, ana.get("/sair").statusCode());
        assertEquals(403, ana.post("/sair", Map.of("csrf_token", "invalido")).statusCode());
        String token = ana.csrf("/perfil");
        String cookie = ana.cookie();
        assertEquals(303, ana.post("/sair?lang=pt", Map.of("csrf_token", token)).statusCode());
        assertEquals(303, ana.comCookie("/perfil", cookie).statusCode());
        assertEquals(303, ana.get("/perfil").statusCode());
        assertEquals(200, bruno.get("/perfil").statusCode());
        assertEquals(brunoId + ":false", bruno.get("/teste/identidade").body());
    }

    static Map<String, String> cadastro(String nome, String email) {
        return new HashMap<>(Map.of("nome", nome, "email", email, "password", "SenhaTeste-123", "confirm_password", "SenhaTeste-123"));
    }
    static long usuario(String email) throws SQLException {
        try (var banco = CONEXAO.abrir(); var consulta = banco.prepareStatement("SELECT id FROM usuarios WHERE email = ?")) {
            consulta.setString(1, email);
            try (var rs = consulta.executeQuery()) { assertTrue(rs.next()); return rs.getLong(1); }
        }
    }
    static long inserir(Connection banco, String sql) throws SQLException {
        try (var ps = banco.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.executeUpdate();
            try (var rs = ps.getGeneratedKeys()) { assertTrue(rs.next()); return rs.getLong(1); }
        }
    }
    static final class Navegador {
        final CookieManager cookies = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
        final HttpClient cliente = HttpClient.newBuilder().cookieHandler(cookies).followRedirects(HttpClient.Redirect.NEVER).build();
        HttpResponse<String> get(String rota) throws Exception { return cliente.send(HttpRequest.newBuilder(base.resolve(rota)).GET().build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)); }
        HttpResponse<String> head(String rota) throws Exception { return cliente.send(HttpRequest.newBuilder(base.resolve(rota)).method("HEAD", HttpRequest.BodyPublishers.noBody()).build(), HttpResponse.BodyHandlers.ofString()); }
        HttpResponse<String> post(String rota, Map<String, String> campos) throws Exception {
            String corpo = campos.entrySet().stream().map(e -> URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8) + "=" + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8)).collect(java.util.stream.Collectors.joining("&"));
            return cliente.send(HttpRequest.newBuilder(base.resolve(rota)).header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(corpo)).build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        }
        String csrf(String rota) throws Exception {
            var matcher = Pattern.compile("name=\"csrf_token\" value=\"([^\"]+)\"").matcher(get(rota).body());
            assertTrue(matcher.find()); return matcher.group(1);
        }
        String cookie() { return cookies.getCookieStore().getCookies().stream().filter(c -> c.getName().equals("LIMEFIT_SESSION")).findFirst().orElseThrow().getValue(); }
        HttpResponse<String> comCookie(String rota, String cookie) throws Exception {
            return HttpClient.newHttpClient().send(HttpRequest.newBuilder(base.resolve(rota)).header("Cookie", "LIMEFIT_SESSION=" + cookie).build(), HttpResponse.BodyHandlers.ofString());
        }
    }
}
