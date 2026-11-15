package br.com.limefit.web;

import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public final class Formulario {
    private Formulario() {}

    public static Map<String, String> ler(HttpExchange pedido) throws IOException {
        String tipo = pedido.getRequestHeaders().getFirst("Content-Type");
        if (tipo == null || !tipo.split(";", 2)[0].trim().equalsIgnoreCase("application/x-www-form-urlencoded")) {
            throw new Invalido(415);
        }
        byte[] corpo = pedido.getRequestBody().readNBytes(16_385);
        if (corpo.length > 16_384) throw new Invalido(413);
        Map<String, String> campos = new HashMap<>();
        try {
            for (String par : new String(corpo, StandardCharsets.UTF_8).split("&")) {
                String[] partes = par.split("=", 2);
                String chave = URLDecoder.decode(partes[0], StandardCharsets.UTF_8);
                String valor = partes.length == 2 ? URLDecoder.decode(partes[1], StandardCharsets.UTF_8) : "";
                if (campos.putIfAbsent(chave, valor) != null) throw new Invalido(400);
            }
        } catch (IllegalArgumentException erro) {
            throw new Invalido(400);
        }
        return campos;
    }

    public static final class Invalido extends IOException {
        private final int codigo;
        public Invalido(int codigo) { this.codigo = codigo; }
        public int codigo() { return codigo; }
    }
}
