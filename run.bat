@echo off
setlocal
echo ===================================================
echo   Compiling Banking Management System...
echo ===================================================

if not exist "bin" mkdir bin

REM Prefer JDK 21 if installed in standard location
set "JAVAC_EXE=C:\Program Files\Java\jdk-21.0.10\bin\javac.exe"
set "JAVA_EXE=C:\Program Files\Java\jdk-21.0.10\bin\java.exe"

if not exist "%JAVAC_EXE%" set "JAVAC_EXE=javac"
if not exist "%JAVA_EXE%" set "JAVA_EXE=java"

"%JAVAC_EXE%" -cp "lib/*" -d bin src/com/bank/model/*.java src/com/bank/exception/*.java src/com/bank/util/*.java src/com/bank/dao/*.java src/com/bank/service/*.java src/com/bank/Main.java

if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Compilation failed!
    pause
    exit /b %ERRORLEVEL%
)

echo ===================================================
echo   Starting Banking Management System...
echo ===================================================
"%JAVA_EXE%" -cp "bin;lib/*" com.bank.Main
pause
