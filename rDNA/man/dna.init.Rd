\name{dna.init}
\alias{dna.init}
\title{Initialize rDNA}
\description{Establish the connection between R and DNA.}
\usage{
dna.init(dna.jar.file)
}
\arguments{
\item{dna.jar.file}{ The path and file name of the \code{dna-1.31.jar} file. If \acronym{DNA} is located in the current working directory, this can be \code{"dna-1.31.jar"}. }
}
\details{
This method must be called once before rDNA can pull any
data from \acronym{DNA}. Hand over the path and file name of the
dna-1.31.jar file to the method.
}
\seealso{
\code{\link{rDNA}}
\code{\link{dna.gui}}
\code{\link{dna.network}}
\code{\link{dna.attributes}}
}
\author{
Philip Leifeld (\url{http://www.philipleifeld.com})
}
\examples{
download.file("http://www.philipleifeld.de/cms/upload/Downloads/dna-1.31.jar",
    destfile = "dna-1.31.jar", mode = "wb")
dna.init("dna-1.31.jar")
}
\keyword{Programming|programming|interface}
\keyword{Programming|IO}
