\name{dna.categories}
\alias{dna.categories}
\title{Get list of categories}
\description{Transfer a vector of all categories in a DNA file to \R.}
\usage{
dna.categories(infile, verbose=TRUE)
}
\arguments{
\item{infile}{ The input .dna file as a string (i.e., enclosed in quotation marks). If the file is not in the current working directory, specify the path together with the file name. Include the file suffix. Example: \code{sample.dna}. }
\item{verbose}{If \code{true}, details about the data import and its progress will be printed. If \code{false}, these information will be suppressed.}
}
\details{
Puts all categories found in a .dna file in a list and 
imports them as a vector into \R.
}
\seealso{
\code{\link{rDNA}}
\code{\link{dna.init}}
\code{\link{dna.gui}}
\code{\link{dna.network}}
\code{\link{dna.attributes}}
}
\author{
Philip Leifeld (\url{http://www.philipleifeld.de})
}
\examples{
download.file(
  "http://www.philipleifeld.de/cms/upload/Downloads/dna-1.27.jar", 
  destfile="dna-1.27.jar", mode="wb")
download.file(
  "http://www.philipleifeld.de/cms/upload/Downloads/sample.dna", 
  destfile="sample.dna", mode="wb")
dna.init("dna-1.27.jar")
categories <- dna.categories("sample.dna")
}
\keyword{Programming|programming|interface}
\keyword{Programming|IO}
