@echo off

REM Lets find out where we are
set BASE_DIR=%~dp0\..

REM Run the Header script
"%JAVA_HOME%\bin\java.exe" -cp %BASE_DIR%\target\classes script.Header %1 %2 %3 %4 %5