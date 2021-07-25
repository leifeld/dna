# define makefile variable for DNA JAR file name with version number
# 1. match version line in Dna.java using grep
# 2. remove string after version information using sed
# 3. remove string before version information using sec (including two tabs)
# 4. replace first space by dash
# 5. replace second space by nothing
# 6. wrap in file name
VERSION     := $(shell grep -P 'version = ".+"' DNA/src/dna/Dna.java | sed 's/";//' | sed 's/\t\tversion = "//' | sed 's/ /-/ ' | sed 's/ //')
JARFILE     := dna-${VERSION}.jar
RDNAVERSION := $(shell grep -P '^Version: .+' rDNA/DESCRIPTION | sed 's/Version: //')

# make all and print a final message that we're done
all: sample dna rDNA manual
	@echo Done.

# compile rDNA source package with DNA jar
rDNA-full: dna
	mkdir -p output/rtemp; \
	cp -r rDNA output/rtemp; \
	cd output; \
	mkdir -p rDNA/inst/java/; \
	cp ${JARFILE} rDNA/inst/java/; \
	R CMD build rDNA; \
	mv rDNA_${RDNAVERSION}.tar.gz rDNA_full_${RDNAVERSION}.tar.gz; \
	cd ..

# compile rDNA source package without DNA
rDNA: mkdir-output
	R CMD build rDNA; \
	mv rDNA*.tar.gz output

# build DNA Jar file
dna: mkdir-output
	cp -r DNA/src output; \
	cp -r DNA/lib/*.jar output/src/; \
	cp -r DNA/META-INF output; \
	cd output/src/; \
	find . -name '*.jar' -exec jar xf {} \;; \
	rm -rf *.jar; \
	javac --release 8 dna/Dna.java; \
	find . -name '*.java' -exec rm -rf {} \;; \
	jar cmf ../META-INF/MANIFEST.MF ../$(JARFILE) *; \
	cd ..; \
	chmod +x $(JARFILE); \
	rm -r src META-INF; \
	cd ..

# copy sample file to output directory
sample: mkdir-output
	cp -r sample/sample.dna output/

# create output directory
mkdir-output:
	mkdir -p output

# clean up: remove output directory and all contents
.PHONY: clean
clean:
	rm -rf output

# test sample
test-sample:
	$(eval SAMPLETEST1 = $(shell sqlite3 output/sample.dna "SELECT EXISTS (SELECT * FROM sqlite_master WHERE type='table' AND name='<tableName>');"))
	@if [ ${SAMPLETEST1} = 0 ]; \
	then echo PASS: sample.dna contains tables.; \
	exit 0; \
	else echo FAIL: sample.dna does not contain any tables.; \
	exit 1; \
	fi
	$(eval SAMPLETEST2 = $(shell sqlite3 sample/sample.dna "PRAGMA integrity_check;"))
	@if [ ${SAMPLETEST2} = ok ]; \
	then echo PASS: sample.dna passed sqlite3 integrity test.; \
	exit 0; \
	else echo FAIL: sample.dna did not pass sqlite3 integrity test.; \
	exit 1; \
	fi

# test dna
test-dna:
	@if jar -tvf output/$(JARFILE) | grep ' dna/Dna\.class'; \
	then echo PASS: $(JARFILE) contains Dna.class.; \
	exit 0; \
	else echo FAIL: $(JARFILE) does not contain Dna.class.; \
	exit 1; \
	fi
	@if jar -tvf output/$(JARFILE) | grep ' libjri.so'; \
	then echo PASS: $(JARFILE) contains libjri.so in root directory.; \
	exit 0; \
	else echo FAIL: $(JARFILE) does not contain libjri.so in root directory.; \
	exit 1; \
	fi
	@if jar -tvf output/$(JARFILE) | grep ' META-INF/MANIFEST.MF'; \
	then echo PASS: $(JARFILE) contains MANIFEST.; \
	exit 0; \
	else echo FAIL: $(JARFILE) does not contain MANIFEST.; \
	exit 1; \
	fi

# test manual
test-manual:
	$(eval MANUALSIZE = $(shell pdftotext '$(OUTPUT_DIR)/$(MANUAL_FILE).pdf' - | wc -w))
	@if [ ${MANUALSIZE} -gt 40000 ]; \
	then echo "PASS: DNA manual PDF contains ${MANUALSIZE} words (test requires > 40000)."; \
	exit 0; \
	else echo "FAIL: DNA manual PDF contains ${MANUALSIZE} words (test requires > 40000)."; \
	exit 1; \
	fi
