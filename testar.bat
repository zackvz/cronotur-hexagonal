@echo off
rem Compila e executa os testes do aeroporto.
setlocal
cd /d "%~dp0"
set "JBIN="
if defined JAVA_HOME set "JBIN=%JAVA_HOME%\bin\"

"%JBIN%javac" -encoding UTF-8 -d build\classes -sourcepath src\main\java src\main\java\br\com\aeroporto\configuracao\AeroportoApp.java
if errorlevel 1 exit /b 1

"%JBIN%javac" -encoding UTF-8 -cp build\classes -d build\test-classes src\test\java\br\com\aeroporto\TestesAeroporto.java
if errorlevel 1 exit /b 1

"%JBIN%java" -cp build\classes;build\test-classes;src\main\resources br.com.aeroporto.TestesAeroporto
