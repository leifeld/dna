![](./DNA/src/icons/dna128.png)

## Discourse Network Analyzer (DNA)

The Java software Discourse Network Analyzer (DNA) is a qualitative content analysis tool with network export facilities. You import text files and annotate statements that persons or organizations make, and the program will return network matrices of actors connected by shared concepts.

- Download the latest [release](https://github.com/leifeld/dna/releases) of the software.

- Check out the detailed [manual](https://github.com/leifeld/dna/releases/download/v2.0-beta.24/dna-manual.pdf) for more information, including installation instructions and information on network methods and `rDNA`.

- If you have questions or want to report bugs, please create an issue in the [issue tracker](https://github.com/leifeld/dna/issues).

## rDNA: Connect DNA to the statistical programming environment R

This is the companion package to DNA. It integrates the Java software with the statistical computing environment `R`.

You can install the most recent release using:

``` r
# install.packages("remotes")
remotes::install_github("leifeld/dna/rDNA@*release",
                        INSTALL_opts = "--no-multiarch")
```

Note that the package relies on `rJava`, which needs to be installed first.

[![R-CMD-check](https://github.com/leifeld/dna/workflows/make/badge.svg)](https://github.com/leifeld/dna/actions) [![Coverage status](https://codecov.io/gh/leifeld/dna/branch/master/graph/badge.svg)](https://codecov.io/github/leifeld/dna?branch=master)

## Upcoming release of DNA 3.0

Update on 20 March 2022: All work on DNA 2.0 and its companion rDNA version was ceased in early 2021. Work on a complete rewrite -- DNA 3.0 -- began in spring 2021 and is ongoing. For now, you are advised to keep using DNA 2.0 beta 25 from the [release page](https://github.com/leifeld/dna/releases/tag/v2.0-beta.25). The source code for the Java-based graphical user interface of DNA 2.0 was frozen in the [DNA2](https://github.com/leifeld/dna/tree/master/DNA2) directory in the GitHub repository. Version 3.0 is being actively developed in the [DNA](https://github.com/leifeld/dna/tree/master/DNA) directory of the repository. You can already clone it to your local computer and compile it with the JDK. It should work reasonably well both with file-based/local and remote/MySQL/PostgreSQL databases with multiple coders for coding and recoding purposes, with many new functions. But a few minor GUI-related functions are still missing and will be added soon. At the moment, the use of DNA 2.0 is still recommended because the current development version of DNA 3.0 does not have any export functions or R connectivity yet. The release of DNA 3.0 with these features is expected some time later in 2022. If you want to start coding your data but are expecting to analyze them only in 2023, feel free to use DNA 3.0 productively if you can manage to compile it.

## Support the project

Please consider contributing to the project by telling other people about the software, citing our underlying [research](https://www.philipleifeld.com/publications) in your publications, reporting or fixing [issues](https://github.com/leifeld/issues), or starting pull requests.