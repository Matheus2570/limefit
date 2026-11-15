package br.com.limefit.autenticacao;

// Identidade carregada do banco; nunca preenchida por um formulário.
public record Usuario(long id, String nome, String email) {}
