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

public final class LoginPagina extends Pagina {
    private static final Logger LOG = Logger.getLogger(LoginPagina.class.getName());
    private final UsuarioDao usuarios;

    public LoginPagina(UsuarioDao usuarios) { super(Tela.LOGIN); this.usuarios = usuarios; }

    @Override
    protected void exibir(HttpExchange pedido, Idioma idioma) throws IOException {
        if (Autenticacao.sessao(pedido).usuario() != null) {
            Resposta.redirecionar(pedido, idioma.url("/perfil"));
            return;
        }
        enviarHtml(pedido, idioma, FormulariosLogin.montar(pedido, idioma, false, "", "", ""));
    }

    @Override
    protected void enviarFormulario(HttpExchange pedido, Idioma idioma, Map<String, String> campos) throws IOException {
        String email = campos.getOrDefault("email", "").strip().toLowerCase(Locale.ROOT);
        String senha = campos.getOrDefault("password", "");
        String erro = idioma.escolher("E-mail ou senha incorretos.", "Incorrect email or password.");
        int codigo = 401;
        if (email.length() <= 190 && !email.isEmpty() && !senha.isEmpty() && senha.length() <= 128) {
            try {
                var usuario = usuarios.autenticar(email, senha);
                if (usuario != null) {
                    Autenticacao.entrar(pedido, usuario);
                    Resposta.redirecionar(pedido, idioma.url("/perfil"));
                    return;
                }
            } catch (SQLException falha) {
                LOG.warning("Login indisponível, SQLState=" + falha.getSQLState());
                erro = idioma.escolher("Não foi possível acessar sua conta agora. Tente novamente em instantes.",
                        "We could not access your account right now. Please try again shortly.");
                codigo = 503;
            }
        }
        enviarHtml(pedido, idioma, codigo, FormulariosLogin.montar(pedido, idioma, false, erro, "", email));
    }
}
