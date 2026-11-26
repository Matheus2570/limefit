package br.com.limefit.modulos.perfil;

import java.math.BigDecimal;

/** Altura em centímetros; peso em quilogramas. identificador é um token técnico único de 64 caracteres. */
public record Perfil(long id, long usuarioId, String identificador, BigDecimal pesoKg,
                     BigDecimal alturaCm, Objetivo objetivo) {
    public BigDecimal calcularImc() {
        if (pesoKg == null || alturaCm == null || pesoKg.signum() <= 0 || alturaCm.signum() <= 0)
            throw new IllegalStateException("Peso e altura devem ser positivos / Weight and height must be positive.");
        return pesoKg.divide(alturaCm.movePointLeft(2).pow(2), 2, java.math.RoundingMode.HALF_UP);
    }
}
