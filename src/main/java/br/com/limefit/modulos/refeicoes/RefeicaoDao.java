package br.com.limefit.modulos.refeicoes;

import br.com.limefit.autenticacao.Sessao;
import br.com.limefit.banco.Conexao;
import br.com.limefit.banco.DaoDaConta;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Acesso às refeições sempre limitado à conta autenticada. */
public final class RefeicaoDao extends DaoDaConta {
    public RefeicaoDao(Conexao conexao, Sessao sessao) { super(conexao, sessao); }

    public List<Refeicao> listarPorData(LocalDate data) throws SQLException {
        long dono = usuarioId();
        String sql = "SELECT id, usuario_id, perfil_id, data_plano, nome FROM refeicoes WHERE data_plano = ? AND usuario_id = ? ORDER BY id";
        List<Refeicao> resultado = new ArrayList<>();
        try (Connection c = conexao.abrir(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setDate(1, Date.valueOf(data)); p.setLong(2, dono);
            try (ResultSet r = p.executeQuery()) { while (r.next()) resultado.add(mapear(r)); }
        }
        return resultado;
    }

    public Optional<Refeicao> buscarPorId(long id) throws SQLException {
        long dono = usuarioId();
        try (Connection c = conexao.abrir(); PreparedStatement p = c.prepareStatement(
                "SELECT id, usuario_id, perfil_id, data_plano, nome FROM refeicoes WHERE id = ? AND usuario_id = ?")) {
            p.setLong(1, id); p.setLong(2, dono);
            try (ResultSet r = p.executeQuery()) { return r.next() ? Optional.of(mapear(r)) : Optional.empty(); }
        }
    }

    public Refeicao cadastrar(long perfilId, LocalDate data, String nome) throws SQLException {
        long dono = usuarioId();
        validarNome(nome);
        String sql = "INSERT INTO refeicoes (perfil_id, data_plano, nome, usuario_id) SELECT id, ?, ?, usuario_id FROM perfis WHERE id = ? AND usuario_id = ?";
        try (Connection c = conexao.abrir(); PreparedStatement p = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            p.setDate(1, Date.valueOf(data)); p.setString(2, nome.trim()); p.setLong(3, perfilId); p.setLong(4, dono);
            if (p.executeUpdate() != 1) throw new SQLException("Perfil não encontrado nesta conta / Profile not found in this account.");
            try (ResultSet r = p.getGeneratedKeys()) { if (!r.next()) throw new SQLException("Não foi possível obter o ID da refeição."); return new Refeicao(r.getLong(1), dono, perfilId, data, nome.trim()); }
        }
    }

    public boolean atualizar(long id, LocalDate data, String nome) throws SQLException {
        long dono = usuarioId(); validarNome(nome);
        try (Connection c = conexao.abrir(); PreparedStatement p = c.prepareStatement(
                "UPDATE refeicoes SET data_plano = ?, nome = ? WHERE id = ? AND usuario_id = ?")) {
            p.setDate(1, Date.valueOf(data)); p.setString(2, nome.trim()); p.setLong(3, id); p.setLong(4, dono);
            return p.executeUpdate() == 1;
        }
    }

    public boolean excluir(long id) throws SQLException {
        long dono = usuarioId();
        try (Connection c = conexao.abrir(); PreparedStatement p = c.prepareStatement("DELETE FROM refeicoes WHERE id = ? AND usuario_id = ?")) {
            p.setLong(1, id); p.setLong(2, dono); return p.executeUpdate() == 1;
        }
    }

    public List<ItemRefeicao> listarItens(long refeicaoId) throws SQLException {
        long dono = usuarioId();
        String sql = "SELECT refeicao_id, alimento_id, usuario_id, quantidade_g FROM itens_refeicao WHERE refeicao_id = ? AND usuario_id = ? ORDER BY alimento_id";
        List<ItemRefeicao> resultado = new ArrayList<>();
        try (Connection c = conexao.abrir(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setLong(1, refeicaoId); p.setLong(2, dono);
            try (ResultSet r = p.executeQuery()) { while (r.next()) resultado.add(new ItemRefeicao(r.getLong("refeicao_id"), r.getLong("alimento_id"), r.getLong("usuario_id"), r.getBigDecimal("quantidade_g"))); }
        }
        return resultado;
    }

    public void definirPorcao(long refeicaoId, long alimentoId, BigDecimal quantidadeG) throws SQLException {
        long dono = usuarioId();
        if (quantidadeG == null || quantidadeG.stripTrailingZeros().scale() > 2 || quantidadeG.signum() <= 0 || quantidadeG.compareTo(new BigDecimal("10000")) > 0)
            throw new IllegalArgumentException("A porção deve ser maior que zero e até 10000 g.");
        String verificar = "SELECT (SELECT COUNT(*) FROM refeicoes WHERE id = ? AND usuario_id = ?) AS refeicao_ok, (SELECT COUNT(*) FROM alimentos WHERE id = ? AND usuario_id = ?) AS alimento_ok";
        String gravar = "INSERT INTO itens_refeicao (refeicao_id, alimento_id, quantidade_g, usuario_id) VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE quantidade_g = VALUES(quantidade_g)";
        try (Connection c = conexao.abrir()) {
            c.setAutoCommit(false);
            try {
                try (PreparedStatement p = c.prepareStatement(verificar)) {
                    p.setLong(1, refeicaoId); p.setLong(2, dono); p.setLong(3, alimentoId); p.setLong(4, dono);
                    try (ResultSet r = p.executeQuery()) {
                        if (!r.next() || r.getInt("refeicao_ok") != 1 || r.getInt("alimento_ok") != 1)
                            throw new SQLException("Refeição ou alimento não pertence a esta conta.");
                    }
                }
                try (PreparedStatement p = c.prepareStatement(gravar)) {
                    p.setLong(1, refeicaoId); p.setLong(2, alimentoId); p.setBigDecimal(3, quantidadeG); p.setLong(4, dono); p.executeUpdate();
                }
                c.commit();
            } catch (SQLException | RuntimeException e) { c.rollback(); throw e; }
            finally { c.setAutoCommit(true); }
        }
    }

    public boolean removerItem(long refeicaoId, long alimentoId) throws SQLException {
        long dono = usuarioId();
        try (Connection c = conexao.abrir(); PreparedStatement p = c.prepareStatement(
                "DELETE FROM itens_refeicao WHERE refeicao_id = ? AND alimento_id = ? AND usuario_id = ?")) {
            p.setLong(1, refeicaoId); p.setLong(2, alimentoId); p.setLong(3, dono); return p.executeUpdate() == 1;
        }
    }

    private Refeicao mapear(ResultSet r) throws SQLException {
        return new Refeicao(r.getLong("id"), r.getLong("usuario_id"), r.getLong("perfil_id"), r.getDate("data_plano").toLocalDate(), r.getString("nome"));
    }
    private void validarNome(String nome) {
        if (nome == null || nome.isBlank() || nome.trim().length() > 100) throw new IllegalArgumentException("O nome deve ter entre 1 e 100 caracteres.");
    }
}
