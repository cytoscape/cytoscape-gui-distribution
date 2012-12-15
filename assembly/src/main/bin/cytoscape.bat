@echo off
setlocal ENABLEEXTENSIONS
set KARAF_TITLE=Cytoscape
set DEBUG_PORT=12345

:: Create the Cytoscape.vmoptions file, if it doesn't exist.
IF EXIST "Cytoscape.vmoptions" GOTO vmoptionsFileExists
CMD /C gen_vmoptions.bat
:vmoptionsFileExists


IF EXIST "Cytoscape.vmoptions" GOTO itIsThere
:: Run with defaults:
echo "*** Missing Cytoscape.vmoptions, falling back to using defaults!"
set JAVA_MAX_MEM=-Xmx800M
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
set KARAF_OPTS=-Xss10M %JAVA_MAX_MEM% -Dcytoscape.home="%PWD:\=\\%" -Duser.dir="%PWD:\=\\%" -splash:CytoscapeSplashScreen.png

set KARAF_DATA=%USERPROFILE%\CytoscapeConfiguration\3\karaf_data
if not exist "%KARAF_DATA%" (
    mkdir "%KARAF_DATA%\tmp"
)

if not "X%JAVA_HOME%"=="X" goto TryJDKEnd
goto :TryJRE

:warn
    echo %KARAF_TITLE%: %*
goto :EOF

:TryJRE
    for /F "usebackq skip=2 tokens=3" %%A in (`reg query "HKLM\SOFTWARE\JavaSoft\Java Runtime Environment" /v "CurrentVersion" 2^>nul`) do (
        set CurrentVersion=%%A
    )
    if defined CurrentVersion (
        for /F "usebackq skip=2 tokens=3*" %%A in (`reg query "HKLM\SOFTWARE\JavaSoft\Java Runtime Environment\%CurrentVersion%" /v JavaHome 2^>nul`) DO (
            set JAVA_HOME=%%A %%B
        )
    )

:TryJDKEnd
    if not exist "%JAVA_HOME%" (
        call :warn JAVA_HOME is not valid: "%JAVA_HOME%"
        goto END
    )
    set JAVA=%JAVA_HOME%\bin\java
:END

rem Karaf uses JAVA_MAX_MEM, so strip off the -Xmx and leave memory size
set JAVA_MAX_MEM=%JAVA_MAX_MEM:-Xmx=%


:: This is probably wrong.  We don't really want the user to be in this directory, do we?
framework/bin/karaf %1 %2 %3 %4 %5 %6 %7 %8
