
# some settings
dnaEnvironment <- new.env(hash = TRUE, parent = emptyenv())
# more settings which quiet concerns of R CMD check about ggplot and dplyr pipelines
if(getRversion() >= "2.15.1")  utils::globalVariables(c("rn",
                                                        "cols3",
                                                        "labels_short",
                                                        "leaf",
                                                        "x",
                                                        "y"))


#' Add a document to the DNA database
#' 
#' Add a new document to the DNA database.
#' 
#' The \code{dna_addDocument} function can add new documents to an existing DNA 
#' database. The user supplies a \link{dna_connection} object as well as various 
#' details about the document, for example the title, text, date etc. The date 
#' can be supplied either as a \link{POSIXct} object or as an integer value 
#' containing millisecond since 1970-01-01. The document ID will be 
#' automatically generated and can be returned if \code{returnID} is set to 
#' \code{TRUE}.
#' 
#' @param connection A \code{dna_connection} object created by the
#'     \code{dna_connection} function.
#' @param title A character object containing the title of the new document.
#' @param text A character object containing the text of the new document. Line 
#'     breaks can be included as \code{"\\n"}.
#' @param coder An integer value indicating which coder created the document.
#' @param author A character object containing the author of the document.
#' @param source A character object containing the source of the document.
#' @param section A character object containing the section of the document.
#' @param notes A character object containing notes about the document.
#' @param type A character object containing the type of the document.
#' @param date A \code{POSIXct} object containing the date/time stamp of the 
#'     document. Alternatively, the date/time can be supplied as an integer 
#'     value indicating the milliseconds since the start of 1970-01-01.
#' @param returnID Return the ID of the newly created document as a numeric 
#'     value?
#' @param verbose Print details?
#' 
#' @author Philip Leifeld
#' @export
dna_addDocument <- function(connection, 
                            title = "", 
                            text = "", 
                            coder = 1, 
                            author = "", 
                            source = "", 
                            section = "", 
                            notes = "", 
                            type = "", 
                            date = Sys.time(), 
                            returnID = FALSE, 
                            verbose = TRUE) {
  
  if (!is.character(title)) {
    stop("The title must be provided as a character object.")
  }
  if (!is.character(text)) {
    stop("The text must be provided as a character object.")
  }
  if (!is.integer(coder)) {
    if (is.numeric(coder)) {
      coder <- as.integer(coder)
    } else {
      stop("The coder must be provided as a numeric object (see dna_getCoders).")
    }
  }
  if (!is.character(author)) {
    stop("The author must be provided as a character object.")
  }
  if (!is.character(source)) {
    stop("The source must be provided as a character object.")
  }
  if (!is.character(section)) {
    stop("The section must be provided as a character object.")
  }
  if (!is.character(notes)) {
    stop("The notes must be provided as a character object.")
  }
  if (!is.character(type)) {
    stop("The type must be provided as a character object.")
  }
  if (any(class(date) %in% c("POSIXct", "POSIXt"))) {
    dateLong <- .jlong(as.integer(date) * 1000)
  } else if (is.numeric(date)) {
    dateLong <- .jlong(as.integer(date))
  } else {
    stop("The document date must be provided as a POSIXct object or as a numeric value indicating milliseconds since 1970-01-01.")
  }
  
  id <- .jcall(connection$dna_connection, 
               "I", 
               "addDocument", 
               title, 
               text, 
               coder, 
               author, 
               source, 
               section, 
               notes, 
               type, 
               dateLong)
  if (verbose == TRUE) {
    cat("A new document with ID", id, "was added to the database.")
  }
  if (returnID == TRUE) {
    return(id)
  }
}


#' Retrieve attributes from DNA's attribute manager
#'
#' Retrieve attributes for a given statement type and variable.
#'
#' For any variable in DNA (e.g., organization or concept), the attribute
#' manager in DNA stores four attributes, or meta-variables, for each value.
#' For example, an organization can be associated with a color, a type,
#' an alias, and notes. The \code{dna_attributes} function serves to retrieve
#' these attributes for all values of a given variable from DNA. To be sure
#' that the attributes are retrieved for the right variable, the user also
#' has to provide the statement type in which the variable is nested. Note that
#' the \code{dna_attributes} function returns the attributes for all values of
#' the respective variable (similar to the \code{includeIsolates = TRUE}
#' argument of the \code{dna_network} function. The attributes are returned as
#' a data frame with the ID of the value as the first column, the actual value
#' as the second column, the color as the third column, type as the fourth
#' column, alias as fifth, notes as sixth, and the number of occurrences of the
#' value as the seventh column. The ninth column indicates whether the value
#' is used anywhere in the dataset, and the tenth and last column indicates if
#' the value is contained in the current connection.
#'
#' @param connection A \code{dna_connection} object created by the
#'     \code{dna_connection} function.
#' @param statementType The name of the statement type in which the variable
#'     of interest is nested. For example, \code{"DNA Statement"}.
#' @param variable The variable for which the attributes should be retrieved.
#'     For example, \code{"organization"} or \code{"concept"}.
#' @param values An optional vector of value names for which the attributes
#'     should be retrieved. If this is \code{NULL}, all attributes will be
#'     retrieved in alphabetical order, as in the \link{dna_network} function.
#'
#' @examples
#' \dontrun{
#' dna_downloadJar()
#' dna_init("dna-2.0-beta21.jar")
#' conn <- dna_connection(dna_sample())
#' at <- dna_attributes(conn, "DNA Statement", "organization")
#' at
#' at2 <- dna_attributes(conn, "DNA Statement", "organization",
#'                       c("Senate", "Sierra Club"))
#' at2
#' }
#' @export
dna_attributes <- function(connection,
                           statementType = "DNA Statement",
                           variable = "organization",
                           values = NULL) {
 
  if (is.null(values)) {
    values <- character(0)
  } else if (class(values) != "character") {
    stop("'values' must be a character vector or NULL.")
  }
 
  .jcall(connection$dna_connection,
         "V",
         "rAttributes",
         variable,
         statementType,
         values)
  dat <- data.frame(id = .jcall(connection$dna_connection, "[I", "getAttributeIds"),
                    value = .jcall(connection$dna_connection, "[S", "getAttributeValues"),
                    color = .jcall(connection$dna_connection, "[S", "getAttributeColors"),
                    type = .jcall(connection$dna_connection, "[S", "getAttributeTypes"),
                    alias = .jcall(connection$dna_connection, "[S", "getAttributeAlias"),
                    note = .jcall(connection$dna_connection, "[S", "getAttributeNotes"),
                    frequency = .jcall(connection$dna_connection, "[I", "getAttributeFrequencies", variable),
                    "in dataset" = .jcall(connection$dna_connection, "[Z", "getAttributeInDataset", statementType),
                    "in network" = .jcall(connection$dna_connection, "[Z", "getAttributeInNetwork", statementType),
                    check.names = FALSE
  )
  return(dat)
}


#' Cluster network from a DNA connection
#'
#' Clustering methods for DNA connections.
#'
#' Perform a cluster analysis based on a DNA connection. Clustering is performed
#' on a collated two-mode network for cluster methods "ward.D", "ward.D2",
#' "single", "complete", "average", "mcquitty", "median" and "centroid" or on a
#' one-mode network with the cluster methods "edge_betweenness", "leading_eigen"
#' and "walktrap" from the \link{igraph} package. The collated two-mode network
#' is constructed by retrieving individual networks for each of the qualifiers
#' levels and combining the results. You can look at this network with
#' View(clust$network) (with "clust" being the outcome of a call to
#' dna_cluster()).
#'
#' @param connection A \code{dna_connection} object created by the
#'   \link{dna_connection} function.
#' @param variable The first variable for network construction  (see
#'   \link{dna_network}). The second one defaults to "concept" but can be
#'   provided via ... if necessary (see \code{variable2} in
#'   \code{dna_connection}).
#' @param duplicates Setting for excluding duplicate statements before network
#'   construction (for details see \link{dna_network}. If exclusion of
#'   duplicates results in a binary matrix, \link[vegan]{vegdist} will be used
#'   instead of \link[stats]{dist} to calculate the dissimilarity matrix.
#' @param clust.method The agglomeration method to be used. When set to
#'   "ward.D", "ward.D2", "single", "complete", "average", "mcquitty", "median"
#'   or "centroid" the respective methods from \link[stats]{hclust} will be
#'   used. When set to "edge_betweenness", "leading_eigen" or "walktrap"
#'   \link[igraph]{cluster_edge_betweenness},
#'   \link[igraph]{cluster_leading_eigen} or \link[igraph]{cluster_walktrap}
#'   respectively, will be used for clustering.
#' @param attribute1,attribute2 Which attribute of variable from DNA should be
#'   used to assign colours? There are two sets of colours saved in the
#'   resulting object as \link{dna_plotDendro} has two graphical elements to
#'   distinguish between values: leaf_colours and leaf_ends. Possible values are
#'   "id", "value", "color", "type", "alias" and "note".
#' @param cutree.k,cutree.h If cutree.k or cutree.h are provided, the tree from
#'   hierarchical clustering is cut into several groups. See $k$ and $h$ in
#'   \link[stats]{cutree} for details.
#' @param ... Additional arguments passed to \link{dna_network}. This is
#'   especially useful to set qualifier (defaults to "agreement") and
#'   normalization (defaults to "no") if non-default values are needed for
#'   clustering. Some other options, like qualifierAggregation are turned off
#'
#' @examples
#' \dontrun{
#' dna_downloadJar()
#' dna_init("dna-2.0-beta21.jar")
#' conn <- dna_connection(dna_sample())
#'
#' clust.l <- dna_cluster(connection,
#'                        mds = TRUE)
#'
#' dna_plotDendro(clust.l)
#' dna_plotHeatmap(clust.l)
#' dna_plotMDS(clust.l,
#'             jitter = c(0.5, 0.7))
#'
#' }
#' @author Johannes B. Gruber
#' @export
#' @importFrom vegan vegdist
#' @importFrom stats setNames dist hclust cutree as.hclust
#' @importFrom igraph graph_from_adjacency_matrix cluster_leading_eigen
#'   cluster_walktrap E
#' @importFrom dplyr summarise group_by_all
#' @importFrom MASS isoMDS
#' @importFrom cluster pam
#' @importFrom splitstackshape cSplit
#' @importFrom grDevices chull
#' @importFrom utils packageVersion capture.output
dna_cluster <- function(connection,
                        variable = "organization",
                        duplicates = "document",
                        clust.method = "ward.D2",
                        attribute1 = "color",
                        attribute2 = "value",
                        cutree.k = NULL,
                        cutree.h = NULL,
                        ...) {
  dots <- list(...)
  if ("qualifierAggregation" %in% names(dots)) {
    message("\"qualifierAggregation\" can't be changed in dna_cluster. The option is ignored.")
    dots["qualifierAggregation"] <- NULL
  }
  if ("normalization" %in% names(dots)){
    normalization_onemode <- ifelse(dots[["normalization"]] %in% 
                                      c("no", "average", "Jaccard", "cosine"),
                                    dots[["normalization"]],
                                    "no")
    normalization_twomode <-   ifelse(dots[["normalization"]] %in% 
                                        c("no", "activity", "prominence"),
                                      dots[["normalization"]],
                                      "no")
    dots["normalization"] <- NULL
  } else {
    normalization_onemode <- "no"
    normalization_twomode <- "no"
  }
  if ("excludeValues" %in% names(dots)){
    excludeValues <- dots["excludeValues"][[1]]
    dots["excludeValues"] <- NULL
  } else {
    excludeValues <- list()
  }
  if (!"statementType" %in% names(dots)) {
    dots$statementType <- "DNA Statement"
  }
  lvls <- do.call(dna_network,
                  c(list(connection = connection,
                         networkType = "eventlist",
                         excludeValues = excludeValues,
                         verbose = FALSE
                  ), dots))
  if ("qualifier" %in% names(dots)) { #
    qualifier <- dots[["qualifier"]]
    dots[["qualifier"]] <- NULL
  } else {
    qualifier <- "agreement"
  }
  if (qualifier %in% names(excludeValues)){
    excl <- unlist(unname(excludeValues[qualifier]))
    excludeValues[qualifier] <- NULL
  }
  lvls <- unique(lvls[, qualifier])
  if (exists("excl")) {
    lvls <- lvls[!lvls %in% excl]
    if (length(lvls) < 1){
      stop (paste0(
        "You excluded all levels of \"", qualifier,
        "\". Computation not possible."
      ))
    }
  }
  dta <- lapply(lvls, function(l){
    excludeVals <- c(stats::setNames(list(lvls[!lvls == l]),
                                    nm = qualifier),
                    excludeValues)
    nw <- do.call(dna_network,
                  c(list(connection = connection,
                         networkType = "twomode",
                         variable1 = variable,
                         normalization = normalization_twomode,
                         isolates = TRUE,
                         duplicates = duplicates,
                         qualifier = qualifier,
                         excludeValues = excludeVals,
                         invertValues = FALSE,
                         verbose = FALSE)
                    , dots)
    )
    colnames(nw) <- paste(colnames(nw), "-", l)
    return(nw)
  })
  dta <- rapply(dta, f = function(x) ifelse(is.nan(x),0,x), how="replace" )
  dta <- do.call("cbind", dta)
  dta <- dta[rowSums(dta) > 0, ]
  dta <- dta[, colSums(dta) > 0]
  nw <- do.call(dna_network,
                c(list(connection = connection,
                       networkType = "onemode",
                       qualifierAggregation = "subtract",
                       normalization = normalization_onemode,
                       variable1 = variable,
                       isolates = FALSE,
                       duplicates = duplicates,
                       qualifier = qualifier,
                       verbose = FALSE)
                  , dots)
  )
  nw <- ifelse(test = nw < 0,
               yes = 0,
               no = nw)
  nw2 <- igraph::graph_from_adjacency_matrix(nw,
                                             mode = "undirected",
                                             weighted = "weight",
                                             diag = FALSE,
                                             add.colnames = NULL)
  cluster_louvain <- igraph::cluster_louvain(nw2,
                                             weights = igraph::E(nw2)$weight)
  if (clust.method %in% c("ward.D",
                          "ward.D2",
                          "single",
                          "complete",
                          "average",
                          "mcquitty",
                          "median",
                          "centroid")) {
    if (all(dta %in% c(0, 1))){ # test if dta is binary
      d <-  vegan::vegdist(dta, method = "jaccard")
    } else {
      d <-  dist(dta, method = "euclidean")
    }
    hc <- hclust(d, method = clust.method)
    hc$activities <- unname(rowSums(dta))
  } else if (clust.method %in% c("edge_betweenness",
                                 "leading_eigen",
                                 "walktrap")) {
    if (clust.method == "edge_betweenness") {
      hc <- igraph::cluster_edge_betweenness(nw2,
                                             weights = igraph::E(nw2)$weight,
                                             edge.betweenness = TRUE,
                                             merges = TRUE,
                                             bridges = TRUE,
                                             modularity = FALSE,
                                             membership = FALSE)
    } else if (clust.method == "leading_eigen") {
      hc <- igraph::cluster_leading_eigen(nw2,
                                          steps = -1,
                                          weights = igraph::E(nw2)$weight,
                                          start = NULL,
                                          callback = NULL,
                                          extra = NULL)
    } else if (clust.method == "walktrap") {
      hc <- igraph::cluster_walktrap(nw2,
                                     weights = igraph::E(nw2)$weight,
                                     steps = 4,
                                     merges = TRUE,
                                     modularity = FALSE,
                                     membership = TRUE)
    }
    hc <- as.hclust(hc, hang = -1, use.modularity = FALSE)
    hc$method <- clust.method
    hc$activities <- unname(rowSums(nw))
  } else {
    stop(paste0("Please provide a valid clust.method: 'ward.D', 'ward.D2',",
               " 'single', 'complete', 'average', 'mcquitty', 'median', 'cen",
               "troid', 'edge_betweenness', 'leading_eigen', 'walktrap'"))
  }
  if (!is.null(c(cutree.k, cutree.h))){
    hc$group <- cutree(hc, k = cutree.k, h = cutree.h)
  }
  col <- dna_getAttributes(connection = connection,
                           statementType = dots$statementType,
                           variable = variable,
                           values = NULL)
  hc$attribute1 <- col[, attribute1][match(hc$labels, col$value)]
  hc$attribute2 <- col[, attribute2][match(hc$labels, col$value)]
  if (any(!is.null(c(cutree.h, cutree.k)))) {
    hc$group <- paste("Group", hc$group)
  } else {
    hc$group <- paste("Group", rep_len(0, length.out = length(hc$labels)))
  }
  if (length(cutree.k) + length(cutree.h) > 0) {
    attr(hc, "cut") <- c("cutree.k" = cutree.k, "cutree.h" = cutree.h)
  } else {
    attr(hc, "cut") <- NA
  }
  nw <- do.call(dna_network,
                c(list(connection = connection,
                       networkType = "twomode",
                       variable1 = variable,
                       normalization = "no",
                       isolates = TRUE,
                       duplicates = duplicates,
                       qualifier = qualifier,
                       verbose = FALSE,
                       qualifierAggregation = "combine")
                  , dots))
  if (any(duplicated(nw))){
    . <- data.frame(nw, check.names = FALSE)
    . <- dplyr::group_by_all(.)
    .$rn <- row.names(.)
    . <- dplyr::summarise(., rowname = paste(rn, collapse = "|"))
    nw <- data.frame(., stringsAsFactors = FALSE)
    row.names(nw) <- nw$rowname
    nw <- nw[, !colnames(nw) == "rowname"]
  }
  if (all(nw %in% c(0, 1))){
    d <-  vegan::vegdist(nw, method = "jaccard")
  } else {
    d <-  dist(nw, method = "euclidean")
  }
  if (length(d) < 2) {
    stop("Clustering cannot be performed on less than three actors.")
  }
  mds <- MASS::isoMDS(d, trace = FALSE)
  k.best <- which.max(sapply(seq(from = 2, to = ncol(nw), by = 1), function(i){
    cluster::pam(d, k = i)$silinfo$avg.width
  }))
  stress <- mds$stress
  mds <- data.frame(variable = row.names(mds$points),
                    Dimension_1 = mds$points[, 1],
                    Dimension_2 = mds$points[, 2],
                    cluster_pam = as.factor(cluster::pam(d, k = k.best)[["clustering"]]),
                    cluster_louvain = as.factor(cluster_louvain$memberships)[match(row.names(mds$points),
                                                                                    cluster_louvain$names)])
 
  if (any(grepl("|", mds$variable, fixed = TRUE))) {
    mds <- splitstackshape::cSplit(mds, "variable", "|", "long")
  }
  hc$mds <- data.frame(mds[!duplicated(mds$variable, fromLast = TRUE), ])
  attributes(hc$mds)$stress <- stress
  hc$call <-  match.call()
  attr(hc, "colours") <- c("attribute1" = attribute1, "attribute2" = attribute2)
  class(hc) <- c("dna_cluster", class(hc))
  hc$network <- dta
  return(hc)
}


