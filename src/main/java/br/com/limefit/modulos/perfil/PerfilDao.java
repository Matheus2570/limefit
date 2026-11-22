package br.com.limefit.modulos.perfil;

import br.com.limefit.autenticacao.Sessao;
import br.com.limefit.banco.Conexao;
import br.com.limefit.banco.DaoDaConta;
import java.sql.SQLException;
import java.math.BigDecimal;
import java.util.Optional;

/** Contrato pendente. Implementar SQL com usuarioId() em TODA leitura e gravação. */
public final class PerfilDao extends DaoDaConta {
    public PerfilDao(Conexao conexao, Sessao sessao) { super(conexao, sessao); }

    /** SELECT de perfis WHERE usuario_id = ?. Vazio se ainda não há perfil. */
    public Optional<Perfil> buscarAtual() throws SQLException {
        throw pendente("PerfilDao.buscarAtual");
    }

    /** Um perfil por conta. INSERT ou UPDATE por usuario_id; preservar identificador em UPDATE. */
    public Perfil salvar(BigDecimal pesoKg, BigDecimal alturaCm, Objetivo objetivo) throws SQLException {
        throw pendente("PerfilDao.salvar");
    }

    /** DELETE por usuario_id; cascata remove refeições e itens desta conta. */
    public boolean excluirAtual() throws SQLException {
        throw pendente("PerfilDao.excluirAtual");
    }
}
