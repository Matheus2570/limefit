package br.com.limefit.autenticacao;

public final class Sessao {
    private final Usuario usuario;
    private final String csrf;
    private volatile long ultimoAcesso = System.currentTimeMillis();

    Sessao(Usuario usuario, String csrf) {
        this.usuario = usuario;
        this.csrf = csrf;
    }

    public Usuario usuario() { return usuario; }
    public String csrf() { return csrf; }
    boolean expirada() { return System.currentTimeMillis() - ultimoAcesso > 30 * 60_000L; }
    void renovar() { ultimoAcesso = System.currentTimeMillis(); }
}
