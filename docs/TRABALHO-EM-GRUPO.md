# Trabalho em grupo — entrega por pasta

Matheus de Carvalho mantém a Home, autenticação e integração. Os cinco módulos dos colegas estão em `src/main/java/br/com/limefit/modulos/`, cada um com tela, conteúdo vazio e guia local. Todos abrem no menu desde o início, após o login.

## Ciclo de entrega

1. Receba o **projeto completo**, instale o JDK e configure o banco pelo README.
2. Crie uma branch, por exemplo `feat/perfil`, `feat/alimentos`, `feat/refeicoes`, `feat/planejamento` ou `feat/projeto-ingles`.
3. Edite principalmente a sua pasta. Preserve nomes de arquivos, pacotes, construtores e assinaturas públicas.
4. Execute `.mvnw.cmd compile exec:java`; teste em inglês e português, com duas contas e após sair/entrar de novo.
5. Devolva **somente a pasta do seu módulo**, incluindo o LEIA-ME atualizado. Não inclua `target/`, `.git`, credenciais ou bibliotecas baixadas.
6. Se precisar mudar o banco, uma classe compartilhada ou o contrato de outro módulo, descreva a proposta em um arquivo separado e combine com Matheus de Carvalho. Não faça a integração depender de uma mudança oculta.
7. Matheus incorpora no mesmo caminho e executa `.mvnw.cmd test` e `python scripts/validar.py`.

## O que já está conectado

O construtor `TelaX(ContextoAplicacao contexto)` recebe os recursos. Use o `JPanel conteudo` herdado de `PainelModulo` para a interface; `BorderLayout`, `GridBagLayout`, `BoxLayout` e `GridLayout` evitam coordenadas fixas. Não crie outro JFrame para a tela.

```java
long usuarioId = contexto.usuarioId(); // Falha se não houver login.
String nome = contexto.sessao().usuario().nome();
contexto.navegar(Tela.REFEICOES);
String titulo = idioma.escolher("Título", "Title");
```

Use `Visual.botao`, `Visual.campo`, `Visual.rotulo` e `Visual.rolagem` para manter as cores e os espaçamentos. Os DAOs são criados com `contexto.conexao()` e `contexto.sessao()` no construtor da tela; não substitua a conexão por credenciais fixas.

Sobrescreva `atualizarTextos()` chamando `super.atualizarTextos()` para traduzir todos os controles e mensagens, inclusive erros já exibidos. Ela também pode ser chamada durante a construção da janela; inicialize seus componentes antes de usá-los.

Carregue dados em `aoExibir()` (chamando `super.aoExibir()`) para atualizar após alterações de outro módulo. Execute JDBC em `SwingWorker.doInBackground()` e atualize Swing em `done()`. Não bloqueie a EDT com consultas. Antes de iniciar o trabalho, capture a revisão da sessão; descarte respostas se ela mudar ou a tela for fechada. Em `limparDados()`, esvazie campos, tabelas, seleções e caches, e invalide trabalhos antigos. `aoOcultar()` pode interromper carregamentos ligados à navegação.

Os modelos são records Java imutáveis, com acesso por `perfil.pesoKg()`, `alimento.nome()` etc. Para editar, monte novos valores ou envie os parâmetros previstos no DAO. Os contratos detalhados e os nomes SQL estão em [CONTAS-E-DADOS](CONTAS-E-DADOS.md).

## Integração entre colegas

- **Perfil** mantém um perfil por conta. Refeições usa `PerfilDao.buscarAtual()` e trata perfil ausente com convite para `Tela.PERFIL`.
- **Alimentos** fornece `AlimentoDao.listar("")` e `buscarPorId(id)` para a seleção das porções.
- **Refeições** mantém datas, nomes e itens. Planejamento pode consumir `listarPorData(data)` e `listarItens(refeicaoId)`.
- **Planejamento** monta a visão por data e calcula os totais. Não há tabela extra de planejamento. Para totais consistentes, seu DAO pode consultar diretamente as tabelas existentes, usando os mesmos contratos e escopos por conta.
- **Projeto** apresenta equipe e revisa o inglês; não precisa de DAO.

Não devolva alterações nos arquivos de outro colega. Coordene qualquer mudança de assinatura para que uma pasta nova continue compilando com as demais.
