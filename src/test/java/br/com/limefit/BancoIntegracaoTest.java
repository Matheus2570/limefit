package br.com.limefit;

import br.com.limefit.autenticacao.*;
import br.com.limefit.banco.Conexao;
import br.com.limefit.modulos.alimentos.AlimentoDao;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import java.sql.*;
import static org.junit.jupiter.api.Assertions.*;

@EnabledIfEnvironmentVariable(named = "LIMEFIT_TEST_DB", matches = "limefit_validacao_[0-9]+")
class BancoIntegracaoTest {
    private final Conexao conexao = new Conexao();

    @Test void hashAnteriorPermaneceIgualDepoisDeEntrar() throws Exception {
        assertEquals(System.getenv("LIMEFIT_TEST_DB"), System.getenv("LIMEFIT_DB_NAME"));
        String hash = "pbkdf2-sha256$100000$AAECAwQFBgcICQoLDA0ODw==$hefbQGknRGCYpczS/rUDtMdfHIDRRPxeKpPZQbBEzhs=";
        executar("INSERT INTO usuarios(nome,email,senha_hash) VALUES(?,?,?)", "Conta Anterior", "anterior@example.test", hash);
        UsuarioDao dao = new UsuarioDao(conexao);
        assertEquals("Conta Anterior", dao.autenticar("anterior@example.test", "Legacy-123").nome());
        assertNull(dao.autenticar("anterior@example.test", "SenhaErrada"));
        try (var c = conexao.abrir(); var q = c.prepareStatement("SELECT senha_hash FROM usuarios WHERE email=?")) {
            q.setString(1, "anterior@example.test");
            try (var r = q.executeQuery()) { assertTrue(r.next()); assertEquals(hash, r.getString(1)); }
        }
    }
    @Test void relacoesNoBancoImpedemMisturarContas() throws Exception {
        UsuarioDao usuarios = new UsuarioDao(conexao);
        usuarios.cadastrar("Conta A", "dados-a@example.test", "SenhaTeste-123");
        usuarios.cadastrar("Conta B", "dados-b@example.test", "SenhaTeste-123");
        long a = usuarios.autenticar("dados-a@example.test", "SenhaTeste-123").id();
        long b = usuarios.autenticar("dados-b@example.test", "SenhaTeste-123").id();
        long perfilA = inserir("INSERT INTO perfis(identificador,peso_kg,altura_cm,objetivo,usuario_id) VALUES(?,80,180,'maintain',?)", String.format("%064x", a), a);
        long perfilB = inserir("INSERT INTO perfis(identificador,peso_kg,altura_cm,objetivo,usuario_id) VALUES(?,70,170,'balance',?)", String.format("%064x", b), b);
        long alimentoA = inserir("INSERT INTO alimentos(nome,calorias,proteinas,carboidratos,gorduras,usuario_id) VALUES('Arroz',100,2,20,1,?)", a);
        long alimentoB = inserir("INSERT INTO alimentos(nome,calorias,proteinas,carboidratos,gorduras,usuario_id) VALUES('Arroz',100,2,20,1,?)", b);
        long refeicaoA = inserir("INSERT INTO refeicoes(perfil_id,data_plano,nome,usuario_id) VALUES(?,'2026-11-22','Lunch',?)", perfilA, a);
        assertThrows(SQLException.class, () -> executar("INSERT INTO refeicoes(perfil_id,data_plano,nome,usuario_id) VALUES(?,'2026-11-22','Outra',?)", perfilB, a));
        assertThrows(SQLException.class, () -> executar("INSERT INTO itens_refeicao(refeicao_id,alimento_id,quantidade_g,usuario_id) VALUES(?,?,100,?)", refeicaoA, alimentoB, a));
        executar("INSERT INTO itens_refeicao(refeicao_id,alimento_id,quantidade_g,usuario_id) VALUES(?,?,150,?)", refeicaoA, alimentoA, a);
        assertThrows(SQLException.class, () -> executar("UPDATE alimentos SET usuario_id=? WHERE id=?", b, alimentoA));
        assertThrows(SQLException.class, () -> executar("INSERT INTO alimentos(nome,calorias,proteinas,carboidratos,gorduras) VALUES('Sem dono',100,2,20,1)"));
        // Exemplo dos WHERE contratados: não retorna nem altera o alimento da outra conta.
        try (var c = conexao.abrir(); var q = c.prepareStatement("SELECT id FROM alimentos WHERE id=? AND usuario_id=?")) {
            q.setLong(1, alimentoA); q.setLong(2, b);
            try (var r = q.executeQuery()) { assertFalse(r.next()); }
        }
        assertEquals(0, executar("UPDATE alimentos SET nome='Inválido' WHERE id=? AND usuario_id=?", alimentoA, b));
        assertEquals(0, executar("DELETE FROM alimentos WHERE id=? AND usuario_id=?", alimentoA, b));
        assertEquals(0, executar("DELETE FROM itens_refeicao WHERE refeicao_id=? AND usuario_id=?", refeicaoA, b));

        Sessao sessao = new Sessao();
        AlimentoDao alimentos = new AlimentoDao(conexao, sessao);
        sessao.entrar(new Usuario(a, "Conta A", "dados-a@example.test"));
        BigDecimal energia = new BigDecimal("123.45");
        var cadastro = alimentos.cadastrar("Pão 100%", energia, BigDecimal.ZERO, BigDecimal.TEN, BigDecimal.ONE);
        assertEquals(cadastro, alimentos.buscarPorId(cadastro.id()).orElseThrow());
        assertEquals(java.util.List.of(cadastro), alimentos.listar("%"));
        assertEquals(1062, assertThrows(SQLException.class, () -> alimentos.cadastrar(
                "Arroz", energia, BigDecimal.ZERO, BigDecimal.TEN, BigDecimal.ONE)).getErrorCode());
        assertEquals(1451, assertThrows(SQLException.class, () -> alimentos.excluir(alimentoA)).getErrorCode());

        sessao.sair();
        sessao.entrar(new Usuario(b, "Conta B", "dados-b@example.test"));
        assertEquals(1, alimentos.listar("").size());
        assertEquals(alimentoB, alimentos.listar("").get(0).id());
        assertTrue(alimentos.buscarPorId(cadastro.id()).isEmpty());
        assertFalse(alimentos.atualizar(cadastro.id(), "Outro", energia, BigDecimal.ZERO, BigDecimal.TEN, BigDecimal.ONE));
        assertFalse(alimentos.excluir(cadastro.id()));

        sessao.sair();
        sessao.entrar(new Usuario(a, "Conta A", "dados-a@example.test"));
        assertTrue(alimentos.atualizar(cadastro.id(), "Pão integral", energia, BigDecimal.ZERO, BigDecimal.TEN, BigDecimal.ONE));
        assertEquals("Pão integral", alimentos.buscarPorId(cadastro.id()).orElseThrow().nome());
        assertTrue(alimentos.excluir(cadastro.id()));
        assertTrue(alimentos.buscarPorId(cadastro.id()).isEmpty());
    }
    @Test void perfilRefeicoesETotaisMantemContaEPrecisao() throws Exception {
        var usuarios=new UsuarioDao(conexao);
        usuarios.cadastrar("Plano A","plano-a@example.test","SenhaTeste-123");
        usuarios.cadastrar("Plano B","plano-b@example.test","SenhaTeste-123");
        Usuario a=usuarios.autenticar("plano-a@example.test","SenhaTeste-123");
        Usuario b=usuarios.autenticar("plano-b@example.test","SenhaTeste-123");
        Sessao sessao=new Sessao(); sessao.entrar(a);
        var perfis=new br.com.limefit.modulos.perfil.PerfilDao(conexao,sessao);
        var alimentos=new AlimentoDao(conexao,sessao);
        var refeicoes=new br.com.limefit.modulos.refeicoes.RefeicaoDao(conexao,sessao);
        var planos=new br.com.limefit.modulos.planejamento.PlanejamentoDao(conexao,sessao);
        var data=java.time.LocalDate.of(2026,11,26);
        assertTrue(perfis.buscarAtual().isEmpty());
        var perfil=perfis.salvar(new BigDecimal("70.50"),new BigDecimal("175"),br.com.limefit.modulos.perfil.Objetivo.MANTER);
        assertEquals(64,perfil.identificador().length());
        var atualizado=perfis.salvar(new BigDecimal("71"),new BigDecimal("175"),br.com.limefit.modulos.perfil.Objetivo.GANHAR_MASSA);
        assertEquals(perfil.id(),atualizado.id()); assertEquals(perfil.identificador(),atualizado.identificador());
        var alimento=alimentos.cadastrar("Aveia teste",new BigDecimal("389.25"),new BigDecimal("13.50"),new BigDecimal("66"),new BigDecimal("7"));
        var refeicao=refeicoes.cadastrar(perfil.id(),data,"Breakfast");
        assertEquals(a.id(),refeicao.usuarioId());
        refeicoes.definirPorcao(refeicao.id(),alimento.id(),new BigDecimal("150.50"));
        var plano=planos.buscarPorData(data);
        assertEquals(java.util.List.of(refeicao),plano.refeicoes());
        assertEquals(0,new BigDecimal("585.82125").compareTo(plano.totais().calorias()));
        assertEquals(0,new BigDecimal("20.3175").compareTo(plano.totais().proteinas()));
        assertEquals(0,new BigDecimal("99.33").compareTo(plano.totais().carboidratos()));
        assertEquals(0,new BigDecimal("10.535").compareTo(plano.totais().gorduras()));
        refeicoes.definirPorcao(refeicao.id(),alimento.id(),new BigDecimal("100"));
        assertEquals(1,refeicoes.listarItens(refeicao.id()).size());
        assertEquals(0,alimento.calorias().compareTo(planos.calcularTotais(data).calorias()));
        sessao.entrar(b);
        assertTrue(perfis.buscarAtual().isEmpty()); assertTrue(planos.buscarPorData(data).refeicoes().isEmpty());
        assertEquals(0,planos.calcularTotais(data).calorias().signum());
        assertTrue(refeicoes.buscarPorId(refeicao.id()).isEmpty()); assertTrue(refeicoes.listarItens(refeicao.id()).isEmpty());
        assertFalse(refeicoes.excluir(refeicao.id())); assertFalse(refeicoes.removerItem(refeicao.id(),alimento.id()));
        assertThrows(SQLException.class,() -> refeicoes.cadastrar(perfil.id(),data,"Forbidden"));
        var perfilB=perfis.salvar(new BigDecimal("80"),new BigDecimal("180"),br.com.limefit.modulos.perfil.Objetivo.MANTER);
        var refeicaoB=refeicoes.cadastrar(perfilB.id(),data,"Lunch B");
        assertThrows(SQLException.class,() -> refeicoes.definirPorcao(refeicaoB.id(),alimento.id(),BigDecimal.TEN));
        sessao.entrar(a);
        assertTrue(refeicoes.atualizar(refeicao.id(),data.plusDays(1),"Moved"));
        assertEquals(0,planos.calcularTotais(data).calorias().signum());
        assertEquals(1,planos.buscarPorData(data.plusDays(1)).refeicoes().size());
        assertTrue(perfis.excluirAtual()); assertTrue(refeicoes.listarItens(refeicao.id()).isEmpty());
        assertTrue(planos.buscarPorData(data.plusDays(1)).refeicoes().isEmpty());
        assertTrue(alimentos.buscarPorId(alimento.id()).isPresent());
        sessao.entrar(b); assertEquals(1,refeicoes.listarPorData(data).size());
    }
    private int executar(String sql, Object... args) throws SQLException {
        try (var c = conexao.abrir(); var q = c.prepareStatement(sql)) {
            for (int i = 0; i < args.length; i++) q.setObject(i + 1, args[i]);
            return q.executeUpdate();
        }
    }
    private long inserir(String sql, Object... args) throws SQLException {
        try (var c = conexao.abrir(); var q = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            for (int i = 0; i < args.length; i++) q.setObject(i + 1, args[i]);
            q.executeUpdate();
            try (var r = q.getGeneratedKeys()) { assertTrue(r.next()); return r.getLong(1); }
        }
    }
}
