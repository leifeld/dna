## Discourse Network Analyzer (DNA) Manual

This is the most recent version of the DNA manual. It is written using **knitr** with **LaTeX** and **R** code chunks embedded in the **LaTeX** code. You can compile your own PDF with the most recent changes. To do so, download or clone the main DNA folder on [GitHub](https://github.com/leifeld/dna) and then make sure you:

* Have [**LaTeX**](https://www.latex-project.org/get/) installed on your system (for compiling Rnw files, the very lightweight [TinyTeX](https://yihui.name/tinytex/) is sufficient, but you will need to download a couple of extra packages).
* Have [**R**](https://cloud.r-project.org/) installed on your system.
* Have [**RStudio**](https://www.rstudio.com/products/rstudio/download/#download) installed on your system.
* Have **DNA** and **rDNA** correctly set up on your system.
* Have the R package **kableExtra** installed (`install.packages("kableExtra")`).

You can find instructions on how to install the programs in the latest release version of the [manual](https://github.com/leifeld/dna/releases/download/v2.0-beta.21/dna-manual.pdf).

Finally you need to choose knitr to weave the file “dna-manual.Rnw” files in RStudio (Tools -> Global Options -> Sweave) and then press the "Compile PDF" button.
