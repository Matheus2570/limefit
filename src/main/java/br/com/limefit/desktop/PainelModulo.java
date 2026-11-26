package br.com.limefit.desktop;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import br.com.limefit.autenticacao.Sessao;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

/** Menu e navegação ficam na janela; cada colega preenche apenas o conteúdo. */
public abstract class PainelModulo extends PainelTela {
    protected final JPanel conteudo = Visual.painelRolavel(new BorderLayout());
    private final JLabel titulo = Visual.titulo(36);
    private final Tela tela;

    protected PainelModulo(ContextoAplicacao contexto, Tela tela) {
        super(contexto);
        this.tela = tela;
        setBorder(new EmptyBorder(32, 40, 32, 40));
        titulo.setBorder(new EmptyBorder(0, 0, 26, 0));
        add(titulo, BorderLayout.NORTH);
        add(Visual.rolagem(conteudo), BorderLayout.CENTER);
        titulo.setText(tela.titulo(idioma));
    }
    @Override public void atualizarTextos() { titulo.setText(tela.titulo(idioma)); }
    @Override public void aoExibir() { contexto.usuarioId(); }

    /** Fixa a conta antes do JDBC, sem bloquear a interface durante a operação. */
    protected final Sessao sessaoDoTrabalho(long revisao) {
        Sessao origem = contexto.sessao();
        synchronized (origem) {
            if (revisao != origem.revisao() || !origem.autenticada()) throw new CancellationException();
            Sessao copia = new Sessao();
            copia.entrar(origem.usuario());
            return copia;
        }
    }

    @FunctionalInterface
    protected interface OperacaoDaConta<T> { T executar(Sessao sessao) throws Exception; }

    /** Os callbacks rodam na EDT e só recebem resultados da sessão que agendou o trabalho. */
    protected final <T> SwingWorker<T, Void> executarNaConta(OperacaoDaConta<T> operacao,
            Consumer<T> sucesso, Consumer<Throwable> falha) {
        long revisao = contexto.sessao().revisao();
        SwingWorker<T, Void> worker = new SwingWorker<>() {
            @Override protected T doInBackground() throws Exception {
                return operacao.executar(sessaoDoTrabalho(revisao));
            }
            @Override protected void done() {
                if (isCancelled() || revisao != contexto.sessao().revisao()) return;
                try { sucesso.accept(get()); }
                catch (CancellationException ignored) { }
                catch (InterruptedException e) { Thread.currentThread().interrupt(); falha.accept(e); }
                catch (ExecutionException e) { falha.accept(e.getCause()); }
            }
        };
        worker.execute();
        return worker;
    }
}
