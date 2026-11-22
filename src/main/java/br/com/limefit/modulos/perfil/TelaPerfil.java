package br.com.limefit.modulos.perfil;

import br.com.limefit.desktop.ContextoAplicacao;
import br.com.limefit.desktop.PainelModulo;
import br.com.limefit.desktop.Tela;

/** Responsável: Matheus Maiolo. Menu, sessão e navegação já vêm da base compartilhada. */
public final class TelaPerfil extends PainelModulo {
    private final PerfilDao dao;

    public TelaPerfil(ContextoAplicacao contexto) {
        super(contexto, Tela.PERFIL);
        dao = new PerfilDao(contexto.conexao(), contexto.sessao());
        // Monte a interface do seu módulo no JPanel conteudo, usando layouts Swing.
    }

    @Override public void atualizarTextos() {
        super.atualizarTextos();
        // Traduza os componentes com idioma.escolher("Português", "English").
    }

    @Override public void limparDados() {
        // Limpe campos, tabelas, seleções e caches da conta anterior aqui.
    }
}
