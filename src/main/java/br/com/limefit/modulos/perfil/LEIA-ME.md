# Módulo perfil — Matheus Maiolo

## Arquivos a editar

- `TelaPerfil.java`: construtor pronto, menu e navegação conectados, `JPanel conteudo` herdado e vazio. Monte a interface nesse painel com layouts Swing.
- Dados: Perfil.java, Objetivo.java e PerfilDao.java.

## O que implementar

- `PerfilDao.buscarAtual()`: buscar o perfil da conta ou `Optional.empty()`.
- `PerfilDao.salvar(pesoKg, alturaCm, objetivo)`: criar ou atualizar o único perfil da conta.
- `PerfilDao.excluirAtual()`: remover por `usuario_id`; refeições e itens dependentes são removidos por cascata.
- `Perfil.calcularImc()`: calcular peso / altura em metros ao quadrado; está explicitamente pendente.

Use `perfis.peso_kg`, `altura_cm`, `objetivo`, `identificador` e `usuario_id`. Gere um token técnico único de 64 caracteres ao inserir `identificador` e preserve nas atualizações. `Objetivo.codigo()` já traduz para `mass`, `loss`, `balance` ou `maintain`. Peso entre 25 e 300 kg; altura entre 100 e 230 cm. Refeições consumirá `buscarAtual()` e `Perfil.id()`; mantenha esse contrato.

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

Receba e execute o projeto completo com `.\mvnw.cmd compile exec:java`. Trabalhe principalmente dentro desta pasta e devolva **a pasta `perfil` inteira**, mantendo os arquivos, os pacotes `br.com.limefit.modulos.perfil` e as assinaturas públicas. Matheus irá copiar a pasta de volta para `src/main/java/br/com/limefit/modulos/` e compilar.

Teste PT/EN, duas contas e sair/entrar novamente. Se precisar alterar outra pasta, uma classe compartilhada ou o banco, descreva a mudança separadamente para combinarmos a integração. Não inclua `target/`, dependências baixadas ou credenciais.
