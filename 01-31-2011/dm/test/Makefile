# Makefile to update the catalogs for dss files
# get all files under current directory
PACKAGE=vista.dm.test
PACKAGE_LOC=vista/dm/test
#
JAVACC_HOME=/home/palm/nsandhu/java/Util/JavaCC1.0
JAVACC=$(JAVACC_HOME)/bin/javacc
JAVACC_OPTIONS=
JTB_HOME=/home/palm/nsandhu/java/Util/jtb
JTB=$(JTB_HOME)/bin/jtbf
JTB_OPTIONS=-np vista.dm.syntaxtree -vp vista.dm.visitor -jd  -tk -printer
#
JAVA_FILES = TestCommon.java TestDTS.java TestExpr.java TestStudy.java TestPortfolio.java TestProject.java
#
include ../../common.makefile
JRE_OPTS = -Dvista.home=../../../vista -mx16m -nojit -classpath ${CLASSPATH_DEF}
#
alltests : test1 test2 test3 test4 test5
	echo "All tests done!"
test1: dbg
	jre ${JRE_OPTS} junit.textui.TestRunner vista.dm.test.TestDTS
test2: dbg
	jre ${JRE_OPTS} junit.textui.TestRunner vista.dm.test.TestExpr
test3: dbg
	jre ${JRE_OPTS} junit.textui.TestRunner vista.dm.test.TestStudy
test4: dbg
	jre ${JRE_OPTS} junit.textui.TestRunner vista.dm.test.TestPortfolio
test5: dbg
	jre ${JRE_OPTS} junit.textui.TestRunner vista.dm.test.TestProject

