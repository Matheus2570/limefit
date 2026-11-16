# LimeFit

Projeto acadêmico de Programação Orientada a Objetos e Inglês Técnico. Home e autenticação implementadas em **Java 17 + j2html**, com JDBC/MariaDB. As páginas dos demais módulos têm apenas menu e título para a equipe desenvolver nas próprias branches.

## Rodar no Windows

1. Inicie **MySQL** no XAMPP. Apache só é necessário para usar o phpMyAdmin.
2. No phpMyAdmin, importe o SQL na ordem abaixo:
   - Banco novo: `sql/limefit.sql` e depois `sql/001-contas-e-propriedade.sql`.
   - Já importou `limefit.sql`: importe **somente `sql/001-contas-e-propriedade.sql`**.
3. Com JDK 17 ou superior, execute na pasta do `pom.xml`:

   ```powershell
   .\mvnw.cmd compile exec:java
   ```

4. Abra http://localhost:8080/. O idioma inicial é inglês; PT/EN aparece no cabeçalho. Crie uma conta em `/cadastro`, confira a confirmação, entre em `/login` e use **Log out / Sair** no menu para encerrar a sessão.
5. http://localhost:8080/status/banco continua consultando o MariaDB. Retorna `"status":"ok"` quando a conexão e a tabela `alimentos` estão disponíveis.

O Maven Wrapper e o driver JDBC existentes foram preservados. O Wrapper recebeu uma correção para a pasta `.m2` sem link simbólico. A primeira compilação também baixa j2html; JUnit é usado somente nos testes.

| Variável de ambiente | Padrão | Uso |
|---|---|---|
| `LIMEFIT_DB_PORT` | `3306` | Porta do MariaDB do XAMPP |
| `LIMEFIT_DB_USER` | `root` | Usuário JDBC |
| `LIMEFIT_DB_PASSWORD` | vazio | Senha JDBC |
| `LIMEFIT_DB_NAME` | `limefit` | Nome do banco; permite testes isolados |
| `LIMEFIT_HTTP_PORT` | `8080` | Porta do servidor Java |
| `LIMEFIT_COOKIE_SECURE` | `false` | Usar `true` quando servido por HTTPS |

Exemplo para uma instalação na porta 3307:

```powershell
$env:LIMEFIT_DB_PORT = '3307'
.\mvnw.cmd compile exec:java
```

## Onde cada integrante trabalha

Todos os caminhos abaixo começam em `src/main/java/br/com/limefit/`. A interface de cada página fica no método `conteudo(HttpExchange pedido, Idioma idioma)` da própria classe. Use `idioma.escolher("Português", "English")` nos textos e `idioma.url("/rota")` nos links e ações dos formulários.

| Integrante | Rota | Classe | Branch sugerida |
|---|---|---|---|
| Matheus de Carvalho | `/` e autenticação | `modulos/inicio/InicioPagina.java`, `modulos/login/` | `feat/inicio-integracao` |
| Matheus Maiolo | `/perfil` | `modulos/perfil/PerfilPagina.java` | `feat/perfil` |
| Lucas Casagrande | `/alimentos` | `modulos/alimentos/AlimentosPagina.java` | `feat/alimentos` |
| Guilherme Azevedo | `/refeicoes` | `modulos/refeicoes/RefeicoesPagina.java` | `feat/refeicoes` |
| Gustavo Silva Gomes | `/planejamento` | `modulos/planejamento/PlanejamentoPagina.java` | `feat/planejamento` |
| Francisco da Silva | `/projeto` | `modulos/projeto/ProjetoPagina.java` | `feat/projeto-ingles` |

Essas cinco rotas exigem login. Modelos e DAOs futuros ficam na mesma pasta da página de cada integrante. O menu, o idioma, a proteção das páginas e o CSRF são aplicados pela classe abstrata `Pagina`.

### CSS de cada módulo

Os estilos ficam em `src/main/resources/assets/`. Cada página carrega `limefit.css` (estrutura e componentes compartilhados) e apenas o CSS do seu módulo, definido em `Tela`. Login, cadastro e confirmação de cadastro compartilham `login.css`.

| Módulo | Arquivo CSS | Classe no `body` |
|---|---|---|
| Início | `inicio.css` | `.pagina-inicio` |
| Login e cadastro | `login.css` | `.pagina-login` |
| Perfil | `perfil.css` | `.pagina-perfil` |
| Alimentos | `alimentos.css` | `.pagina-alimentos` |
| Refeições | `refeicoes.css` | `.pagina-refeicoes` |
| Planejamento | `planejamento.css` | `.pagina-planejamento` |
| Projeto | `projeto.css` | `.pagina-projeto` |

Cada integrante altera o CSS do próprio módulo, incluindo as regras responsivas. Prefixe os seletores com a classe da página para limitar o alcance dos estilos:

```css
.pagina-refeicoes .lista {
    display: grid;
}
```

Use `limefit.css` para mudanças que devem valer para todos: cores, tipografia base, menu, rodapé, botões e outros componentes comuns. Os módulos que ainda têm apenas título já possuem seu arquivo CSS preparado para receber estilos.

**O ID da conta sempre vem de `usuarioId(pedido)` ou `Autenticacao.exigirUsuario(pedido).id()`.** Nunca use `usuario_id` ou `perfil_id` recebidos do navegador como identidade. Inclusive os alimentos são particulares de cada conta. Veja os contratos e exemplos em [Contas e dados](docs/CONTAS-E-DADOS.md) antes de implementar os DAOs.

## Validação

Com MySQL do XAMPP ligado e Python 3 disponível:

```powershell
python scripts/validar.py
```

O script cria um banco temporário, importa o esquema original e os registros fictícios, aplica a migração duas vezes e executa cinco testes de integração HTTP/JDBC. Cria Ana e Bruno, confere PT/EN, cadastro, duplicidade, hash, login, CSRF, rotação de sessão, acesso direto a páginas privadas, saída, propriedade dos alimentos e restrições entre contas. A aplicação usa uma porta HTTP livre; seu servidor na 8080 continua independente. O banco temporário é removido ao terminar, inclusive as duas contas.

Use `LIMEFIT_MYSQL` se o executável `mysql` estiver fora do PATH e de `C:\xampp\mysql\bin\mysql.exe`. Os testes usam as mesmas credenciais JDBC da aplicação; o usuário do teste precisa criar e remover o banco temporário. `mvnw.cmd test` sem `LIMEFIT_TEST_DB` pula os testes de integração para não gravar no banco pessoal.

`sql/dados-teste.sql` foi mantido como fixture do esquema original, executada **antes** da migração na validação. Depois da migração, cadastre dados com um dono conforme o guia; esse SQL antigo não cria dados visíveis para uma conta.

Leia também [Trabalho em grupo](docs/TRABALHO-EM-GRUPO.md) e [Arquivos e validação desta entrega](docs/ENTREGA.md).
