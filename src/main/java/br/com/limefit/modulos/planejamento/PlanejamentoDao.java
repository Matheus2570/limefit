package br.com.limefit.modulos.planejamento;

import br.com.limefit.autenticacao.Sessao;
import br.com.limefit.banco.Conexao;
import br.com.limefit.banco.DaoDaConta;
import java.sql.SQLException;
import java.time.LocalDate;

/** Contrato pendente. Implementar SQL com usuarioId() em TODA leitura e gravação. */
public final class PlanejamentoDao extends DaoDaConta {
    public PlanejamentoDao(Conexao conexao, Sessao sessao) { super(conexao, sessao); }

    /** Consultar refeições e calcular totais em uma conexão/transação de leitura consistente. Mesmo usuário em todos os JOINs. */
    public PlanoDiario buscarPorData(LocalDate data) throws SQLException {
        throw pendente("PlanejamentoDao.buscarPorData");
    }

    /** SUM de alimentos.nutriente * itens_refeicao.quantidade_g / 100. COALESCE a zero se vazio; filtrar data e usuário. */
    public TotaisNutricionais calcularTotais(LocalDate data) throws SQLException {
        throw pendente("PlanejamentoDao.calcularTotais");
    }
}
