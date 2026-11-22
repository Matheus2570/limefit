package br.com.limefit.banco;

import br.com.limefit.autenticacao.Sessao;

/** Base dos contratos futuros. Não captura a identidade no construtor. */
public abstract class DaoDaConta {
    protected final Conexao conexao;
    private final Sessao sessao;
    protected DaoDaConta(Conexao conexao, Sessao sessao) {
        this.conexao = conexao;
        this.sessao = sessao;
    }
    protected final long usuarioId() { return sessao.exigirUsuarioId(); }
    protected final UnsupportedOperationException pendente(String metodo) {
        usuarioId();
        return new UnsupportedOperationException(metodo + ": ainda precisa ser implementado / must still be implemented.");
    }
}
