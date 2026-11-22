package br.com.limefit.modulos.refeicoes;

import java.time.LocalDate;

public record Refeicao(long id, long usuarioId, long perfilId, LocalDate dataPlano, String nome) {}
