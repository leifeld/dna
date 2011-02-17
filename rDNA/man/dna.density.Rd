\name{dna.density}
\alias{dna.density}
\title{Compute density for matrices}
\description{Compute the within- and between-block density for weighted or binary network matrices.}
\usage{
dna.density(network.matrix, partitions="", weighted=FALSE, 
  verbose=FALSE)
}
\arguments{
\item{network.matrix}{A co-occurrence or adjacency network which is saved as a matrix object. It should have the correct row and column labels assigned, otherwise partitions will not work.}
\item{partitions}{An optional argument where the affiliation of row and column vertices to groups can be specified. The vertex types or groups of the row labels in the network.matrix should be submitted as a one-column matrix with meaningful row names. The row and column names of the network.matrix are looked up in the partitions if specified. The output will be a group x group density table for the different partitions. For example, if row actor 1 in the network.matrix belongs to group A, there should be an entry in the partitions matrix with the row name "actor 1" and the value "group A".}
\item{weighted}{A boolean variable indicating whether the weight of an edge should be counted when the density is calculated.}
\item{verbose}{A boolean variable indicating whether the results shall also be printed to the screen.}
}
\details{
This method computes the density of network matrices. It works 
for weighted or binary networks, and it can return within- and 
between-block density if actor types or the affiliation of actors 
to theoretical groups is given. For example, in a network of 
political actors, each actor may have a certain actor type: 
interest group, governmental actor, party etc. If the type of 
each row and column actor can be looked up in the one-column 
matrix given by the optional partitions argument, the result of 
the dna.density method will be a type x type matrix containing 
the densities within each actor type and between the various 
actor types. Within- and between block density for theoretical 
blocks is elsewhere referred to as node-mixing.
}
\seealso{
\code{\link{dna.attributes}}
\code{\link{dna.network}}
}
\author{
Philip Leifeld (\url{http://www.philipleifeld.de})
}
