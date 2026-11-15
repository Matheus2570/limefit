"""Valida autenticação e isolamento em um banco temporário do MariaDB local.

Executar na raiz: python scripts/validar.py
Requer MySQL do XAMPP (ou LIMEFIT_MYSQL) e as mesmas variáveis JDBC da aplicação.
Não importa SQL no banco pessoal e não encerra o servidor que estiver na porta 8080.
"""
from pathlib import Path
import os
import shutil
import subprocess
import time

ROOT = Path(__file__).resolve().parent.parent
MYSQL = os.environ.get("LIMEFIT_MYSQL", shutil.which("mysql") or r"C:\xampp\mysql\bin\mysql.exe")
BANCO = "limefit_validacao_" + str(time.time_ns())
AMBIENTE = dict(os.environ)
# MYSQL_PWD evita colocar a senha na lista de argumentos do processo.
AMBIENTE["MYSQL_PWD"] = os.environ.get("LIMEFIT_DB_PASSWORD", "")


def sql(texto):
    subprocess.run(
        [MYSQL, "--host=127.0.0.1", "--port=" + os.environ.get("LIMEFIT_DB_PORT", "3306"),
         "--user=" + os.environ.get("LIMEFIT_DB_USER", "root"), "--default-character-set=utf8mb4", "--batch"],
        input=texto, encoding="utf-8", env=AMBIENTE, check=True,
    )


def main():
    sql(f"CREATE DATABASE {BANCO} CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;")
    try:
        for arquivo in ["sql/limefit.sql", "sql/dados-teste.sql", "sql/001-contas-e-propriedade.sql",
                        "sql/001-contas-e-propriedade.sql"]:
            texto = (ROOT / arquivo).read_text(encoding="utf-8")
            texto = texto.replace("USE limefit;", f"USE {BANCO};")
            texto = texto.replace("CREATE DATABASE IF NOT EXISTS limefit", f"CREATE DATABASE IF NOT EXISTS {BANCO}")
            sql(texto)
        ambiente_teste = dict(os.environ, LIMEFIT_DB_NAME=BANCO, LIMEFIT_TEST_DB=BANCO)
        # O ambiente de automação pode ter user.home=C:\; usa o cache normal do usuário.
        repositorio = Path(os.environ.get("USERPROFILE", str(Path.home()))) / ".m2" / "repository"
        comando = [str(ROOT / "mvnw.cmd"), f"-Dmaven.repo.local={repositorio}", "test"] if os.name == "nt" else [str(ROOT / "mvnw"), "test"]
        subprocess.run(comando, cwd=ROOT, env=ambiente_teste, check=True)
    finally:
        # Nome gerado neste processo; nunca usa LIMEFIT_DB_NAME para apagar um banco.
        assert BANCO.startswith("limefit_validacao_") and BANCO.removeprefix("limefit_validacao_").isdigit()
        sql(f"DROP DATABASE {BANCO};")


if __name__ == "__main__":
    main()
