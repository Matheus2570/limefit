package br.com.limefit.web;

import java.net.URI;
import java.util.Arrays;

public enum Idioma {
    PORTUGUES("pt-BR", "pt"), INGLES("en", "en");

    private final String tag;
    private final String parametro;

    Idioma(String tag, String parametro) {
        this.tag = tag;
        this.parametro = parametro;
    }

    public static Idioma daRequisicao(URI uri) {
        return uri.getRawQuery() != null && Arrays.asList(uri.getRawQuery().split("&")).contains("lang=pt")
                ? PORTUGUES : INGLES;
    }

    public String escolher(String portugues, String ingles) { return this == INGLES ? ingles : portugues; }
    public Idioma outro() { return this == INGLES ? PORTUGUES : INGLES; }
    public String tag() { return tag; }
    public String consulta() { return "?lang=" + parametro; }
    public String url(String rota) { return rota + consulta(); }
}
