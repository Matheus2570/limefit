# Sessão, esquema e contratos dos módulos

## Identidade desktop

Existe um objeto `Sessao` por `ContextoAplicacao`. Ele contém apenas `Usuario(id,nome,email)` e uma revisão que muda ao entrar ou sair. Nenhuma senha/hash fica na sessão. O login carrega a identidade do MariaDB; campos da interface não definem o ID.

A janela exige autenticação em Perfil, Alimentos, Refeições, Planejamento e Projeto. Ao tentar navegar sem conta, abre Login; após autenticar, retorna ao módulo solicitado. No login normal, retorna à Home. Ela mostra o nome da pessoa e o botão de perfil; o cabeçalho oferece Sair. Ao sair ou fechar a janela, a identidade é removida e todas as telas recebem `limparDados()`.

Use `contexto.usuarioId()` ou `contexto.sessao().exigirUsuarioId()` na interface. Os DAOs herdam `usuarioId()` de `DaoDaConta`; obtenha o ID uma vez no início de cada operação e use esse valor em todos os comandos da operação. O DAO não mantém uma identidade capturada no construtor. Nunca aceite o dono de um campo editável ou use `id=1`.

Para trabalhos em segundo plano, registre a revisão da sessão ao agendar. Só inicie uma operação se ainda for a mesma sessão; use o ID obtido no início em toda a transação. Em `done()`, confira a revisão antes de aplicar dados à interface. A autenticação implementada já invalida respostas antigas ao navegar, sair ou trocar de conta.

## Banco conferido

MariaDB local 10.11.16, tabelas e constraints conferidas antes de alterar a aplicação. O banco atual já contém a migração `001-contas-e-propriedade.sql`. **Swing não exige alteração SQL.** Há uma conta no banco pessoal; os testes usam bancos temporários.

Para instalar do zero, `sql/limefit.sql` agora contém tudo: banco, cinco tabelas, índices, chaves e oito gatilhos. O incremental 001 permanece disponível apenas para instalações anteriores à propriedade por conta. `sql/conferir-banco.sql` faz somente leitura de metadados. Não execute os scripts de `sql/legado/` no banco de trabalho: são fixtures de teste do esquema original.

| Tabela | Colunas e contrato |
|---|---|
| `usuarios` | `id BIGINT UNSIGNED`, `nome VARCHAR(120)`, `email VARCHAR(190)` único, `senha_hash VARCHAR(255)`, `criado_em TIMESTAMP` |
| `perfis` | `id INT UNSIGNED`, `identificador CHAR(64)` ASCII único, `peso_kg DECIMAL(5,2)` entre 25 e 300, `altura_cm DECIMAL(5,2)` entre 100 e 230, `objetivo ENUM('mass','loss','balance','maintain')`, `usuario_id BIGINT UNSIGNED` único |
| `alimentos` | `id INT UNSIGNED`, `nome VARCHAR(100)`, `calorias DECIMAL(6,2) UNSIGNED` até 1000, `proteinas`, `carboidratos`, `gorduras DECIMAL(5,2) UNSIGNED` até 100, `usuario_id BIGINT UNSIGNED`; nome único por conta |
| `refeicoes` | `id INT UNSIGNED`, `perfil_id INT UNSIGNED`, `data_plano DATE`, `nome VARCHAR(100)`, `usuario_id BIGINT UNSIGNED` |
| `itens_refeicao` | PK composta `(refeicao_id,alimento_id)` ambos INT UNSIGNED, `quantidade_g DECIMAL(7,2)` maior que 0 e até 10000, `usuario_id BIGINT UNSIGNED` |

Java usa `long` para IDs, `BigDecimal` para medidas/nutrientes e `LocalDate` para datas. Nenhuma tabela nova é necessária. Planejamento é uma visão das refeições por `data_plano`.

O campo `perfis.identificador` continua para preservar o esquema e os registros; ele não é a identidade de login. Ao criar um perfil, gere um token técnico único de 64 caracteres (por exemplo SHA-256 de um UUID); preserve-o nas atualizações. `Objetivo.codigo()` fornece exatamente os valores SQL do ENUM. Altura está em centímetros e deve ser dividida por 100 no cálculo do IMC.

Todos os nutrientes dos alimentos são **por 100 g**. Para uma porção: `nutriente * quantidade_g / 100`. Use `BigDecimal`, some sem arredondar intermediários e arredonde só para exibir. Um plano sem refeições retorna lista vazia e totais zero quando o colega implementar, nunca null.

