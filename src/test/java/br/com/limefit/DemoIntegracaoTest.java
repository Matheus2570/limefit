package br.com.limefit;

import br.com.limefit.autenticacao.*;
import br.com.limefit.banco.Conexao;
import br.com.limefit.demonstracao.PopularDemo;
import br.com.limefit.modulos.planejamento.PlanejamentoDao;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import static org.junit.jupiter.api.Assertions.*;

@EnabledIfEnvironmentVariable(named = "LIMEFIT_TEST_DB", matches = "limefit_validacao_[0-9]+")
class DemoIntegracaoTest {
    private final Conexao conexao = new Conexao();

    @Test void cargaPortatilAutenticaPreservaEdicoesENaoDuplica() throws Exception {
        assertEquals(System.getenv("LIMEFIT_TEST_DB"), System.getenv("LIMEFIT_DB_NAME"));
        var usuarios = new UsuarioDao(conexao);
        usuarios.cadastrar("Outra demo", "outra-demo@example.test", "SenhaTeste-123");
        LocalDate data = LocalDate.of(2026, 11, 26);
        PopularDemo.popular(conexao, data);
        Usuario usuario = usuarios.autenticar(PopularDemo.EMAIL, PopularDemo.SENHA);
        assertNotNull(usuario);
        Sessao sessao = new Sessao(); sessao.entrar(usuario);
        var plano = new PlanejamentoDao(conexao, sessao).buscarPorData(data);
        assertEquals(4, plano.refeicoes().size());
        assertTrue(plano.totais().calorias().signum() > 0);
        assertEquals(15, contar("alimentos", usuario.id()));
        assertEquals(12, contar("refeicoes", usuario.id()));
        assertEquals(43, contar("itens_refeicao", usuario.id()));
        List<Long> antes = checksums();
        PopularDemo.popular(conexao, data);
        assertEquals(antes, checksums());
        try (var c = conexao.abrir()) {
            try (var p = c.prepareStatement("UPDATE perfis SET peso_kg=80 WHERE usuario_id=?")) {
                p.setLong(1, usuario.id()); p.executeUpdate();
            }
            try (var p = c.prepareStatement("UPDATE itens_refeicao SET quantidade_g=45 WHERE usuario_id=?")) {
                p.setLong(1, usuario.id()); p.executeUpdate();
            }
        }
        antes = checksums();
        PopularDemo.popular(conexao, data);
        assertEquals(antes, checksums(), "Preserva edições manuais e todas as outras contas");
        try (var c = conexao.abrir(); var p = c.prepareStatement("UPDATE usuarios SET senha_hash=? WHERE id=?")) {
            p.setString(1, Senhas.criar("OutraSenha-123")); p.setLong(2, usuario.id()); p.executeUpdate();
        }
        antes = checksums();
        assertThrows(IllegalStateException.class, () -> PopularDemo.popular(conexao, data));
        assertEquals(antes, checksums(), "Falha reverte a carga inteira");
    }

    private long contar(String tabela, long dono) throws Exception {
        // tabela vem exclusivamente das três constantes no teste.
        try (var c = conexao.abrir(); var p = c.prepareStatement("SELECT COUNT(*) FROM " + tabela + " WHERE usuario_id=?")) {
            p.setLong(1, dono);
            try (var r = p.executeQuery()) { assertTrue(r.next()); return r.getLong(1); }
        }
    }

    private List<Long> checksums() throws Exception {
        try (var c = conexao.abrir(); var p = c.createStatement();
             var r = p.executeQuery("CHECKSUM TABLE usuarios,perfis,alimentos,refeicoes,itens_refeicao")) {
            List<Long> valores = new ArrayList<>();
            while (r.next()) valores.add(r.getLong(2));
            return valores;
        }
    }
}
