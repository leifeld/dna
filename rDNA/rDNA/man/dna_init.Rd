% Generated by roxygen2: do not edit by hand
% Please edit documentation in R/rDNA.R
\name{dna_init}
\alias{dna_init}
\title{Initialize the connection with DNA}
\usage{
dna_init(jarfile = dna_jar(), memory = 1024, returnString = FALSE)
}
\arguments{
\item{jarfile}{The file name of the DNA jar file, e.g.,
\code{"dna-3.0.7.jar"}. Can be auto-detected using the
\code{\link{dna_jar}} function, which looks for a version matching the
installed \pkg{rDNA} version in the library path and working directory.}

\item{memory}{The amount of memory in megabytes to allocate to DNA, for
example \code{1024} or \code{4096}.}

\item{returnString}{Return a character object representing the jar file name?}
}
\description{
Establish a connection between \pkg{rDNA} and the DNA software.
}
\details{
To use \pkg{rDNA}, DNA first needs to be initialized. This means that
\pkg{rDNA} needs to be told where the DNA executable file, i.e., the jar
file, is located. When the \code{dna_init} function is used, the connection
to the DNA software is established, and this connection is valid for the rest
of the \R session. To initialize a connection with a different DNA version or
path, the \R session would need to be restarted first.
}
\examples{
\dontrun{
dna_init()
}

}
\seealso{
Other startup: 
\code{\link{dna_getHeadlessDna}()},
\code{\link{dna_jar}()},
\code{\link{dna_sample}()}
}
\author{
Philip Leifeld
}
\concept{startup}
