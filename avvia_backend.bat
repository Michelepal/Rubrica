@echo off
cd /d "%~dp0backend"
call gradlew.bat bootRun --args="--spring.profiles.active=dev" > backend-local.log 2>&1
