@echo off
rem Compila e executa o aeroporto. Exemplo: rodar.bat --http --arquivo
setlocal
cd /d "%~dp0"
set "JBIN="
if defined JAVA_HOME set "JBIN=%JAVA_HOME%\bin\"

"%JBIN%javac" -encoding UTF-8 -d build\classes -sourcepath src\main\java src\main\java\br\com\aeroporto\configuracao\AeroportoApp.java
if errorlevel 1 exit /b 1

"%JBIN%java" -cp build\classes;src\main\resources br.com.aeroporto.configuracao.AeroportoApp %*
