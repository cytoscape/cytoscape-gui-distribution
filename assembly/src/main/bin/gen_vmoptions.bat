@echo off

:: Generates the Cytoscape.vmoptions file

set physmem=768
set minmem=768
set maxmem=768
if exist findmem.out del findmem.out
systeminfo | find "Total Physical Memory" > findmem.out
if %ERRORLEVEL% NEQ 0 GOTO Javatest
for /f "tokens=4" %%i in (findmem.out) do set physmem=%%i
set physmem=%physmem:,=%

:Javatest
	if exist findstr.out del findstr.out
	java -version 2>&1 | findstr /I 64-Bit > findstr.out
	if %ERRORLEVEL% EQU 0 GOTO 64bit
	java -version 2>&1 | findstr /i Java > findstr.out
	IF %ERRORLEVEL% EQU 0 GOTO 32bit
	goto Nojava

:64bit
	if %physmem% GTR 1535 set maxmem=1024
	if %physmem% GTR 2047 set maxmem=1536
	if %physmem% GTR 3071 (
		set /a minmem=2048
		set /a maxmem=%physmem%-1024
	) else (
		set /a minmem=%maxmem%
	)
	goto setVmoptions
 	
:32bit
	if %physmem% GTR 1535 set maxmem=1024
	if %physmem% GTR 2047 set maxmem=1200
	set /a minmem=%maxmem%
	goto setVmoptions

:setVmoptions
	echo -Xms%minmem%M>Cytoscape.vmoptions
	echo -Xmx%maxmem%M>>Cytoscape.vmoptions
	echo -Xss5M>>Cytoscape.vmoptions
	goto End

:Nojava
	echo ERROR: Can't find java executable

:End
	del findstr.out
	del findmem.out