\name{rDNA}
\docType{package}
\alias{rDNA-package}
\alias{rDNA}
\title{Control DNA from R}
\description{Control the Java software Discourse Network Analyzer from within \R}
\details{
A package that controls the Java software Discourse 
Network Analyzer (\acronym{DNA}) from within \R. Network matrices, 
statement frequency time series and attributes of actors 
can be transferred directly into \R. Requires the package \pkg{rJava}. 
Please obtain the latest copy of DNA from http://www.philipleifeld.de.

Make sure you execute \link{dna.init} before running any other rDNA 
command.

To display citation information, execute \code{citation("rDNA")}.
}
\seealso{
\code{\link{dna.init}}
\code{\link{dna.gui}}
\code{\link{dna.attributes}}
\code{\link{dna.network}}
}
\author{
Philip Leifeld (\url{http://www.philipleifeld.de})
}
\keyword{Programming|programming|interface}
\keyword{Programming|IO}
