echo Starting Polymer Build 
:: command to create a small deployable unit with just two files produce with .prod suffix
:: This is failing!!! -> polybuild qual_animator.html --maximum-crush --suffix prod 
set OUTDIR=../ant-build
echo Building animator html files
vulcanize --inline-css --inline-scripts --strip-comments dsm2-grid-animator.html | crisper --html %OUTDIR%\dsm2-grid-animator.html --js %OUTDIR%\dsm2-grid-animator.js
vulcanize --inline-css --inline-scripts --strip-comments index.html | crisper --html %OUTDIR%\index.html --js %OUTDIR%\index.js