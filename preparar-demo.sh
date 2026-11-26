#!/bin/sh
set -eu
LIMEFIT_PROJECT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
cd "$LIMEFIT_PROJECT_DIR"
exec sh ./mvnw '-Dexec.mainClass=br.com.limefit.demonstracao.PopularDemo' compile exec:java
