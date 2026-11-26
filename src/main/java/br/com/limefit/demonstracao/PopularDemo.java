package br.com.limefit.demonstracao;

import br.com.limefit.autenticacao.Senhas;
import br.com.limefit.banco.Conexao;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;

/** Carga portátil: usa somente o Java, o JDBC e a configuração do aplicativo. */
public final class PopularDemo {
    public static final String EMAIL = "demo@limefit.local";
    public static final String SENHA = "LimeFit123!";

    private PopularDemo() { }

    public static void main(String[] args) throws Exception {
        try {
            LocalDate data = args.length == 0 ? LocalDate.now() : LocalDate.parse(args[0]);
            if (args.length > 1) throw new IllegalArgumentException("Use no máximo uma data AAAA-MM-DD.");
            popular(new Conexao(), data);
            System.out.println("Demonstração pronta: 15 alimentos, 12 refeições e 43 porções.");
            System.out.println("Datas: " + data.minusDays(1) + " a " + data.plusDays(1));
            System.out.println("E-mail: " + EMAIL + " | Senha: " + SENHA);
            System.out.println("Dados existentes preservados. Valores nutricionais ilustrativos para apresentação.");
            System.out.println("Entre com a conta demo. Se já estiver aberta, acesse outra tela e volte.");
        } catch (SQLException e) {
            System.err.println("Não foi possível preparar os dados. Confira se o MariaDB/MySQL está ligado,");
            System.err.println("se o esquema limefit foi instalado e se LIMEFIT_DB_PORT/NAME/USER/PASSWORD estão corretos.");
            throw e;
        }
    }

    /** Uma transação; nunca sobrescreve perfil, alimentos ou porções já cadastrados. */
    public static void popular(Conexao conexao, LocalDate data) throws SQLException, IOException {
        if (data == null || data.isBefore(LocalDate.of(1000, 1, 2)) || data.isAfter(LocalDate.of(9998, 12, 31))) {
            throw new IllegalArgumentException("Escolha uma data entre 1000-01-02 e 9998-12-31.");
        }
        List<String[]> alimentos = lerRecurso("alimentos.csv", 6);
        List<String[]> porcoes = lerRecurso("porcoes.csv", 4);
        try (Connection c = conexao.abrir()) {
            c.setAutoCommit(false);
            try {
                long dono = obterConta(c);
                long perfil = obterPerfil(c, dono);
                Map<String, Long> idsAlimentos = new HashMap<>();
                for (String[] alimento : alimentos) {
                    idsAlimentos.put(alimento[0], obterAlimento(c, dono, alimento));
                }
                Map<String, Long> idsRefeicoes = new HashMap<>();
                for (String[] porcao : porcoes) {
                    LocalDate dia = data.plusDays(Integer.parseInt(porcao[0]));
                    String chave = dia + "/" + porcao[1];
                    Long refeicao = idsRefeicoes.get(chave);
                    if (refeicao == null) {
                        refeicao = obterRefeicao(c, dono, perfil, dia, porcao[1]);
                        idsRefeicoes.put(chave, refeicao);
                    }
                    Long alimento = idsAlimentos.get(porcao[2]);
                    if (alimento == null) throw new IOException("Alimento desconhecido na carga: " + porcao[2]);
                    try (var p = c.prepareStatement("""
                            INSERT INTO itens_refeicao(refeicao_id,alimento_id,quantidade_g,usuario_id)
                            VALUES(?,?,?,?) ON DUPLICATE KEY UPDATE quantidade_g=quantidade_g
                            """)) {
                        p.setLong(1, refeicao); p.setLong(2, alimento);
                        p.setBigDecimal(3, new BigDecimal(porcao[3])); p.setLong(4, dono);
                        p.executeUpdate();
                    }
                }
                c.commit();
            } catch (SQLException | IOException | RuntimeException e) {
                try { c.rollback(); } catch (SQLException rollback) { e.addSuppressed(rollback); }
                throw e;
            }
        }
    }

