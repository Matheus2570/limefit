# Entrega final — LimeFit Desktop

Integração validada em **26/11/2026**, com Java Swing, JDBC e MariaDB.

## Funcionalidades

- Home, cadastro, confirmação sem login automático, login e saída.
- Perfil por conta: consulta, cadastro/atualização, objetivo, IMC e exclusão com confirmação.
- Alimentos: busca e CRUD, nutrientes por 100 g, validação decimal e mensagens para nome duplicado ou alimento em uso.
- Refeições: listagem por data, criação, edição de nome/data, exclusão e inclusão/atualização/remoção de porções.
- Planejamento: data localizada em PT/EN, Hoje e navegação entre dias, refeições e totais de calorias, proteínas, carboidratos e gorduras.
- Projeto: apresentação, equipe, português/inglês e retorno à Home.

## Correções da integração

O planejamento usa o modelo real de `Refeicao`, incluindo o ID do proprietário, consulta lista e totais na mesma transação e fecha a conexão. O perfil preserva seu token técnico e salva de forma atômica pela chave única da conta. Medidas e porções com mais de duas casas decimais são recusadas antes de gravar.

Todo JDBC dos módulos fica fora da EDT. Os trabalhos recebem a identidade da sessão que os agendou; trocar de conta não redireciona uma gravação em andamento. Respostas antigas são descartadas, e campos, listas e caches são limpos na saída. Controles bloqueiam operações concorrentes no mesmo formulário.

A tela de refeições mantém os IDs dos itens, sem consultas ao banco na EDT nem busca por nome para remover alimentos. Formulários, cabeçalhos, mensagens e números foram revisados em PT/EN. O layout de refeições adapta as colunas à largura; o Perfil mostra os campos, IMC e ações na janela padrão. As capturas foram inspecionadas e os problemas de layout encontrados foram corrigidos.

## Banco

Não houve alteração no esquema nem nos dados do banco pessoal `limefit`. O MariaDB do XAMPP foi iniciado para validação. Os testes criam seus próprios bancos `limefit_validacao_<número>` e os removem ao terminar.

- Instalação nova: `sql/limefit.sql`.
- Banco já migrado: nenhum script adicional.
- Instalação antiga sem contas/propriedade: migração `sql/001-contas-e-propriedade.sql`.
- `sql/legado/`: somente fixtures para validar migração.

## Verificação executada

```powershell
python scripts/validar.py
```

O script importa o SQL e a migração em bancos temporários, executa `mvnw.cmd clean test` e remove os bancos. Resultado: **17 testes, 0 falhas, 0 erros, nenhum ignorado**, com as 39 classes de produção compiladas do zero para Java 17.

Foram conferidos:

- SQL completo importado duas vezes, com cinco tabelas e oito gatilhos.
- Migração reaplicada sem alterar checksums de contas, hashes ou dados legados.
- Hash PBKDF2 histórico, salt individual, validações de cadastro e login.
- Restrições SQL e DAOs contra mistura de dados de duas contas.
- Criação e atualização do perfil preservando ID/token; exclusão em cascata preservando alimentos e os dados da outra conta.
- Porções atualizadas sem duplicação; datas alteradas; dias vazios com totais zero.
- Precisão dos quatro totais: por exemplo, 150,50 g de um alimento com 389,25 kcal/100 g somam 585,82125 kcal antes do arredondamento de exibição.
- Tarefas em andamento mantêm a conta original e não aplicam respostas após troca de sessão.
- Janela real: cadastro de duas contas, login, navegação, perfil, alimentos, criação/edição de refeição, atualização/remoção de porções, planejamento, datas inválidas, PT/EN e exclusão de perfil com confirmação.
- Carga Java de demonstração com autenticação, repetição sem duplicação, preservação de edições e rollback para senha diferente.
- Saída e entrada com a segunda conta sem dados privados da primeira.
- Home em janela de 900×700 e módulos na janela padrão de 1200×820.

As capturas são geradas em `target/validacao/`: `perfil-pt.png`, `refeicoes-pt.png`, `planejamento-pt.png`, `projeto-en.png`, além de Home, alimentos, cadastro e login. Os testes acionam componentes Swing dentro de um JFrame real, na EDT. O Run and Debug do VS Code não foi acionado pela interface; o projeto foi compilado e executado pela validação Maven.

O JDK local é 25, com `release 17`. Os avisos de Unsafe são do Maven nesse JDK. As mensagens de restrição SQL no log são esperadas nos testes que verificam tentativas inválidas. A execução precisou de acesso ao cache Maven e ao diretório do MariaDB fora do sandbox, autorizado pela revisão automática.

## Carga de demonstração

`preparar-demo.cmd` (Windows) ou `sh preparar-demo.sh` (Linux/macOS) prepara a conta `demo@limefit.local` (senha `LimeFit123!`), um perfil, 15 alimentos, 12 refeições e 43 porções entre ontem, hoje e amanhã. A carga não altera o esquema nem os dados de outras contas. O comando pode ser repetido e preserva registros e edições existentes; valores nutricionais são ilustrativos.

A carga usa o próprio Java/JDBC, sem depender de Python, cliente mysql ou caminhos específicos de um computador. A conexão é a mesma do aplicativo; em outro computador, são necessários JDK 17+, MariaDB e o esquema configurado. O primeiro uso do Maven Wrapper requer internet. O atalho Python anterior apenas encaminha para a carga Java.

A carga é verificada por `DemoIntegracaoTest` em banco temporário: contagens dos registros, quatro refeições por data, repetição sem mudança de checksums, preservação de edições e outra conta, recusa de sobrescrever senha diferente e login/consulta de planejamento pelas classes Java reais. O atalho Windows também foi executado com sucesso a partir de outra pasta com espaços no nome. O atalho de shell foi incluído para Linux/macOS; não foi executado nesses sistemas nesta sessão. Nenhuma carga foi aplicada ao banco pessoal durante esses testes.

## Executar

Com o MySQL do XAMPP ligado, na raiz do projeto:

```powershell
.\mvnw.cmd compile exec:java
```

O uso de cada tela está no [README](../README.md); contratos e unidades estão em [CONTAS-E-DADOS](CONTAS-E-DADOS.md). Não há métodos de funcionalidade deixados como pendentes.
