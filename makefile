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
MANUAL_FILE := dna-manual.Rnw

# define makefile variable for DNA JAR file name with version number
# 1. match version line in Dna.java using grep
# 2. remove string after version information using sed
# 3. remove string before version information using sec (including two tabs)
# 4. replace first space by dash
# 5. replace second space by nothing
# 6. wrap in file name
VERSION     := $(shell grep -P 'version = ".+"' DNA/src/dna/Dna.java | sed 's/";//' | sed 's/\t\tversion = "//' | sed 's/ /-/ ' | sed 's/ //')
JARFILE     := dna-${VERSION}.jar

# make all and print a final message that we're done
all: sample dna rDNA manual
	@echo done.

# compile the manual using knitr and texi2pdf
manual: mkdir-output
	$(CP) $(MANUAL_DIR) $(OUTPUT_DIR);                   \
	cd $(OUTPUT_DIR)/$(MANUAL_DIR);                      \
	$(RSCRIPT) "library(knitr); knit('$(MANUAL_FILE)')"; \
	$(LATEX) *.tex;                                      \
	mv *.pdf ..;                                         \
	cd ..;                                               \
	$(RM) $(MANUAL_DIR)

# compile rDNA source package
rDNA: mkdir-output
	$(RBUILD) $(R_DIR); \
	mv $(R_DIR)*.tar.gz $(OUTPUT_DIR)

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
	$(JAVAC) dna/Dna.java; \
	$(JAVAC) dna/export/ExporterR.java; \
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
