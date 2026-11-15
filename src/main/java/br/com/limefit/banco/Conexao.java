package br.com.limefit.banco;

import org.mariadb.jdbc.MariaDbDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public final class Conexao {
    private final String url;
    private final String usuario;
    private final String senha;

    public Conexao() {
        String porta = System.getenv().getOrDefault("LIMEFIT_DB_PORT", "3306");
        String banco = System.getenv().getOrDefault("LIMEFIT_DB_NAME", "limefit");
        if (!banco.matches("[A-Za-z0-9_]+")) throw new IllegalArgumentException("Nome de banco inválido");
        url = "jdbc:mariadb://127.0.0.1:" + porta + "/" + banco + "?connectTimeout=2000&socketTimeout=4000";
        usuario = System.getenv().getOrDefault("LIMEFIT_DB_USER", "root");
        senha = System.getenv().getOrDefault("LIMEFIT_DB_PASSWORD", "");
    }

    public Connection abrir() throws SQLException {
        return new MariaDbDataSource(url).getConnection(usuario, senha);
    }
}
