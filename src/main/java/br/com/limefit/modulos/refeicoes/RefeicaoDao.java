package br.com.limefit.modulos.refeicoes;

import br.com.limefit.autenticacao.Sessao;
import br.com.limefit.banco.Conexao;
import br.com.limefit.banco.DaoDaConta;
import java.sql.SQLException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/** Contrato pendente. Implementar SQL com usuarioId() em TODA leitura e gravação. */
public final class RefeicaoDao extends DaoDaConta {
    public RefeicaoDao(Conexao conexao, Sessao sessao) { super(conexao, sessao); }

    /** SELECT por usuario_id e data_plano, ordenado por id. Usado pelo Planejamento. */
    public List<Refeicao> listarPorData(LocalDate data) throws SQLException {
        throw pendente("RefeicaoDao.listarPorData");
    }

    /** SELECT por id e usuario_id. */
    public Optional<Refeicao> buscarPorId(long id) throws SQLException {
        throw pendente("RefeicaoDao.buscarPorId");
    }

    /** INSERT com usuario_id da sessão; validar perfilId na mesma conta. */
    public Refeicao cadastrar(long perfilId, LocalDate data, String nome) throws SQLException {
        throw pendente("RefeicaoDao.cadastrar");
    }

    /** UPDATE de data_plano e nome WHERE id = ? AND usuario_id = ?. */
    public boolean atualizar(long id, LocalDate data, String nome) throws SQLException {
        throw pendente("RefeicaoDao.atualizar");
    }

    /** DELETE por id e usuario_id. FK remove os itens da mesma conta. */
    public boolean excluir(long id) throws SQLException {
        throw pendente("RefeicaoDao.excluir");
    }

    /** SELECT de itens_refeicao por refeicao_id e usuario_id. Ordenar por alimento_id. */
    public List<ItemRefeicao> listarItens(long refeicaoId) throws SQLException {
        throw pendente("RefeicaoDao.listarItens");
    }

    /** INSERT ou UPDATE da quantidade. Verificar refeição e alimento na conta na mesma transação. */
    public void definirPorcao(long refeicaoId, long alimentoId, BigDecimal quantidadeG) throws SQLException {
        throw pendente("RefeicaoDao.definirPorcao");
    }

    /** DELETE por refeicao_id, alimento_id e usuario_id. */
    public boolean removerItem(long refeicaoId, long alimentoId) throws SQLException {
        throw pendente("RefeicaoDao.removerItem");
    }
}
