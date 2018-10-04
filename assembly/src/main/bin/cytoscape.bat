@echo off

:: First, see if help (-h, --help) or version (-v, --version) command line arguments
:: are specified. If so, display help or the current version and exit.

:: Note that we're observing a special CY_DEBUG_START environment variable.
:: If it's set, we dump startup information. Feel free to add startup
:: information as appropriate.

set CYTOSCAPE_VERSION=Cytoscape version: 3.7.0-RC1

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
echo  -P,--props ^<file^>     Load cytoscape properties file (Java properties
echo                        format) or individual property: -P name=value.
echo  -V,--vizmap ^<file^>    Load vizmap properties file (Cytoscape VizMap
echo                        format).
echo  -S,--script ^<file^>    Execute commands from script file.
echo  -R,--rest ^<port^>      Start a rest service.
echo.

GOTO END_BATCH
:skipHelp

set checkVersion=false
IF "%1"=="-v" set checkVersion=true
IF "%1"=="--version" set checkVersion=true

IF "%checkVersion%"=="false" GOTO skipCheckVersion 	
echo %CYTOSCAPE_VERSION%

GOTO END_BATCH
:skipCheckVersion

setlocal ENABLEEXTENSIONS
set KARAF_TITLE=Cytoscape
set DEBUG_PORT=12345

:: Create the Cytoscape.vmoptions file, if it doesn't exist.
IF EXIST "Cytoscape.vmoptions" GOTO vmoptionsFileExists
IF EXIST "gen_vmoptions.bat" CMD /C gen_vmoptions.bat
:vmoptionsFileExists


IF EXIST "Cytoscape.vmoptions" GOTO itIsThere
:: Run with defaults:
echo "*** Missing Cytoscape.vmoptions, falling back to using defaults!"
set JAVA_OPTS=-Xms1250M -Xmx1250M
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
set KARAF_OPTS=-Dcom.sun.management.jmxremote -Dcytoscape.home="%PWD:~0,-1%"

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

:: Strategy: First try finding a commonly installed JRE. If that fails
:: find a system JRE through the registry.

:: The range of allowable VMs will be MinJVM <= VM < MaxJVM
:: Note that simple lexigraphic comparisons *WON'T WORK* with JVMs > 9
set MinJVM=1.8.0_162
set MinRegistryJVM=1.8
set MaxJVM=1.9
set TestPath=%CommonProgramFiles%\i4j_jres

if exist "%TestPath%" goto tryCommonJRE
echo Did not find common files directory %TestPath%
goto tryRegistry

:tryCommonJRE
if defined CY_DEBUG_START (
  echo Looking for common JVMs in %TestPath%
)

:: List the directory in descending order, which will result is our
:: picking the most recent JVM in the allowable range. We skip JVMs 
:: outside of the allowable range.
for /F %%i in ('dir /B/ON "%TestPath%"') do (
  if "%%i" GEQ "%MinJVM%" (
    if "%%i" LSS "%MaxJVM%" (
      set JAVA_HOME=%TestPath%\%%i

      if defined CY_DEBUG_START (
        echo Found common JVM at %TestPath%\%%i
      )
      goto END
    )
  )
)

:tryRegistry
if defined CY_DEBUG_START (
  echo Looking for system JVMs in registry
)

:: No JVM found in CommonFiles ... try going through the registry.
:: The registry lists the "CurrentVersion" key for the JRE as a platform
:: identifier (e.g., 1.8) and not a specific JVM version (e.g., 1.8.0_162).
:: It then has a subkey that matches the "CurrentVersion", and that subkey
:: has a path value. So, our drill is to get the CurrentVersion, then 
:: get the path from the corresponding subkey. There could be other
:: subkeys identifying specific (inactive) JVM versions ... we don't 
:: care about those.
::
:: Note that it's possible that the JVM found here will be out of our
:: useful range (e.g., the platform identifier is 1.9, while we
:: must have 1.8) ... we'll ignore the current version if that's the case.
::
set CurrentVersion=
set JAVA_HOME=

for /F "usebackq skip=2 tokens=3" %%A in (`reg query "HKLM\SOFTWARE\JavaSoft\Java Runtime Environment" /v "CurrentVersion" 2^>nul`) do (
    set CurrentVersion=%%A
)
if defined CurrentVersion (
    for /F "usebackq skip=2 tokens=3*" %%A in (`reg query "HKLM\SOFTWARE\JavaSoft\Java Runtime Environment\%CurrentVersion%" /v JavaHome 2^>nul`) DO (
        set JAVA_HOME=%%A %%B

        if defined CY_DEBUG_START (
          echo Found system JVM %CurrentVersion% with path %%A %%B
        )

        goto TryJDKEnd
    )
)
if defined CY_DEBUG_START (
  echo No system JVM defined in registry
)


:TryJDKEnd
if not exist "%JAVA_HOME%" (
    call :warn JAVA_HOME is not valid: "%JAVA_HOME%"
    goto END
)

set JAVA=%JAVA_HOME%\bin\java
:END

if defined CY_DEBUG_START (
  echo Using JVM found at %JAVA_HOME%
  pause
)

:: This is probably wrong.  We don't really want the user to be in this directory, do we?
framework/bin/karaf %1 %2 %3 %4 %5 %6 %7 %8

:END_BATCH
