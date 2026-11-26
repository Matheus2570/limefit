package br.com.limefit.banco;

import br.com.limefit.autenticacao.Sessao;

/** Base dos DAOs privados. Consulta a identidade no início de cada operação. */
public abstract class DaoDaConta {
    protected final Conexao conexao;
    private final Sessao sessao;
    protected DaoDaConta(Conexao conexao, Sessao sessao) {
        this.conexao = conexao;
        this.sessao = sessao;
    }
    protected final long usuarioId() { return sessao.exigirUsuarioId(); }
}
