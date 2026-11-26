package br.com.limefit.modulos.perfil;

import br.com.limefit.autenticacao.Sessao;
import br.com.limefit.banco.Conexao;
import br.com.limefit.banco.DaoDaConta;
import java.math.BigDecimal;
import java.sql.*;
import java.util.HexFormat;
import java.util.Optional;
import java.security.SecureRandom;

/** Um perfil por conta; o identificador técnico é preservado nas atualizações. */
public final class PerfilDao extends DaoDaConta {
    public PerfilDao(Conexao conexao, Sessao sessao) { super(conexao, sessao); }

    public Optional<Perfil> buscarAtual() throws SQLException {
        long dono = usuarioId();
        try (Connection c = conexao.abrir()) { return buscar(c, dono); }
    }

    public Perfil salvar(BigDecimal pesoKg, BigDecimal alturaCm, Objetivo objetivo) throws SQLException {
        long dono = usuarioId();
        validarMedida(pesoKg, 25, 300);
        validarMedida(alturaCm, 100, 230);
        if (objetivo == null) throw new IllegalArgumentException("Selecione um objetivo / Select a goal.");
        byte[] token = new byte[32];
        new SecureRandom().nextBytes(token);
        try (Connection c = conexao.abrir()) {
            c.setAutoCommit(false);
            try {
                // A chave única usuario_id também protege contra duas gravações simultâneas.
                try (PreparedStatement p = c.prepareStatement("""
                        INSERT INTO perfis(identificador,peso_kg,altura_cm,objetivo,usuario_id)
                        VALUES(?,?,?,?,?) ON DUPLICATE KEY UPDATE
                        peso_kg=VALUES(peso_kg), altura_cm=VALUES(altura_cm), objetivo=VALUES(objetivo)
                        """)) {
                    p.setString(1, HexFormat.of().formatHex(token));
                    p.setBigDecimal(2, pesoKg); p.setBigDecimal(3, alturaCm);
                    p.setString(4, objetivo.codigo()); p.setLong(5, dono);
                    p.executeUpdate();
                }
                Perfil perfil = buscar(c, dono).orElseThrow(() -> new SQLException("Profile not found after saving"));
                c.commit();
                return perfil;
            } catch (SQLException | RuntimeException e) {
                try { c.rollback(); } catch (SQLException rollback) { e.addSuppressed(rollback); }
                throw e;
            }
        }
    }

    public boolean excluirAtual() throws SQLException {
        long dono = usuarioId();
        try (Connection c = conexao.abrir(); PreparedStatement p = c.prepareStatement("DELETE FROM perfis WHERE usuario_id=?")) {
            p.setLong(1, dono);
            return p.executeUpdate() > 0;
        }
    }

    static void validarMedida(BigDecimal valor, int minimo, int maximo) {
        if (valor == null || valor.compareTo(BigDecimal.valueOf(minimo)) < 0
                || valor.compareTo(BigDecimal.valueOf(maximo)) > 0 || valor.stripTrailingZeros().scale() > 2)
            throw new IllegalArgumentException("Medida inválida / Invalid measurement.");
    }

    private static Optional<Perfil> buscar(Connection c, long dono) throws SQLException {
        try (PreparedStatement p = c.prepareStatement("SELECT * FROM perfis WHERE usuario_id=?")) {
            p.setLong(1, dono);
            try (ResultSet r = p.executeQuery()) {
                return r.next() ? Optional.of(new Perfil(r.getLong("id"), dono, r.getString("identificador"),
                        r.getBigDecimal("peso_kg"), r.getBigDecimal("altura_cm"), Objetivo.doCodigo(r.getString("objetivo"))))
                        : Optional.empty();
            }
        }
    }
}
