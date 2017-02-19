\name{dna_attributes}
\alias{dna_attributes}
\title{Get actor attributes}
\description{Transfer the attributes of a variable from the attribute manager in DNA to \R.}
\usage{
dna.attributes(dna_connection, statementType = "DNA Statement", 
    variable = "organization")
}
\arguments{
\item{dna_connection}{ The database connection. Needs to be established using the \link{dna_connection} function. }
\item{statementType}{ The statement type to which the variable belongs, e.g., "DNA Statement". }
\item{variable}{ The variable name for which attributes should be compiled. }
}
\details{
Pulls the attributes of a variable as a data.frame into \R.
}
\seealso{
\code{\link{rDNA}}
\code{\link{dna_init}}
\code{\link{dna_network}}
\code{\link{dna_categories}}
}
\author{
Philip Leifeld (\url{http://www.philipleifeld.com})
}
\examples{
##download.file("http://www.philipleifeld.de/cms/upload/Downloads/dna-1.31.jar",
##    destfile = "dna-1.31.jar", mode = "wb")
##download.file("http://www.philipleifeld.de/cms/upload/Downloads/sample.dna", 
##    destfile = "sample.dna", mode = "wb")
##dna.init("dna-1.31.jar")
##attributes <- dna.attributes("sample.dna", organizations = TRUE)
}
\keyword{Programming|programming|interface}
\keyword{Programming|IO}
