# Módulo perfil — Matheus Maiolo

## Implementado

Cadastro e atualização do único perfil da conta, exclusão com confirmação e cálculo de IMC. Peso entre 25 e 300 kg; altura entre 100 e 230 cm; até duas casas decimais. O identificador técnico de 64 caracteres é preservado em atualizações. Excluir perfil remove refeições e itens por cascata, preservando alimentos.

## Integração

Tela Swing baseada em `PainelModulo` e `Visual`, conectada à janela pelo `ContextoAplicacao`. Textos, validações e mensagens estão em PT/EN. Os módulos com dados usam JDBC fora da EDT e descartam resultados de sessões antigas. Ao sair, campos, tabelas, seleção e caches são limpos.

Modelos e assinaturas: [Contratos de dados](../../../../../../../../docs/CONTAS-E-DADOS.md). Não foi necessária alteração no esquema SQL.

## Validação

Na raiz do projeto, execute `.\mvnw.cmd test` e `python scripts/validar.py`. O segundo comando exige MySQL do XAMPP e sessão gráfica; usa bancos temporários. Teste também PT/EN, duas contas e saída/entrada. Não inclua `target/`, credenciais ou bibliotecas baixadas na entrega.
