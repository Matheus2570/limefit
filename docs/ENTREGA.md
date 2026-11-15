# Entrega: Home e autenticação

## Resultado

Home com foto do prato, cabeçalho escuro, botões verde-limão e duas seções. Login, cadastro, confirmação e saída portados para Java. Inglês inicial e PT/EN em todas as telas e mensagens de autenticação. As cinco páginas dos colegas têm somente menu comum, título e rodapé, com login obrigatório.

`index (1)(1).html` não estava disponível em Downloads; a referência encontrada foi `index (1).html`. O logo e a foto já presentes em `assets` têm os mesmos hashes das imagens embutidas nesse HTML. Foram reutilizados sem modificações. Os arquivos PHP do ZIP foram lidos como referência; não foram incorporados ao servidor Java nem suas contas importadas.

## Arquivos alterados

- `pom.xml`: j2html, JUnit e Surefire.
- `mvnw.cmd`: correção do acesso a `Target[0]` quando `.m2` não é um link simbólico.
- `src/main/java/br/com/limefit/Aplicacao.java`: composição comum das rotas e registro da autenticação.
- `src/main/java/br/com/limefit/banco/Conexao.java`: nome de banco configurável, mantendo `limefit` como padrão e as configurações JDBC existentes.
- `src/main/java/br/com/limefit/web/Idioma.java`: inglês padrão e URLs com idioma explícito.
- `src/main/java/br/com/limefit/web/Tela.java`: metadados das telas, menu e proteção das cinco rotas privadas.
- `src/main/java/br/com/limefit/web/Layout.java`: menu, PT/EN, conta, saída, rodapé e documento em j2html.
- `src/main/java/br/com/limefit/web/Pagina.java`: renderização Java, autenticação, processamento de formulário e validação CSRF comuns.
- `src/main/java/br/com/limefit/web/Resposta.java`: redirecionamentos e cabeçalhos HTTP; erro de método em PT/EN.
- `src/main/java/br/com/limefit/modulos/inicio/InicioPagina.java`: Home.
- `src/main/java/br/com/limefit/modulos/perfil/PerfilPagina.java`: ponto de edição da página vazia do Perfil.
- `src/main/java/br/com/limefit/modulos/alimentos/AlimentosPagina.java`: ponto de edição da página vazia de Alimentos.
- `src/main/java/br/com/limefit/modulos/refeicoes/RefeicoesPagina.java`: ponto de edição da página vazia de Refeições.
- `src/main/java/br/com/limefit/modulos/planejamento/PlanejamentoPagina.java`: ponto de edição da página vazia de Planejamento.
- `src/main/java/br/com/limefit/modulos/projeto/ProjetoPagina.java`: ponto de edição da página vazia do Projeto.
- `src/main/resources/assets/limefit.css`: visual e adaptação para celular.
- `sql/dados-teste.sql`: somente comentários para identificar a fixture anterior à migração.
- `README.md` e `docs/TRABALHO-EM-GRUPO.md`: execução, importação, testes e divisão das branches atualizados.

## Arquivos adicionados

- `autenticacao/Autenticacao.java`, `Sessao.java`, `Usuario.java`, `Senhas.java`, `UsuarioDao.java` e `Sair.java`, sob `src/main/java/br/com/limefit/`.
- `web/Formulario.java` e `banco/DadosDaConta.java`, sob a mesma raiz Java.
- `modulos/login/LoginPagina.java`, `CadastroPagina.java`, `CadastroSucessoPagina.java` e `FormulariosLogin.java`, sob a mesma raiz Java.
- `src/main/resources/assets/autenticacao.js`: mostrar/ocultar senha, sem dependências externas.
- `sql/001-contas-e-propriedade.sql`: usuários e propriedade dos dados.
- `src/test/java/br/com/limefit/LoginIntegracaoTest.java`: testes HTTP/JDBC.
- `scripts/validar.py`: preparação e limpeza do banco temporário dos testes.
- `docs/CONTAS-E-DADOS.md` e este relatório.

## Arquivos removidos

Removidos os 13 templates sem referências de execução:

- `src/main/resources/views/layout.html`.
- `pagina.html` e `pagina.en.html` nas pastas `views/inicio`, `views/perfil`, `views/alimentos`, `views/refeicoes`, `views/planejamento` e `views/projeto`.

Não havia PHP no código-fonte da pasta aberta. `pom.xml`, os dois Maven Wrappers, `.mvn`, CSS, imagens, `sql/limefit.sql` e a fixture SQL foram preservados. `Roteador` e `StatusBanco` foram reaproveitados. Os HTML compilados antigos em `target/classes/views` também foram retirados; `target` contém somente artefatos locais ignorados pelo Git.

## SQL para importar

Como o banco existente já recebeu `limefit.sql`, importe **apenas `sql/001-contas-e-propriedade.sql`** no phpMyAdmin. O banco principal não foi alterado durante esta entrega. Para uma instalação nova, importe primeiro `sql/limefit.sql` e depois a migração.

O script preserva os dados antigos com dono `NULL`; eles ficam invisíveis às contas até uma associação administrativa explícita. Dados novos exigem dono. Os alimentos passam a ser particulares e as FKs compostas impedem relacionamentos entre contas. Veja detalhes e exemplos de consultas em [Contas e dados](CONTAS-E-DADOS.md).

## Validação executada em 15/11/2026

- `mvnw.cmd compile`: compilação Java com sucesso.
- `mvnw.cmd compile exec:java`: servidor iniciado na porta 8081 durante a conferência, sem interromper o processo já existente do usuário.
- `python scripts/validar.py`: **5 testes, 0 falhas, 0 erros, 0 ignorados**.
- Home em inglês, PT/EN, campos e mensagens traduzidos, cadastro, confirmação, login incorreto/correto, e-mail duplicado, hash de senha, CSRF e saída conferidos por HTTP.
- Acesso direto às cinco páginas privadas sem sessão redirecionou ao login; autenticado, elas retornaram menu e título.
- `/status/banco` respondeu HTTP 200 com `status=ok` e consulta real ao MariaDB.
- Ana e Bruno foram cadastrados pelo fluxo HTTP, com IDs distintos e sessões separadas. Um `usuario_id` falso no formulário não alterou a identidade. Requisições concorrentes mantiveram a conta correta. A saída de Ana invalidou seu cookie anterior e preservou a sessão de Bruno.
- Consultas de propriedade recusaram o alimento alheio; UPDATE e DELETE filtrados pela conta não afetaram linhas de outra pessoa. O banco recusou alimento sem dono, troca de dono e vínculos de perfil/refeição/alimento entre contas diferentes.
- A migração foi aplicada duas vezes em banco isolado. Os registros legados permaneceram sem dono, com os totais originais: 340 kcal, 15,5 g de proteínas, 55 g de carboidratos e 6 g de gorduras.
- Chrome headless com viewport real de 320, 390 e 1440 pixels: imagens carregadas, sem rolagem horizontal, e botão de mostrar/ocultar senha funcional. Capturas locais em `target/home-desktop.png`, `home-mobile.png`, `login-mobile.png`, `cadastro-mobile.png` e `cadastro-small.png`.

As contas de teste ficam somente nos bancos temporários e são removidas com eles. O comando normal de execução continua `mvnw.cmd compile exec:java`. Nenhum CRUD das cinco páginas foi implementado.
