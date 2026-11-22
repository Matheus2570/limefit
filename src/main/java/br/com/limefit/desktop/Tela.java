package br.com.limefit.desktop;

public enum Tela {
    INICIO("Início", "Home", false), LOGIN("Entrar", "Log in", false),
    CADASTRO("Criar conta", "Create account", false),
    CADASTRO_SUCESSO("Conta criada", "Account created", false),
    PERFIL("Meu perfil", "My profile", true), ALIMENTOS("Alimentos", "Foods", true),
    REFEICOES("Refeições", "Meals", true), PLANEJAMENTO("Planejamento", "Planning", true),
    PROJETO("O projeto", "The project", true);

    private final String pt, en;
    private final boolean privada;
    Tela(String pt, String en, boolean privada) { this.pt = pt; this.en = en; this.privada = privada; }
    public String titulo(Idioma idioma) { return idioma.escolher(pt, en); }
    public boolean privada() { return privada; }
}
