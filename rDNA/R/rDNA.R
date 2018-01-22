
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
#' @param jarfile The file name of the DNA jar file, e.g., 
#'     \code{"dna-2.0-beta20.jar"}.
#' @param memory The amount of memory in megabytes to allocate to DNA, for 
#'     example \code{1024} or \code{4096}.
#' 
#' @examples
#' \dontrun{
#' dna_init("dna-2.0-beta20.jar")
#' }
#' @export
#' @import rJava
dna_init <- function(jarfile = "dna-2.0-beta20.jar", memory = 1024) {
  assign("dnaJarString", jarfile, pos = dnaEnvironment)
  message(paste("Jar file:", dnaEnvironment[["dnaJarString"]]))
  .jinit(dnaEnvironment[["dnaJarString"]], 
         force.init = TRUE, 
         parameters = paste0("-Xmx", memory, "m"))
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
#' \dontrun{
#' dna_init("dna-2.0-beta20.jar")
#' dna_gui()
#' }
#' @export
dna_gui <- function(infile = NULL, javapath = NULL, memory = 1024) {
  djs <- dnaEnvironment[["dnaJarString"]]
  if (is.null(djs)) {stop(paste0(djs, " could not be located in directory ", getwd(), "."))}
  if(!is.null(infile)){
    if (!file.exists(infile)) {stop(
      if (grepl("/", infile, fixed = TRUE)) 
      {paste0("infile ", infile, " could not be located.")}
      else 
      {paste0("infile ", infile, " could not be located in working directory ", getwd(), ".")}
    )}
  }
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


#' Provides a small sample database
#' 
#' Retrieves the location of a small local .dna sample file.
#' 
#' A small sample database to test the functions of rDNA.
#' 
#' @examples
#' \dontrun{
#' dna_init("dna-2.0-beta20.jar")
#' dna_connection(dna_sample())
#' }
#' @author Johannes Gruber
#' @export
dna_sample <- function(){
  system.file("extdata", "sample.dna", package = "rDNA")
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
#' \dontrun{
#' dna_init("dna-2.0-beta20.jar")
#' dna_connection(dna_sample())
#' }
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
#' @param ... Further options (currently not used).
#' 
#' @examples
#' \dontrun{
#' dna_init("dna-2.0-beta20.jar")
#' conn <- dna_connection(dna_sample(), verbose = FALSE)
#' conn
#' }
#' @export
print.dna_connection <- function(x, ...) {
  .jcall(x$dna_connection, "V", "rShow")
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
#' dna_init("dna-2.0-beta20.jar")
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
#'     \code{"acrosstime"} (for counting only one identical statement across 
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
#' dna_init("dna-2.0-beta20.jar")
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
    excludeTypes = c(excludeTypes, excludeTypes)
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
  if (!is.null(excludeValues) && length(excludeValues) > 0) {
    for (i in 1:length(excludeValues)) {
      if (length(excludeValues[[i]]) == 1) {
        excludeValues[[i]] = c(excludeValues[[i]], excludeValues[[i]])
      }
    }
  }
  
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
    return(dta)
  } else if (timewindow == "no") {
    mat <- .jcall(connection$dna_connection, "[[D", "getMatrix", simplify = TRUE)
    rownames(mat) <- .jcall(connection$dna_connection, "[S", "getRowNames", simplify = TRUE)
    colnames(mat) <- .jcall(connection$dna_connection, "[S", "getColumnNames", simplify = TRUE)
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
    return(dta)
  }
}
