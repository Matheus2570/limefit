# Contas e dados

## Identidade no Java

`Autenticacao` mantém as sessões em memória no servidor. O cookie `LIMEFIT_SESSION` contém um token aleatório de 256 bits, com `HttpOnly`, `SameSite=Lax` e caminho `/`. A identidade não fica no cookie. Ao entrar, o token e o CSRF são trocados; ao sair, a sessão anterior é invalidada. As sessões expiram após 30 minutos sem uso e terminam quando o servidor reinicia. Para HTTPS, configure `LIMEFIT_COOKIE_SECURE=true`.

`Usuario` contém ID, nome e e-mail retornados pelo banco. As senhas novas usam PBKDF2-HMAC-SHA256, 600 mil iterações, salt aleatório por senha e comparação do resultado em tempo constante. O ZIP é uma referência de fluxo e visual: seus scripts PHP e suas contas não são importados. Hashes PHP não são convertidos por este SQL; o fluxo entregue cria contas pelo cadastro Java.

Dentro de uma subclasse de `Pagina`:

```java
long dono = usuarioId(pedido);
```

Em outro handler ou classe Java:

```java
long dono = Autenticacao.exigirUsuario(pedido).id();
DadosDaConta conta = new DadosDaConta(conexao, pedido);
boolean permitido = conta.pertence(DadosDaConta.Registro.ALIMENTO, alimentoId);
```

`exigirUsuario` falha se não houver login. `DadosDaConta` recebe a requisição e obtém o dono da sessão; seu construtor não recebe o dono de um formulário. A checagem `pertence` ajuda a validar referências, mas cada consulta, atualização ou exclusão ainda precisa filtrar a conta na própria instrução SQL.

As páginas herdam a checagem de login de `Pagina.handle`. Novas páginas privadas precisam usar uma `Tela` com `privada=true`. Handlers de API registrados diretamente no roteador devem verificar a sessão antes de processar dados e retornar 401 quando ela não existir.

## Esquema e migração

Importe `sql/001-contas-e-propriedade.sql` sobre o `sql/limefit.sql` existente. Em uma instalação nova, importe os dois nessa ordem. O script foi validado no MariaDB do XAMPP e pode ser reaplicado.

| Tabela | Propriedade e vínculos |
|---|---|
| `usuarios` | Conta com e-mail único e hash de senha |
| `perfis` | `usuario_id`, um perfil por conta; `identificador` original preservado |
| `alimentos` | `usuario_id`; nome único **por conta**, permitindo o mesmo nome para pessoas diferentes |
| `refeicoes` | `usuario_id`; a FK `(perfil_id, usuario_id)` exige perfil do mesmo dono |
| `itens_refeicao` | `usuario_id`; as FKs compostas exigem refeição **e alimento** do mesmo dono |

As colunas `usuario_id` são nullable somente para preservar linhas anteriores à migração. Gatilhos exigem dono em novos registros e impedem retirar ou trocar um dono já atribuído. As linhas antigas permanecem com seus IDs, valores e vínculos, porém com dono `NULL`, ficando fora de consultas `WHERE usuario_id = ?`. Não há uma conta global nem alimentos compartilhados.

Se o grupo precisar recuperar dados antigos, um administrador deve identificar o dono correto e associar explicitamente as linhas. Associe primeiro perfil e alimentos, depois refeições e itens, na mesma transação. Nunca ofereça ao usuário uma opção de reivindicar registros por ID. A migração não faz essa associação automaticamente.

O cadastro cria somente a conta. O colega do Perfil implementará a criação do perfil com medidas válidas; não há peso ou altura fictícios criados pelo login. Ao criar o perfil, mantenha `identificador` com 64 caracteres únicos, por exemplo dois UUIDs sem hífen concatenados; ele não substitui `usuario_id`.

## Contrato para os futuros DAOs

Use `PreparedStatement`, `try-with-resources` e o dono obtido da sessão. Os exemplos abaixo são orientações para as branches; não há CRUD desses módulos implementado nesta entrega.

Listar e consultar:

