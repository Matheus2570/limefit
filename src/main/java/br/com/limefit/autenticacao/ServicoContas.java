package br.com.limefit.autenticacao;

import java.sql.SQLException;
import java.util.Locale;

/** Regras preservadas do cadastro web, independentes dos componentes Swing. */
public final class ServicoContas {
    private final UsuarioDao usuarios;
    public ServicoContas(UsuarioDao usuarios) { this.usuarios = usuarios; }

    public void cadastrar(String nome, String email, String senha, String confirmacao) throws SQLException, ValidacaoConta {
        nome = nome.strip();
        email = normalizarEmail(email);
        if (nome.isBlank() || nome.length() > 120 || !emailValido(email)) throw new ValidacaoConta(MensagemConta.DADOS);
        if (senha.length() < 8 || senha.length() > 128) throw new ValidacaoConta(MensagemConta.SENHA);
        if (!senha.equals(confirmacao)) throw new ValidacaoConta(MensagemConta.CONFIRMACAO);
        try { usuarios.cadastrar(nome, email, senha); }
        catch (SQLException erro) {
            if (erro.getErrorCode() == 1062) throw new ValidacaoConta(MensagemConta.DUPLICADO);
            throw erro;
        }
    }
    public Usuario autenticar(String email, String senha) throws SQLException, ValidacaoConta {
        email = normalizarEmail(email);
        if (!emailValido(email) || senha.isEmpty() || senha.length() > 128) throw new ValidacaoConta(MensagemConta.EMAIL);
        Usuario usuario = usuarios.autenticar(email, senha);
        if (usuario == null) throw new ValidacaoConta(MensagemConta.CREDENCIAIS);
        return usuario;
    }
    private static String normalizarEmail(String email) { return email.strip().toLowerCase(Locale.ROOT); }
    private static boolean emailValido(String email) {
        return email.length() <= 190 && email.matches("[^\\s@]+@[^\\s@.]+(?:\\.[^\\s@.]+)+");
    }
}
