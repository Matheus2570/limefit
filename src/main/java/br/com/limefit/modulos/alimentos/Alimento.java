package br.com.limefit.modulos.alimentos;

import java.math.BigDecimal;

/** Todos os nutrientes são por 100 g; energia em kcal e macronutrientes em gramas. */
public record Alimento(long id, long usuarioId, String nome, BigDecimal calorias,
                       BigDecimal proteinas, BigDecimal carboidratos, BigDecimal gorduras) {}
