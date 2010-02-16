# 
PACKAGES=(time set report graph db/dss db/dss/native gui app)
all: 
	(cd time; make dbg)
	(cd set; make dbg)
	(cd report; make dbg)
	(cd graph; make dbg)
	(cd graph; make 2d)
	(cd db/dss; make dbg)
	(cd db/dss; make rmic)
	(cd db/dss/native; make opt)
	(cd gui; make dbg)
	(cd app; make dbg)
# site specific info
include local.makefile
# site generic info
include common.makefile
	
all-clean: 
	(find . -name "*.class" -print | gxargs rm -rf;)

all-docs: FORCE
#	generate docs
	(rm -rf docs; mkdir docs;)
	${JAVADOC} ${JAVADOC_OPT} vista.time vista.set vista.graph vista.db.dss vista.gui vista.app
#vista.dm

FORCE:
	
all-cleandocs:
	rm -rf docs/*.html

metrics:
	@generate_src_list.awk `find . -name src.list -print` | gxargs java -ms8m -mx32m -classpath ${CLASSPATH_DEF}:../classes javaquery.metrics > metrics.txt
	@mawk '$$2 !~ "\(\)" {print $$0}' metrics.txt > metricsClass.txt
	@date>> complexity.trail
	@mawk '{i+=$$3};END{print "Class Complexity of DWR/Data is " i;}' metricsClass.txt >> complexity.trail
	@mawk '$$0 ~ "\(\)" {i+=$$3};END{print "Method Complexity of DWR/Data is " i;}' metrics.txt >> complexity.trail
	@./generate_src_list.awk `find . -name src.list -print` | gxargs mawk '$$0 ~ ";" || $$0 ~ "{" || $$0 ~ "}" {i++}; END{print "Number of lines of code is " i}' >> complexity.trail
	@tail -4 complexity.trail

lsrc:
	@./generate_src_list.awk `find . -name src.list -print` | gxargs java -mx32m -classpath ${CLASSPATH_DEF} javancss.Main

wc:
	date >> package.size
	./generate_src_list.awk `find . -name src.list -print` | gxargs wc -l >> package.size
	./generate_src_list.awk `find . -name src.list -print` | gxargs wc -l 

tags:
	tags.rc

source:
	cp -P `gfind . \
	  \( -name '*.java' -o -name '*.c' -o -name '*.h' -o -name '*.f' -o -name '*.inc' \) \
	  -print` $(FTP_DIR)

jar: 
	(cd installer; rm -rf *.class)
	(cd lib/Lib; rm -rf *.class *.pyc)
	(rm -rf classes; mkdir classes; mkdir classes/vista)
	cp .vista classes/vista
	cp version classes/vista
	cp -P `find . -name "*.class" -print` classes/vista
	cp -P `find . -name "*.properties" -print` classes/vista
	cp -P `find . -name "*.data" -print` classes/vista
	cp *.gif classes/vista
	(cd classes; jar -cf vista.jar vista; cp vista.jar ../$(LIB_DIR))
	(cd help; jar -cf vista-help.jar *.hs *.xml *.jhm vista-help; cp vista-help.jar ../$(LIB_DIR))

native: 
	( cd db/dss/native; make opt; cd ../; cp libdss.so ../../$(LIB_DIR))

install: update all jar native


release: install
	(cd installer; make)

old-release: install
	(cd ../; \
	zip vista-bin.zip -o -r vista -i \*.txt -x \*~; \
	zip vista-bin.zip -o -r vista/bin/* -x \*~; \
	zip vista-bin.zip -o -r vista/lib/* -x \*~; \
	zip vista-bin.zip -o -r vista/scripts/* -x \*~; \
	mv vista-bin.zip ${FTP_DIR}; \
	)
	
fullrelease: release source vista-docs
	(cd ../ ; zip vista-src.zip -o -r vista -x \*.zip -x CVS -x \*~;)
	mv ../vista-src.zip ${FTP_DIR}

vista-docs: docs
	rm -rf $(FTP_DIR)/docs
	cp -a docs $(FTP_DIR)/

update:
	cvs update
