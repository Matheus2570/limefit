# Trabalho em grupo — integração final

Os cinco módulos estão integrados em `src/main/java/br/com/limefit/modulos/`:

| Responsável | Módulo | Entrega |
|---|---|---|
| Matheus de Carvalho | Home, autenticação e integração | Navegação, sessão, PT/EN e validação |
| Matheus Maiolo | Perfil | Cadastro, consulta, atualização, exclusão e IMC |
| Lucas | Alimentos | Busca e CRUD com nutrientes por 100 g |
| Guilherme | Refeições | CRUD por data e porções dos alimentos |
| Gustavo | Planejamento | Refeições e totais nutricionais por data |
| Francisco | Projeto | Apresentação, equipe e textos em PT/EN |

## Manutenção

Preserve pacotes, modelos e assinaturas descritos em [CONTAS-E-DADOS](CONTAS-E-DADOS.md). Os guias locais de cada módulo documentam o estado implementado. Alterações futuras devem ser feitas em branches e verificadas no projeto completo.

A base compartilhada fornece `ContextoAplicacao`, `PainelModulo` e `Visual`. As telas são painéis Swing na janela principal. Todas as operações JDBC ficam fora da EDT. `PainelModulo.executarNaConta` vincula o trabalho à sessão que o agendou e descarta resultados antigos. `limparDados()` esvazia campos, tabelas e caches ao sair ou trocar de conta.

## Fluxo entre módulos

Perfil cria o registro necessário para Refeições. Alimentos fornece o catálogo privado de nutrientes por 100 g. Refeições associa porções em gramas a uma data e Planejamento calcula os totais sem arredondamentos intermediários. Excluir perfil/refeição remove dependentes por cascata; excluir alimento em uso é recusado pelo banco.

Não há migração adicional nesta integração. O banco já migrado é compatível. Para instalação nova, use somente `sql/limefit.sql`.

## Verificar uma alteração

```powershell
.\mvnw.cmd test
python scripts/validar.py
```

A validação completa usa bancos temporários e uma janela real. Não inclua `target/`, credenciais ou dependências baixadas na entrega. As evidências são descritas em [ENTREGA](ENTREGA.md).
