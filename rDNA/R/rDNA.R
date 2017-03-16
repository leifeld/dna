
# some settings
dnaEnvironment <- new.env(hash = TRUE, parent = emptyenv())

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
#' @param jarfile The file name of the DNA jar file, e.g., \code{"dna-2.0-beta19.jar"}.
#' 
#' @examples
#' download.file("https://github.com/leifeld/dna/releases/download/v2.0-beta.19/dna-2.0-beta19.jar", 
#'               destfile = "dna-2.0-beta19.jar", mode = "wb")
#' dna_init("dna-2.0-beta19.jar")
#' @export
dna_init <- function(jarfile = "dna-2.0-beta19.jar") {
  assign("dnaJarString", jarfile, pos = dnaEnvironment)
  message(paste("Jar file:", dnaEnvironment[["dnaJarString"]]))
  .jinit(dnaEnvironment[["dnaJarString"]], force.init = TRUE)
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
#' 
#' @examples
#' download.file("https://github.com/leifeld/dna/releases/download/v2.0-beta.19/dna-2.0-beta19.jar", 
#'               destfile = "dna-2.0-beta19.jar", mode = "wb")
#' dna_init("dna-2.0-beta19.jar")
#' dna_gui()
#' @export
dna_gui <- function(infile = NULL, javapath = NULL, memory = 1024) {
  djs <- dnaEnvironment[["dnaJarString"]]
  if (is.null(djs)) {
    stop(paste0(dna.jar.file, " could not be located in directory ", getwd(), "."))
  } else {
    if (is.null(infile)) {
      f <- ""
    } else {
      f <- paste0(" ", infile)
    }
    if (is.null(javapath)) {
      jp <- "java"
    } else if (grepl("/$", javapath)) {
      jp <- paste0(javapath, "java")
    } else {
      jp <- paste0(javapath, "/java")
    }
    system(paste0(jp, " -jar -Xmx", memory, "M ", djs, f))
  }
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
#'     loading the database?
#' 
#' @examples
#' download.file("https://github.com/leifeld/dna/releases/download/v2.0-beta.19/dna-2.0-beta19.jar", 
#'               destfile = "dna-2.0-beta19.jar", mode = "wb")
#' download.file("https://github.com/leifeld/dna/releases/download/v2.0-beta.19/sample.dna", 
#'               destfile = "sample.dna", mode = "wb")
#' dna_init("dna-2.0-beta19.jar")
#' dna_connection("sample.dna")
#' @export
dna_connection <- function(infile, login = NULL, password = NULL, verbose = TRUE) {
  if (is.null(login) || is.null(password)) {
    export <- .jnew("dna.export/Exporter", "sqlite", infile, "", "", verbose)
  } else {
    export <- .jnew("dna.export/Exporter", "mysql", infile, login, password, verbose)
  }
  obj <- list(dna_connection = export)
  class(obj) <- "dna_connection"
  if (verbose == TRUE) {
    print(obj)
  }
  return(obj)
}


#' Print the summary of a \code{dna_connection} object
#' 
#' Show details of a \code{dna_connection} object.
#' 
#' Print the number of documents and statements to the console after 
#' establishing a DNA connection.
#' 
#' @param x A \code{dna_connection} object.
#' 
#' @examples
#' download.file("https://github.com/leifeld/dna/releases/download/v2.0-beta.19/dna-2.0-beta19.jar", 
#'               destfile = "dna-2.0-beta19.jar", mode = "wb")
#' download.file("https://github.com/leifeld/dna/releases/download/v2.0-beta.19/sample.dna", 
#'               destfile = "sample.dna", mode = "wb")
#' dna_init("dna-2.0-beta19.jar")
#' conn <- dna_connection("sample.dna", verbose = FALSE)
#' conn
#' @export
print.dna_connection <- function(x) {
  .jcall(x$dna_connection, "V", "rShow")
}


# retrieve attribute data.frame
dna_attributes <- function(dna_connection, 
                           statementType = "DNA Statement", 
                           variable = "organization") {
  
  .jcall(dna_connection$dna_connection, 
         "V", 
         "rAttributes", 
         variable, 
         statementType)
  dat <- data.frame(id = .jcall(dna_connection$dna_connection, "[I", "getAttributeIds"), 
                    value = .jcall(dna_connection$dna_connection, "[S", "getAttributeValues"), 
                    color = .jcall(dna_connection$dna_connection, "[S", "getAttributeColors"), 
                    type = .jcall(dna_connection$dna_connection, "[S", "getAttributeTypes"), 
                    alias = .jcall(dna_connection$dna_connection, "[S", "getAttributeAlias"), 
                    note = .jcall(dna_connection$dna_connection, "[S", "getAttributeNotes"), 
                    frequency = .jcall(dna_connection$dna_connection, "[I", "getAttributeFrequencies", variable), 
                    "in dataset" = .jcall(dna_connection$dna_connection, "[Z", "getAttributeInDataset", statementType), 
                    "in network" = .jcall(dna_connection$dna_connection, "[Z", "getAttributeInNetwork", statementType), 
                    check.names = FALSE
  )
  return(dat)
}


# compute a one-mode or two-mode network matrix
dna_network <- function(dna_connection, 
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
  
  if (length(excludeValues) > 0) {
    dat <- matrix("", nrow = sum(sapply(excludeValues, length)), ncol = 2)
    count = 0
    for (i in 1:length(excludeValues)) {
      if (length(excludeValues[[i]]) > 0) {
        for (j in 1:length(excludeValues[[i]])) {
          count = count + 1
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
  
  if (length(excludeAuthors) == 1) {
    excludeAuthors = c(excludeAuthors, excludeAuthors)
  }
  if (length(excludeSources) == 1) {
    excludeSources = c(excludeSources, excludeSources)
  }
  if (length(excludeSections) == 1) {
    excludeSections = c(excludeSections, excludeSections)
  }
  if (length(excludeTypes) == 1) {
    excludeTypes = c(excludeTypes, excludeTypes)
  }
  
  .jcall(dna_connection$dna_connection, 
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
    stop("Event lists are currently not supported by rDNA and may be added at some point.")
  } else {
    mat <- .jcall(dna_connection$dna_connection, "[[D", "getMatrix", simplify = TRUE)
    rownames(mat) <- .jcall(dna_connection$dna_connection, "[S", "getRowNames", simplify = TRUE)
    colnames(mat) <- .jcall(dna_connection$dna_connection, "[S", "getColumnNames", simplify = TRUE)
    return(mat)
  }
}
