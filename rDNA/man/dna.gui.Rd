\name{dna_gui}
\alias{dna_gui}
\title{Start DNA GUI}
\description{Start the GUI of the Java software Discourse Network Analyzer.}
\usage{
dna.gui(infile = NULL, javapath = NULL, memory = 1024)
}
\arguments{
\item{infile}{If an optional filename is provided here, DNA will open this database upon startup.}
\item{javapath}{The optional path to the java binary executable in case Java's path was not properly exported.}
\item{memory}{The amount of memory to allocate for DNA. By default, 1024 MB will be allocated. This should be enough for most .dna files. If your computer is low on memory, you may set it to 512 or 256. If your file is very large and your computer has enough RAM, you can allocate 2048 MB or more.}
}
\details{
The method starts the user interface of the Java software
Discourse Network Analyzer (\acronym{DNA}).
}
\seealso{
\code{\link{rDNA}}
\code{\link{dna_init}}
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
##\dontrun{
##dna.gui()
##}
}
\keyword{Programming|programming|interface}
\keyword{Programming|IO}
