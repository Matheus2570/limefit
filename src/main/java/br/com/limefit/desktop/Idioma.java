package br.com.limefit.desktop;

/** Compartilhado com todos os módulos. A escolha inicial é inglês. */
public final class Idioma {
    private boolean ingles = true;
    public String escolher(String portugues, String english) { return ingles ? english : portugues; }
    public boolean ingles() { return ingles; }
    public void usarIngles(boolean ingles) { this.ingles = ingles; }
}
