# LimeFit Desktop

Projeto acadêmico de POO e Inglês, em **Java 17 + Swing + JDBC/MariaDB**. O comando de execução abre uma janela própria. Home, cadastro, login, Perfil, Alimentos, Refeições, Planejamento e Projeto estão integrados, com dados separados por conta e interface em português e inglês.

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

Todos os módulos estão implementados. Os guias `LEIA-ME.md` de cada pasta descrevem o funcionamento e os contratos preservados. Consulte [Trabalho em grupo](docs/TRABALHO-EM-GRUPO.md) e [Contratos de dados](docs/CONTAS-E-DADOS.md).

## Usar o aplicativo

1. Crie uma conta e entre. O cadastro não faz login automaticamente.
2. Em **Meu perfil**, informe peso (25–300 kg), altura (100–230 cm) e objetivo. O IMC é calculado automaticamente. Medidas aceitam ponto ou vírgula e até duas casas decimais.
3. Em **Alimentos**, cadastre nutrientes por **100 g**. É possível buscar, editar e excluir alimentos; alimentos usados em refeições precisam ser removidos dessas refeições antes da exclusão.
4. Em **Refeições**, selecione a data no formato `AAAA-MM-DD`, digite um nome e clique em **Criar**. Selecione a refeição para definir as porções dos alimentos. Repetir o mesmo alimento atualiza sua porção. **Salvar nome e data** altera a refeição selecionada; **Nova refeição** limpa a seleção.
5. Em **Planejamento**, consulte as refeições e os totais de calorias, proteínas, carboidratos e gorduras. Use Hoje, dia anterior/próximo ou digite a data: `dd/mm/aaaa` em português e `mm/dd/yyyy` em inglês.
6. Em **O projeto**, veja a apresentação e os integrantes. PT/EN pode ser alternado pelo cabeçalho.

Excluir uma refeição remove seus itens; excluir o perfil remove suas refeições e itens. As telas pedem confirmação dessas exclusões. Os alimentos são preservados ao excluir o perfil. Sair limpa os dados da interface, e a próxima conta só acessa seus próprios registros.

## Preparar uma apresentação em outro computador

Copie ou clone o projeto inteiro e abra o terminal na pasta que contém `pom.xml`. A carga usa o próprio Java/JDBC: não precisa de Python, cliente mysql nem caminho fixo do XAMPP.

Requisitos: **JDK 17 ou superior**, servidor MariaDB com o esquema `sql/limefit.sql` instalado e conexão configurada pelas variáveis `LIMEFIT_DB_PORT`, `LIMEFIT_DB_NAME`, `LIMEFIT_DB_USER` e `LIMEFIT_DB_PASSWORD`, quando diferentes dos padrões. O Maven Wrapper baixa suas dependências na primeira execução, por isso precisa de internet nessa etapa. Em banco já migrado, não reinstale o SQL.

No Windows (PowerShell ou terminal do VS Code):

```powershell
.\preparar-demo.cmd
if ($LASTEXITCODE -eq 0) {
    .\mvnw.cmd compile exec:java
}
```

No Linux/macOS:

```sh
sh ./preparar-demo.sh && sh ./mvnw compile exec:java
```

O atalho encontra o projeto a partir da sua própria localização, mesmo quando a pasta contém espaços. Entre com **demo@limefit.local**, senha **LimeFit123!**. A carga cria um perfil, 15 alimentos e 12 refeições com 43 porções distribuídas entre ontem, hoje e amanhã. Os valores nutricionais são ilustrativos para demonstrar o aplicativo.

Pode repetir o comando: registros existentes, alterações manuais e outras contas são preservados. Para manter as telas preenchidas no dia da apresentação, rode novamente nesse dia. Se já estiver usando a conta demo, acesse outra tela e volte para atualizar. Se essa conta existir com outra senha, a carga é revertida sem sobrescrever seus dados.

Exemplo de uma instalação com senha de banco diferente, no PowerShell:

```powershell
$env:LIMEFIT_DB_USER = 'root'
$env:LIMEFIT_DB_PASSWORD = 'senha-do-banco'
.\preparar-demo.cmd
```

Para preparar uma data específica, execute `.\mvnw.cmd "-Dexec.mainClass=br.com.limefit.demonstracao.PopularDemo" "-Dexec.args=2026-11-26" compile exec:java` (em Linux/macOS, use `sh ./mvnw`). O atalho antigo `python scripts/popular_demo.py` continua disponível e encaminha para a mesma carga Java.

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

O primeiro comando executa os testes unitários sem acessar o banco pessoal; os testes de integração são ignorados sem o ambiente temporário. O segundo exige Python 3, uma sessão gráfica, MySQL do XAMPP e permissão do usuário SQL para criar/remover bancos temporários. Use `LIMEFIT_MYSQL` para indicar outro caminho do cliente mysql. O script prepara bancos separados, verifica o SQL completo e a migração, compila do zero, abre uma janela Swing real, testa duas contas e o fluxo Perfil → Alimentos → Refeições → Planejamento, e remove os bancos temporários ao terminar. Não altera `limefit`.

Resultados e evidências em [Entrega e validação](docs/ENTREGA.md). As capturas ficam em `target/validacao/` após a validação completa.
