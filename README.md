# LimeFit Desktop

Projeto acadêmico de POO e Inglês, em **Java 17 + Swing + JDBC/MariaDB**. O comando de execução abre uma janela própria. Home, cadastro, confirmação, login e saída estão implementados. Perfil, Alimentos, Refeições, Planejamento e Projeto abrem com menu e título e têm o conteúdo vazio para a equipe.

## Executar no Windows / VS Code

1. Instale JDK 17 ou superior. No XAMPP, inicie **MySQL**. Apache só é necessário para abrir o phpMyAdmin.
2. **Seu banco `limefit` já migrado atende ao desktop. Não execute alteração de esquema.** As cinco tabelas, colunas de dono, índices, FKs compostas e oito gatilhos foram conferidos no MariaDB local.
3. Para uma **instalação nova**, execute apenas [sql/limefit.sql](sql/limefit.sql), que agora contém o esquema completo. Para uma instalação antiga sem `usuarios`/`usuario_id`, existe [sql/001-contas-e-propriedade.sql](sql/001-contas-e-propriedade.sql); a conversão para Swing não exige nova migração. A pasta `sql/legado/` contém somente fixtures usadas na validação, não scripts para instalar a aplicação atual.
4. Abra a pasta que contém `pom.xml` no VS Code e execute no terminal PowerShell:

   ```powershell
   .\mvnw.cmd compile exec:java
   ```

   Também é possível usar **Run Java** em `Aplicacao.java` ou a configuração **LimeFit Desktop** em Run and Debug. A tarefa **LimeFit: abrir janela** executa o mesmo comando Maven. Feche a janela para encerrar a aplicação; não há servidor web.

O idioma inicial é inglês. PT/EN traduz os títulos, o menu, os formulários, as validações e as mensagens. O cadastro aceita nome de até 120 caracteres, e-mail de até 190 e senha de 8 a 128 caracteres. A confirmação não autentica automaticamente: entre depois de criar a conta. E-mails são normalizados para minúsculas e permanecem únicos. Os hashes PBKDF2 e as contas anteriores são reaproveitados sem conversão.

| Variável | Padrão | Finalidade |
|---|---|---|
| `LIMEFIT_DB_PORT` | `3306` | Porta do MariaDB local |
| `LIMEFIT_DB_NAME` | `limefit` | Banco |
| `LIMEFIT_DB_USER` | `root` | Usuário JDBC |
| `LIMEFIT_DB_PASSWORD` | vazio | Senha JDBC |

Exemplo: `$env:LIMEFIT_DB_PORT = '3307'`, seguido do comando Maven. O Wrapper mantém sua correção para `.m2` sem link simbólico e usa o perfil do Windows para o cache, inclusive quando o JDK informa `user.home=C:\`. Um `-Dmaven.repo.local=...` explícito ainda tem precedência.

## Pastas de trabalho

A raiz Java é `src/main/java/br/com/limefit/`.

| Responsável | Pasta a devolver | Tela |
|---|---|---|
| Matheus de Carvalho | `modulos/inicio/`, `modulos/login/` e integração | `TelaInicio`, `TelaLogin`, `TelaCadastro`, `TelaCadastroSucesso` |
| Matheus Maiolo | `modulos/perfil/` | `TelaPerfil.java` |
| Lucas | `modulos/alimentos/` | `TelaAlimentos.java` |
| Guilherme | `modulos/refeicoes/` | `TelaRefeicoes.java` |
| Gustavo | `modulos/planejamento/` | `TelaPlanejamento.java` |
| Francisco | `modulos/projeto/` | `TelaProjeto.java` |

Cada pasta dos colegas inclui seu próprio `LEIA-ME.md`. Os módulos que usam dados já contêm modelos e um DAO com assinaturas estáveis. **Os métodos pendentes lançam `UnsupportedOperationException`; não retornam dados fictícios nem gravam no banco.** As telas continuam vazias até a implementação dos colegas.

Compartilhe o projeto inteiro. Cada colega trabalha no próprio módulo, mantém nomes/pacotes/contratos e devolve a pasta correspondente. Ao integrar, copie para o mesmo caminho e compile. Alterações na base ou no banco devem ser descritas separadamente e combinadas antes de integrar. Consulte [Trabalho em grupo](docs/TRABALHO-EM-GRUPO.md) e [Contratos de dados](docs/CONTAS-E-DADOS.md).

## Estrutura e POO

- `Aplicacao`: inicializa o Swing na EDT.
- `desktop/JanelaPrincipal`: único JFrame; CardLayout, menu, proteção das telas e navegação.
- `desktop/ContextoAplicacao`: composição de conexão, sessão, idioma e navegação; injetada pelos construtores.
- `desktop/PainelTela` e `PainelModulo`: herança para o ciclo de vida e o painel de conteúdo.
- `desktop/Visual`: componentes, cores, layouts e imagens compartilhados.
- `autenticacao/ServicoContas`: validação; `UsuarioDao`: JDBC; `Senhas`: PBKDF2; `Sessao`: identidade em memória.
- `banco/Conexao`: mesma conexão JDBC existente; `DaoDaConta`: base dos contratos de dados privados.
- `modulos/`: interfaces, modelos e DAOs de cada pessoa.

A dependência de execução é o driver MariaDB. JUnit é usado só nos testes. Não há j2html, páginas, cookies, CSS, JavaScript ou servidor HTTP na aplicação. As imagens originais continuam em `src/main/resources/assets/`. A Home usa duas colunas em janelas maiores e empilha texto/foto em janelas menores. Os formulários têm rolagem vertical quando necessário.

## Validação

```powershell
.\mvnw.cmd test
python scripts/validar.py
```

O primeiro comando executa os testes unitários sem acessar o banco pessoal. O segundo exige Python 3, uma sessão gráfica, MySQL do XAMPP e permissão do usuário SQL para criar/remover bancos temporários. Use `LIMEFIT_MYSQL` para indicar outro caminho do cliente mysql. O script prepara bancos separados, verifica o SQL completo e a migração, abre uma janela Swing real, testa duas contas e remove os bancos temporários ao terminar. Não altera `limefit`.

Resultados e evidências em [Entrega e validação](docs/ENTREGA.md). As capturas ficam em `target/validacao/` após a validação completa.
