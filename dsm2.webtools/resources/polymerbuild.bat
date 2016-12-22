:: command to create a small deployable unit with just two files produce with .prod suffix
:: This is failing!!! -> polybuild qual_animator.html --maximum-crush --suffix prod 
set APP_INSTALL_ROOT=z:\dsm2.webtools.server
set APP_WEBAPP=%APP_INSTALL_ROOT%\wtpwebapps\dsm2.webtools
vulcanize --inline-css --inline-scripts --strip-comments dsm2-grid-animator.html | crisper --html %APP_WEBAPP%\dsm2-grid-animator.html --js %APP_WEBAPP%\dsm2-grid-animator.js