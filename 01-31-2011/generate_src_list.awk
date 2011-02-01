#!/site/bin/mawk -f
# generate src.list file from the listings in the given directories
BEGIN { list="" }
/JAVA_FILES/{
nf = split( FILENAME, filenameParts, "/");
directory = ".";
for(i=2; i < nf; i++) {
  directory = directory "/" filenameParts[i];
#  print directory;
}
for(i=3; i<= NF; i++) list = list " " directory "/" $i;
}
END{
  print list;
}
