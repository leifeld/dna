# how to write an R package https://hilaryparker.com/2014/04/29/writing-an-r-package-from-scratch/

library("devtools")
library("roxygen2")

#Update Documentation
#setwd("C:/Users/binis/Documents/GitHub/dna/rDNA")
#setwd("F:/Dropbox/Github/dna/rDNA")
# setwd("/home/johannes/Documents/Github 4/dna/rDNA/")
setwd("/home/johannes/Documents/Github/dna/rDNA/")
desc <- readLines("DESCRIPTION")
date <- desc[grepl("^Date:", desc)]
date2 <- gsub("[^[:digit:]-]", "", date)
desc[grepl("^Date:", desc)] <- gsub(date2, Sys.Date(), desc[grepl("^Date:", desc)])
vers <- desc[grepl("^Version:", desc)]
vers2 <- gsub("[^[:digit:].]", "", vers)
vers3 <- readline(prompt = paste("New Version? Old:", vers2)) 
desc[grepl("^Version:", desc)] <- gsub(vers2, vers3, desc[grepl("^Version:", desc)])
writeLines(desc, "DESCRIPTION")


roxygen2::roxygenise(clean = TRUE)
setwd("..")
devtools::check("rDNA")
devtools::spell_check("rDNA", dict = "en_GB", ignore = c(
  "CLASSPATH",
  "dd",
  "dna",
  "DNA's",
  "docTitle",
  "excludeTypes",
  "Gruber",
  "Leifeld",
  "igraph",
  "ggplot",
  "java",
  "Jaccard",
  "etc",
  "mySQL",
  "nw",
  "org",
  "pts",
  "wd",
  "plottable",
  "linetype",
  "bw", 
  "color", 
  "cutree", 
  "eigen", 
  "lineend", 
  "louvain", 
  "mcquitty", 
  "MDS", 
  "pam", 
  "plotDendro", 
  "RColorBrewer", 
  "walktrap", 
  "yyyy",
  "onemode",
  "twomode",
  "getDocuments",
  "clust",
  "dh",
  "drl",
  "graphopt",
  "kk",
  "knitr",
  "lables",
  "lgl",
  "mds",
  "POSIXct",
  "qualifierAggregation",
  "setDocuments",
  "dist",
  "edgelist",
  "eventSequence",
  "isoMDS",
  "plotMDS",
  "timewindow",
  "vegdist"
))
#source("https://install-github.me/MangoTheCat/goodpractice")
#goodpractice::gp("rDNA")
#lintr::lint("rDNA.R")
#lintr::lint_package("rDNA")

#devtools::install("rDNA")

# update github
system("git status")

system("git add -A")
commit_message <- readline(prompt = "Commit message") 
system(paste0("git commit -m'",
              commit_message,
              "'"))

#build
build(pkg = "rDNA", manual = TRUE)

# create the package in wd
setwd("C:/Users/binis/Documents/GitHub/dna")
install("rDNA", args = c("--no-multiarch", "--no-test-load"))

# change version number in manual



### quick test
setwd("/home/johannes/Documents/Github/dna/rDNA/")
roxygen2::roxygenise(clean = TRUE)
setwd("..")
#devtools::check("rDNA")
devtools::install("rDNA")

