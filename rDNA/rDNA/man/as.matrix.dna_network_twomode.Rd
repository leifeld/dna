% Generated by roxygen2: do not edit by hand
% Please edit documentation in R/dna_network.R
\name{as.matrix.dna_network_twomode}
\alias{as.matrix.dna_network_twomode}
\title{Convert a \code{dna_network_twomode} object to a matrix}
\usage{
\method{as.matrix}{dna_network_twomode}(x, ...)
}
\arguments{
\item{x}{The \code{dna_network_twomode} object, as returned by the
\code{\link{dna_network}} function.}

\item{...}{Additional arguments. Currently not in use.}
}
\description{
Convert a \code{dna_network_twomode} object to a matrix.
}
\details{
Remove the attributes and \code{"dna_network_twomode"} class label from a
\code{dna_network_twomode} object and return it as a numeric matrix.
}
\seealso{
Other networks: 
\code{\link{as.matrix.dna_network_onemode}()},
\code{\link{autoplot.dna_network}},
\code{\link{dna_network}()},
\code{\link{dna_tidygraph}()},
\code{\link{print.dna_network_onemode}()},
\code{\link{print.dna_network_twomode}()}
}
\author{
Philip Leifeld
}
\concept{networks}