    private static long obterConta(Connection c) throws SQLException {
        try (var p = c.prepareStatement("""
                INSERT INTO usuarios(nome,email,senha_hash) VALUES(?,?,?)
                ON DUPLICATE KEY UPDATE id=LAST_INSERT_ID(id)
                """)) {
            p.setString(1, "Apresentação LimeFit"); p.setString(2, EMAIL); p.setString(3, Senhas.criar(SENHA));
            p.executeUpdate();
        }
        // Bloqueia a conta durante a carga, inclusive se dois comandos forem iniciados juntos.
        try (var p = c.prepareStatement("SELECT id,senha_hash FROM usuarios WHERE email=? FOR UPDATE")) {
            p.setString(1, EMAIL);
            try (var r = p.executeQuery()) {
                if (!r.next()) throw new SQLException("Conta demo não encontrada.");
                if (!Senhas.verificar(SENHA, r.getString("senha_hash"))) {
                    throw new IllegalStateException("A conta demo já existe com outra senha. Nenhum dado foi alterado.");
                }
                return r.getLong("id");
            }
        }
    }

    private static long obterPerfil(Connection c, long dono) throws SQLException {
        byte[] token = new byte[32];
        new SecureRandom().nextBytes(token);
        try (var p = c.prepareStatement("""
                INSERT INTO perfis(identificador,peso_kg,altura_cm,objetivo,usuario_id)
                VALUES(?,75,175,'balance',?) ON DUPLICATE KEY UPDATE id=LAST_INSERT_ID(id)
                """)) {
            p.setString(1, HexFormat.of().formatHex(token)); p.setLong(2, dono); p.executeUpdate();
        }
        try (var p = c.prepareStatement("SELECT id FROM perfis WHERE usuario_id=?")) {
            p.setLong(1, dono);
            try (var r = p.executeQuery()) {
                if (!r.next()) throw new SQLException("Perfil demo não encontrado.");
                return r.getLong(1);
            }
        }
    }

    private static long obterAlimento(Connection c, long dono, String[] dados) throws SQLException {
        try (var p = c.prepareStatement("""
                INSERT INTO alimentos(nome,calorias,proteinas,carboidratos,gorduras,usuario_id)
                VALUES(?,?,?,?,?,?) ON DUPLICATE KEY UPDATE id=LAST_INSERT_ID(id)
                """)) {
            p.setString(1, dados[1]);
            for (int i = 2; i < 6; i++) p.setBigDecimal(i, new BigDecimal(dados[i]));
            p.setLong(6, dono); p.executeUpdate();
        }
        try (var p = c.prepareStatement("SELECT id FROM alimentos WHERE usuario_id=? AND nome=?")) {
            p.setLong(1, dono); p.setString(2, dados[1]);
            try (var r = p.executeQuery()) {
                if (!r.next()) throw new SQLException("Alimento demo não encontrado.");
                return r.getLong(1);
            }
        }
    }

    private static long obterRefeicao(Connection c, long dono, long perfil, LocalDate dia, String nome) throws SQLException {
        try (var p = c.prepareStatement("SELECT id FROM refeicoes WHERE usuario_id=? AND data_plano=? AND nome=? ORDER BY id LIMIT 1")) {
            p.setLong(1, dono); p.setDate(2, java.sql.Date.valueOf(dia)); p.setString(3, nome);
            try (var r = p.executeQuery()) { if (r.next()) return r.getLong(1); }
        }
        try (var p = c.prepareStatement("INSERT INTO refeicoes(perfil_id,data_plano,nome,usuario_id) VALUES(?,?,?,?)", Statement.RETURN_GENERATED_KEYS)) {
            p.setLong(1, perfil); p.setDate(2, java.sql.Date.valueOf(dia)); p.setString(3, nome); p.setLong(4, dono);
            p.executeUpdate();
            try (var r = p.getGeneratedKeys()) {
                if (!r.next()) throw new SQLException("ID da refeição demo ausente.");
                return r.getLong(1);
            }
        }
    }

    private static List<String[]> lerRecurso(String nome, int colunas) throws IOException {
        var stream = PopularDemo.class.getResourceAsStream("/demo/" + nome);
        if (stream == null) throw new IOException("Recurso de demonstração ausente: " + nome);
        List<String[]> linhas = new ArrayList<>();
        try (var leitor = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            for (String linha; (linha = leitor.readLine()) != null;) {
                if (linha.isBlank() || linha.startsWith("#")) continue;
                String[] campos = linha.split(";", -1);
                if (campos.length != colunas) throw new IOException("Linha inválida em " + nome);
                linhas.add(campos);
            }
        }
        return linhas;
    }
}
