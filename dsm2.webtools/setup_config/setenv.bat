rem Setting Java Home - Nicky Sandhu 04/01/2017
set "JAVA_HOME=%~dp0%..\..\jdk1.8.0_112"
set "DSM2_NATIVE_LIBPATH=%~dp0%..\..\native-libraries"
set "CATALINA_OPTS=-Djava.library.path=%~dp0;%DSM2_NATIVE_LIBPATH%\HDF5;%DSM2_NATIVE_LIBPATH%\HEC-DSSVue\lib"
