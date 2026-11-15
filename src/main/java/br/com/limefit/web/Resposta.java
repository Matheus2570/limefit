package br.com.limefit.web;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public final class Resposta {
    private Resposta() {}

    public static void redirecionar(HttpExchange pedido, String caminho) throws IOException {
        pedido.getResponseHeaders().set("Location", caminho);
        pedido.getResponseHeaders().set("Cache-Control", "no-store");
        pedido.sendResponseHeaders(303, -1);
    }

    public static void texto(HttpExchange pedido, int codigo, String tipo, String conteudo) throws IOException {
        enviar(pedido, codigo, tipo + "; charset=utf-8", conteudo.getBytes(StandardCharsets.UTF_8));
    }

    public static void enviar(HttpExchange pedido, int codigo, String tipo, byte[] conteudo) throws IOException {
        pedido.getResponseHeaders().set("Content-Type", tipo);
        pedido.getResponseHeaders().set("X-Content-Type-Options", "nosniff");
        pedido.getResponseHeaders().set("Referrer-Policy", "same-origin");
        pedido.getResponseHeaders().set("Content-Security-Policy", "default-src 'self'; style-src 'self'; script-src 'self'; img-src 'self'; base-uri 'none'; frame-ancestors 'none'; form-action 'self'");
        if (pedido.getRequestMethod().equals("HEAD")) {
            pedido.getResponseHeaders().set("Content-Length", String.valueOf(conteudo.length));
            pedido.sendResponseHeaders(codigo, -1);
        } else {
            pedido.sendResponseHeaders(codigo, conteudo.length);
            pedido.getResponseBody().write(conteudo);
        }
    }

    public static void metodoInvalido(HttpExchange pedido, String permitidos) throws IOException {
        pedido.getResponseHeaders().set("Allow", permitidos);
        texto(pedido, 405, "text/plain", Idioma.daRequisicao(pedido.getRequestURI()).escolher("Método HTTP não permitido.", "HTTP method not allowed."));
    }
}
