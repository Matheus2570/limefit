package br.com.limefit.modulos.perfil;

import java.math.BigDecimal;

/** Altura em centímetros; peso em quilogramas. identificador é um token técnico único de 64 caracteres. */
public record Perfil(long id, long usuarioId, String identificador, BigDecimal pesoKg,
                     BigDecimal alturaCm, Objetivo objetivo) {
    public BigDecimal calcularImc() {
        throw new UnsupportedOperationException("IMC ainda precisa ser implementado / BMI must still be implemented.");
    }
}
