package br.com.limefit.modulos.planejamento;

import java.math.BigDecimal;

/** Soma de nutrientes por 100 g multiplicados por quantidade_g / 100; arredondar só ao exibir. */
public record TotaisNutricionais(BigDecimal calorias, BigDecimal proteinas,
                                 BigDecimal carboidratos, BigDecimal gorduras) {}
