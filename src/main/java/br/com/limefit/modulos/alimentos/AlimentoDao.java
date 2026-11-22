package br.com.limefit.modulos.alimentos;

import br.com.limefit.autenticacao.Sessao;
import br.com.limefit.banco.Conexao;
import br.com.limefit.banco.DaoDaConta;
import java.sql.SQLException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/** Contrato pendente. Implementar SQL com usuarioId() em TODA leitura e gravação. */
public final class AlimentoDao extends DaoDaConta {
    public AlimentoDao(Conexao conexao, Sessao sessao) { super(conexao, sessao); }

    /** Filtro vazio lista todos da conta. SELECT por usuario_id e nome, ordenado por nome. */
    public List<Alimento> listar(String filtroNome) throws SQLException {
        throw pendente("AlimentoDao.listar");
    }

    /** SELECT WHERE id = ? AND usuario_id = ?. Outra conta equivale a ausente. */
    public Optional<Alimento> buscarPorId(long id) throws SQLException {
        throw pendente("AlimentoDao.buscarPorId");
    }

    /** INSERT com usuario_id da sessão. Nutrientes por 100 g; retornar chave gerada. */
    public Alimento cadastrar(String nome, BigDecimal calorias, BigDecimal proteinas, BigDecimal carboidratos, BigDecimal gorduras) throws SQLException {
        throw pendente("AlimentoDao.cadastrar");
    }

    /** UPDATE WHERE id = ? AND usuario_id = ?. Retornar false se ausente. */
    public boolean atualizar(long id, String nome, BigDecimal calorias, BigDecimal proteinas, BigDecimal carboidratos, BigDecimal gorduras) throws SQLException {
        throw pendente("AlimentoDao.atualizar");
    }

    /** DELETE WHERE id = ? AND usuario_id = ?. Se alimento está em uso, informar a restrição FK. */
    public boolean excluir(long id) throws SQLException {
        throw pendente("AlimentoDao.excluir");
    }
}
