package br.com.limefit.modulos.planejamento;

import br.com.limefit.modulos.refeicoes.Refeicao;
import java.time.LocalDate;
import java.util.List;

/** Visão calculada; não existe tabela planejamento. As datas estão em refeicoes.data_plano. */
public record PlanoDiario(LocalDate data, List<Refeicao> refeicoes, TotaisNutricionais totais) {
    public PlanoDiario { refeicoes = List.copyOf(refeicoes); }
}
