\name{dna.network}
\alias{dna.network}
\title{Get network data}
\description{Convert a .dna file into a sociomatrix and import it into R.}
\usage{
dna.network(infile, algorithm="cooccurrence", agreement="combined", start.date="01.01.1900", stop.date="31.12.2099", two.mode.type="oc", one.mode.type="organizations", via="categories", ignore.duplicates=TRUE, include.isolates=FALSE, normalization=FALSE, window.size=100, step.size=1, exclude.persons=c(""), exclude.organizations=c(""), exclude.categories=c(""))
}
\details{
Specify an input .dna file, specify options for generating a network, 
and transfer the network as a matrix object into \R.
}
\arguments{
\item{infile}{ The input .dna file as a string (i.e., enclosed in quotation marks). If the file is not in the current working directory, specify the path together with the file name. Include the file suffix. Example: \code{sample.dna}. }
\item{algorithm}{ The algorithm which should be used to create the network. Refer to the \acronym{DNA} manual at \url{http://www.philipleifeld.de} for details. Possible values are: \code{affiliation} (for a two-mode network of actors and concepts), \code{cooccurrence} (for an actor or concept co-occurrence/one-mode network), \code{timewindow} (for the time window algorithm) and \code{attenuation} (for the attenuation algorithm). }
\item{agreement}{ The agreement pattern to be used. Must be one of the following: \code{yes}, \code{no}, \code{combined} or \code{conflict}. }
\item{start.date}{ Only statements after this date will be retained. The start date is a character string of the form \code{dd.mm.yyyy}, where \code{dd} is the two-digit day, \code{mm} the two-digit month and \code{yyyy} the four-digit year.}
\item{stop.date}{ Only statements before this date will be retained. The stop date is a character string of the form \code{dd.mm.yyyy}, where \code{dd} is the two-digit day, \code{mm} the two-digit month and \code{yyyy} the four-digit year.}
\item{two.mode.type}{ If the \code{affiliation} algorithm is selected, this argument determines the vertex classes to be used. The following values are possible: \code{oc} (which stands for organizations x categories),  \code{pc} (persons x categories) and \code{po} (persons x organizations). }
\item{one.mode.type}{ If the \code{cooccurrence}, \code{timewindow} or \code{attenuation} algorithm is selected, this argument specifies the vertex class to be used. The following values are possible: \code{organizations} (which stands for organizations x organizations), \code{persons} (persons x persons) or \code{categories} (categories x categories). }
\item{via}{ If the one.mode.type argument is active (i.e., the \code{cooccurrence}, \code{timewindow} or \code{attenuation} algorithm is used), this argument specifies via which variable a co-occurrence network is created. For example, if an organizations x organizations network is created, organizations can either be connected via their shared persons or categories. Valid values are thus \code{persons}, \code{organizations} and \code{categories}, but \strong{not} the vertex type used in the \code{one.mode.type} argument. }
\item{ignore.duplicates}{ A boolean variable indicating whether two statements with the same actor, category, agreement pattern and date should be counted separately during network creation. For example, if a speaker re-iterates the same concepts in the same way over and over again in the same article, each of these statements increases the edge weight between this speaker and other speakers using the same argument if \code{ignore.duplicates} is switched off (i.e., set to \code{FALSE}). }
\item{include.isolates}{ If several time slices are exported, usually the network matrices will have different dimensions. If the \code{include.isolates} argument is set to \code{TRUE}, all actors - even if they are inactive in the current time slice - are included in the matrix. This guarantees that several time slices have the same dimensions and the same order of actors.}
\item{normalization}{ Some actors make statements more frequently than others, and this behavior is caused by their institutional position. These actors are likely to be at the center of the network. If \code{normalization} is set to \code{TRUE}, \acronym{DNA} tries to correct for institutional positions by dividing edge weights by the average total number of statements of both actors involved in an edge. For more details, please refer to the \acronym{DNA} manual at \url{http://www.philipleifeld.de}. }
\item{window.size}{If the \code{timewindow} algorithm is used, the \code{window.size} argument controls the size of the time window. Integer values are possible. Recommended values are somewhere between \code{10} and \code{2000} days, depending on the theory and the dataset. }
\item{step.size}{ If the \code{timewindow} algorithm is used, the \code{step.size} argument controls the rate at which the time window moves, i.e., the number of days by which the window is moved at each step. Using \code{1} day is recommended. For non-overlapping time windows, use the same value as in the window.size argument. }
\item{exclude.persons}{ Specify a list of persons to be excluded from the network. For example, \code{c("person 1", "person 2")}. Note that the names must appear exactly as they are used on the dataset. }
\item{exclude.organizations}{ Specify a list of organizations to be excluded from the network. For example, \code{c("organization 1", "organization 2")}. Note that the names must appear exactly as they are used on the dataset. }
\item{exclude.categories}{ Specify a list of categories to be excluded from the network. For example, \code{c("category 1", "category 2")}. Note that the concept names must appear exactly as they are used on the dataset. }
}
\seealso{
\code{\link{rDNA}}
\code{\link{dna.init}}
\code{\link{dna.gui}}
\code{\link{dna.attributes}}
}
\author{
Philip Leifeld
\url{http://www.philipleifeld.de}
}
\examples{
# download files and initialize DNA:
download.file("http://www.philipleifeld.de/cms/upload/Downloads/dna-1.23.jar", destfile="dna-1.23.jar")
download.file("http://www.philipleifeld.de/cms/upload/Downloads/sample.dna", destfile="sample.dna")
library(rDNA)
dna.init("dna-1.23.jar")

# plot a congruence network using the statnet package:
congruence <- dna.network("sample.dna", exclude.categories="There should be legislation to regulate emissions.")
library(statnet)
congruence.nw <- network(congruence)
plot(congruence.nw, displaylabels=TRUE, label.cex=0.6, pad=0.8)

# do a hierarchical cluster analysis with an affiliation network:
affiliation.yes <- dna.network("sample.dna", algorithm="affiliation", agreement="yes", include.isolates=TRUE)
affiliation.no <- dna.network("sample.dna", algorithm="affiliation", agreement="no", include.isolates=TRUE)
affiliation <- cbind(affiliation.yes, affiliation.no)
affiliation <- affiliation[rowSums(affiliation) > 0,] #remove isolates
distances <- dist(affiliation, method="binary")
clustering <- hclust(distances)
plot(clustering)
}
\keyword{Programming|programming|interface}
\keyword{Programming|IO}