#' Establish a database connection
#'
#' Connect to a local .dna file or remote mySQL DNA database.
#'
#' Before any data can be loaded from a database, a connection with
#' the database must be established. The \code{dna_connection}
#' function establishes a database connection and loads the documents
#' and statements into memory for further processing.
#'
#' @param infile The file name of the .dna database or the URL of the mySQL
#'     database to load
#' @param login The user name for accessing the database (only applicable
#'     to remote mySQL databases; can be \code{NULL} if a local .dna file
#'     is used).
#' @param password The password for accessing the database (only applicable
#'     to remote mySQL databases; can be \code{NULL} if a local .dna file
#'     is used).
#' @param verbose Print details the number of documents and statements after
#'   loading the database?
#'
#' @examples
#' \dontrun{
#' dna_downloadJar()
#' dna_init("dna-2.0-beta21.jar")
#' dna_connection(dna_sample())
#' }
#' @export
dna_connection <- function(infile, login = NULL, password = NULL, verbose = TRUE) {
  if (is.null(login) || is.null(password)) {
    export <- .jnew("dna.export/ExporterR", "sqlite", infile, "", "", verbose)
  } else {
    export <- .jnew("dna.export/ExporterR", "mysql", infile, login, password, verbose)
  }
  obj <- list(dna_connection = export)
  class(obj) <- "dna_connection"
  if (verbose == TRUE) {
    print(obj)
  }
  return(obj)
}


#' Download the binary DNA JAR file
#'
#' Downloads the newest released DNA JAR file necessary for running
#' \code{dna_init}.
#'
#' This simple function downloads the DNA JAR from the latest release.
#'
#' @param filename Name of the downloaded Jar.
#' @param filepath Download path. Defaults to working directory.
#' @param force Logical. Should the file be overwritten if it already exists.
#'
#' @examples
#' \dontrun{
#' dna_downloadJar()
#' }
#' @export
#' @importFrom utils download.file
dna_downloadJar <- function(filename = "dna-2.0-beta21.jar",
                            filepath = character(),
                            force = FALSE) {
  # temporary fix until next release
  url <- paste0("https://github.com/leifeld/dna/raw/master/manual/dna-2.0-beta21.jar")
  if (any(!file.exists(paste0(filepath, filename)), force)) {
    download.file(url = url,
                  destfile = paste0(filepath, filename),
                  mode = "wb",
                  cacheOK = FALSE,
                  extra = character())
  } else {
    warning("Newest DNA JAR file already exists. Try \"force = TRUE\" to download it anyway")
  }
}


#' Retrieve a dataframe with attributes from a DNA connection
#' 
#' Retrieve a dataframe with attributes from a DNA connection.
#' 
#' Attributes are metadata for the values saved in a variable. For example, an 
#' organization can be associated with a certain color or type. This function 
#' serves to retrieve these attributes for the values of a given variable from 
#' DNA as a dataframe. The user supplies a DNA connection and specifies the 
#' variable for which the attributes should be extracted as well as the 
#' statement type in which the variable is defined (by statement type ID). 
#' Optionally, a vector of values for which the attributes should be extracted 
#' can be specified; this limits the vector to the values specified. The 
#' resulting dataframe contains an additional column for the frequency with 
#' which the respective value is used in statements across the database. The 
#' \code{dna_getAttributes} function is similar to the \link{dna_attributes} 
#' function and takes the same arguments, but there are subtle differences: 
#' \link{dna_attributes} extracts attributes and frequencies only for the 
#' statements defined in a network export, i.e., after filtering statements for 
#' time, duplicates etc. This enables the user to match actor types, colours etc. 
#' more easily to network data.
#' 
#' @param connection A \code{dna_connection} object created by the
#'     \code{dna_connection} function.
#' @param statementType The ID of the statement type (as an integer) or the name 
#'     of the statement type (as a character object) in which the variable is 
#'     defined.
#' @param variable The name of the variable for which attribute data should be 
#'     retrieved, for example \code{"organization"} or \code{"concept"}.
#' @param values An optional character vector of entries to which the dataframe 
#'     should be limited. If all values and attributes should be retrieved for a 
#'     given variable, this can be \code{NULL} or a character vector of length 
#'     0.
#' 
#' @examples
#' \dontrun{
#' dna_init("dna-2.0-beta21.jar")
#' conn <- dna_connection(dna_sample())
#' attributes <- dna_getAttributes(statementType = 1, 
#'                                 variable = "organization", 
#'                                 values = c("Alliance to Save Energy", 
#'                                            "Senate", 
#'                                            "Sierra Club"))
#' }
#' 
#' @author Philip Leifeld
#' @export
dna_getAttributes <- function(connection, 
                              statementType = 1, 
                              variable = "organization", 
                              values = NULL) {
  
  if ((!is.numeric(statementType) && !is.character(statementType)) || length(statementType) > 1) {
    stop("'statementType' must be a single integer or character value referencing the ID or name of the statement type in which the variable is defined.")
  }
  if (!is.integer(statementType) && is.numeric(statementType)) {
    statementType <- as.integer(statementType)
  }
  if (!is.character(variable) || length(variable) > 1) {
    stop("'variable' must be a single character object referencing the variable for which the attribute data should be retrieved.")
  }
  if (is.null(values)) {
    values <- character(0)
  }
  if (!is.character(values)) {
    stop("'values' must be NULL or a (possibly empty) character vector.")
  }
  
  attributes <- .jcall(connection$dna_connection, 
                       "[Ljava/lang/Object;", 
                       "getAttributes", 
                       statementType, 
                       variable, 
                       values)
  
  names(attributes) <- c("id", 
                         "value", 
                         "color", 
                         "type", 
                         "alias", 
                         "notes", 
                         "frequency")
  
  attributes <- lapply(attributes, .jevalArray)
  attributes <- as.data.frame(attributes, stringsAsFactors = FALSE)
  return(attributes)
}


#' Retrieve a dataframe with documents from a DNA connection
#' 
#' Retrieve a dataframe with all documents from a DNA connection.
#' 
#' This function creates a dataframe with one row per document and contains 
#' columns for the document ID, title, the complete text, and all meta data. The 
#' dataframe can then be manually manipulated and returned to the DNA database 
#' through the \link{dna_setDocuments} function.
#' 
#' @param connection A \code{dna_connection} object created by the
#'     \code{dna_connection} function.
#' 
#' @examples
#' \dontrun{
#' dna_init("dna-2.0-beta21.jar")
#' conn <- dna_connection(dna_sample())
#' documents <- dna_getDocuments(conn)
#' documents$title[1] <- "New title for first document"
#' documents$notes[3] <- "Added a note via rDNA."
#' documents <- documents[, -5]  # Removing the fifth document
#' dna_setDocuments(conn, documents, simulate = TRUE)  # apply changes
#' }
#' 
#' @author Philip Leifeld
#' @export
dna_getDocuments <- function(connection) {
  documents <- .jcall(connection$dna_connection, 
                      "[Ljava/lang/Object;", 
                      "getDocuments")
  names(documents) <- c("id", 
                        "title", 
                        "text", 
                        "coder", 
                        "author", 
                        "source", 
                        "section", 
                        "notes", 
                        "type", 
                        "date")
  documents <- lapply(documents, .jevalArray)
  documents$date <- as.POSIXct(documents$date / 1000, origin = "1970-01-01")
  documents <- as.data.frame(documents, stringsAsFactors = FALSE)
  return(documents)
}


#' Open the DNA GUI
#'
#' Start DNA and optionally load a database.
#'
#' Start the DNA GUI. Optionally load a .dna database or a mySQL online
#' database upon start-up of the GUI. This function is useful to use
#' DNA on the fly to quickly recode statements or look something up.
#'
#' @param infile The file name of the .dna database or the URL of the mySQL
#'     database to load upon start-up of the GUI
#' @param javapath The path to the \code{java} command. This may be useful if
#'     the CLASSPATH is not set and the java command can not be found. Java
#'     is necessary to start the DNA GUI.
#' @param memory The amount of memory in megabytes to allocate to DNA, for
#'     example \code{1024} or \code{4096}.
#' @param verbose Print details and error messages from the call to DNA?
#'
#' @examples
#' \dontrun{
#' dna_init("dna-2.0-beta21.jar")
#' dna_gui()
#' }
#' @export
dna_gui <- function(infile = NULL,
                    javapath = NULL,
                    memory = 1024,
                    verbose = TRUE) {
  djs <- dnaEnvironment[["dnaJarString"]]
  if (is.null(djs)) {
    stop(paste0(djs, " could not be located in directory ", getwd(), "."))
  }
  if(!is.null(infile)) {
    if (!file.exists(infile)) {
      stop(
        if (grepl("/", infile, fixed = TRUE)) {
          paste0("infile ", infile, " could not be located.")
        } else {
          paste0("infile ", 
                 infile, 
                 " could not be located in working directory ", 
                 getwd(), ".")
        }
      )
    }
  }
  if (is.null(infile)) {
    f <- ""
  } else {
    f <- paste0(" \"", infile, "\"")
  }
  if (is.null(javapath)) {
    jp <- "java"
  } else if (grepl("/$", javapath)) {
    jp <- paste0(javapath, "java")
  } else {
    jp <- paste0(javapath, "/java")
  }
  system(paste0(jp, " -jar -Xmx", memory, "M ", djs, f), intern = !verbose)
}


#' Initialize the connection with DNA
#'
#' Establish a connection between \pkg{rDNA} and the DNA software.
#'
#' To use \pkg{rDNA}, DNA first needs to be initialized. This means that
#' \pkg{rDNA} needs to be told where the DNA executable file, i.e., the JAR
#' file, is located. When the \code{dna_init} function is used, the connection
#' to the DNA software is established, and this connection is valid for the
#' rest of the \R session. To initialize a connection with a different DNA
#' version or path, the \R session would need to be restarted first.
#'
#' @param jarfile The file name of the DNA jar file, e.g.,
#'     \code{"dna-2.0-beta21.jar"}.
#' @param memory The amount of memory in megabytes to allocate to DNA, for
#'     example \code{1024} or \code{4096}.
#'
#' @examples
#' \dontrun{
#' dna_downloadJar()
#' dna_init("dna-2.0-beta21.jar")
#' }
#' @export
#' @import rJava
dna_init <- function(jarfile = "dna-2.0-beta21.jar", memory = 1024) {
  if (!file.exists(jarfile)) {
    stop(if (grepl("/", jarfile, fixed = TRUE)){
      paste0("jarfile \"", jarfile, "\" could not be located.")
    } else {
      paste0(
        "jarfile \"",
        jarfile,
        "\" could not be located in working directory \"",
        getwd(),
        "\"."
      )
    })
  }
  assign("dnaJarString", jarfile, pos = dnaEnvironment)
  message(paste("Jar file:", dnaEnvironment[["dnaJarString"]]))
  .jinit(dnaEnvironment[["dnaJarString"]],
         force.init = TRUE,
         parameters = paste0("-Xmx", memory, "m"))
}


