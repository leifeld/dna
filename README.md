## Discourse Network Analyzer (DNA)

The Java software Discourse Network Analyzer (DNA) is a qualitative content analysis tool with network export facilities. You import text files and annotate statements that persons or organizations make, and the program will return network matrices of actors connected by shared concepts.

- Download the latest [release](https://github.com/leifeld/dna/releases) of the software.

- Check out the detailed [manual](https://github.com/leifeld/dna/releases/download/v2.0-beta.24/dna-manual.pdf) for more information, including installation instructions and information on network methods and `rDNA`.

- If you have questions or want to report bugs, please create an issue in the [issue tracker](https://github.com/leifeld/dna/issues).

<br />

## rDNA. A Package to Control DNA from R

This is the companion package to DNA. It integrates the Java software with the statistical computing environment `R`.

You can install the most recent release using:

``` r
# install.packages("remotes")
remotes::install_github("leifeld/dna/rDNA@*release", INSTALL_opts = "--no-multiarch")
```

This is the recommended version for most users. Note that the package relies on `rJava`, which needs to be installed first. For details on the installation process on different operating systems, consult the chapter "Installation of DNA and rDNA" in the [manual](https://github.com/leifeld/dna/releases/download/v2.0-beta.24/dna-manual.pdf).

To update `rDNA`, you can use:

``` r
# install.packages("remotes")
remotes::install_github("leifeld/dna/rDNA@*release", INSTALL_opts = "--no-multiarch")
library("rDNA")
dna_downloadJar() # update DNA as well to have matching versions
```

Please note that if you prefer to use the very latest version, you are required to compile the current jar file from the sources on GitHub, for example using the provided make file. Then you can install `rDNA` from source using:

``` r
# install.packages("remotes")
remotes::install_github("leifeld/dna/rDNA", INSTALL_opts = "--no-multiarch")
```

[![R-CMD-check](https://github.com/leifeld/dna/workflows/make/badge.svg)](https://github.com/leifeld/dna/actions) [![Coverage status](https://codecov.io/gh/leifeld/dna/branch/master/graph/badge.svg)](https://codecov.io/github/leifeld/dna?branch=master)
[<img src="http://img.shields.io/liberapay/patrons/leifeld.svg?logo=liberapay">](https://liberapay.com/leifeld)
[<img src="http://img.shields.io/liberapay/receives/leifeld.svg?logo=liberapay">](https://liberapay.com/leifeld)

## Contribute to the project

Please consider contributing to the project by telling other people about the software; citing our underlying [research](https://www.philipleifeld.com/publications) in your publications; fixing [issues](https://github.com/leifeld/issues) or adding features via pull requests; or funding the development financially:

<a href="https://liberapay.com/leifeld/donate"><img alt="Donate using Liberapay" src="https://liberapay.com/assets/widgets/donate.svg"></a>
