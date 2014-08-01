@echo off

:: Generates the Cytoscape.vmoptions file based on whether
:: we're dealing with a 32 bit or 64 bit JVM.

:: Create the .cytoscape directory if it doesn't already exist
;if exist "%HOMEPATH%\.cytoscape" goto dot_cytoscape_exists
;mkdir "%HOMEPATH%\.cytoscape"
;:dot_cytoscape_exists

set physmem=768
set mem=768
if exist findmem.out del findmem.out
systeminfo | find "Total Physical Memory" > findmem.out
if %ERRORLEVEL% NEQ 0 GOTO setVmoptions
for /f "tokens=4" %%i in (findmem.out) do set physmem=%%i
set physmem=%physmem:,=%

if %physmem% GTR 1536 set mem=1024
if %physmem% GTR 2048 set mem=1536
if %physmem% GTR 3072 set /a mem=%physmem%-1024
REM if %physmem% GTR 4096 set mem=3072
REM if %physmem% GTR 9216 set mem=4096

:setVmoptions
	REM echo "%mem% MB"
        echo -Xmx%mem%M  >Cytoscape.vmoptions
	goto End

:End
	del findstr.out
	del findmem.out
