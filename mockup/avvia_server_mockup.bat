@echo off
setlocal

set "MOCKUP_DIR=%~dp0"
set "CODEX_NODE=%LOCALAPPDATA%\OpenAI\Codex\bin\node.exe"

where node >nul 2>nul
if %ERRORLEVEL% EQU 0 (
  cd /d "%MOCKUP_DIR%"
  node server.js
  goto :eof
)

if exist "%CODEX_NODE%" (
  cd /d "%MOCKUP_DIR%"
  "%CODEX_NODE%" server.js
  goto :eof
)

echo Node.js non trovato.
echo Puoi comunque aprire il mockup senza server facendo doppio click su:
echo "%MOCKUP_DIR%index.html"
pause

