\name{dna.attributes}
\alias{dna.attributes}
\title{Get actor attributes}
\description{Transfer the attributes of persons or organizations to \R.}
\usage{
dna.attributes(infile, organizations = TRUE, verbose = TRUE)
}
\arguments{
\item{infile}{ The input .dna file as a string (i.e., enclosed in quotation marks). If the file is not in the current working directory, specify the path together with the file name. Include the file suffix. Example: \code{sample.dna}. }
\item{organizations}{If \code{TRUE}, the attributes of organizations will be returned. If \code{FALSE}, the attributes of persons will be returned. }
\item{verbose}{If \code{true}, details about the data import and its progress will be printed. If \code{false}, these information will be suppressed.}
}
\details{
Pulls the attributes of persons or organizations as a matrix into \R. 
The matrix has three columns: \code{type} (the type of actor), \code{alias} 
(the alias/description variable in \acronym{DNA}), \code{note} (the notes field) 
and \code{color} (the \acronym{RGB} color used in \acronym{DNA}).
}
\seealso{
\code{\link{rDNA}}
\code{\link{dna.init}}
\code{\link{dna.network}}
\code{\link{dna.categories}}
}
\author{
Philip Leifeld (\url{http://www.philipleifeld.de})
}
\examples{
download.file("http://www.philipleifeld.de/cms/upload/Downloads/dna-1.30.jar",
    destfile = "dna-1.30.jar", mode = "wb")
download.file("http://www.philipleifeld.de/cms/upload/Downloads/sample.dna", 
    destfile = "sample.dna", mode = "wb")
dna.init("dna-1.30.jar")
attributes <- dna.attributes("sample.dna", organizations = TRUE)
}
\keyword{Programming|programming|interface}
\keyword{Programming|IO}
