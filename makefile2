# define makefile variables for programs and compilers
JAVAC       := javac
FIND        := find
RM          := rm -rf
MKDIR       := mkdir -p
CP          := cp -r
RBUILD      := R CMD build
RSCRIPT     := Rscript -e
LATEX       := texi2pdf

# define makefile variables for directories and files
OUTPUT_DIR  := output
SOURCE_DIR  := DNA/src
LIB_DIR     := DNA/lib
SAMPLE_DIR  := sample
SAMPLE_FILE := sample.dna
R_DIR       := rDNA
MANUAL_DIR  := manual
MANUAL_FILE := dna-manual

# define makefile variable for DNA JAR file name with version number
# 1. match version line in Dna.java using grep
# 2. remove string after version information using sed
# 3. remove string before version information using sec (including two tabs)
# 4. replace first space by dash
# 5. replace second space by nothing
# 6. wrap in file name
VERSION     := $(shell grep -P 'version = ".+"' DNA/src/dna/Dna.java | sed 's/";//' | sed 's/\t\tversion = "//' | sed 's/ /-/ ' | sed 's/ //')
JARFILE     := dna-${VERSION}.jar
RDNAVERSION := $(shell grep -P '^Version: .+' $(R_DIR)/DESCRIPTION | sed 's/Version: //')

# make all and print a final message that we're done
all: sample rDNA manual
	@echo done.

# compile the manual using knitr and texi2pdf
manual: rdnainst
	$(RSCRIPT) "if(! 'knitr' %in% installed.packages()) install.packages('knitr')"; \
	$(RSCRIPT) "if(! 'ggplot2' %in% installed.packages()) install.packages('ggplot2')"; \
	$(RSCRIPT) "if(! 'gridExtra' %in% installed.packages()) install.packages('gridExtra')"; \
	$(RSCRIPT) "if(! 'kableExtra' %in% installed.packages()) install.packages('kableExtra')"; \
	$(RSCRIPT) "if(! 'LexisNexisTools' %in% installed.packages()) install.packages('LexisNexisTools')"; \
	$(RSCRIPT) "if(! 'quanteda.textmodels' %in% installed.packages()) install.packages('quanteda.textmodels')"; \
	$(RSCRIPT) "if(! 'statnet' %in% installed.packages()) install.packages('statnet')"; \
	$(RSCRIPT) "if(! 'igraph' %in% installed.packages()) install.packages('igraph')"; \
	$(RSCRIPT) "if(! 'rJava' %in% installed.packages()) install.packages('rJava')"; \
	$(RSCRIPT) "if(! 'quanteda.corpora' %in% installed.packages()) remotes::install_github('quanteda/quanteda.corpora')"; \
	$(MKDIR) $(OUTPUT_DIR); \
	$(CP) $(MANUAL_DIR) $(OUTPUT_DIR); \
	cd $(OUTPUT_DIR)/$(MANUAL_DIR);                          \
	$(RSCRIPT) "library(knitr); knit('$(MANUAL_FILE).Rnw')"; \
	$(LATEX) $(MANUAL_FILE).tex;                             \
	mv *.pdf ..;                                             \
	cd ..;                                                   \
	$(RM) $(MANUAL_DIR)

# install rDNA
rdnainst: rDNA-full-temp
	$(RSCRIPT) "if(! 'devtools' %in% installed.packages()) install.packages('devtools')"; \
	cd $(OUTPUT_DIR)/rtemp; \
	$(RSCRIPT) "devtools::install('rDNA', dependencies = TRUE, upgrade = FALSE)"; \
	cd ..; \
	$(RM) rtemp; \
	cd ..; \
	rmdir --ignore-fail-on-non-empty $(OUTPUT_DIR)

# compile rDNA source package without DNA
rDNA: mkdir-output
	$(RBUILD) $(R_DIR); \
	mv $(R_DIR)*.tar.gz $(OUTPUT_DIR)

# compile rDNA source package with DNA
rDNA-full: rDNA-full-temp
	cd $(OUTPUT_DIR)/rtemp; \
	$(RBUILD) $(R_DIR); \
	mv $(R_DIR)_${RDNAVERSION}.tar.gz ../$(R_DIR)_full_${RDNAVERSION}.tar.gz; \
	cd ..; \
	$(RM) rtemp

# create temporary version of full rDNA package
rDNA-full-temp: mkdir-output compile-java
	$(MKDIR) $(OUTPUT_DIR)/rtemp; \
	$(CP) $(R_DIR) $(OUTPUT_DIR)/rtemp; \
	cd $(OUTPUT_DIR); \
	$(MKDIR) temp; \
	mv src temp/; \
	cd temp/src; \
	jar cmf ../../../$(SOURCE_DIR)/META-INF/MANIFEST.MF ../../${JARFILE} *; \
	chmod +x ../../$(JARFILE); \
	cd ../..; \
	$(MKDIR) rtemp/$(R_DIR)/inst/java/; \
	mv ${JARFILE} rtemp/$(R_DIR)/inst/java/; \
	$(RM) temp; \