#' Compute and retrieve a network
#'
#' Compute and retrieve a network from DNA.
#'
#' This function serves to compute a one-mode or two-mode network or an event
#' list in DNA and retrieve it as a matrix or data frame, respectively. The
#' arguments resemble the export options in DNA. It is also possible to compute
#' a temporal sequence of networks using the moving time window approach, in
#' which case the networks are retrieved as a list of matrices.
#'
#' @param connection A \code{dna_connection} object created by the
#'     \code{dna_connection} function.
#' @param networkType The kind of network to be computed. Can be
#'     \code{"twomode"}, \code{"onemode"}, or \code{"eventlist"}.
#' @param statementType The name of the statement type in which the variable
#'     of interest is nested. For example, \code{"DNA Statement"}.
#' @param variable1 The first variable for network construction. In a one-mode
#'     network, this is the variable for both the rows and columns. In a
#'     two-mode network, this is the variable for the rows only. In an event
#'     list, this variable is only used to check for duplicates (depending on
#'     the setting of the \code{duplicate} argument).
#' @param variable1Document A boolean value indicating whether the first
#'     variable is at the document level (i.e., \code{"author"},
#'     \code{"source"}, \code{"section"}, or \code{"type"}).
#' @param variable2 The second variable for network construction. In a one-mode
#'     network, this is the variable over which the ties are created. For
#'     example, if an organization x organization network is created, and ties
#'     in this network indicate co-reference to a concept, then the second
#'     variable is the \code{"concept"}. In a two-mode network, this is the
#'     variable used for the columns of the network matrix. In an event list,
#'     this variable is only used to check for duplicates (depending on the
#'     setting of the \code{duplicate} argument).
#' @param variable2Document A boolean value indicating whether the second
#'     variable is at the document level (i.e., \code{"author"},
#'     \code{"source"}, \code{"section"}, or \code{"type"}).
#' @param qualifier The qualifier variable. In a one-mode network, this
#'     variable can be used to count only congruence or conflict ties. For
#'     example, in an organization x organization network via common concepts,
#'     a binary \code{"agreement"} qualifier could be used to record only ties
#'     where both organizations have a positive stance on the concept or where
#'     both organizations have a negative stance on the concept. With an
#'     integer qualifier, the tie weight between the organizations would be
#'     proportional to the similarity or distance between the two organizations
#'     on the scale of the integer variable. In a two-mode network, the
#'     qualifier variable can be used to retain only positive or only negative
#'     statements or subtract negative from positive mentions. All of this
#'     depends on the setting of the \code{qualifierAggregation} argument. For
#'     event lists, the qualifier variable is only used for filtering out
#'     duplicates (depending on the setting of the \code{duplicate} argument.
#' @param qualifierAggregation The aggregation rule for the \code{qualifier}
#'     variable. In one-mode networks, this must be \code{"ignore"} (for
#'     ignoring the qualifier variable), \code{"congruence"} (for recording a
#'     network tie only if both nodes have the same qualifier value in the
#'     binary case or for recording the similarity between the two nodes on the
#'     qualifier variable in the integer case), \code{"conflict"} (for
#'     recording a network tie only if both nodes have a different qualifier
#'     value in the binary case or for recording the distance between the two
#'     nodes on the qualifier variable in the integer case), or
#'     \code{"subtract"} (for subtracting the conflict tie value from the
#'     congruence tie value in each dyad). In two-mode networks, this must be
#'     \code{"ignore"}, \code{"combine"} (for creating multiplex combinations,
#'     e.g., 1 for positive, 2 for negative, and 3 for mixed), or
#'     \code{subtract} (for subtracting negative from positive ties). In event
#'     lists, this setting is ignored.
#' @param normalization Normalization of edge weights. Valid settings for
#'     one-mode networks are \code{"no"} (for switching off normalization),
#'     \code{"average"} (for average activity normalization), \code{"Jaccard"}
#'     (for Jaccard coefficient normalization), and \code{"cosine"} (for
#'     cosine similarity normalization). Valid settings for two-mode networks
#'     are \code{"no"}, \code{"activity"} (for activity normalization), and
#'     \code{"prominence"} (for prominence normalization).
#' @param isolates Should all nodes of the respective variable be included in
#'     the network matrix (\code{isolates = TRUE}), or should only those nodes
#'     be included that are active in the current time period and are not
#'     excluded (\code{isolates = FALSE})?
#' @param duplicates Setting for excluding duplicate statements before network
#'     construction. Valid settings are \code{"include"} (for including all
#'     statements in network construction), \code{"document"} (for counting
#'     only one identical statement per document), \code{"week"} (for counting
#'     only one identical statement per calendar week), \code{"month"} (for
#'     counting only one identical statement per calendar month), \code{"year"}
#'     (for counting only one identical statement per calendar year), and
#'     \code{"acrossrange"} (for counting only one identical statement across
#'     the whole time range).
#' @param start.date The start date for network construction in the format
#'     "dd.mm.yyyy". All statements before this date will be excluded.
#' @param start.time The start time for network construction on the specified
#'     \code{start.date}. All statements before this time on the specified date
#'     will be excluded.
#' @param stop.date The stop date for network construction in the format
#'     "dd.mm.yyyy". All statements after this date will be excluded.
#' @param stop.time The stop time for network construction on the specified
#'     \code{stop.date}. All statements after this time on the specified date
#'     will be excluded.
#' @param timewindow Possible values are \code{"no"}, \code{"events"},
#'     \code{"seconds"}, \code{"minutes"}, \code{"hours"}, \code{"days"},
#'     \code{"weeks"}, \code{"months"}, and \code{"years"}. If \code{"no"} is
#'     selected (= the default setting), no time window will be used. If any of
#'     the time units is selected, a moving time window will be imposed, and
#'     only the statements falling within the time period defined by the window
#'     will be used to create the network. The time window will then be moved
#'     forward by one time unit at a time, and a new network with the new time
#'     boundaries will be created. This is repeated until the end of the overall
#'     time span is reached. All time windows will be saved as separate
#'     networks in a list. The duration of each time window is defined by the
#'     \code{windowsize} argument. For example, this could be used to create a
#'     time window of 6 months which moves forward by one month each time, thus
#'     creating time windows that overlap by five months. If \code{"events"} is
#'     used instead of a natural time unit, the time window will comprise
#'     exactly as many statements as defined in the \code{windowsize} argument.
#'     However, if the start or end statement falls on a date and time where
#'     multiple events happen, those additional events that occur simultaneously
#'     are included because there is no other way to decide which of the
#'     statements should be selected. Therefore the window size is sometimes
#'     extended when the start or end point of a time window is ambiguous in
#'     event time.
#' @param windowsize The number of time units of which a moving time window is
#'     comprised. This can be the number of statement events, the number of days
#'     etc., as defined in the \code{"timewindow"} argument.
#' @param excludeValues A list of named character vectors that contains entries
#'     which should be excluded during network construction. For example,
#'     \code{list(concept = c("A", "B"), organization = c("org A", "org B"))}
#'     would exclude all statements containing concepts "A" or "B" or
#'     organizations "org A" or "org B" when the network is constructed. This
#'     is irrespective of whether these values appear in \code{variable1},
#'     \code{variable2}, or the \code{qualifier}. Note that only variables at
#'     the statement level can be used here. There are separate arguments for
#'     excluding statements nested in documents with certain meta-data.
#' @param excludeAuthors A character vector of authors. If a statement is
#'     nested in a document where one of these authors is set in the "Author"
#'     meta-data field, the statement is excluded from network construction.
#' @param excludeSources A character vector of sources. If a statement is
#'     nested in a document where one of these sources is set in the "Source"
#'     meta-data field, the statement is excluded from network construction.
#' @param excludeSections A character vector of sections. If a statement is
#'     nested in a document where one of these sections is set in the "Section"
#'     meta-data field, the statement is excluded from network construction.
#' @param excludeTypes A character vector of types. If a statement is
#'     nested in a document where one of these types is set in the "Type"
#'     meta-data field, the statement is excluded from network construction.
#' @param invertValues A boolean value indicating whether the entries provided
#'     by the \code{excludeValues} argument should be excluded from network
#'     construction (\code{invertValues = FALSE}) or if they should be the only
#'     values that should be included during network construction
#'     (\code{invertValues = TRUE}).
#' @param invertAuthors A boolean value indicating whether the entries provided
#'     by the \code{excludeAuthors} argument should be excluded from network
#'     construction (\code{invertAuthors = FALSE}) or if they should be the
#'     only values that should be included during network construction
#'     (\code{invertAuthors = TRUE}).
#' @param invertSources A boolean value indicating whether the entries provided
#'     by the \code{excludeSources} argument should be excluded from network
#'     construction (\code{invertSources = FALSE}) or if they should be the
#'     only values that should be included during network construction
#'     (\code{invertSources = TRUE}).
#' @param invertSections A boolean value indicating whether the entries
#'     provided by the \code{excludeSections} argument should be excluded from
#'     network construction (\code{invertSections = FALSE}) or if they should
#'     be the only values that should be included during network construction
#'     (\code{invertSections = TRUE}).
#' @param invertTypes A boolean value indicating whether the entries provided
#'     by the \code{excludeTypes} argument should be excluded from network
#'     construction (\code{invertTypes = FALSE}) or if they should be the
#'     only values that should be included during network construction
#'     (\code{invertTypes = TRUE}).
#' @param verbose A boolean value indicating whether details of network
#'     construction should be printed to the R console.
#'
#' @examples
#' \dontrun{
#' dna_downloadJar()
#' dna_init("dna-2.0-beta21.jar")
#' conn <- dna_connection(dna_sample())
#' nw <- dna_network(conn,
#'                   networkType = "onemode",
#'                   variable1 = "organization",
#'                   variable2 = "concept",
#'                   qualifier = "agreement",
#'                   qualifierAggregation = "congruence",
#'                   normalization = "average",
#'                   excludeValues = list("concept" =
#'                       c("There should be legislation to regulate emissions.")))
#'                       
#' # plot network
#' dna_plotNetwork(nw)
#' dna_plotHive(nw)
#' }
#' @export
dna_network <- function(connection,
                        networkType = "twomode",
                        statementType = "DNA Statement",
                        variable1 = "organization",
                        variable1Document = FALSE,
                        variable2 = "concept",
                        variable2Document = FALSE,
                        qualifier = "agreement",
                        qualifierAggregation = "ignore",
                        normalization = "no",
                        isolates = FALSE,
                        duplicates = "include",
                        start.date = "01.01.1900",
                        stop.date = "31.12.2099",
                        start.time = "00:00:00",
                        stop.time = "23:59:59",
                        timewindow = "no",
                        windowsize = 100,
                        excludeValues = list(),
                        excludeAuthors = character(),
                        excludeSources = character(),
                        excludeSections = character(),
                        excludeTypes = character(),
                        invertValues = FALSE,
                        invertAuthors = FALSE,
                        invertSources = FALSE,
                        invertSections = FALSE,
                        invertTypes = FALSE,
                        verbose = TRUE) {
 
  # convert single values to vectors by means of duplication if necessary
  if (length(excludeTypes) == 1) {
    excludeTypes <- c(excludeTypes, excludeTypes)
  }
  if (length(excludeAuthors) == 1) {
    excludeAuthors <- c(excludeAuthors, excludeAuthors)
  }
  if (length(excludeSources) == 1) {
    excludeSources <- c(excludeSources, excludeSources)
  }
  if (length(excludeSections) == 1) {
    excludeSections <- c(excludeSections, excludeSections)
  }
  if (!is.null(excludeValues) && length(excludeValues) > 0) {
    for (i in 1:length(excludeValues)) {
      if (length(excludeValues[[i]]) == 1) {
        excludeValues[[i]] <- c(excludeValues[[i]], excludeValues[[i]])
      }
    }
  }
  if (length(excludeValues) > 0) {
    dat <- matrix("", nrow = sum(sapply(excludeValues, length)), ncol = 2)
    count <- 0
    for (i in 1:length(excludeValues)) {
      if (length(excludeValues[[i]]) > 0) {
        for (j in 1:length(excludeValues[[i]])) {
          count <- count + 1
          dat[count, 1] <- names(excludeValues)[i]
          dat[count, 2] <- excludeValues[[i]][j]
        }
      }
    }
    var <- dat[, 1]
    val <- dat[, 2]
  } else {
    var <- character()
    val <- character()
  }
  myint <- as.integer(100)
  .jcall(connection$dna_connection,
         "V",
         "rNetwork",
         networkType,
         statementType,
         variable1,
         variable1Document,
         variable2,
         variable2Document,
         qualifier,
         qualifierAggregation,
         normalization,
         isolates,
         duplicates,
         start.date,
         stop.date,
         start.time,
         stop.time,
         timewindow,
         as.integer(windowsize),
         var,
         val,
         excludeAuthors,
         excludeSources,
         excludeSections,
         excludeTypes,
         invertValues,
         invertAuthors,
         invertSources,
         invertSections,
         invertTypes,
         verbose
  )
 
  if (networkType == "eventlist") {
    objects <- .jcall(connection$dna_connection, "[Ljava/lang/Object;", "getEventListColumnsR", simplify = TRUE)
    columnNames <- .jcall(connection$dna_connection, "[S", "getEventListColumnsRNames", simplify = TRUE)
    dta <- data.frame(id = .jevalArray(objects[[1]]))
    dta$time <- as.POSIXct(.jevalArray(objects[[2]]), origin = "1970-01-01")
    dta$docId <- .jevalArray(objects[[3]])
    dta$docTitle <- .jevalArray(objects[[4]])
    dta$docAuthor <- .jevalArray(objects[[5]])
    dta$docSource <- .jevalArray(objects[[6]])
    dta$docSection <- .jevalArray(objects[[7]])
    dta$docType <- .jevalArray(objects[[8]])
    for (i in 1:length(columnNames)) {
      dta[[columnNames[i]]] <- .jevalArray(objects[[i + 8]])
    }
    attributes(dta)$call <- match.call()
    class(dta) <- c("dna_eventlist", class(dta))
    return(dta)
  } else if (timewindow == "no") {
    mat <- .jcall(connection$dna_connection, "[[D", "getMatrix", simplify = TRUE)
    rownames(mat) <- .jcall(connection$dna_connection, "[S", "getRowNames", simplify = TRUE)
    colnames(mat) <- .jcall(connection$dna_connection, "[S", "getColumnNames", simplify = TRUE)
    attributes(mat)$call <- match.call()
    class(mat) <- c(paste0("dna_network_", networkType), class(mat))
    return(mat)
  } else {
    timeLabels <- .jcall(connection$dna_connection, "[J", "getTimeWindowTimes", simplify = TRUE)
    timeLabels <- as.POSIXct(timeLabels, origin = "1970-01-01")
    mat <- list()
    for (t in 1:length(timeLabels)) {
      m <- .jcall(connection$dna_connection, "[[D", "getTimeWindowNetwork", as.integer(t - 1), simplify = TRUE)
      rownames(m) <- .jcall(connection$dna_connection, "[S", "getTimeWindowRowNames", as.integer(t - 1), simplify = TRUE)
      colnames(m) <- .jcall(connection$dna_connection, "[S", "getTimeWindowColumnNames", as.integer(t - 1), simplify = TRUE)
      mat[[t]] <- m
    }
    dta <- list()
    dta$networks <- mat
    dta$time <- timeLabels
    attributes(dta)$call <- match.call()
    class(dta) <- c(paste0("dna_network_", networkType, "_timewindows"), class(dta))
    return(dta)
  }
}


#' @rdname dna_barplot
#' @usage NULL
#' @export
dna_plotCentrality <- function(...) {
  .Deprecated('dna_barplot')
  dna_barplot(...)
}


#' Plot agreement and disagreement
#'
#' Plot agreement and disagreement towards statements.
#'
#' This function plots agreement and disagreement towards DNA Statements for
#' different categories such as "concept", "person" or "docTitle". The goal is to
#' determine the centrality of claims. If, for example, concepts are not very
#' contested, this may mask the extent of polarization with regard to the other
#' concepts. It often makes sense to exclude those concept in further analysis.
#'
#' @param connection A \code{dna_connection} object created by the
#'   \link{dna_connection} function.
#' @param of Category over which (dis-)agreement will be plotted. Most useful
#'   categories are "concept" and "actor" but document categories can be used.
#' @param lab.pos,lab.neg Names for (dis-)agreement labels.
#' @param lab Determines whether (dis-)agreement labels and title are displayed.
#' @param colours If TRUE, statement colours will be used to fill the bars. Not
#'   possible for all categories.
#' @param fontSize Text size in pts.
#' @param barWidth Thickness of the bars. bars will touch when set to 1. When
#'   set to 0.5, space between two bars is the same as thickness of bars.
#' @param axisWidth Thickness of the x-axis which separates agreement from
#'   disagreement.
#' @param truncate Sets the number of characters to which axis labels (i.e. the
#'   categories of "of") should be truncated.
#' @param ... Additional arguments passed to \link{dna_network}.
#'
#' @examples
#' \dontrun{
#' dna_downloadJar()
#' dna_init("dna-2.0-beta21.jar")
#' conn <- dna_connection(dna_sample())
#'
#' dna_barplot(connection = conn,
#'                    of = "concept",
#'                    colours = FALSE,
#'                    barWidth = 0.5)
#' }
#' @author Johannes B. Gruber
#' @export
#' @import ggplot2
dna_barplot <- function(connection,
                        of = "concept",
                        lab.pos = "Agreement",
                        lab.neg = "Disagreement",
                        lab = TRUE,
                        colours = FALSE,
                        fontSize = 12,
                        barWidth = 0.6,
                        axisWidth = 1.5,
                        truncate = 40,
                        ...) {
  #retrieve data from network
  dta <- dna_network(connection = connection,
                     networkType = "eventlist",
                     verbose = FALSE,
                     ...)
  dots <- list(...)
  # test validity of "of"-value
  if(!of %in% colnames(dta)|of %in% c("id", "agreement")){
    stop(
      paste0("\"", of, "\" is not a valid \"of\" value. Choose one of the following:\n",
             paste0("\"", colnames(dta)[!colnames(dta) %in% c("id", "agreement")], "\"", collapse = ",\n"))
    )
  }
  if(of %in% c("time", "docId", "docTitle", "docAuthor", "docSource", "docSection", "docType")){
    warning(
      paste0("\"colours = TRUE\" not possible for \"of = \"", of, "\"\".", collapse = ",\n")
    )
    colours <- FALSE
  }
  
  # count (dis-)agreement per "of"
  dta <- as.data.frame(table(dta$agreement, dta[, of]),
                       stringsAsFactors = FALSE)
  colnames(dta) <- c("agreement", "of", "Frequency")
  binary <- all(dta$agreement %in% c(0, 1))
  if (binary) {
    dta$count <- dta$Frequency
    dta$absFrequency <- dta$Frequency
  } else {
    dta$count <- dta$Frequency
    dta$Frequency <- dta$Frequency * as.numeric(dta$agreement)
    dta$absFrequency <- abs(dta$Frequency)
  }
  
  # order data per total mentions (disagreement + agreement)
  dta2 <- stats::aggregate(absFrequency ~ of, sum, data=dta)
  dta2 <- dta2[order(dta2$absFrequency, decreasing = TRUE), ]
  
  # replicate order of dta2$of to dta
  dta$of <- factor(dta$of, levels = rev(dta2$of))
  
  # get bar colours
  if (colours){
    if (!"statementType" %in% names(dots)) {
      dots$statementType <- "DNA Statement"
    }
    col <- dna_getAttributes(connection = connection, statementType = dots$statementType,
                             variable = of, values = NULL)
    dta$colour <- as.character(col$color[match(dta$of, col$value)])
    dta$text_colour <- "black"
    dta$text_colour[sum(grDevices::col2rgb(dta$colour) *c(299, 587,114))/1000 < 123] <- "white"
  } else {
    dta$colour <- "white"
    dta$text_colour <- "black"
  }
  
  if (binary) {
    # setting disagreement as -1 instead 0
    dta$agreement <- ifelse(dta$agreement == 0, -1, 1)
    
    # recode Frequency in positive and negative
    dta$Frequency <- dta$Frequency * as.integer(dta$agreement)
    dta$absFrequency <- abs(dta$Frequency)
    
    # generate position of bar labels
    offset <- (max(dta$Frequency) + abs(min(dta$Frequency))) * 0.05
    offset <- ifelse(offset < 0.5, 0.5, offset) # offset should be at least 0.5
    if(offset > abs(min(dta$Frequency))){offset <- abs(min(dta$Frequency))}
    if(offset > max(dta$Frequency)){offset <- abs(min(dta$Frequency))}
    dta$pos <- ifelse(dta$Frequency > 0,
                      dta$Frequency + offset,
                      dta$Frequency - offset)
    
    # move 0 labels where neccessary
    dta$pos[dta$Frequency == 0] <- ifelse(dta$agreement[dta$Frequency == 0] == 1,
                                          dta$pos[dta$Frequency == 0] * -1,
                                          dta$pos[dta$Frequency == 0])
    dta$label <- as.factor(dta$count)
  } else {
    dta <- dta[dta$Frequency != 0, ]
    dta$pos <- ifelse(dta$Frequency > 0,
                      1.1,
                      -0.1)
    # add 0 values in case all frequencies are positive/negative
    for (c in unique(dta$of)) {
      if(all(dta$Frequency[dta$of == c] < 0)) {
        dta <- rbind(dta,
                     dta[dta$of == c, ][1, ])
        dta[nrow(dta), c(1, 3, 4)] <- 0
      }
      if(all(dta$Frequency[dta$of == c] > 0)) {
        dta <- rbind(dta,
                     dta[dta$of == c, ][1, ])
        dta[nrow(dta), c(1, 3, 4)] <- 0
      }
    }
    dta$label <- paste(dta$count, dta$agreement, sep = " x ")
  }
  offset <- (max(dta$Frequency) + abs(min(dta$Frequency))) * 0.05
  offset <- ifelse(offset < 0.5, 0.5, offset)
  yintercepts <- data.frame(x = c(0.5, length(unique(dta$of)) + 0.5),
                            y = c(0, 0))
  high <- yintercepts$x[2] + 0.25
  
  g <- ggplot(dta[order(as.numeric(dta$agreement), 
                        decreasing = TRUE),], 
              aes_string(x = "of", 
                         y = "Frequency", 
                         fill = "agreement", 
                         label = "label")) +
    geom_bar(aes_string(fill = "colour",
                        colour = "text_colour"),
             stat="identity",
             width = barWidth,
             show.legend = FALSE) +
    coord_flip() +
    theme_minimal() +
    geom_line(aes_string(x = "x", y = "y"), 
              data = yintercepts, 
              size = axisWidth,
              inherit.aes = FALSE) +
    theme(panel.grid.major = element_blank(),
          panel.grid.minor = element_blank(),
          axis.title.x = element_blank(),
          axis.title.y = element_blank(),
          axis.text.x = element_blank(),
          axis.ticks.y = element_blank(),
          axis.text.y = element_text(size = fontSize),
          plot.title = element_text(hjust = ifelse(max(nchar(as.character(dta$of))) > 10, -0.15, 0))) +
    scale_fill_manual(values = dta$colour) +
    scale_color_identity()
  if (binary) {
    g <- g +
      geom_text(aes_string(x = "of", 
                           y = "pos",
                           label = "label"), 
                size = (fontSize / .pt),
                inherit.aes = FALSE,
                data = dta)
  } else {
    g <- g +
      geom_text(aes_string(colour = "text_colour"),
                size = (fontSize / .pt), 
                position = position_stack(vjust = 0.5),
                inherit.aes = TRUE)
  }
  if(lab){
    g <- g +
      annotate("text",
               x = high,
               y = offset * 2,
               hjust = 0,
               label = lab.pos,
               size = (fontSize / .pt)) +
      annotate("text",
               x = high,
               y = 0 - offset * 2,
               hjust = 1,
               label = lab.neg,
               size = (fontSize / .pt)) +
      scale_x_discrete(labels = function(x) trim(x, n = truncate), 
                       expand = c(0, 2, 0, 2))
  } else {
    g <- g +
      scale_x_discrete(labels = function(x) trim(x, n = truncate))
  }
  return(g)
}


