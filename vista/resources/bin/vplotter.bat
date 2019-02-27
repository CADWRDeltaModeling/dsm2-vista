@echo off
rem ##############################
rem Batch file for running vplotter. copied over from vscript.bat
rem ##############################
set vista_home=%~dp0/..

if exist "%vista_home%/jython/jython.jar" goto :valid


:notfound

echo ############################################################
echo   Error: VISTA files not found
echo   ___
echo   Installation instructions
echo   ___
echo   The value of the environment variable vista_home in the 
echo   file vscript.bat needs to match the location where
echo   VISTA has been installed
echo ############################################################
PAUSE
goto :end

:valid

setlocal
rem ###############
rem Set path to location of dll
rem ###############

set PATH=%path%;%vista_home%/bin;%vista_home%/lib;

set PYPATH="%vista_home%/jython/Lib;%vista_home%/lib/Lib;%vista_home%/../scripts"

set CPATH="%vista_home%/lib/vista.jar;%vista_home%/lib/vista-help.jar;%vista_home%/jython/jython.jar;%vista_home%/lib/pd.jar;%vista_home%/lib/misc.jar;%vista_home%/lib/jhall.jar;%vista_home%/lib/jnios.jar;%vista_home%/lib/widgets.jar;%vista_home%/lib/jhdf5.jar;%vista_home%/lib/jhdf5obj.jar;%vista_home%/lib/jhdfobj.jar;%vista_home%/lib/heclib.jar;%vista_home%/lib/dsm2-input-model.jar;%vista_home%/lib/ojdbc6.jar"

set CPATH="%vista_home%/lib/vista.jar;%vista_home%/lib/vista-help.jar;%vista_home%/jython/jython.jar;%vista_home%/lib/pd.jar;%vista_home%/lib/misc.jar;%vista_home%/lib/jhall.jar;%vista_home%/lib/jnios.jar;%vista_home%/lib/widgets.jar;%vista_home%/lib/jhdf5.jar;%vista_home%/lib/jhdf5obj.jar;%vista_home%/lib/jhdfobj.jar;%vista_home%/lib/dsm2-input-model.jar;%vista_home%/lib/ojdbc6.jar;%vista_home%/lib/slf4j-api-1.7.2.jar;%vista_home%/lib/logback-core-1.0.9.jar;%vista_home%/lib/logback-classic-1.0.9.jar"

set HECPATH=%vista_home%/lib/heclib.jar;%vista_home%/lib/hec.jar;%vista_home%/lib/hecData.jar;%vista_home%/lib/rma.jar;%vista_home%/lib/dssvueHelp.jar;%vista_home%/lib/images.jar;

set CPATH=%CPATH%;%HECPATH%

set LPATH="%vista_home%/lib" 

set PYHOME="%vista_home%/jython"

set ARGS=

:loop
if [%1] == [] goto endloop
        set ARGS=%ARGS% %1
        shift
        goto loop
:endloop

rem ###############
rem starting vplotter
rem ###############

"%vista_home%/jre8/bin/java" -mx256m  -Djava.library.path=%LPATH% -Dvista.home="%vista_home%" -Dpython.home=%PYHOME% -Dpython.path=%PYPATH% -Dpython.console=org.python.util.InteractiveConsole -classpath %CPATH% org.python.util.jython "%vista_home%/bin/vplotter.py" -f "%1%"
endlocal
:end 