## Assinaturas acordadas

Todos os DAOs recebem `(Conexao conexao, Sessao sessao)` e todos os métodos declarados lançam `SQLException`. Eles ainda não executam SQL: lançam uma exceção que identifica a implementação pendente. Não engula essa exceção nem apresente sucesso antes de implementar.

- `PerfilDao.buscarAtual(): Optional<Perfil>`
- `PerfilDao.salvar(BigDecimal pesoKg, BigDecimal alturaCm, Objetivo objetivo): Perfil`
- `PerfilDao.excluirAtual(): boolean`
- `AlimentoDao.listar(String filtroNome): List<Alimento>`; string vazia significa todos da conta.
- `AlimentoDao.buscarPorId(long id): Optional<Alimento>`
- `AlimentoDao.cadastrar(String nome, BigDecimal calorias, BigDecimal proteinas, BigDecimal carboidratos, BigDecimal gorduras): Alimento`
- `AlimentoDao.atualizar(long id, String nome, BigDecimal calorias, BigDecimal proteinas, BigDecimal carboidratos, BigDecimal gorduras): boolean`
- `AlimentoDao.excluir(long id): boolean`
- `RefeicaoDao.listarPorData(LocalDate data): List<Refeicao>`
- `RefeicaoDao.buscarPorId(long id): Optional<Refeicao>`
- `RefeicaoDao.cadastrar(long perfilId, LocalDate data, String nome): Refeicao`
- `RefeicaoDao.atualizar(long id, LocalDate data, String nome): boolean`
- `RefeicaoDao.excluir(long id): boolean`
- `RefeicaoDao.listarItens(long refeicaoId): List<ItemRefeicao>`
- `RefeicaoDao.definirPorcao(long refeicaoId, long alimentoId, BigDecimal quantidadeG): void`; insere ou atualiza a quantidade.
- `RefeicaoDao.removerItem(long refeicaoId, long alimentoId): boolean`
- `PlanejamentoDao.buscarPorData(LocalDate data): PlanoDiario`
- `PlanejamentoDao.calcularTotais(LocalDate data): TotaisNutricionais`

`Optional.empty()` representa registro ausente ou pertencente a outra conta. Atualizações/exclusões retornam `false` se nenhuma linha da conta foi afetada. SQL ou valores inválidos produzem erros que a tela deve apresentar no idioma atual. Falta de implementação nunca é resultado vazio.

## Regras obrigatórias da implementação futura

Cada SELECT, UPDATE e DELETE filtra o proprietário. Cada INSERT preenche `usuario_id` com o ID autenticado. Use PreparedStatement e try-with-resources. Exemplos:

```sql
SELECT id, nome, calorias, proteinas, carboidratos, gorduras, usuario_id
FROM alimentos WHERE id = ? AND usuario_id = ?;
UPDATE alimentos SET nome = ? WHERE id = ? AND usuario_id = ?;
DELETE FROM itens_refeicao
WHERE refeicao_id = ? AND alimento_id = ? AND usuario_id = ?;
```

As consultas por data filtram `data_plano` e `usuario_id`. Nos JOINs, relacione também o dono:

```sql
SELECT COALESCE(SUM(a.calorias * i.quantidade_g / 100), 0) AS calorias
FROM refeicoes r
JOIN itens_refeicao i ON i.refeicao_id = r.id AND i.usuario_id = r.usuario_id
JOIN alimentos a ON a.id = i.alimento_id AND a.usuario_id = i.usuario_id
WHERE r.data_plano = ? AND r.usuario_id = ?;
```

Aplique o mesmo cálculo a proteínas, carboidratos e gorduras. Obtenha lista e totais em uma leitura consistente para `PlanoDiario`.

Antes de gravar referências (`perfil_id`, `refeicao_id`, `alimento_id`), confira se todas pertencem à conta e use a mesma transação para a operação inteira, com rollback em erro. As FKs compostas reforçam isso no banco. Um alimento em uso não pode ser apagado; apresente a restrição em vez de remover dados de outras entidades silenciosamente. Excluir perfil ou refeição remove seus dependentes por cascata; comunique esse efeito na futura tela de exclusão.

`usuario_id NULL` é exclusivo de linhas legadas sem dono conhecido. Elas são preservadas e ficam fora das consultas por conta; não as associe ao usuário que entrar primeiro. Os gatilhos exigem dono em novos registros e impedem trocar um dono já atribuído. Qualquer associação administrativa de legados deve ser combinada e documentada separadamente.