#' Plots a dendrogram from dna_cluster objects
#'
#' Plots a dendrogram from objects derived via \link{dna_cluster}.
#'
#' This function is a convenience wrapper for several different dendrogram
#' types, which can be plotted using the \code{ggraph} package.
#'
#' @param clust A \code{dna_cluster} object created by the \link{dna_cluster}
#'   function.
#' @param shape The shape of the dendrogram. Available options are "elbows",
#'   "link", "diagonal", "arc", and "fan". See
#'   \link[ggraph]{layout_dendrogram_auto}.
#' @param activity Should activity of variable in \link{dna_cluster} be used to
#'   determine size of leaf_ends (logical). Activity means the number of
#'   statements which remained after duplicates were removed.
#' @param leaf_colours Determines which data is used to colour the leafs of the
#'   dendrogram. Can be either "attribute1", "attribute2" or "group". Set to
#'   \code{character()} leafs-lines should not be coloured.
#' @param colours There are three options from where to derive the colours in
#'   the plot: (1.) "identity" tries to use the names of variables as colours
#'   (e.g., if you retrieved the names as attribute from DNA), fails if names
#'   are not plottable colours; (2.) "manual" provide colours via
#'   custom_colours; (3.) "brewer" automatically select nice colours from a
#'   \code{RColorBrewer} palette (palettes can be set in custom_colours,
#'   defaults to \code{Set3}).
#' @param custom_colours Either provide enough colours to manually set the
#'   colours in the plot (if colours = "manual") or select a palette from
#'   \code{RColorBrewer} (if colours = "brewer").
#' @param branch_colour Provide one colour in which all branches are coloured.
#' @param line_width Width of all lines.
#' @param line_alpha Alpha of all lines.
#' @param ends_size If \code{activity = FALSE}, the size of the lineend symbols
#'   can be set to one size for the whole plot.
#' @param leaf_ends Determines which data is used to colour the leaf_ends of the
#'   dendrogram. Can be either "attribute1", "attribute2" or "group". Set to
#'   \code{character()} if no line ends should be displayed.
#' @param custom_shapes If shapes are provided, those are used for leaf_ends
#'   instead of the standard ones. Available shapes range from 0:25 and 32:127.
#' @param ends_alpha Alpha of all leaf_ends.
#' @param rectangles If a colour is provided, this will draw rectangles in given
#'   colour around the groups.
#' @param leaf_linetype,branch_linetype Determines which lines are used for
#'   leafs and branches. Takes "a" for straight line or "b" for dotted line.
#' @param font_size Set the font size for the entire plot.
#' @param theme See themes in \code{ggplot2}. The theme "bw" was customised to
#'   look best with dendrograms. Leave empty to use standard ggplot theme.
#'   Customise the theme by adding \code{+ theme_*} after this function...
#' @param truncate Sets the number of characters to which labels should be
#'   truncated. Value \code{Inf} turns off truncation.
#' @param leaf_labels Either "ticks" to display the labels as axis ticks or
#'   "node" to label nodes directly. Node labels are also take the same colour
#'   as the leaf the label.
#' @param circular Logical. Should the layout be transformed to a circular
#'   representation. See \link[ggraph]{layout_dendrogram_auto}.
#' @param show_legend Logical. Should a legend be displayed.
#' @param ... Not used. If you want to add more plot options use \code{+} and
#'   the ggplot2 logic (see example).
#'
#' @examples
#' \dontrun{
#' dna_downloadJar()
#' dna_init("dna-2.0-beta21.jar")
#' conn <- dna_connection(dna_sample())
#' clust <- dna_cluster(conn)
#' dend <- dna_plotDendro(clust)
#'
#' # Flip plot with ggplot2 command
#' library("ggplot2")
#' dend + coord_flip()
#' }
#' @author Johannes B. Gruber
#' @export
#' @import ggraph
#' @importFrom stats as.dendrogram is.leaf dendrapply aggregate
dna_plotDendro <- function(clust,
                           shape = "elbows",
                           activity = FALSE,
                           leaf_colours = "attribute1",
                           branch_colour = "#636363",
                           colours = "identity",
                           custom_colours = character(),
                           leaf_ends = character(),
                           custom_shapes = character(),
                           ends_alpha = 1,
                           ends_size = 3,
                           rectangles = character(),
                           leaf_linetype = "a",
                           branch_linetype = "b",
                           line_width = 1,
                           line_alpha = 1,
                           font_size = 12,
                           theme = "bw",
                           truncate = 30,
                           leaf_labels = "ticks",
                           circular = FALSE,
                           show_legend = TRUE,
                           ...) {
  # truncate lables----
  clust$labels_short <- ifelse(nchar(clust$labels) > truncate,
                               paste0(gsub("\\s+$", "",
                                           strtrim(clust$labels, width = truncate)),
                                      "..."),
                               clust$labels)
 
  # format as dendrogram
  hierarchy <- stats::as.dendrogram(clust)
  # Add colours----
  hierarchy <- stats::dendrapply(hierarchy, function(x) {
    if (stats::is.leaf(x)) {
      if (length(leaf_colours) > 0) {
        attr(x, "Colour1") <- as.character(clust[[leaf_colours]][match(as.character(labels(x)),
                                                                       clust$labels)])
      } else {
        attr(x, "Colour1") <- ""
      }
      if (length(leaf_ends) > 0) {
        attr(x, "Colour2") <- as.character(clust[[leaf_ends]][match(as.character(labels(x)),
                                                                    clust$labels)])
      } else {
        attr(x, "Colour2") <- ""
      }
     
      attr(x, "Activity") <- clust$activities[clust$order[match(as.character(labels(x)),
                                                                clust$labels)]]
      attr(x, "labels_short") <- clust$labels_short[match(as.character(labels(x)),
                                                          clust$labels)]
      attr(x, "linetype") <- leaf_linetype
    } else {
      if (length(leaf_colours) > 0) {
        attr(x, "Colour1") <- branch_colour
      } else {
        attr(x, "Colour1") <- ""
      }
      if (length(leaf_ends) > 0) {
        attr(x, "Colour2") <- branch_colour
      } else {
        attr(x, "Colour2") <- ""
      }
      attr(x, "Activity") <- 0
      attr(x, "labels_short") <- ""
      attr(x, "linetype") <- branch_linetype
    }
    attr(x, "edgePar") <- list(cols1 = attr(x, "Colour1"),
                               cols2 = attr(x, "Colour2"),
                               linetype = attr(x, "linetype"))
    attr(x, "nodePar") <- list(cols3 = attr(x, "Colour1"),
                               cols4 = attr(x, "Colour2"),
                               Activity = attr(x, "Activity"),
                               labels_short = attr(x, "labels_short"))
    x
  })
  # create dedrogram----
  dg <- ggraph(graph = hierarchy,
               layout = "dendrogram",
               circular = circular)

  # Recode show_legend
  show_legend <- ifelse(show_legend, # ggplot wants this recoding for some reason
                        NA,
                        show_legend)
  
  # shape----
  if (shape == "elbows"){
    dg <- dg +
      geom_edge_elbow(aes_string(colour = "cols1",
                                 edge_linetype = "linetype"),
                      show.legend = show_legend ,
                      width = line_width,
                      alpha = line_alpha)
  } else if (shape == "link"){
    dg <- dg +
      geom_edge_link(aes_string(colour = "cols1",
                                edge_linetype = "linetype"),
                     show.legend = show_legend ,
                     width = line_width,
                     alpha = line_alpha)
  } else if (shape == "diagonal"){
    dg <- dg +
      geom_edge_diagonal(aes_string(colour = "cols1",
                                    edge_linetype = "linetype"),
                         show.legend = show_legend,
                         width = line_width,
                         alpha = line_alpha)
  } else if (shape == "arc"){
    dg <- dg +
      geom_edge_arc(aes_string(colour = "cols1",
                               edge_linetype = "linetype"),
                    show.legend = show_legend ,
                    width = line_width,
                    alpha = line_alpha)
  } else if (shape == "fan"){
    dg <- dg +
      geom_edge_fan(aes_string(colour = "cols1",
                               edge_linetype = "linetype"),
                    show.legend = show_legend ,
                    width = line_width,
                    alpha = line_alpha)
  }
  dg <- dg +
    scale_edge_linetype_discrete(guide = "none")
  if (length(leaf_colours) > 0) {
    clust[[leaf_colours]] <- as.factor(clust[[leaf_colours]])
    autoCols <- c(branch_colour, levels(clust[[leaf_colours]]))
    if (is.na(show_legend)) {
      guide <- "legend"
      if (leaf_colours == "attribute1") guidename <- attr(clust, "colours")[1]
      if (leaf_colours == "attribute2") guidename <- attr(clust, "colours")[2]
      if (leaf_colours == "group") guidename <- "group"
      guidename <- paste0(toupper(substr(guidename, 1, 1)),
                          substr(guidename, 2, nchar(guidename)))
    } else {
      guide <- "none"
      guidename <- waiver()
    }
  }
  if (colours == "identity" & length(leaf_colours) > 0) {
    autoCols <- setNames(autoCols, nm = c(branch_colour, levels(clust[[leaf_colours]])))
    dg <- dg +
      scale_edge_colour_manual(breaks = autoCols[-1],
                               values = autoCols,
                               guide = guide,
                               name = guidename)
  } else if (colours == "manual" & length(leaf_colours) > 0) {
    manCols <- c(branch_colour, custom_colours)
    manCols <- setNames(manCols, nm = c(branch_colour, levels(clust[[leaf_colours]])))
    dg <- dg +
      scale_edge_colour_manual(breaks = autoCols[-1],
                               values = manCols,
                               guide = guide,
                               name = guidename)
  } else if (colours == "brewer" & length(leaf_colours) > 0) {
    if (length(custom_colours) == 0) {
      custom_colours <- "Set3"
    }
    brewCols <- c(branch_colour,
                  scales::brewer_pal(type = "div",
                                     palette = custom_colours)(length(levels(clust[[leaf_colours]]))))
    brewCols <- setNames(brewCols, nm = c(branch_colour, levels(clust[[leaf_colours]])))
    dg <- dg +
      scale_edge_colour_manual(breaks = autoCols[-1],
                               values = brewCols,
                               guide = guide,
                               name = guidename)
  }
  if (length(leaf_colours) == 0) {
    dg <- dg +
      scale_edge_colour_manual(values = "black", guide = "none")
  }
 
  # theme----
  if (theme == "bw") {
    dg <- dg +
      theme_bw() +
      theme(panel.border = element_blank(),
            axis.title = element_blank(),
            panel.grid.major = element_blank(),
            panel.grid.minor = element_blank(),
            text = element_text(size = font_size),
            axis.line = element_blank(),
            axis.ticks.x = element_blank(),
            axis.text.x = element_text(angle = 90, vjust = 0.5, hjust = 1))
  } else if (theme == "void") {
    dg <- dg +
      theme_void() +
      theme(text = element_text(size = font_size))
  } else if (theme == "light") {
    dg <- dg +
      theme_light() +
      theme(text = element_text(size = font_size))
  } else if (theme == "dark") {
    dg <- dg +
      theme_dark() +
      theme(text = element_text(size = font_size))
  }
  # labels----
  if (leaf_labels == "ticks") {
    dg <- dg +
      scale_x_continuous(breaks = seq(0, length(clust$labels)-1, by = 1),
                         labels = clust$labels_short[clust$order])
  } else if (leaf_labels == "nodes") {
    if (circular == FALSE) {
      dg <- dg +
        geom_node_text(aes_string(label = "labels_short",
                                  filter = "leaf",
                                  colour = "cols3"),
                       angle = 270,
                       hjust = 0,
                       nudge_y = -0.02,
                       size = (font_size / .pt),
                       show.legend = FALSE) +
        expand_limits(y = c(-2.3, 2.3))
      #circular plots----
    } else {
      dg <- dg +
        geom_node_text(aes(filter = leaf,
                           angle = ifelse(node_angle(x, y) < 270 & node_angle(x, y) > 90,
                                          node_angle(x, y) + 180,
                                          node_angle(x, y)),
                           label = labels_short,
                           hjust = ifelse(node_angle(x, y) < 270 & node_angle(x, y) > 90,
                                          1.05,
                                          -0.05),
                          
                           colour = cols3),
                       size = (font_size / .pt),
                       show.legend = FALSE) +
        expand_limits(x = c(-2.3, 2.3), y = c(-2.3, 2.3))
    }
  }
 
  # line ends----
  if (length(leaf_ends) > 0) {
    if (is.na(show_legend)) {
      guide <- "legend"
      if (leaf_ends == "attribute1") legendname <- attr(clust, "colours")[1]
      if (leaf_ends == "attribute2") legendname <- attr(clust, "colours")[2]
      if (leaf_ends == "group") legendname <- "group"
      legendname <- paste0(toupper(substr(legendname, 1, 1)),
                           substr(legendname, 2, nchar(legendname)))
    } else {
      legendname <- waiver()
    }
    if (activity){
      dg <- dg +
        geom_node_point(aes_string(filter = "leaf",
                                   colour = "cols3",
                                   size = "Activity",
                                   shape = "cols4"),
                        show.legend = show_legend,
                        alpha = ends_alpha)
    } else {
      dg <- dg +
        geom_node_point(aes_string(filter = "leaf",
                                   colour = "cols3",
                                   shape = "cols4"),
                        size = ends_size,
                        show.legend = show_legend,
                        alpha = ends_alpha)
    }
   
    # custom_shapes----
    if (length(custom_shapes) > 0) {
      dg <- dg +
        scale_shape_manual(values = custom_shapes,
                           name = legendname)
    } else {
      dg <- dg +
        scale_shape_discrete(name = legendname)
    }
  }
 
  # rectangles----
  if (length(rectangles) > 0 & !circular) {
    rect <- data.frame(label = clust$labels_short[clust$order],
                       cluster = clust$group[clust$order],
                       y = min(clust$height),
                       x = seq_along(clust$labels_short)-1)
    rect <- aggregate(x~cluster, rect, range)
    rect$xmin <- rect$x[, 1] - 0.25
    rect$xmax <- rect$x[, 2] + 0.25
    rect$ymax <- min(clust$height) + max(range(clust$height)) / 10
    rect$ymin <- 0 - max(range(clust$height)) / 100
   
    dg <- dg +
      geom_rect(data = rect,
                aes_string(xmin = "xmin",
                           xmax = "xmax",
                           ymin = "ymin",
                           ymax = "ymax"),
                color = rectangles,
                fill = NA)
  }
 
  # color node text and points----
  if (length(leaf_colours) > 0) {
   
    if (colours == "identity") {
      dg <- dg +
        scale_colour_identity(guide = "none")
    } else if (colours == "manual") {
      dg <- dg +
        scale_colour_manual(values = manCols[-1],
                            guide = guide,
                            name = guidename)
    } else if (colours == "brewer") {
      dg <- dg +
        scale_colour_manual(values = brewCols[-1],
                            guide = guide,
                            name = guidename)
    }
  }  else {
    dg <- dg +
      scale_colour_manual(values = "black",
                          guide = "none",
                          name = waiver())
  }
  return(dg)
}


