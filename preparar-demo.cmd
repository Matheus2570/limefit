@echo off
setlocal
pushd "%~dp0"
if errorlevel 1 exit /b 1
call "%~dp0mvnw.cmd" "-Dexec.mainClass=br.com.limefit.demonstracao.PopularDemo" compile exec:java
set "LIMEFIT_DEMO_EXIT=%ERRORLEVEL%"
popd
exit /b %LIMEFIT_DEMO_EXIT%
