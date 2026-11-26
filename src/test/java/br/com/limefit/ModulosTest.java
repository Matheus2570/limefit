package br.com.limefit;

import br.com.limefit.autenticacao.*;
import br.com.limefit.banco.Conexao;
import br.com.limefit.modulos.perfil.*;
import br.com.limefit.modulos.planejamento.*;
import br.com.limefit.modulos.refeicoes.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ModulosTest {
    @Test void trabalhoAgendadoNaoMudaDeConta() throws Exception {
        var ctx=new br.com.limefit.desktop.ContextoAplicacao();
        Usuario ana=new Usuario(11,"Ana","ana@example.test");
        Usuario bruno=new Usuario(22,"Bruno","bruno@example.test");
        ctx.sessao().entrar(ana);
        class Modulo extends br.com.limefit.desktop.PainelModulo {
            Modulo() { super(ctx,br.com.limefit.desktop.Tela.PERFIL); }
            Sessao copiar(long revisao) { return sessaoDoTrabalho(revisao); }
        }
        Modulo modulo=new Modulo();
        long revisao=ctx.sessao().revisao();
        Sessao copia=modulo.copiar(revisao);
        ctx.sessao().entrar(bruno);
        assertEquals(11,copia.exigirUsuarioId());
        assertThrows(java.util.concurrent.CancellationException.class,() -> modulo.copiar(revisao));
    }

    @Test void respostaEmAndamentoNaoVoltaDepoisDaTrocaDeConta() throws Exception {
        var ctx=new br.com.limefit.desktop.ContextoAplicacao();
        ctx.sessao().entrar(new Usuario(11,"Ana","ana@example.test"));
        var iniciou=new java.util.concurrent.CountDownLatch(1);
        var liberar=new java.util.concurrent.CountDownLatch(1);
        var terminou=new java.util.concurrent.CountDownLatch(1);
        var aplicado=new java.util.concurrent.atomic.AtomicBoolean();
        class Modulo extends br.com.limefit.desktop.PainelModulo {
            Modulo() { super(ctx,br.com.limefit.desktop.Tela.PERFIL); }
            javax.swing.SwingWorker<Long,Void> agendar() {
                var worker=executarNaConta(s -> {
                    iniciou.countDown();
                    if(!liberar.await(5,java.util.concurrent.TimeUnit.SECONDS)) throw new IllegalStateException("Timeout");
                    return s.exigirUsuarioId();
                }, id -> aplicado.set(true), erro -> aplicado.set(true));
                worker.addPropertyChangeListener(e -> {
                    if(e.getNewValue()==javax.swing.SwingWorker.StateValue.DONE) terminou.countDown();
                });
                return worker;
            }
        }
        var tarefa=new java.util.concurrent.FutureTask<javax.swing.SwingWorker<Long,Void>>(() -> new Modulo().agendar());
        javax.swing.SwingUtilities.invokeAndWait(tarefa);
        var worker=tarefa.get();
        assertTrue(iniciou.await(5,java.util.concurrent.TimeUnit.SECONDS));
        ctx.sessao().entrar(new Usuario(22,"Bruno","bruno@example.test"));
        liberar.countDown();
        assertEquals(11L,worker.get(5,java.util.concurrent.TimeUnit.SECONDS));
        assertTrue(terminou.await(5,java.util.concurrent.TimeUnit.SECONDS));
        assertFalse(aplicado.get());
    }

    @Test void imcConverteCentimetrosEArredonda() {
        Perfil perfil=new Perfil(1,1,"",new BigDecimal("70.50"),new BigDecimal("175"),Objetivo.MANTER);
        assertEquals(new BigDecimal("23.02"),perfil.calcularImc());
        assertThrows(IllegalStateException.class,() -> new Perfil(1,1,"",BigDecimal.TEN,BigDecimal.ZERO,Objetivo.MANTER).calcularImc());
    }
    @Test void planoPreservaListaImutavel() {
        var refeicoes=new ArrayList<Refeicao>();
        var plano=new PlanoDiario(LocalDate.now(),refeicoes,TotaisNutricionais.ZERO);
        refeicoes.add(new Refeicao(1,1,1,LocalDate.now(),"Almoço"));
        assertTrue(plano.refeicoes().isEmpty());
        assertThrows(UnsupportedOperationException.class,() -> plano.refeicoes().add(refeicoes.get(0)));
    }
    @Test void todosOsDaosExigemLoginAntesDeAcessarBanco() {
        Sessao s=new Sessao(); Conexao c=new Conexao();
        assertThrows(IllegalStateException.class,() -> new PerfilDao(c,s).buscarAtual());
        assertThrows(IllegalStateException.class,() -> new PerfilDao(c,s).excluirAtual());
        assertThrows(IllegalStateException.class,() -> new RefeicaoDao(c,s).listarPorData(LocalDate.now()));
        assertThrows(IllegalStateException.class,() -> new PlanejamentoDao(c,s).buscarPorData(LocalDate.now()));
    }
    @Test void medidasEPorcoesNaoSaoArredondadasSilenciosamente() {
        Sessao s=new Sessao(); s.entrar(new Usuario(1,"Teste","teste@example.test"));
        PerfilDao perfis=new PerfilDao(new Conexao(),s);
        assertThrows(IllegalArgumentException.class,() -> perfis.salvar(new BigDecimal("70.555"),new BigDecimal("175"),Objetivo.MANTER));
        assertThrows(IllegalArgumentException.class,() -> perfis.salvar(new BigDecimal("70"),new BigDecimal("99"),Objetivo.MANTER));
        RefeicaoDao refeicoes=new RefeicaoDao(new Conexao(),s);
        assertThrows(IllegalArgumentException.class,() -> refeicoes.definirPorcao(1,1,new BigDecimal("0.001")));
        assertThrows(IllegalArgumentException.class,() -> refeicoes.definirPorcao(1,1,new BigDecimal("10000.01")));
    }
}