#' Plots a heatmap from dna_cluster objects
#'
#' Plots a heatmap with dendrograms from objects derived via \link{dna_cluster}.
#'
#' This function plots a heatmap including dendrograms on the x- and y-axis of
#' the heatmap plot. The available options for colouring the tiles can be
#' displayed using \code{RColorBrewer::display.brewer.all()} (RColorBrewer needs
#' to be installed).
#'
#' @param clust A \code{dna_cluster} object created by the \link{dna_cluster}
#'   function.
#' @param truncate Sets the number of characters to which labels should be
#'   truncated. Value \code{Inf} turns off truncation.
#' @param values If TRUE, will display the values in the tiles of the heatmap.
#' @param colours There are two options: When "brewer" is selected, the function
#'   \link[ggplot2]{scale_fill_distiller} is used to colour the heatmap tiles.
#'   When "gradient" is selected, \link[ggplot2]{scale_fill_gradient} will be
#'   used. The colour palette and low/high values can be supplied using the
#'   argument \code{custom_colours}
#' @param custom_colours For \code{colours = "brewer"} you can use either a
#'   string with a palette name or the index number of a brewer palette (see
#'   details). If \code{colours = "gradient"} you need to supply at least two
#'   values. Colours are then derived from a sequential colour gradient palette.
#'   \link[ggplot2]{scale_fill_gradient}. If more than two colours are provided
#'   \link[ggplot2]{scale_fill_gradientn} is used instead.
#' @param square If TRUE, will make the tiles of the heatmap quadratic.
#' @param dendro_x If TRUE, will draw a dendrogram on the x-axis.
#' @param dendro_x_size,dendro_y_size Control the size of the dendrograms on the
#'   x- and y-axis
#' @param qualifierLevels Takes a list with integer values of the qualifier
#'   levels (as characters) as names and character values as labels (See
#'   example).
#' @param show_legend Logical. Should a legend be displayed.
#' @param ... Additional arguments passed to \link{dna_plotDendro}.
#'
#' @examples
#' \dontrun{
#' dna_downloadJar()
#' dna_init("dna-2.0-beta21.jar")
#' conn <- dna_connection(dna_sample())
#' clust <- dna_cluster(conn)
#' dend <- dna_plotHeatmap(clust,
#'                         qualifierLevels = list("0" = "no",
#'                                                "1" = "yes"))
#' }
#' @author Johannes B. Gruber
#' @export
#' @import ggplot2
#' @importFrom cowplot ggdraw insert_yaxis_grob insert_xaxis_grob
#' @importFrom reshape2 melt
dna_plotHeatmap <- function(clust,
                            truncate = 40,
                            values = FALSE,
                            colours = character(),
                            custom_colours = character(),
                            square = TRUE,
                            dendro_x = TRUE,
                            dendro_x_size = 0.2,
                            dendro_y_size = 0.2,
                            qualifierLevels = list("0" = "no",
                                                   "1" = "yes"),
                            show_legend = TRUE,
                            ...) {
  nw <- clust[["network"]]
  # construct column labels----
  pn <- colnames(nw)
  if (max(sapply(regmatches(pn, gregexpr("-", pn)), length)) == 0) {
    pn <- ""
  } else {
    pn <- regmatches(pn, gregexpr("-?[0-9]\\d*(\\.\\d+)?$", pn))
    pn <- paste0(" ", pn)
    colnames(nw) <- gsub("\\s+-\\s+-?[[:digit:]]$",
                         "",
                         colnames(nw))
  }
  colnames(nw) <- trim(colnames(nw),
                       truncate - 3)
  # test if truncation created duplicated colnames
  if(any(unlist(sapply(unique(pn), function(i){
    duplicated(colnames(nw)[pn == i])
  })))){
    warning(paste0("After truncation, some column labels are now exactly the same.",
                   "Those are followed by # + number now. Consider increasing the 'truncation' value."))
    colnames(nw) <- paste0("L", pn, colnames(nw))
    d <- grepl("\\...$", colnames(nw))
    colnames(nw) <- make.unique(sub("\\...$", "", colnames(nw)), sep = " #")
    colnames(nw)[duplicated(sub(" #[[:digit:]]$", "", colnames(nw)))] <-
      sapply(colnames(nw)[duplicated(sub(" #[[:digit:]]$", "", colnames(nw)))], function(i){
        d <- paste0("#", gsub(".*#", "", i))
        w <- sub(" #[[:digit:]]$", "", i)
        w <- trim(w,
                  truncate - 3,
                  e = "")
        paste(w, d)
      })
    colnames(nw) <- sub("^L.[[:digit:]]", "", colnames(nw))
    colnames(nw)[d] <- paste0(colnames(nw)[d], "...")
  }
  if (any(!pn %in% "^$")) {
    colnames(nw) <- paste0(colnames(nw),
                           " -",
                           pn)
  }
  # convert qualifier levels
  if (length(qualifierLevels) > 0){
    for (l in seq_len(length(qualifierLevels))) {
      colnames(nw) <- gsub(paste0(names(qualifierLevels[l]), "$"),
                           qualifierLevels[l],
                           colnames(nw))
    }
  }
  # truncate row labels----
  row.names(nw) <- trim(row.names(nw),
                        truncate)
 
  if(any(duplicated(row.names(nw)))){
    warning(paste0("After truncation, some row labels are now exactly the same. Those are followed by",
                   " # + number now. Consider increasing the 'truncation' value."))
    row.names(nw) <- paste0(make.names(sub("...$", "", row.names(nw)), unique=TRUE), "...")
  }
  # re-construct clust objects----
  args <- c(as.list(clust$call)[-1],
            formals(dna_cluster)[-1])
  args <- args[!duplicated(names(args))]
  dend_y <- clust
  if (!args$clust.method %in% c("ward.D",
                                    "ward.D2",
                                    "single",
                                    "complete",
                                    "average",
                                    "mcquitty",
                                    "median",
                                    "centroid")) {
    warning(paste0("The dendrogram on the x-axis of the ", 
                   "dna_plotHeatmap cannot be made using \"",
                   args$clust.method,
                   "\". This dendro",
                   "gram is constructed using the method ",
                   "\"ward.D2\" instead."))
    args$clust.method <- "ward.D2"
  }
  
  if (all(t(nw) %in% c(0, 1))){
    d <-  vegan::vegdist(t(nw), method = "jaccard")
  } else {
    d <-  dist(t(nw), method = "euclidean")
  }
  dend_x <- hclust(d, method = args$clust.method)
  
  dend_x$activities <- unname(rowSums(t(nw)))
  
  # plot clust y ----
  dots <- list(...)
  if (!"leaf_colours" %in% names(dots)) {
    dots <- c(dots,
              list(leaf_colours = character()))
  }
  if (!"leaf_colours" %in% names(dots)) {
    dots <- c(dots,
             list(theme = "void"))
  }
  if (!"branch_linetype" %in% names(dots)) {
    dots <- c(dots,
              list(branch_linetype = "a"))
  }
  plt_dendr_y <- do.call(dna_plotDendro,
                         c(list(clust = dend_y,
                                leaf_labels = ""),
                           dots)) +
    scale_x_continuous(expand = c(0.0, 0.5, 0.0, 0.5)) +
    coord_flip() +
    scale_y_reverse()
  # plot clust x ----
  if (dendro_x) {
    plt_dendr_x <- do.call(dna_plotDendro,
                           c(list(clust = dend_x,
                                  leaf_labels = ""),
                             dots)) +
      scale_x_continuous(expand = c(0.0, 0.5, 0.0, 0.5))
  }
  ## heatmap ----
  df <- reshape2::melt(nw[dend_y$order, dend_x$order])
  df$posy <- seq_len(length(levels(df$Var1)))
  df$posx <- as.vector(sapply(seq_len(length(levels(df$Var2))),
                              rep,
                              length(levels(df$Var1))))
  plt_hmap <- ggplot(data = df , aes_string(x = "posx",
                                            y = "posy",
                                            fill = "value")) +
    geom_tile(show.legend = show_legend) +
    theme(axis.text.x = element_text(angle = 90,
                                     vjust = 0.5,
                                     hjust = 1),
          panel.grid = element_blank(),
          axis.line = element_blank(),
          axis.title = element_blank(),
          axis.text = element_text(margin = margin(t = 0,
                                                   r = 0,
                                                   b = 0,
                                                   l = 0,
                                                   unit = "pt")),
          plot.margin = unit(c(0, 0, 0, 0), "lines")) +
    scale_y_continuous(breaks = unique(df$posy),
                       labels = levels(df$Var1),
                       position = "right",
                       expand = c(0, 0)) +
    scale_x_continuous(breaks = unique(df$posx),
                       labels = levels(df$Var2),
                       position = "right",
                       expand = c(0, 0))
  if (square) plt_hmap <- plt_hmap + coord_fixed(expand = FALSE)
  ### display values ----
  if (values) {
    plt_hmap <- plt_hmap +
      geom_text(aes_string(label = "value"))
  }
  ## colour heatmap ----
  if (length(colours) > 0) {
    if (colours == "brewer") {
      if (length(custom_colours) < 1) custom_colours <- 2
      plt_hmap <- plt_hmap +
        scale_fill_distiller(palette = custom_colours,
                             direction = 1)
    } else if (colours == "gradient"){
      if (length(custom_colours) < 1){
        custom_colours <- c("gray", "blue")
      }
      plt_hmap <- plt_hmap +
        scale_fill_gradientn(colours = custom_colours)
    }
  }
  ### merge plots---
  g <- insert_yaxis_grob(plot = plt_hmap,
                         plt_dendr_y,
                         width = grid::unit(dendro_y_size, "null"),
                         position = "left")
  if (dendro_x) {
    g <- insert_xaxis_grob(plot = g,
                           plt_dendr_x,
                           height = grid::unit(dendro_x_size, "null"),
                           position = "top")
  }
  g <- ggdraw(plot = g)
  return(g)
}