```sql
SELECT id, nome, calorias, proteinas, carboidratos, gorduras
FROM alimentos WHERE usuario_id = ? ORDER BY nome;

SELECT id, nome, calorias FROM alimentos WHERE id = ? AND usuario_id = ?;

SELECT id FROM perfis WHERE usuario_id = ?;

SELECT id, nome, data_plano FROM refeicoes
WHERE usuario_id = ? AND data_plano = ? ORDER BY id;
```

O `id` de um alimento ou refeição pode vir da URL como seletor. O parâmetro `usuario_id` sempre vem da sessão. A ausência de resultado deve ser tratada como registro inexistente, sem revelar se pertence a outra conta.

Gravar um alimento:

```java
long dono = usuarioId(pedido);
try (var banco = conexao.abrir();
     var inserir = banco.prepareStatement("""
         INSERT INTO alimentos
             (nome, calorias, proteinas, carboidratos, gorduras, usuario_id)
         VALUES (?, ?, ?, ?, ?, ?)
         """)) {
    // Parâmetros 1 a 5: dados validados do formulário.
    inserir.setString(1, nome);
    inserir.setBigDecimal(2, calorias);
    inserir.setBigDecimal(3, proteinas);
    inserir.setBigDecimal(4, carboidratos);
    inserir.setBigDecimal(5, gorduras);
    inserir.setLong(6, dono);
    inserir.executeUpdate();
}
```

Alterar ou excluir, conferindo se `executeUpdate()` afetou a quantidade esperada de registros:

```sql
UPDATE alimentos SET nome = ?, calorias = ? WHERE id = ? AND usuario_id = ?;
DELETE FROM alimentos WHERE id = ? AND usuario_id = ?;
UPDATE perfis SET peso_kg = ?, altura_cm = ?, objetivo = ? WHERE id = ? AND usuario_id = ?;
UPDATE refeicoes SET nome = ? WHERE id = ? AND usuario_id = ?;
DELETE FROM refeicoes WHERE id = ? AND usuario_id = ?;
DELETE FROM itens_refeicao WHERE refeicao_id = ? AND alimento_id = ? AND usuario_id = ?;
```

Para criar uma refeição, consulte o perfil pelo dono e valide cada alimento com `id = ? AND usuario_id = ?`. Grave refeição e itens com o mesmo dono, na mesma transação. As FKs compostas também recusam vínculos entre contas. Excluir uma refeição remove seus itens; alimentos usados em itens têm exclusão restrita pelas FKs existentes.

Para somar nutrientes no Planejamento:

```sql
SELECT r.id, r.nome,
       SUM(a.calorias * i.quantidade_g / 100) AS calorias,
       SUM(a.proteinas * i.quantidade_g / 100) AS proteinas,
       SUM(a.carboidratos * i.quantidade_g / 100) AS carboidratos,
       SUM(a.gorduras * i.quantidade_g / 100) AS gorduras
FROM refeicoes r
JOIN itens_refeicao i ON i.refeicao_id = r.id AND i.usuario_id = r.usuario_id
JOIN alimentos a ON a.id = i.alimento_id AND a.usuario_id = r.usuario_id
WHERE r.usuario_id = ? AND r.data_plano = ?
GROUP BY r.id, r.nome;
```

## Formulários e telas

Cada módulo sobrescreve `conteudo(HttpExchange pedido, Idioma idioma)` e retorna `DomContent` do j2html. A base aplica menu, idioma e título do documento. O j2html escapa textos: passe dados do usuário para `text`, `p`, `span` ou `.withValue`, evitando `rawHtml`.

Para processar um formulário, sobrescreva:

```java
@Override
protected void enviarFormulario(HttpExchange pedido, Idioma idioma,
                                Map<String, String> campos) throws IOException {
    long dono = usuarioId(pedido);
    // Validar campos, chamar o DAO com o dono da sessão e redirecionar.
}
```

`Pagina` já lê o formulário e valida o CSRF antes de chamar esse método. Inclua no formulário j2html:

```java
input().withType("hidden").withName("csrf_token")
       .withValue(Autenticacao.sessao(pedido).csrf())
```

Use `method=post` para gravações e `idioma.url(...)` nas ações e redirecionamentos. A saída também usa POST e CSRF; `GET /sair` retorna 405. O formulário aceita `application/x-www-form-urlencoded` até 16 KiB; campos repetidos são recusados.
