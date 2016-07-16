\name{dna.gui}
\alias{dna.gui}
\title{Start DNA GUI}
\description{Start the GUI of the Java software Discourse Network Analyzer.}
\usage{
dna.gui(memory = 1024)
}
\arguments{
\item{memory}{The amount of memory to allocate for DNA. By default, 1024 MB will be allocated. This should be enough for most .dna files. If your computer is low on memory, you may set it to 512 or 256. If your file is very large and your computer has enough RAM, you can allocate 2048 MB or more.}
}
\details{
The method starts the user interface of the Java software
Discourse Network Analyzer (\acronym{DNA}). All changes in the GUI
have to be saved manually and will not be transferred back
to \R.
}
\seealso{
\code{\link{rDNA}}
\code{\link{dna.init}}
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
\dontrun{
dna.gui()
}
}
\keyword{Programming|programming|interface}
\keyword{Programming|IO}
