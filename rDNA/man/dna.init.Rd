\name{dna_init}
\alias{dna_init}
\title{Initialize rDNA}
\description{Establish the connection between R and DNA.}
\usage{
dna_init(jarfile)
}
\arguments{
\item{jarfile}{ The file name of the DNA jar file. Make sure the DNA jar file is located in the current working directory. }
}
\details{
This method must be called once before rDNA can pull any
data from \acronym{DNA}. Hand over the file name of the
DNA jar file to the method. This is necessary to instruct 
rDNA as to where DNA is located, so both can work together.
}
\seealso{
\code{\link{rDNA}}
\code{\link{dna_gui}}
\code{\link{dna_network}}
\code{\link{dna_attributes}}
}
\author{
Philip Leifeld (\url{http://www.philipleifeld.com})
}
\examples{
##download.file("http://www.philipleifeld.de/cms/upload/Downloads/dna-1.31.jar",
##    destfile = "dna-1.31.jar", mode = "wb")
##dna.init("dna-1.31.jar")
}
\keyword{Programming|programming|interface}
\keyword{Programming|IO}
