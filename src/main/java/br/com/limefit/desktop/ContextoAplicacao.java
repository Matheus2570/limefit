package br.com.limefit.desktop;

import br.com.limefit.autenticacao.Sessao;
import br.com.limefit.autenticacao.Usuario;
import br.com.limefit.autenticacao.UsuarioDao;
import br.com.limefit.banco.Conexao;
import java.util.function.Consumer;

/** Recursos recebidos pelos módulos no construtor; sem estado global estático. */
public final class ContextoAplicacao {
    private final Conexao conexao = new Conexao();
    private final Sessao sessao = new Sessao();
    private final Idioma idioma = new Idioma();
    private final UsuarioDao usuarios = new UsuarioDao(conexao);
    private Consumer<Tela> navegacao;
    private Runnable atualizar = () -> {};
    private Runnable limpar = () -> {};

    public Conexao conexao() { return conexao; }
    public Sessao sessao() { return sessao; }
    public Idioma idioma() { return idioma; }
    public UsuarioDao usuarios() { return usuarios; }
    public long usuarioId() { return sessao.exigirUsuarioId(); }
    public void navegar(Tela tela) { navegacao.accept(tela); }

    void conectar(Consumer<Tela> navegacao, Runnable atualizar, Runnable limpar) {
        this.navegacao = navegacao;
        this.atualizar = atualizar;
        this.limpar = limpar;
    }

    public void mudarIdioma(boolean ingles) {
        idioma.usarIngles(ingles);
        atualizar.run();
    }

    public void entrar(Usuario usuario) {
        limpar.run();
        sessao.entrar(usuario);
        atualizar.run();
    }

    public void sair() {
        sessao.sair();
        limpar.run();
        atualizar.run();
        navegar(Tela.INICIO);
    }
}