#' Produces a hive plot from DNA data
#'
#' This function is an easy wrapper to create hive plots from one-mode networks
#' with data from DNA.
#'
#' This function is a convenience wrapper to plot networks with
#' \link[ggraph]{ggraph} from one-mode network objects created via
#' \link{dna_network} rDNA.
#'
#' @param x A \code{dna_network_onemode} object created by the
#'   \link{dna_network} function.
#' @param axis Takes the name of an attribute in DNA (i.e. "id", "value",
#'   "color", "type", "alias", "notes" or "frequency") or "group" to colour
#'   nodes. The option "group" only makes sense if you provide group membership
#'   information to the \code{groups} argument.
#' @param axis_label If TRUE, axis labels are plotted at the end of the axis and
#'   are removed from the legend.
#' @param axis_colours There are five options for colouring the axis segments:
#'   (1.) "auto" either uses the colours in the DNA database or leaves the
#'   standard ggplot2 colours; (2.) "identity" tries to use the names of
#'   variables as colours (e.g., if you set node_attribute = "color" or have
#'   provided a colour name in another attributes field in DNA) but fails if
#'   names are not plottable colours; (3) "manual" let's you provide colours via
#'   custom_colours; (4.) "brewer" automatically selects nice colours from a
#'   \code{RColorBrewer} palette (palettes can be set in custom_colours,
#'   defaults to \code{Set3}); and (5.) "single" uses the first value in
#'   custom_colours for all axis.
#' @param custom_colours Takes custom values to control the node colours. The
#'   format of the necessary values depends on the setting of
#'   \code{node_colours}: When \code{node_colours = "manual"}, a character
#'   object containing the enough colour names for all groups is needed; When
#'   \code{node_colours = "brewer"} you need to supply a a palette from
#'   \code{RColorBrewer} (otherwise defaults to "Set3"); When \code{node_colours
#'   "single"} only a single colour name is needed (defaults to "red").
#' @param edge_weight If TRUE, edge weights will be used to determine width of
#'   the lines between nodes. The minimum and maximum width can be controlled
#'   with \code{edge_size_range}.
#' @param edge_size_range Takes a numeric vector with two values: minimum and
#'   maximum \code{edge_weight}.
#' @param edge_colour Provide the name of a colour to use for edges.
#' @param edge_alpha Takes numeric values to control the alpha-transperency of
#'   edges. Values lower than 1 make the edges transparent.
#' @param node_label If TRUE, text is added next to nodes to label them. If
#'   "label", a rectangle is drawn underneath the text, often making it easier
#'   to read. If FALSE no lables are drawn.
#' @param label_repel Controls how far from the labels will be put from nodes.
#'   The exact position of text is random but overplotting is avoided.
#' @param label_lines If TRUE, draws lines between nodes and labels if labels
#'   are further away from nodes.
#' @param font_size Control the font size of the node labels.
#' @param theme Provide the name of a theme. Available options are "graph"
#'   (which is customised to look best with networks), "bw", "void", "light" and
#'   "dark". Leave empty to use standard ggplot theme. Choose other themes or
#'   customise with tools from \link{ggplot2} by adding \code{+ theme_*} after
#'   this function.
#' @param truncate Sets the number of characters to which labels should be
#'   truncated. Value \code{Inf} turns off truncation.
#' @param groups Takes a \code{dna_cluster} object or a named list or character
#'   object. In case of a named list or character object, the names must match
#'   the values of \code{variable1} used during network construction (see
#'   example).
#' @param threshold Minimum threshold for which edges should be plotted.
#' @param seed Numeric value passed to \link{set.seed}. The default is as good
#'   as any other value but provides that plots are always reproducible.
#' @param show_legend Logical. Should a legend be displayed.
#' @param ... Currently not used.
#'
#' @examples
#' \dontrun{
#' dna_downloadJar()
#' dna_init()
#' conn <- dna_connection(dna_sample())
#'
#' # Plot from one-mode network
#' nw <- dna_network(conn, networkType = "onemode")
#' dna_plotHive(nw)
#'
#' # Use groups from dna_cluster
#' clust <- dna_cluster(conn, cutree.k = 2)
#' dna_plotHive(nw, axis = "group", groups = clust)
#'
#' # Use custom groups from dna_cluster
#' groups <- c("Alliance to Save Energy" = "group 1",
#'             "Energy and Environmental Analysis, Inc." = "group 2",
#'             "Environmental Protection Agency" = "group 3",
#'             "National Petrochemical & Refiners Association" = "group 1",
#'             "Senate" = "group 2",
#'             "Sierra Club" = "group 3",
#'             "U.S. Public Interest Research Group"  = "group 1")
#' dna_plotHive(nw, axis = "group", groups = groups)
#' }
#'
#' @export
#' @import ggraph
#' @import igraph
#' @importFrom ggrepel geom_label_repel geom_text_repel
dna_plotHive <- function(x,
                         axis = "type",
                         axis_label = FALSE,
                         axis_colours = "auto",
                         custom_colours = character(),
                         edge_weight = TRUE,
                         edge_size_range = c(0.2, 2),
                         edge_colour = "grey",
                         edge_alpha = 1,
                         node_label = TRUE,
                         label_repel = 0.5,
                         label_lines = FALSE,
                         font_size = 6,
                         theme = "graph",
                         truncate = 30,
                         groups = list(),
                         threshold = NULL,
                         seed = 12345,
                         show_legend = TRUE,
                         ...) {
  layout <- "hive"
  # Make igraph
  if (any(grepl("dna_network_twomode", class(x)))) {
    stop("Twomode networks are currently not allowed.")
  }
  graph <- dna_toIgraph(x)
  if (!is.null(threshold)) {
    graph <- delete.edges(graph, which(!E(graph)$weight >= threshold))
  }
  
  # get attributes
  # colour and attribute----
  args <- c(as.list(attributes(x)$call)[-1])
  args["networkType"] <- "eventlist"
  if (is.null(args[["statementType"]])) {
    args[["statementType"]] <- formals("dna_network")[["statementType"]]
  }
  if (is.null(args[["variable1"]])) {
    args[["variable1"]] <- formals("dna_network")[["variable1"]]
  }
  att <- dna_getAttributes(eval(args[["connection"]]), 
                           statementType = args[["statementType"]],
                           variable = args[["variable1"]], 
                           values = row.names(x))
  
  V(graph)$degree <- degree(graph)
  
  if (axis == "group") {
    if (!length(groups) > 0) {
      groups <- rep("Group 0", length(V(graph)$name))
      names(groups) <- V(graph)$name
    } else if (any(grepl("list|character", class(groups)))) {
      groups <- groups[match(V(graph)$name, names(groups))]
    } else if (any(grepl("dna_cluster", class(groups)))) {
      groups <- groups$group[match(V(graph)$name, groups$labels)]
    }
    node_attribute <- "Membership"
    V(graph)$attribute <- groups
  } else {
    V(graph)$attribute <- as.character(att[, axis])[match(att$value, V(graph)$name)]
    node_attribute <- paste0(toupper(substr(axis, 1, 1)),
                             substr(axis, 2, nchar(axis)))
  }
  
  if (edge_weight) {
    E(graph)$Weight <- E(graph)$weight
  } else {
    E(graph)$Weight <- NULL
  }
  
  lyt <- create_layout(graph, layout = layout, axis = "attribute", sort.by = "degree")
  lyt$name_short <- trim(as.character(lyt$name), n = truncate)
  colnames(lyt) <- gsub("attribute", node_attribute, colnames(lyt))
  show_legend <- ifelse(show_legend, # ggplot wants this recoding for some reason
                        NA,
                        show_legend)
  g <- ggraph(lyt) +
    geom_edge_hive(aes_string(width = "Weight"), 
                   alpha = edge_alpha,
                   colour = edge_colour,
                   show.legend = show_legend) +
    scale_edge_width(range = edge_size_range)
  if (axis_label) {
    yexp <- (max(lyt$y) - min(lyt$y)) / 4
    xexp <- (max(lyt$x) - min(lyt$x)) / 4
    g <- g +
      geom_axis_hive(aes_string(colour = node_attribute), 
                     size = 2, 
                     label = TRUE,
                     show.legend = FALSE) +
      scale_y_continuous(expand = c(0, yexp, 0, yexp)) +
      scale_x_continuous(expand = c(0, xexp, 0, xexp))
  } else {
    g <- g +
      geom_axis_hive(aes_string(colour = node_attribute), 
                     size = 2, 
                     label = FALSE,
                     show.legend = show_legend)
  }
  
  # add labels----
  if ((is.logical(node_label) & node_label == TRUE) | node_label == "label") {
    if (node_label == "label") {
      g <- g +
        geom_label_repel(aes_string(x = "x", y = "y",
                                    label = "name_short"),
                         point.padding = label_repel,
                         box.padding = label_repel,
                         fontface = 'bold',
                         size = font_size/.pt,
                         min.segment.length = ifelse(label_lines, 0.5, Inf))
    } else {
      g <- g +
        geom_text_repel(aes_string(x = "x", y = "y",
                                   label = "name_short"),
                        point.padding = label_repel,
                        box.padding = label_repel,
                        fontface = 'bold',
                        size = font_size/.pt,
                        min.segment.length = ifelse(label_lines, 0.5, Inf))
    }
  }
  
  # theme ----
  if (theme == "graph") {
    g <- g  +
      theme_graph(base_family = "", base_size = font_size)
  } else if (theme == "bw") {
    g <- g +
      theme_bw(base_size = font_size)
  } else if (theme == "void") {
    g <- g +
      theme_void(base_size = font_size)
  } else if (theme == "light") {
    g <- g +
      theme_light(base_size = font_size)
  } else if (theme == "dark") {
    g <- g +
      theme_dark(base_size = font_size)
  }
  
  # colours ----
  if (!axis_colours == "auto") {
    if (axis_colours == "identity") {
      g <- g +
        scale_colour_identity()
    } else if (axis_colours == "manual" | axis_colours == "single") {
      if (length(custom_colours) == 0) {
        custom_colours <- "red"
      }
      if (axis_colours == "single") {
        custom_colours <- rep(custom_colours[1], 
                              length.out = length(unique(lyt[, node_attribute])))
      }
      g <- g +
        scale_colour_manual(values = custom_colours)
    } else if (axis_colours == "brewer") {
      if (length(custom_colours) == 0) {
        custom_colours <- "Set3"
      }
      g <- g +
        scale_colour_brewer(palette = custom_colours)
    }
  } 
  return(g)
}


#' Plots an MDS scatterplot from dna.cluster objects
#'
#' Plots a scatterplot with the results of non-metric multidimensional scaling
#' performed in \link{dna_cluster}.
#'
#' This function is a convenience wrapper for using the \code{ggplot2} package
#' to make a scatterplot of the results of non-metric multidimensional scaling
#' performed in \link{dna_cluster}. It can also add ellipses of polygons to
#' highlight clusters.
#'
#' @param clust A \code{dna_cluster} object created by the \link{dna_cluster}
#'   function.
#' @param draw_polygons Logical. Should clusters be highlighted with coloured
#'   polygons?
#' @param custom_colours Manually provide colours for the points and polygons.
#' @param custom_shape Manually provide shapes to use for the scatterplot.
#' @param alpha The alpha level of the polygons drawn when draw.clusters =
#'   "polygon".
#' @param jitter Takes either one value, to control the width of the jittering
#'   of points, two values to control width and height of the jittering of
#'   points (e.g., c(.l, .2)) or "character()" to turn off the jittering of
#'   points.
#' @param seed Seed for jittering.
#' @param label Logical. Should labels be plotted?
#' @param label_size,font_colour,label_background Control the label size, font
#'   colour of the labels and if a background should be displayed when
#'   \code{label = TRUE}. label_size takes numeric values, font_colour takes a
#'   character string with a valid colour value and label_background can be
#'   either TRUE or FALSE.
#' @param point_size Size of the points in the scatterplot.
#' @param expand Expand x- and y-axis (e.g., to make room for labels). The first
#'   value is the units by which the x-axis is expanded in both directions, the
#'   second controls expansion of the y axis.
#' @param stress Should stress from the MDS be displayed on the plot.
#' @param axis_labels Provide custom axis labels.
#' @param clust_method Can be either "pam" for \link[cluster]{pam}, "louvain"
#'   for \link[igraph]{cluster_louvain} or "inherit" to use the method provided
#'   by the call to from \link{dna_cluster}.
#' @param truncate Sets the number of characters to which labels should be
#'   truncated. Value \code{Inf} turns off truncation.
#' @param title Title of the MDS plot.
#' @param ... Not used. If you want to add more plot options use \code{+} and
#'   the ggplot2 logic (see example).
#' @examples
#' \dontrun{
#' dna_downloadJar()
#' dna_init("dna-2.0-beta21.jar")
#' conn <- dna_connection(dna_sample())
#' clust <- dna_cluster(conn)
#' mds <- dna_plotMDS(clust)
#' # Flip plot with ggplot2 command
#' library("ggplot2")
#' mds +
#'   coord_flip()
#' }
#' @author Johannes B. Gruber
#' @export
#' @import ggplot2
#' @importFrom ggrepel geom_label_repel
dna_plotMDS <- function(clust,
                        draw_polygons = TRUE,
                        alpha = .25,
                        jitter = NULL,
                        seed = 12345,
                        label = FALSE,
                        label_size = 3.5,
                        point_size = 1,
                        label_background = FALSE,
                        font_colour = "black",
                        expand = 0,
                        stress = TRUE,
                        truncate = 40,
                        custom_colours = character(),
                        custom_shape = character(),
                        axis_labels = character(),
                        clust_method = "pam",
                        title = "Nonmetric Multidimensional Scaling",
                        ...) {
  df <- clust[["mds"]]
  if (length(jitter) > 0) {
    set.seed(seed)
    df$Dimension_1 <- jitter(df$Dimension_1, amount = jitter[1])
    if (length(jitter) > 1) {
      df$Dimension_2 <- jitter(df$Dimension_2, amount = jitter[2])
    }
  }
  if (clust_method == "inherit") {
   df$cluster <- clust$group[match(clust$labels, df$variable)]
  } else if (clust_method == "pam") {
    df$cluster <- df$cluster_pam
  } else if (clust_method == "louvain") {
    df$cluster <- df$cluster_louvain
  } else {
    stop(paste0("Please provide a valid clust_method: 'inherit', 'pam' or ",
                "'louvain'."))
  }
  df$variable <- trim(as.character(df$variable), truncate)
  g <- ggplot(df, aes_string(x = "Dimension_1",
                             y = "Dimension_2",
                             fill = "cluster",
                             label = "variable"))
  g <- g +
    geom_point(aes_string(colour = "cluster",
                          shape = "cluster"),
                          size = point_size)
  if (draw_polygons){
      polygons <- lapply(unique(df$cluster), function(i) {
        df[df$cluster == i, ][grDevices::chull(x = df[df$cluster == i, ]$Dimension_1,
                                               y = df[df$cluster == i, ]$Dimension_2), ]
      })
      polygons <- do.call(rbind, polygons)
      g <- g +
        geom_polygon(data = polygons,
                     alpha = alpha)
  }
  if (label) {
    if (label_background) {
      g <- g +
        ggrepel::geom_label_repel(size = label_size,
                                  color = font_colour,
                                  show.legend = FALSE)
    } else {
      g <- g +
        ggrepel::geom_text_repel(size = label_size,
                                 color = font_colour,
                                 show.legend = FALSE)
    }
  }
  if (length(expand) > 0) {
    g <- g +
      scale_x_continuous(limits = c(min(df$Dimension_1) - expand[1],
                                    max(df$Dimension_1) + expand[1]))
    if (length(expand) > 1) {
      g <- g +
        scale_y_continuous(limits = c(min(df$Dimension_2) - expand[2],
                                    max(df$Dimension_2) + expand[2]))
    } else {expand[2] <- 0}
  } else {expand[1] <- 0}
  if (length(custom_colours) > 0) {
    g <- g +
      scale_color_manual(values = custom_colours) +
      scale_fill_manual(values = custom_colours)
  }
  if (length(custom_shape) > 0) {
    g <- g +
      scale_shape_manual(values = custom_shape)
  }
  if (length(axis_labels) > 0) {
    g <- g +
      xlab(label = axis_labels[1]) +
      ylab(label = axis_labels[2])
  }
  if (length(title) > 0) {
    g <- g +
      ggtitle(title)
  }
  if (stress) {
    a <- data.frame(x = max(df$Dimension_1) + expand[1],
                    y = max(df$Dimension_2) + expand[2],
                    label = paste("Stress:", round(attributes(df)$stress, digits = 6)))
    g <- g +
      geom_text(data = a, aes(x = x, y = y, label = label),
                inherit.aes = FALSE, hjust=1)
  }
  return(g)
}



