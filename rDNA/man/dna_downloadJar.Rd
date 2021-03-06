% Generated by roxygen2: do not edit by hand
% Please edit documentation in R/rDNA.R
\name{dna_downloadJar}
\alias{dna_downloadJar}
\title{Download the binary DNA JAR file}
\usage{
dna_downloadJar(path = getwd(), force = FALSE, returnString = FALSE)
}
\arguments{
\item{path}{Directory path in which the jar file will be stored.}

\item{force}{Logical. Should the file be overwritten if it already exists?}

\item{returnString}{Logical. Return the file name of the downloaded jar file?}
}
\description{
Downloads the newest released DNA JAR file necessary for running
\link{dna_init}.
}
\details{
This function uses GitHub's API to download the latest DNA jar file to the
working directory. If you would like to store the DNA JAR file permanently
in the rDNA library path, you can use the argument
\code{path = paste0(dirname(system.file(".", package = "rDNA")), "/", "java")}.
Do this only if the library path does not already contain a DNA JAR file with
the same version number as the rDNA package. To list the contents of the
\code{java} sub-directory of the library path, where the DNA JAR file is
usually stored, you can try:
\code{p <- paste0(dirname(system.file(".", package = "rDNA")), "/", "java")}
followed by \code{dir(p)}.

The correct DNA JAR file is normally downloaded and stored in the rDNA
library path when the rDNA package is installed. Use \code{dna_downloadJar}
only if this failed or if you want to download an additional or newer copy
of the DNA JAR file to your working directory, perhaps for purposes of
replicability. Note that the DNA JAR file and the rDNA package must have the
same version number for full compatibility. To check your rDNA version, try
\code{packageVersion("rDNA")}.
}
\seealso{
\code{\link{dna_installJar}}
}
\author{
Philip Leifeld, Johannes B. Gruber
}
