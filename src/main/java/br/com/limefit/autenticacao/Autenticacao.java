package br.com.limefit.autenticacao;

import com.sun.net.httpserver.HttpExchange;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class Autenticacao {
    private static final String COOKIE = "LIMEFIT_SESSION";
    private static final SecureRandom ALEATORIO = new SecureRandom();
    private static final Map<String, Sessao> SESSOES = new ConcurrentHashMap<>();
    private static final boolean HTTPS = Boolean.parseBoolean(System.getenv("LIMEFIT_COOKIE_SECURE"));

    private Autenticacao() {}

    public static Sessao sessao(HttpExchange pedido) {
        // A resposta tem prioridade: contém o token recém-criado/rotacionado neste pedido.
        // Não usar HttpContext.attributes: é compartilhado entre requisições/contas.
        String token = token(pedido.getResponseHeaders().getFirst("Set-Cookie"));
        if (token == null) token = token(pedido.getRequestHeaders().getFirst("Cookie"));
        Sessao sessao = token == null ? null : SESSOES.get(token);
        if (sessao != null && !sessao.expirada()) {
            sessao.renovar();
            return sessao;
        }
        if (token != null) SESSOES.remove(token);
        return criar(pedido, null);
    }

    public static Usuario exigirUsuario(HttpExchange pedido) {
        Usuario usuario = sessao(pedido).usuario();
        if (usuario == null) throw new IllegalStateException("Conta autenticada obrigatória");
        return usuario;
    }

    public static boolean csrfValido(HttpExchange pedido, String recebido) {
        return recebido != null && MessageDigest.isEqual(sessao(pedido).csrf().getBytes(StandardCharsets.UTF_8),
                recebido.getBytes(StandardCharsets.UTF_8));
    }

    public static void entrar(HttpExchange pedido, Usuario usuario) {
        invalidar(pedido);
        criar(pedido, usuario); // Novo token e novo CSRF evitam fixação de sessão.
    }

    public static void sair(HttpExchange pedido) {
        invalidar(pedido);
        pedido.getResponseHeaders().set("Set-Cookie", COOKIE + "=; Path=/; HttpOnly; SameSite=Lax; Max-Age=0" + (HTTPS ? "; Secure" : ""));
    }

    private static void invalidar(HttpExchange pedido) {
        String resposta = token(pedido.getResponseHeaders().getFirst("Set-Cookie"));
        String recebido = token(pedido.getRequestHeaders().getFirst("Cookie"));
        if (resposta != null) SESSOES.remove(resposta);
        if (recebido != null) SESSOES.remove(recebido);
    }

    private static Sessao criar(HttpExchange pedido, Usuario usuario) {
        SESSOES.entrySet().removeIf(entrada -> entrada.getValue().expirada());
        String token = aleatorio();
        Sessao sessao = new Sessao(usuario, aleatorio());
        SESSOES.put(token, sessao);
        pedido.getResponseHeaders().set("Set-Cookie", COOKIE + "=" + token + "; Path=/; HttpOnly; SameSite=Lax" + (HTTPS ? "; Secure" : ""));
        return sessao;
    }

    private static String token(String cookies) {
        if (cookies == null) return null;
        for (String parte : cookies.split(";")) {
            String[] campo = parte.trim().split("=", 2);
            if (campo.length == 2 && campo[0].equals(COOKIE) && campo[1].matches("[A-Za-z0-9_-]{43}")) return campo[1];
        }
        return null;
    }

    private static String aleatorio() {
        byte[] bytes = new byte[32];
        ALEATORIO.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
