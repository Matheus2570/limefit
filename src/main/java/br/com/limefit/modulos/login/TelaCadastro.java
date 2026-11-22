package br.com.limefit.modulos.login;

import br.com.limefit.autenticacao.Usuario;
import br.com.limefit.desktop.ContextoAplicacao;
import br.com.limefit.desktop.Tela;

public final class TelaCadastro extends FormularioConta {
    public TelaCadastro(ContextoAplicacao contexto) { super(contexto, true); }
    @Override protected void concluir(Usuario usuario) {
        limparDados();
        contexto.navegar(Tela.CADASTRO_SUCESSO);
    }
}
