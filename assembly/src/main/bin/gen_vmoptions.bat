@echo off

:: Generates the Cytoscape.vmoptions file

:: Create the .cytoscape directory if it doesn't already exist
;if exist "%HOMEPATH%\.cytoscape" goto dot_cytoscape_exists
;mkdir "%HOMEPATH%\.cytoscape"
;:dot_cytoscape_exists

set physmem=768
set minmem=768
set maxmem=768
if exist findmem.out del findmem.out
systeminfo | find "Total Physical Memory" > findmem.out
if %ERRORLEVEL% NEQ 0 GOTO setVmoptions
for /f "tokens=4" %%i in (findmem.out) do set physmem=%%i
set physmem=%physmem:,=%

if %physmem% GTR 1535 set maxmem=1024
if %physmem% GTR 2047 set maxmem=1536
if %physmem% GTR 3071 (
	set /a minmem=2048
	set /a maxmem=%physmem%-1024
) else (
	set /a minmem=%maxmem%
)

:setVmoptions
		echo -Xms%minmem%M>Cytoscape.vmoptions
		echo -Xmx%maxmem%M>>Cytoscape.vmoptions
	goto End

:End
	del findmem.out
