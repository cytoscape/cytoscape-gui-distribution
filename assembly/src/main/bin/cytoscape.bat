@echo off

:: First, see if help (-h, --help) or version (-v, --version) command line arguments
:: are specified. If so, display help or the current version and exit.
:: Note: Current version to be implemented after 3.1

set CYTOSCAPE_VERSION="Cytoscape version: 3.1.0-SNAPSHOT"

set help=false
IF "%1"=="-h" set help=true
IF "%1"=="--help" set help=true

IF "%help%"=="false" GOTO skipHelp 	
echo.	
echo Cytoscape Command-line Arguments
echo ================================
echo usage: cytoscape.bat [OPTIONS]
echo  -h,--help             Print this message.
echo  -v,--version          Print the version number.
echo  -s,--session ^<file^>   Load a cytoscape session (.cys) file.
echo  -N,--network ^<file^>   Load a network file (any format).
echo  -T,--table ^<file^>     Load a data table file (any table format).
echo  -P,--props ^<file^>     Load cytoscape properties file (Java properties
echo                        format) or individual property: -P name=value.
echo  -V,--vizmap ^<file^>    Load vizmap properties file (Cytoscape VizMap
echo                        format).
echo  -S,--command ^<file^>   Execute commands from script file
echo  -R,--rest ^<port^>      Start a rest service
echo.

GOTO END_BATCH
:skipHelp

set checkVersion=false
IF "%1"=="-v" set checkVersion=true
IF "%1"=="--version" set checkVersion=true

IF "%help%"=="false" GOTO skipCheckVersion 	
echo.
echo "%CYTOSCAPE_VERSION%"
echo.

GOTO END_BATCH
:skipCheckVersion

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
set JAVA_OPTS=-Xmx1250M
GOTO setDebugOpts

:: We end up here if we have a Cytoscape.vmoptions file:
:itIsThere
setLocal EnableDelayedExpansion
set JAVA_OPTS=
for /f "tokens=* delims= " %%a in (Cytoscape.vmoptions) do (
set JAVA_OPTS=!JAVA_OPTS! %%a
)
set JAVA_OPTS=%JAVA_OPTS:~1%

:setDebugOpts
set JAVA_DEBUG_OPTS=-Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=%DEBUG_PORT%
set PWD=%~dp0
set KARAF_OPTS=-Xms128M -Dcom.sun.management.jmxremote -Dcytoscape.home="%PWD:\=\\%" -Duser.dir="%PWD:\=\\%" -splash:CytoscapeSplashScreen.png

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

:: This is probably wrong.  We don't really want the user to be in this directory, do we?
framework/bin/karaf %1 %2 %3 %4 %5 %6 %7 %8

:END_BATCH