#' Plots a network from DNA data
#'
#' This function is an easy wrapper to create network plots from one- and
#' two-mode networks with data from DNA.
#'
#' This function is a convenience wrapper to plot networks with
#' \link[ggraph]{ggraph} from network objects created in rDNA. Specifically,
#' one- and two-mode networks from calls to \link{dna_network} are supported.
#'
#' The available layouts are listed and explained in
#' \link[ggraph]{layout_igraph_auto} under "Standard layouts". When layouts are
#' added to igraph, those should quickly become available as well.
#'
#' Use \code{RColorBrewer::display.brewer.all()} to see which palettes are
#' available as \code{custom_colours} when \code{colours = "brewer"}.
#'
#' @param x A \code{dna_network} object created by the \link{dna_network}
#'   function.
#' @param layout The type of layout to use. Available layouts include "nicely"
#'   (which tries to choose a suiting layout), "bipartite" (for two-mode
#'   networks), "circle", "dh", "drl", "fr", "gem", "graphopt", "kk", "lgl",
#'   "mds", "randomly" and "star". Other layouts might be available (see
#'   Details).
#' @param edges When set to "link" (default) straight lines are used to connect
#'   nodes. Other available options are "arc", "diagonal" and "fan".
#' @param edge_weight If TRUE, edge weights will be used to determine the width
#'   of the lines. The minimum and maximum width can be controlled with
#'   \code{edge_size_range}.
#' @param edge_size_range Takes a numeric vector with two values: minimum and
#'   maximum \code{edge_weight}.
#' @param edge_colour Provide the name of a colour to use for edges. Defaults to
#'   "grey".
#' @param edge_alpha Takes numeric values to control the alpha-transperency of
#'   edges. Values lower than 1 make the edges transparent.
#' @param node_size Takes numeric values to control the size of nodes (defaults
#'   to 6).
#' @param node_attribute Takes the name of an attribute in DNA (i.e. "id",
#'   "value", "color", "type", "alias", "notes" or "frequency") or "group" to
#'   colour nodes. The option "group" only makes sense if you provide group
#'   membership information to the \code{groups} argument.
#' @param node_colours There are five options for colouring the nodes: (1.)
#'   "auto" either leaves the standard ggplot2 colours or uses the colours in
#'   the DNA database; (2.) "identity" tries to use \code{node_attribute} for
#'   colours (i.e., if you set \code{node_attribute = "color"} or have provided
#'   a colour name in another attribute field in DNA) but fails if names are not
#'   plottable colours; (3) "manual" lets you provide colours via
#'   custom_colours; (4.) "brewer" automatically selects nice colours from a
#'   \code{RColorBrewer} palette (palettes can be set in custom_colours); and
#'   (5.) "single" uses the first value in custom_colours for all nodes.
#' @param custom_colours Takes custom values to control the node colours. The
#'   format of the necessary values depends on the setting of
#'   \code{node_colours}: When \code{node_colours = "manual"}, a character
#'   object containing the enough colour names for all groups is needed; When
#'   \code{node_colours = "brewer"} you need to supply a a palette from
#'   \code{RColorBrewer} (otherwise defaults to "Set3"); When \code{node_colours
#'   "single"} only a single colour name is needed (defaults to "red").
#' @param node_shape Controls the node shape. Available shapes range from 0:25
#'   and 32:127.
#' @param node_label If TRUE, text is added next to nodes to label them. If
#'   "label", a rectangle is drawn underneath the text, often making it easier
#'   to read. If FALSE no lables are drawn.
#' @param font_size Controls the font size of the node labels. The default, 6,
#'   looks best on many viewers and knitr reports.
#' @param theme Provide the name of a theme. Available options are "graph"
#'   (which is customised to look best with networks), "bw", "void", "light" and
#'   "dark". Leave empty to use standard ggplot theme. Choose other themes or
#'   customise with tools from \link{ggplot2} by adding \code{+ theme_*} after
#'   this function.
#' @param label_repel Controls how far labels will be put from nodes. The exact
#'   position of text is random but overplotting is avoided.
#' @param label_lines If TRUE, draws lines between nodes and labels if labels
#'   are further away from nodes.
#' @param truncate Sets the number of characters to which labels should be
#'   truncated. Value \code{Inf} turns off truncation.
#' @param groups Takes a \code{dna_cluster} object or a named list or character
#'   object. In case of a named list or character object, the names must match
#'   the values of \code{variable1} used during network construction (see
#'   example).
#' @param threshold Minimum threshold for which edges should be plotted.
#' @param seed Numeric value passed to \link{set.seed}. The default is as good
#'   as any other value but provides that plots are always reproducible.
#' @param show_legend If TRUE, displays a legend.
#' @param ... Arguments passed on to the layout function (see
#'   \link[ggraph]{layout_igraph_auto}). If you want to add more plot options
#'   use \code{+} and ggplot2 functions.
#' @examples
#' \dontrun{
#' dna_downloadJar()
#' dna_init()
#' conn <- dna_connection(dna_sample())
#'
#' # Plot from one-mode network
#' nw <- dna_network(conn, networkType = "onemode")
#' dna_plotNetwork(nw)
#'
#' # Plot from two-mode network (and add ggplot option)
#' nw2 <- dna_network(conn, networkType = "twomode")
#' dna_plotNetwork(nw2) + ggplot2::coord_flip()
#'
#' # Use groups from dna_cluster
#' clust <- dna_cluster(conn, cutree.k = 2)
#' dna_plotNetwork(nw, node_attribute = "group", groups = clust)
#'
#' # Use custom groups from dna_cluster
#' groups <- c("Alliance to Save Energy" = "group 1",
#'             "Energy and Environmental Analysis, Inc." = "group 2",
#'             "Environmental Protection Agency" = "group 3",
#'             "National Petrochemical & Refiners Association" = "group 1",
#'             "Senate" = "group 2",
#'             "Sierra Club" = "group 3",
#'             "U.S. Public Interest Research Group"  = "group 1")
#' dna_plotNetwork(nw, node_attribute = "group", groups = groups)
#' }
#'
#' @export
#' @import ggraph
#' @import igraph
#' @importFrom ggrepel geom_label_repel geom_text_repel
dna_plotNetwork <- function(x,
                            layout = "auto",
                            edges = "link",
                            edge_weight = TRUE,
                            edge_size_range = c(0.2, 2),
                            edge_colour = "grey",
                            edge_alpha = 1,
                            node_size = 6,
                            node_attribute = "color",
                            node_colours = "auto",
                            custom_colours = character(),
                            node_shape = 19,
                            node_label = TRUE,
                            font_size = 6,
                            theme = "graph",
                            label_repel = 0.5,
                            label_lines = FALSE,
                            truncate = 30,
                            groups = list(),
                            threshold = NULL,
                            seed = 12345,
                            show_legend = TRUE,
                            ...) {
  # Make igraph object----
  set.seed(seed)
  if (any(grepl("dna_network_twomode", class(x)))) {
    if (layout == "auto") {
      layout <- "bipartite"
      message("Using `bipartite` as default layout")
    }
  }
  graph <- dna_toIgraph(x)
  
  # Groups-----
  if (!length(groups) > 0) {
    groups <- rep("Group 0", length(V(graph)$name))
    names(groups) <- V(graph)$name
  } else if (any(grepl("list|character", class(groups)))) {
    V(graph)$group <- groups[match(V(graph)$name, names(groups))]
  } else if (any(grepl("dna_cluster", class(groups)))) {
    V(graph)$group <- groups$group[match(V(graph)$name, groups$labels)]
  }
  
  # colour and attribute----
  args <- c(as.list(attributes(x)$call)[-1])
  args["networkType"] <- "eventlist"
  if (is.null(args[["statementType"]])) {
    args[["statementType"]] <- formals("dna_network")[["statementType"]]
  }
  if (is.null(args[["variable1"]])) {
    args[["variable1"]] <- formals("dna_network")[["variable1"]]
  }
  cols <- dna_getAttributes(eval(args[["connection"]]), statementType = args[["statementType"]],
                         variable = args[["variable1"]], values = row.names(x))
  V(graph)$colour <- as.character(cols$color)[match(V(graph)$name, cols$value)]
  if (node_attribute == "group") {
    V(graph)$attribute <- V(graph)$group
  } else {
    if (!any(node_attribute %in% colnames(cols))) {
      stop(paste0("Not a possible 'node_attribute'. Please choose one of: 'group', '",
                  paste(colnames(cols), collapse = "', '"), "'."))
    }
    V(graph)$attribute <- as.character(cols[, node_attribute])[match(V(graph)$name, cols$value)]
  }
  if (edge_weight) {
    E(graph)$Weight <- E(graph)$weight
  } else {
    E(graph)$Weight <- NULL
  }
  if (!is.null(threshold)) {
    graph <- delete.edges(graph, which(!E(graph)$weight >= threshold))
  }
  # start the plot ----
  lyt <- create_layout(graph, layout = layout, ...)
  if (node_attribute == "group") {
    node_attribute <- "Membership"
  } else {
    node_attribute <- paste0(toupper(substr(node_attribute, 1, 1)),
                             substr(node_attribute, 2, nchar(node_attribute)))
  }
  lyt$name_short <- trim(as.character(lyt$name), n = truncate)
  
  if (any(grepl("dna_network_twomode", class(x)))) {
    lyt$attribute <- as.character(lyt$attribute)
    if (node_colours == "auto" & node_attribute == "Color") {
      cols <- dna_getAttributes(eval(args[["connection"]]), 
                                statementType = args[["statementType"]],
                                variable = "concept")
      lyt$attribute[is.na(lyt$attribute)] <- 
        as.character(cols$color)[match(lyt$name[is.na(lyt$attribute)],
                                       cols$value)]
    } else {
      lyt$attribute[is.na(lyt$attribute)] <- "Concept"
    }
    
  }
  colnames(lyt) <- gsub("attribute", node_attribute, colnames(lyt))
  g <- ggraph(lyt)
  # add lines----
  if (edges == "link") {
    g <- g +
      geom_edge_link(aes_string(width = "Weight"), alpha = edge_alpha,
                     colour = edge_colour,
                     show.legend = show_legend)
  } else if (edges == "arc") {
    g <- g +
      geom_edge_arc(aes_string(width = "Weight"), alpha = edge_alpha,
                    colour = edge_colour,
                    show.legend = show_legend)
  } else if (edges == "diagonal") {
    g <- g +
      geom_edge_diagonal(aes_string(width = "Weight"), alpha = edge_alpha,
                         colour = edge_colour,
                         show.legend = show_legend)
  } else if (edges == "fan") {
    g <- g +
      geom_edge_fan(aes_string(width = "Weight"),
                    alpha = edge_alpha,
                    colour = edge_colour,
                    show.legend = show_legend)
  }
  g <- g +
    scale_edge_width(range = edge_size_range)
  # add nodes----
  show_legend <- ifelse(show_legend,
                        NA,
                        show_legend)
  if (node_colours == "single") {
    if (length(custom_colours) == 0) {
      custom_colours <- "red"
    }
    g <- g +
      geom_node_point(colour = custom_colours,
                      size = node_size,
                      shape = node_shape,
                      show.legend = show_legend) +
      scale_color_identity()
  } else {
    g <- g +
      geom_node_point(aes_string(colour = node_attribute),
                      size = node_size,
                      shape = node_shape,
                      show.legend = show_legend)
  }
  # add labels----
  if ((is.logical(node_label) & node_label == TRUE) | node_label == "label") {
    # Make room for labels
    yexp <- (max(lyt$y) - min(lyt$y)) / 3
    xexp <- (max(lyt$x) - min(lyt$x)) / 3
    g <- g +
      scale_y_continuous(expand = c(0, yexp, 0, yexp)) +
      scale_x_continuous(expand = c(0, xexp, 0, xexp))
    if (node_label == "label") {
      g <- g +
        geom_label_repel(aes_string(x = "x", y = "y",
                                    label = "name_short"),
                         point.padding = label_repel,
                         box.padding = label_repel,
                         fontface = 'bold',
                         size = font_size/.pt,
                         min.segment.length = ifelse(label_lines, 0.5, Inf))
    } else {
      g <- g +
        geom_text_repel(aes_string(x = "x", y = "y",
                                   label = "name_short"),
                        point.padding = label_repel,
                        box.padding = label_repel,
                        fontface = 'bold',
                        size = font_size/.pt,
                        min.segment.length = ifelse(label_lines, 0.5, Inf))
    }
  }
  
  # theme ----
  if (theme == "graph") {
    g <- g  +
      theme_graph(base_family = "", base_size = font_size)
  } else if (theme == "bw") {
    g <- g +
      theme_bw(base_size = font_size)
  } else if (theme == "void") {
    g <- g +
      theme_void(base_size = font_size)
  } else if (theme == "light") {
    g <- g +
      theme_light(base_size = font_size)
  } else if (theme == "dark") {
    g <- g +
      theme_dark(base_size = font_size)
  }
  # colours ----
  if (node_colours == "auto" & node_attribute == "Color") {
    node_colours <- "identity"
  }
  if (!node_colours == "auto" | !node_colours == "single") {
    if (node_colours == "identity") {
      g <- g +
        scale_colour_identity()
    } else if (node_colours == "manual") {
      g <- g +
        scale_colour_manual(values = custom_colours)
    } else if (node_colours == "brewer") {
      if (length(custom_colours) == 0) {
        custom_colours <- "Set3"
      }
      g <- g +
        scale_colour_brewer(palette = custom_colours)
    }
  } 
  return(g)
}


#' Plot \link{dna_timeWindow} objects
#'
#' Plot \link{dna_timeWindow} objects in a grid separated by facet.
#'
#' A convenience function to plot an object created with \link{dna_timeWindow}
#' function. Uses \link[ggplot2]{geom_line} under the hood to plot results from
#' a call to \link{dna_timeWindow} and facets a grid view using
#' \link[ggplot2]{facet_grid}. Customised themes and ggplot2 functions can be
#' passed on with +.
#'
#' @param x A \code{dna_timeWindow} object created by the \link{dna_timeWindow}
#'   function.
#' @param facetValues The name or names of the facet values which should be included in the
#'   plot.
#' @param include.y Include specific value of facet in the plot.
#' @param rows,cols Number of rows and columns in which the plots are arranged.
#'   plot.
#' @param ... Currently not used. Additional parameters should be passed on to ggplot2 via
#'   e.g. \code{+ theme_bw()}.
#'
#' @examples
#' \dontrun{
#' dna_downloadJar()
#' dna_init("dna-2.0-beta21.jar")
#' conn <- dna_connection(dna_sample())
#'
#' tW <- dna_timeWindow(connection = conn,
#'                      timewindow = "days",
#'                      windowsize = 10,
#'                      facet = "Authors",
#'                      facetValues = c("Bluestein, Joel",
#'                                      "Voinovich, George",
#'                                      "Whitman, Christine Todd"),
#'                      method = "modularity",
#'                      excludeValues = list(),
#'                      excludeAuthors = character(),
#'                      excludeSources = character(),
#'                      excludeSections = character(),
#'                      excludeTypes = character(),
#'                      verbose = TRUE)
#'
#' plot <- dna_plotTimeWindow(tW,
#'                            facetValues = c("Bluestein, Joel", "Voinovich, George", "all"),
#'                            include.y = 1,
#'                            rows = 3)
#' plot + theme_bw()
#' }
#' @author Johannes B. Gruber, Philip Leifeld
#' @export
#' @import ggplot2
dna_plotTimeWindow <- function(x,
                               facetValues = "all",
                               include.y = NULL,
                               rows = NULL,
                               cols = NULL,
                               ...){
  method <- colnames(x)[3]
  if(!any(grepl("dna_timeWindow", class(x)))){
    warning("x is not an object of class \"dna_timeWindow\".")
  }
  if (identical(facetValues, "all")){
    ggplot2::ggplot(x, aes_string(x = "time", y = paste(method))) +
      geom_line() +
      geom_smooth(stat = 'smooth', method = 'gam', formula = y ~ s(x, bs = "cs")) +
      facet_wrap(~ facet, nrow = rows, ncol = cols)+
      expand_limits(y = include.y)
  } else {
    if (all(facetValues %in% x$facet)){
      if (length(facetValues) == 1){
        ggplot2::ggplot(x[grep(paste0("^", facetValues, "$"), x$facet),], aes_string(x = "time", y = paste(method))) +
          geom_line() +
          geom_smooth(stat = 'smooth', method = 'gam', formula = y ~ s(x, bs = "cs")) +
          expand_limits(y = include.y)
      } else {
        ggplot2::ggplot(x[x$facet %in% facetValues,], aes_string(x = "time", y = paste(method))) +
          geom_line() +
          geom_smooth(stat = 'smooth', method = 'gam', formula = y ~ s(x, bs = "cs")) +
          facet_wrap(~ facet, nrow = rows, ncol = cols)+
          expand_limits(y = include.y)
      }
    } else {
      stop(
        paste0("\"", facetValues[!facetValues %in% x$facet], "\" was not found in facetValues")
      )
    }
  }
}


#' Removes a document from the database
#' 
#' Removes a document from the database based on its ID.
#' 
#' The user provides a connection object and the ID of an existing statement in 
#' the DNA database, and this statement is removed both from memory and from the 
#' SQL database, possibly including any statements contained in the document.
#' 
#' @param connection A \code{dna_connection} object created by the
#'   \link{dna_connection} function.
#' @param id An integer value denoting the ID of the document to be removed. The 
#'   \link{dna_getDocuments} function can be used to look up IDs.
#' @param removeStatements The document given by \code{id} may contain 
#'     statements. If \code{removeStatements = TRUE} is set, these statements 
#'     are removed along with the respective document. If 
#'     \code{removeStatements = FALSE} is set, the statements are not deleted, 
#'     the document is kept as well, and a message is printed.
#' @param simulate Should the changes only be simulated instead of actually 
#'     applied to the DNA connection and the SQL database? This can help to 
#'     plan more complex recode operations.
#' @param verbose Print details on whether the document could be removed?
#' 
#' @author Philip Leifeld
#' @export
dna_removeDocument <- function(connection, 
                               id, 
                               removeStatements = FALSE, 
                               simulate = TRUE, 
                               verbose = TRUE) {
  if (!is.integer(id)) {
    id <- as.integer(id)
  }
  .jcall(connection$dna_connection, 
         "V", 
         "removeDocument", 
         id, 
         removeStatements, 
         simulate, 
         verbose)
}



#' Provides a small sample database
#'
#' Copies a small .dna sample file to the current working directory and returns
#' the location of this newly created file.
#'
#' A small sample database to test the functions of rDNA.
#'
#' @param overwrite Should sample.dna be overwritten if found in the current
#'   working directory?
#' @param verbose Display warning message if file exists in current wd.
#'
#' @examples
#' \dontrun{
#' dna_downloadJar()
#' dna_init("dna-2.0-beta21.jar")
#' dna_connection(dna_sample())
#' }
#' @author Johannes Gruber
#' @export
dna_sample <- function(overwrite = FALSE,
                       verbose = TRUE) {
  if (file.exists(paste0(getwd(), "/sample.dna")) & overwrite == FALSE) {
    if (verbose){
      warning(
        "Sample file exists in wd. Use overwrite = TRUE to create fresh sample file."
      )
    }
  } else {
    file.copy(from = system.file("extdata", "sample.dna", package = "rDNA"),
              to = paste0(getwd(), "/sample.dna"),
              overwrite = overwrite)
  }
  return(paste0(getwd(), "/sample.dna"))
}