# copy-sample - copy the sample.dna database to the output directory
sample: mkdir-output
	$(CP) $(SAMPLE_DIR)/$(SAMPLE_FILE) $(OUTPUT_DIR)/

# create jar file based on MANIFEST from source directory and make executable
dna: compile-java
	cd $(OUTPUT_DIR)/src/;                                            \
	jar cmf ../../$(SOURCE_DIR)/META-INF/MANIFEST.MF ../${JARFILE} *; \
	chmod +x ../$(JARFILE);                                           \
	cd ..;                                                            \
	$(RM) src/

# compile-java - compile main class (trickling down to all dependencies),
# then delete sources from output directory
compile-java: extract-jar-libs
	cd $(OUTPUT_DIR)/src/; \
	$(JAVAC) -version; \
	$(JAVAC) --release 8 dna/Dna.java; \
	$(JAVAC) --release 8 dna/export/ExporterR.java; \
	$(FIND) . -name '*.java' -exec $(RM) {} \;

# extract-jar-libs - extract libraries
extract-jar-libs: copy-java-libs
	cd $(OUTPUT_DIR)/src/;                      \
	$(FIND) . -name '*.jar' -exec jar xf {} \;; \
	$(RM) *.jar

# copy-java-libs - copy the jar libraries to the src directory in the output directory
copy-java-libs: copy-java-sources
	$(CP) $(LIB_DIR)/*.jar $(OUTPUT_DIR)/src/

# copy-java-sources - copy the Java source files to the output directory
copy-java-sources: mkdir-output
	$(CP) $(SOURCE_DIR) $(OUTPUT_DIR)

# mkdir-output - create output directory
mkdir-output:
	$(MKDIR) $(OUTPUT_DIR)
	
# clean up: remove output directory and all contents
.PHONY: clean
clean:
	$(RM) $(OUTPUT_DIR)

# check and test rDNA
test-rDNA: rDNA
	cd $(OUTPUT_DIR); \
	R CMD check --no-multiarch --no-manual --as-cran $(R_DIR)_$(RDNAVERSION).tar.gz

# test manual
test-manual:
	$(eval MANUALSIZE = $(shell pdftotext '$(OUTPUT_DIR)/$(MANUAL_FILE).pdf' - | wc -w))
	@if [ ${MANUALSIZE} -gt 40000 ]; \
	then echo "PASS: DNA manual PDF contains ${MANUALSIZE} words (test requires > 40000)."; \
	exit 0; \
	else echo "FAIL: DNA manual PDF contains ${MANUALSIZE} words (test requires > 40000)."; \
	exit 1; \
	fi

# test sample
test-sample:
	$(eval SAMPLETEST1 = $(shell sqlite3 $(OUTPUT_DIR)/$(SAMPLE_FILE) "SELECT EXISTS (SELECT * FROM sqlite_master WHERE type='table' AND name='<tableName>');"))
	@if [ ${SAMPLETEST1} = 0 ]; \
	then echo PASS: sample.dna contains tables.; \
	exit 0; \
	else echo FAIL: sample.dna does not contain any tables.; \
	exit 1; \
	fi
	$(eval SAMPLETEST2 = $(shell sqlite3 $(OUTPUT_DIR)/$(SAMPLE_FILE) "PRAGMA integrity_check;"))
	@if [ ${SAMPLETEST2} = ok ]; \
	then echo PASS: sample.dna passed sqlite3 integrity test.; \
	exit 0; \
	else echo FAIL: sample.dna did not pass sqlite3 integrity test.; \
	exit 1; \
	fi

# test dna
test-dna:
	@if jar -tvf $(OUTPUT_DIR)/$(JARFILE) | grep ' dna/Dna\.class'; \
	then echo PASS: $(JARFILE) contains Dna.class.; \
	exit 0; \
	else echo FAIL: $(JARFILE) does not contain Dna.class.; \
	exit 1; \
	fi
	@if jar -tvf $(OUTPUT_DIR)/$(JARFILE) | grep ' dna/export/ExporterR\.class'; \
	then echo PASS: $(JARFILE) contains ExporterR.class.; \
	exit 0; \
	else echo FAIL: $(JARFILE) does not contain ExporterR.class.; \
	exit 1; \
	fi
	@if jar -tvf $(OUTPUT_DIR)/$(JARFILE) | grep ' libjri.so'; \
	then echo PASS: $(JARFILE) contains libjri.so in root directory.; \
	exit 0; \
	else echo FAIL: $(JARFILE) does not contain libjri.so in root directory.; \
	exit 1; \
	fi
