package br.com.limefit.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public final class Roteador implements HttpHandler {
    private static final Logger LOG = Logger.getLogger(Roteador.class.getName());
    private static final Pattern ARQUIVO_ESTATICO = Pattern.compile("/assets/(?:[A-Za-z0-9_-]+/)*[A-Za-z0-9_-]+\\.(css|js|png|webp|jpe?g)");
    private final Map<String, HttpHandler> rotas = new HashMap<>();

    public void registrar(Pagina pagina) {
        registrar(pagina.rota(), pagina);
    }

    public void registrar(String caminho, HttpHandler rota) {
        if (rotas.putIfAbsent(caminho, rota) != null) {
            throw new IllegalArgumentException("Rota duplicada: " + caminho);
        }
    }

    @Override
    public void handle(HttpExchange pedido) throws IOException {
        try {
            String caminho = pedido.getRequestURI().getPath();
            if (caminho.length() > 1 && caminho.endsWith("/")) {
                caminho = caminho.substring(0, caminho.length() - 1);
            }
            HttpHandler rota = rotas.get(caminho);
            if (rota != null) {
                rota.handle(pedido);
            } else if (ARQUIVO_ESTATICO.matcher(caminho).matches()) {
                arquivo(pedido, caminho);
            } else {
                paginaNaoEncontrada(pedido);
            }
        } catch (IOException | RuntimeException erro) {
            LOG.log(Level.WARNING, "Falha ao responder à requisição", erro);
            if (pedido.getResponseCode() == -1) {
                Resposta.texto(pedido, 500, "text/plain", "Erro interno do LimeFit.");
            }
        } finally {
            pedido.close();
        }
    }

    private void arquivo(HttpExchange pedido, String caminho) throws IOException {
        if (!"GET".equals(pedido.getRequestMethod()) && !"HEAD".equals(pedido.getRequestMethod())) {
            Resposta.metodoInvalido(pedido, "GET, HEAD");
            return;
        }
        try (InputStream arquivo = Roteador.class.getResourceAsStream(caminho)) {
            if (arquivo == null) {
                paginaNaoEncontrada(pedido);
                return;
            }
            String tipo = switch (caminho.substring(caminho.lastIndexOf('.') + 1)) {
                case "css" -> "text/css; charset=utf-8";
                case "js" -> "text/javascript; charset=utf-8";
                case "png" -> "image/png";
                case "webp" -> "image/webp";
                default -> "image/jpeg";
            };
            Resposta.enviar(pedido, 200, tipo, arquivo.readAllBytes());
        }
    }

    private void paginaNaoEncontrada(HttpExchange pedido) throws IOException {
        Resposta.texto(pedido, 404, "text/plain", "Página não encontrada.");
    }
}
