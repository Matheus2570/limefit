package br.com.limefit.autenticacao;

/** Uma sessão por aplicação. Não contém senha, hash ou dados dos módulos. */
public final class Sessao {
    private Usuario usuario;
    private long revisao;
    public synchronized Usuario usuario() { return usuario; }
    public synchronized boolean autenticada() { return usuario != null; }
    public synchronized long revisao() { return revisao; }
    public synchronized long exigirUsuarioId() {
        if (usuario == null) throw new IllegalStateException("Login required / Entre na sua conta");
        return usuario.id();
    }
    public synchronized void entrar(Usuario usuario) {
        this.usuario = java.util.Objects.requireNonNull(usuario);
        revisao++;
    }
    public synchronized void sair() {
        usuario = null;
        revisao++;
    }
}
