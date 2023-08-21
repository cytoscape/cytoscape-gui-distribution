@echo off

:: First, see if help (-h, --help) or version (-v, --version) command line arguments
:: are specified. If so, display help or the current version and exit.

:: Note that we're observing a special CY_DEBUG_START environment variable.
:: If it's set, we dump startup information. Feel free to add startup
:: information as appropriate.

set CYTOSCAPE_VERSION=Cytoscape version: 3.11.0-SNAPSHOT

set help=false
IF "%1"=="-h" set help=true
IF "%1"=="--help" set help=true

IF "%help%"=="false" goto skipHelp
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

goto END_BATCH
:skipHelp

if defined CY_DEBUG_START (
    echo.
    echo CY_DEBUG_START variable set, outputting debug information
    echo.
)

set checkVersion=false
IF "%1"=="-v" set checkVersion=true
IF "%1"=="--version" set checkVersion=true

IF "%checkVersion%"=="false" goto skipCheckVersion
echo %CYTOSCAPE_VERSION%

goto END_BATCH
:skipCheckVersion

setlocal ENABLEEXTENSIONS
setlocal EnableDelayedExpansion
set DEBUG_PORT=12345

:: Create the Cytoscape.vmoptions file, if it doesn't exist.
IF EXIST "Cytoscape.vmoptions" GOTO vmoptionsFileExists

:: This might fail if directory is NOT writable...
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
set JAVA_OPTS=%JAVA_OPTS% -Djdk.util.zip.disableZip64ExtraFieldValidation=true


:setDebugOpts
set JAVA_DEBUG_OPTS=-Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=%DEBUG_PORT%
set PWD=%~dp0
set KARAF_OPTS=-Dcom.sun.management.jmxremote -Dcytoscape.home="%PWD:~0,-1%"

set KARAF_DATA=%USERPROFILE%\CytoscapeConfiguration\3\karaf_data
if not exist "%KARAF_DATA%" (
    mkdir "%KARAF_DATA%\tmp"
)

if defined CY_DEBUG_START (
  echo Checking if JAVA_HOME is already set
)

:: If JAVA_HOME is set, assume all is well and use that to launch
:: Cytoscape
if not "X%JAVA_HOME%"=="X" goto TryJDKEnd

if defined CY_DEBUG_START (
  echo JAVA_HOME not set, seeing if Java installed under jre folder in Cytoscape install
)

:: Try finding JRE under Cytoscape installation directory.
set TestPath=%~dp0%jre

if exist "%TestPath%" (
    set JAVA_HOME=%TestPath%
    if defined CY_DEBUG_START (
        echo Setting JAVA_HOME to JRE installed with Cytoscape: %JAVA_HOME%
    )
    goto TryJDKEnd
)


:: Look for desired java in install4j shared installation directory
set DesiredJVM=17

set TestPath=%CommonProgramFiles%\i4j_jres

if not exist "%TestPath%" (
    if defined CY_DEBUG_START (
        echo Did not find common files directory %TestPath%
    )
    goto tryRegistry
)

if defined CY_DEBUG_START (
  echo Looking for common JVMs in %TestPath%
)

:: List the directory in descending order by date, which will result in our
:: picking the most recent JVM matching DesiredJVM. 
for /F %%i in ('dir /B/O-D "%TestPath%"') do (
  set tver=%%i
  set mver=!tver:~0,3!

  if "!mver!" == "%DesiredJVM%." (
      set JAVA_HOME=%TestPath%\%%i
      if defined CY_DEBUG_START (
          echo Found common JVM at %TestPath%\%%i
      )
      goto TryJDKEND
  ) else (
      if defined CY_DEBUG_START (
        echo %%i Major version !mver! does not match %DesiredJVM%.
      )
  )
)

:tryRegistry
if defined CY_DEBUG_START (
  echo Looking for system JVMs in registry
)

:: No JVM found under Cytoscape install or in CommonFiles ... 
:: try going through the registry.
:: For Java 17 Oracle, the registry lists the "CurrentVersion" as the 
:: specific version (e.g., 17.0.13)
:: The JavaHome subkey under the version has a path value.
:: So, our drill is to get the CurrentVersion, then 
:: get the path from JavaHome. There could be other
:: subkeys identifying specific (inactive) JVM versions ... we don't 
:: care about those.
::
set CurrentVersion=
set JAVA_HOME=