#' Recode documents and metadata in the DNA database
#' 
#' Recode documents and their metadata in a DNA database.
#' 
#' This function takes a dataframe with 10 columns (ID, title, text, coder, 
#' author, sources, section, notes, type, date) as returned by the 
#' \link{dna_getDocuments} function and hands it over to a DNA connection in 
#' order to update the documents in the database based on the contents of the 
#' dataframe. The typical workflow is to retrieve the documents and their 
#' metadata using \link{dna_getDocuments}, manipulating the documents, and then 
#' applying the changes with \link{dna_setDocuments}. Documents that are no 
#' longer in the dataframe are removed from the database; documents in the 
#' dataframe that are not in the database are added to the database; and 
#' contents of existing documents are updated. By default, the changes are only 
#' simulated and not actually written into the database. The user can inspect 
#' the reported changes and then apply the actual changes by setting 
#' \code{simulate = FALSE}.
#' 
#' @param connection A \code{dna_connection} object created by the
#'     \code{dna_connection} function.
#' @param documents A dataframe with ten columns: id (integer), title 
#'     (character), text (character), coder (integer), author (character), 
#'     sources (character), section (character), notes (character), type 
#'     (character), and date (POSIXct or integer; if integer, the value 
#'     indicates milliseconds since the start of 1970-01-01). \code{NA} values 
#'     or \code{-1} values are permitted in the id column. If these are 
#'     encountered, a new ID is automatically generated, and the document is 
#'     added.
#' @param removeStatements If a document is present in the DNA database but not 
#'     in the \code{documents} dataframe, the respective document is removed 
#'     from the database. However, the document may contain statements. If 
#'     \code{removeStatements = TRUE} is set, these statements are removed along 
#'     with the respective document. If \code{removeStatements = FALSE} is set, 
#'     the statements are not deleted, the document is kept as well, and a 
#'     message is printed.
#' @param simulate Should the changes only be simulated instead of actually 
#'     applied to the DNA connection and the SQL database? This can help to 
#'     plan more complex recode operations.
#' @param verbose Print details about the recode operations?
#' 
#' @examples
#' \dontrun{
#' dna_init("dna-2.0-beta21.jar")
#' conn <- dna_connection(dna_sample())
#' documents <- dna_getDocuments(conn)
#' documents$title[1] <- "New title for first document"
#' documents$notes[3] <- "Added a note via rDNA."
#' documents <- documents[, -5]  # Removing the fifth document
#' dna_setDocuments(conn, documents, simulate = TRUE)  # apply changes
#' }
#' 
#' @author Philip Leifeld
#' @export
dna_setDocuments <- function(connection, 
                             documents, 
                             removeStatements = FALSE, 
                             simulate = TRUE, 
                             verbose = TRUE) {
  
  if (!is.data.frame(documents)) {
    stop("'documents' must be a data.frame similar to the one returned by dna_getDocuments.")
  }
  if (ncol(documents) != 10) {
    stop("'documents' must be a data.frame with 10 columns.")
  }
  if (!is.integer(documents[, 1])) {
    stop("The first column of 'documents' must be integer and must contain the document IDs.")
  }
  if (!is.character(documents[, 2])) {
    if (is.factor(documents[, 2])) {
      documents[, 2] <- as.character(documents[, 2])
    } else {
      stop("The second column of 'documents' must contain the document titles as character objects.")
    }
  }
  if (!is.character(documents[, 3])) {
    if (is.factor(documents[, 3])) {
      documents[, 3] <- as.character(documents[, 3])
    } else {
      stop("The third column of 'documents' must contain the document texts as character objects.")
    }
  }
  if (!is.numeric(documents[, 4])) {
    stop("The fourth column of 'documents' must contain the coder IDs as integer values (see dna_getCoders).")
  } else if (!is.integer(documents[, 4])) {
    documents[, 4] <- as.integer(documents[, 4])
  }
  if (!is.character(documents[, 5])) {
    if (is.factor(documents[, 5])) {
      documents[, 5] <- as.character(documents[, 5])
    } else {
      stop("The fifth column of 'documents' must contain the document authors as character objects.")
    }
  }
  if (!is.character(documents[, 6])) {
    if (is.factor(documents[, 6])) {
      documents[, 6] <- as.character(documents[, 6])
    } else {
      stop("The sixth column of 'documents' must contain the document sources as character objects.")
    }
  }
  if (!is.character(documents[, 7])) {
    if (is.factor(documents[, 7])) {
      documents[, 7] <- as.character(documents[, 7])
    } else {
      stop("The seventh column of 'documents' must contain the document sections as character objects.")
    }
  }
  if (!is.character(documents[, 8])) {
    if (is.factor(documents[, 8])) {
      documents[, 8] <- as.character(documents[, 8])
    } else {
      stop("The eighth column of 'documents' must contain the document notes as character objects.")
    }
  }
  if (!is.character(documents[, 9])) {
    if (is.factor(documents[, 9])) {
      documents[, 9] <- as.character(documents[, 9])
    } else {
      stop("The ninth column of 'documents' must contain the document types as character objects.")
    }
  }
  if (any(class(documents[, 10]) %in% c("POSIXct", "POSIXt"))) {
    documents[, 10] <- .jlong(as.integer(documents[, 10]) * 1000)
  } else if (is.numeric(documents[, 10])) {
    documents[, 10] <- .jlong(as.integer(documents[, 10]))
  } else {
    stop("The tenth column of 'documents' must contain the document dates as POSIXct objects or as numeric objects indicating milliseconds since 1970-01-01.")
  }
  if (verbose == TRUE) {
    if (nrow(documents) == 0) {
      warning("'documents' has 0 rows. Deleting all documents from the database.")
    }
  }
  
  # replace NAs with -1, which will be replaced by an auto-generated ID in DNA
  if (any(is.na(documents[, 1]))) {
    documents[which(is.na(documents[, 1])), 1] <- as.integer(-1)
  }
  
  documents <- .jarray(lapply(documents, .jarray))
  .jcall(connection$dna_connection, 
         "V", 
         "setDocuments", 
         documents, 
         removeStatements, 
         simulate, 
         verbose)
}


#' Computes for a temporal sequence of networks
#'
#' Computes a measure for each network in a temporal sequence of networks.
#'
#' This function serves as a convenience wrapper to calculate a measure for each
#' network in a temporal sequence of networks. The standard is to calculate the
#' modularity of the network for each time window (see
#' \link[igraph]{modularity.igraph}). The function can also be used to split
#' your data (facet) and calculate networks for each facet type.
#'
#' @param connection A \link{dna_connection} object created by the
#'     \link{dna_connection} function.
#' @param timewindow Same as in \link{dna_network}.
#' @param windowsize Same as in \link{dna_network}.
#' @param facet Which value from the dna database should be used to subset the
#'   networks. Can be "Authors" for document author, "Sources" for document
#'   source, "Sections" for documents which contain a certain section or "Types"
#'   to subset document types.
#' @param facetValues Which values should be used to facet calculation of the
#'   networks. Always contains the value 'all' for comparison. Use e.g.
#'   excludeTypes to exclude documents from comparison.
#' @param method Is used to compute exactly one measurement for each network
#'   computed in the temporal sequence of networks. Can contain the name of any
#'   function which reduces a matrix to just one value.
#' @param verbose Display messages if TRUE or 1. Also display messages details
#'   of network construction when 2
#' @param ... Additional arguments passed to \link{dna_network}.
#'
#' @examples
#' \dontrun{
#' dna_downloadJar()
#' dna_init("dna-2.0-beta21.jar")
#' conn <- dna_connection(dna_sample())
#'
#' tW <- dna_timeWindow(connection = conn,
#'                      timewindow = "days",
#'                      windowsize = 10,
#'                      facet = "Authors",
#'                      facetValues = c("Bluestein, Joel",
#'                                      "Voinovich, George",
#'                                      "Whitman, Christine Todd"),
#'                      method = "modularity",
#'                      verbose = TRUE)
#'
#' dna_plotTimeWindow(tW, facetValues = c("Bluestein, Joel", "Voinovich, George"))
#' }
#' @author Johannes B. Gruber, Philip Leifeld
#' @export
#' @import ggplot2
dna_timeWindow <- function(connection,
                           timewindow = "days",
                           windowsize = 100,
                           facet = "Types",
                           facetValues = character(),
                           method = "modularity",
                           verbose = 2,
                           ...) { #passed on to dna_network
 
  dots <- list(...)
  if("excludeAuthors" %in% names(dots)){
    excludeAuthors <- unname(unlist(dots["excludeAuthors"]))
    dots["excludeAuthors"] <- NULL
  } else {
    excludeAuthors <- character()
  }
  if("excludeSources" %in% names(dots)){
    excludeSources <- unname(unlist(dots["excludeSources"]))
    dots["excludeSources"] <- NULL
  } else {
    excludeSources <- character()
  }
  if("excludeSections" %in% names(dots)){
    excludeSections <- unname(unlist(dots["excludeSections"]))
    dots["excludeSections"] <- NULL
  } else {
    excludeSections <- character()
  }
  if("excludeTypes" %in% names(dots)){
    excludeTypes <- unname(unlist(dots["excludeTypes"]))
    dots["excludeTypes"] <- NULL
  } else {
    excludeTypes <- character()
  }
  facetValues <- c(facetValues, "all")
  if (facet == "Authors" ) {Authors <- facetValues} else {Authors <- character()}
  if (facet == "Sources") {Sources <- facetValues} else {Sources <- character()}
  if (facet == "Sections") {Sections <- facetValues} else {Sections <- character()}
  if (facet == "Types") {Types <- facetValues} else {Types <- character()}
  if (any(Authors %in% excludeAuthors)){
    cat(paste0("\"", Authors[Authors %in% excludeAuthors], "\"", collapse = ", "),
        "is found in both \"Authors\" and \"excludeAuthors\".",
        paste0("\"", Authors[Authors %in% excludeAuthors], "\"", collapse = ", "),
        " was removed from \"excludeAuthors\".\n")
    excludeAuthors <- excludeAuthors[!excludeAuthors %in% Authors]
  }
  if (any(Sources %in% excludeSources)){
    cat(paste0("\"", Sources[Sources %in% excludeSources], "\"", collapse = ", "),
        "is found in both \"Sources\" and \"excludeSources\".",
        paste0("\"", Sources[Sources %in% excludeSources], "\"", collapse = ", "),
        " was removed from \"excludeSources\".\n")
    excludeSources <- excludeSources[!excludeSources %in% Sources]
  }
  if (any(Sections %in% excludeSections)){
    cat(paste0("\"", Sections[Sections %in% excludeSections], "\"", collapse = ", "),
        "is found in both \"Sections\" and \"excludeSections\".",
        paste0("\"", Sections[Sections %in% excludeSections], "\"", collapse = ", "),
        " was removed from \"excludeSections\".\n")
    excludeSections <- excludeSections[!excludeSections %in% Sections]
  }
  if (any(Types %in% excludeTypes)){
    cat(paste0("\"", Types[Types %in% excludeTypes], "\"", collapse = ", "),
        "is found in both \"Types\" and \"excludeTypes\".",
        paste0("\"", Types[Types %in% excludeTypes], "\"", collapse = ", "),
        " was removed from \"excludeTypes\".\n")
    excludeTypes <- excludeTypes[!excludeTypes %in% Types]
  }
  if (method == "modularity"){
    mod.m <- lapply(facetValues, function(x){
      if (verbose|verbose == 2){cat("Calculating Type =", facetValues[facetValues %in% x], "\n")}
      nw <- do.call(dna_network,
                    c(list(connection = connection,
                           networkType = "onemode",
                           qualifierAggregation = "congruence",
                           timewindow = timewindow,
                           windowsize = windowsize,
                           excludeAuthors = c(Authors[Authors %in% x], excludeAuthors),
                           excludeSources = c(Sources[Sources %in% x], excludeSources),
                           excludeSections = c(Sections[Sections %in% x], excludeSections),
                           excludeTypes = c(Types[Types %in% x], excludeTypes),
                           verbose = ifelse(verbose > 1, TRUE, FALSE)
                    ), dots)
      )
      mod.m <- data.frame(index = 1:length(nw$networks),
                          time = nw$time,
                          modularity = sapply(nw$networks, lvmod),
                          facet = rep(facetValues[facetValues %in% x], length(nw$networks)))
      return(mod.m)
    })
  } else {
    if(!exists(method, mode = 'function')){
      stop(
        paste0("\"", method, "\" is not a valid function.")
      )
    } else {
      if (length(do.call(method, list(matrix(c(1,2,3, 11,12,13), nrow = 2, ncol = 3))))!=1){
        stop(
          paste0("\"", method, "\" is not a valid method for dna_timeWindow.\n dna_timeWindow needs a
                 function which provides exactly one value when applied to an object of class matrix.
                 See ?dna_timeWindow for help.")
          )} else {
            mod.m <- lapply(Types, function(x){
              if (verbose|verbose == 2){cat("Calculating Type =", Types[Types %in% x], "\n")}
              nw <- do.call(dna_network,
                            c(list(connection = connection,
                                   networkType = "onemode",
                                   qualifierAggregation = "congruence",
                                   timewindow = timewindow,
                                   windowsize = windowsize,
                                   excludeAuthors = c(Authors[Authors %in% x], excludeAuthors),
                                   excludeSources = c(Sources[Sources %in% x], excludeSources),
                                   excludeSections = c(Sections[Sections %in% x], excludeSections),
                                   excludeTypes = c(Types[Types %in% x], excludeTypes),
                                   verbose = ifelse(verbose > 1, TRUE, FALSE)
                            ), dots)
              )
              mod.m <- data.frame(index = 1:length(nw$networks),
                                  time = nw$time,
                                  x = sapply(nw$networks, method),
                                  facet = rep(facetValues[facetValues %in% x], length(nw$networks)))
              colnames(mod.m)[3] <- method
              return(mod.m)
            })
      }}}
  mod.df <- do.call("rbind", mod.m)
  class(mod.df) <- c("data.frame", "dna_timeWindow", paste(method))
  return(mod.df)
}



#' Convert DNA networks to igraph objects
#'
#' This function can convert objects of class 'dna_network_onemode' or
#' 'dna_network_twomode' to igraph objects.
#'
#' @param x A dna_network (one- or two-mode).
#' @param weighted Logical. Should edge weights be used to create a weighted
#'   graph from the dna_network object.
#'
#' @return
#' @export
#' @importFrom igraph graph_from_adjacency_matrix graph_from_incidence_matrix
#'
#' @examples
#' \dontrun{
#' dna_downloadJar()
#' dna_init("dna-2.0-beta21.jar")
#' conn <- dna_connection(dna_sample())
#' nw <- dna_network(conn,
#'                   networkType = "onemode")
#' graph <- dna_toIgraph(nw)
#' }
dna_toIgraph <- function(x,
                         weighted = TRUE) {
  if (any(grepl("dna_network_onemode", class(x)))) {
    graph <- graph_from_adjacency_matrix(x,
                                         mode = "undirected",
                                         weighted = weighted,
                                         diag = FALSE,
                                         add.colnames = NULL,
                                         add.rownames = NA)
  } else if (any(grepl("dna_network_twomode", class(x)))) {
    graph <- graph_from_incidence_matrix(x,
                                         directed = FALSE,
                                         weighted = weighted,
                                         add.names = NULL)
  } else {
    stop("Only takes objects of class 'dna_network_onemode' or 'dna_network_twomode'.")
  }
  return(graph)
}


#' Calculate the modularity of a network
#'
#' Calculate the modularity of a network retrieved via \link{dna_network}.
#'
#' Uses the function \link[igraph]{modularity.igraph} to calculate the division
#' of the network into modules for a network retrieved via \link{dna_network}.
#'
#' @param mat A network matrix found e.g. in nw$networks (nw being an object
#'   generated via \link{dna_network}).
#'
#' @examples
#' \dontrun{
#' dna_downloadJar()
#' dna_init("dna-2.0-beta21.jar")
#' conn <- dna_connection(dna_sample())
#' nw <- dna_network(conn,
#'                   networkType = "onemode")
#' modularity <- lvmod(nw)
#' modularity
#' }
#' @author Philip Leifeld, Johannes B. Gruber
#' @export
#' @importFrom igraph graph.adjacency
#' @importFrom igraph cluster_louvain
#' @importFrom igraph modularity
lvmod <- function(mat) {
  g <- igraph::graph.adjacency(mat, mode = "undirected", weighted = TRUE)
  lv <- igraph::cluster_louvain(g)
  mod <- igraph::modularity(lv)
  return(mod)
}


#' Print the summary of a \code{dna_cluster} object
#'
#' Show details of a \code{dna_cluster} object.
#'
#' Print original call to the function, the information from the
#' \link[stats]{hclust} call and additional variables which can be used for
#' plotting the object.
#'
#' @param x A \code{dna_cluster} object.
#' @param ... Further options (currently not used).
#'
#' @examples
#' \dontrun{
#' dna_downloadJar()
#' dna_init("dna-2.0-beta21.jar")
#' conn <- dna_connection(dna_sample(), verbose = FALSE)
#' clust.l <- dna_cluster(conn)
#' clust.l
#' }
#' @export
#' @importFrom stats na.omit
print.dna_cluster <- function(x, ...) {
  if (!is.null(x$call)) {
    cat("\nCall:\n", deparse(x$call), "\n\n", sep = "")
  }
  if (!is.null(x$method)) {
    cat("Cluster method   :", x$method, "\n")
  }
  if (!is.null(x$dist.method) & !is.na(x$dist.method)) {
    cat("Distance         :", x$dist.method, "\n")
  }
  cat("Number of objects:", length(x$labels), "\n")
  if (length(na.omit(attr(x, "cut"))) > 0) {
    cat("Cut at           :", paste(gsub("cutree.", "",
                                         names(attr(x, "cut"))), "=",
                                    attr(x, "cut"),
                                    collapse = ", "),
        "\n")
  }
  cat("Used for colours :\n", paste(names(attr(x, "colours")),
                                    paste0("\"",
                                           attr(x, "colours"),
                                           "\"\n"),
                                    sep = "      : ",
                                    collapse = " "))
 
  cat("\n")
  invisible(x)
}


#' Print the summary of a \code{dna_connection} object
#'
#' Show details of a \code{dna_connection} object.
#'
#' Print the number of documents and statements to the console after
#' establishing a DNA connection.
#'
#' @param x A \code{dna_connection} object.
#' @param ... Further options (currently not used).
#'
#' @examples
#' \dontrun{
#' dna_downloadJar()
#' dna_init("dna-2.0-beta21.jar")
#' conn <- dna_connection(dna_sample(), verbose = FALSE)
#' conn
#' }
#' @export
print.dna_connection <- function(x, ...) {
  .jcall(x$dna_connection, "V", "rShow")
}


#' Truncate labels
#'
#' Internal function, used to truncate labels
#'
#' @param x A character string
#' @param n Max number of characters to truncate to. Value \code{Inf} turns off
#'   truncation.
#' @param e String added at the end of x to signal it was truncated.
#'
#' @author Johannes B. Gruber
trim <- function(x, n, e = "..."){
  ifelse(nchar(x) > n,
         paste0(gsub("\\s+$", "",
                     strtrim(x, width = n)),
                e),
         x)
}
