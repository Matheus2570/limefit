# Módulo refeicoes — Guilherme

## Implementado

Refeições por data (AAAA-MM-DD), cadastro, edição de nome/data e exclusão com confirmação. Requer perfil para criar refeições. Porções maiores que zero e até 10000 g, com até duas casas decimais e ponto ou vírgula. Repetir um alimento atualiza a porção; remover usa o ID do item. Excluir refeição remove seus itens.

## Integração

Tela Swing baseada em `PainelModulo` e `Visual`, conectada à janela pelo `ContextoAplicacao`. Textos, validações e mensagens estão em PT/EN. Os módulos com dados usam JDBC fora da EDT e descartam resultados de sessões antigas. Ao sair, campos, tabelas, seleção e caches são limpos.

Modelos e assinaturas: [Contratos de dados](../../../../../../../../docs/CONTAS-E-DADOS.md). Não foi necessária alteração no esquema SQL.

## Validação

Na raiz do projeto, execute `.\mvnw.cmd test` e `python scripts/validar.py`. O segundo comando exige MySQL do XAMPP e sessão gráfica; usa bancos temporários. Teste também PT/EN, duas contas e saída/entrada. Não inclua `target/`, credenciais ou bibliotecas baixadas na entrega.