for /F "usebackq skip=2 tokens=3" %%A in (`reg query "HKLM\SOFTWARE\JavaSoft\JDK" /v "CurrentVersion" 2^>nul`) do (
   set tver=%%A
   set mver=!tver:~0,3!
   if "!mver!" == "%DesiredJVM%." ( 
       set CurrentVersion=%%A
   ) else (
       if defined CY_DEBUG_START (
           echo %%A Major version does not match %DesiredJVM%
       )
   )
)
if defined CurrentVersion (
    for /F "usebackq skip=2 tokens=3*" %%A in (`reg query "HKLM\SOFTWARE\JavaSoft\JDK\%CurrentVersion%" /v JavaHome 2^>nul`) DO (
        set JAVA_HOME=%%A %%B

        if defined CY_DEBUG_START (
          echo Found system Oracle JDK JVM %CurrentVersion% with path %%A %%B
        )

        goto TryJDKEnd
    )
)


:: Look for suitable Eclipse JDK JVM
:: By examining Eclipse Adoptium directory for folders starting with jdk-XX.
:: where XX must match %DesiredJVM% set above

set TestPath=%ProgramFiles%\Eclipse Adoptium

if not exist "%TestPath%" (
    if defined CY_DEBUG_START (
        echo Did not find Eclipse Java directory %TestPath%
    )
    goto TryJDKEnd
)

if defined CY_DEBUG_START (
  echo Looking for JVMs in %TestPath%
)

:: List the directory in descending order by date, which will result in our
:: picking the most recent JVM matching DesiredJVM.
for /F %%i in ('dir /B/O-D "%TestPath%"') do (
  set tver=%%i
  set mver=!tver:~0,7!

  if "!mver!" == "jdk-%DesiredJVM%." (
      set JAVA_HOME=%TestPath%\%%i
      if defined CY_DEBUG_START (
          echo Found Eclipse JVM at %TestPath%\%%i
      )
      goto TryJDKEND
  ) else (
      if defined CY_DEBUG_START (
        echo %%i Major version !mver! does not match jdk-%DesiredJVM%.
      )
  )
)

:: Look for suitable Microsoft JDK JVM
:: By examining Microsoft directory for folders starting with jdk-XX.
:: where XX must match %DesiredJVM% set above

set TestPath=%ProgramFiles%\Microsoft

if not exist "%TestPath%" (
    if defined CY_DEBUG_START (
        echo Did not find Micorsoft Java directory %TestPath%
    )
    goto TryJDKEnd
)

if defined CY_DEBUG_START (
  echo Looking for JVMs in %TestPath%
)

:: List the directory in descending order by date, which will result in our
:: picking the most recent JVM matching DesiredJVM.
for /F %%i in ('dir /B/O-D "%TestPath%"') do (
  set tver=%%i
  set mver=!tver:~0,7!

  if "!mver!" == "jdk-%DesiredJVM%." (
      set JAVA_HOME=%TestPath%\%%i
      if defined CY_DEBUG_START (
          echo Found Microsoft JVM at %TestPath%\%%i
      )
      goto TryJDKEND
  ) else (
      if defined CY_DEBUG_START (
        echo %%i Major version !mver! does not match jdk-%DesiredJVM%.
      )
  )
)


:TryJDKEnd

if not exist "%JAVA_HOME%" (
    echo.
    echo ERROR: unable to start Cytoscape on command line, JAVA_HOME is not valid: %JAVA_HOME%
    echo Please visit https://cytoscape.org in a browser for help
    echo.
    exit /b 1
)

:: only output if CY_DEBUG_START variable is defined
if defined CY_DEBUG_START (
  echo Using JVM found at %JAVA_HOME%
  pause
)

:: Work around Java 17.0.8 issu
set EXTRA_JAVA_OPTS="-Djdk.util.zip.disableZip64ExtraFieldValidation=true"

:: This is probably wrong.  We don't really want the user to be in this directory, do we?
framework/bin/karaf %1 %2 %3 %4 %5 %6 %7 %8
:END_BATCH
