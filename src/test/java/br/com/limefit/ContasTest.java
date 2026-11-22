package br.com.limefit;

import br.com.limefit.autenticacao.*;
import br.com.limefit.banco.*;
import br.com.limefit.desktop.Idioma;
import br.com.limefit.modulos.alimentos.AlimentoDao;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ContasTest {
    @Test void aceitaHashHistoricoSemRecalcularFormato() {
        // Fixture calculada independentemente com hashlib/PBKDF2, anterior à interface Swing.
        String hash = "pbkdf2-sha256$100000$AAECAwQFBgcICQoLDA0ODw==$hefbQGknRGCYpczS/rUDtMdfHIDRRPxeKpPZQbBEzhs=";
        assertTrue(Senhas.verificar("Legacy-123", hash));
        assertFalse(Senhas.verificar("incorreta", hash));
        assertFalse(Senhas.verificar("Legacy-123", "hash inválido"));
    }
    @Test void novasSenhasUsamSaltIndividual() {
        String a = Senhas.criar("SenhaTeste-123"), b = Senhas.criar("SenhaTeste-123");
        assertNotEquals(a, b);
        assertTrue(a.startsWith("pbkdf2-sha256$600000$"));
        assertTrue(Senhas.verificar("SenhaTeste-123", a));
    }
    @Test void sessaoEDaoNaoRetemIdentidadeAnterior() {
        Sessao sessao = new Sessao();
        class ConsultaIdentidade extends DaoDaConta {
            ConsultaIdentidade() { super(new Conexao(), sessao); }
            long id() { return usuarioId(); }
        }
        var consulta = new ConsultaIdentidade();
        assertThrows(IllegalStateException.class, consulta::id);
        sessao.entrar(new Usuario(11, "Ana", "ana@example.test"));
        assertEquals(11, consulta.id());
        long versao = sessao.revisao();
        sessao.sair();
        assertTrue(sessao.revisao() > versao);
        assertNull(sessao.usuario());
        assertThrows(IllegalStateException.class, consulta::id);
        sessao.entrar(new Usuario(22, "Bruno", "bruno@example.test"));
        assertEquals(22, consulta.id());
    }
    @Test void contratoPendenteExigeContaENaoFingeSucesso() {
        Sessao sessao = new Sessao();
        AlimentoDao dao = new AlimentoDao(new Conexao(), sessao);
        assertThrows(IllegalStateException.class, () -> dao.listar(""));
        sessao.entrar(new Usuario(1, "Ana", "ana@example.test"));
        assertTrue(assertThrows(UnsupportedOperationException.class, () -> dao.listar("")).getMessage().contains("implementado"));
    }
    @Test void regrasDeCadastroSaoPreservadas() {
        ServicoContas contas = new ServicoContas(new UsuarioDao(new Conexao()));
        assertEquals(MensagemConta.DADOS, assertThrows(ValidacaoConta.class,
                () -> contas.cadastrar("", "invalido", "12345678", "12345678")).mensagem());
        assertEquals(MensagemConta.SENHA, assertThrows(ValidacaoConta.class,
                () -> contas.cadastrar("Ana", "ana@example.test", "curta", "curta")).mensagem());
        assertEquals(MensagemConta.CONFIRMACAO, assertThrows(ValidacaoConta.class,
                () -> contas.cadastrar("Ana", "ana@example.test", "12345678", "outra")).mensagem());
    }
    @Test void idiomaInicialEInglesEMensagensMudam() {
        Idioma idioma = new Idioma();
        assertTrue(idioma.ingles());
        assertEquals("Incorrect email or password.", MensagemConta.CREDENCIAIS.texto(idioma));
        idioma.usarIngles(false);
        assertEquals("E-mail ou senha incorretos.", MensagemConta.CREDENCIAIS.texto(idioma));
    }
}
