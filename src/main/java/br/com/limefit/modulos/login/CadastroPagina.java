package br.com.limefit.modulos.login;

import br.com.limefit.autenticacao.Autenticacao;
import br.com.limefit.autenticacao.UsuarioDao;
import br.com.limefit.web.*;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;

public final class CadastroPagina extends Pagina {
    private static final Logger LOG = Logger.getLogger(CadastroPagina.class.getName());
    private final UsuarioDao usuarios;

    public CadastroPagina(UsuarioDao usuarios) { super(Tela.CADASTRO); this.usuarios = usuarios; }

    @Override
    protected void exibir(HttpExchange pedido, Idioma idioma) throws IOException {
        if (Autenticacao.sessao(pedido).usuario() != null) {
            Resposta.redirecionar(pedido, idioma.url("/perfil"));
            return;
        }
        enviarHtml(pedido, idioma, FormulariosLogin.montar(pedido, idioma, true, "", "", ""));
    }

    @Override
    protected void enviarFormulario(HttpExchange pedido, Idioma idioma, Map<String, String> campos) throws IOException {
        String nome = campos.getOrDefault("nome", "").strip();
        String email = campos.getOrDefault("email", "").strip().toLowerCase(Locale.ROOT);
        String senha = campos.getOrDefault("password", "");
        String erro;
        int codigo = 422;
        if (nome.isBlank() || nome.length() > 120 || email.length() > 190 || !email.matches("[^\\s@]+@[^\\s@.]+(?:\\.[^\\s@.]+)+")) {
            erro = idioma.escolher("Informe seu nome (até 120 caracteres) e um e-mail válido.", "Enter your name (up to 120 characters) and a valid email.");
        } else if (senha.length() < 8 || senha.length() > 128) {
            erro = idioma.escolher("A senha deve ter de 8 a 128 caracteres.", "Your password must have 8 to 128 characters.");
        } else if (!senha.equals(campos.get("confirm_password"))) {
            erro = idioma.escolher("As senhas não coincidem.", "The passwords do not match.");
        } else {
            try {
                usuarios.cadastrar(nome, email, senha);
                Resposta.redirecionar(pedido, idioma.url("/cadastro/sucesso"));
                return;
            } catch (SQLException falha) {
                if (falha.getErrorCode() == 1062) {
                    erro = idioma.escolher("Este e-mail já está cadastrado.", "This email is already registered.");
                    codigo = 409;
                } else {
                    LOG.warning("Cadastro indisponível, SQLState=" + falha.getSQLState());
                    erro = idioma.escolher("Não foi possível criar sua conta agora. Tente novamente em instantes.",
                            "We could not create your account right now. Please try again shortly.");
                    codigo = 503;
                }
            }
        }
        enviarHtml(pedido, idioma, codigo, FormulariosLogin.montar(pedido, idioma, true, erro, nome, email));
    }
}
