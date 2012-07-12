@echo off

set KARAF_TITLE=Cytoscape
set DEBUG_PORT=12345

:: Create the Cytoscape.vmoptions file, if it doesn't exist.
IF EXIST "Cytoscape.vmoptions" GOTO vmoptionsFileExists
CALL gen_vmoptions.bat
:vmoptionsFileExists


IF EXIST "Cytoscape.vmoptions" GOTO itIsThere
; Run with defaults:
echo "*** Missing Cytoscape.vmoptions, falling back to using defaults!"
set JAVA_MAX_MEM=1550M
GOTO setDebugOpts

:: We end up here if we have a Cytoscape.vmoptions file:
:itIsThere
:: Read max memory
setLocal EnableDelayedExpansion
for /f "tokens=* delims= " %%a in (Cytoscape.vmoptions) do (
set /a N+=1
set opt!N!=%%a
)
set JAVA_MAX_MEM=!opt1!

:setDebugOpts
set JAVA_DEBUG_OPTS=-Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=%DEBUG_PORT%
set PWD=%~dp0
set KARAF_OPTS=-Xss10M -Dcytoscape.home="%PWD:\=\\%" -Duser.dir="%PWD:\=\\%" -splash:CytoscapeSplashScreen.png

if not "X%JAVA_HOME%"=="X" goto TryJDKEnd
goto :TryJRE

:warn
    echo %KARAF_TITLE%: %*
goto :EOF

:TryJRE
    reg export "HKEY_LOCAL_MACHINE\SOFTWARE\JavaSoft\Java Runtime Environment" __reg1.txt
    if not exist __reg1.txt goto :TryJDK
    type __reg1.txt | find "CurrentVersion" > __reg2.txt
    if errorlevel 1 goto :TryJDK
    for /f "tokens=2 delims==" %%x in (__reg2.txt) do set JavaTemp=%%~x
    if errorlevel 1 goto :TryJDK
    set JavaTemp=%JavaTemp%##
    set JavaTemp=%JavaTemp:                ##=##%
    set JavaTemp=%JavaTemp:        ##=##%
    set JavaTemp=%JavaTemp:    ##=##%
    set JavaTemp=%JavaTemp:  ##=##%
    set JavaTemp=%JavaTemp: ##=##%
    set JavaTemp=%JavaTemp:##=%
    del __reg1.txt
    del __reg2.txt
    reg export "HKEY_LOCAL_MACHINE\SOFTWARE\JavaSoft\Java Runtime Environment\%JavaTemp%" __reg1.txt
    if not exist __reg1.txt goto :TryJDK
    type __reg1.txt | find "JavaHome" > __reg2.txt
    if errorlevel 1 goto :TryJDK
    for /f "tokens=2 delims==" %%x in (__reg2.txt) do set JAVA_HOME=%%~x
    if errorlevel 1 goto :TryJDK
    del __reg1.txt
    del __reg2.txt
    goto TryJDKEnd
:TryJDK
    reg export "HKEY_LOCAL_MACHINE\SOFTWARE\JavaSoft\Java Development Kit" __reg1.txt
    if not exist __reg1.txt (
        call :warn Unable to retrieve JAVA_HOME
        goto END
    )
    type __reg1.txt | find "CurrentVersion" > __reg2.txt
    if errorlevel 1 (
        call :warn Unable to retrieve JAVA_HOME
        goto END
    )
    for /f "tokens=2 delims==" %%x in (__reg2.txt) do set JavaTemp=%%~x
    if errorlevel 1 (
        call :warn Unable to retrieve JAVA_HOME
        goto END
    )
    set JavaTemp=%JavaTemp%##
    set JavaTemp=%JavaTemp:                ##=##%
    set JavaTemp=%JavaTemp:        ##=##%
    set JavaTemp=%JavaTemp:    ##=##%
    set JavaTemp=%JavaTemp:  ##=##%
    set JavaTemp=%JavaTemp: ##=##%
    set JavaTemp=%JavaTemp:##=%
    del __reg1.txt
    del __reg2.txt
    reg export "HKEY_LOCAL_MACHINE\SOFTWARE\JavaSoft\Java Development Kit\%JavaTemp%" __reg1.txt
    if not exist __reg1.txt (
        call :warn Unable to retrieve JAVA_HOME from JDK
        goto END
    )
    type __reg1.txt | find "JavaHome" > __reg2.txt
    if errorlevel 1 (
        call :warn Unable to retrieve JAVA_HOME
        goto END
    )
    for /f "tokens=2 delims==" %%x in (__reg2.txt) do set JAVA_HOME=%%~x
    if errorlevel 1 (
        call :warn Unable to retrieve JAVA_HOME
        goto END
    )
    del __reg1.txt
    del __reg2.txt
:TryJDKEnd
    if not exist "%JAVA_HOME%" (
        call :warn JAVA_HOME is not valid: "%JAVA_HOME%"
        goto END
    )
    set JAVA=%JAVA_HOME%\bin\java
:END

:: This is probably wrong.  We don't really want the user to be in this directory, do we?
framework/bin/karaf %1 %2 %3 %4 %5 %6 %7 %8
