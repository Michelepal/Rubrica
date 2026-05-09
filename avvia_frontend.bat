@echo off
set "PATH=%~dp0.tools\node-v22.12.0-win-x64;%PATH%"
cd /d "%~dp0frontend"
call "%~dp0.tools\node-v22.12.0-win-x64\npm.cmd" start -- --host localhost --port 4200 > frontend-local.log 2>&1
