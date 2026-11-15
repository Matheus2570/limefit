package br.com.limefit.banco;

import br.com.limefit.autenticacao.Autenticacao;
import com.sun.net.httpserver.HttpExchange;
import java.sql.SQLException;

/** Ajuda comum para os futuros DAOs. A identidade vem exclusivamente da sessão. */
public final class DadosDaConta {
    public enum Registro {
        PERFIL("perfis"), ALIMENTO("alimentos"), REFEICAO("refeicoes");
        private final String tabela;
        Registro(String tabela) { this.tabela = tabela; }
    }

    private final Conexao conexao;
    private final long usuarioId;

    public DadosDaConta(Conexao conexao, HttpExchange pedido) {
        this.conexao = conexao;
        this.usuarioId = Autenticacao.exigirUsuario(pedido).id();
    }

    public long usuarioId() { return usuarioId; }

    // Útil para selecionar referências de um formulário; não substitui o WHERE da gravação.
    public boolean pertence(Registro registro, long id) throws SQLException {
        // O nome da tabela vem de um enum fechado, nunca da URL ou de um formulário.
        try (var banco = conexao.abrir();
             var consulta = banco.prepareStatement("SELECT 1 FROM " + registro.tabela + " WHERE id = ? AND usuario_id = ?")) {
            consulta.setLong(1, id);
            consulta.setLong(2, usuarioId);
            try (var resultado = consulta.executeQuery()) { return resultado.next(); }
        }
    }
}
