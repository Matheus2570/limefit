package br.com.limefit.modulos.perfil;

/** Valores exatos do ENUM perfis.objetivo. Não renomear os códigos SQL. */
public enum Objetivo {
    GANHAR_MASSA("mass"), PERDER_PESO("loss"), EQUILIBRAR("balance"), MANTER("maintain");
    private final String codigo;
    Objetivo(String codigo) { this.codigo = codigo; }
    public String codigo() { return codigo; }
    public static Objetivo doCodigo(String codigo) {
        for (Objetivo objetivo : values()) if (objetivo.codigo.equals(codigo)) return objetivo;
        throw new IllegalArgumentException("Objetivo: " + codigo);
    }
}
