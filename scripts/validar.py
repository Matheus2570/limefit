"""Valida Swing/JDBC e os scripts SQL em bancos temporários, sem modificar limefit.
Executar na raiz: python scripts/validar.py. Requer Python 3, JDK e MySQL do XAMPP.
A sessão gráfica é necessária para DesktopIntegracaoTest.
"""
from pathlib import Path
import os
import shutil
import subprocess
import time

ROOT = Path(__file__).resolve().parent.parent
MYSQL = os.environ.get("LIMEFIT_MYSQL", shutil.which("mysql") or r"C:\xampp\mysql\bin\mysql.exe")
AMBIENTE = dict(os.environ)
AMBIENTE["MYSQL_PWD"] = os.environ.get("LIMEFIT_DB_PASSWORD", "")
PREFIXO = "limefit_validacao_"


def sql(texto):
    resultado = subprocess.run(
        [MYSQL, "--host=127.0.0.1", "--port=" + os.environ.get("LIMEFIT_DB_PORT", "3306"),
         "--user=" + os.environ.get("LIMEFIT_DB_USER", "root"), "--default-character-set=utf8mb4",
         "--batch", "--skip-column-names"],
        input=texto, encoding="utf-8", env=AMBIENTE, check=True, capture_output=True,
    )
    return resultado.stdout.strip()


def importar(arquivo, banco):
    texto = (ROOT / arquivo).read_text(encoding="utf-8")
    texto = texto.replace("USE limefit;", f"USE {banco};")
    texto = texto.replace("CREATE DATABASE IF NOT EXISTS limefit", f"CREATE DATABASE IF NOT EXISTS {banco}")
    sql(texto)


def validar_legado(banco):
    importar("sql/legado/limefit-original.sql", banco)
    importar("sql/legado/dados-teste.sql", banco)
    importar("sql/001-contas-e-propriedade.sql", banco)
    # Insere conta com hash conhecido e registra as tabelas antes de repetir a migração.
    sql(f"USE {banco}; INSERT INTO usuarios(nome,email,senha_hash) VALUES('Legado','legado@example.test',"
        "'pbkdf2-sha256$100000$AAECAwQFBgcICQoLDA0ODw==$hefbQGknRGCYpczS/rUDtMdfHIDRRPxeKpPZQbBEzhs=');")
    consulta = f"USE {banco}; CHECKSUM TABLE usuarios,perfis,alimentos,refeicoes,itens_refeicao;"
    antes = sql(consulta)
    importar("sql/001-contas-e-propriedade.sql", banco)
    assert antes == sql(consulta), "A repetição da migração alterou dados!"
    assert sql(f"SELECT COUNT(*) FROM {banco}.perfis WHERE usuario_id IS NULL;") == "1"
    assert sql(f"SELECT COUNT(*) FROM {banco}.alimentos WHERE usuario_id IS NULL;") == "2"
    assert sql(f"SELECT COUNT(*) FROM {banco}.refeicoes WHERE usuario_id IS NULL;") == "1"
    assert sql(f"SELECT COUNT(*) FROM {banco}.itens_refeicao WHERE usuario_id IS NULL;") == "2"
    print("PASS: migração repetível preservou contas, hashes e todos os registros legados.", flush=True)


def main():
    criados = []
    try:
        legado = PREFIXO + str(time.time_ns())
        criados.append(legado)
        sql(f"CREATE DATABASE {legado} CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;")
        validar_legado(legado)
        banco = PREFIXO + str(time.time_ns())
        criados.append(banco)
        importar("sql/limefit.sql", banco)
        importar("sql/limefit.sql", banco)
        assert sql(f"SELECT COUNT(*) FROM information_schema.TABLES WHERE TABLE_SCHEMA='{banco}';") == "5"
        assert sql(f"SELECT COUNT(*) FROM information_schema.TRIGGERS WHERE TRIGGER_SCHEMA='{banco}';") == "8"
        print("PASS: SQL completo cria cinco tabelas e oito gatilhos em instalação nova.", flush=True)
        ambiente_teste = dict(os.environ, LIMEFIT_DB_NAME=banco, LIMEFIT_TEST_DB=banco)
        comando = [str(ROOT / ("mvnw.cmd" if os.name == "nt" else "mvnw")), "test"]
        subprocess.run(comando, cwd=ROOT, env=ambiente_teste, check=True)
    finally:
        for banco in criados:
            # Somente nomes gerados neste processo; jamais usa LIMEFIT_DB_NAME para apagar.
            assert banco.startswith(PREFIXO) and banco.removeprefix(PREFIXO).isdigit()
            sql(f"DROP DATABASE IF EXISTS {banco};")
        print("Bancos temporários removidos.", flush=True)


if __name__ == "__main__":
    main()
