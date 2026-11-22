package br.com.limefit.modulos.refeicoes;

import java.math.BigDecimal;

/** Chave primária composta (refeicaoId, alimentoId); porção em gramas. */
public record ItemRefeicao(long refeicaoId, long alimentoId, long usuarioId, BigDecimal quantidadeG) {}
