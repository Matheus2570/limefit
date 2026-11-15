package br.com.limefit.banco;

import br.com.limefit.web.Resposta;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

public final class StatusBanco implements HttpHandler {
    private static final Logger LOG = Logger.getLogger(StatusBanco.class.getName());
    private final Conexao conexao;

    public StatusBanco(Conexao conexao) {
        this.conexao = conexao;
    }

    @Override
    public void handle(HttpExchange pedido) throws IOException {
        if (!"GET".equals(pedido.getRequestMethod()) && !"HEAD".equals(pedido.getRequestMethod())) {
            Resposta.metodoInvalido(pedido, "GET, HEAD");
            return;
        }
        pedido.getResponseHeaders().set("Cache-Control", "no-store");
        try (Connection banco = conexao.abrir();
             Statement consulta = banco.createStatement();
             ResultSet resultado = consulta.executeQuery("SELECT COUNT(*) FROM alimentos")) {
            resultado.next();
            Resposta.texto(pedido, 200, "application/json", "{\"status\":\"ok\",\"alimentos\":" + resultado.getLong(1) + "}");
        } catch (SQLException erro) {
            LOG.warning("Conexão com o MariaDB: " + erro.getMessage());
            Resposta.texto(pedido, 503, "application/json",
                    "{\"status\":\"indisponivel\",\"mensagem\":\"Inicie o MySQL no XAMPP e importe sql/limefit.sql.\"}");
        }
    }
}
