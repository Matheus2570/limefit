package br.com.limefit.autenticacao;

import br.com.limefit.banco.Conexao;
import java.sql.SQLException;

public final class UsuarioDao {
    private final Conexao conexao;

    public UsuarioDao(Conexao conexao) { this.conexao = conexao; }

    public void cadastrar(String nome, String email, String senha) throws SQLException {
        String hash = Senhas.criar(senha);
        try (var banco = conexao.abrir();
             var inserir = banco.prepareStatement("INSERT INTO usuarios (nome, email, senha_hash) VALUES (?, ?, ?)")) {
            inserir.setString(1, nome);
            inserir.setString(2, email);
            inserir.setString(3, hash);
            inserir.executeUpdate();
        }
    }

    public Usuario autenticar(String email, String senha) throws SQLException {
        Usuario usuario = null;
        String hash = null;
        try (var banco = conexao.abrir();
             var consulta = banco.prepareStatement("SELECT id, nome, email, senha_hash FROM usuarios WHERE email = ?")) {
            consulta.setString(1, email);
            try (var resultado = consulta.executeQuery()) {
                if (resultado.next()) {
                    usuario = new Usuario(resultado.getLong("id"), resultado.getString("nome"), resultado.getString("email"));
                    hash = resultado.getString("senha_hash");
                }
            }
        }
        return Senhas.verificar(senha, hash) ? usuario : null;
    }
}
