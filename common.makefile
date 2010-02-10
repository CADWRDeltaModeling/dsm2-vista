# be sure to include local.makefile before this
#
CLASS_DIR=${VISTA_HOME}${PATH_SEP}..
CLASS_LOC=$(CLASS_DIR)/$(PACKAGE_LOC)
CLASS_FILES=$(JAVA_FILES:%.java=${CLASS_LOC}/%.class)

#.KEEP_STATE:

#PRECIOUS: $(JAVA_FILES) 

# java related stuff ( change this to suit yourself)
JAVA = $(JAVA_HOME)/bin/java -mx32m
JAVAC = $(JAVA_HOME)/bin/javac -J"-mx44m"
JAVAC2 = $(JAVA_HOME2)/bin/javac -J"-mx44m"
RMIC =  $(JAVA_HOME)/bin/rmic
JAVAJAR=$(JAVA_HOME)/lib/classes.zip
#JAVAJAR=$(JAVA_HOME)/jre/lib/rt.jar$(CPATH_SEP)$(JAVA_HOME)/jre/lib/i18n.jar
SWINGJAR=$(VISTA_HOME)/lib/swingall.jar
VISTAJAR=$(VISTA_HOME)/lib/vista.jar
TESTJAR=$(VISTA_HOME)/lib/junit.jar
PDJAR=$(VISTA_HOME)/lib/pd.jar
MISCJAR=$(VISTA_HOME)/lib/misc.jar
JPYJAR=$(VISTA_HOME)/lib/jpy.jar
PTMJAR=$(VISTA_HOME)/lib/ptm.jar
#
JAVAC_OPTS= -g -d "$(CLASS_DIR)" -deprecation
#
CLASSPATH_DEF= ".$(CPATH_SEP)${CLASS_DIR}$(CPATH_SEP)$(JAVAJAR)$(CPATH_SEP)$(SWINGJAR)$(CPATH_SEP)$(PDJAR)$(CPATH_SEP)$(TESTJAR)$(CPATH_SEP)$(MISCJAR)$(CPATH_SEP)$(JPYJAR)$(CPATH_SEP)$(PTMJAR)"

#${TESTS} :=JAVAC_OPT= -g -d $(CLASS_DIR)
#${TESTS}:= CLASSPATH_DEF=../${CLASS_DIR}:/site/lib/java/classes.zip

BIN_DIR=bin
LIB_DIR=lib
# document generation options
JAVADOC = /site/java/jdk1.2/bin/javadoc
JAVADOC_OPT= -classpath ${CLASSPATH_DEF} -d $(CLASS_DIR)/vista/docs -version -author 

opt dbg: setversion $(CLASS_FILES)
	@echo "All done"

setversion:
	echo "version=Vista 1.0 : Build date " `date +%d-%b-%y` > ${VISTA_HOME}/version

test: dbg
	(cd test $(CMD_SEP)  make test )

$(CLASS_LOC)/%.class: %.java $?
	$(JAVAC) $(JAVAC_OPTS) -classpath $(CLASSPATH_DEF) $< 

clean: 
	(cd $(CLASS_LOC) $(CMD_SEP) $(DELETE) *.class)
	
docs: ${JAVA_FILES}
#   generate docs
	${JAVADOC} ${JAVADOC_OPT} $(JAVA_FILES)

cleandocs:
	rm -rf $(CLASS_DIR)/docs/*.html

