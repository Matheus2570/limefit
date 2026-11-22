# Módulo alimentos — Lucas

## Arquivos a editar

- `TelaAlimentos.java`: construtor pronto, menu e navegação conectados, `JPanel conteudo` herdado e vazio. Monte a interface nesse painel com layouts Swing.
- Dados: Alimento.java e AlimentoDao.java.

## O que implementar

- `AlimentoDao.listar(filtroNome)`: string vazia lista todos os alimentos da conta.
- `buscarPorId(id)`: `Optional.empty()` para ausente ou alimento de outra conta.
- `cadastrar(nome, calorias, proteinas, carboidratos, gorduras)`: criar e retornar chave gerada.
- `atualizar(id, nome, calorias, proteinas, carboidratos, gorduras)`: retornar se afetou uma linha da conta.
- `excluir(id)`: respeitar o uso em refeições; exibir o erro de FK se estiver em uso.

Use as colunas `nome`, `calorias`, `proteinas`, `carboidratos`, `gorduras`, `usuario_id`. Nutrientes por **100 g**, energia em kcal e macros em gramas; `BigDecimal` em todos os valores. Calorias até 1000; cada macro entre 0 e 100. O nome é único dentro da conta. Refeições consumirá `listar("")` e `buscarPorId(id)`. Planejamento usará os mesmos nutrientes para calcular totais; mantenha as unidades.

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

Receba e execute o projeto completo com `.\mvnw.cmd compile exec:java`. Trabalhe principalmente dentro desta pasta e devolva **a pasta `alimentos` inteira**, mantendo os arquivos, os pacotes `br.com.limefit.modulos.alimentos` e as assinaturas públicas. Matheus irá copiar a pasta de volta para `src/main/java/br/com/limefit/modulos/` e compilar.

Teste PT/EN, duas contas e sair/entrar novamente. Se precisar alterar outra pasta, uma classe compartilhada ou o banco, descreva a mudança separadamente para combinarmos a integração. Não inclua `target/`, dependências baixadas ou credenciais.
