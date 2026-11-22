package br.com.limefit.modulos.login;

import br.com.limefit.autenticacao.Usuario;
import br.com.limefit.desktop.ContextoAplicacao;

public final class TelaLogin extends FormularioConta {
    private final Runnable aposLogin;
    public TelaLogin(ContextoAplicacao contexto, Runnable aposLogin) {
        super(contexto, false);
        this.aposLogin = aposLogin;
    }
    @Override protected void concluir(Usuario usuario) {
        contexto.entrar(usuario);
        aposLogin.run();
    }
}
