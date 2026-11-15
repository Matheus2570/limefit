# Trabalho em grupo no GitHub

A divisão por integrante, rota, classe Java e branch está no [README](../README.md). As interfaces agora são escritas em Java com j2html. Não existem templates em `resources/views`.

## Publicar a base

Na pasta do `pom.xml`, com um repositório vazio criado no GitHub:

```powershell
git init -b main
git add .
git commit -m "Implementa Home e autenticação Java do LimeFit"
git remote add origin https://github.com/SEU-USUARIO/limefit.git
git push -u origin main
```

Adicione os colegas em Settings → Collaborators. Cada integrante aceita o convite antes de clonar.

## Desenvolver um módulo

Exemplo para Alimentos:

```powershell
git clone https://github.com/SEU-USUARIO/limefit.git
cd limefit
git switch -c feat/alimentos
```

Edite `src/main/java/br/com/limefit/modulos/alimentos/AlimentosPagina.java`. Implemente a interface em `conteudo` usando j2html, e crie modelo e DAO nessa mesma pasta. Faça ambos os idiomas com `idioma.escolher`. Os outros integrantes seguem o mesmo padrão no próprio módulo.

Antes de escrever SQL, leia [Contas e dados](CONTAS-E-DADOS.md). O dono vem de `usuarioId(pedido)`. Alimentos também são privados. Toda consulta, alteração e exclusão deve filtrar a conta; referências de perfil, refeição e alimento precisam ter o mesmo dono.

```powershell
.\mvnw.cmd compile exec:java
```

Teste a página autenticado com duas contas, incluindo tentativa de usar um ID de registro da outra conta. Para verificar a base comum, execute `python scripts/validar.py` com o MySQL do XAMPP ligado.

Depois de testar:

```powershell
git status
git add src/main/java/br/com/limefit/modulos/alimentos
git commit -m "Implementa alimentos por conta"
git push -u origin HEAD
```

Inclua também CSS ou imagens próprios do módulo se houver. Abra um Pull Request para `main`. Para atualizar sua branch, faça commit do trabalho atual, rode `git fetch origin` e depois `git merge origin/main`.

`web`, `autenticacao`, `banco`, `Aplicacao.java`, `pom.xml`, CSS comum e SQL são compartilhados. Coordenem mudanças nesses arquivos para facilitar a integração. Para novas mudanças de banco, adicionem outro SQL incremental, preservando os registros existentes.

## Integração

Perfil cria o perfil da conta, Alimentos cadastra os alimentos daquela conta, Refeições usa apenas perfil e alimentos do mesmo dono e Planejamento soma apenas as refeições desse dono por data. O pitch do módulo Projeto pode seguir gancho, solução, benefício e convite final em inglês.

As páginas desta base ficam vazias de propósito, com menu e título. A demonstração integrada dos módulos será possível depois das entregas das respectivas branches.
