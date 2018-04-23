## Discourse Network Analyzer (DNA)

The Java software Discourse Network Analyzer (DNA) is a qualitative content analysis tool with network export facilities. You import text files and annotate statements that persons or organizations make, and the program will return network matrices of actors connected by shared concepts.

- Download the latest [release](https://github.com/leifeld/dna/releases) of the software.

- Check out the detailed [manual](https://github.com/leifeld/dna/releases/download/v2.0-beta.20/dna-manual.pdf) for more information, including installation instructions and information on network methods and rDNA.

- If you have questions or want to report bugs, please create an issue in the [issue tracker](https://github.com/leifeld/dna/issues).

<br />

## rDNA. A Package to Control DNA from R

This is the companion package to DNA. It integrates the Java software with the statistical computing environment R.

You can install the package using:

``` r
# install.packages("devtools")
devtools::install_github("leifeld/dna/rDNA", args = "--no-multiarch")
```
The package relies on `rJava`, which needs to be installed first. For details on the installation process on different operating systems, consult the chapter "Installation of DNA and rDNA" in the manual.

[![Build Status](https://travis-ci.org/leifeld/dna.svg?branch=master)](https://travis-ci.org/leifeld/dna)

