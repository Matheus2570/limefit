package br.com.limefit.modulos.planejamento;

import br.com.limefit.autenticacao.Sessao;
import br.com.limefit.banco.Conexao;
import br.com.limefit.banco.DaoDaConta;
import br.com.limefit.modulos.refeicoes.Refeicao;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Refeições e totais da data em uma leitura consistente, limitada à conta. */
public final class PlanejamentoDao extends DaoDaConta {
    public PlanejamentoDao(Conexao conexao, Sessao sessao) { super(conexao, sessao); }

    public PlanoDiario buscarPorData(LocalDate data) throws SQLException {
        long dono = usuarioId();
        Objects.requireNonNull(data, "data");
        try (Connection c = conexao.abrir()) {
            c.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
            c.setAutoCommit(false);
            try {
                List<Refeicao> refeicoes = new ArrayList<>();
                try (PreparedStatement p = c.prepareStatement("SELECT * FROM refeicoes WHERE usuario_id=? AND data_plano=? ORDER BY id")) {
                    p.setLong(1, dono); p.setDate(2, Date.valueOf(data));
                    try (ResultSet r = p.executeQuery()) {
                        while (r.next()) refeicoes.add(new Refeicao(r.getLong("id"), dono, r.getLong("perfil_id"),
                                r.getDate("data_plano").toLocalDate(), r.getString("nome")));
                    }
                }
                PlanoDiario resultado = new PlanoDiario(data, refeicoes, somar(c, dono, data));
                c.commit();
                return resultado;
            } catch (SQLException | RuntimeException e) {
                try { c.rollback(); } catch (SQLException rollback) { e.addSuppressed(rollback); }
                throw e;
            }
        }
    }

    public TotaisNutricionais calcularTotais(LocalDate data) throws SQLException {
        long dono = usuarioId();
        Objects.requireNonNull(data, "data");
        try (Connection c = conexao.abrir()) { return somar(c, dono, data); }
    }

    private static TotaisNutricionais somar(Connection c, long dono, LocalDate data) throws SQLException {
        try (PreparedStatement p = c.prepareStatement("""
                SELECT COALESCE(SUM(a.calorias*i.quantidade_g/100),0) AS calorias,
                       COALESCE(SUM(a.proteinas*i.quantidade_g/100),0) AS proteinas,
                       COALESCE(SUM(a.carboidratos*i.quantidade_g/100),0) AS carboidratos,
                       COALESCE(SUM(a.gorduras*i.quantidade_g/100),0) AS gorduras
                FROM refeicoes r
                JOIN itens_refeicao i ON i.refeicao_id=r.id AND i.usuario_id=r.usuario_id
                JOIN alimentos a ON a.id=i.alimento_id AND a.usuario_id=i.usuario_id
                WHERE r.usuario_id=? AND r.data_plano=?
                """)) {
            p.setLong(1, dono); p.setDate(2, Date.valueOf(data));
            try (ResultSet r = p.executeQuery()) {
                if (!r.next()) return TotaisNutricionais.ZERO;
                return new TotaisNutricionais(r.getBigDecimal("calorias"), r.getBigDecimal("proteinas"),
                        r.getBigDecimal("carboidratos"), r.getBigDecimal("gorduras"));
            }
        }
    }
}
