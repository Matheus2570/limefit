package br.com.limefit.modulos.alimentos;

import br.com.limefit.autenticacao.Sessao;
import br.com.limefit.banco.Conexao;
import br.com.limefit.banco.DaoDaConta;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class AlimentoDao extends DaoDaConta {
    private static final String CONSULTA = """
            SELECT id, usuario_id, nome, calorias, proteinas, carboidratos, gorduras
            FROM alimentos WHERE usuario_id = ?
            """;

    public AlimentoDao(Conexao conexao, Sessao sessao) { super(conexao, sessao); }

    public List<Alimento> listar(String filtroNome) throws SQLException {
        long dono = usuarioId();
        String filtro = filtroNome.strip().replace("!", "!!").replace("%", "!%").replace("_", "!_");
        try (var c = conexao.abrir();
             var sql = c.prepareStatement(CONSULTA + " AND nome LIKE ? ESCAPE '!' ORDER BY nome")) {
            sql.setLong(1, dono);
            sql.setString(2, "%" + filtro + "%");
            try (var linhas = sql.executeQuery()) {
                List<Alimento> alimentos = new ArrayList<>();
                while (linhas.next()) alimentos.add(ler(linhas));
                return alimentos;
            }
        }
    }

    public Optional<Alimento> buscarPorId(long id) throws SQLException {
        long dono = usuarioId();
        try (var c = conexao.abrir(); var sql = c.prepareStatement(CONSULTA + " AND id = ?")) {
            sql.setLong(1, dono);
            sql.setLong(2, id);
            try (var linhas = sql.executeQuery()) {
                return linhas.next() ? Optional.of(ler(linhas)) : Optional.empty();
            }
        }
    }

    public Alimento cadastrar(String nome, BigDecimal calorias, BigDecimal proteinas,
                             BigDecimal carboidratos, BigDecimal gorduras) throws SQLException {
        long dono = usuarioId();
        Alimento dados = dados(0, dono, nome, calorias, proteinas, carboidratos, gorduras);
        try (var c = conexao.abrir();
             var sql = c.prepareStatement("""
                     INSERT INTO alimentos (nome, calorias, proteinas, carboidratos, gorduras, usuario_id)
                     VALUES (?, ?, ?, ?, ?, ?)
                     """, Statement.RETURN_GENERATED_KEYS)) {
            preencher(sql, dados);
            sql.setLong(6, dono);
            sql.executeUpdate();
            try (var chaves = sql.getGeneratedKeys()) {
                if (!chaves.next()) throw new SQLException("Missing generated food ID");
                return new Alimento(chaves.getLong(1), dono, dados.nome(), dados.calorias(),
                        dados.proteinas(), dados.carboidratos(), dados.gorduras());
            }
        }
    }

    public boolean atualizar(long id, String nome, BigDecimal calorias, BigDecimal proteinas,
                             BigDecimal carboidratos, BigDecimal gorduras) throws SQLException {
        long dono = usuarioId();
        Alimento dados = dados(id, dono, nome, calorias, proteinas, carboidratos, gorduras);
        try (var c = conexao.abrir();
             var sql = c.prepareStatement("""
                     UPDATE alimentos SET nome = ?, calorias = ?, proteinas = ?, carboidratos = ?, gorduras = ?
                     WHERE usuario_id = ? AND id = ?
                     """)) {
            preencher(sql, dados);
            sql.setLong(6, dono);
            sql.setLong(7, id);
            return sql.executeUpdate() == 1;
        }
    }

    public boolean excluir(long id) throws SQLException {
        long dono = usuarioId();
        try (var c = conexao.abrir();
             var sql = c.prepareStatement("DELETE FROM alimentos WHERE usuario_id = ? AND id = ?")) {
            sql.setLong(1, dono);
            sql.setLong(2, id);
            return sql.executeUpdate() == 1;
        }
    }

    private static Alimento ler(ResultSet linha) throws SQLException {
        return new Alimento(linha.getLong("id"), linha.getLong("usuario_id"), linha.getString("nome"),
                linha.getBigDecimal("calorias"), linha.getBigDecimal("proteinas"),
                linha.getBigDecimal("carboidratos"), linha.getBigDecimal("gorduras"));
    }

    private static void preencher(PreparedStatement sql, Alimento dados) throws SQLException {
        sql.setString(1, dados.nome());
        sql.setBigDecimal(2, dados.calorias());
        sql.setBigDecimal(3, dados.proteinas());
        sql.setBigDecimal(4, dados.carboidratos());
        sql.setBigDecimal(5, dados.gorduras());
    }

    private static Alimento dados(long id, long dono, String nome, BigDecimal calorias,
                                 BigDecimal proteinas, BigDecimal carboidratos, BigDecimal gorduras) {
        if (nome == null || nome.isBlank() || nome.strip().length() > 100) {
            throw new IllegalArgumentException("Nome inválido / Invalid name");
        }
        return new Alimento(id, dono, nome.strip(), validar(calorias, 1000), validar(proteinas, 100),
                validar(carboidratos, 100), validar(gorduras, 100));
    }

    static BigDecimal validar(BigDecimal valor, int maximo) {
        if (valor == null || valor.signum() < 0 || valor.compareTo(BigDecimal.valueOf(maximo)) > 0
                || valor.stripTrailingZeros().scale() > 2) {
            throw new IllegalArgumentException("Nutriente inválido / Invalid nutrient");
        }
        return valor.setScale(2);
    }
}