
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
  if (!is.null(jarfile)) {
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
  }
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
#' @param verbose Print details and error messages from the call to DNA?
#'
#' @examples
#' \dontrun{
#' dna_init("dna-2.0-beta20.jar")
#' dna_gui()
#' }
#' @export
dna_gui <- function(infile = NULL,
                    javapath = NULL,
                    memory = 1024,
                    verbose = TRUE) {
  djs <- dnaEnvironment[["dnaJarString"]]
  if (is.null(djs)) {
    stop(paste0(djs, " could not be located in directory ",
                getwd(), "."))
  }
  if(!is.null(infile)){
    if (!file.exists(infile)) {
      stop(
        if (grepl("/", infile, fixed = TRUE))
        {paste0("infile ", infile, " could not be located.")}
        else
        {paste0("infile ", infile, " could not be located in working directory ",
                getwd(), ".")
        }
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
  system(paste0(jp, " -jar -Xmx", memory, "M ", djs, f), intern = !verbose)
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
#' dna_init("dna-2.0-beta20.jar")
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
#' dna_init("dna-2.0-beta20.jar")
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
#' @param ... additional arguments passed to \link{dna_network}.
#'
#' @examples
#' \dontrun{
#' dna_init("dna-2.0-beta20.jar")
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
                           timewindow = "no",
                           windowsize = 100,
                           facet = character(),
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

  if( facet == "Authors" ){Authors <- facetValues} else {Authors <- character()}
  if (facet == "Sources"){Sources <- facetValues} else {Sources <- character()}
  if (facet == "Sections"){Sections <- facetValues} else {Sections <- character()}
  if (facet == "Types"){Types <- facetValues} else {Types <- character()}
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
#' @param ... Not used. Additional parameters should be passed on to ggplot2 via
#'   e.g. \code{+ theme_bw()}.
#'
#' @examples
#' \dontrun{
#' dna_init("dna-2.0-beta20.jar")
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


#' Plot agreement and disagreement
#'
#' Plot agreement and disagreement towards statements (i.e. their centrality).
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
#' dna_init("dna-2.0-beta20.jar")
#' conn <- dna_connection(dna_sample())
#'
#' dna_plotCentrality(connection = conn,
#'                    of = "concept",
#'                    colours = FALSE,
#'                    barWidth = 0.5)
#' }
#' @author Johannes B. Gruber
#' @export
#' @import ggplot2
dna_plotCentrality <- function(connection,
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
  
  #count (dis-)agreement per "of"
  dta <- as.data.frame(table(dta$agreement, dta[, of]),
                       stringsAsFactors = FALSE)
  
  #rename columns to work with them more easily
  colnames(dta) <- c("agreement", "concept", "Frequency")
  
  # order data per total mentions (disagreement + agreement)
  dta2 <- stats::aggregate(Frequency ~ concept, sum, data=dta)
  dta2 <- dta2[order(dta2$Frequency, decreasing = TRUE), ]
  # replicate order of dta2$concept to dta
  dta <- dta[order(match(dta$concept, dta2$concept)), ]
  
  # get bar colours
  if (colours){
    col <- dna_attributes(connection = connection, statementType = "DNA Statement",
                          variable = of, values = NULL)
    dta$color <- as.character(col$color[match(dta$concept, col$value)])
  } else {
    dta$color <- "white"
  }
  
  # truncate where "of" is longer than truncate value
  dta$concept <- ifelse(nchar(dta$concept) > truncate,
                        paste0(gsub("\\s+$", "",
                                    strtrim(dta$concept, width = truncate)),
                               "..."),
                        dta$concept
  )
  if(length(dta$concept) / length(unique(dta$concept)) != 2){
    warning("After truncation, some labels are now excatly the same. I will try to fix that.")
    dta2$concept <- ifelse(nchar(dta2$concept) > truncate,
                           paste0(gsub("\\s+$", "",
                                       strtrim(dta2$concept, width = truncate)),
                                  "..."),
                           dta2$concept
    )
    i <- 1
    while(any(duplicated(dta2$concept))){
      dta2$concept[duplicated(dta2$concept)] <- paste0(dta2$concept[duplicated(dta2$concept)], ".", i)
      i <- i + 1
    }
    dta2 <- dta2[rep(seq_len(nrow(dta2)), each=2),]
    dta$concept <- dta2$concept
  }
  
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
  
  high <- length(unique(dta$concept)) + 1.5
  yintercepts <- data.frame(x = c(0, high-1),
                            y = c(0, 0))
  
  g <-  ggplot(dta, aes_string(x = "concept", y = "Frequency")) +
    geom_bar(stat="identity",
             position = position_dodge(),
             fill = dta$color,
             colour = "black",
             width = barWidth) +
    coord_flip() +
    theme_bw() +
    geom_line(aes_string(x = "x", y = "y"), data = yintercepts, size = axisWidth) +
    theme(panel.border = element_blank(),
          panel.grid.major = element_blank(),
          panel.grid.minor = element_blank(),
          axis.line = element_blank(),
          axis.title.x = element_blank(),
          axis.title.y = element_blank(),
          axis.text.x = element_blank(),
          axis.ticks.y = element_blank(),
          axis.text.y = element_text(size = fontSize),
          plot.title = element_text(hjust = ifelse(max(nchar(dta$concept)) > 10, -0.15, 0))) +
    geom_text(aes_string(x = "concept", y = "pos", label = "absFrequency"), size = (fontSize / .pt)) +
    scale_y_discrete(expand = c(0, offset, 0, offset)) + #make some room for labels
    scale_fill_manual("legend", values = dta$color)
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
      scale_x_discrete(expand = c(0, 2, 0, 2))
  }
  return(g)
}


#' Cluster network from a DNA connection
#'
#' Clustering methods for DNA connections.
#'
#' Perform a cluster analysis based on a DNA connection.
#'
#' @param connection A \code{dna_connection} object created by the
#'   \link{dna_connection} function.
#' @param variable The first variable for network construction  (see
#'   \link{dna_network}). The second one defaults to "concept" but can be
#'   provided via ... if necessary.
#' @param rm.duplicates Should duplicates be removed from documents in the
#'   database (see \link{dna_network}. This also influences if
#'   \link[vegan]{vegdist} or \link[stats]{dist} is used to calculate the
#'   dissimilarity matrix.
#' @param clust.method The agglomeration method to be used. See
#'   \link[stats]{hclust}.
#' @param qualifier The qualifier variable. See \link{dna_network}.
#' @param colours Which attribute of variable from DNA should be used to assign
#'   colours? Additionally, can be "membership" if cut.k or cut.h are provided.
#' @param cut.k,cut.h See k and h in \link[stats]{cutree}.
#' @param ... additional arguments passed to \link{dna_network}
#'
#' @examples
#' \dontrun{
#' dna_init("dna-2.0-beta20.jar")
#' conn <- dna_connection(dna_sample())
#'
#' clust.l <- dna_cluster(connection)
#'
#' dna_plotCluster(clust.l)
#' }
#' @author Johannes B. Gruber
#' @export
#' @importFrom vegan vegdist
#' @importFrom stats setNames dist hclust cutree
dna_cluster <- function(connection,
                        variable = "organization",
                        rm.duplicates = FALSE,
                        clust.method = "ward.D2",
                        qualifier = "agreement",
                        colours = "color",
                        cut.k = NULL,
                        cut.h = NULL,
                        ...) {#passed on to dna_network

  dots <- list(...)
  #dots <- list("excludeValues" = excludeValues2)
  #print(unlist(unname(dots["excludeValues"])))
  if ("excludeValues" %in% names(dots)){
    excludeValues <- dots["excludeValues"][[1]]
    dots["excludeValues"] <- NULL
  } else {
    excludeValues <- list()
  }
  if (qualifier %in% names(excludeValues)){
    excl <- unlist(unname(excludeValues[qualifier]))
    excludeValues[qualifier] <- NULL
  }


  # 1. create two-mode networks (with or without duplicates) for all levels of
  # the qualifier variable separately, including isolates
  # find all qualifier levels
  lvls <- do.call(dna_network,
                  c(list(connection = connection,
                         networkType = "eventlist",
                         excludeValues = excludeValues,
                         verbose = FALSE
                  ), dots))

  lvls <- unique(lvls[, qualifier])

  if (exists("excl")) {
    lvls <- lvls[!lvls %in% excl]
    if (length(lvls) < 1){
      stop(paste0(
        "You excluded all levels of \"", qualifier,
        "\". Computation not possible."
      ))
    }}

  dta <- lapply(lvls, function(l){
    # add level of the qualifier variable to exclude
    excludeVals = c(stats::setNames(list(l),
                             nm = qualifier),
                    excludeValues)

    nw <- do.call(dna_network,
                  c(list(connection = connection,
                         networkType = "twomode",
                         variable1 = variable,
                         normalization = "no",
                         isolates = TRUE,
                         duplicates = ifelse(rm.duplicates,
                                             "include",
                                             "document"),
                         qualifier = qualifier,
                         qualifierAggregation = "ignore",
                         excludeValues = excludeVals,
                         verbose = FALSE))
                  #, dots)
    )

    colnames(nw) <- paste(colnames(nw), "-", l)
    return(nw)
  })

  # 2. collate them horizontally usign cbind
  dta <- do.call("cbind", dta)

  # 3. remove lines from the matrix where the row sum is zero
  dta <- dta[rowSums(dta) > 0, ]
  dta <- dta[, colSums(dta) > 0]

  # 4. compute Jaccard dissimilarity matrix using the vegdist function in the
  # vegan package (or dist if If duplicates are included in the affiliation
  # matrices)
  if (rm.duplicates){
    d <-  vegan::vegdist(dta, method = "jaccard")
  } else {
    d <-  dist(dta, method = "euclidean")
  }
  hc <- hclust(d, method = clust.method)
  if(!is.null(c(cut.k, cut.h))){
    hc$group <- cutree(hc, k = cut.k, h = cut.h)
  }
  hc$activity <- rowSums(dta)

  # add colours
  if (!colours == "membership") {
    col <- dna_attributes(connection = connection, statementType = "DNA Statement",
                          variable = variable, values = NULL)
    hc$colours <- col[, colours][match(hc$labels, col$value)]

  } else {
    if(!is.null(c(cut.k, cut.h))){
      hc$colours <- paste("Group", hc$group)
    } else {
      warning("Assigning colours based on cluster membership only works if either cut.k or cut.h are provided")
    }
  }
  return(hc)
}


#' Plots cluster objects
#'
#' Plots objects derived via \link{dna_cluster}.
#'
#' This function is a convenience wrapper for several different dendrogram
#' types, which can be plotted using the \code{ggraph} package.
#'
#' @param dend A \code{dna_cluster} object created by the
#'     \link{dna_cluster} function.
#' @param shape See \link[ggraph]{layout_dendrogram_auto}.
#' @param colour Should the plot be coloured or not (logical).
#' @param activity Should activity of variable in \link{dna_cluster} be used to determine size of lineends (logical).
#' @param lineends Should lineends be displayed (logical).
#' @param colours There are three options from where to derive the colours in
#'   the plot: (1.) "identity" uses the names of variables as colours, fails if
#'   names are not plottable colours; (2.) "manual" provide colours via
#'   custom.colours; (3.) "brewer" automatically select nice colours from a
#'   \code{RColorBrewer} palette (palette can be set in custom.colours).
#' @param custom.colours Either provide enough colours to manually set the
#'   colours in the plot (if colours = "manual") or select a palette from
#'   \code{RColorBrewer} (if colours = "brewer").
#' @param branch.colour Provide one colour in which all branches are coloured.
#' @param leaf.linetype,branch.linetype "a" for straight line or "b" for dotted line.
#' @param lineWidth Width of all lines.
#' @param lineAlpha Alpha of all lines.
#' @param endsAlpha Alpha of all lineends
#' @param fontSize Set the font size for the entire plot.
#' @param theme See themes in \code{ggplot2}. The theme "bw" was customised to look best with dendrograms.
#' @param truncate Sets the number of characters to which labels should be truncated.
#' @param leaf.labels Either "ticks" to display the labels as axis ticks or "node" to label nodes directly.
#' @param circular Logical. Should the layout be transformed to a circular representation. See  \link[ggraph]{layout_dendrogram_auto}.
#' @param show.legend Logical. Should the legend be displayed.
#'
#' @param ... additional arguments passed to \link{dna_network}
#'
#' @examples
#' \dontrun{
#' dna_init("dna-2.0-beta20.jar")
#' conn <- dna_connection(dna_sample())
#' at <- dna_attributes(conn, "DNA Statement", "organization")
#' dna_plotCentrality(conn)
#' }
#' @author Johannes B. Gruber
#' @export
#' @import ggraph
#' @importFrom stats as.dendrogram is.leaf dendrapply
dna_plotCluster <- function(dend,
                            shape = "elbows",
                            colour = TRUE,
                            activity = TRUE,
                            lineends = TRUE,
                            colours = "identity",
                            custom.colours = character(),
                            branch.colour = "#636363",
                            leaf.linetype = "a",
                            branch.linetype = "a",
                            lineWidth = 1,
                            lineAlpha = 1,
                            endsAlpha = 1,
                            fontSize = 12,
                            theme = "bw",
                            truncate = 40,
                            leaf.labels = "ticks",
                            circular = FALSE,
                            show.legend = FALSE,
                            ...) {

  # truncate lables
  dend$labels_short <- ifelse(nchar(dend$labels) > truncate,
                              paste0(gsub("\\s+$", "",
                                          strtrim(dend$labels, width = truncate)),
                                     "..."),
                              dend$labels)

  # format as dendrogram
  hierarchy <- stats::as.dendrogram(dend)
  # Add colours
  hierarchy <- stats::dendrapply(hierarchy, function(x) {
    if (stats::is.leaf(x)) {
      attr(x, "Colour") <- as.character(dend$colours[match(as.character(labels(x)), dend$labels)])
      attr(x, "Activity") <- unname(dend$activity[dend$order[match(as.character(labels(x)),
                                                                   dend$labels)]])
      attr(x, "labels_short") <- dend$labels_short[match(as.character(labels(x)),
                                                         dend$labels)]
      attr(x, "linetype") <- leaf.linetype
    } else {
      attr(x, "Colour") <- branch.colour
      attr(x, "Activity") <- 0
      attr(x, "labels_short") <- ""
      attr(x, "linetype") <- branch.linetype
    }
    attr(x, "edgePar") <- list(cols = attr(x, "Colour"),
                               linetype = attr(x, "linetype"))
    attr(x, "nodePar") <- list(cols = attr(x, "Colour"),
                               Activity = attr(x, "Activity"),
                               labels_short = attr(x, "labels_short"))
    x
  })

  # create initial dedrogram
  dg <- ggraph::ggraph(graph = hierarchy,
                       layout = "dendrogram",
                       circular = circular)

  # add the shape
  if (shape == "elbows"){
    if(colour){
      dg <- dg +
        geom_edge_elbow(aes_string(colour = "cols",
                            edge_linetype = "linetype"),
                        show.legend = show.legend,
                        width = lineWidth,
                        alpha = lineAlpha)
    } else {
      dg <- dg +
        geom_edge_elbow(aes_string(edge_linetype = "linetype"),
                        show.legend = show.legend,
                        width = lineWidth,
                        alpha = lineAlpha)
    }
  }
  if (shape == "link"){
    if(colour){
      dg <- dg +
        geom_edge_link(aes_string(colour = "cols",
                           edge_linetype = "linetype"),
                       show.legend = show.legend,
                       width = lineWidth,
                       alpha = lineAlpha)
    } else {
      dg <- dg +
        geom_edge_link(aes_string(edge_linetype = "linetype"),
                       show.legend = show.legend,
                       width = lineWidth,
                       alpha = lineAlpha)
    }
  }
  if (shape == "diagonal"){
    if(colour){
      dg <- dg +
        geom_edge_diagonal(aes_string(colour = "cols",
                               edge_linetype = "linetype"),
                           show.legend = show.legend,
                           width = lineWidth,
                           alpha = lineAlpha)
    } else {
      dg <- dg +
        geom_edge_diagonal(aes_string(edge_linetype = "linetype"),
                           show.legend = show.legend,
                           width = lineWidth,
                           alpha = lineAlpha)
    }
  }
  if (shape == "arc"){
    if(colour){
      dg <- dg +
        geom_edge_arc(aes_string(colour = "cols",
                          edge_linetype = "linetype"),
                      show.legend = show.legend,
                      width = lineWidth,
                      alpha = lineAlpha)
    } else {
      dg <- dg +
        geom_edge_arc(aes_string(edge_linetype = "linetype"),
                      show.legend = show.legend,
                      width = lineWidth,
                      alpha = lineAlpha)
    }
  }
  if (shape == "fan"){
    if(colour){
      dg <- dg +
        geom_edge_fan(aes_string(colour = "cols",
                          edge_linetype = "linetype"),
                      show.legend = show.legend,
                      width = lineWidth,
                      alpha = lineAlpha)
    } else {
      dg <- dg +
        geom_edge_fan(aes_string(edge_linetype = "linetype"),
                      show.legend = show.legend,
                      width = lineWidth,
                      alpha = lineAlpha)
    }
  }
  dend$colours <- as.factor(dend$colours)
  if (colours == "identity" & colour) {
    dg <- dg +
      scale_edge_colour_identity() # gives error if colour names are not actual colours
  }
  if (colours == "manual" & colour) {
    cols2 <- c(branch.colour, custom.colours)
    cols2 <- setNames(cols2, nm = c(branch.colour, levels(dend$colours)))
    dg <- dg +
      scale_edge_colour_manual(values = cols2)
  }
  if (colours == "brewer" & colour) {
    if (length(custom.colours) == 0) {
      custom.colours = "Set3"
    }
    cols3 <- c(branch.colour,
               scales::brewer_pal(type = "div",
                                  palette = custom.colours)(length(levels(dend$colours))))
    cols3 <- setNames(cols3, nm = c(branch.colour, levels(dend$colours)))
    dg <- dg +
      scale_edge_colour_manual(values = cols3)
  }

  #theme
  if (theme == "bw") {
    dg <- dg +
      theme_bw() +
      theme(panel.border = element_blank(),
            axis.title = element_blank(),
            panel.grid.major = element_blank(),
            panel.grid.minor = element_blank(),
            text = element_text(size = fontSize),
            axis.line = element_blank(),
            #axis.text.y = element_blank(),
            axis.text.x = element_text(angle = 90, vjust = 0.5, hjust = 1))
  }
  if (theme == "ggplot") {
    dg <- dg +
      theme(text = element_text(size = fontSize))
  }
  if (theme == "void") {
    dg <- dg +
      theme_void() +
      theme(text = element_text(size = fontSize))
  }
  if (theme == "light") {
    dg <- dg +
      theme_light() +
      theme(text = element_text(size = fontSize))
  }
  if (theme == "dark") {
    dg <- dg +
      theme_dark() +
      theme(text = element_text(size = fontSize))
  }
  # labels
  if (leaf.labels == "ticks") {
    dg <- dg +
      scale_x_continuous(breaks = seq(0, length(dend$labels)-1, by = 1),
                         label = dend$labels_short)
  }
  if (leaf.labels == "nodes") {
    if (circular == FALSE) {
      if(colour){
        dg <- dg +
          geom_node_text(aes_string(label = "labels_short",
                             filter = "leaf",
                             colour = "cols"),
                         angle=270,
                         hjust=0,
                         nudge_y = -0.02,
                         size = fontSize)
      } else {
        dg <- dg +
          geom_node_text(aes_string(label = "labels_short",
                             filter = "leaf"),
                         angle=270,
                         hjust=0,
                         nudge_y = -0.02,
                         size = fontSize)
      }
      dg <- dg +
        expand_limits(y = c(-2.3, 2.3))
    } else {
      if(colour){
        dg <- dg +
          geom_node_text(aes(filter = leaf,
                             angle = ifelse(node_angle(x, y) < 270 & node_angle(x, y) > 90,
                                            node_angle(x, y) + 180,
                                            node_angle(x, y)),
                             label = labels_short,
                             hjust = ifelse(node_angle(x, y) < 270 & node_angle(x, y) > 90,
                                            1.05,
                                            -0.05),
                             colour = cols),
                         show.legend = show.legend) +
          expand_limits(x = c(-2.3, 2.3), y = c(-2.3, 2.3))
      } else {
        dg <- dg +
          geom_node_text(aes(filter = leaf,
                             angle = ifelse(node_angle(x, y) < 270 & node_angle(x, y) > 90,
                                            node_angle(x, y) + 180,
                                            node_angle(x, y)),
                             label = labels_short,
                             hjust = ifelse(node_angle(x, y) < 270 & node_angle(x, y) > 90,
                                            1.05,
                                            -0.05)),
                         show.legend = show.legend) +
          expand_limits(x = c(-2.3, 2.3), y = c(-2.3, 2.3))
      }
    }
  }

  # line ends
  if (lineends) {
    if (activity & colour){
      dg <- dg +
        geom_node_point(aes_string(filter = "leaf",
                            colour = "cols",
                            size = "Activity",
                            shape = "cols"),
                        show.legend = show.legend,
                        alpha = endsAlpha)
    }
    if (!activity & colour){
      dg <- dg +
        geom_node_point(aes_string(filter = "leaf",
                            colour = "cols",
                            shape = "cols"),
                        show.legend = show.legend,
                        alpha = endsAlpha)
    }
    if (activity & !colour){
      dg <- dg +
        geom_node_point(aes_string(filter = "leaf",
                            size = "Activity",
                            shape = "cols"),
                        show.legend = show.legend,
                        alpha = endsAlpha)
    }
    if (!activity & !colour){
      dg <- dg +
        geom_node_point(aes_string(filter = "leaf",
                            shape = "cols"),
                        show.legend = show.legend,
                        alpha = endsAlpha)
    }
  }
  # cuttree rectangles see
  # stackoverflow.com/questions/24140339/tree-cut-and-rectangles-around-clusters-for-a-horizontal-dendrogram-in-r

  # color node text and points
  # colour
  if (colour) {
    if (colours == "identity") {
      dg <- dg +
        scale_colour_identity()
    }
    if (colours == "manual") {
      dg <- dg +
        scale_colour_manual(values = cols2[-1])
    }
    if (colours == "brewer") {
      dg <- dg +
        scale_colour_manual(values = cols3[-1])
    }
  }
  return(dg)
}