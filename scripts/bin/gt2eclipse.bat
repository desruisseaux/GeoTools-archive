@echo off

REM Lets find out where we are
set BASE_DIR=%~dp0\..
"%JAVA_HOME%\bin\java.exe" -cp %BASE_DIR%\target\classes GT2Eclipse %1 %2 %3 %4 %5