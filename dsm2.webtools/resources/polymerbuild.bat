# command to create a small deployable unit with just two files produce with .prod suffix
# polybuild qual_animator.html --maximum-crush --suffix prod
vulcanize --inline-css --inline-scripts --strip-comments dsm2-grid-animator.html | crisper --html dsm2-grid-animator.build.html --js dsm2-grid-animator.build.js