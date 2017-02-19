\name{rDNA}
\docType{package}
\alias{rDNA-package}
\alias{rDNA}
\title{Control DNA from R}
\description{Control the Java software Discourse Network Analyzer from within \R}
\details{
A package that controls the Java software Discourse Network Analyzer (\acronym{DNA}) from within \R. Network matrices and attributes of actors can be transferred directly into \R. Requires the package \pkg{rJava}. Make sure you execute \link{dna_init} to connect rDNA with DNA and \link{dna_connection} to connect rDNA with a DNA database before running any other rDNA command.

To display citation information, execute \code{citation("rDNA")}.
}
\seealso{
\code{\link{dna_init}}
\code{\link{dna_gui}}
\code{\link{dna_attributes}}
\code{\link{dna_network}}
}
\author{
Philip Leifeld (\url{http://www.philipleifeld.com})
}
\keyword{Programming|programming|interface}
\keyword{Programming|IO}
