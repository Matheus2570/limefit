package br.com.limefit.web;

public enum Tela {
    INICIO("/", "Início", "Home", false, true),
    PERFIL("/perfil", "Perfil", "Profile", true, true),
    ALIMENTOS("/alimentos", "Alimentos", "Foods", true, true),
    REFEICOES("/refeicoes", "Refeições", "Meals", true, true),
    PLANEJAMENTO("/planejamento", "Planejamento", "Planner", true, true),
    PROJETO("/projeto", "Projeto", "Project", true, true),
    LOGIN("/login", "Entrar", "Log in", false, false),
    CADASTRO("/cadastro", "Criar conta", "Create account", false, false),
    CADASTRO_SUCESSO("/cadastro/sucesso", "Conta criada", "Account created", false, false);

    private final String rota, portugues, ingles;
    private final boolean privada, menu;

    Tela(String rota, String portugues, String ingles, boolean privada, boolean menu) {
        this.rota = rota;
        this.portugues = portugues;
        this.ingles = ingles;
        this.privada = privada;
        this.menu = menu;
    }

    public String rota() { return rota; }
    public String titulo(Idioma idioma) { return idioma.escolher(portugues, ingles); }
    public boolean privada() { return privada; }
    public boolean noMenu() { return menu; }
}
