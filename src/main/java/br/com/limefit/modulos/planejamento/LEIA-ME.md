# Módulo planejamento — Gustavo

## Arquivos a editar

- `TelaPlanejamento.java`: construtor pronto, menu e navegação conectados, `JPanel conteudo` herdado e vazio. Monte a interface nesse painel com layouts Swing.
- Dados: PlanoDiario.java, TotaisNutricionais.java e PlanejamentoDao.java.

## O que implementar

- `PlanejamentoDao.buscarPorData(LocalDate data)`: montar `PlanoDiario` com as refeições e totais da conta.
- `PlanejamentoDao.calcularTotais(LocalDate data)`: somar calorias, proteínas, carboidratos e gorduras.

**Não crie uma tabela planejamento.** A data está em `refeicoes.data_plano`; as porções estão em `itens_refeicao`. Some `alimentos.nutriente * itens_refeicao.quantidade_g / 100` em `BigDecimal`. Use `COALESCE` para zero e lista vazia em dias sem refeições. Arredonde só ao exibir. `PlanoDiario` mantém a lista imutável.

É possível consumir `RefeicaoDao.listarPorData(data)` e `listarItens(refeicaoId)`, e `AlimentoDao.buscarPorId(id)`, após os colegas implementarem. Para obter lista e totais com consistência, o seu DAO pode usar uma única conexão/transação de leitura e JOINs nas tabelas existentes. Sempre inclua `usuario_id` nos JOINs e no WHERE. Caso a tela permita criar/alterar refeições, use os métodos já acordados de `RefeicaoDao`, sem duplicar seu cadastro.

## Recursos já recebidos pelo construtor

```java
long idDaConta = contexto.usuarioId(); // Lança exceção se não houver login.
String nomeDaPessoa = contexto.sessao().usuario().nome();
contexto.navegar(Tela.PERFIL); // Use o enum Tela para navegar.
String texto = idioma.escolher("Português", "English");
```

A conexão é `contexto.conexao()` e a sessão é `contexto.sessao()`. Os módulos com dados já instanciam seu DAO no construtor. No DAO, use `long dono = usuarioId()` no início da operação e mantenha esse valor em todos os comandos. Não guarde o ID no construtor da tela e não coloque `usuario_id` em um campo editável.

Os modelos e as assinaturas estão acordados em [CONTAS-E-DADOS](../../../../../../../../docs/CONTAS-E-DADOS.md). Os métodos pendentes lançam `UnsupportedOperationException` com uma mensagem de implementação necessária. Enquanto não implementar, mostre essa pendência no idioma atual; não simule sucesso nem retorne listas fictícias.

Use os componentes de `br.com.limefit.desktop.Visual`. Em `atualizarTextos()`, chame `super.atualizarTextos()` e traduza os componentes, validações e mensagens. Em `aoExibir()`, chame `super.aoExibir()` antes de carregar dados. Faça JDBC fora da EDT com SwingWorker; confira a revisão da sessão antes de iniciar e antes de aplicar o resultado. Implemente `limparDados()` para esvaziar campos, tabelas e caches e invalidar trabalhos antigos ao sair ou trocar de conta.

## Entrega

Receba e execute o projeto completo com `.\mvnw.cmd compile exec:java`. Trabalhe principalmente dentro desta pasta e devolva **a pasta `planejamento` inteira**, mantendo os arquivos, os pacotes `br.com.limefit.modulos.planejamento` e as assinaturas públicas. Matheus irá copiar a pasta de volta para `src/main/java/br/com/limefit/modulos/` e compilar.

Teste PT/EN, duas contas e sair/entrar novamente. Se precisar alterar outra pasta, uma classe compartilhada ou o banco, descreva a mudança separadamente para combinarmos a integração. Não inclua `target/`, dependências baixadas ou credenciais.
