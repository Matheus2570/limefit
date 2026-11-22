# Entrega — LimeFit Desktop Swing

## Implementado

- Aplicação Java 17 iniciada na EDT, com um JFrame e CardLayout de JPanels.
- Home com logo e foto originais, fundo escuro, verde-limão, apresentação, entrada/cadastro e perfil conforme a sessão.
- Cadastro, confirmação separada, login, mensagens de validação, e-mail duplicado, nome da pessoa e saída.
- Inglês inicial e PT/EN para títulos, menu, campos, validações, mensagens já exibidas e confirmação.
- JDBC de autenticação fora da EDT; respostas antigas descartadas ao navegar, sair ou trocar de conta.
- Sessão desktop compartilhada, proteção de todas as cinco telas dos colegas e limpeza de dados ao trocar de conta/sair/fechar.
- Cinco módulos independentes, cada um com tela vazia e LEIA-ME. Quatro DAOs com contratos pendentes, modelos e colunas combinados previamente.
- Configurações Run and Debug/tarefa do VS Code e comando Maven abrindo a janela.
- Removidos servidor HTTP, páginas, roteador, cookies, CSS, JavaScript e dependência j2html, após conferir referências. Assets PNG preservados. Testes e documentação substituídos pelos equivalentes desktop.

## Banco e compatibilidade

O MariaDB do XAMPP estava desligado. Foi iniciado e o banco real foi inspecionado por consultas de metadados: cinco tabelas, colunas, índices, FKs compostas e oito gatilhos da migração anterior. Existe uma conta pessoal, que não foi usada nos testes nem alterada. Não foi necessário alterar o esquema.

A conexão JDBC e `Senhas.java` foram preservados. A compatibilidade foi testada com uma conta temporária contendo um hash PBKDF2 do formato anterior, calculado independentemente. O login correto foi aceito, a senha incorreta recusada e o hash persistido permaneceu idêntico.

- **Banco atual já migrado:** nenhum script de alteração é necessário. `sql/conferir-banco.sql` é uma conferência opcional e somente de leitura.
- **Instalação nova:** executar somente `sql/limefit.sql` (agora completo).
- **Banco anterior sem propriedade:** migração existente `sql/001-contas-e-propriedade.sql`, preservada.
- `sql/legado/` contém o esquema e a fixture antigos, apenas para testes.

## Verificação executada

`python scripts/validar.py` prepara bancos temporários, executa nove testes JUnit e remove os bancos ao terminar. Resultado: **9 testes, 0 falhas, 0 erros, nenhum ignorado**.

- Abertura real de JFrame e navegação por componentes Swing na EDT.
- Home em inglês e português; cadastro com senha curta e confirmação divergente nos dois idiomas.
- Cadastro de Ana e Bruno pela interface, confirmação sem login automático e e-mail duplicado.
- Senha incorreta, tradução do erro já exibido, login correto e normalização de e-mail.
- ID e nome distintos nas duas sessões na mesma janela, saída, limpeza de identidade/campos e exigência de novo login.
- Abertura de Perfil, Alimentos, Refeições, Planejamento e Projeto; retorno ao destino privado solicitado após login.
- Hash histórico, salts individuais e validações independentes da interface.
- Restrições no banco contra mistura de perfil/alimento/refeição de outra conta, contra mudança de dono e contra inserção sem dono.
- Exemplos de SELECT/UPDATE/DELETE com filtro por conta não acessam registros da outra conta. Os DAOs futuros permanecem pendentes, conforme solicitado.
- SQL completo importado duas vezes em banco novo; cinco tabelas e oito gatilhos conferidos.
- Migração incremental aplicada ao esquema original com registros legados e reaplicada após criar uma conta com hash: checksums das cinco tabelas preservados.
- Layout e capturas da janela nos tamanhos 1200×820 e 900×700; Home sem rolagem horizontal na janela menor.

As capturas de componentes Swing ficam em `target/validacao/`: `home-en.png`, `login-pt-erro.png`, `home-bruno-en.png`, `home-menor.png`, `cadastro-menor.png` e confirmações. A pasta target é gerada e não deve ser devolvida pelos colegas.

Os testes de interface acionam os componentes Swing com `doClick`/entrada nos campos, dentro de uma janela real; as capturas usam o desenho do JFrame. Não foi acionado o botão Run and Debug dentro da interface do VS Code. A configuração foi entregue, e o comando Maven exigido foi executado. A senha da conta pessoal não foi utilizada; a compatibilidade de login anterior foi validada com o hash de teste descrito acima.

A automação exigiu acesso ao cache Maven fora do sandbox e inicialização do MariaDB do XAMPP. Não houve rejeição de aprovação. O JDK local é 25; o Maven compila com `release 17`. Os avisos de Unsafe vêm do Maven no JDK 25 e não impediram compilar/executar os testes.

## Entrega dos módulos

Os colegas devem devolver somente a respectiva pasta em `src/main/java/br/com/limefit/modulos/`. Cada LEIA-ME descreve os métodos pendentes, dependências entre módulos, obtenção do usuário autenticado e regra para combinar alterações compartilhadas/banco separadamente. Modelos, parâmetros, retornos, unidades e colunas estão em `docs/CONTAS-E-DADOS.md`.
