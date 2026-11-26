"""Atalho de compatibilidade. A carga agora é feita pelo Java do projeto.

Sem Python: Windows .\\preparar-demo.cmd; Linux/macOS sh ./preparar-demo.sh.
"""
from datetime import date
from pathlib import Path
import argparse
import os
import subprocess
import sys


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--data", type=date.fromisoformat, help="Data central AAAA-MM-DD (padrão: hoje).")
    args = parser.parse_args()
    root = Path(__file__).resolve().parent.parent
    comando = [str(root / "mvnw.cmd")] if os.name == "nt" else ["sh", str(root / "mvnw")]
    comando += ["-Dexec.mainClass=br.com.limefit.demonstracao.PopularDemo"]
    if args.data:
        comando += ["-Dexec.args=" + args.data.isoformat()]
    comando += ["compile", "exec:java"]
    try:
        return subprocess.run(comando, cwd=root).returncode
    except OSError as erro:
        print("Não foi possível executar o Maven Wrapper: " + str(erro), file=sys.stderr)
        return 1


if __name__ == "__main__":
    sys.exit(main())
