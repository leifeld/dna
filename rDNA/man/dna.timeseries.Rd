\name{dna.timeseries}
\alias{dna.timeseries}
\title{Compute time series statistics}
\description{Compute actor-wise statement frequency time series statistics.}
\usage{
dna.timeseries(infile, persons=FALSE, time.unit="month", 
  ignore.duplicates="article", separate.actors=TRUE, 
  start.date="first", stop.date="last", include.persons="all", 
  include.organizations="all", include.categories="all")
}
\arguments{
\item{infile}{ The input .dna file as a string (i.e., enclosed in quotation marks). If the file is not in the current working directory, specify the path together with the file name. Include the file suffix. Example: \code{sample.dna}. }
\item{persons}{Specify as a boolean value whether persons should be exported. If set to \code{false}, organizations are exported.}
\item{time.unit}{If set to \code{month}, statement frequencies are computed per month. If set to \code{year}, the number of statements per year is computed. The value \code{total} causes \acronym{DNA} to aggregate all frequencies per actor into one column.}
\item{ignore.duplicates}{A duplicate statement is a statement with the same person, organization, category and agreement pattern as another statement. By default, a duplicate statement is omitted if it is found within the same article (option \code{article}). Other options are \code{month} (i.e., duplicate statements are omitted if they occur in the same month) and \code{off} (i.e., all duplicates are retained).}
\item{separate.actors}{If this boolean argument is set to \code{true}, there will be one actor per row in the resulting matrix. If \code{false}, the frequencies of all actors will be aggregated into a single row.}
\item{start.date}{ Only statements after this date will be retained. The start date is a character string of the form \code{dd.mm.yyyy}, where \code{dd} is the two-digit day, \code{mm} the two-digit month and \code{yyyy} the four-digit year. Alternatively, the value \code{first} will use the first date found in the .dna file.}
\item{stop.date}{ Only statements before this date will be retained. The stop date is a character string of the form \code{dd.mm.yyyy}, where \code{dd} is the two-digit day, \code{mm} the two-digit month and \code{yyyy} the four-digit year. Alternatively, the value \code{last} will use the last date found in the .dna file.}
\item{include.persons}{ Specify a list of persons to be included in the statistics. For example, \code{c("person 1", "person 2")}. Note that the names must appear exactly as they are used on the dataset. Alternatively, the string value \code{all} will include all persons in the .dna file.}
\item{include.organizations}{ Specify a list of organizations to be included in the statistics. For example, \code{c("organization 1", "organization 2")}. Note that the names must appear exactly as they are used on the dataset. Alternatively, the string value \code{all} will include all organizations in the .dna file.}
\item{include.categories}{ Specify a list of categories to be included in the statistics. For example, \code{c("category 1", "category 2")}. Note that the concept names must appear exactly as they are used on the dataset. Alternatively, the value \code{all} will include all categories in the .dna file.}
}
\details{
An important piece of information in actor-based content 
analysis is the question how active each actor or actor 
group is over time. This function generates a matrix of 
statement frequencies of all actors, aggregated per actor 
or as a total value and either per year, per month or 
overall.
}
\seealso{
\code{\link{rDNA}}
\code{\link{dna.init}}
\code{\link{dna.gui}}
\code{\link{dna.network}}
}
\author{
Philip Leifeld (\url{http://www.philipleifeld.de})
}
\examples{
download.file(
  "http://www.philipleifeld.de/cms/upload/Downloads/dna-1.24.jar", 
  destfile="dna-1.24.jar", mode="wb")
download.file(
  "http://www.philipleifeld.de/cms/upload/Downloads/sample.dna", 
  destfile="sample.dna", mode="wb")
dna.init("dna-1.24.jar")
time.series <- dna.timeseries("sample.dna")
}
\keyword{Programming|programming|interface}
\keyword{Programming|IO}
