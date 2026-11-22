# Módulo projeto — Francisco

## Arquivos a editar

- `TelaProjeto.java`: construtor pronto, menu e navegação conectados, `JPanel conteudo` herdado e vazio. Monte a interface nesse painel com layouts Swing.
- Dados: não há modelo ou DAO necessário para este módulo.

## O que implementar

Monte a apresentação do projeto, os integrantes e a revisão do inglês no `JPanel conteudo`. O conteúdo está vazio de propósito. Não há operações de banco pendentes neste módulo.

Use `idioma.escolher("Português", "English")` também nos textos longos e revise os textos dos demais módulos em conjunto com seus responsáveis. Se desejar propor uma correção em Home ou na base, descreva-a separadamente; a devolução desta pasta não altera arquivos compartilhados.

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

Receba e execute o projeto completo com `.\mvnw.cmd compile exec:java`. Trabalhe principalmente dentro desta pasta e devolva **a pasta `projeto` inteira**, mantendo os arquivos, os pacotes `br.com.limefit.modulos.projeto` e as assinaturas públicas. Matheus irá copiar a pasta de volta para `src/main/java/br/com/limefit/modulos/` e compilar.

Teste PT/EN, duas contas e sair/entrar novamente. Se precisar alterar outra pasta, uma classe compartilhada ou o banco, descreva a mudança separadamente para combinarmos a integração. Não inclua `target/`, dependências baixadas ou credenciais.
