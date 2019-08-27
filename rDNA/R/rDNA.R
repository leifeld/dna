# Startup ----------------------------------------------------------------------

dnaEnvironment <- new.env(hash = TRUE, parent = emptyenv())

# display version number and date when the package is attached
#' @importFrom utils packageDescription
.onAttach <- function(libname, pkgname) {
  desc <- packageDescription(pkgname, libname)
  packageStartupMessage(
    'Version:      ', desc$Version, '\n',
    'Date:         ', desc$Date, '\n',
    'Author:       Philip Leifeld  (University of Glasgow)\n',
    'Contributors: Johannes B. Gruber (University of Glasgow),\n',
    '              Tim Henrichsen  (Scuola superiore Sant\'Anna Pisa)\n',
    'Project home: github.com/leifeld/dna'
  )
}

# more settings which quiet concerns of R CMD check about ggplot and dplyr pipelines
if (getRversion() >= "2.15.1") {
  utils::globalVariables(
    c("rn",
      "cols3",
      "labels_short",
      "leaf",
      "x",
      "y",
      "mean_dim1",
      "mean_dim2",
      "name",
      "it",
      "color"
    )
  )
}


# Data access ------------------------------------------------------------------

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
#'   database to load
#' @param login The user name for accessing the database (only applicable
#'   to remote mySQL databases; can be \code{NULL} if a local .dna file
#'   is used).
#' @param password The password for accessing the database (only applicable
#'   to remote mySQL databases; can be \code{NULL} if a local .dna file
#'   is used).
#' @param create If the file or remote database structure does not exist yet,
#'   should it be created with default values?
#' @param verbose Print details the number of documents and statements after
#'   loading the database?
#'
#' @examples
#' \dontrun{
#' dna_init()
#' dna_connection(dna_sample())
#' }
#'
#' @author Philip Leifeld
#'
#' @importFrom rJava .jnew
#' @export
dna_connection <- function(infile, login = NULL, password = NULL, create = FALSE, verbose = TRUE) {
  if (is.null(login) & is.null(password) & !file.exists(infile) & !isTRUE(create)) {
    if (grepl("/", infile, fixed = TRUE)) {
      msg <- paste0("infile '",
                    infile,
                    "' could not be located. Use 'create = TRUE' to create a new database.")
    } else {
      msg <- paste0("infile '",
                    infile,
                    "' could not be located in working directory '",
                    getwd(),
                    "'. Use 'create = TRUE' to create a new database.")
    }
    stop(msg)
  }
  if (!grepl("/", infile, fixed = TRUE)) {
    infile <- paste0(getwd(), "/", infile)
  }
  if (is.null(dnaEnvironment[["dnaJarString"]])) {
    stop("No connection between rDNA and DNA detected. Maybe dna_init() would help.")
  }
  if (is.null(login) || is.null(password)) {
    export <- .jnew("dna.export/ExporterR", "sqlite", infile, "", "", verbose)
  } else {
    export <- .jnew("dna.export/ExporterR", "mysql", infile, login, password, verbose)
  }
  obj <- list(dna_connection = export)
  class(obj) <- "dna_connection"
  if (isTRUE(verbose)) {
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
#' dna_init()
#' conn <- dna_connection(dna_sample(), verbose = FALSE)
#' conn
#' }
#'
#' @author Philip Leifeld
#'
#' @importFrom rJava .jcall
#' @export
print.dna_connection <- function(x, ...) {
  cat(.jcall(x$dna_connection, "S", "rShow"))
}

#' Print the summary of a \code{dna_dataframe} object
#'
#' Show details of a \code{dna_dataframe} object.
#'
#' Print a data frame returned by \link{dna_getDocuments},
#' \link{dna_getStatements}, or \link{dna_getAttributes}. The only difference
#' between this print method and the default print method for data frames is
#' that the \code{text} column and other columns containing character strings
#' are truncated for better readability on screen.
#'
#' @param x A \code{dna_connection} object.
#' @param truncate Number of characters to which character columns in the data
#'   frame should be truncated.
#' @param ... Further options (currently not used).
#'
#' @author Philip Leifeld
#'
#' @export
print.dna_dataframe <- function(x, truncate = 20, ...) {
  x2 <- x
  class(x2) <- class(x2)[-1]
  x2[, unlist(sapply(x2, is.character))] <- apply(x2[, unlist(sapply(x2, is.character))],
                                                  1:2,
                                                  function(t) trim(x = trimws(t), n = truncate, e = "*"))
  cat("Note: text denoted by * is truncated to", truncate, "characters for readability.\n\n")
  print(x2)
}

#' Download the binary DNA jar file
#'
#' Downloads the newest released DNA jar file necessary for running
#' \link{dna_init}.
#'
#' This function uses GitHub's API to download the latest DNA jar file to the
#' working directory.
#'
#' @param path Directory path in which the jar file will be stored.
#' @param force Logical. Should the file be overwritten if it already exists?
#' @param returnString Logical. Return the file name of the downloaded jar file?
#'
#' @author Philip Leifeld, Johannes B. Gruber
#'
#' @importFrom utils download.file
#' @export
dna_downloadJar <- function(path = paste0(dirname(system.file(".", package = "rDNA")), "/", "extdata"),
                            force = FALSE,
                            returnString = FALSE) {
  u <- url("https://api.github.com/repos/leifeld/dna/releases")
  open(u)
  lines <- readLines(u, warn = FALSE)
  m <- gregexpr("https://github.com/leifeld/dna/releases/download/.{3,15}?/dna-.{3,15}?\\.jar", lines, perl = TRUE)
  m <- unlist(regmatches(lines, m))[1]
  close(u)
  filename <- strsplit(m[1], "/")[[1]]
  filename <- filename[length(filename)]
  filename <- paste0(path, ifelse(endsWith(path, "/"), "", "/"), filename)
  if (force == TRUE || (force == FALSE && !file.exists(filename))) {
    download.file(url = m[1], destfile = filename, mode = "wb", cacheOK = FALSE)
  } else {
    warning("Latest DNA jar file already exists. Use 'force = TRUE' to overwrite it.")
  }
  if (returnString == TRUE) {
    return(filename)
  }
}

#' Open the DNA GUI
#'
#' Start DNA graphical user interface and optionally load a database.
#'
#' Start the DNA graphical user interface (GUI). Optionally load a .dna database
#' or a mySQL online database upon start-up of the GUI.
#'
#' @param infile The file name of the .dna database or the URL of the mySQL
#'   database to load upon start-up of the GUI or a \link{dna_connection}
#'   object.
#' @param login If \code{infile} is a mySQL connection string, \code{login} is
#'   the login user name for the database. This argument is not used if the
#'   \code{infile} is a file-based database.
#' @param password If \code{infile} is a mySQL connection string,
#'   \code{password} is the password for the database. This argument is not used
#'   if the \code{infile} is a file-based database.
#' @param javapath The path to the \code{java} command. This may be useful if
#'   the CLASSPATH is not set and the java command can not be found. Java
#'   is necessary to start the DNA GUI.
#' @param memory The amount of memory in megabytes to allocate to DNA, for
#'   example \code{1024} or \code{4096}.
#' @param verbose Print details and error messages from the call to DNA?
#'
#' @examples
#' \dontrun{
#' dna_init()
#' dna_gui()
#' }
#'
#' @author Philip Leifeld
#'
#' @importFrom rJava J
#' @export
dna_gui <- function(infile = NULL,
                    login = NULL,
                    password = NULL,
                    javapath = NULL,
                    memory = 1024,
                    verbose = TRUE) {
  if (is.null(dnaEnvironment[["dnaJarString"]])) {
    stop("No connection between rDNA and DNA detected. Maybe dna_init() would help.")
  }
  djs <- dnaEnvironment[["dnaJarString"]]
  if (is.null(djs)) {
    stop(paste0("'", djs, "' could not be located in working directory '", getwd(), "'."))
  }
  if (!is.null(infile)) {
    if (class(infile) == "dna_connection") {
      if (is.null(login)) {
        login <- J(infile$dna_connection, "getLogin")
      }
      if (is.null(password)) {
        password <- J(infile$dna_connection, "getPassword")
      }
      infile <- J(infile$dna_connection, "getDbfile")
    }
    if (!file.exists(infile)) {
      stop(
        if (grepl("/", infile, fixed = TRUE)) {
          paste0("'", infile, "' could not be located.")
        } else {
          paste0("'", infile, "' could not be located in working directory '", getwd(), "'.")
        }
      )
    }
  }

  if (is.null(infile)) {
    f <- ""
  } else if (is.null(login) || is.null(password) || (login == "" && password == "")) {
    f <- paste0(" \"", infile, "\"")
  } else {
    f <- paste0(" \"", infile, "\" \"", login, "\" \"", password, "\"")
  }
  if (is.null(javapath)) {
    jp <- "java"
  } else if (grepl("/$", javapath)) {
    jp <- paste0(javapath, "java")
  } else {
    jp <- paste0(javapath, "/java")
  }
  if (verbose == TRUE) {
    message("To return to R, close the DNA window when done.")
  }
  system(paste0(jp, " -jar -Xmx", memory, "M \"", djs, "\"", f), intern = !verbose)
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
#'   \code{"dna-2.0-beta23.jar"}. Will be auto-detected by choosing the most
#'   recent version stored in the library path or working directory if
#'   \code{jarfile = NULL}.
#' @param memory The amount of memory in megabytes to allocate to DNA, for
#'   example \code{1024} or \code{4096}.
#' @param returnString Return a character object representing the jar file name?
#'
#' @author Philip Leifeld
#'
#' @export
#' @import rJava
dna_init <- function(jarfile = NULL, memory = 1024, returnString = FALSE) {
  if (is.null(jarfile) || is.na(jarfile)) {

    # auto-detect file name in library directory
    path <- paste0(dirname(system.file(".", package = "rDNA")), "/", "extdata")
    files <- dir(path)
    files <- files[grepl("^dna-.+\\.jar$", files)]
    files <- sort(files)
    if (length(files) > 0) {
      jarfile <- paste0(path, "/", files[length(files)])
    }

    # auto-detect file name in working directory
    jarfile_wd <- NULL
    path_wd <- getwd()
    files_wd <- dir(path_wd)
    files_wd <- files_wd[grepl("^dna-.+\\.jar$", files_wd)]
    files_wd <- sort(files_wd)
    if (length(files_wd) > 0) {
      jarfile_wd <- paste0(path_wd, "/", files_wd[length(files_wd)])
    }

    # use file in working directory if version is more recent or none found in library path
    if ((!is.null(jarfile) && !is.null(jarfile_wd) && basename(jarfile_wd) > basename(jarfile)) || is.null(jarfile)) {
      jarfile <- jarfile_wd
    }

    # if none was found whatsoever, attempt to download to library path
    if (is.null(jarfile)) {
      message("No jar file found. Trying to download most recent version to library path.")
      jarfile <- dna_downloadJar(path = path, returnString = TRUE)
      message("Done.")
    }
  }
  if (is.null(jarfile) || length(jarfile) == 0) {
    message("No DNA jar file found in the library path or working directory.")
    if (isTRUE(returnString)) {
      return(NULL)
    }
  }
  if (!is.character(jarfile) || length(jarfile) > 1 || !grepl("^dna-.+\\.jar$", basename(jarfile))) {
    stop("'jarfile' must be a character object of length 1 that points to the DNA jar file.")
  }
  if (!file.exists(jarfile)) {
    stop(paste0("jarfile '", jarfile, "' could not be located."))
  }
  assign("dnaJarString", jarfile, pos = dnaEnvironment)
  message(paste("Jar file:", dnaEnvironment[["dnaJarString"]]))
  .jinit(dnaEnvironment[["dnaJarString"]],
         force.init = TRUE,
         parameters = paste0("-Xmx", memory, "m"))
  if (isTRUE(returnString)) {
    return(jarfile)
  }
}

#' Provides a small sample database
#'
#' Copies a small .dna sample file to the current working directory and returns
#' the location of this newly created file.
#'
#' A small sample database to test the functions of rDNA.
#'
#' @param overwrite Logical. Should sample.dna be overwritten if found in the
#'   current working directory?
#' @param verbose Display warning message if file exists in current wd.
#'
#' @examples
#' \dontrun{
#' dna_init()
#' dna_connection(dna_sample())
#' }
#'
#' @author Johannes B. Gruber
#'
#' @export
dna_sample <- function(overwrite = FALSE,
                       verbose = TRUE) {
  if (file.exists(paste0(getwd(), "/sample.dna")) & overwrite == FALSE) {
    if (verbose) {
      warning(
        "Sample file already exists in working directory. Use 'overwrite = TRUE' to create fresh sample file."
      )
    }
  } else {
    file.copy(from = system.file("extdata", "sample.dna", package = "rDNA"),
              to = paste0(getwd(), "/sample.dna"),
              overwrite = overwrite)
  }
  return(paste0(getwd(), "/sample.dna"))
}


# Data management --------------------------------------------------------------

#' Add an attribute to the DNA database
#'
#' Add a new attribute to the DNA database.
#'
#' The \code{dna_addAttribute} function can add new attributes to an existing
#' DNA database. Attributes are the annotations that are coded within a
#' variable, along with some meta-data. For example, if the variable is
#' "organization", then an attribute value could be "Environmental Protection
#' Agency", and other attribute meta-data for this value could be its color,
#' actor type etc. The \code{dna_addAttribute} function can not only be used to
#' add entries to the attributes table in an ongoing project, but also to
#' pre-populate a DNA database with values for deductive coding. Either way, the
#' user supplies a \link{dna_connection} object as well as various details about
#' the attribute to be added, for example the value, color, type etc. The
#' attribute ID will be automatically generated and can be returned if
#' \code{returnID} is set to \code{TRUE}.
#'
#' @param connection A \code{dna_connection} object created by the
#'   \code{dna_connection} function.
#' @param statementType The ID of the statement type (as an integer) or the name
#'   of the statement type (as a character object) in which the variable is
#'   defined.
#' @param variable The name of the variable for which attribute data should be
#'   retrieved, for example \code{"organization"} or \code{"concept"}.
#' @param value The value of the new attribute as a character object.
#' @param color A character object containing the color of the new document as a
#'   hexadecimal RGB value.
#' @param type The type of the new attribute as a character object.
#' @param alias A character object containing the alias of the new attribute.
#' @param notes The notes of the new attribute as a character object.
#' @param returnID Return the ID of the newly created attribute as a numeric
#'   value?
#' @param verbose Print details?
#'
#' @author Philip Leifeld
#'
#' @importFrom rJava .jcall
#' @export
dna_addAttribute <- function(connection,
                             statementType = 1,
                             variable = "organization",
                             value = "new value",
                             color = "#000000",
                             type = "",
                             alias = "",
                             notes = "",
                             returnID = FALSE,
                             verbose = TRUE) {
  if (is.numeric(statementType) && !is.integer(statementType)) {
    statementType <- as.integer(statementType)
  }
  if (!class(statementType) %in% c("character", "integer")) {
    stop("The statement type must be either an integer value or a character object.")
  }
  if (length(statementType) > 1) {
    stop("Only one single statement type must be provided, not multiple types.")
  }
  if (!is.character(variable)) {
    stop("The variable must be provided as a character object.")
  }
  if (length(variable) > 1) {
    stop("Only one single variable must be provided, not multiple types.")
  }
  if (!is.character(value)) {
    stop("The value must be provided as a character object.")
  }
  if (length(value) > 1) {
    stop("Only one single value must be provided, not multiple values.")
  }
  if (length(color) != 1) {
    stop("'color' must be an object of length 1.")
  }
  color <- col2hex(color)
  if (!is.character(type)) {
    stop("The type must be provided as a character object.")
  }
  if (length(type) > 1) {
    stop("Only one single type must be provided, not multiple types.")
  }
  if (!is.character(alias)) {
    stop("The alias must be provided as a character object.")
  }
  if (length(alias) > 1) {
    stop("Only one single alias must be provided, not multiple aliases.")
  }
  if (!is.character(notes)) {
    stop("The notes must be provided as a character object.")
  }
  if (length(notes) > 1) {
    stop("Only one single note must be provided, not multiple notes.")
  }
  id <- .jcall(connection$dna_connection,
               "I",
               "addAttribute",
               statementType,
               variable,
               value,
               color,
               type,
               alias,
               notes)
  if (verbose == TRUE) {
    message("A new attribute with ID ", id, " was added to the database.")
  }
  if (returnID == TRUE) {
    return(id)
  }
}

#' Add a new coder to the DNA database
#'
#' Add a new coder to the DNA database.
#'
#' The \code{dna_addCoder} function can add a new coder to an existing DNA
#' database. The user supplies a \link{dna_connection} object, the name of the
#' new coder, the color used to display the coder in the graphical user
#' interface, as well as various permissions of the coder.
#'
#' @param connection A \code{dna_connection} object created by the
#'   \code{dna_connection} function.
#' @param name A character object indicating the name of the new coder.
#' @param color A character object indicating the color in which the new coder
#'   should be displayed in the graphical user interface. The color must be
#'   supplied as a hexadecimal string, for example \code{"#FFFF00"} for yellow.
#' @param addDocuments Logical: should the coder have the permission to add new
#'   documents via the graphical user interface?
#' @param editDocuments Logical: should the coder have permission to edit the
#'   meta-data of documents in the graphical user interface?
#' @param deleteDocuments Logical: should the coder have permission to delete
#'   documents from the database in the graphical user interface?
#' @param importDocuments Logical: should the coder have permission to import
#'   documents into the database via the graphical user interface?
#' @param viewOthersDocuments Logical: should the coder have permission to view
#'   the documents that were added by other coders?
#' @param editOthersDocuments Logical: should the coder have permission to edit
#'   the meta-data of documents added by other coders?
#' @param addStatements Logical: should the coder have permission to add new
#'   statements to the database?
#' @param viewOthersStatements Logical: should the coder have permission to view
#'   the statements in the graphical user interface that were added by other
#'   coders?
#' @param editOthersStatements Logical: should the coder have permission to edit
#'   the statements in the graphical user interface that were added by other
#'   coders?
#' @param editCoders Logical: should the coder have permission to add, remove,
#'   or edit coders in the graphical user interface?
#' @param editStatementTypes Logical: should the coder have permission to add,
#'   remove, or edit statement types in the graphical user interface?
#' @param editRegex Logical: should the coder have permission to add, remove, or
#'   edit regular expressions in the graphical user interface?
#' @param verbose Print details?
#'
#' @author Philip Leifeld
#'
#' @importFrom rJava .jcall
#' @export
dna_addCoder <- function(connection,
                         name,
                         color,
                         addDocuments = TRUE,
                         editDocuments = TRUE,
                         deleteDocuments = TRUE,
                         importDocuments = TRUE,
                         viewOthersDocuments = TRUE,
                         editOthersDocuments = TRUE,
                         addStatements = TRUE,
                         viewOthersStatements = TRUE,
                         editOthersStatements = TRUE,
                         editCoders = TRUE,
                         editStatementTypes = TRUE,
                         editRegex = TRUE,
                         verbose = TRUE) {
  if (is.null(name) || is.na(name) || !is.character(name) || length(name) != 1) {
    stop("'name' must be a character object of length 1.")
  }
  if (length(color) != 1) {
    stop("'color' must be an object of length 1.")
  }
  color <- col2hex(color)
  if (is.null(addDocuments) || is.na(addDocuments) || !is.logical(addDocuments) || length(addDocuments) != 1) {
    stop("'addDocuments' must be a TRUE or FALSE.")
  }
  if (is.null(editDocuments) || is.na(editDocuments) || !is.logical(editDocuments) || length(editDocuments) != 1) {
    stop("'editDocuments' must be a TRUE or FALSE.")
  }
  if (is.null(deleteDocuments) || is.na(deleteDocuments) || !is.logical(deleteDocuments) || length(deleteDocuments) != 1) {
    stop("'deleteDocuments' must be a TRUE or FALSE.")
  }
  if (is.null(importDocuments) || is.na(importDocuments) || !is.logical(importDocuments) || length(importDocuments) != 1) {
    stop("'importDocuments' must be a TRUE or FALSE.")
  }
  if (is.null(viewOthersDocuments) || is.na(viewOthersDocuments) || !is.logical(viewOthersDocuments) || length(viewOthersDocuments) != 1) {
    stop("'viewOthersDocuments' must be a TRUE or FALSE.")
  }
  if (is.null(editOthersDocuments) || is.na(editOthersDocuments) || !is.logical(editOthersDocuments) || length(editOthersDocuments) != 1) {
    stop("'editOthersDocuments' must be a TRUE or FALSE.")
  }
  if (is.null(addStatements) || is.na(addStatements) || !is.logical(addStatements) || length(addStatements) != 1) {
    stop("'addStatements' must be a TRUE or FALSE.")
  }
  if (is.null(viewOthersStatements) || is.na(viewOthersStatements) || !is.logical(viewOthersStatements) || length(viewOthersStatements) != 1) {
    stop("'viewOthersStatements' must be a TRUE or FALSE.")
  }
  if (is.null(editOthersStatements) || is.na(editOthersStatements) || !is.logical(editOthersStatements) || length(editOthersStatements) != 1) {
    stop("'editOthersStatements' must be a TRUE or FALSE.")
  }
  if (is.null(editCoders) || is.na(editCoders) || !is.logical(editCoders) || length(editCoders) != 1) {
    stop("'editCoders' must be a TRUE or FALSE.")
  }
  if (is.null(editStatementTypes) || is.na(editStatementTypes) || !is.logical(editStatementTypes) || length(editStatementTypes) != 1) {
    stop("'editStatementTypes' must be a TRUE or FALSE.")
  }
  if (is.null(editRegex) || is.na(editRegex) || !is.logical(editRegex) || length(editRegex) != 1) {
    stop("'editRegex' must be a TRUE or FALSE.")
  }
  if (is.null(verbose) || is.na(verbose) || !is.logical(verbose) || length(verbose) != 1) {
    stop("'verbose' must be TRUE or FALSE.")
  }
  .jcall(connection$dna_connection,
         "V",
         "addCoder",
         name,
         color,
         "",
         addDocuments,
         editDocuments,
         deleteDocuments,
         importDocuments,
         viewOthersDocuments,
         editOthersDocuments,
         addStatements,
         viewOthersStatements,
         editOthersStatements,
         editCoders,
         editStatementTypes,
         editRegex,
         verbose)
}

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
#'   \code{dna_connection} function.
#' @param title A character object containing the title of the new document.
#' @param text A character object containing the text of the new document. Line
#'   breaks can be included as \code{"\\n"}.
#' @param coder An integer value indicating which coder created the document.
#' @param author A character object containing the author of the document.
#' @param source A character object containing the source of the document.
#' @param section A character object containing the section of the document.
#' @param notes A character object containing notes about the document.
#' @param type A character object containing the type of the document.
#' @param date A \code{POSIXct} object containing the date/time stamp of the
#'   document. Alternatively, the date/time can be supplied as an integer
#'   value indicating the milliseconds since the start of 1970-01-01.
#' @param returnID Return the ID of the newly created document as a numeric
#'   value?
#' @param verbose Print details?
#'
#' @author Philip Leifeld
#'
#' @importFrom rJava .jcall
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
    message("A new document with ID ", id, " was added to the database.")
  }
  if (returnID == TRUE) {
    return(id)
  }
}

#' Add a new regular expression to the database
#'
#' Add a new regular expression to the database.
#'
#' Add a new statement type to a database. The statement type contains no
#' variables but can be populated with variables using the
#' \link{dna_addVariable} function. Along with the the label used to describe
#' the statement type, a color needs to be supplied in order to display the
#' statement type in this color in the GUI (see \link{dna_gui}).
#'
#' @param connection A \code{dna_connection} object created by the
#'   \code{dna_connection} function.
#' @param regex A regular expression.
#' @param color A color in the form of a hexadecimal RGB string, such as
#'   \code{"#FFFF00"} for yellow.
#'
#' @author Philip Leifeld
#'
#' @importFrom rJava .jcall
#' @export
dna_addRegex <- function(connection, regex, color = "#FFFF00") {
  if (is.null(regex) || is.na(regex) || length(regex) != 1 || !is.character(regex)) {
    stop("'regex' must be a character object of length 1.")
  }
  if (length(color) != 1) {
    stop("'color' must be an object of length 1.")
  }
  color <- col2hex(color)
  .jcall(connection$dna_connection, "V", "addRegex", regex, color)
}

#' Add a statement to the DNA database
#'
#' Add a new statement to the DNA database.
#'
#' The \code{dna_addStatement} function can add a new statement to an existing
#' DNA database. The user supplies a \link{dna_connection} object as well as
#' the document ID, location of the statement in the document, and the variables
#' and their values. As different statement types have different variables, the
#' \code{...} argument catches all variables and their values supplied by the
#' user. The statement ID will be automatically generated and can be returned
#' if \code{returnID} is set to \code{TRUE}.
#'
#' @param connection A \code{dna_connection} object created by the
#'   \code{dna_connection} function.
#' @param documentID An integer specifying the ID of the document for which the
#' statement should be added.
#' @param startCaret An integer for the start location of the statement in the
#' document text. Must be non-negative and not larger than the number of
#' characters minus one in the document.
#' @param endCaret An integer for the stop location of the statement in the
#' document text. Must be non-negative, greater than \code{startCaret}, and not
#' larger than the number of characters in the document.
#' @param statementType The statement type of the statement that will be added.
#' Can be provided as an integer ID of the statement type or as a character
#' object representing the name of the statement type (if there is no
#' ambiguity).
#' @param coder An integer value indicating which coder created the document.
#' @param returnID Return the ID of the newly created statement as a numeric
#'   value?
#' @param verbose Print details?
#' @param ... Values of the variables contained in the statement, for example
#' \code{organization = "some actor", concept = "my concept", agreement = 1}.
#' Values for Boolean variables can be provided as \code{logical} values
#' (\code{TRUE} or \code{FALSE}) or \code{numeric} values (\code{1} or
#' \code{0}).
#'
#' @author Philip Leifeld
#'
#' @importFrom rJava .jarray
#' @importFrom rJava .jcall
#' @export
dna_addStatement <- function(connection,
                             documentID,
                             startCaret = 1,
                             endCaret = 2,
                             statementType = "DNA Statement",
                             coder = 1,
                             returnID = FALSE,
                             verbose = TRUE,
                             ...) {
  if (!is.integer(documentID)) {
    if (is.numeric(documentID)) {
      documentID <- as.integer(documentID)
    } else {
      stop("'documentID' must be a numeric value specifying the ID of the document to which the statement should be added. You can look up document IDs using the 'dna_getDocuments' function.")
    }
  }
  if (!is.integer(startCaret)) {
    if (is.numeric(startCaret)) {
      startCaret <- as.integer(startCaret)
    } else {
      stop("'startCaret' must be a single numeric value specifying the start location in of the statement in the document.")
    }
  }
  if (!is.integer(endCaret)) {
    if (is.numeric(endCaret)) {
      endCaret <- as.integer(endCaret)
    } else {
      stop("'endCaret' must be a single numeric value specifying the end location in of the statement in the document.")
    }
  }
  if (!is.character(statementType) && !is.numeric(statementType)) {
    stop("'statementType' must be a numeric ID of the statement type or a character object indicating the name of the statement type.")
  } else if (is.numeric(statementType) && !is.integer(statementType)) {
    statementType <- as.integer(statementType)
  }
  if (!is.integer(coder)) {
    if (is.numeric(coder)) {
      coder <- as.integer(coder)
    } else {
      stop("The coder must be provided as a numeric object (see dna_getCoders).")
    }
  }
  ellipsis <- list(...)
  ellipsis <- lapply(ellipsis, function(x) {
    if (is.logical(x)) {
      if (x == TRUE) {
        x <- 1
      } else if (x == FALSE) {
        x <- 0
      }
    }
    if (is.numeric(x)) {
      x <- as.integer(x)
    }
    if (!class(x) %in% c("character", "integer", "logical")) {
      stop("All supplied values must be character, integer, or logical.")
    }
    if (length(x) != 1) {
      stop("All supplied values must be of length 1.")
    }
    return(x)
  })
  varNames <- names(ellipsis)
  ellipsis <- as.data.frame(ellipsis, stringsAsFactors = FALSE)
  ellipsis <- .jarray(lapply(ellipsis, .jarray))

  id <- .jcall(connection$dna_connection,
               "I",
               "addStatement",
               documentID,
               startCaret,
               endCaret,
               statementType,
               coder,
               varNames,
               ellipsis,
               verbose)

  if (returnID == TRUE) {
    return(id)
  }
}

#' Add a new statement type (without variables) to the database
#'
#' Add a new statement type (without variables) to the database.
#'
#' Add a new statement type to a database. The statement type contains no
#' variables but can be populated with variables using the
#' \link{dna_addVariable} function. Along with the the label used to describe
#' the statement type, a color needs to be supplied in order to display the
#' statement type in this color in the GUI (see \link{dna_gui}).
#'
#' @param connection A \code{dna_connection} object created by the
#'   \code{dna_connection} function.
#' @param label A descriptive label for the statement type. For example
#'   \code{"DNA Statement" or "My new statement type"}. The label may contain
#'   spaces.
#' @param color A color in the form of a hexadecimal RGB string, such as
#'   \code{"#FFFF00"} for yellow.
#' @param ... Additional arguments can be added here to define the variables
#'   associated with the statement type. For example,
#'   \code{person = "short text"} or \code{agreement = "boolean"} or multiple
#'   arguments like these separated by comma. The variable names should not
#'   contain any spaces, and the values that indicate the data types should be
#'   of types "short text", "long text", "boolean", or "integer".
#'
#' @author Philip Leifeld
#'
#' @importFrom rJava .jarray
#' @importFrom rJava .jcall
#' @export
dna_addStatementType <- function(connection, label, color = "#FFFF00", ...) {
  if (is.null(label) || is.na(label) || length(label) != 1 || !is.character(label)) {
    stop("'label' must be a character object of length 1.")
  }
  if (length(color) != 1) {
    stop("'color' must be an object of length 1.")
  }
  color <- col2hex(color)
  dots <- list(...)
  if (any(sapply(dots, length) > 1)) {
    stop("Some arguments in ... are longer than 1. All variables need to be associated with exactly one data type.")
  }
  if (!all(sapply(dots, is.character))) {
    stop("Some arguments in ... are not character strings. They need to indicate the variable type as a character string.")
  }
  if (any(grepl("\\W", names(dots)))) {
    stop("Variable names must not contain any spaces.")
  }
  variableNames <- names(dots)
  if (is.null(variableNames)) {
    variableNames <- character()
  }
  variableNames <- .jarray(variableNames)  # wrap in .jarray in case there is only one element
  variableTypes <- unlist(dots)
  if (is.null(variableTypes)) {
    variableTypes <- character()
  }
  variableTypes <- .jarray(variableTypes)  # wrap in .jarray in case there is only one element

  .jcall(connection$dna_connection, "V", "addStatementType", label, color, variableNames, variableTypes)
}

#' Add a new variable to a statement type in the database
#'
#' Add a new variable to a statement type in the database.
#'
#' Add a new variable to an existing statement type in the database, based on
#' the statement type ID or label, a name for the new variable, and a data type
#' specification.
#'
#' @param connection A \code{dna_connection} object created by the
#'   \code{dna_connection} function.
#' @param statementType The statement type in which the new variable should be
#'   defined. The statement type can be supplied as an integer ID or character
#'   string, for example \code{1} or \code{"DNA Statement"}.
#' @param variable The name of the new variable as a character object. Only
#'   characters and numbers are allowed, i.e., no whitespace characters.
#' @param dataType The data type of the new variable. Valid values are
#'   \code{"short text"} (for things like persons, organizations, locations
#'   etc., up to 200 characters), \code{"long text"} (for things like notes,
#'   can store more than 200 characters), \code{"boolean"} (for qualifier
#'   variables such as a binary agreement variable), and \code{"integer"} (for
#'   ordinal Likert scales, such as -5 to +5 or -1 to +1).
#' @param simulate Should the changes only be simulated instead of actually
#'   applied to the DNA connection and the SQL database? This can help to
#'   plan more complex recode operations.
#' @param verbose Print details about the recode operations?
#'
#' @examples
#' \dontrun{
#' dna_init()
#' conn <- dna_connection(dna_sample())
#' dna_addVariable(conn, 1, "location", "short text")
#' }
#'
#' @author Philip Leifeld
#'
#' @importFrom rJava .jcall
#' @export
dna_addVariable <- function(connection,
                            statementType = 1,
                            variable,
                            dataType = "short text",
                            simulate = TRUE,
                            verbose = TRUE) {
  if (is.null(statementType) || is.na(statementType) || length(statementType) != 1
      || (!is.numeric(statementType) && !is.character(statementType))) {
    stop("'statementType' must be an integer or character object of length 1.")
  }
  if (is.numeric(statementType) && !is.integer(statementType)) {
    statementType <- as.integer(statementType)
  }
  if (is.null(variable) || is.na(variable) || length(variable) != 1 || !is.character(variable)) {
    stop("'variable' must be a character object of length 1.")
  }
  if (grepl("\\W", variable)) {
    stop("'variable' must not contain any spaces. Only characters and numbers are allowed.")
  }
  if (is.null(dataType) || is.na(dataType) || length(dataType) != 1 || !is.character(dataType)) {
    stop("'dataType' must be a character object of length 1.")
  }
  if (!dataType %in% c("short text", "long text", "integer", "boolean")) {
    stop("'dataType' must be 'short text', 'long text', 'integer', or 'boolean'.")
  }
  if (is.null(simulate) || is.na(simulate) || !is.logical(simulate) || length(simulate) != 1) {
    stop("'simulate' must be TRUE or FALSE.")
  }
  if (is.null(verbose) || is.na(verbose) || !is.logical(verbose) || length(verbose) != 1) {
    stop("'verbose' must be TRUE or FALSE.")
  }
  .jcall(connection$dna_connection,
         "V",
         "addVariable",
         statementType,
         variable,
         dataType,
         simulate,
         verbose)
}

#' Change the color of a statement type
#'
#' Change the color of a statement type.
#'
#' This function assigns a new color to a statement type. The color is used to
#' display statements of this type in the DNA GUI (see \link{dna_gui}).
#'
#' @param connection A \code{dna_connection} object created by the
#'   \code{dna_connection} function.
#' @param statementType The statement type whose color should be updated. The
#' statement type can be supplied as an integer ID or character string, for
#' example \code{1} or \code{"DNA Statement"}.
#' @param color A color in the form of a hexadecimal RGB string, such as
#'   \code{"#FFFF00"} for yellow.
#'
#' @author Philip Leifeld
#'
#' @importFrom rJava .jcall
#' @export
dna_colorStatementType <- function(connection, statementType, color = "#FFFF00") {
  if (is.null(statementType) || is.na(statementType) || length(statementType) != 1
      || (!is.numeric(statementType) && !is.character(statementType))) {
    stop("'statementType' must be an integer or character object of length 1.")
  }
  if (is.numeric(statementType) && !is.integer(statementType)) {
    statementType <- as.integer(statementType)
  }
  if (length(color) != 1) {
    stop("'color' must be an object of length 1.")
  }
  color <- col2hex(color)
  .jcall(connection$dna_connection, "V", "colorStatementType", statementType, color)
}

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
#' which the respective value is used in statements across the database.
#'
#' @param connection A \code{dna_connection} object created by the
#'   \code{dna_connection} function.
#' @param statementType The ID of the statement type (as an integer) or the name
#'   of the statement type (as a character object) in which the variable is
#'   defined.
#' @param variable The name of the variable for which attribute data should be
#'   retrieved, for example \code{"organization"} or \code{"concept"}.
#' @param values An optional character vector of entries to which the dataframe
#'   should be limited. If all values and attributes should be retrieved for a
#'   given variable, this can be \code{NULL} or a character vector of length 0.
#'
#' @examples
#' \dontrun{
#' dna_init()
#' conn <- dna_connection(dna_sample())
#' attributes <- dna_getAttributes(statementType = 1,
#'                                 variable = "organization",
#'                                 values = c("Alliance to Save Energy",
#'                                            "Senate",
#'                                            "Sierra Club"))
#' }
#'
#' @author Philip Leifeld
#'
#' @importFrom rJava .jevalArray
#' @importFrom rJava .jcall
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
  class(attributes) <- c("dna_dataframe", class(attributes))
  return(attributes)
}

#' Retrieve a dataframe with all coders and their permissions
#'
#' Retrieve a dataframe with all coders and their permissions.
#'
#' This function creates a dataframe with one row per coder and contains columns
#' for the ID, name, color, and 12 permissions of the coders. The permissions
#' serve to govern what coders are allowed to do in the graphical user
#' interface.
#'
#' @param connection A \code{dna_connection} object created by the
#'   \code{dna_connection} function.
#'
#' @author Philip Leifeld
#'
#' @importFrom rJava .jevalArray
#' @importFrom rJava J
#' @export
dna_getCoders <- function(connection) {
  coders <- J(connection$dna_connection, "getCoders")
  coders <- lapply(coders, .jevalArray)
  coders <- as.data.frame(coders, stringsAsFactors = FALSE)
  colnames(coders) <- c("ID",
                        "name",
                        "color",
                        "addDocuments",
                        "editDocuments",
                        "deleteDocuments",
                        "importDocuments",
                        "viewOthersDocuments",
                        "editOthersDocuments",
                        "addStatements",
                        "viewOthersStatements",
                        "editOthersStatements",
                        "editCoders",
                        "editStatementTypes",
                        "editRegex")
  return(coders)
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
#'   \code{dna_connection} function.
#'
#' @examples
#' \dontrun{
#' dna_init()
#' conn <- dna_connection(dna_sample())
#' documents <- dna_getDocuments(conn)
#' documents$title[1] <- "New title for first document"
#' documents$notes[3] <- "Added a note via rDNA."
#' documents <- documents[, -5]  # Removing the fifth document
#' dna_setDocuments(conn, documents, simulate = TRUE)  # apply changes
#' }
#'
#' @author Philip Leifeld
#'
#' @importFrom rJava .jevalArray
#' @importFrom rJava .jcall
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
  class(documents) <- c("dna_dataframe", class(documents))
  return(documents)
}

#' Retrieve a dataframe with all regular expressions
#'
#' Retrieve a dataframe with all regular expressions.
#'
#' The DNA graphical user interface can highlight keywords with colors in the
#' text. This facilitates the coding process. The \code{dna_getRegex} function
#' retrieves all regular expressions defined in a DNA database along with their
#' colors.
#'
#' @param connection A \code{dna_connection} object created by the
#'   \code{dna_connection} function.
#'
#' @author Philip Leifeld
#'
#' @importFrom rJava .jcall
#' @importFrom rJava .jevalArray
#' @export
dna_getRegex <- function(connection) {
  regex <- .jcall(connection$dna_connection,
                  "[Ljava/lang/Object;",
                  "getRegex")
  names(regex) <- c("regex", "color")
  regex <- lapply(regex, .jevalArray)
  regex <- as.data.frame(regex, stringsAsFactors = FALSE)
  return(regex)
}

#' Retrieve a dataframe with all settings
#'
#' Retrieve a dataframe with all settings related to the GUI.
#'
#' The \code{dna_getSettings} function retrieves all settings related to the
#' graphical user interface, including pop-up window width, active coder etc.
#'
#' @param connection A \code{dna_connection} object created by the
#'   \code{dna_connection} function.
#'
#' @author Philip Leifeld
#'
#' @importFrom rJava .jcall
#' @importFrom rJava .jevalArray
#' @export
dna_getSettings <- function(connection) {
  settings <- .jcall(connection$dna_connection,
                     "[Ljava/lang/Object;",
                     "getSettings")
  names(settings) <- c("key", "value")
  settings <- lapply(settings, .jevalArray)
  settings <- as.data.frame(settings, stringsAsFactors = FALSE)
  return(settings)
}

#' Retrieve a dataframe with statements from a DNA connection
#'
#' Retrieve a dataframe with all statements from a DNA connection.
#'
#' This function creates a dataframe with one row per statement and contains
#' columns for the statement ID, document ID, start and end position in the
#' text, statement type ID, coder ID, and all variables. Statements are
#' retrieved for a specific statement type. The data frame can then be manually
#' manipulated and returned to the DNA database using the
#' \link{dna_setStatements} function.
#'
#' @param connection A \code{dna_connection} object created by the
#'   \code{dna_connection} function.
#' @param statementType The statement type for which statements should be
#'   retrieved. The statement type can be supplied as an integer or character
#'   string, for example \code{1} or \code{"DNA Statement"}.
#'
#' @examples
#' \dontrun{
#' dna_init()
#' conn <- dna_connection(dna_sample())
#' statements <- dna_getStatements(conn, statementType = "DNA Statement")
#' }
#'
#' @author Philip Leifeld
#'
#' @importFrom rJava .jevalArray
#' @importFrom rJava J
#' @export
dna_getStatements <- function(connection, statementType) {
  if (is.numeric(statementType)) {
    statementType <- as.integer(statementType)
  }
  if (!is.integer(statementType) && !is.character(statementType)) {
    stop("'statementType' must be integer or character.")
  }
  if (length(statementType) != 1) {
    stop("'statementType' must have length 1.")
  }

  statements <- J(connection$dna_connection, "getStatements", statementType)
  statements <- lapply(statements, .jevalArray)
  statements <- as.data.frame(statements, stringsAsFactors = FALSE)

  variables <- J(connection$dna_connection, "getVariables", statementType)
  variables <- lapply(variables, .jevalArray)
  variables <- as.data.frame(variables, stringsAsFactors = FALSE)
  variables <- variables[, 1]

  colnames(statements) <- c("id",
                            "documentId",
                            "startCaret",
                            "endCaret",
                            "statementTypeId",
                            "coder",
                            variables)
  class(statements) <- c("dna_dataframe", class(statements))
  return(statements)
}

#' Retrieve a dataframe with statement types from a DNA connection
#'
#' Retrieve a dataframe with all statement types from a DNA connection.
#'
#' This function creates a dataframe with one row per statement type and
#' contains columns for the statement type ID, label, and color (as an RGB hex
#' string). The statement type IDs can then be used to retrieve the variables
#' defined within a statement type using the \link{dna_getVariables} function.
#'
#' @param connection A \code{dna_connection} object created by the
#'   \code{dna_connection} function.
#'
#' @examples
#' \dontrun{
#' dna_init()
#' conn <- dna_connection(dna_sample())
#' statementTypes <- dna_getStatementTypes(conn)
#' }
#'
#' @author Philip Leifeld
#'
#' @importFrom rJava .jevalArray
#' @importFrom rJava J
#' @export
dna_getStatementTypes <- function(connection) {
  statementTypes <- J(connection$dna_connection, "getStatementTypes")
  statementTypes <- lapply(statementTypes, .jevalArray)
  statementTypes <- as.data.frame(statementTypes, stringsAsFactors = FALSE)
  colnames(statementTypes) <- c("id", "label", "color")
  return(statementTypes)
}

#' Retrieve a dataframe with all variables for a statement type
#'
#' Retrieve a dataframe with all variables defined in a given statement type.
#'
#' For a given statement type ID (see \link{dna_getStatementTypes}), this
#' function creates a dataframe with one row per variable and contains columns
#' for the variable name and the data type associated with this variable.
#'
#' @param connection A \code{dna_connection} object created by the
#'   \code{dna_connection} function.
#' @param statementType The statement type for which statements should be
#'   retrieved. The statement type can be supplied as an integer or character
#'   string, for example \code{1} or \code{"DNA Statement"}.
#'
#' @examples
#' \dontrun{
#' dna_init()
#' conn <- dna_connection(dna_sample())
#' variables <- dna_getVariables(conn, 1)
#' }
#'
#' @author Philip Leifeld
#'
#' @importFrom rJava .jevalArray
#' @importFrom rJava J
#' @export
dna_getVariables <- function(connection, statementType) {
  if (is.null(statementType) || is.na(statementType) || length(statementType) != 1) {
    stop("'statementType' must be an integer or character object of length 1.")
  }
  if (is.numeric(statementType) && !is.integer(statementType)) {
    statementType <- as.integer(statementType)
  } else if (!is.character(statementType) && !is.integer(statementType)) {
    stop("'statementType' must be an integer or character object of length 1.")
  }

  variables <- J(connection$dna_connection, "getVariables", statementType)
  variables <- lapply(variables, .jevalArray)
  variables <- as.data.frame(variables, stringsAsFactors = FALSE)
  colnames(variables) <- c("label", "type")

  return(variables)
}

#' Recast a variable into a different data type
#'
#' Recast a variable into a different data type.
#'
#' This function converts a variable into a different data type. The user
#' supplies the statement type in which the variable is defined and the variable
#' name, and the variable is converted into a different data type.
#'
#' Depending on the current data type of the variable, different actions are
#' taken as follows:
#' \describe{
#'  \item{"short text"}{Will be converted into "long text".}
#'  \item{"long text"}{Will be converted into "short text". The function goes
#'    through all statements and truncates the respective values if they are
#'    longer than 200 characters, both in the statements and attributes.}
#'  \item{"boolean"}{Will be converted into "integer" by recoding \code{0} into
#'    \code{-1} and keeping \code{1} as \code{1}.}
#'  \item{"integer"}{Will be converted into "boolean". If there are precisely
#'    two values across all statements, the smaller value will be recoded into
#'    \code{0} and the larger value into \code{1}. If there is only one value
#'    across all statements, this value will be recoded as \code{1}. If there
#'    are no statements of this statement type, no recoding will be done. If
#'    there are more than two values across all statements, an error message
#'    will be printed.}
#' }
#'
#' By default, changes are only simulated, but this can be changed using the
#' \code{simulate} argument in order to actually apply the changes to the
#' database.
#'
#' @param connection A \code{dna_connection} object created by the
#'   \code{dna_connection} function.
#' @param statementType The statement type in which the variable is defined that
#'   should be recast into a different data type. The statement type can be
#'   supplied as an integer ID or character string, for example \code{1} or
#'   \code{"DNA Statement"}.
#' @param variable The name of the variable that should be recast into a
#'   different data type.
#' @param simulate Should the changes only be simulated instead of actually
#'   applied to the DNA connection and the SQL database? This can help to
#'   plan more complex recode operations.
#' @param verbose Print details about the recode operations?
#'
#' @author Philip Leifeld
#'
#' @importFrom rJava .jcall
#' @export
dna_recastVariable <- function(connection, statementType, variable, simulate = TRUE, verbose = TRUE) {
  if (is.null(statementType) || is.na(statementType) || length(statementType) != 1
      || (!is.numeric(statementType) && !is.character(statementType))) {
    stop("'statementType' must be an integer or character object of length 1.")
  }
  if (is.numeric(statementType) && !is.integer(statementType)) {
    statementType <- as.integer(statementType)
  }
  if (is.null(variable) || is.na(variable) || length(variable) != 1 || !is.character(variable)) {
    stop("'variable' must be a character object of length 1.")
  }
  if (grepl("\\W", variable)) {
    stop("'variable' must not contain any spaces. Only characters and numbers are allowed.")
  }
  if (is.null(simulate) || is.na(simulate) || !is.logical(simulate) || length(simulate) != 1) {
    stop("'simulate' must be TRUE or FALSE.")
  }
  if (is.null(verbose) || is.na(verbose) || !is.logical(verbose) || length(verbose) != 1) {
    stop("'verbose' must be TRUE or FALSE.")
  }
  .jcall(connection$dna_connection, "V", "recastVariable", statementType, variable, simulate, verbose)
}

#' Removes an attribute entry from the database
#'
#' Removes an attribute entry from the database based on its ID or value.
#'
#' The user provides a connection object and the ID or value label of an
#' existing attribute in the DNA database, and this attribute is removed both
#' from memory and from the SQL database, possibly including any statements
#' associated with the attribute.
#'
#' @param connection A \code{dna_connection} object created by the
#'   \link{dna_connection} function.
#' @param statementType The ID of the statement type (as an integer) or the name
#'   of the statement type (as a character object) in which the variable is
#'   defined.
#' @param variable The name of the variable from which attribute should be
#'   removed, for example \code{"organization"} or \code{"concept"}.
#' @param attribute An integer value denoting the ID of the attribute to be
#'   removed, or a value label (as a character object) denoting the entry to be
#'   removed. The \link{dna_getAttributes} function can be used to look up IDs.
#' @param removeStatements The attribute given by \code{attribute} may contain
#'   statements. If \code{removeStatements = TRUE} is set, these statements
#'   are removed along with the respective attribute. If
#'   \code{removeStatements = FALSE} is set, the statements are not deleted,
#'   the attribute is kept as well, and a message is printed.
#' @param simulate Should the changes only be simulated instead of actually
#'   applied to the DNA connection and the SQL database? This can help to
#'   plan more complex recode operations.
#' @param verbose Print details on whether the attribute could be removed?
#'
#' @author Philip Leifeld
#'
#' @importFrom rJava .jcall
#' @export
dna_removeAttribute <- function(connection,
                                statementType = 1,
                                variable = "organization",
                                attribute,
                                removeStatements = FALSE,
                                simulate = TRUE,
                                verbose = TRUE) {
  if (!is.integer(attribute) && is.numeric(attribute)) {
    attribute <- as.integer(attribute)
  }
  if (!is.integer(attribute) && !is.character(attribute)) {
    attribute <- as.character(attribute)
  }
  if (length(attribute) > 1) {
    stop("Only one attribute can be removed at a time. Use a loop or vectorization if you need to remove multiple attributes.")
  }
  if (is.numeric(statementType) && !is.integer(statementType)) {
    statementType <- as.integer(statementType)
  }
  if (!is.integer(statementType) && !is.character(statementType)) {
    statementType <- as.character(statementType)
  }
  .jcall(connection$dna_connection,
         "V",
         "removeAttribute",
         statementType,
         variable,
         attribute,
         removeStatements,
         simulate,
         verbose)
}

#' Removes a coder from the database
#'
#' Removes a coder from the database based on its ID or name.
#'
#' The user provides a connection object and the ID or name of a coder, and the
#' function will remove the coder from the database. If there are documents or
#' statements associated with the coder, an error will be printed instead.
#'
#' @param connection A \code{dna_connection} object created by the
#'   \link{dna_connection} function.
#' @param coder An integer value denoting the ID of the coder to be removed or
#'   a character object of length 1 indicating the name of the coder to be
#'   removed.
#' @param verbose Print details?
#'
#' @author Philip Leifeld
#'
#' @importFrom rJava .jcall
#' @export
dna_removeCoder <- function(connection, coder, verbose = TRUE) {
  if (!is.null(coder) && !is.na(coder) && is.character(coder) && length(coder) == 1) {
    # fine: character
  } else if (!is.null(coder) && !is.na(coder) && is.integer(coder) && length(coder) == 1) {
    # fine: integer
  } else if (!is.null(coder) && !is.na(coder) && is.numeric(coder) && length(coder) == 1) {
    coder <- as.integer(coder) # convert to integer
  } else {
    stop("'coder' must be an integer coder ID or the name of the coder to be removed.")
  }
  if (is.null(verbose) || is.na(verbose) || !is.logical(verbose) || length(verbose) != 1) {
    stop("'verbose' must be TRUE or FALSE.")
  }
  .jcall(connection$dna_connection,
         "V",
         "removeCoder",
         coder,
         verbose)
}

#' Removes a document from the database
#'
#' Removes a document from the database based on its ID.
#'
#' The user provides a connection object and the ID of an existing document in
#' the DNA database, and this document is removed both from memory and from the
#' SQL database, possibly including any statements contained in the document.
#'
#' @param connection A \code{dna_connection} object created by the
#'   \link{dna_connection} function.
#' @param id An integer value denoting the ID of the document to be removed. The
#'   \link{dna_getDocuments} function can be used to look up IDs.
#' @param removeStatements The document given by \code{id} may contain
#'   statements. If \code{removeStatements = TRUE} is set, these statements
#'   are removed along with the respective document. If
#'   \code{removeStatements = FALSE} is set, the statements are not deleted,
#'   the document is kept as well, and a message is printed.
#' @param simulate Should the changes only be simulated instead of actually
#'   applied to the DNA connection and the SQL database? This can help to
#'   plan more complex recode operations.
#' @param verbose Print details on whether the document could be removed?
#'
#' @author Philip Leifeld
#'
#' @importFrom rJava .jcall
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

#' Removes a statement from the database
#'
#' Removes a statement from the database based on its ID.
#'
#' The user provides a connection object and the ID of an existing statement in
#' the DNA database, and this statement is removed both from memory and from the
#' SQL database.
#'
#' @param connection A \code{dna_connection} object created by the
#'   \link{dna_connection} function.
#' @param regex The regular expression that should be removed as a character
#'   object.
#'
#' @author Philip Leifeld
#'
#' @importFrom rJava .jcall
#' @export
dna_removeRegex <- function(connection, regex) {
  if (is.null(regex) || is.na(regex) || length(regex) != 1 || !is.character(regex)) {
    stop("'regex' must be a character object of length 1.")
  }
  .jcall(connection$dna_connection, "V", "removeRegex", regex)
}

#' Removes a statement from the database
#'
#' Removes a statement from the database based on its ID.
#'
#' The user provides a connection object and the ID of an existing statement in
#' the DNA database, and this statement is removed both from memory and from the
#' SQL database.
#'
#' @param connection A \code{dna_connection} object created by the
#'   \link{dna_connection} function.
#' @param id An integer value denoting the ID of the statement to be removed.
#' The \link{dna_getStatements} function can be used to look up IDs.
#' @param verbose Print details on whether the document could be removed?
#'
#' @author Philip Leifeld
#'
#' @importFrom rJava .jcall
#' @export
dna_removeStatement <- function(connection,
                                id,
                                verbose = TRUE) {
  if (!is.integer(id)) {
    if (is.numeric(id)) {
      id <- as.integer(id)
    } else {
      stop("'id' must be a numeric or integer statement ID.")
    }
  }
  .jcall(connection$dna_connection,
         "V",
         "removeStatement",
         id,
         verbose)
}

#' Remove a statement type from the database
#'
#' Remove a statement type from the database.
#'
#' Completely remove a statement type from the database, including all
#' variables, attributes, and statements that are associated with the statement
#' type.
#'
#' @param connection A \code{dna_connection} object created by the
#'   \code{dna_connection} function.
#' @param statementType The statement type to be deleted. The statement type
#'   can be supplied as an integer ID or character string, for example \code{1}
#'   or \code{"DNA Statement"}.
#' @param simulate Should the changes only be simulated instead of actually
#'   applied to the DNA connection and the SQL database? This can help to
#'   plan more complex recode operations.
#' @param verbose Print details about the recode operations?
#'
#' @author Philip Leifeld
#'
#' @importFrom rJava .jcall
#' @export
dna_removeStatementType <- function(connection, statementType, simulate = TRUE, verbose = TRUE) {
  if (is.null(statementType) || is.na(statementType) || length(statementType) != 1
      || (!is.numeric(statementType) && !is.character(statementType))) {
    stop("'statementType' must be an integer or character object of length 1.")
  }
  if (is.numeric(statementType) && !is.integer(statementType)) {
    statementType <- as.integer(statementType)
  }
  if (is.null(simulate) || is.na(simulate) || !is.logical(simulate) || length(simulate) != 1) {
    stop("'simulate' must be TRUE or FALSE.")
  }
  if (is.null(verbose) || is.na(verbose) || !is.logical(verbose) || length(verbose) != 1) {
    stop("'verbose' must be TRUE or FALSE.")
  }
  .jcall(connection$dna_connection,
         "V",
         "removeStatementType",
         statementType,
         simulate,
         verbose)
}

#' Add a new variable to a statement type in the database
#'
#' Add a new variable to a statement type in the database.
#'
#' Add a new variable to an existing statement type in the database, based on
#' the statement type ID or label, a name for the new variable, and a data type
#' specification.
#'
#' @param connection A \code{dna_connection} object created by the
#'   \code{dna_connection} function.
#' @param statementType The statement type from which variable should be
#'   deleted. The statement type can be supplied as an integer ID or character
#'   string, for example \code{1} or \code{"DNA Statement"}.
#' @param variable The name of the variable as a character object.
#' @param simulate Should the changes only be simulated instead of actually
#'   applied to the DNA connection and the SQL database? This can help to
#'   plan more complex recode operations.
#' @param verbose Print details about the recode operations?
#'
#' @examples
#' \dontrun{
#' dna_init()
#' conn <- dna_connection(dna_sample())
#' dna_removeVariable(conn, 1, "person")
#' }
#'
#' @author Philip Leifeld
#'
#' @importFrom rJava .jcall
#' @export
dna_removeVariable <- function(connection,
                               statementType = 1,
                               variable,
                               simulate = TRUE,
                               verbose = TRUE) {
  if (is.null(statementType) || is.na(statementType) || length(statementType) != 1
      || (!is.numeric(statementType) && !is.character(statementType))) {
    stop("'statementType' must be an integer or character object of length 1.")
  }
  if (is.numeric(statementType) && !is.integer(statementType)) {
    statementType <- as.integer(statementType)
  }
  if (is.null(variable) || is.na(variable) || length(variable) != 1 || !is.character(variable)) {
    stop("'variable' must be a character object of length 1.")
  }
  if (is.null(simulate) || is.na(simulate) || !is.logical(simulate) || length(simulate) != 1) {
    stop("'simulate' must be TRUE or FALSE.")
  }
  if (is.null(verbose) || is.na(verbose) || !is.logical(verbose) || length(verbose) != 1) {
    stop("'verbose' must be TRUE or FALSE.")
  }
  .jcall(connection$dna_connection,
         "V",
         "removeVariable",
         statementType,
         variable,
         simulate,
         verbose)
}

#' Rename a statement type
#'
#' Rename a statement type by assigning a new label.
#'
#' This function renames a statement type by replacing its label with a new
#' label.
#'
#' @param connection A \code{dna_connection} object created by the
#'   \code{dna_connection} function.
#' @param statementType The statement type that should be renamed. The statement
#' type can be supplied as an integer ID or character string, for example
#' \code{1} or \code{"DNA Statement"}.
#' @param label A descriptive new label for the statement type. For example
#'   \code{"DNA Statement" or "My statement type"}. The label may contain
#'   spaces.
#'
#' @author Philip Leifeld
#'
#' @importFrom rJava .jcall
#' @export
dna_renameStatementType <- function(connection, statementType, label) {
  if (is.null(statementType) || is.na(statementType) || length(statementType) != 1
      || (!is.numeric(statementType) && !is.character(statementType))) {
    stop("'statementType' must be an integer or character object of length 1.")
  }
  if (is.numeric(statementType) && !is.integer(statementType)) {
    statementType <- as.integer(statementType)
  }
  if (is.null(label) || is.na(label) || length(label) != 1 || !is.character(label)) {
    stop("'label' must be a character object of length 1.")
  }
  .jcall(connection$dna_connection, "V", "renameStatementType", statementType, label)
}

#' Rename a variable
#'
#' Rename a variable by assigning a new label.
#'
#' This function renames a statement type by replacing its label with a new
#' label.
#'
#' @param connection A \code{dna_connection} object created by the
#'   \code{dna_connection} function.
#' @param statementType The statement type in which the variable is defined that
#'   should be renamed. The statement type can be supplied as an integer ID or
#'   character string, for example \code{1} or \code{"DNA Statement"}.
#' @param variable The name of the variable that should be renamed.
#' @param label A descriptive new label for the variable. For example
#'   \code{"actor" or "intensity"}. The label must not contain spaces.
#' @param simulate Should the changes only be simulated instead of actually
#'   applied to the DNA connection and the SQL database? This can help to
#'   plan more complex recode operations.
#' @param verbose Print details about the recode operations?
#'
#' @author Philip Leifeld
#'
#' @importFrom rJava .jcall
#' @export
dna_renameVariable <- function(connection, statementType, variable, label, simulate = TRUE, verbose = TRUE) {
  if (is.null(statementType) || is.na(statementType) || length(statementType) != 1
      || (!is.numeric(statementType) && !is.character(statementType))) {
    stop("'statementType' must be an integer or character object of length 1.")
  }
  if (is.numeric(statementType) && !is.integer(statementType)) {
    statementType <- as.integer(statementType)
  }
  if (is.null(variable) || is.na(variable) || length(variable) != 1 || !is.character(variable)) {
    stop("'variable' must be a character object of length 1.")
  }
  if (grepl("\\W", variable)) {
    stop("'variable' must not contain any spaces. Only characters and numbers are allowed.")
  }
  if (is.null(label) || is.na(label) || length(label) != 1 || !is.character(label)) {
    stop("'label' must be a character object of length 1.")
  }
  if (grepl("\\W", label)) {
    stop("'label' must not contain any spaces. Only characters and numbers are allowed.")
  }
  if (is.null(simulate) || is.na(simulate) || !is.logical(simulate) || length(simulate) != 1) {
    stop("'simulate' must be TRUE or FALSE.")
  }
  if (is.null(verbose) || is.na(verbose) || !is.logical(verbose) || length(verbose) != 1) {
    stop("'verbose' must be TRUE or FALSE.")
  }
  .jcall(connection$dna_connection, "V", "renameVariable", statementType, variable, label, simulate, verbose)
}

#' Recode attributes in the DNA database
#'
#' Add, remove, and edit values and attributes for a variable in a DNA database.
#'
#' This function takes a dataframe with columns "id", "value", "color", "type",
#' "alias", and "notes" (in no particular order and ignoring any additional
#' columns) and hands it over to a DNA connection in order to update the
#' attributes in the database for a specific statement type and variable, based
#' on the contents of the dataframe. The typical workflow is to retrieve the
#' attributes for some statement type and variable using
#' \link{dna_getAttributes}, manipulating the attributes, and then applying the
#' changes with \link{dna_setAttributes}. Attributes that are no longer in the
#' dataframe are removed from the database; attributes in the dataframe that
#' are not in the database are added to the database; and contents of existing
#' attributes are updated. By default, the changes are only simulated and not
#' actually written into the database. The user can inspect the reported
#' changes and then apply the actual changes by setting \code{simulate = FALSE}.
#'
#' @param connection A \code{dna_connection} object created by the
#'   \code{dna_connection} function.
#' @param attributes A dataframe with at least six columns: id (integer), value
#'   (character), color (character; must be in hexadecimal RGB format),
#'   type (character), alias (character), and notes (character), in no
#'   particular order. \code{NA} values or \code{-1} values are permitted in the
#'   id column. If these are encountered, a new ID is automatically generated,
#'   and the attribute is added.
#' @param statementType The ID of the statement type (as an integer) or the name
#'   of the statement type (as a character object) in which the variable is
#'   defined.
#' @param variable The name of the variable for which attribute data should be
#'   retrieved, for example \code{"organization"} or \code{"concept"}.
#' @param removeStatements If an attribute is present in the DNA database but
#'   not in the \code{attributes} dataframe, the respective attribute is removed
#'   from the database. However, the attribute may have been used in statements.
#'   If \code{removeStatements = TRUE} is set, these statements are removed
#'   along with the respective attribute. If \code{removeStatements = FALSE} is
#'   set, the statements are not deleted, the attribute is kept as well, and a
#'   message is printed.
#' @param simulate Should the changes only be simulated instead of actually
#'   applied to the DNA connection and the SQL database? This can help to
#'   plan more complex recode operations.
#' @param verbose Print details about the recode operations?
#'
#' @examples
#' \dontrun{
#' dna_init()
#' conn <- dna_connection(dna_sample())
#' at <- dna_getAttributes(conn)
#'
#' at$value[2] <- "new organization name"   # recode a value
#' at$color[5] <- "#0000FF"                 # recode a color
#' at$notes[3] <- "Added a note via rDNA."  # recode a note
#' at <- at[-6, ]                           # remove an attribute
#' at <- rbind(at,
#'             data.frame(id = NA,
#'                        value = "new actor",
#'                        color = "#00FFCC",
#'                        type = "NGO",
#'                        alias = "",
#'                        notes = ""))
#' dna_setAttributes(conn,
#'                   statementType = "DNA Statement",
#'                   variable = "organization",
#'                   at,
#'                   removeStatements = TRUE,
#'                   simulate = TRUE)
#' }
#'
#' @author Philip Leifeld
#'
#' @importFrom rJava .jarray
#' @importFrom rJava .jcall
#' @export
dna_setAttributes <- function(connection,
                              attributes,
                              statementType = 1,
                              variable = "organization",
                              removeStatements = FALSE,
                              simulate = TRUE,
                              verbose = TRUE) {
  if ((!is.numeric(statementType) && !is.character(statementType)) || length(statementType) > 1) {
    stop("'statementType' must be a single integer or character value referencing the ID or name of the statement type in which the variable is defined.")
  }
  if (!is.integer(statementType) && is.numeric(statementType)) {
    statementType <- as.integer(statementType)
  }
  if (!is.character(variable) || length(variable) > 1) {
    stop("'variable' must be a single character object referencing the variable for which the attribute data should be set.")
  }
  if (!is.data.frame(attributes)) {
    stop("'attributes' must be a data.frame similar to the one returned by dna_getAttributes.")
  }
  if (!"id" %in% colnames(attributes)) {
    "The 'attributes' data.frame does not contain a column with the label 'id'."
  }
  if (!"value" %in% colnames(attributes)) {
    "The 'attributes' data.frame does not contain a column with the label 'value'."
  }
  if (!"color" %in% colnames(attributes)) {
    "The 'attributes' data.frame does not contain a column with the label 'color'."
  }
  if (!"type" %in% colnames(attributes)) {
    "The 'attributes' data.frame does not contain a column with the label 'type'."
  }
  if (!"alias" %in% colnames(attributes)) {
    "The 'attributes' data.frame does not contain a column with the label 'alias'."
  }
  if (!"notes" %in% colnames(attributes)) {
    "The 'attributes' data.frame does not contain a column with the label 'notes'."
  }
  if (!is.integer(attributes$id)) {
    stop("The 'id' column of 'attributes' must be integer and must contain the attribute IDs.")
  }
  if (!is.character(attributes$value)) {
    if (is.factor(attributes$value)) {
      attributes$value <- as.character(attributes$value)
    } else {
      stop("The 'value' column of 'attributes' must contain the attribute values as character objects.")
    }
  }
  if (!is.character(attributes$color)) {
    if (is.factor(attributes$color)) {
      attributes$color <- as.character(attributes$color)
    } else {
      stop("The 'color' column of 'attributes' must contain the attribute colors as character objects in hexadecimal format.")
    }
  }
  if (!is.character(attributes$type)) {
    if (is.factor(attributes$type)) {
      attributes$type <- as.character(attributes$type)
    } else {
      stop("The 'type' column of 'attributes' must contain the attribute types as character objects.")
    }
  }
  if (!is.character(attributes$alias)) {
    if (is.factor(attributes$alias)) {
      attributes$alias <- as.character(attributes$alias)
    } else {
      stop("The 'alias' column of 'attributes' must contain the attribute aliases as character objects.")
    }
  }
  if (!is.character(attributes$notes)) {
    if (is.factor(attributes$notes)) {
      attributes$notes <- as.character(attributes$notes)
    } else {
      stop("The 'notes' column of 'attributes' must contain the attribute notes as character objects.")
    }
  }
  if (verbose == TRUE) {
    if (nrow(attributes) == 0) {
      warning("'attributes' has 0 rows. Deleting all attributes from the database.")
    }
  }
  # replace NAs with -1, which will be replaced by an auto-generated ID in DNA
  if (any(is.na(attributes$id))) {
    attributes$id[which(is.na(attributes$id))] <- as.integer(-1)
  }
  attributes <- .jarray(lapply(attributes, .jarray))
  .jcall(connection$dna_connection,
         "V",
         "setAttributes",
         statementType,
         variable,
         attributes,
         removeStatements,
         simulate,
         verbose)
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
#'   \code{dna_connection} function.
#' @param documents A data frame with the following columns:
#'   \enumerate{
#'     \item id (integer - can be \code{NA} if a new statement is added),
#'     \item title (character - the title of the document),
#'     \item text (character - the document text),
#'     \item coder (integer - ID of  the coder),
#'     \item author (character - the document author),
#'     \item source (character - the document source),
#'     \item section (character - the document section),
#'     \item notes (character - the document notes),
#'     \item type (character - the document type),
#'     \item date (POSIXct or integer; if integer, the value indicates
#'     milliseconds since the start of 1970-01-01 - the document date/time).
#'   }
#'   \code{NA} values or \code{-1} values are permitted in the id column. If
#'   these are encountered, a new ID is automatically generated, and the
#'   document is added.
#' @param removeStatements If a document is present in the DNA database but not
#'   in the \code{documents} dataframe, the respective document is removed
#'   from the database. However, the document may contain statements. If
#'   \code{removeStatements = TRUE} is set, these statements are removed along
#'   with the respective document. If \code{removeStatements = FALSE} is set,
#'   the statements are not deleted, the document is kept as well, and a
#'   message is printed.
#' @param simulate Should the changes only be simulated instead of actually
#'   applied to the DNA connection and the SQL database? This can help to
#'   plan more complex recode operations.
#' @param verbose Print details about the recode operations?
#'
#' @examples
#' \dontrun{
#' dna_init()
#' conn <- dna_connection(dna_sample())
#' documents <- dna_getDocuments(conn)
#' documents$title[1] <- "New title for first document"
#' documents$notes[3] <- "Added a note via rDNA."
#' documents <- documents[, -5] # removing the fifth document
#' dna_setDocuments(conn, documents, simulate = TRUE) # apply changes
#' }
#'
#' @author Philip Leifeld
#'
#' @importFrom rJava .jarray
#' @importFrom rJava .jcall
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
    stop("'documents' must contain exactly ten columns. You can use the 'dna_getDocuments' function to create a template.")
  }
  if (colnames(documents)[1] != "id") {
    stop("The first column of 'documents' must be called 'id' and contain the document IDs.")
  }
  if (!is.integer(documents[, 1])) {
    if (is.numeric(documents[, 1])) {
      documents[, 1] <- as.integer(documents[, 1])
    } else {
      stop("'documents$id' must contain integer values.")
    }
  }
  if (colnames(documents)[2] != "title") {
    stop("The second column of 'documents' must be called 'title' and contain the document titles.")
  }
  if (!is.character(documents[, 2])) {
    if (is.factor(documents[, 2])) {
      documents[, 2] <- as.character(documents[, 2])
    } else {
      stop("'documents$title' must contain character objects.")
    }
  }
  if (colnames(documents)[3] != "text") {
    stop("The third column of 'documents' must be called 'text' and contain the document texts.")
  }
  if (!is.character(documents[, 3])) {
    if (is.factor(documents[, 3])) {
      documents[, 3] <- as.character(documents[, 3])
    } else {
      stop("'documents$text' must contain character objects.")
    }
  }
  if (colnames(documents)[4] != "coder") {
    stop("The fourth column of 'documents' must be called 'coder' and contain the coder IDs.")
  }
  if (!is.integer(documents[, 4])) {
    if (is.numeric(documents[, 4])) {
      documents[, 4] <- as.integer(documents[, 4])
    } else {
      stop("'documents$coder' must contain integer values.")
    }
  }
  if (colnames(documents)[5] != "author") {
    stop("The fifth column of 'documents' must be called 'author' and contain the document author.")
  }
  if (!is.character(documents[, 5])) {
    if (is.factor(documents[, 5])) {
      documents[, 5] <- as.character(documents[, 5])
    } else {
      stop("'documents$author' must contain character objects.")
    }
  }
  if (colnames(documents)[6] != "source") {
    stop("The sixth column of 'documents' must be called 'source' and contain the document source.")
  }
  if (!is.character(documents[, 6])) {
    if (is.factor(documents[, 6])) {
      documents[, 6] <- as.character(documents[, 6])
    } else {
      stop("'documents$source' must contain character objects.")
    }
  }
  if (colnames(documents)[7] != "section") {
    stop("The seventh column of 'documents' must be called 'section' and contain the document section.")
  }
  if (!is.character(documents[, 7])) {
    if (is.factor(documents[, 7])) {
      documents[, 7] <- as.character(documents[, 7])
    } else {
      stop("'documents$section' must contain character objects.")
    }
  }
  if (colnames(documents)[8] != "notes") {
    stop("The eighth column of 'documents' must be called 'notes' and contain the document notes.")
  }
  if (!is.character(documents[, 8])) {
    if (is.factor(documents[, 8])) {
      documents[, 8] <- as.character(documents[, 8])
    } else {
      stop("'documents$notes' must contain character objects.")
    }
  }
  if (colnames(documents)[9] != "type") {
    stop("The ninth column of 'documents' must be called 'type' and contain the document type.")
  }
  if (!is.character(documents[, 9])) {
    if (is.factor(documents[, 9])) {
      documents[, 9] <- as.character(documents[, 9])
    } else {
      stop("'documents$type' must contain character objects.")
    }
  }
  if (colnames(documents)[10] != "date") {
    stop("The tenth column of 'documents' must be called 'date' and contain the document date/time.")
  }
  if (any(class(documents[, 10]) %in% c("POSIXct", "POSIXt"))) {
    documents[, 10] <- .jlong(as.integer(documents[, 10]) * 1000)
  } else if (is.numeric(documents[, 10])) {
    documents[, 10] <- .jlong(as.integer(documents[, 10]))
  } else {
    stop("'documents$date' must contain the document dates as POSIXct objects or as numeric objects indicating milliseconds since 1970-01-01.")
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

#' Recode statements in the DNA database
#'
#' Recode statements in a DNA database.
#'
#' This function takes a dataframe with columns "id", "documentId",
#' "startCaret", "endCaret", "statementTypeId", "coder", and addition columns
#' for the variables of the respective statement type, as returned by the
#' \link{dna_getStatements} function, and hands it over to a DNA connection in
#' order to update the statements in the database based on the contents of the
#' dataframe. The typical workflow is to retrieve the statements using
#' \link{dna_getStatements}, manipulate the statements in the data frame, and
#' then apply the changes with \link{dna_setStatements}. Statements that are no
#' longer in the data frame are removed from the database; statements in the
#' data frame that are not in the database are added to the database; and
#' contents of existing statements are updated. By default, the changes are only
#' simulated and not actually written into the database. The user can inspect
#' the reported changes and then apply the actual changes by setting
#' \code{simulate = FALSE}. If attributes for any of the variables are affected,
#' they are renamed or added if there is no other instance in the database. Note
#' that the removal or update of statements does not automatically remove any
#' attributes. See \link{dna_removeAttribute} and \link{dna_setAttributes} for
#' this purpose.
#'
#' @param connection A \code{dna_connection} object created by the
#'   \code{dna_connection} function.
#' @param statements A data frame with the following columns:
#'   \enumerate{
#'     \item id (integer - can be \code{NA} if a new statement is added),
#'     \item documentId (integer - needs to refer to an existing document ID),
#'     \item startCaret (integer - the start position of the statement in the
#'     text as a character count, starting at 0 for the first character in the
#'     document),
#'     \item endCaret (integer - the end position of the statement in the text
#'     as a character count, where for example a value of 1 would indicate that
#'     the statement ends after the first character in the document),
#'     \item statementTypeId (integer - ID of the corresponding statement type),
#'     \item coder (integer - ID of the coder),
#'     \item additional columns for the respective variable, such as
#'     organization, concept, agreement, etc.
#'   }
#'   \code{NA} values or \code{-1} values are permitted in the id column. If
#'   these are encountered, a new ID is automatically generated, and the
#'   statement is added.
#' @param simulate Should the changes only be simulated instead of actually
#'   applied to the DNA connection and the SQL database? This can help to
#'   plan more complex recode operations.
#' @param verbose Print details about the recode operations?
#'
#' @examples
#' \dontrun{
#' dna_init()
#' conn <- dna_connection(dna_sample())
#' statements <- dna_getStatements(conn)
#' statements$organization[1] <- "New actor for first statement"
#' statements$concept[3] <- "New concept for the third statement"
#' statements <- statements[, -5] # removing the fifth statement
#' dna_setStatements(conn, statements, simulate = TRUE) # apply changes
#' }
#'
#' @author Philip Leifeld
#'
#' @importFrom rJava .jarray
#' @importFrom rJava .jcall
#' @importFrom rJava .jevalArray
#' @importFrom rJava J
#' @export
dna_setStatements <- function(connection,
                              statements,
                              simulate = TRUE,
                              verbose = TRUE) {
  if (!is.data.frame(statements)) {
    stop("'statements' must be a data.frame similar to the one returned by dna_getStatements.")
  }
  if (ncol(statements) < 6) {
    stop("'statements' must contain at least six columns. You can use the 'dna_getStatements' function to create a template.")
  }
  if (colnames(statements)[1] != "id") {
    stop("The first column of 'statements' must be called 'id' and contain the statement IDs.")
  }
  if (!is.integer(statements[, 1])) {
    if (is.numeric(statements[, 1])) {
      statements[, 1] <- as.integer(statements[, 1])
    } else {
      stop("'statements$id' must contain integer values.")
    }
  }
  if (colnames(statements)[2] != "documentId") {
    stop("The second column of 'statements' must be called 'documentId' and contain the document IDs.")
  }
  if (!is.integer(statements[, 2])) {
    if (is.numeric(statements[, 2])) {
      statements[, 2] <- as.integer(statements[, 2])
    } else {
      stop("'statements$documentId' must contain integer values.")
    }
  }
  if (colnames(statements)[3] != "startCaret") {
    stop("The third column of 'statements' must be called 'startCaret' and contain the start position of the statement in the text.")
  }
  if (!is.integer(statements[, 3])) {
    if (is.numeric(statements[, 3])) {
      statements[, 3] <- as.integer(statements[, 3])
    } else {
      stop("'statements$startCaret' must contain integer values.")
    }
  }
  if (colnames(statements)[4] != "endCaret") {
    stop("The fourth column of 'statements' must be called 'endCaret' and contain the end position of the statement in the text.")
  }
  if (!is.integer(statements[, 4])) {
    if (is.numeric(statements[, 4])) {
      statements[, 4] <- as.integer(statements[, 4])
    } else {
      stop("'statements$endCaret' must contain integer values.")
    }
  }
  if (colnames(statements)[5] != "statementTypeId") {
    stop("The fifth column of 'statements' must be called 'statementTypeId' and contain the statement type IDs.")
  }
  if (!is.integer(statements[, 5])) {
    if (is.numeric(statements[, 5])) {
      statements[, 5] <- as.integer(statements[, 5])
    } else {
      stop("'statements$statementTypeId' must contain integer values.")
    }
  }
  if (colnames(statements)[6] != "coder") {
    stop("The sixth column of 'statements' must be called 'coder' and contain the coder IDs.")
  }
  if (!is.integer(statements[, 6])) {
    if (is.numeric(statements[, 6])) {
      statements[, 6] <- as.integer(statements[, 6])
    } else {
      stop("'statements$coder' must contain integer values.")
    }
  }

  # check validity of variables
  variables <- J(connection$dna_connection, "getVariables", as.integer(statements$statementTypeId[1]))
  variables <- lapply(variables, .jevalArray)
  variables <- as.data.frame(variables, stringsAsFactors = FALSE)
  colnames(variables) <- c("variable", "type")
  for (i in 1:nrow(variables)) {
    if (variables[i, 2] == "boolean") {
      if (is.integer(statements[, 6 + i])) {
        # fine
      } else if (is.numeric(statements[, 6 + i]) || is.logical(statements[, 6 + i])) {
        statements[, 6 + i] <- as.integer(statements[, 6 + i])
      } else {
        stop(paste0("'statements$`", variables[i, 1], "`' must contain only binary values (0 or 1)."))
      }
      if (!all(statements[, 6 + i] %in% 0:1)) {
        stop(paste0("'statements$`", variables[i, 1], "`' must contain only binary values (0 or 1)."))
      }
    } else if (variables[i, 2] == "integer") {
      if (is.integer(statements[, 6 + i])) {
        # fine
      } else if (is.numeric(statements[, 6 + i]) || is.logical(statements[, 6 + i])) {
        statements[, 6 + i] <- as.integer(statements[, 6 + i])
      } else {
        stop(paste0("'statements$`", variables[i, 1], "`' must contain integer values."))
      }
    } else {
      if (!is.character(statements[, 6 + i])) {
        statements[, 6 + i] <- as.character(statements[, 6 + i])
      }
      statements[, 6 + i][is.na(statements[, 6 + i])] <- ""
    }
  }

  if (verbose == TRUE && nrow(statements) == 0) {
    warning("'statements' has 0 rows. Deleting all statements from the database.")
  }

  # replace NAs with -1, which will be replaced by an auto-generated ID in DNA
  if (any(is.na(statements[, 1]))) {
    statements[which(is.na(statements[, 1])), 1] <- as.integer(-1)
  }

  statements <- .jarray(lapply(statements, .jarray))
  .jcall(connection$dna_connection,
         "V",
         "setStatements",
         statements,
         simulate,
         verbose)
}

#' Update the name, color, or permissions of a coder in the DNA database
#'
#' Update the name, color, or permissions of a coder in the DNA database.
#'
#' The \code{dna_updateCoder} function can update the name, color, or any
#' permissions of an existing coder in a DNA database.
#'
#' @param connection A \code{dna_connection} object created by the
#'   \code{dna_connection} function.
#' @param coder The ID (as an integer) or name (as a character object) of the
#'   coder whose values should be updated.
#' @param name A character object indicating the name of the new coder. If
#'   \code{NULL}, the current value is retained and no changes are made.
#' @param color A character object indicating the color in which the coder
#'   should be displayed in the graphical user interface. The color must be
#'   supplied as a hexadecimal string, for example \code{"#FFFF00"} for yellow.
#'   If \code{NULL}, the current value is retained and no changes are made.
#' @param addDocuments Logical: should the coder have the permission to add new
#'   documents via the graphical user interface? If \code{NULL}, the current
#'   value is retained and no changes are made.
#' @param editDocuments Logical: should the coder have permission to edit the
#'   meta-data of documents in the graphical user interface? If \code{NULL},
#'   the current value is retained and no changes are made.
#' @param deleteDocuments Logical: should the coder have permission to delete
#'   documents from the database in the graphical user interface? If
#'   \code{NULL}, the current value is retained and no changes are made.
#' @param importDocuments Logical: should the coder have permission to import
#'   documents into the database via the graphical user interface? If
#'   \code{NULL}, the current value is retained and no changes are made.
#' @param viewOthersDocuments Logical: should the coder have permission to view
#'   the documents that were added by other coders? If \code{NULL}, the current
#'   value is retained and no changes are made.
#' @param editOthersDocuments Logical: should the coder have permission to edit
#'   the meta-data of documents added by other coders? If \code{NULL}, the
#'   current value is retained and no changes are made.
#' @param addStatements Logical: should the coder have permission to add new
#'   statements to the database? If \code{NULL}, the current value is retained
#'   and no changes are made.
#' @param viewOthersStatements Logical: should the coder have permission to view
#'   the statements in the graphical user interface that were added by other
#'   coders? If \code{NULL}, the current value is retained and no changes are
#'   made.
#' @param editOthersStatements Logical: should the coder have permission to edit
#'   the statements in the graphical user interface that were added by other
#'   coders? If \code{NULL}, the current value is retained and no changes are
#'   made.
#' @param editCoders Logical: should the coder have permission to add, remove,
#'   or edit coders in the graphical user interface? If \code{NULL}, the current
#'   value is retained and no changes are made.
#' @param editStatementTypes Logical: should the coder have permission to add,
#'   remove, or edit statement types in the graphical user interface? If
#'   \code{NULL}, the current value is retained and no changes are made.
#' @param editRegex Logical: should the coder have permission to add, remove, or
#'   edit regular expressions in the graphical user interface? If \code{NULL},
#'   the current value is retained and no changes are made.
#' @param verbose Print details?
#'
#' @author Philip Leifeld
#'
#' @importFrom rJava .jcall
#' @export
dna_updateCoder <- function(connection,
                            coder,
                            name = NULL,
                            color = NULL,
                            addDocuments = NULL,
                            editDocuments = NULL,
                            deleteDocuments = NULL,
                            importDocuments = NULL,
                            viewOthersDocuments = NULL,
                            editOthersDocuments = NULL,
                            addStatements = NULL,
                            viewOthersStatements = NULL,
                            editOthersStatements = NULL,
                            editCoders = NULL,
                            editStatementTypes = NULL,
                            editRegex = NULL,
                            verbose = TRUE) {
  if (is.null(connection) || is.na(connection) || class(connection) != "dna_connection") {
    stop("'connection' must be a 'dna_connection' object. Use the 'dna_connection' function to create one.")
  }
  if (!is.null(name) && !is.na(name) && (!is.character(name) || length(name) != 1)) {
    stop("'name' must be NULL (for no changes) or a character object of length 1.")
  }
  if (!is.null(color) && !is.na(color) && length(color) != 1) {
    stop("'color' must be NULL (for no changes) or an object of length 1.")
  } else if (!is.null(color) && is.na(color)) {
    color <- NULL
  } else if (!is.null(color)) {
    color <- col2hex(color)
  }
  if (!is.null(addDocuments) && !is.na(addDocuments) && (!is.logical(addDocuments) || length(addDocuments) != 1)) {
    stop("'addDocuments' must be NULL (for no changes) or TRUE or FALSE.")
  }
  if (!is.null(editDocuments) && !is.na(editDocuments) && (!is.logical(editDocuments) || length(editDocuments) != 1)) {
    stop("'editDocuments' must be NULL (for no changes) or TRUE or FALSE.")
  }
  if (!is.null(deleteDocuments) && !is.na(deleteDocuments) && (!is.logical(deleteDocuments) || length(deleteDocuments) != 1)) {
    stop("'deleteDocuments' must be NULL (for no changes) or TRUE or FALSE.")
  }
  if (!is.null(importDocuments) && !is.na(importDocuments) && (!is.logical(importDocuments) || length(importDocuments) != 1)) {
    stop("'importDocuments' must be NULL (for no changes) or TRUE or FALSE.")
  }
  if (!is.null(viewOthersDocuments) && !is.na(viewOthersDocuments) && (!is.logical(viewOthersDocuments) || length(viewOthersDocuments) != 1)) {
    stop("'viewOthersDocuments' must be NULL (for no changes) or TRUE or FALSE.")
  }
  if (!is.null(editOthersDocuments) && !is.na(editOthersDocuments) && (!is.logical(editOthersDocuments) || length(editOthersDocuments) != 1)) {
    stop("'editOthersDocuments' must be NULL (for no changes) or TRUE or FALSE.")
  }
  if (!is.null(addStatements) && !is.na(addStatements) && (!is.logical(addStatements) || length(addStatements) != 1)) {
    stop("'addStatements' must be NULL (for no changes) or TRUE or FALSE.")
  }
  if (!is.null(viewOthersStatements) && !is.na(viewOthersStatements) && (!is.logical(viewOthersStatements) || length(viewOthersStatements) != 1)) {
    stop("'viewOthersStatements' must be NULL (for no changes) or TRUE or FALSE.")
  }
  if (!is.null(editOthersStatements) && !is.na(editOthersStatements) && (!is.logical(editOthersStatements) || length(editOthersStatements) != 1)) {
    stop("'editOthersStatements' must be NULL (for no changes) or TRUE or FALSE.")
  }
  if (!is.null(editCoders) && !is.na(editCoders) && (!is.logical(editCoders) || length(editCoders) != 1)) {
    stop("'editCoders' must be NULL (for no changes) or TRUE or FALSE.")
  }
  if (!is.null(editStatementTypes) && !is.na(editStatementTypes) && (!is.logical(editStatementTypes) || length(editStatementTypes) != 1)) {
    stop("'editStatementTypes' must be NULL (for no changes) or TRUE or FALSE.")
  }
  if (!is.null(editRegex) && !is.na(editRegex) && (!is.logical(editRegex) || length(editRegex) != 1)) {
    stop("'editRegex' must be NULL (for no changes) or TRUE or FALSE.")
  }
  if (!is.null(name) && is.na(name)) {
    name <- NULL
  }
  if (!is.null(addDocuments) && is.na(addDocuments)) {
    addDocuments <- NULL
  }
  if (!is.null(editDocuments) && is.na(editDocuments)) {
    editDocuments <- NULL
  }
  if (!is.null(deleteDocuments) && is.na(deleteDocuments)) {
    deleteDocuments <- NULL
  }
  if (!is.null(viewOthersDocuments) && is.na(viewOthersDocuments)) {
    viewOthersDocuments <- NULL
  }
  if (!is.null(editOthersDocuments) && is.na(editOthersDocuments)) {
    editOthersDocuments <- NULL
  }
  if (!is.null(addStatements) && is.na(addStatements)) {
    addStatements <- NULL
  }
  if (!is.null(viewOthersStatements) && is.na(viewOthersStatements)) {
    viewOthersStatements <- NULL
  }
  if (!is.null(editOthersStatements) && is.na(editOthersStatements)) {
    editOthersStatements <- NULL
  }
  if (!is.null(editCoders) && is.na(editCoders)) {
    editCoders <- NULL
  }
  if (!is.null(editStatementTypes) && is.na(editStatementTypes)) {
    editStatementTypes <- NULL
  }
  if (!is.null(editRegex) && is.na(editRegex)) {
    editRegex <- NULL
  }
  if (is.null(verbose) || is.na(verbose) || !is.logical(verbose) || length(verbose) != 1) {
    stop("'verbose' must be TRUE or FALSE.")
  }
  coders <- dna_getCoders(connection)
  if (is.character(name)) {
    row <- which(coders$name == coder)
    if (length(row) > 1) {
      stop("More than one coder with name '" + coder + "' was found. Aborting.")
    } else if (length(row) < 1) {
      stop("No coder with name '" + coder + "' was found. Aborting.")
    }
  } else {
    row <- which(coders$ID == coder)
    if (length(row) < 1) {
      stop("No coder with ID " + coder + " was found. Aborting.")
    }
  }
  coderId <- coders$ID[row]
  if (is.null(name)) {
    name <- coders$name[row]
  }
  if (is.null(color)) {
    color <- coders$color[row]
  }
  if (is.null(addDocuments)) {
    addDocuments <- coders$addDocuments[row]
  }
  if (is.null(editDocuments)) {
    editDocuments <- coders$editDocuments[row]
  }
  if (is.null(deleteDocuments)) {
    deleteDocuments <- coders$deleteDocuments[row]
  }
  if (is.null(importDocuments)) {
    importDocuments <- coders$importDocuments[row]
  }
  if (is.null(viewOthersDocuments)) {
    viewOthersDocuments <- coders$viewOthersDocuments[row]
  }
  if (is.null(editOthersDocuments)) {
    editOthersDocuments <- coders$editOthersDocuments[row]
  }
  if (is.null(addStatements)) {
    addStatements <- coders$addStatements[row]
  }
  if (is.null(viewOthersStatements)) {
    viewOthersStatements <- coders$viewOthersStatements[row]
  }
  if (is.null(editOthersStatements)) {
    editOthersStatements <- coders$editOthersStatements[row]
  }
  if (is.null(editCoders)) {
    editCoders <- coders$editCoders[row]
  }
  if (is.null(editStatementTypes)) {
    editStatementTypes <- coders$editStatementTypes[row]
  }
  if (is.null(editRegex)) {
    editRegex <- coders$editRegex[row]
  }
  .jcall(connection$dna_connection,
         "V",
         "updateCoder",
         coderId,
         name,
         color,
         "", # password; not used
         addDocuments,
         editDocuments,
         deleteDocuments,
         importDocuments,
         viewOthersDocuments,
         editOthersDocuments,
         addStatements,
         viewOthersStatements,
         editOthersStatements,
         editCoders,
         editStatementTypes,
         editRegex,
         verbose)
}

#' Update a setting related to the GUI
#'
#' Update a setting related to the graphical user interface.
#'
#' The \code{dna_updateSetting} function can update a setting related to the
#' graphical user interface, such as the last active coder, the width of
#' statement pop-up windows, or the criterion by which statements are colored in
#' the text.
#'
#' @param connection A \code{dna_connection} object created by the
#'   \code{dna_connection} function.
#' @param key The setting to be updated. Valid keys are
#'   \code{"statementColor"}, \code{"activeCoder"}, \code{"popupWidth"},
#'   \code{"version"}, or \code{"date"}.
#' @param value The value to be set. For \code{"statementColor"}, valid values
#'   are \code{"statementType"} or \code{"coder"}. This setting determines by
#'   which criterion statements are colored in the text: either by the color of
#'   the respective statement type or by the color of the active coder. For
#'   \code{"activeCoder"}, valid values are the coder IDs of existing coders
#'   (see \link{dna_getCoders}), but as character objects. This setting ensures
#'   that this coder is activated in the graphical user interface when the
#'   database is opened. For \code{"popupWidth"}, valid values range from
#'   \code{"220"} to \code{"9999"}. This determines the width of the pop-up
#'   windows in the graphical user interface, in which the variables can be
#'   coded. For \code{"version"}, a version string like \code{"2.0 beta 24"}
#'   is permitted. This indicates the version number that was used to create the
#'   database. \code{"date"} is the date the database was last changed by the
#'   graphical user interface. It is given in the format \code{"YYYY-mm-dd"}.
#'   All values must be supplied as character objects.
#'
#' @author Philip Leifeld
#'
#' @importFrom rJava .jcall
#' @export
dna_updateSetting <- function(connection, key, value) {
  if (is.null(key) || is.na(key) || !is.character(key) || length(key) != 1) {
    stop("'key' must be a character object of length 1.")
  }
  if (is.null(value) || is.na(value) || !is.character(value) || length(value) != 1) {
    stop("'value' must be a character object of length 1.")
  }
  .jcall(connection$dna_connection,
         "V",
         "updateSetting",
         key,
         value)
}


# Analysis/Transformation ------------------------------------------------------

#' Cluster network from a DNA connection
#'
#' Clustering methods for DNA connections.
#'
#' Perform a cluster analysis based on a DNA connection. Clustering is performed
#' on the distance matrix of a collated two-mode network for cluster methods
#' "ward.D", "ward.D2", "single", "complete", "average", "mcquitty", "median"
#' and "centroid" or on a one-mode "subtract" network (with negative values
#' replaced by 0) for the cluster methods "edge_betweenness", "leading_eigen"
#' and "walktrap" from the \link{igraph} package. The collated two-mode network
#' is constructed by retrieving individual networks for each of the qualifiers
#' levels and combining the results by columns. Alternatively, you can use a
#' two-mode "subtract" network with option \code{collate = TRUE}. You can look
#' at this network with \code{View(clust$network)} ("clust" being the outcome of
#' a call to \code{dna_cluster()}).
#'
#' The distance matrix is calculated either by \link[vegan]{vegdist}, if the
#' collated two-mode network is binary, or by \link[stats]{dist}, in all other
#' cases.
#'
#' Besides clustering, this function also performs non-metric multidimensional
#' scaling (see \link[MASS]{isoMDS}) and factor analysis (see
#' \link[stats]{factanal}). The results can be extracted from the returned
#' object using \code{clust.l$mds} or \code{clust.l$fa} respectively. Both
#' results can also be plotted using \link{dna_plotCoordinates}.
#'
#' @param connection A \code{dna_connection} object created by the
#'   \link{dna_connection} function.
#' @param variable1 The first variable for network construction (see
#'   \link{dna_network}). Defaults to \code{"organization"}.
#' @param variable2 The second variable for network construction (see
#'   \link{dna_network}). Defaults to \code{"concept"}.
#' @param transpose Logical. If \code{TRUE}, variable2 is clustered instead of
#'   variable1.
#' @param collate Logical. If \code{FALSE}, clustering is performed on a
#'   "subtract" network instead of the collated twomode network (see
#'   \link{dna_network} for information on "subtract" networks).
#' @param duplicates Setting for excluding duplicate statements before network
#'   construction (for details see \link{dna_network}). If exclusion of
#'   duplicates results in a binary matrix, \link[vegan]{vegdist} will be used
#'   instead of \link[stats]{dist} to calculate the dissimilarity matrix.
#' @param clust.method The agglomeration method to be used. When set to
#'   \code{"ward.D"}, \code{"ward.D2"}, \code{"single"}, \code{"complete"},
#'   \code{"average"}, \code{"mcquitty"}, \code{"median"} or \code{"centroid"}
#'   the respective methods from \link[stats]{hclust} will be used. When set to
#'   \code{"edge_betweenness"}, \code{"leading_eigen"} or \code{"walktrap"}
#'   \link[igraph]{cluster_edge_betweenness},
#'   \link[igraph]{cluster_leading_eigen} or \link[igraph]{cluster_walktrap}
#'   respectively, will be used for clustering.
#' @param attribute1,attribute2 Which attribute of variable from DNA should be
#'   used to assign colors? There are two sets of colors saved in the
#'   resulting object as \link{dna_plotDendro} has two graphical elements to
#'   distinguish between values: leaf_colors and leaf_ends. Possible values are
#'   \code{"id"}, \code{"value"}, \code{"color"}, \code{"type"}, \code{"alias"}
#'   and \code{"note"}.
#' @param cutree.k,cutree.h If cutree.k or cutree.h are provided, the tree from
#'   hierarchical clustering is cut into several groups. See $k$ and $h$ in
#'   \link[stats]{cutree} for details.
#' @param dimensions The desired dimension for the solution of the MDS and also
#'   the desired number of factors to extract from the factor analysis. Only two
#'   can be plotted but you might want to calculate more and then choose which
#'   ones to plot.
#' @param ... Additional arguments passed to \link{dna_network},
#'   \link[stats]{factanal}) and \link[MASS]{isoMDS}). This is especially useful
#'   to set qualifier (defaults to \code{"agreement"}) and normalization
#'   (defaults to \code{"no"}) if non-default values are needed in the clustered
#'   network.
#'
#' @examples
#' dna_init()
#' conn <- dna_connection(dna_sample())
#'
#' clust.l <- dna_cluster(conn)
#'
#' dna_plotDendro(clust.l)
#' dna_plotHeatmap(clust.l)
#' dna_plotCoordinates(clust.l,
#' jitter = c(0.5, 0.7))
#'
#' @author Johannes B. Gruber
#' @export
#' @importFrom vegan vegdist
#' @importFrom stats setNames dist hclust cutree as.hclust factanal
#' @importFrom igraph graph_from_adjacency_matrix cluster_leading_eigen
#'   cluster_walktrap E
#' @importFrom dplyr summarise group_by_all
#' @importFrom MASS isoMDS
#' @importFrom cluster pam
#' @importFrom splitstackshape cSplit
#' @importFrom grDevices chull
#' @importFrom utils packageVersion capture.output
dna_cluster <- function(connection,
                        variable1 = "organization",
                        variable2 = "concept",
                        transpose = FALSE,
                        collate = TRUE,
                        duplicates = "document",
                        clust.method = "ward.D2",
                        attribute1 = "color",
                        attribute2 = "value",
                        cutree.k = NULL,
                        cutree.h = NULL,
                        dimensions = 2,
                        ...) {
  dots <- list(...)
  if (any(names(formals(factanal)) %in% names(dots))) {
    dots_fa <- dots[names(dots) %in% names(formals(factanal))]
    dots[names(dots) %in% names(formals(factanal))] <- NULL
  } else {
    dots_fa <- list()
  }

  if ("normalization" %in% names(dots)) {
    normalization_onemode <- ifelse(dots[["normalization"]] %in%
                                      c("no", "average", "Jaccard", "cosine"),
                                    dots[["normalization"]],
                                    "no")
    normalization_twomode <- ifelse(dots[["normalization"]] %in%
                                      c("no", "activity", "prominence"),
                                    dots[["normalization"]],
                                    "no")
    dots["normalization"] <- NULL
  } else {
    normalization_onemode <- "no"
    normalization_twomode <- "no"
  }
  if ("excludeValues" %in% names(dots)) {
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
                         variable1 = variable1,
                         variable2 = variable2,
                         excludeValues = excludeValues,
                         verbose = FALSE
                  ), dots))
  if ("qualifier" %in% names(dots)) {
    qualifier <- dots[["qualifier"]]
    dots[["qualifier"]] <- NULL
  } else {
    qualifier <- "agreement"
  }
  if (qualifier %in% names(excludeValues)) {
    excl <- unlist(unname(excludeValues[qualifier]))
    excludeValues[qualifier] <- NULL
  }
  lvls <- unique(lvls[, qualifier])
  if (exists("excl")) {
    lvls <- lvls[!lvls %in% excl]
    if (length(lvls) < 1) {
      stop (paste0(
        "You excluded all levels of \"", qualifier,
        "\". Computation not possible."
      ))
    }
  }
  if (collate) {
    dta <- lapply(lvls, function(l) {
      excludeVals <- c(stats::setNames(list(lvls[!lvls == l]),
                                       nm = qualifier),
                       excludeValues)
      nw <- do.call(dna_network,
                    c(list(connection = connection,
                           networkType = "twomode",
                           variable1 = variable1,
                           variable2 = variable2,
                           qualifier = qualifier,
                           qualifierAggregation = "ignore",
                           normalization = normalization_twomode,
                           isolates = TRUE,
                           duplicates = duplicates,
                           excludeValues = excludeVals,
                           invertValues = FALSE,
                           verbose = FALSE)
                      , dots)
      )
      colnames(nw) <- paste(colnames(nw), "-", l)
      return(nw)
    })
    dta <- rapply(dta, f = function(x) ifelse(is.nan(x), 0, x), how = "replace" )
    dta <- do.call("cbind", dta)
    dta.fa <- dta
    dta <- dta[rowSums(dta) > 0, ]
    dta <- dta[, colSums(dta) > 0]
  } else {
    dta <- do.call(dna_network,
                   c(list(connection = connection,
                          networkType = "twomode",
                          variable1 = variable1,
                          variable2 = variable2,
                          qualifier = qualifier,
                          qualifierAggregation = "subtract",
                          normalization = normalization_twomode,
                          isolates = TRUE,
                          duplicates = duplicates,
                          excludeValues = excludeValues,
                          invertValues = FALSE,
                          verbose = FALSE)
                     , dots))
    dta.fa <- dta
  }
  if (transpose) {
    dta <- t(dta)
    dta.fa <- t(dta.fa)
    . <- variable1
    variable1 <- variable2
    variable2 <- .
  }
  # create onemode for igraph and MDS louvain cluster
  nw <- do.call(dna_network,
                c(list(connection = connection,
                       networkType = "onemode",
                       variable1 = variable1,
                       variable2 = variable2,
                       qualifier = qualifier,
                       qualifierAggregation = "subtract",
                       normalization = normalization_onemode,
                       isolates = FALSE,
                       duplicates = duplicates,
                       excludeValues = excludeValues,
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
    if (all(dta %in% c(0, 1))) { # test if dta is binary
      d <- vegan::vegdist(dta, method = "jaccard")
    } else {
      d <- dist(dta, method = "euclidean")
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
  if (!is.null(c(cutree.k, cutree.h))) {
    hc$group <- cutree(hc, k = cutree.k, h = cutree.h)
  }
  # Retrieve colors for variable1. Even if transpose, this is correct
  col <- dna_getAttributes(connection = connection,
                           statementType = dots$statementType,
                           variable = variable1,
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
  # FA
  fa <- tryCatch(do.call(factanal,
                         c(list(x = t(dta.fa),
                                factors = dimensions)
                           , dots_fa)),
                 error = function(e) {
                   e <- paste("In factor analysis: ", e)
                   warning(e, call. = FALSE)
                   e <- NULL
                 }
  )
  # MDS
  dta_mds <- dta
  if (any(duplicated(dta_mds))) {
    . <- data.frame(dta_mds, check.names = FALSE)
    . <- dplyr::group_by_all(.)
    .$rn <- row.names(.)
    . <- dplyr::summarise(., rowname = paste(rn, collapse = "|"))
    . <- data.frame(., stringsAsFactors = FALSE)
    row.names(.) <- .$rowname
    dta_mds <- .[, !colnames(.) == "rowname"]
  }
  if (all(dta_mds %in% c(0, 1))) {
    d <- vegan::vegdist(dta_mds, method = "jaccard")
  } else {
    d <- dist(dta_mds, method = "euclidean")
  }
  if (length(d) < 2) {
    stop("Clustering cannot be performed on less than three actors.")
  }
  mds <- MASS::isoMDS(d, trace = FALSE, k = dimensions)
  k.best <- which.max(sapply(seq(from = 2, to = nrow(dta_mds) - 1, by = 1), function(i) {
    cluster::pam(dta_mds, diss = FALSE, k = i)$silinfo$avg.width
  }))
  stress <- mds$stress
  mat <- data.frame(mds$points)
  colnames(mat) <- gsub("^X", "Dimension_", colnames(mat))
  mds <- data.frame(variable = row.names(mds$points),
                    cluster_pam = as.factor(cluster::pam(d, k = k.best)[["clustering"]]),
                    cluster_louvain = as.factor(cluster_louvain$memberships)[match(row.names(mds$points),
                                                                                   cluster_louvain$names)])
  mds <- cbind(mds, mat)
  if (any(grepl("|", mds$variable, fixed = TRUE))) {
    mds <- splitstackshape::cSplit(mds, "variable", "|", "long")
  }
  hc$mds <- data.frame(mds[!duplicated(mds$variable, fromLast = TRUE), ])
  hc$fa <- fa
  attributes(hc$mds)$stress <- stress
  hc$call <- match.call()
  attr(hc, "colors") <- c("attribute1" = attribute1, "attribute2" = attribute2)
  class(hc) <- c("dna_cluster", class(hc))
  hc$network <- dta
  return(hc)
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
#' dna_init()
#' conn <- dna_connection(dna_sample(), verbose = FALSE)
#' clust.l <- dna_cluster(conn)
#' clust.l
#'
#' @export
#' @importFrom stats na.omit
print.dna_cluster <- function(x, ...) {
  if (!is.null(x$call)) {
    cat("\nCall:\n", deparse(x$call), "\n\n", sep = "")
  }
  if (!is.null(x$method)) {
    cat("Cluster method :", x$method, "\n")
  }
  if (!is.null(x$dist.method) & !is.na(x$dist.method)) {
    cat("Distance :", x$dist.method, "\n")
  }
  cat("Number of objects:", length(x$labels), "\n")
  if (length(na.omit(attr(x, "cut"))) > 0) {
    cat("Cut at :", paste(gsub("cutree.", "",
                               names(attr(x, "cut"))), "=",
                          attr(x, "cut"),
                          collapse = ", "),
        "\n")
  }
  cat("Used for colors :\n", paste(names(attr(x, "colors")),
                                   paste0("\"",
                                          attr(x, "colors"),
                                          "\"\n"),
                                   sep = ": ",
                                   collapse = " "))
  cat("\n")
  invisible(x)
}


#' Compute multiple cluster solutions for a discourse network
#'
#' Compute multiple cluster solutions for a discourse network.
#'
#' This function applies a number of different graph clustering techniques to
#' a discourse network dataset. The user provides a \link{dna_connection}
#' object, along with many of the same arguments as in the \link{dna_network}
#' function, and a few additional arguments that determine which kinds of
#' clustering methods should be used and how. In particular, the \code{k}
#' argument can be \code{0} (for arbitrary numbers of clusters) or any positive
#' integer value (e.g., \code{2}, for constraining the number of clusters to
#' exactly \code{k} groups). This is useful for assessing the polarization of a
#' discourse network.
#'
#' In particular, the function can be used to compute the maximal modularity of
#' a smoothed time series of discourse networks using the \code{timewindow} and
#' \code{windowsize} arguments for a given \code{k} across a number of
#' clustering methods. The resulting smoothed time series of bipolarization or
#' multipolarization values can be plotted using the \link{dna_plotModularity}
#' function.
#'
#' The \code{dna_multiclust} function replaces the \code{dna_timeWindow}
#' function that was present in earlier versions of the \pkg{rDNA} package. In
#' order to generate bipolarization measures, use \code{k = 2}. For
#' multipolarization, use \code{k = 0}.
#'
#' It is also possible to switch off all but one clustering method using the
#' respective arguments and carry out a simple cluster analysis with the method
#' of choice for a certain time span of the discourse network, without any time
#' window options.
#'
#' @param saveObjects Store the original output of the respective clustering
#'   method in the \code{cl} slot of the return object? If \code{TRUE}, one
#'   cluster object per time point will be saved, for all time points for which
#'   network data are available. At each time point, only the cluster object
#'   with the highest modularity score will be saved, all others discarded. The
#'   \code{max_mod} slot of the object contains additional information on which
#'   measure was saved at each time point and what the corresponding modularity
#'   score is.
#' @param k The number of clusters to compute. This constrains the choice of
#'   clustering methods because some methods require a predefined \code{k} while
#'   other methods do not. To permit arbitrary numbers of clusters, depending on
#'   the respective algorithm (or the value of modularity in some cases), choose
#'   \code{k = 0}. This corresponds to the theoretical notion of
#'   "multipolarization". For "bipolarization", choose \code{k = 2} in order to
#'   constrain the cluster solutions to exactly two groups.
#' @param k.max If \code{k = 0}, there can be arbitrary numbers of clusters. In
#'   this case, \code{k.max} sets the maximal number of clusters that can be
#'   identified.
#' @param single Include hierarchical clustering with single linkage in the pool
#'   of clustering methods? The \code{\link[stats]{hclust}} function from
#'   the \pkg{stats} package is applied to Jaccard distances in the affiliation
#'   network for this purpose. Only valid if \code{k > 1}.
#' @param average Include hierarchical clustering with average linkage in the
#'   pool of clustering methods? The \code{\link[stats]{hclust}} function from
#'   the \pkg{stats} package is applied to Jaccard distances in the affiliation
#'   network for this purpose. Only valid if \code{k > 1}.
#' @param complete Include hierarchical clustering with complete linkage in the
#'   pool of clustering methods? The \code{\link[stats]{hclust}} function from
#'   the \pkg{stats} package is applied to Jaccard distances in the affiliation
#'   network for this purpose. Only valid if \code{k > 1}.
#' @param ward Include hierarchical clustering with Ward's algorithm in the
#'   pool of clustering methods? The \code{\link[stats]{hclust}} function from
#'   the \pkg{stats} package is applied to Jaccard distances in the affiliation
#'   network for this purpose. If \code{k = 0} is selected, different solutions
#'   with varying \code{k} are attempted, and the solution with the highest
#'   modularity is retained.
#' @param kmeans Include k-means clustering in the pool of clustering methods?
#'   The \code{\link[stats]{kmeans}} function from the \pkg{stats} package is
#'   applied to Jaccard distances in the affiliation network for this purpose.
#'   If \code{k = 0} is selected, different solutions with varying \code{k} are
#'   attempted, and the solution with the highest modularity is retained.
#' @param pam Include partitioning around medoids in the pool of clustering
#'   methods? The \code{\link[cluster]{pam}} function from the \pkg{cluster}
#'   package is applied to Jaccard distances in the affiliation network for this
#'   purpose. If \code{k = 0} is selected, different solutions with varying
#'   \code{k} are attempted, and the solution with the highest modularity is
#'   retained.
#' @param equivalence Include equivalence clustering (as implemented in the
#'   \code{\link[sna]{equiv.clust}} function in the \pkg{sna} package), based on
#'   shortest path distances between nodes (as implemented in the
#'   \code{\link[sna]{sedist}} function in the \pkg{sna} package) in the
#'   positive subtract network? If \code{k = 0} is selected, different solutions
#'   with varying \code{k} are attempted, and the solution with the highest
#'   modularity is retained.
#' @param concor_one Include CONvergence of iterative CORrelations (CONCOR) in
#'   the pool of clustering methods? The algorithm is applied to the positive
#'   subtract network to identify \code{k = 2} clusters. The method is omitted
#'   if \code{k != 2}.
#' @param concor_two Include CONvergence of iterative CORrelations (CONCOR) in
#'   the pool of clustering methods? The algorithm is applied to the affiliation
#'   network to identify \code{k = 2} clusters. The method is omitted
#'   if \code{k != 2}.
#' @param louvain Include the Louvain community detection algorithm in the pool
#'   of clustering methods? The \code{\link[igraph]{cluster_louvain}} function
#'   in the \pkg{igraph} package is applied to the positive subtract network for
#'   this purpose.
#' @param fastgreedy Include the fast and greedy community detection algorithm
#'   in the pool of clustering methods? The
#'   \code{\link[igraph]{cluster_fast_greedy}} function in the \pkg{igraph}
#'   package is applied to the positive subtract network for this purpose.
#' @param walktrap Include the Walktrap community detection algorithm
#'   in the pool of clustering methods? The
#'   \code{\link[igraph]{cluster_walktrap}} function in the \pkg{igraph}
#'   package is applied to the positive subtract network for this purpose.
#' @param leading_eigen Include the leading eigenvector community detection
#'   algorithm in the pool of clustering methods? The
#'   \code{\link[igraph]{cluster_leading_eigen}} function in the \pkg{igraph}
#'   package is applied to the positive subtract network for this purpose.
#' @param edge_betweenness Include the edge betweenness community detection
#'   algorithm by Girvan and Newman in the pool of clustering methods? The
#'   \code{\link[igraph]{cluster_edge_betweenness}} function in the \pkg{igraph}
#'   package is applied to the positive subtract network for this purpose. Note
#'   that this method is disabled by default because there is a
#'   non-deterministic bug in the implementation, which sometimes leads the \R
#'   session to abort; see \url{https://github.com/igraph/rigraph/issues/336}
#'   (as of 15 April 2019).
#' @param infomap Include the infomap community detection algorithm
#'   in the pool of clustering methods? The
#'   \code{\link[igraph]{cluster_infomap}} function in the \pkg{igraph}
#'   package is applied to the positive subtract network for this purpose.
#' @param label_prop Include the label propagation community detection algorithm
#'   in the pool of clustering methods? The
#'   \code{\link[igraph]{cluster_label_prop}} function in the \pkg{igraph}
#'   package is applied to the positive subtract network for this purpose.
#' @param spinglass Include the spinglass community detection algorithm
#'   in the pool of clustering methods? The
#'   \code{\link[igraph]{cluster_spinglass}} function in the \pkg{igraph}
#'   package is applied to the positive subtract network for this purpose. Note
#'   that this method is disabled by default because it is relatively slow.
#' @inheritParams dna_network
#'
#' @return The function creates a \code{dna_multiclust} object, which contains
#'   the following items:
#' \describe{
#'   \item{k}{The number of clusters determined by the user.}
#'   \item{cl}{Cluster objects returned by the respective cluster function. If
#'     multiple methods are used, this returns the object with the highest
#'     modularity.}
#'   \item{max_mod}{A data frame with one row per time point (that is, only one
#'     row in the default case and multiple rows if time windows are used) and
#'     the maximal modularity for the given time point across all cluster
#'     methods.}
#'   \item{modularity}{A data frame with the modularity values for all separate
#'     cluster methods and all time points.}
#'   \item{membership}{A large data frame with all nodes' membership information
#'     for each time point and each clustering method.}
#' }
#'
#' @author Philip Leifeld
#'
#' @seealso \code{\link{print.dna_multiclust}}, \code{\link{dna_plotModularity}}
#'
#' @examples
#' library("rDNA")
#' dna_init()
#' samp <- dna_sample()
#' conn <- dna_connection(samp)
#'
#' # example 1: compute 12 cluster solutions for one time point
#' mc1 <- dna_multiclust(connection = conn,
#'                       variable1 = "organization",
#'                       variable2 = "concept",
#'                       qualifier = "agreement",
#'                       duplicates = "document",
#'                       k = 0,                # flexible numbers of clusters
#'                       saveObjects = TRUE)   # retain hclust object
#'
#' mc1$modularity      # return modularity scores for 12 clustering methods
#' mc1$max_mod         # return the maximal value of the 12, along with dates
#' mc1$memberships     # return cluster memberships for all 12 cluster methods
#' plot(mc1$cl[[1]])   # plot hclust dendrogram
#'
#' # example 2: compute only Girvan-Newman edge betweenness with two clusters
#' set.seed(12345)
#' mc2 <- dna_multiclust(connection = conn,
#'                       k = 2,
#'                       single = FALSE,
#'                       average = FALSE,
#'                       complete = FALSE,
#'                       ward = FALSE,
#'                       kmeans = FALSE,
#'                       pam = FALSE,
#'                       equivalence = FALSE,
#'                       concor_one = FALSE,
#'                       concor_two = FALSE,
#'                       louvain = FALSE,
#'                       fastgreedy = FALSE,
#'                       walktrap = FALSE,
#'                       leading_eigen = FALSE,
#'                       edge_betweenness = TRUE,
#'                       infomap = FALSE,
#'                       label_prop = FALSE,
#'                       spinglass = FALSE)
#' mc2$memberships  # return membership in two clusters
#' mc2$modularity   # return modularity of the cluster solution
#'
#' # example 3: smoothed bipolarization using time window algorithm
#' mc3 <- dna_multiclust(connection = conn,
#'                       k = 2,
#'                       timewindow = "events",
#'                       windowsize = 28)
#' mc3$max_mod              # maximal modularity and method per time point
#' dna_plotModularity(mc3)  # smoothed polarization curve (toy example)
#' dna_plotModularity(mc3, only.max = FALSE)  # separately for all methods
#' dna_plotModularity(mc3, anomalize = TRUE)  # anomaly detection
#'
#' @import ggplot2
#' @importFrom cluster pam
#' @importFrom dplyr bind_rows progress_estimated
#' @importFrom igraph graph.adjacency modularity cluster_fast_greedy
#'   cluster_walktrap cluster_leading_eigen cluster_edge_betweenness
#'   cluster_louvain cut_at
#' @importFrom sna equiv.clust sedist
#' @importFrom stats kmeans hclust cutree cor
#' @importFrom vegan vegdist
#' @export
dna_multiclust <- function(connection,
                           statementType = "DNA Statement",
                           variable1 = "organization",
                           variable1Document = FALSE,
                           variable2 = "concept",
                           variable2Document = FALSE,
                           qualifier = "agreement",
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
                           saveObjects = FALSE,
                           k = 0,
                           k.max = 5,
                           single = TRUE,
                           average = TRUE,
                           complete = TRUE,
                           ward = TRUE,
                           kmeans = TRUE,
                           pam = TRUE,
                           equivalence = TRUE,
                           concor_one = TRUE,
                           concor_two = TRUE,
                           louvain = TRUE,
                           fastgreedy = TRUE,
                           walktrap = TRUE,
                           leading_eigen = TRUE,
                           edge_betweenness = FALSE,
                           infomap = TRUE,
                           label_prop = TRUE,
                           spinglass = FALSE,
                           verbose = TRUE) {

  if (is.null(k) || is.na(k) || !is.numeric(k) || length(k) > 1 || is.infinite(k) || k < 0) {
    stop("'k' must be a non-negative integer number. Can be 0 for flexible numbers of clusters.")
  }
  if (is.null(k.max) || is.na(k.max) || !is.numeric(k.max) || length(k.max) > 1 || is.infinite(k.max) || k.max < 1) {
    stop("'k.max' must be a positive integer number.")
  }

  # determine what kind of two-mode network to create
  if (is.null(qualifier) || is.na(qualifier) || !is.character(qualifier)) {
    qualifierAggregation <- "ignore"
  } else {
    v <- dna_getVariables(connection = connection, statementType = statementType)
    if (v$type[v$label == qualifier] == "boolean") {
      qualifierAggregation <- "combine"
    } else {
      qualifierAggregation <- "subtract"
    }
  }

  if (isTRUE(verbose)) {
    message("(1/3) Computing networks... ", appendLF = FALSE)
  }
  nw_aff <- dna_network(connection,
                        networkType = "twomode",
                        statementType = statementType,
                        variable1 = variable1,
                        variable1Document = variable1Document,
                        variable2 = variable2,
                        variable2Document = variable2Document,
                        qualifier = qualifier,
                        qualifierAggregation = qualifierAggregation,
                        normalization = "no",
                        duplicates = duplicates,
                        start.date = start.date,
                        stop.date = stop.date,
                        start.time = start.time,
                        stop.time = stop.time,
                        timewindow = timewindow,
                        windowsize = windowsize,
                        excludeValues = excludeValues,
                        excludeAuthors = excludeAuthors,
                        excludeSources = excludeSources,
                        excludeSections = excludeSections,
                        excludeTypes = excludeTypes,
                        invertValues = invertValues,
                        invertAuthors = invertAuthors,
                        invertSources = invertSources,
                        invertSections = invertSections,
                        invertTypes = invertTypes,
                        verbose = FALSE)
  nw_sub <- dna_network(connection,
                        networkType = "onemode",
                        statementType = statementType,
                        variable1 = variable1,
                        variable1Document = variable1Document,
                        variable2 = variable2,
                        variable2Document = variable2Document,
                        qualifier = qualifier,
                        qualifierAggregation = "subtract",
                        normalization = "average",
                        duplicates = duplicates,
                        start.date = start.date,
                        stop.date = stop.date,
                        start.time = start.time,
                        stop.time = stop.time,
                        timewindow = timewindow,
                        windowsize = windowsize,
                        excludeValues = excludeValues,
                        excludeAuthors = excludeAuthors,
                        excludeSources = excludeSources,
                        excludeSections = excludeSections,
                        excludeTypes = excludeTypes,
                        invertValues = invertValues,
                        invertAuthors = invertAuthors,
                        invertSources = invertSources,
                        invertSections = invertSections,
                        invertTypes = invertTypes,
                        verbose = FALSE)

  if (timewindow == "no") {
    dta <- list()
    dta$networks <- list(nw_sub)
    nw_sub <- dta
    dta <- list()
    dta$networks <- list(nw_aff)
    nw_aff <- dta
  }
  if (isTRUE(verbose)) {
    message("done.")
    if (timewindow == "no") {
      message("(2/3) Computing cluster solutions... ", appendLF = FALSE)
    } else {
      message("(2/3) Computing cluster solutions... ", appendLF = TRUE)
    }
  }

  p <- dplyr::progress_estimated(length(nw_sub$networks))

  obj <- list()
  if (isTRUE(saveObjects)) {
    obj$cl <- list()
  }
  dta_dat <- list()
  dta_mem <- list()
  dta_mod <- list()
  counter <- 1
  for (i in 1:length(nw_sub$networks)) {
    p$tick()$print()

    # prepare dates
    if (timewindow == "no") {
      dta_dat[[i]] <- data.frame(i = i,
                                 start = attributes(nw_sub$networks[[i]])$start,
                                 stop = attributes(nw_sub$networks[[i]])$stop)
    } else {
      dta_dat[[i]] <- data.frame(i = i,
                                 start.date = nw_sub$start[i],
                                 middle.date = nw_sub$time[i],
                                 stop.date = nw_sub$stop[i])
    }

    # prepare two-mode network
    x <- nw_aff$networks[[i]]
    if (qualifierAggregation == "combine") {
      combined <- cbind(apply(x, 1:2, function(x) ifelse(x %in% c(1, 3), 1, 0)),
                        apply(x, 1:2, function(x) ifelse(x %in% c(2, 3), 1, 0)))
    } else {
      combined <- x
    }
    combined <- combined[rowSums(combined) > 0, , drop = FALSE]
    jac <- vegan::vegdist(combined, method = "jaccard")

    # prepare one-mode network
    y <- nw_sub$networks[[i]]
    y[y < 0] <- 0
    class(y) <- "matrix"
    g <- igraph::graph.adjacency(y, mode = "undirected", weighted = TRUE)

    if (nrow(combined) > 1) {
      counter_current <- 1
      current_cl <- list()
      current_mod <- numeric()

      # Hierarchical clustering with single linkage
      if (isTRUE(single) && k > 1) {
        try({
          suppressWarnings(cl <- hclust(jac, method = "single"))
          mem <- cutree(cl, k = k)
          dta_mem[[counter]] <- data.frame(i = rep(i, length(mem)),
                                           method = rep("Hierarchical (Single)", length(mem)),
                                           node = rownames(x),
                                           cluster = mem,
                                           stringsAsFactors = FALSE)
          dta_mod[[counter]] <- data.frame(i = i,
                                           method = "Hierarchical (Single)",
                                           k = k,
                                           modularity = igraph::modularity(x = g, membership = mem),
                                           stringsAsFactors = FALSE)
          if (isTRUE(saveObjects)) {
            current_cl[[counter_current]] <- cl
            current_mod[counter_current] <- dta_mod[[counter]]$modularity[nrow(dta_mod[[counter]])]
            counter_current <- counter_current + 1
          }
          counter <- counter + 1
        }, silent = TRUE)
      }

      # Hierarchical clustering with single linkage with optimal k
      if (isTRUE(single) && k < 2) {
        try({
          suppressWarnings(cl <- hclust(jac, method = "single"))
          opt_k <- lapply(2:k.max, function(x) {
            mem <- cutree(cl, k = x)
            mod <- igraph::modularity(x = g, membership = mem)
            return(list(mem = mem, mod = mod))
          })
          mod <- sapply(opt_k, function(x) x$mod)
          kk <- which.max(mod)
          mod <- max(mod)
          mem <- opt_k[[kk]]$mem
          dta_mem[[counter]] <- data.frame(i = rep(i, length(mem)),
                                           method = rep("Hierarchical (Single)", length(mem)),
                                           node = rownames(x),
                                           cluster = mem,
                                           stringsAsFactors = FALSE)
          dta_mod[[counter]] <- data.frame(i = i,
                                           method = "Hierarchical (Single)",
                                           k = kk + 1, # add one because the series started with k = 2
                                           modularity = mod,
                                           stringsAsFactors = FALSE)
          if (isTRUE(saveObjects)) {
            current_cl[[counter_current]] <- cl
            current_mod[counter_current] <- dta_mod[[counter]]$modularity[nrow(dta_mod[[counter]])]
            counter_current <- counter_current + 1
          }
          counter <- counter + 1
        }, silent = TRUE)
      }

      # Hierarchical clustering with average linkage
      if (isTRUE(average) && k > 1) {
        try({
          suppressWarnings(cl <- hclust(jac, method = "average"))
          mem <- cutree(cl, k = k)
          dta_mem[[counter]] <- data.frame(i = rep(i, length(mem)),
                                           method = rep("Hierarchical (Average)", length(mem)),
                                           node = rownames(x),
                                           cluster = mem,
                                           stringsAsFactors = FALSE)
          dta_mod[[counter]] <- data.frame(i = i,
                                           method = "Hierarchical (Average)",
                                           k = k,
                                           modularity = igraph::modularity(x = g, membership = mem),
                                           stringsAsFactors = FALSE)
          if (isTRUE(saveObjects)) {
            current_cl[[counter_current]] <- cl
            current_mod[counter_current] <- dta_mod[[counter]]$modularity[nrow(dta_mod[[counter]])]
            counter_current <- counter_current + 1
          }
          counter <- counter + 1
        }, silent = TRUE)
      }

      # Hierarchical clustering with average linkage with optimal k
      if (isTRUE(average) && k < 2) {
        try({
          suppressWarnings(cl <- hclust(jac, method = "average"))
          opt_k <- lapply(2:k.max, function(x) {
            mem <- cutree(cl, k = x)
            mod <- igraph::modularity(x = g, membership = mem)
            return(list(mem = mem, mod = mod))
          })
          mod <- sapply(opt_k, function(x) x$mod)
          kk <- which.max(mod)
          mod <- max(mod)
          mem <- opt_k[[kk]]$mem
          dta_mem[[counter]] <- data.frame(i = rep(i, length(mem)),
                                           method = rep("Hierarchical (Average)", length(mem)),
                                           node = rownames(x),
                                           cluster = mem,
                                           stringsAsFactors = FALSE)
          dta_mod[[counter]] <- data.frame(i = i,
                                           method = "Hierarchical (Average)",
                                           k = kk + 1, # add one because the series started with k = 2
                                           modularity = mod,
                                           stringsAsFactors = FALSE)
          if (isTRUE(saveObjects)) {
            current_cl[[counter_current]] <- cl
            current_mod[counter_current] <- dta_mod[[counter]]$modularity[nrow(dta_mod[[counter]])]
            counter_current <- counter_current + 1
          }
          counter <- counter + 1
        }, silent = TRUE)
      }

      # Hierarchical clustering with complete linkage
      if (isTRUE(complete) && k > 1) {
        try({
          suppressWarnings(cl <- hclust(jac, method = "complete"))
          mem <- cutree(cl, k = k)
          dta_mem[[counter]] <- data.frame(i = rep(i, length(mem)),
                                           method = rep("Hierarchical (Complete)", length(mem)),
                                           node = rownames(x),
                                           cluster = mem,
                                           stringsAsFactors = FALSE)
          dta_mod[[counter]] <- data.frame(i = i,
                                           method = "Hierarchical (Complete)",
                                           k = k,
                                           modularity = igraph::modularity(x = g, membership = mem),
                                           stringsAsFactors = FALSE)
          if (isTRUE(saveObjects)) {
            current_cl[[counter_current]] <- cl
            current_mod[counter_current] <- dta_mod[[counter]]$modularity[nrow(dta_mod[[counter]])]
            counter_current <- counter_current + 1
          }
          counter <- counter + 1
        }, silent = TRUE)
      }

      # Hierarchical clustering with complete linkage with optimal k
      if (isTRUE(complete) && k < 2) {
        try({
          suppressWarnings(cl <- hclust(jac, method = "complete"))
          opt_k <- lapply(2:k.max, function(x) {
            mem <- cutree(cl, k = x)
            mod <- igraph::modularity(x = g, membership = mem)
            return(list(mem = mem, mod = mod))
          })
          mod <- sapply(opt_k, function(x) x$mod)
          kk <- which.max(mod)
          mod <- max(mod)
          mem <- opt_k[[kk]]$mem
          dta_mem[[counter]] <- data.frame(i = rep(i, length(mem)),
                                           method = rep("Hierarchical (Complete)", length(mem)),
                                           node = rownames(x),
                                           cluster = mem,
                                           stringsAsFactors = FALSE)
          dta_mod[[counter]] <- data.frame(i = i,
                                           method = "Hierarchical (Complete)",
                                           k = kk + 1, # add one because the series started with k = 2
                                           modularity = mod,
                                           stringsAsFactors = FALSE)
          if (isTRUE(saveObjects)) {
            current_cl[[counter_current]] <- cl
            current_mod[counter_current] <- dta_mod[[counter]]$modularity[nrow(dta_mod[[counter]])]
            counter_current <- counter_current + 1
          }
          counter <- counter + 1
        }, silent = TRUE)
      }

      # Hierarchical clustering with the Ward algorithm
      if (isTRUE(ward) && k > 1) {
        try({
          suppressWarnings(cl <- hclust(jac, method = "ward.D2"))
          mem <- cutree(cl, k = k)
          dta_mem[[counter]] <- data.frame(i = rep(i, length(mem)),
                                           method = rep("Hierarchical (Ward)", length(mem)),
                                           node = rownames(x),
                                           cluster = mem,
                                           stringsAsFactors = FALSE)
          dta_mod[[counter]] <- data.frame(i = i,
                                           method = "Hierarchical (Ward)",
                                           k = k,
                                           modularity = igraph::modularity(x = g, membership = mem),
                                           stringsAsFactors = FALSE)
          if (isTRUE(saveObjects)) {
            current_cl[[counter_current]] <- cl
            current_mod[counter_current] <- dta_mod[[counter]]$modularity[nrow(dta_mod[[counter]])]
            counter_current <- counter_current + 1
          }
          counter <- counter + 1
        }, silent = TRUE)
      }

      # Hierarchical clustering with the Ward algorithm with optimal k
      if (isTRUE(ward) && k < 2) {
        try({
          suppressWarnings(cl <- hclust(jac, method = "ward.D2"))
          opt_k <- lapply(2:k.max, function(x) {
            mem <- cutree(cl, k = x)
            mod <- igraph::modularity(x = g, membership = mem)
            return(list(mem = mem, mod = mod))
          })
          mod <- sapply(opt_k, function(x) x$mod)
          kk <- which.max(mod)
          mod <- max(mod)
          mem <- opt_k[[kk]]$mem
          dta_mem[[counter]] <- data.frame(i = rep(i, length(mem)),
                                           method = rep("Hierarchical (Ward)", length(mem)),
                                           node = rownames(x),
                                           cluster = mem,
                                           stringsAsFactors = FALSE)
          dta_mod[[counter]] <- data.frame(i = i,
                                           method = "Hierarchical (Ward)",
                                           k = kk + 1, # add one because the series started with k = 2
                                           modularity = mod,
                                           stringsAsFactors = FALSE)
          if (isTRUE(saveObjects)) {
            current_cl[[counter_current]] <- cl
            current_mod[counter_current] <- dta_mod[[counter]]$modularity[nrow(dta_mod[[counter]])]
            counter_current <- counter_current + 1
          }
          counter <- counter + 1
        }, silent = TRUE)
      }

      # k-means
      if (isTRUE(kmeans) && k > 1) {
        try({
          suppressWarnings(cl <- kmeans(jac, centers = k))
          mem <- cl$cluster
          dta_mem[[counter]] <- data.frame(i = rep(i, length(mem)),
                                           method = rep("k-Means", length(mem)),
                                           node = rownames(x),
                                           cluster = mem,
                                           stringsAsFactors = FALSE)
          dta_mod[[counter]] <- data.frame(i = i,
                                           method = "k-Means",
                                           k = k,
                                           modularity = igraph::modularity(x = g, membership = mem),
                                           stringsAsFactors = FALSE)
          if (isTRUE(saveObjects)) {
            current_cl[[counter_current]] <- cl
            current_mod[counter_current] <- dta_mod[[counter]]$modularity[nrow(dta_mod[[counter]])]
            counter_current <- counter_current + 1
          }
          counter <- counter + 1
        }, silent = TRUE)
      }

      # k-means with optimal k
      if (isTRUE(kmeans) && k < 2) {
        try({
          opt_k <- lapply(2:k.max, function(x) {
            suppressWarnings(cl <- kmeans(jac, centers = x))
            mem <- cl$cluster
            mod <- igraph::modularity(x = g, membership = mem)
            return(list(cl = cl, mem = mem, mod = mod))
          })
          mod <- sapply(opt_k, function(x) x$mod)
          kk <- which.max(mod)
          mod <- max(mod)
          mem <- opt_k[[kk]]$mem
          dta_mem[[counter]] <- data.frame(i = rep(i, length(mem)),
                                           method = rep("k-Means", length(mem)),
                                           node = rownames(x),
                                           cluster = mem,
                                           stringsAsFactors = FALSE)
          dta_mod[[counter]] <- data.frame(i = i,
                                           method = "k-Means",
                                           k = kk + 1, # add one because the series started with k = 2
                                           modularity = mod,
                                           stringsAsFactors = FALSE)
          if (isTRUE(saveObjects)) {
            cl <- opt_k[[kk]]$cl
            current_cl[[counter_current]] <- cl
            current_mod[counter_current] <- dta_mod[[counter]]$modularity[nrow(dta_mod[[counter]])]
            counter_current <- counter_current + 1
          }
          counter <- counter + 1
        }, silent = TRUE)
      }

      # pam
      if (isTRUE(pam) && k > 1) {
        try({
          suppressWarnings(cl <- cluster::pam(jac, k = k))
          mem <- cl$cluster
          dta_mem[[counter]] <- data.frame(i = rep(i, length(mem)),
                                           method = rep("Partitioning around Medoids", length(mem)),
                                           node = rownames(x),
                                           cluster = mem,
                                           stringsAsFactors = FALSE)
          dta_mod[[counter]] <- data.frame(i = i,
                                           method = "Partitioning around Medoids",
                                           k = k,
                                           modularity = igraph::modularity(x = g, membership = mem),
                                           stringsAsFactors = FALSE)
          if (isTRUE(saveObjects)) {
            current_cl[[counter_current]] <- cl
            current_mod[counter_current] <- dta_mod[[counter]]$modularity[nrow(dta_mod[[counter]])]
            counter_current <- counter_current + 1
          }
          counter <- counter + 1
        }, silent = TRUE)
      }

      # pam with optimal k
      if (isTRUE(pam) && k < 2) {
        try({
          opt_k <- lapply(2:k.max, function(x) {
            suppressWarnings(cl <- cluster::pam(jac, k = x))
            mem <- cl$cluster
            mod <- igraph::modularity(x = g, membership = mem)
            return(list(cl = cl, mem = mem, mod = mod))
          })
          mod <- sapply(opt_k, function(x) x$mod)
          kk <- which.max(mod)
          mod <- max(mod)
          mem <- opt_k[[kk]]$mem
          dta_mem[[counter]] <- data.frame(i = rep(i, length(mem)),
                                           method = rep("Partitioning around Medoids", length(mem)),
                                           node = rownames(x),
                                           cluster = mem,
                                           stringsAsFactors = FALSE)
          dta_mod[[counter]] <- data.frame(i = i,
                                           method = "Partitioning around Medoids",
                                           k = kk + 1, # add one because the series started with k = 2
                                           modularity = mod,
                                           stringsAsFactors = FALSE)
          if (isTRUE(saveObjects)) {
            cl <- opt_k[[kk]]$cl
            current_cl[[counter_current]] <- cl
            current_mod[counter_current] <- dta_mod[[counter]]$modularity[nrow(dta_mod[[counter]])]
            counter_current <- counter_current + 1
          }
          counter <- counter + 1
        }, silent = TRUE)
      }

      # Equivalence clustering
      if (isTRUE(equivalence) && k > 1) {
        try({
          suppressWarnings(cl <- sna::equiv.clust(y, sna::sedist(y, method = "euclidean")))
          mem <- cutree(cl$cluster, k = k)
          dta_mem[[counter]] <- data.frame(i = rep(i, length(mem)),
                                           method = rep("Equivalence", length(mem)),
                                           node = rownames(x),
                                           cluster = mem,
                                           stringsAsFactors = FALSE)
          dta_mod[[counter]] <- data.frame(i = i,
                                           method = "Equivalence",
                                           k = k,
                                           modularity = igraph::modularity(x = g, membership = mem),
                                           stringsAsFactors = FALSE)
          if (isTRUE(saveObjects)) {
            current_cl[[counter_current]] <- cl
            current_mod[counter_current] <- dta_mod[[counter]]$modularity[nrow(dta_mod[[counter]])]
            counter_current <- counter_current + 1
          }
          counter <- counter + 1
        }, silent = TRUE)
      }

      # Equivalence clustering with optimal k
      if (isTRUE(equivalence) && k < 2) {
        try({
          suppressWarnings(cl <- sna::equiv.clust(y, sna::sedist(y, method = "euclidean")))
          opt_k <- lapply(2:k.max, function(x) {
            mem <- cutree(cl$cluster, k = x)
            mod <- igraph::modularity(x = g, membership = mem)
            return(list(mem = mem, mod = mod))
          })
          mod <- sapply(opt_k, function(x) x$mod)
          kk <- which.max(mod)
          mod <- max(mod)
          mem <- opt_k[[kk]]$mem
          dta_mem[[counter]] <- data.frame(i = rep(i, length(mem)),
                                           method = rep("Equivalence", length(mem)),
                                           node = rownames(x),
                                           cluster = mem,
                                           stringsAsFactors = FALSE)
          dta_mod[[counter]] <- data.frame(i = i,
                                           method = "Equivalence",
                                           k = kk + 1, # add one because the series started with k = 2
                                           modularity = mod,
                                           stringsAsFactors = FALSE)
          if (isTRUE(saveObjects)) {
            current_cl[[counter_current]] <- cl
            current_mod[counter_current] <- dta_mod[[counter]]$modularity[nrow(dta_mod[[counter]])]
            counter_current <- counter_current + 1
          }
          counter <- counter + 1
        }, silent = TRUE)
      }

      # CONCOR based on the positive subtract network
      if (isTRUE(concor_one) && k %in% c(0, 2)) {
        try({
          suppressWarnings(mi <- cor(y))
          iter <- 1
          while (any(abs(mi) <= 0.999) & iter <= 50) {
            mi[is.na(mi)] <- 0
            mi <- cor(mi)
            iter <- iter + 1
          }
          mem <- ((mi[, 1] > 0) * 1) + 1
          dta_mem[[counter]] <- data.frame(i = rep(i, length(mem)),
                                           method = rep("CONCOR (One-Mode)", length(mem)),
                                           node = rownames(x),
                                           cluster = mem,
                                           stringsAsFactors = FALSE)
          dta_mod[[counter]] <- data.frame(i = i,
                                           method = "CONCOR (One-Mode)",
                                           k = 2,
                                           modularity = igraph::modularity(x = g, membership = mem),
                                           stringsAsFactors = FALSE)
          if (isTRUE(saveObjects)) {
            current_cl[[counter_current]] <- mem
            current_mod[counter_current] <- dta_mod[[counter]]$modularity[nrow(dta_mod[[counter]])]
            counter_current <- counter_current + 1
          }
          counter <- counter + 1
        }, silent = TRUE)
      }

      # CONCOR based on the combined affiliation network
      if (isTRUE(concor_two) && k %in% c(0, 2)) {
        try({
          suppressWarnings(mi <- cor(t(combined)))
          iter <- 1
          while (any(abs(mi) <= 0.999) & iter <= 50) {
            mi[is.na(mi)] <- 0
            mi <- cor(mi)
            iter <- iter + 1
          }
          mem <- ((mi[, 1] > 0) * 1) + 1
          dta_mem[[counter]] <- data.frame(i = rep(i, length(mem)),
                                           method = rep("CONCOR (Two-Mode)", length(mem)),
                                           node = rownames(x),
                                           cluster = mem,
                                           stringsAsFactors = FALSE)
          dta_mod[[counter]] <- data.frame(i = i,
                                           method = "CONCOR (Two-Mode)",
                                           k = 2,
                                           modularity = igraph::modularity(x = g, membership = mem),
                                           stringsAsFactors = FALSE)
          if (isTRUE(saveObjects)) {
            current_cl[[counter_current]] <- mem
            current_mod[counter_current] <- dta_mod[[counter]]$modularity[nrow(dta_mod[[counter]])]
            counter_current <- counter_current + 1
          }
          counter <- counter + 1
        }, silent = TRUE)
      }

      # Louvain clustering
      if (isTRUE(louvain) && k < 2) {
        try({
          suppressWarnings(cl <- igraph::cluster_louvain(g))
          mem <- igraph::membership(cl)
          dta_mem[[counter]] <- data.frame(i = rep(i, length(mem)),
                                           method = rep("Louvain", length(mem)),
                                           node = rownames(x),
                                           cluster = as.numeric(mem),
                                           stringsAsFactors = FALSE)
          dta_mod[[counter]] <- data.frame(i = i,
                                           method = "Louvain",
                                           k = max(as.numeric(mem)),
                                           modularity = igraph::modularity(x = g, membership = mem),
                                           stringsAsFactors = FALSE)
          if (isTRUE(saveObjects)) {
            current_cl[[counter_current]] <- cl
            current_mod[counter_current] <- dta_mod[[counter]]$modularity[nrow(dta_mod[[counter]])]
            counter_current <- counter_current + 1
          }
          counter <- counter + 1
        }, silent = TRUE)
      }

      # Fast & Greedy community detection (with or without cut)
      if (isTRUE(fastgreedy)) {
        try({
          suppressWarnings(cl <- igraph::cluster_fast_greedy(g, merges = TRUE))
          if (k == 0) {
            mem <- igraph::membership(cl)
          } else {
            mem <- suppressWarnings(igraph::cut_at(cl, no = k))
            if ((k + 1) %in% as.numeric(mem)) {
              stop()
            }
          }
          dta_mem[[counter]] <- data.frame(i = rep(i, length(mem)),
                                           method = rep("Fast & Greedy", length(mem)),
                                           node = rownames(x),
                                           cluster = as.numeric(mem),
                                           stringsAsFactors = FALSE)
          dta_mod[[counter]] <- data.frame(i = i,
                                           method = "Fast & Greedy",
                                           k = ifelse(k == 0, max(as.numeric(mem)), k),
                                           modularity = igraph::modularity(x = g, membership = mem),
                                           stringsAsFactors = FALSE)
          if (isTRUE(saveObjects)) {
            current_cl[[counter_current]] <- cl
            current_mod[counter_current] <- dta_mod[[counter]]$modularity[nrow(dta_mod[[counter]])]
            counter_current <- counter_current + 1
          }
          counter <- counter + 1
        }, silent = TRUE)
      }

      # Walktrap community detection (with or without cut)
      if (isTRUE(walktrap)) {
        try({
          suppressWarnings(cl <- igraph::cluster_walktrap(g, merges = TRUE))
          if (k == 0) {
            mem <- igraph::membership(cl)
          } else {
            mem <- suppressWarnings(igraph::cut_at(cl, no = k))
            if ((k + 1) %in% as.numeric(mem)) {
              stop()
            }
          }
          dta_mem[[counter]] <- data.frame(i = rep(i, length(mem)),
                                           method = rep("Walktrap", length(mem)),
                                           node = rownames(x),
                                           cluster = as.numeric(mem),
                                           stringsAsFactors = FALSE)
          dta_mod[[counter]] <- data.frame(i = i,
                                           method = "Walktrap",
                                           k = ifelse(k == 0, max(as.numeric(mem)), k),
                                           modularity = igraph::modularity(x = g, membership = mem),
                                           stringsAsFactors = FALSE)
          if (isTRUE(saveObjects)) {
            current_cl[[counter_current]] <- cl
            current_mod[counter_current] <- dta_mod[[counter]]$modularity[nrow(dta_mod[[counter]])]
            counter_current <- counter_current + 1
          }
          counter <- counter + 1
        }, silent = TRUE)
      }

      # Leading Eigenvector community detection (only without cut)
      if (isTRUE(leading_eigen) && k < 2) { # it *should* work with cut_at because is.hierarchical(cl) returns TRUE, but it never works...
        try({
          suppressWarnings(cl <- igraph::cluster_leading_eigen(g))
          mem <- igraph::membership(cl)
          dta_mem[[counter]] <- data.frame(i = rep(i, length(mem)),
                                           method = rep("Leading Eigenvector", length(mem)),
                                           node = rownames(x),
                                           cluster = as.numeric(mem),
                                           stringsAsFactors = FALSE)
          dta_mod[[counter]] <- data.frame(i = i,
                                           method = "Leading Eigenvector",
                                           k = max(as.numeric(mem)),
                                           modularity = igraph::modularity(x = g, membership = mem),
                                           stringsAsFactors = FALSE)
          if (isTRUE(saveObjects)) {
            current_cl[[counter_current]] <- cl
            current_mod[counter_current] <- dta_mod[[counter]]$modularity[nrow(dta_mod[[counter]])]
            counter_current <- counter_current + 1
          }
          counter <- counter + 1
        }, silent = TRUE)
      }

      # Edge Betweenness community detection (with or without cut)
      if (isTRUE(edge_betweenness)) {
        try({
          suppressWarnings(cl <- igraph::cluster_edge_betweenness(g, merges = TRUE))
          if (k == 0) {
            mem <- igraph::membership(cl)
          } else {
            mem <- suppressWarnings(igraph::cut_at(cl, no = k))
            if ((k + 1) %in% as.numeric(mem)) {
              stop()
            }
          }
          dta_mem[[counter]] <- data.frame(i = rep(i, length(mem)),
                                           method = rep("Edge Betweenness", length(mem)),
                                           node = rownames(x),
                                           cluster = as.numeric(mem),
                                           stringsAsFactors = FALSE)
          dta_mod[[counter]] <- data.frame(i = i,
                                           method = "Edge Betweenness",
                                           k = ifelse(k == 0, max(as.numeric(mem)), k),
                                           modularity = igraph::modularity(x = g, membership = mem),
                                           stringsAsFactors = FALSE)
          if (isTRUE(saveObjects)) {
            current_cl[[counter_current]] <- cl
            current_mod[counter_current] <- dta_mod[[counter]]$modularity[nrow(dta_mod[[counter]])]
            counter_current <- counter_current + 1
          }
          counter <- counter + 1
        }, silent = TRUE)
      }

      # Infomap community detection
      if (isTRUE(infomap) && k < 2) {
        try({
          suppressWarnings(cl <- igraph::cluster_infomap(g))
          mem <- igraph::membership(cl)
          dta_mem[[counter]] <- data.frame(i = rep(i, length(mem)),
                                           method = rep("Infomap", length(mem)),
                                           node = rownames(x),
                                           cluster = as.numeric(mem),
                                           stringsAsFactors = FALSE)
          dta_mod[[counter]] <- data.frame(i = i,
                                           method = "Infomap",
                                           k = max(as.numeric(mem)),
                                           modularity = igraph::modularity(x = g, membership = mem),
                                           stringsAsFactors = FALSE)
          if (isTRUE(saveObjects)) {
            current_cl[[counter_current]] <- cl
            current_mod[counter_current] <- dta_mod[[counter]]$modularity[nrow(dta_mod[[counter]])]
            counter_current <- counter_current + 1
          }
          counter <- counter + 1
        }, silent = TRUE)
      }

      # Label Propagation community detection
      if (isTRUE(label_prop) && k < 2) {
        try({
          suppressWarnings(cl <- igraph::cluster_label_prop(g))
          mem <- igraph::membership(cl)
          dta_mem[[counter]] <- data.frame(i = rep(i, length(mem)),
                                           method = rep("Label Propagation", length(mem)),
                                           node = rownames(x),
                                           cluster = as.numeric(mem),
                                           stringsAsFactors = FALSE)
          dta_mod[[counter]] <- data.frame(i = i,
                                           method = "Label Propagation",
                                           k = max(as.numeric(mem)),
                                           modularity = igraph::modularity(x = g, membership = mem),
                                           stringsAsFactors = FALSE)
          if (isTRUE(saveObjects)) {
            current_cl[[counter_current]] <- cl
            current_mod[counter_current] <- dta_mod[[counter]]$modularity[nrow(dta_mod[[counter]])]
            counter_current <- counter_current + 1
          }
          counter <- counter + 1
        }, silent = TRUE)
      }

      # Spinglass community detection
      if (isTRUE(spinglass) && k < 2) {
        try({
          suppressWarnings(cl <- igraph::cluster_spinglass(g))
          mem <- igraph::membership(cl)
          dta_mem[[counter]] <- data.frame(i = rep(i, length(mem)),
                                           method = rep("Spinglass", length(mem)),
                                           node = rownames(x),
                                           cluster = as.numeric(mem),
                                           stringsAsFactors = FALSE)
          dta_mod[[counter]] <- data.frame(i = i,
                                           method = "Spinglass",
                                           k = max(as.numeric(mem)),
                                           modularity = igraph::modularity(x = g, membership = mem),
                                           stringsAsFactors = FALSE)
          if (isTRUE(saveObjects)) {
            current_cl[[counter_current]] <- cl
            current_mod[counter_current] <- dta_mod[[counter]]$modularity[nrow(dta_mod[[counter]])]
            counter_current <- counter_current + 1
          }
          counter <- counter + 1
        }, silent = TRUE)
      }

      # retain cluster object where modularity was maximal
      if (isTRUE(saveObjects) && length(current_cl) > 0) {
        obj$cl[[i]] <- current_cl[[which.max(current_mod)]]
      }
    }
  }
  if (isTRUE(verbose)) {
    if (timewindow == "no") {
      message("done.")
    } else {
      message("\nDone.")
    }
    message("(3/3) Post-processing data... ", appendLF = FALSE)
  }
  obj$cl <- obj$cl[!sapply(obj$cl, is.null)] # remove NULL objects that may occur when the network is empty
  obj$k <- k
  obj$max_mod <- dplyr::bind_rows(dta_dat)
  obj$memberships <- dplyr::bind_rows(dta_mem)
  obj$modularity <- dplyr::bind_rows(dta_mod)
  if (nrow(obj$modularity) == 0) {
    stop("No output rows. Either you switched all clustering methods off, or all methods you used produced errors.")
  }
  obj$max_mod <- obj$max_mod[obj$max_mod$i %in% obj$modularity$i, ] # remove date entries where the network is empty
  obj$max_mod$max_mod <- sapply(obj$max_mod$i, function(x) max(obj$modularity$modularity[obj$modularity$i == x], na.rm = TRUE)) # attach max_mod to $max_mod
  # attach max_method to $max_mod
  obj$max_mod$max_method <- sapply(obj$max_mod$i,
                                   function(x) obj$modularity$method[obj$modularity$i == x & obj$modularity$modularity == max(obj$modularity$modularity[obj$modularity$i == x], na.rm = TRUE)][1])
  # attach k to max_mod
  obj$max_mod$k <- sapply(obj$max_mod$i, function(x) max(obj$modularity$k[obj$modularity$i == x], na.rm = TRUE))

  # diagnostics
  if (isTRUE(single) && !"Hierarchical (Single)" %in% obj$modularity$method && k > 1) {
    warning("'single' omitted due to an unknown problem.")
  }
  if (isTRUE(average) && !"Hierarchical (Average)" %in% obj$modularity$method && k > 1) {
    warning("'average' omitted due to an unknown problem.")
  }
  if (isTRUE(complete) && !"Hierarchical (Complete)" %in% obj$modularity$method && k > 1) {
    warning("'complete' omitted due to an unknown problem.")
  }
  if (isTRUE(ward) && !"Hierarchical (Ward)" %in% obj$modularity$method) {
    warning("'ward' omitted due to an unknown problem.")
  }
  if (isTRUE(kmeans) && !"k-Means" %in% obj$modularity$method) {
    warning("'kmeans' omitted due to an unknown problem.")
  }
  if (isTRUE(pam) && !"Partitioning around Medoids" %in% obj$modularity$method) {
    warning("'pam' omitted due to an unknown problem.")
  }
  if (isTRUE(equivalence) && !"Equivalence" %in% obj$modularity$method) {
    warning("'equivalence' omitted due to an unknown problem.")
  }
  if (isTRUE(concor_one) && !"CONCOR (One-Mode)" %in% obj$modularity$method && k %in% c(0, 2)) {
    warning("'concor_one' omitted due to an unknown problem.")
  }
  if (isTRUE(concor_two) && !"CONCOR (Two-Mode)" %in% obj$modularity$method && k %in% c(0, 2)) {
    warning("'concor_two' omitted due to an unknown problem.")
  }
  if (isTRUE(louvain) && !"Louvain" %in% obj$modularity$method && k < 2) {
    warning("'louvain' omitted due to an unknown problem.")
  }
  if (isTRUE(fastgreedy) && !"Fast & Greedy" %in% obj$modularity$method) {
    warning("'fastgreedy' omitted due to an unknown problem.")
  }
  if (isTRUE(walktrap) && !"Walktrap" %in% obj$modularity$method) {
    warning("'walktrap' omitted due to an unknown problem.")
  }
  if (isTRUE(leading_eigen) && !"Leading Eigenvector" %in% obj$modularity$method && k < 2) {
    warning("'leading_eigen' omitted due to an unknown problem.")
  }
  if (isTRUE(edge_betweenness) && !"Edge Betweenness" %in% obj$modularity$method) {
    warning("'edge_betweenness' omitted due to an unknown problem.")
  }
  if (isTRUE(infomap) && !"Infomap" %in% obj$modularity$method && k < 2) {
    warning("'infomap' omitted due to an unknown problem.")
  }
  if (isTRUE(label_prop) && !"Label Propagation" %in% obj$modularity$method && k < 2) {
    warning("'label_prop' omitted due to an unknown problem.")
  }
  if (isTRUE(spinglass) && !"Spinglass" %in% obj$modularity$method && k < 2) {
    warning("'spinglass' omitted due to an unknown problem.")
  }

  class(obj) <- "dna_multiclust"
  if (isTRUE(verbose)) {
    message("done.")
  }
  return(obj)
}

#' Print the summary of a \code{dna_multiclust} object
#'
#' Show details of a \code{dna_multiclust} object.
#'
#' Print abbreviated contents for the slots of a \code{dna_multiclust} object,
#' which can be created using the \link{dna_multiclust} function.
#'
#' @param x A \code{dna_multiclust} object.
#' @param ... Further options (currently not used).
#'
#' @author Philip Leifeld
#' @seealso \code{\link{dna_multiclust}}, \code{\link{dna_plotModularity}}
#' @export
print.dna_multiclust <- function(x, ...) {
  cat(paste0("$k\n", x$k, "\n"))
  if ("cl" %in% names(x)) {
    cat(paste0("\n$cl\n", length(x$cl), " cluster object(s) embedded.\n"))
  }
  cat("\n$max_mod\n")
  print(head(x$max_mod))
  if (nrow(x$max_mod) > 6) {
    cat(paste0("[... ", nrow(x$max_mod), " rows]\n"))
  }
  cat("\n$modularity\n")
  print(head(x$modularity))
  if (nrow(x$modularity) > 6) {
    cat(paste0("[... ", nrow(x$modularity), " rows]\n"))
  }
  cat("\n$memberships\n")
  print(head(x$memberships))
  if (nrow(x$memberships) > 6) {
    cat(paste0("[... ", nrow(x$memberships), " rows]\n"))
  }
}


#' One-dimensional binary scaling from a DNA connection
#'
#' Scale ideological positions of two variables (e.g., organizations and
#' concepts) from a DNA connection by using Markov Chain Monte Carlo for binary
#' one-dimensional Item Response Theory. This is one of the four scaling
#' functions. For one-dimensional ordinal scaling, see \link{dna_scale1dord},
#' for two-dimensional binary scaling, see \link{dna_scale2dbin} and for
#' two-dimensional ordinal scaling \link{dna_scale2dord}.
#'
#' This function is a convenience wrapper for the \link[MCMCpack]{MCMCirt1d}
#' function. Using Markov Chain Monte Carlo (MCMC), \code{dna_scale1dbin}
#' generates a sample from the posterior distribution using standard Gibbs
#' sampling. For the model form and further help for the scaling arguments, see
#' \link[MCMCpack]{MCMCirt1d}.
#'
#' As in a two-mode network in \link{dna_network}, two variables have to be
#' provided for the scaling. The first variable corresponds to the rows of a
#' two-mode network and usually entails actors (e.g., \code{"organizations"}),
#' while the second variable is equal to the columns of a two-mode network,
#' typically expressed by \code{"concepts"}. The \code{dna_scale} functions
#' use \code{"actors"} and \code{"concepts"} as synonyms for \code{variable1}
#' and \code{variable2}. However, the scaling is not restricted to
#' \code{"actors"} and \code{"concepts"} but depends on what you provide in
#' \code{variable1} or \code{variable2}.
#'
#' For a binary qualifier, \code{dna_scale1dbin} internally uses the
#' \code{combine} qualifier aggregation and then recodes the values into
#' \code{0} for disagreement, \code{1} for agreement and \code{NA} for mixed
#' positions and non-mentions of concepts. Integer qualifiers are also recoded
#' into \code{0} and \code{1} by rescaling the qualifier values between
#' \code{0} and \code{1}. You can further relax the recoding of \code{NA} values by setting a
#' \code{threshold} which lets you decide at which percentage of agreement and
#' disagreement an actor position on a concept can be considered as
#' agreement/disagreement or mixed position.
#'
#' The argument \code{drop_min_actors} excludes actors with only a limited
#' number of concepts used. Limited participation of actors in a debate can
#' impact the scaling of the ideal points, as actors with only few mentions of
#' concepts convey limited information on their ideological position. The same
#' can also be done for concepts with the argument \code{drop_min_concepts}.
#' Concepts that have been rarely mentioned do not strongly discriminate the
#' ideological positions of actors and can, therefore, impact the accuracy of
#' the scaling. Reducing the number of actors of concepts to be scaled hence
#' improves the precision of the ideological positions for both variables and
#' the scaling itself. Another possibility to reduce the number of concepts is
#' to use \code{drop_constant_concepts}, which will reduce concepts not having
#' any variation in the agreement/disagreement structure of actors. This means
#' that all concepts will be dropped which have only agreeing or disagreeing
#' statements.
#'
#' As \code{dna_scale1dbin} implements a Bayesian Item Response Theory
#' approach, \code{priors} and \code{starting values} can be set on the actor
#' and concept parameters. Changing the default \code{prior} values can often
#' help you to achieve better results. Constraints on the actor parameters can
#' also be specified to help identifying the model and to indicate in which
#' direction ideological positions of actors and concepts run. The returned
#' MCMC output can also be post-processed by normalizing the samples for each
#' iteration with \code{mcmc_normalize}. Normalization can be a sufficient
#' way of identifying one-dimensional ideal point models.
#'
#' To plot the resulting ideal points of actors and concepts, you can use the
#' \link{dna_plotScale} function. To assess if the returned MCMC chain has
#' converged to its stationary distribution, please use
#' \link{dna_convergenceScale}. The evaluation of convergence is essential to
#' report conclusions based on accurate parameter estimates. Achieving chain
#' convergence often requires setting the iterations of the MCMC chain to
#' several million.
#'
#' @param connection A \code{dna_connection} object created by the
#'   \link{dna_connection} function.
#' @param variable1 The first variable for the scaling construction (see
#'   \link{dna_network}). Defaults to \code{"organization"}.
#' @param variable2 The second variable for the scaling construction (see
#'   \link{dna_network}). Defaults to \code{"concept"}.
#' @param qualifier The qualifier variable for the scaling construction (see
#'   \link{dna_network}). Defaults to \code{"agreement"}.
#' @param threshold Numeric value that specifies when a mixed position can be
#'   considered as agreement or disagreement. If e.g. one actor has 60 percent
#'   of agreeing and 40 percent of disagreeing statements towards a concept, a
#'   \code{threshold} of 0.51 will recode the actor position on this concept as
#'   "agreement". The same accounts also for disagreeing statements. If one
#'   actor has 60 percent of disagreeing and 40 percent of agreeing statements,
#'   a \code{threshold} of 0.51 will recode the actor position on this concept
#'   as "disagreement". All values in between the \code{threshold} (e.g., 55
#'   percent agreement and 45 percent of disagreement and a threshold of 0.6)
#'   will be recoded as \code{NA}. If is set to \code{NULL}, all "mixed"
#'   positions of actors will be recoded as \code{NA}. Must be strictly
#'   positive.
#' @param theta_constraints A list specifying the constraints on the actor
#'   parameter. Three forms of constraints are possible:
#'   \code{actorname = value}, which will constrain an actor to be equal to the
#'   specified value (e.g. \code{0}), \code{actorname = "+"}, which will
#'   constrain the actor to be positively scaled and \code{actorname = "-"},
#'   which will constrain the actor to be negatively scaled (see example).
#' @param mcmc_iterations The number of iterations for the sampler.
#' @param mcmc_burnin The number of burn-in iterations for the sampler.
#' @param mcmc_thin The thinning interval for the sampler. Iterations must be
#'   divisible by the thinning interval.
#' @param mcmc_normalize Logical. Should the MCMC output be normalized? If
#'   \code{TRUE}, samples are normalized to a mean of \code{0} and a standard
#'   deviation of \code{1}.
#' @param theta_start The \code{starting values} for the actor parameters. Can
#'   either be a scalar or a column vector with as many elements as the number
#'   of actors included in the scaling. If set to the default \code{NA},
#'   \code{starting values} will be set according to an eigenvalue-eigenvector
#'   decomposition of the actor agreement score.
#' @param alpha_start The \code{starting values} for the concept difficulty
#'   parameters. Can either be a scalar or a column vector with as many
#'   elements as the number of actors included in the scaling. If set to the
#'   default \code{NA}, \code{starting values} will be set according to a
#'   series of probit regressions that condition the starting values of the
#'   difficulty parameters.
#' @param beta_start The \code{starting values} for the concept discrimination
#'   parameters. Can either be a scalar or a column vector with as many
#'   elements as the number of actors included in the scaling. If set to the
#'   default \code{NA}, \code{starting values} will be set according to a
#'   series of probit regressions that condition the \code{starting values} of
#'   the discrimination parameters.
#' @param theta_prior_mean A scalar value specifying the prior mean of the
#'   actor parameters.
#' @param theta_prior_variance A scalar value specifying the prior inverse
#'   variances of the actor parameters.
#' @param alpha_beta_prior_mean Mean of the difficulty and discrimination
#'   parameters. Can either be a scalar or a 2-vector. If a scalar, both means
#'   will be set according to the specified value.
#' @param alpha_beta_prior_variance Inverse variance of the difficulty and
#'   discrimination parameters. Can either be a scalar or a 2-vector. If a
#'   scalar, both means will be set according to the specified value.
#' @param store_variables A character vector indicating which variables should
#'   be stored from the scaling. Can either take the value of the character
#'   vector indicated in \code{variable1} or \code{variable2} or \code{"both"}
#'   to store both variables. Note that saving both variables can impact the
#'   speed of the scaling. Defaults to \code{"both"}.
#' @param drop_constant_concepts Logical. Should concepts that have no
#'   variation be deleted before the scaling? Defaults to \code{FALSE}.
#' @param drop_min_actors A numeric value specifying the minimum number of
#'   concepts actors should have mentioned to be included in the scaling.
#'   Defaults to \code{1}.
#' @param drop_min_concepts A numeric value specifying the minimum number a
#'   concept should have been jointly mentioned by actors. Defaults to \code{2}.
#' @param verbose A boolean or numeric value indicating whether the iterations
#'   of the scaling should be printed to the R console. If set to a numeric
#'   value, every \code{verboseth} iteration will be printed. If set to
#'   \code{TRUE}, \code{verbose} will print the total of iterations and burn-in
#'   divided by \code{10}.
#' @param seed The random seed for the scaling.
#' @param ... Additional arguments passed to \link{dna_network}. Actors can
#'   e.g. be removed with the \code{excludeValues} arguments. The scaling can
#'   also be applied to a specific time slice by using \code{start.date} and
#'   \code{stop.date}.
#'
#' @examples
#' dna_init()
#' conn <- dna_connection(dna_sample())
#' dna_scale <- dna_scale1dbin(
#'   conn,
#'   variable1 = "organization",
#'   variable2 = "concept",
#'   qualifier = "agreement",
#'   threshold = 0.51,
#'   theta_constraints = list(
#'     `National Petrochemical & Refiners Association` = "+",
#'     `Alliance to Save Energy` = "-"),
#'   mcmc_iterations = 20000,
#'   mcmc_burnin = 2000,
#'   mcmc_thin = 10,
#'   mcmc_normalize = TRUE,
#'   theta_prior_mean = 0,
#'   theta_prior_variance = 1,
#'   alpha_beta_prior_mean = 0,
#'   alpha_beta_prior_variance = 0.25,
#'   store_variables = "both",
#'   drop_constant_concepts = FALSE,
#'   drop_min_actors = 1,
#'   verbose = TRUE,
#'   seed = 12345
#' )
#'
#' @author Tim Henrichsen, Johannes B. Gruber
#' @export
#' @importFrom MCMCpack MCMCirt1d
#' @importFrom scales rescale
#' @importFrom coda as.mcmc
dna_scale1dbin <- function(connection,
                           variable1 = "organization",
                           variable2 = "concept",
                           qualifier = "agreement",
                           threshold = NULL,
                           theta_constraints = NULL,
                           mcmc_iterations = 20000,
                           mcmc_burnin = 1000,
                           mcmc_thin = 10,
                           mcmc_normalize = FALSE,
                           theta_start = NA,
                           alpha_start = NA,
                           beta_start = NA,
                           theta_prior_mean = 0,
                           theta_prior_variance = 1,
                           alpha_beta_prior_mean = 0,
                           alpha_beta_prior_variance = 0.25,
                           store_variables = "both",
                           drop_constant_concepts = FALSE,
                           drop_min_actors = 1,
                           drop_min_concepts = 2,
                           verbose = TRUE,
                           seed = 12345,
                           ...) {
  dots <- list(...)
  out <- bin_recode(connection = connection,
                    variable1 = variable1,
                    variable2 = variable2,
                    qualifier = qualifier,
                    threshold = threshold,
                    drop_min_actors = drop_min_actors,
                    drop_min_concepts = drop_min_concepts,
                    store_variables = store_variables,
                    dots = dots)
  nw2 <- out$nw2
  dots <- out$dots
  dots_nw <- out$dots_nw
  invertValues <- out$invertValues
  excludeValues <- out$excludeValues
  # Scaling
  x <- do.call(eval(parse(text = "MCMCpack::MCMCirt1d")), c(list(
    nw2,
    theta.constraints = theta_constraints,
    burnin = mcmc_burnin,
    mcmc = mcmc_iterations,
    thin = mcmc_thin,
    verbose = ifelse(verbose == TRUE,
                     ((mcmc_iterations + mcmc_burnin) / 10),
                     verbose),
    seed = seed,
    theta.start = theta_start,
    alpha.start = alpha_start,
    beta.start = beta_start,
    t0 = theta_prior_mean,
    T0 = theta_prior_variance,
    ab0 = alpha_beta_prior_mean,
    AB0 = alpha_beta_prior_variance,
    store.item = (store_variables == variable2 | store_variables == "both"),
    store.ability = (store_variables == variable1 | store_variables == "both"),
    drop.constant.items = drop_constant_concepts),
    dots))
  if (mcmc_normalize) {
    names <- colnames(x)
    x <- coda::as.mcmc(t(apply(x, 1, scale)))
    colnames(x) <- names
  }
  dna_scale <- list()
  dna_scale$sample <- x
  # Store actor frequency for possible min argument in dna_plotScale
  nw_freq <- do.call("dna_network", c(list(connection = connection,
                                           networkType = "twomode",
                                           variable1 = variable1,
                                           variable2 = variable2,
                                           qualifier = qualifier,
                                           qualifierAggregation = "ignore",
                                           verbose = FALSE,
                                           excludeValues = excludeValues,
                                           invertValues = invertValues),
                                      dots_nw))
  if (store_variables == variable1 | store_variables == "both") {
    actors <- x[, grepl("^theta.", colnames(x))]
    hpd <- as.data.frame(coda::HPDinterval(actors, prob = 0.95))
    actors <- as.data.frame(colMeans(actors))
    actors <- merge(actors, hpd, by = 0)
    colnames(actors)[colnames(actors) == "colMeans(actors)"] <- "mean"
    colnames(actors)[colnames(actors) == "lower"] <- "HPD2.5"
    colnames(actors)[colnames(actors) == "upper"] <- "HPD97.5"
    actors$Row.names <- gsub("^theta.", "", actors$Row.names)
    at <- dna_getAttributes(connection = connection,
                            variable = variable1,
                            values = actors$Row.names)
    at$frequency <- rowSums(nw_freq)[match(at$value, rownames(nw_freq))]
    actors <- merge(actors, at, by.x = "Row.names", by.y = "value")
    actors <- actors[, !(colnames(actors) == "id")]
    actors$variable <- "actor"
  }
  if (store_variables == variable2 | store_variables == "both") {
    concepts <- x[, grepl("^beta.", colnames(x))]
    hpd <- as.data.frame(coda::HPDinterval(concepts, prob = 0.95))
    concepts <- as.data.frame(colMeans(concepts))
    concepts <- merge(concepts, hpd, by = 0)
    colnames(concepts)[colnames(concepts) == "colMeans(concepts)"] <- "mean"
    colnames(concepts)[colnames(concepts) == "lower"] <- "HPD2.5"
    colnames(concepts)[colnames(concepts) == "upper"] <- "HPD97.5"
    concepts$Row.names <- gsub("^beta.", "", concepts$Row.names)
    at <- dna_getAttributes(connection = connection,
                            variable = variable2,
                            values = concepts$Row.names)
    concepts <- merge(concepts, at, by.x = "Row.names", by.y = "value")
    concepts <- concepts[, !(colnames(concepts) == "id")]
    concepts$variable <- "concept"
  }
  if (store_variables == "both") {
    dna_scale$attributes <- rbind(actors, concepts)
  } else if (store_variables == variable1) {
    dna_scale$attributes <- actors
  } else if (store_variables == variable2){
    dna_scale$attributes <- concepts
  }
  dna_scale$call <- mget(names(formals()), sys.frame(sys.nframe()))
  dna_scale$call$connection <- NULL
  class(dna_scale) <- c("dna_scale1dbin", class(dna_scale))
  class(dna_scale) <- c("dna_scale", class(dna_scale))
  return(dna_scale)
}


#' One-dimensional ordinal scaling from a DNA connection.
#'
#' Scale ideological positions of two variables (e.g., organizations and
#' concepts) from a DNA connection by using Markov Chain Monte Carlo for
#' ordinal one-dimensional Item Response Theory. This is one of the four
#' scaling functions. For one-dimensional binary scaling, see
#' \link{dna_scale1dbin}, for two-dimensional binary scaling, see
#' \link{dna_scale2dbin} and for two-dimensional ordinal scaling
#' \link{dna_scale2dord}.
#'
#' This function is a convenience wrapper for the
#' \link[MCMCpack]{MCMCordfactanal} function. Using Markov Chain Monte Carlo
#' (MCMC), \code{dna_scale1dord} generates a sample from the posterior
#' distribution of an ordinal data factor analysis model, using a
#' Metropolis-Hastings within Gibbs sampling algorithm. For the model form and
#' further help for the scaling arguments, see \link[MCMCpack]{MCMCordfactanal}.
#'
#' As in a two-mode network in \link{dna_network}, two variables have to be
#' provided for the scaling. The first variable corresponds to the rows of a
#' two-mode network and usually entails actors (e.g., \code{"organizations"}),
#' while the second variable is equal to the columns of a two-mode network,
#' typically expressed by \code{"concepts"}. The \code{dna_scale} functions
#' use \code{"actors"} and \code{"concepts"} as synonyms for \code{variable1}
#' and \code{variable2}. However, the scaling is not restricted to
#' \code{"actors"} and \code{"concepts"} but depends on what you provide in
#' \code{variable1} or \code{variable2}.
#'
#' \code{dna_scale1dord} internally uses the \code{combine} qualifier
#' aggregation and then recodes the values into \code{1} for disagreement,
#' \code{2} for mixed positions and \code{3} for agreement. Integer qualifiers
#' are not recoded. When \code{zero_is_na} is set to \code{TRUE}, non-mentions
#' of concepts are set to \code{NA}, while setting the argument to \code{FALSE}
#' recodes them to \code{2} as mixed position. By setting a \code{threshold},
#' you can further decide at which percentage of agreement and disagreement
#' an actor position on a concept can be considered as agreement/disagreement
#' or mixed position.
#'
#' The argument \code{drop_min_actors} excludes actors with only a limited
#' number of concepts used. Limited participation of actors in a debate can
#' impact the scaling of the ideal points, as actors with only few mentions of
#' concepts convey limited information on their ideological position. The same
#' can also be done for concepts with the argument \code{drop_min_concepts}.
#' Concepts that have been rarely mentioned do not strongly discriminate the
#' ideological positions of actors and can, therefore, impact the accuracy of the
#' scaling. Reducing the number of actors of concepts to be scaled hence
#' improves the precision of the ideological positions for both variables and
#' the scaling itself. Another possibility to reduce the number of concepts is
#' to use \code{drop_constant_concepts}, which will reduce concepts not having
#' any variation in the agreement/disagreement structure of actors. This means
#' that all concepts will be dropped which have only agreeing, disagreeing or
#' mixed statements.
#'
#' As \code{dna_scale1dord} implements a Bayesian Item Response Theory
#' approach, \code{priors} and \code{starting values} can be set on the concept
#' parameters. Changing the default \code{prior} values can often help you to
#' achieve better results. Constraints on the concept parameters can also be
#' specified to help identifying the model and to indicate in which direction
#' ideological positions of actors and concepts run. The scaling estimates an
#' item discrimination parameter and an item difficulty parameter for each
#' concept. We advise constraining the item discrimination parameter, as the
#' item difficulty parameter, in general, should not be constrained. The
#' returned MCMC output can also be post-processed by normalizing the samples
#' for each iteration with \code{mcmc_normalize}. Normalization can be a
#' sufficient way of identifying one-dimensional ideal point models.
#'
#' To plot the resulting ideal points of actors and concepts, you can use the
#' \link{dna_plotScale} function. To assess if the returned MCMC chain has
#' converged to its stationary distribution, please use
#' \link{dna_convergenceScale}. The evaluation of convergence is essential to
#' report conclusions based on accurate parameter estimates. Achieving chain
#' convergence often requires setting the iterations of the MCMC chain to
#' several million.
#'
#' @param connection A \code{dna_connection} object created by the
#'   \link{dna_connection} function.
#' @param variable1 The first variable for the scaling construction (see
#'   \link{dna_network}). Defaults to \code{"organization"}.
#' @param variable2 The second variable for the scaling construction (see
#'   \link{dna_network}). Defaults to \code{"concept"}.
#' @param qualifier The qualifier variable for the scaling construction (see
#'   \link{dna_network}). Defaults to \code{"agreement"}.
#' @param zero_as_na Logical. If \code{TRUE}, all non-mentions of an actor
#'   towards a concept will be recoded as \code{NA}. If \code{FALSE} as
#'   \code{2}.
#' @param threshold Numeric value that specifies when a mixed position can be
#'   considered as agreement or disagreement. If e.g., one actor has 60 percent
#'   of agreeing and 40 percent of disagreeing statements towards a concept, a
#'   \code{threshold} of 0.51 will recode the actor position on this concept as
#'   "agreement". The same accounts also for disagreeing statements. If one
#'   actor has 60 percent of disagreeing and 40 percent of agreeing statements,
#'   a \code{threshold} of 0.51 will recode the actor position on this concept
#'   as "disagreement". All values in between the \code{threshold} (e.g., 55
#'   percent agreement and 45 percent of disagreement and a threshold of 0.6)
#'   will be recoded as \code{2}. If is set to \code{NULL}, all "mixed"
#'   positions of actors will be recoded as \code{2}. Must be strictly
#'   positive.
#' @param lambda_constraints A list of lists specifying constraints on the
#'   concept parameters. Note that value \code{1} in the brackets of the
#'   argument refers to the negative item difficulty parameters, which in
#'   general should not be constrained. Value \code{2} relates to the item
#'   discrimination parameter and should be used for constraints on concepts.
#'   Three forms of constraints are possible:
#'   \code{conceptname = list(2, value)} will constrain the item discrimination
#'   parameter to be equal to the specified value (e.g., 0).
#'   \code{conceptname = list(2,"+")} will constrain the item discrimination
#'   parameter to be positively scaled and \code{conceptname = list(2, "-")}
#'   will constrain the parameter to be negatively scaled (see example).
#' @param mcmc_iterations The number of iterations for the sampler.
#' @param mcmc_burnin The number of burn-in iterations for the sampler.
#' @param mcmc_thin The thinning interval for the sampler. Iterations must be
#'   divisible by the thinning interval.
#' @param mcmc_tune The tuning parameter for the acceptance rates of the
#'   sampler. Acceptance rates should ideally range between \code{0.15} and
#'   \code{0.5}. Can be either a scalar or a k-vector. Must be strictly
#'   positive.
#' @param mcmc_normalize Logical. Should the MCMC output be normalized? If
#'   \code{TRUE}, samples are normalized to a mean of \code{0} and a standard
#'   deviation of \code{1}.
#' @param lambda_start \code{Starting values} for the concept parameters. Can
#'   be either a scalar or a matrix. If set to \code{NA} (default), the
#'   \code{starting values} for the unconstrained parameters in the first
#'   column are based on the observed response patterns. The remaining
#'   unconstrained elements are set to starting values of either \code{1.0} or
#'   \code{-1.0}, depending on the nature of the constraint.
#' @param lambda_prior_mean The prior mean of the concept parameters. Can be
#'   either a scalar or a matrix.
#' @param lambda_prior_variance The prior inverse variances of the concept
#'   parameters. Can be either a scalar or a matrix.
#' @param store_variables A character vector indicating which variables should
#'   be stored from the scaling. Can either take the value of the character
#'   vector indicated in \code{variable1} or \code{variable2} or \code{"both"}
#'   to store both variables. Note that saving both variables can impact the
#'   speed of the scaling. Defaults to \code{"both"}.
#' @param drop_constant_concepts Logical. Should concepts that have no
#'   variation be deleted before the scaling? Defaults to \code{FALSE}.
#' @param drop_min_actors A numeric value specifying the minimum number of
#'   concepts actors should have mentioned to be included in the scaling.
#'   Defaults to \code{1}.
#' @param drop_min_concepts A numeric value specifying the minimum number a
#'   concept should have been jointly mentioned by actors. Defaults to \code{2}.
#' @param verbose A boolean or numeric value indicating whether the iterations
#'   of the scaling should be printed to the R console. If set to a numeric
#'   value, every \code{verboseth} iteration will be printed. If set to
#'   \code{TRUE}, \code{verbose} will print the total of iterations and burn-in
#'   divided by \code{10}.
#' @param seed The random seed for the scaling.
#' @param ... Additional arguments passed to \link{dna_network}. Actors can
#'   e.g., be removed with the \code{excludeValues} arguments. The scaling can
#'   also be applied to a specific time slice by using \code{start.date} and
#'   \code{stop.date}.
#'
#' @examples
#' dna_init()
#' conn <- dna_connection(dna_sample())
#' dna_scale <- dna_scale1dord(
#'   conn,
#'   variable1 = "organization",
#'   variable2 = "concept",
#'   qualifier = "agreement",
#'   zero_as_na = TRUE,
#'   threshold = 0.6,
#'   lambda_constraints = list(`CO2 legislation will not hurt the economy.` = list(2, "-")),
#'   mcmc_iterations = 20000,
#'   mcmc_burnin = 2000,
#'   mcmc_thin = 10,
#'   mcmc_tune = 1.5,
#'   mcmc_normalize = FALSE,
#'   lambda_prior_mean = 0,
#'   lambda_prior_variance = 0.1,
#'   store_variables = "organization",
#'   drop_constant_concepts = TRUE,
#'   verbose = TRUE,
#'   seed = 12345
#' )
#'
#' @author Tim Henrichsen, Johannes B. Gruber
#' @export
#' @importFrom MCMCpack MCMCordfactanal
#' @importFrom coda as.mcmc
dna_scale1dord <- function(connection,
                           variable1 = "organization",
                           variable2 = "concept",
                           qualifier = "agreement",
                           zero_as_na = TRUE,
                           threshold = NULL,
                           lambda_constraints = NULL,
                           mcmc_iterations = 20000,
                           mcmc_burnin = 1000,
                           mcmc_thin = 10,
                           mcmc_tune = 1.5,
                           mcmc_normalize = FALSE,
                           lambda_start = NA,
                           lambda_prior_mean = 0,
                           lambda_prior_variance = 1,
                           store_variables = "both",
                           drop_constant_concepts = FALSE,
                           drop_min_actors = 1,
                           drop_min_concepts = 2,
                           verbose = TRUE,
                           seed = 12345,
                           ...) {
  dots <- list(...)
  out <- ord_recode(connection = connection,
                    variable1 = variable1,
                    variable2 = variable2,
                    qualifier = qualifier,
                    zero_as_na = zero_as_na,
                    threshold = threshold,
                    drop_min_actors = drop_min_actors,
                    drop_min_concepts = drop_min_concepts,
                    store_variables = store_variables,
                    dots = dots)
  nw2 <- out$nw2
  dots <- out$dots
  dots_nw <- out$dots_nw
  invertValues <- out$invertValues
  excludeValues <- out$excludeValues
  # Scaling
  x <- do.call(eval(parse(text = "MCMCpack::MCMCordfactanal")), c(list(
    nw2,
    factors = 1,
    lambda.constraints = lambda_constraints,
    burnin = mcmc_burnin,
    mcmc = mcmc_iterations,
    thin = mcmc_thin,
    tune = mcmc_tune,
    verbose = ifelse(verbose == TRUE,
                     ((mcmc_iterations + mcmc_burnin) / 10),
                     verbose),
    seed = seed,
    lambda.start = lambda_start,
    l0 = lambda_prior_mean,
    L0 = lambda_prior_variance,
    store.lambda = (store_variables == variable2 | store_variables == "both"),
    store.scores = (store_variables == variable1 | store_variables == "both"),
    drop.constantvars = drop_constant_concepts),
    dots))
  if (mcmc_normalize) {
    names <- colnames(x)
    x <- coda::as.mcmc(t(apply(x, 1, scale)))
    colnames(x) <- names
  }
  dna_scale <- list()
  dna_scale$sample <- x
  # Store actor frequency for possible min argument in dna_plotScale
  nw_freq <- do.call("dna_network", c(list(connection = connection,
                                           networkType = "twomode",
                                           variable1 = variable1,
                                           variable2 = variable2,
                                           qualifier = qualifier,
                                           qualifierAggregation = "ignore",
                                           verbose = FALSE,
                                           excludeValues = excludeValues,
                                           invertValues = invertValues),
                                      dots_nw))
  if (store_variables == variable1 | store_variables == "both") {
    actors <- x[, grepl("^phi.", colnames(x))]
    hpd <- as.data.frame(coda::HPDinterval(actors, prob = 0.95))
    actors <- as.data.frame(colMeans(actors))
    actors <- merge(actors, hpd, by = 0)
    colnames(actors)[colnames(actors) == "colMeans(actors)"] <- "mean"
    colnames(actors)[colnames(actors) == "lower"] <- "HPD2.5"
    colnames(actors)[colnames(actors) == "upper"] <- "HPD97.5"
    actors$Row.names <- gsub("^phi.|.2$", "", actors$Row.names)
    at <- dna_getAttributes(connection = connection,
                            variable = variable1,
                            values = actors$Row.names)
    at$frequency <- rowSums(nw_freq)[match(at$value, rownames(nw_freq))]
    actors <- merge(actors, at, by.x = "Row.names", by.y = "value")
    actors <- actors[, !(colnames(actors) == "id")]
    actors$variable <- "actor"
  }
  if (store_variables == variable2 | store_variables == "both") {
    concepts <- x[, grepl("^Lambda", colnames(x))]
    concepts <- concepts[, grepl(".2$", colnames(concepts))]
    hpd <- as.data.frame(coda::HPDinterval(concepts, prob = 0.95))
    concepts <- as.data.frame(colMeans(concepts))
    concepts <- merge(concepts, hpd, by = 0)
    colnames(concepts)[colnames(concepts) == "colMeans(concepts)"] <- "mean"
    colnames(concepts)[colnames(concepts) == "lower"] <- "HPD2.5"
    colnames(concepts)[colnames(concepts) == "upper"] <- "HPD97.5"
    concepts$Row.names <- gsub("^Lambda|.2$", "", concepts$Row.names)
    at <- dna_getAttributes(connection = connection,
                            variable = variable2,
                            values = concepts$Row.names)
    concepts <- merge(concepts, at, by.x = "Row.names", by.y = "value")
    concepts <- concepts[, !(colnames(concepts) == "id")]
    concepts$variable <- "concept"
  }
  if (store_variables == "both") {
    dna_scale$attributes <- rbind(actors, concepts)
  } else if (store_variables == variable1) {
    dna_scale$attributes <- actors
  } else if (store_variables == variable2){
    dna_scale$attributes <- concepts
  }
  dna_scale$call <- mget(names(formals()), sys.frame(sys.nframe()))
  dna_scale$call$connection <- NULL
  class(dna_scale) <- c("dna_scale1dord", class(dna_scale))
  class(dna_scale) <- c("dna_scale", class(dna_scale))
  return(dna_scale)
}


#' Two-dimensional binary scaling from a DNA connection
#'
#' Scale ideological positions of two variables (e.g., organizations and
#' concepts) from a DNA connection by using Markov Chain Monte Carlo for binary
#' two-dimensional Item Response Theory. This is one of the four scaling
#' functions. For one-dimensional binary scaling, see \link{dna_scale1dbin},
#' for one-dimensional ordinal scaling, see \link{dna_scale1dord} and for
#' two-dimensional ordinal scaling \link{dna_scale2dord}.
#'
#' This function is a convenience wrapper for the \link[MCMCpack]{MCMCirtKd}
#' function. Using Markov Chain Monte Carlo (MCMC), \code{dna_scale2dbin}
#' generates a sample from the posterior distribution using standard Gibbs
#' sampling. For the model form and further help for the scaling arguments, see
#' \link[MCMCpack]{MCMCirtKd}.
#'
#' As in a two-mode network in \link{dna_network}, two variables have to be
#' provided for the scaling. The first variable corresponds to the rows of a
#' two-mode network and usually entails actors (e.g., \code{"organizations"}),
#' while the second variable is equal to the columns of a two-mode network,
#' typically expressed by \code{"concepts"}. The \code{dna_scale} functions
#' use \code{"actors"} and \code{"concepts"} as synonyms for \code{variable1}
#' and \code{variable2}. However, the scaling is not restricted to
#' \code{"actors"} and \code{"concepts"} but depends on what you provide in
#' \code{variable1} or \code{variable2}.
#'
#' \code{dna_scale2dbin} internally uses the \code{combine} qualifier
#' aggregation and then recodes the values into \code{0} for disagreement,
#' \code{1} for agreement and \code{NA} for mixed positions and non-mentions of
#' concepts. Integer qualifiers are also recoded into \code{0} and \code{1} by
#' rescaling the qualifier values between \code{0} and \code{1}. You can
#' further relax the recoding of \code{NA} values by setting a \code{threshold}
#' which lets you decide at which percentage of agreement and disagreement
#' an actor position on a concept can be considered as agreement/disagreement
#' or mixed position.
#'
#' The argument \code{drop_min_actors} excludes actors with only a limited
#' number of concepts used. Limited participation of actors in a debate can
#' impact the scaling of the ideal points, as actors with only few mentions of
#' concepts convey limited information on their ideological position. The same
#' can also be done for concepts with the argument \code{drop_min_concepts}.
#' Concepts that have been rarely mentioned do not strongly discriminate the
#' ideological positions of actors and can, therefore, impact the accuracy of
#' the scaling. Reducing the number of actors of concepts to be scaled hence
#' improves the precision of the ideological positions for both variables and
#' the scaling itself. Another possibility to reduce the number of concepts is
#' to use \code{drop_constant_concepts}, which will reduce concepts not having
#' any variation in the agreement/disagreement structure of actors. This means
#' that all concepts will be dropped which have only agreeing, disagreeing or
#' mixed statements.
#'
#' As \code{dna_scale2dbin} implements a Bayesian Item Response Theory
#' approach, \code{priors} and \code{starting values} can be set on the concept
#' parameters. Changing the default \code{prior} values can often
#' help you to achieve better results. Constraints on the parameters can also
#' be specified to help identifying the model and to indicate in which
#' direction ideological positions of actors and concepts run. Please note
#' that, unlike \link{dna_scale1dbin}, this function constrains the values
#' indicated in \code{variable2}. For these values, the scaling estimates an
#' item discrimination parameter for each dimension and an item difficulty
#' parameter for both dimensions. The item difficulty parameter should,
#' however, not be constrained (see \link[MCMCpack]{MCMCirtKd}). Therefore, you
#' should set constraints on the item discrimination parameters.
#'
#' Fitting two-dimensional scaling models requires a good choice of concept
#' constraints to specify the ideological dimensions of your data. A suitable
#' way of identifying your ideological dimensions is to constrain one item
#' discrimination parameter to load only on one dimension. This means that we
#' set one parameter to load either positive or negative on one dimension and
#' setting it to zero on the other. A second concept should also be constrained
#' to load either positive or negative on one dimension (see example)
#'
#' To plot the resulting ideal points of actors and concepts, you can use the
#' \link{dna_plotScale} function. To assess if the returned MCMC chain has
#' converged to its stationary distribution, please use
#' \link{dna_convergenceScale}. The evaluation of convergence is essential to
#' report conclusions based on accurate parameter estimates. Achieving chain
#' convergence often requires setting the iterations of the MCMC chain to
#' several million.
#'
#' @param connection A \code{dna_connection} object created by the
#'   \link{dna_connection} function.
#' @param variable1 The first variable for the scaling construction (see
#'   \link{dna_network}). Defaults to \code{"organization"}.
#' @param variable2 The second variable for the scaling construction (see
#'   \link{dna_network}). Defaults to \code{"concept"}.
#' @param qualifier The qualifier variable for the scaling construction (see
#'   \link{dna_network}). Defaults to \code{"agreement"}.
#' @param threshold Numeric value that specifies when a mixed position can be
#'   considered as agreement or disagreement. If e.g., one actor has 60 percent
#'   of agreeing and 40 percent of disagreeing statements towards a concept, a
#'   \code{threshold} of 0.51 will recode the actor position on this concept as
#'   "agreement". The same accounts also for disagreeing statements. If one
#'   actor has 60 percent of disagreeing and 40 percent of agreeing statements,
#'   a \code{threshold} of 0.51 will recode the actor position on this concept
#'   as "disagreement". All values in between the \code{threshold} (e.g., 55
#'   percent agreement and 45 percent of disagreement and a threshold of 0.6)
#'   will be recoded as \code{NA}. If is set to \code{NULL}, all "mixed"
#'   positions of actors will be recoded as \code{NA}. Must be strictly
#'   positive.
#' @param item_constraints A list of lists specifying constraints on the
#'   concept parameters. Note that value \code{1} in the brackets of the
#'   argument refers to the item difficulty parameters, which in
#'   general should not be constrained. All values above \code{1} relate to the
#'   item discrimination parameters on the single dimensions. These should be
#'   used for constraints on concepts. Three forms of constraints are
#'   possible: \code{conceptname = list(2, value)} will constrain a concept to
#'   be equal to the specified value (e.g., 0) on the first dimension of the
#'   item discrimination parameter. \code{conceptname = list(2,"+")} will
#'   constrain the concept to be positively scaled on the first dimension and
#'   \code{conceptname = list(2, "-")} will constrain the concept to be
#'   negatively scaled on the first dimension (see example). If you
#'   wish to constrain a concept on the second dimension, please indicate this
#'   with a \code{3} in the first position in the bracket.
#' @param mcmc_iterations The number of iterations for the sampler.
#' @param mcmc_burnin The number of burn-in iterations for the sampler.
#' @param mcmc_thin The thinning interval for the sampler. Iterations
#'   must be divisible by the thinning interval.
#' @param alpha_beta_start \code{Starting values} for the item difficulty and
#'   discrimination parameters. Can be either a scalar or a matrix. If set to
#'   \code{NA}, the \code{starting values} for the unconstrained concepts are
#'   set to values generated from a series of proportional odds logistic
#'   regression fits and \code{starting values} for inequality constrained
#'   elements are set to either \code{1.0} or \code{-1.0}, depending on the
#'   nature of the constraints.
#' @param alpha_beta_prior_mean The prior means for the item difficulty and
#'   discrimination parameters.
#' @param alpha_beta_prior_variance The inverse variances of the item
#'   difficulty and discrimination parameters. Can either be a scalar or a
#'   matrix of two dimensions times the concepts.
#' @param store_variables A character vector indicating which variables should
#'   be stored from the scaling. Can either take the value of the character
#'   vector indicated in \code{variable1} or \code{variable2} or \code{"both"}
#'   to store both variables. Note that saving both variables can impact the
#'   speed of the scaling. Defaults to \code{"both"}.
#' @param drop_constant_concepts Logical. Should concepts that have no
#'   variation be deleted before the scaling? Defaults to \code{FALSE}.
#' @param drop_min_actors A numeric value specifying the minimum number of
#'   concepts actors should have mentioned to be included in the scaling.
#'   Defaults to \code{1}.
#' @param drop_min_concepts A numeric value specifying the minimum number a
#'   concept should have been jointly mentioned by actors. Defaults to \code{2}.
#' @param verbose A boolean or numeric value indicating whether the iterations
#'   of the scaling should be printed to the R console. If set to a numeric
#'   value, every \code{verboseth} iteration will be printed. If set to
#'   \code{TRUE}, \code{verbose} will print the total of iterations and burn-in
#'   divided by \code{10}.
#' @param seed The random seed for the scaling.
#' @param ... Additional arguments passed to \link{dna_network}. Actors can
#'   e.g., be removed with the \code{excludeValues} arguments. The scaling can
#'   also be applied to a specific time slice by using \code{start.date} and
#'   \code{stop.date}.
#'
#' @examples
#' dna_init()
#' conn <- dna_connection(dna_sample())
#' dna_scale <- dna_scale2dbin(
#'   conn,
#'   variable1 = "organization",
#'   variable2 = "concept",
#'   qualifier = "agreement",
#'   threshold = 0.6,
#'   item_constraints = list(
#'     `Climate change is caused by greenhouse gases (CO2).` = list(2, "-"),
#'     `Climate change is caused by greenhouse gases (CO2).` = c(3, 0),
#'     `CO2 legislation will not hurt the economy.` = list(3, "-")),
#'   mcmc_iterations = 20000,
#'   mcmc_burnin = 2000,
#'   mcmc_thin = 10,
#'   alpha_beta_prior_mean = 0,
#'   alpha_beta_prior_variance = 1,
#'   store_variables = "organization",
#'   drop_constant_concepts = TRUE,
#'   verbose = TRUE,
#'   seed = 12345
#' )
#'
#' @author Tim Henrichsen, Johannes B. Gruber
#' @export
#' @importFrom MCMCpack MCMCirtKd
#' @importFrom scales rescale
dna_scale2dbin <- function(connection,
                           variable1 = "organization",
                           variable2 = "concept",
                           qualifier = "agreement",
                           threshold = NULL,
                           item_constraints = NULL,
                           mcmc_iterations = 20000,
                           mcmc_burnin = 1000,
                           mcmc_thin = 10,
                           alpha_beta_start = NA,
                           alpha_beta_prior_mean = 0,
                           alpha_beta_prior_variance = 0.1,
                           store_variables = "both",
                           drop_constant_concepts = FALSE,
                           drop_min_actors = 1,
                           drop_min_concepts = 2,
                           verbose = TRUE,
                           seed = 12345,
                           ...) {
  dots <- list(...)
  out <- bin_recode(connection = connection,
                    dots = dots,
                    variable1 = variable1,
                    variable2 = variable2,
                    qualifier = qualifier,
                    threshold = threshold,
                    drop_min_actors = drop_min_actors,
                    drop_min_concepts = drop_min_concepts,
                    store_variables = store_variables)
  nw2 <- out$nw2
  dots <- out$dots
  dots_nw <- out$dots_nw
  invertValues <- out$invertValues
  excludeValues <- out$excludeValues
  # Scaling
  x <- do.call(eval(parse(text = "MCMCpack::MCMCirtKd")), c(list(
    nw2,
    dimensions = 2,
    item.constraints = item_constraints,
    burnin = mcmc_burnin,
    mcmc = mcmc_iterations,
    thin = mcmc_thin,
    verbose = ifelse(verbose == TRUE,
                     ((mcmc_iterations + mcmc_burnin) / 10),
                     verbose),
    seed = seed,
    alphabeta.start = alpha_beta_start,
    b0 = alpha_beta_prior_mean,
    B0 = alpha_beta_prior_variance,
    store.item = (store_variables == variable2 | store_variables == "both"),
    store.ability = (store_variables == variable1 | store_variables == "both"),
    drop.constant.items = drop_constant_concepts),
    dots))
  dna_scale <- list()
  dna_scale$sample <- x
  # Store actor frequency for possible min argument in dna_plotScale
  nw_freq <- do.call("dna_network", c(list(connection = connection,
                                           networkType = "twomode",
                                           variable1 = variable1,
                                           variable2 = variable2,
                                           qualifier = qualifier,
                                           qualifierAggregation = "ignore",
                                           verbose = FALSE,
                                           excludeValues = excludeValues,
                                           invertValues = invertValues),
                                      dots_nw))
  if (store_variables == variable1 | store_variables == "both") {
    actors <- x[, grepl("^theta.", colnames(x))]
    hpd <- as.data.frame(coda::HPDinterval(actors, prob = 0.95))
    actors <- as.data.frame(colMeans(actors))
    actors <- merge(actors, hpd, by = 0)
    colnames(actors)[colnames(actors) == "colMeans(actors)"] <- "mean"
    colnames(actors)[colnames(actors) == "lower"] <- "HPD2.5"
    colnames(actors)[colnames(actors) == "upper"] <- "HPD97.5"
    actors1 <- actors[grepl(".1$", actors$Row.names), drop = FALSE, ]
    actors1$Row.names <- gsub("^theta.|.1$", "", actors1$Row.names)
    actors2 <- actors[grepl(".2$", actors$Row.names), drop = FALSE, ]
    actors2$Row.names <- gsub("^theta.|.2$", "", actors2$Row.names)
    actors <- merge(actors1,
                    actors2,
                    by = "Row.names",
                    suffixes = c("_dim1", "_dim2"))
    at <- dna_getAttributes(connection = connection,
                            variable = variable1,
                            values = actors$Row.names)
    at$frequency <- rowSums(nw_freq)[match(at$value, rownames(nw_freq))]
    actors <- merge(actors, at, by.x = "Row.names", by.y = "value")
    actors <- actors[, !(colnames(actors) == "id")]
    actors$variable <- "actor"
  }
  if (store_variables == variable2 | store_variables == "both") {
    concepts <- x[, grepl("^beta.", colnames(x))]
    hpd <- as.data.frame(coda::HPDinterval(concepts, prob = 0.95))
    concepts <- as.data.frame(colMeans(concepts))
    concepts <- merge(concepts, hpd, by = 0)
    colnames(concepts)[colnames(concepts) == "colMeans(concepts)"] <- "mean"
    colnames(concepts)[colnames(concepts) == "lower"] <- "HPD2.5"
    colnames(concepts)[colnames(concepts) == "upper"] <- "HPD97.5"
    concepts1 <- concepts[grepl(".1$", concepts$Row.names), drop = FALSE, ]
    concepts1$Row.names <- gsub("^beta.|.1$", "", concepts1$Row.names)
    concepts2 <- concepts[grepl(".2$", concepts$Row.names), drop = FALSE, ]
    concepts2$Row.names <- gsub("^beta.|.2$", "", concepts2$Row.names)
    concepts <- merge(concepts1,
                      concepts2,
                      by = "Row.names",
                      suffixes = c("_dim1", "_dim2"))
    at <- dna_getAttributes(connection = connection,
                            variable = variable2,
                            values = concepts$Row.names)
    concepts <- merge(concepts, at, by.x = "Row.names", by.y = "value")
    concepts <- concepts[, !(colnames(concepts) == "id")]
    concepts$variable <- "concept"
  }
  if (store_variables == "both") {
    dna_scale$attributes <- rbind(actors, concepts)
  } else if (store_variables == variable1) {
    dna_scale$attributes <- actors
  } else if (store_variables == variable2){
    dna_scale$attributes <- concepts
  }
  dna_scale$call <- mget(names(formals()), sys.frame(sys.nframe()))
  dna_scale$call$connection <- NULL
  class(dna_scale) <- c("dna_scale2dbin", class(dna_scale))
  class(dna_scale) <- c("dna_scale", class(dna_scale))
  return(dna_scale)
}


#' Two-dimensional ordinal scaling from a DNA connection
#'
#' Scale ideological positions of two variables (e.g., organizations and
#' concepts from a DNA connection by using Markov Chain Monte Carlo for
#' ordinal two-dimensional Item Response Theory. This is one of the four
#' scaling functions. For one-dimensional binary scaling, see
#' \link{dna_scale1dbin}, for one-dimensional ordinal scaling, see
#' \link{dna_scale1dord} and for two-dimensional binary scaling
#' \link{dna_scale2dbin}.
#'
#' This function is a convenience wrapper for the
#' \link[MCMCpack]{MCMCordfactanal} function. Using Markov Chain Monte Carlo
#' (MCMC), \code{dna_scale2dord} generates a sample from the posterior
#' distribution of an ordinal data factor analysis model, using a
#' Metropolis-Hastings within Gibbs sampling algorithm. For the model form and
#' further help for the scaling arguments, see \link[MCMCpack]{MCMCordfactanal}.
#'
#' As in a two-mode network in \link{dna_network}, two variables have to be
#' provided for the scaling. The first variable corresponds to the rows of a
#' two-mode network and usually entails actors (e.g., \code{"organizations"}),
#' while the second variable is equal to the columns of a two-mode network,
#' typically expressed by \code{"concepts"}. The \code{dna_scale} functions
#' use \code{"actors"} and \code{"concepts"} as synonyms for \code{variable1}
#' and \code{variable2}. However, the scaling is not restricted to
#' \code{"actors"} and \code{"concepts"} but depends on what you provide in
#' \code{variable1} or \code{variable2}.
#'
#' \code{dna_scale2dord} internally uses the \code{combine} qualifier
#' aggregation and then recodes the values into \code{1} for disagreement,
#' \code{2} for mixed positions and \code{3} for agreement. Integer qualifiers
#' are not recoded. When \code{zero_is_na} is set to \code{TRUE}, non-mentions
#' of concepts are set to \code{NA}, while setting the argument to \code{FALSE}
#' recodes them to \code{2} as mixed position. By setting a \code{threshold},
#' you can further decide at which percentage of agreement and disagreement
#' an actor position on a concept can be considered as agreement/disagreement
#' or mixed position.
#'
#' The argument \code{drop_min_actors} excludes actors with only a limited
#' number of concepts used. Limited participation of actors in a debate can
#' impact the scaling of the ideal points, as actors with only few mentions of
#' concepts convey limited information on their ideological position. The same
#' can also be done for concepts with the argument \code{drop_min_concepts}.
#' Concepts that have been rarely mentioned do not strongly discriminate the
#' ideological positions of actors and can, therefore, impact the accuracy of
#' the scaling. Reducing the number of actors of concepts to be scaled hence
#' improves the precision of the ideological positions for both variables and
#' the scaling itself. Another possibility to reduce the number of concepts is
#' to use \code{drop_constant_concepts}, which will reduce concepts not having
#' any variation in the agreement/disagreement structure of actors. This means
#' that all concepts will be dropped which have only agreeing, disagreeing or
#' mixed statements.
#'
#' As \code{dna_scale2dord} implements a Bayesian Item Response Theory
#' approach, \code{priors} and \code{starting values} can be set on the concept
#' parameters. Changing the default \code{prior} values can often
#' help you to achieve better results. Constraints on the concept parameters
#' can also be specified to help identifying the model and to indicate in which
#' direction ideological positions of actors and concepts run. For concepts,
#' the scaling estimates an item discrimination parameter for each dimension
#' and an item difficulty for both dimensions. The item difficulty parameter
#' should, however, not be constrained (see \link[MCMCpack]{MCMCordfactanal}).
#' Therefore, you should set constraints on the item discrimination parameters.
#'
#' Fitting two-dimensional scaling models requires a good choice of concept
#' constraints to specify the ideological dimensions of your data. A suitable
#' way of identifying your ideological dimensions is to constrain one item
#' discrimination parameter to load only on one dimension. This means that we
#' set one parameter to load either positive or negative on one dimension and
#' setting it to zero on the other. A second concept should also be constrained
#' to load either positive or negative on one dimension (see example)
#'
#' To plot the resulting ideal points of actors and concepts, you can use the
#' \link{dna_plotScale} function. To assess if the returned MCMC chain has
#' converged to its stationary distribution, please use
#' \link{dna_convergenceScale}. The evaluation of convergence is essential to
#' report conclusions based on accurate parameter estimates. Achieving chain
#' convergence often requires setting the iterations of the MCMC chain to
#' several million.
#'
#' @param connection A \code{dna_connection} object created by the
#'   \link{dna_connection} function.
#' @param variable1 The first variable for the scaling construction (see
#'   \link{dna_network}). Defaults to \code{"organization"}.
#' @param variable2 The second variable for network construction (see
#'   \link{dna_network}). Defaults to \code{"concept"}.
#' @param qualifier The qualifier variable for the scaling construction (see
#'   \link{dna_network}). Defaults to \code{"agreement"}.
#' @param zero_as_na Logical. If \code{TRUE}, all non-mentions of an actor
#'   towards a concept will be recoded as \code{NA}. If \code{FALSE} as
#'   \code{2}.
#' @param threshold Numeric value that specifies when a mixed position can be
#'   considered as agreement or disagreement. If e.g., one actor has 60 percent
#'   of agreeing and 40 percent of disagreeing statements towards a concept, a
#'   \code{threshold} of 0.51 will recode the actor position on this concept as
#'   "agreement". The same accounts also for disagreeing statements. If one
#'   actor has 60 percent of disagreeing and 40 percent of agreeing statements,
#'   a \code{threshold} of 0.51 will recode the actor position on this concept
#'   as "disagreement". All values in between the \code{threshold} (e.g., 55
#'   percent agreement and 45 percent of disagreement and a threshold of 0.6)
#'   will be recoded as \code{2}. If is set to \code{NULL}, all "mixed"
#'   positions of actors will be recoded as \code{2}. Must be strictly
#'   positive.
#' @param lambda_constraints A list of lists specifying constraints on the
#'   concept parameters. Note that value \code{1} in the brackets of the
#'   argument refers to the item difficulty parameters, which in
#'   general should not be constrained. All values above \code{1} relate to the
#'   item discrimination parameters on the single dimensions. These should be
#'   used for constraints on concepts. Three forms of constraints are
#'   possible: \code{conceptname = list(2, value)} will constrain a concept to
#'   be equal to the specified value (e.g., 0) on the first dimension of the
#'   item discrimination parameter. \code{conceptname = list(2,"+")} will
#'   constrain the concept to be positively scaled on the first dimension and
#'   \code{conceptname = list(2, "-")} will constrain the concept to be
#'   negatively scaled on the first dimension (see example). If you
#'   wish to constrain a concept on the second dimension, please indicate this
#'   with a \code{3} in the first position in the bracket.
#' @param mcmc_iterations The number of iterations for the sampler.
#' @param mcmc_burnin The number of burn-in iterations for the sampler.
#' @param mcmc_thin The thinning interval for the sampler. Iterations must be
#'   divisible by the thinning interval.
#' @param mcmc_tune The tuning parameter for the acceptance rates of the
#'   sampler. Acceptance rates should ideally range between \code{0.15} and
#'   \code{0.5}. Can be either a scalar or a k-vector. Must be strictly
#'   positive.
#' @param lambda_start \code{Starting values} for the concept parameters. Can
#'   be either a scalar or a matrix. If set to \code{NA} (default), the
#'   \code{starting values} for the unconstrained parameters in the first
#'   column are based on the observed response pattern. The remaining
#'   unconstrained elements are set to \code{starting values} of either
#'   \code{1.0} or \code{-1.0}, depending on the nature of the constraint.
#' @param lambda_prior_mean The prior mean of the concept parameters. Can be
#'   either a scalar or a matrix.
#' @param lambda_prior_variance The prior inverse variances of the concept
#'   parameters. Can be either a scalar or a matrix.
#' @param store_variables A character vector indicating which variables should
#'   be stored from the scaling. Can either take the value of the character
#'   vector indicated in \code{variable1} or \code{variable2} or \code{"both"}
#'   to store both variables. Note that saving both variables can impact the
#'   speed of the scaling. Defaults to \code{"both"}.
#' @param drop_constant_concepts Logical. Should concepts that have no
#'   variation be deleted before the scaling? Defaults to \code{FALSE}.
#' @param drop_min_actors A numeric value specifying the minimum number of
#'   concepts actors should have mentioned to be included in the scaling.
#'   Defaults to \code{1}.
#' @param drop_min_concepts A numeric value specifying the minimum number a
#'   concept should have been jointly mentioned by actors. Defaults to \code{2}.
#' @param verbose A boolean or numeric value indicating whether the iterations
#'   of the scaling should be printed to the R console. If set to a numeric
#'   value, every \code{verboseth} iteration will be printed. If set to
#'   \code{TRUE}, \code{verbose} will print the total of iterations and burn-in
#'   divided by \code{10}.
#' @param seed The random seed for the scaling.
#' @param ... Additional arguments passed to \link{dna_network}. Actors can
#'   e.g., be removed with the \code{excludeValues} arguments. The scaling can
#'   also be applied to a specific time slice by using \code{start.date} and
#'   \code{stop.date}.
#'
#' @examples
#' dna_init()
#' conn <- dna_connection(dna_sample())
#' dna_scale <- dna_scale2dord(
#'   conn,
#'   variable1 = "organization",
#'   variable2 = "concept",
#'   qualifier = "agreement",
#'   zero_as_na = TRUE,
#'   threshold = 0.6,
#'   lambda_constraints = list(
#'     `Climate change is caused by greenhouse gases (CO2).` = list(2, "-"),
#'     `Climate change is caused by greenhouse gases (CO2).` = list(3, 0),
#'     `CO2 legislation will not hurt the economy.` = list(3, "-")),
#'   mcmc_iterations = 20000,
#'   mcmc_burnin = 2000,
#'   mcmc_thin = 10,
#'   mcmc_tune = 1.5,
#'   lambda_prior_mean = 0,
#'   lambda_prior_variance = 0.1,
#'   store_variables = "both",
#'   drop_constant_concepts = TRUE,
#'   verbose = TRUE,
#'   seed = 12345
#' )
#'
#' @author Tim Henrichsen, Johannes B. Gruber
#' @export
#' @importFrom MCMCpack MCMCordfactanal
dna_scale2dord <- function(connection,
                           variable1 = "organization",
                           variable2 = "concept",
                           qualifier = "agreement",
                           zero_as_na = TRUE,
                           threshold = NULL,
                           lambda_constraints = NULL,
                           mcmc_iterations = 20000,
                           mcmc_burnin = 1000,
                           mcmc_thin = 10,
                           mcmc_tune = 1.5,
                           lambda_start = NA,
                           lambda_prior_mean = 0,
                           lambda_prior_variance = 0.1,
                           store_variables = "both",
                           drop_constant_concepts = FALSE,
                           drop_min_actors = 1,
                           drop_min_concepts = 2,
                           verbose = TRUE,
                           seed = 12345,
                           ...) {
  dots <- list(...)
  out <- ord_recode(connection = connection,
                    variable1 = variable1,
                    variable2 = variable2,
                    qualifier = qualifier,
                    zero_as_na = zero_as_na,
                    threshold = threshold,
                    drop_min_actors = drop_min_actors,
                    drop_min_concepts = drop_min_concepts,
                    store_variables = store_variables,
                    dots = dots)
  nw2 <- out$nw2
  dots <- out$dots
  dots_nw <- out$dots_nw
  invertValues <- out$invertValues
  excludeValues <- out$excludeValues
  # Scaling
  x <- do.call(eval(parse(text = "MCMCpack::MCMCordfactanal")), c(list(
    nw2,
    factors = 2,
    lambda.constraints = lambda_constraints,
    burnin = mcmc_burnin,
    mcmc = mcmc_iterations,
    thin = mcmc_thin,
    tune = mcmc_tune,
    verbose = ifelse(verbose == TRUE,
                     ((mcmc_iterations + mcmc_burnin) / 10),
                     verbose),
    seed = seed,
    lambda.start = lambda_start,
    l0 = lambda_prior_mean,
    L0 = lambda_prior_variance,
    store.lambda = (store_variables == variable2 | store_variables == "both"),
    store.scores = (store_variables == variable1 | store_variables == "both"),
    drop.constantvars = drop_constant_concepts),
    dots))
  dna_scale <- list()
  dna_scale$sample <- x
  # Store actor frequency for possible min argument in dna_plotScale
  nw_freq <- do.call("dna_network", c(list(connection = connection,
                                           networkType = "twomode",
                                           variable1 = variable1,
                                           variable2 = variable2,
                                           qualifier = qualifier,
                                           qualifierAggregation = "ignore",
                                           verbose = FALSE,
                                           excludeValues = excludeValues,
                                           invertValues = invertValues),
                                      dots_nw))
  if (store_variables == variable1 | store_variables == "both") {
    actors <- x[, grepl("^phi.", colnames(x))]
    hpd <- as.data.frame(coda::HPDinterval(actors, prob = 0.95))
    actors <- as.data.frame(colMeans(actors))
    actors <- merge(actors, hpd, by = 0)
    colnames(actors)[colnames(actors) == "colMeans(actors)"] <- "mean"
    colnames(actors)[colnames(actors) == "lower"] <- "HPD2.5"
    colnames(actors)[colnames(actors) == "upper"] <- "HPD97.5"
    actors1 <- actors[grepl(".2$", actors$Row.names), drop = FALSE, ]
    actors1$Row.names <- gsub("^phi.|.2$", "", actors1$Row.names)
    actors2 <- actors[grepl(".3$", actors$Row.names), drop = FALSE, ]
    actors2$Row.names <- gsub("^phi.|.3$", "", actors1$Row.names)
    actors <- merge(actors1,
                    actors2,
                    by = "Row.names",
                    suffixes = c("_dim1", "_dim2"))
    at <- dna_getAttributes(connection = connection,
                            variable = variable1,
                            values = actors$Row.names)
    at$frequency <- rowSums(nw_freq)[match(at$value, rownames(nw_freq))]
    actors <- merge(actors, at, by.x = "Row.names", by.y = "value")
    actors <- actors[, !(colnames(actors) == "id")]
    actors$variable <- "actor"
  }
  if (store_variables == variable2 | store_variables == "both") {
    concepts <- x[, grepl("^Lambda", colnames(x))]
    hpd <- as.data.frame(coda::HPDinterval(concepts, prob = 0.95))
    concepts <- as.data.frame(colMeans(concepts))
    concepts <- merge(concepts, hpd, by = 0)
    colnames(concepts)[colnames(concepts) == "colMeans(concepts)"] <- "mean"
    colnames(concepts)[colnames(concepts) == "lower"] <- "HPD2.5"
    colnames(concepts)[colnames(concepts) == "upper"] <- "HPD97.5"
    concepts1 <- concepts[grepl(".2$", concepts$Row.names), drop = FALSE, ]
    concepts1$Row.names <- gsub("^Lambda|.2$", "", concepts1$Row.names)
    concepts2 <- concepts[grepl(".3$", concepts$Row.names), drop = FALSE, ]
    concepts2$Row.names <- gsub("^Lambda|.3$", "", concepts2$Row.names)
    concepts <- merge(concepts1,
                      concepts2,
                      by = "Row.names",
                      suffixes = c("_dim1", "_dim2"))
    at <- dna_getAttributes(connection = connection,
                            variable = variable2,
                            values = concepts$Row.names)
    concepts <- merge(concepts, at, by.x = "Row.names", by.y = "value")
    concepts <- concepts[, !(colnames(concepts) == "id")]
    concepts$variable <- "concept"
  }
  if (store_variables == "both") {
    dna_scale$attributes <- rbind(actors, concepts)
  } else if (store_variables == variable1) {
    dna_scale$attributes <- actors
  } else if (store_variables == variable2){
    dna_scale$attributes <- concepts
  }
  dna_scale$call <- mget(names(formals()), sys.frame(sys.nframe()))
  dna_scale$call$connection <- NULL
  class(dna_scale) <- c("dna_scale2dord", class(dna_scale))
  class(dna_scale) <- c("dna_scale", class(dna_scale))
  return(dna_scale)
}


#' Plot a \code{dna_scale} object
#'
#' Plot convergence diagnostics of the MCMC chain created by the four
#' \code{dna_scale} functions.
#'
#' Plots \code{trace plots} of the \code{dna_scale} MCMC output. For further
#' convergence diagnostics, see \link{dna_convergenceScale}.
#'
#' @param x A dna_scale object.
#' @param ... Further options (currently not used).
#'
#' @examples
#' dna_init()
#' conn <- dna_connection(dna_sample())
#' dna_scale <- dna_scale1dbin(conn,
#'                             variable1 = "organization",
#'                             variable2 = "concept",
#'                             qualifier = "agreement",
#'                             threshold = 0.51,
#'                             mcmc_iterations = 20000,
#'                             mcmc_burnin = 2000,
#'                             mcmc_thin = 10,
#'                             store_variables = "both",
#'                             drop_min_actors = 1,
#'                             seed = 12345)
#' plot(dna_scale)
#'
#' @author Tim Henrichsen, Johannes B. Gruber
#' @export
plot.dna_scale <- function(x, ...) {
  dna_convergenceScale(x,
                       variable = x$call$store_variables,
                       method = "trace",
                       colors = TRUE)
}


#' Print the summary of a \code{dna_scale} object
#'
#' Show details of the MCMC chain created by the four \code{dna_scale}
#' functions.
#'
#' Prints the method that was used for the scaling, the number of values in the
#' object and their means.
#'
#' @param x A \code{dna_scale} object.
#' @param ... Further options (currently not used).
#'
#' @examples
#' dna_init()
#' conn <- dna_connection(dna_sample())
#' dna_scale <- dna_scale1dbin(conn,
#'                             variable1 = "organization",
#'                             variable2 = "concept",
#'                             qualifier = "agreement",
#'                             threshold = 0.51,
#'                             mcmc_iterations = 20000,
#'                             mcmc_burnin = 2000,
#'                             mcmc_thin = 10,
#'                             store_variables = "both")
#' dna_scale
#' @author Tim Henrichsen, Johannes B. Gruber
#' @export
print.dna_scale <- function(x, ...) {
  cat("Method:", class(x)[2], "\n")
  cat("Number of objects:", nrow(x$attributes), "\n")
  cat("Mean (", ifelse(nrow(x$attributes) > 12, 12,
                       nrow(x$attributes)),
      " of ", nrow(x$attributes), "):", "\n", sep = "")
  if ("dna_scale1dbin" %in% class(x) |
      "dna_scale1dord" %in% class(x)) {
    x$attributes[2] <- round(x$attributes[2], digits = 2)
    out <- head(x$attributes[, 1:2], n = 6)
    out <- rbind(out, c("...", rep_len("", length.out = (ncol(x$attributes) - 1))))
    out <- rbind(out, tail(x$attributes[, 1:2]))
    print.data.frame(out, row.names = FALSE, right = FALSE)
  } else {
    x$attributes[2:3] <- round(x$attributes[2:3], digits = 2)
    out <- head(x$attributes[, 1:3], n = 6)
    out <- rbind(out, c("...", rep_len("", length.out = (ncol(x$attributes) - 1))))
    out <- rbind(out, tail(x$attributes[, 1:3]))
    print.data.frame(out, row.names = FALSE, right = FALSE)
  }
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
#'   \code{dna_connection} function.
#' @param networkType The kind of network to be computed. Can be
#'   \code{"twomode"}, \code{"onemode"}, or \code{"eventlist"}.
#' @param statementType The name of the statement type in which the variable
#'   of interest is nested. For example, \code{"DNA Statement"}.
#' @param variable1 The first variable for network construction. In a one-mode
#'   network, this is the variable for both the rows and columns. In a
#'   two-mode network, this is the variable for the rows only. In an event
#'   list, this variable is only used to check for duplicates (depending on
#'   the setting of the \code{duplicate} argument).
#' @param variable1Document A boolean value indicating whether the first
#'   variable is at the document level (i.e., \code{"author"},
#'   \code{"source"}, \code{"section"}, \code{"type"}, \code{"id"}, or
#'   \code{"title"}).
#' @param variable2 The second variable for network construction. In a one-mode
#'   network, this is the variable over which the ties are created. For
#'   example, if an organization x organization network is created, and ties
#'   in this network indicate co-reference to a concept, then the second
#'   variable is the \code{"concept"}. In a two-mode network, this is the
#'   variable used for the columns of the network matrix. In an event list,
#'   this variable is only used to check for duplicates (depending on the
#'   setting of the \code{duplicate} argument).
#' @param variable2Document A boolean value indicating whether the second
#'   variable is at the document level (i.e., \code{"author"},
#'   \code{"source"}, \code{"section"}, \code{"type"}, \code{"id"}, or
#'   \code{"title"}
#' @param qualifier The qualifier variable. In a one-mode network, this
#'   variable can be used to count only congruence or conflict ties. For
#'   example, in an organization x organization network via common concepts,
#'   a binary \code{"agreement"} qualifier could be used to record only ties
#'   where both organizations have a positive stance on the concept or where
#'   both organizations have a negative stance on the concept. With an
#'   integer qualifier, the tie weight between the organizations would be
#'   proportional to the similarity or distance between the two organizations
#'   on the scale of the integer variable.
#'
#'   In a two-mode network, the qualifier variable can be used to retain only
#'   positive or only negative statements or subtract negative from positive
#'   mentions. All of this depends on the setting of the
#'   \code{qualifierAggregation} argument. For event lists, the qualifier
#'   variable is only used for filtering out duplicates (depending on the
#'   setting of the \code{duplicate} argument.
#'
#'   The qualifier can also be \code{NULL}, in which case it is ignored, meaning
#'   that values in \code{variable1} and \code{variable2} are unconditionally
#'   associated with each other in the network when they co-occur. This is
#'   identical to selecting a qualifier variable and setting
#'   \code{qualifierAggregation = "ignore"}.
#' @param qualifierAggregation The aggregation rule for the \code{qualifier}
#'   variable. In one-mode networks, this must be \code{"ignore"} (for
#'   ignoring the qualifier variable), \code{"congruence"} (for recording a
#'   network tie only if both nodes have the same qualifier value in the
#'   binary case or for recording the similarity between the two nodes on the
#'   qualifier variable in the integer case), \code{"conflict"} (for
#'   recording a network tie only if both nodes have a different qualifier
#'   value in the binary case or for recording the distance between the two
#'   nodes on the qualifier variable in the integer case), or
#' \code{"subtract"} (for subtracting the conflict tie value from the
#'   congruence tie value in each dyad). In two-mode networks, this must be
#' \code{"ignore"}, \code{"combine"} (for creating multiplex combinations,
#'   e.g., 1 for positive, 2 for negative, and 3 for mixed), or
#' \code{subtract} (for subtracting negative from positive ties). In event
#'   lists, this setting is ignored.
#' @param normalization Normalization of edge weights. Valid settings for
#'   one-mode networks are \code{"no"} (for switching off normalization),
#'   \code{"average"} (for average activity normalization), \code{"Jaccard"}
#'   (for Jaccard coefficient normalization), and \code{"cosine"} (for
#'   cosine similarity normalization). Valid settings for two-mode networks
#'   are \code{"no"}, \code{"activity"} (for activity normalization), and
#'   \code{"prominence"} (for prominence normalization).
#' @param isolates Should all nodes of the respective variable be included in
#'   the network matrix (\code{isolates = TRUE}), or should only those nodes
#'   be included that are active in the current time period and are not
#'   excluded (\code{isolates = FALSE})?
#' @param duplicates Setting for excluding duplicate statements before network
#'   construction. Valid settings are \code{"include"} (for including all
#'   statements in network construction), \code{"document"} (for counting
#'   only one identical statement per document), \code{"week"} (for counting
#'   only one identical statement per calendar week), \code{"month"} (for
#'   counting only one identical statement per calendar month), \code{"year"}
#'   (for counting only one identical statement per calendar year), and
#'   \code{"acrossrange"} (for counting only one identical statement across
#'   the whole time range).
#' @param start.date The start date for network construction in the format
#'   "dd.mm.yyyy". All statements before this date will be excluded.
#' @param start.time The start time for network construction on the specified
#'   \code{start.date}. All statements before this time on the specified date
#'   will be excluded.
#' @param stop.date The stop date for network construction in the format
#'   "dd.mm.yyyy". All statements after this date will be excluded.
#' @param stop.time The stop time for network construction on the specified
#'   \code{stop.date}. All statements after this time on the specified date
#'   will be excluded.
#' @param timewindow Possible values are \code{"no"}, \code{"events"},
#'   \code{"seconds"}, \code{"minutes"}, \code{"hours"}, \code{"days"},
#'   \code{"weeks"}, \code{"months"}, and \code{"years"}. If \code{"no"} is
#'   selected (= the default setting), no time window will be used. If any of
#'   the time units is selected, a moving time window will be imposed, and
#'   only the statements falling within the time period defined by the window
#'   will be used to create the network. The time window will then be moved
#'   forward by one time unit at a time, and a new network with the new time
#'   boundaries will be created. This is repeated until the end of the overall
#'   time span is reached. All time windows will be saved as separate
#'   networks in a list. The duration of each time window is defined by the
#'   \code{windowsize} argument. For example, this could be used to create a
#'   time window of 6 months which moves forward by one month each time, thus
#'   creating time windows that overlap by five months. If \code{"events"} is
#'   used instead of a natural time unit, the time window will comprise
#'   exactly as many statements as defined in the \code{windowsize} argument.
#'   However, if the start or end statement falls on a date and time where
#'   multiple events happen, those additional events that occur simultaneously
#'   are included because there is no other way to decide which of the
#'   statements should be selected. Therefore the window size is sometimes
#'   extended when the start or end point of a time window is ambiguous in
#'   event time.
#' @param windowsize The number of time units of which a moving time window is
#'   comprised. This can be the number of statement events, the number of days
#'   etc., as defined in the \code{"timewindow"} argument.
#' @param excludeValues A list of named character vectors that contains entries
#'   which should be excluded during network construction. For example,
#'   \code{list(concept = c("A", "B"), organization = c("org A", "org B"))}
#'   would exclude all statements containing concepts "A" or "B" or
#'   organizations "org A" or "org B" when the network is constructed. This
#'   is irrespective of whether these values appear in \code{variable1},
#'   \code{variable2}, or the \code{qualifier}. Note that only variables at
#'   the statement level can be used here. There are separate arguments for
#'   excluding statements nested in documents with certain meta-data.
#' @param excludeAuthors A character vector of authors. If a statement is
#'   nested in a document where one of these authors is set in the "Author"
#'   meta-data field, the statement is excluded from network construction.
#' @param excludeSources A character vector of sources. If a statement is
#'   nested in a document where one of these sources is set in the "Source"
#'   meta-data field, the statement is excluded from network construction.
#' @param excludeSections A character vector of sections. If a statement is
#'   nested in a document where one of these sections is set in the "Section"
#'   meta-data field, the statement is excluded from network construction.
#' @param excludeTypes A character vector of types. If a statement is
#'   nested in a document where one of these types is set in the "Type"
#'   meta-data field, the statement is excluded from network construction.
#' @param invertValues A boolean value indicating whether the entries provided
#'   by the \code{excludeValues} argument should be excluded from network
#'   construction (\code{invertValues = FALSE}) or if they should be the only
#'   values that should be included during network construction
#'   (\code{invertValues = TRUE}).
#' @param invertAuthors A boolean value indicating whether the entries provided
#'   by the \code{excludeAuthors} argument should be excluded from network
#'   construction (\code{invertAuthors = FALSE}) or if they should be the
#'   only values that should be included during network construction
#'   (\code{invertAuthors = TRUE}).
#' @param invertSources A boolean value indicating whether the entries provided
#'   by the \code{excludeSources} argument should be excluded from network
#'   construction (\code{invertSources = FALSE}) or if they should be the
#'   only values that should be included during network construction
#'   (\code{invertSources = TRUE}).
#' @param invertSections A boolean value indicating whether the entries
#'   provided by the \code{excludeSections} argument should be excluded from
#'   network construction (\code{invertSections = FALSE}) or if they should
#'   be the only values that should be included during network construction
#'   (\code{invertSections = TRUE}).
#' @param invertTypes A boolean value indicating whether the entries provided
#'   by the \code{excludeTypes} argument should be excluded from network
#'   construction (\code{invertTypes = FALSE}) or if they should be the
#'   only values that should be included during network construction
#'   (\code{invertTypes = TRUE}).
#' @param fileFormat An optional file format specification for saving the
#'   resulting network(s) to a file instead of returning an object. Valid values
#'   are \code{"csv"} (for network matrices or event lists), \code{"dl"} (for
#'   UCINET DL full-matrix files), and \code{"graphml"} (for visone .graphml
#'   files). The \code{"graphml"} specification is compatible with time windows.
#' @param outfile An optional output file name for saving the resulting
#'   network(s) to a file instead of returning an object.
#' @param verbose A boolean value indicating whether details of network
#'   construction should be printed to the R console.
#'
#' @examples
#' dna_init()
#' conn <- dna_connection(dna_sample())
#' nw <- dna_network(conn,
#'   networkType = "onemode",
#'   variable1 = "organization",
#'   variable2 = "concept",
#'   qualifier = "agreement",
#'   qualifierAggregation = "congruence",
#'   normalization = "average",
#'   excludeValues = list("concept" =
#'   c("There should be legislation to regulate emissions.")))
#'
#' # plot network
#' dna_plotNetwork(nw)
#' dna_plotHive(nw)
#'
#' @author Philip Leifeld
#'
#' @importFrom rJava .jarray
#' @importFrom rJava .jcall
#' @importFrom rJava .jevalArray
#' @importFrom rJava .jnull
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
                        fileFormat = NULL,
                        outfile = NULL,
                        verbose = TRUE) {

  # check time window arguments
  if (is.null(timewindow) ||
      is.na(timewindow) ||
      !is.character(timewindow) ||
      length(timewindow) != 1 ||
      !timewindow %in% c("no", "events", "seconds", "minutes", "hours", "days", "weeks", "months", "years")) {
    timewindow <- "no"
    warning("'timewindow' argument not recognized. Using 'timewindow = \"no\"'.")
  }
  if (is.null(windowsize) ||
      is.na(windowsize) ||
      !is.numeric(windowsize) ||
      length(windowsize) != 1 ||
      windowsize < 0 ||
      (windowsize == 0 && timewindow != "no")) {
    windowsize <- 100
    warning("'windowsize' argument not recognized. Using 'windowsize = 100'.")
  }

  # check and convert exclude arguments
  if (!is.character(excludeAuthors)) {
    stop("'excludeAuthors' must be a character object.")
  }
  if (!is.character(excludeSources)) {
    stop("'excludeSources' must be a character object.")
  }
  if (!is.character(excludeSections)) {
    stop("'excludeSections' must be a character object.")
  }
  if (!is.character(excludeTypes)) {
    stop("'excludeTypes' must be a character object.")
  }
  if (!is.list(excludeValues) || (length(excludeValues) > 0 && is.null(names(excludeValues)))) {
    stop("'excludeValues' must be a named list.")
  }
  excludeAuthors <- .jarray(excludeAuthors)
  excludeSources <- .jarray(excludeSources)
  excludeSections <- .jarray(excludeSections)
  excludeTypes <- .jarray(excludeTypes)

  # compile exclude variables and values vectors
  dat <- matrix("", nrow = length(unlist(excludeValues)), ncol = 2)
  count <- 0
  if (length(excludeValues) > 0) {
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
  var <- .jarray(var)
  val <- .jarray(val)

  if (is.null(variable1) || is.na(variable1) || length(variable1) != 1 || !is.character(variable1)) {
    stop("'variable1' must be a character object of length 1.")
  }
  if (is.null(variable2) || is.na(variable2) || length(variable2) != 1 || !is.character(variable2)) {
    stop("'variable2' must be a character object of length 1.")
  }
  if (is.null(qualifier) || is.na(qualifier)) {
    qualifier <- .jnull(class = "java/lang/String")
  } else if (length(qualifier) != 1 || !is.character(qualifier)) {
    stop("'qualifier' must be NULL or a character object of length 1.")
  }
  if (is.null(variable1Document) || is.na(variable1Document) || length(variable1Document) != 1 || !is.logical(variable1Document)) {
    stop("'variable1Document' must be TRUE or FALSE.")
  }
  if (is.null(variable2Document) || is.na(variable2Document) || length(variable2Document) != 1 || !is.logical(variable2Document)) {
    stop("'variable2Document' must be TRUE or FALSE.")
  }
  if (is.null(normalization) || is.na(normalization) || length(normalization) != 1 || !is.character(normalization)
      || !normalization %in% c("no", "activity", "prominence", "average", "Jaccard", "jaccard", "cosine")) {
    stop("'normalization' must be 'no', 'activity', 'prominence', 'average', 'Jaccard', or 'cosine'.")
  }
  if (normalization %in% c("activity", "prominence") && networkType == "onemode") {
    stop("'normalization' must be 'no', 'average', 'Jaccard', or 'cosine' when networkType = 'onemode'.")
  }
  if (normalization %in% c("average", "Jaccard", "jaccard", "cosine") && networkType == "twomode") {
    stop("'normalization' must be 'no', 'activity', or 'prominence' when networkType = 'twomode'.")
  }

  if (!is.null(fileFormat) && !fileFormat %in% c("csv", "dl", "graphml")) {
    stop("'fileFormat' must be 'csv', 'dl', or 'graphml'.")
  }
  if (!is.null(fileFormat) && timewindow != "no" && fileFormat %in% c("csv", "dl")) {
    stop("Only .graphml files are currently compatible with time windows.")
  }
  if (!is.null(fileFormat) && networkType == "eventlist" && fileFormat %in% c("dl", "graphml")) {
    stop("Only .csv files are currently compatible with event lists.")
  }
  if (is.null(outfile) || is.null(fileFormat)) {
    fileExport <- TRUE
  } else {
    fileExport <- FALSE
  }
  if (is.null(fileFormat)) {
    fileFormat <- .jnull(class = "java/lang/String")
  }
  if (is.null(outfile)) {
    outfile <- .jnull(class = "java/lang/String")
  }

  # call Java function to create network
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
         outfile,
         fileFormat,
         verbose
  )

  if (isTRUE(fileExport)) {
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
      attributes(mat)$start <- as.POSIXct(.jcall(connection$dna_connection, "J", "getTime", "start", simplify = TRUE), origin = "1970-01-01")
      attributes(mat)$stop <- as.POSIXct(.jcall(connection$dna_connection, "J", "getTime", "stop", simplify = TRUE), origin = "1970-01-01")
      attributes(mat)$call <- match.call()
      class(mat) <- c(paste0("dna_network_", networkType), class(mat))
      return(mat)
    } else {
      timeLabels <- .jcall(connection$dna_connection, "[J", "getTimeWindowTimes", "middle", simplify = TRUE)
      timeLabels <- as.POSIXct(timeLabels, origin = "1970-01-01")
      startLabels <- .jcall(connection$dna_connection, "[J", "getTimeWindowTimes", "start", simplify = TRUE)
      startLabels <- as.POSIXct(startLabels, origin = "1970-01-01")
      stopLabels <- .jcall(connection$dna_connection, "[J", "getTimeWindowTimes", "stop", simplify = TRUE)
      stopLabels <- as.POSIXct(stopLabels, origin = "1970-01-01")
      numStatements <- .jcall(connection$dna_connection, "[I", "getTimeWindowNumStatements", simplify = TRUE)
      mat <- list()
      for (t in 1:length(timeLabels)) {
        m <- .jcall(connection$dna_connection, "[[D", "getTimeWindowNetwork", as.integer(t - 1), simplify = TRUE)
        rownames(m) <- .jcall(connection$dna_connection, "[S", "getTimeWindowRowNames", as.integer(t - 1), simplify = TRUE)
        colnames(m) <- .jcall(connection$dna_connection, "[S", "getTimeWindowColumnNames", as.integer(t - 1), simplify = TRUE)
        attributes(m)$call <- match.call()
        class(m) <- c(paste0("dna_network_", networkType), class(m))
        mat[[t]] <- m
      }
      dta <- list()
      dta$networks <- mat
      dta$start <- startLabels
      dta$time <- timeLabels
      dta$stop <- stopLabels
      dta$numStatements <- numStatements
      attributes(dta)$call <- match.call()
      class(dta) <- c(paste0("dna_network_", networkType, "_timewindows"), class(dta))
      return(dta)
    }
  }
}


# Transformation ---------------------------------------------------------------

#' Convert DNA networks to igraph objects
#'
#' This function can convert objects of class 'dna_network_onemode' or
#' 'dna_network_twomode' to igraph objects.
#'
#' @param x A dna_network (one- or two-mode).
#' @param weighted Logical. Should edge weights be used to create a weighted
#'   graph from the dna_network object.
#'
#' @author Johannes B. Gruber
#'
#' @export
#' @importFrom igraph graph_from_adjacency_matrix graph_from_incidence_matrix
#'
#' @examples
#' dna_init()
#' conn <- dna_connection(dna_sample())
#' nw <- dna_network(conn, networkType = "onemode")
#' graph <- dna_toIgraph(nw)
dna_toIgraph <- function(x,
                         weighted = TRUE) {
  if (any(class(x) %in% "dna_network_onemode")) {
    graph <- graph_from_adjacency_matrix(x,
                                         mode = "undirected",
                                         weighted = weighted,
                                         diag = FALSE,
                                         add.colnames = NULL,
                                         add.rownames = NA)
  } else if (any(class(x) %in% "dna_network_twomode")) {
    graph <- graph_from_incidence_matrix(x,
                                         directed = FALSE,
                                         weighted = weighted,
                                         add.names = NULL)
  } else {
    stop("Only takes objects of class 'dna_network_onemode' or 'dna_network_twomode'.")
  }
  return(graph)
}

#' Convert DNA networks to eventSequence objects
#'
#' This function can produce eventSequence objects (see
#' \link[rem]{eventSequence}) used in the package rem from DNA connections.
#'
#' @param x A \code{dna_connection} object created by the \code{dna_connection}
#'   function or a \code{dna_eventlist} object created with \code{dna_network}
#'   (setting \code{networkType = "eventlist"}).
#' @param variable The first variable for network construction(see
#'   \link{dna_network}). The second one defaults to "concept" but can be
#'   provided via ... if necessary (see \code{variable2} in
#'   \code{dna_connection}).
#' @param ... Additional arguments passed to \link{dna_network} and
#' \link[rem]{eventSequence}.
#'
#' @return A data.frame containing an event sequence for relational event
#' models.
#' @export
#' @importFrom rem eventSequence
#'
#' @author Johannes B. Gruber
#'
#' @examples
#' dna_init()
#' conn <- dna_connection(dna_sample())
#'
#' ### Convert dna_connection to eventSequence
#' eventSequence <- dna_toREM(conn)
#'
#' ### Convert network object to eventSequence
#' nw <- dna_network(conn, networkType = "eventlist")
#' eventSequence <- dna_toREM(nw)
#'
#' ### Pass on arguments to dna_network
#' eventSequence2 <- dna_toREM(conn, duplicates = "document")
#'
#' ### Pass on arguments to eventSequence
#' eventSequence3 <- dna_toREM(conn, excludeTypeOfDay = "Wednesday")
dna_toREM <- function(x,
                      variable = "organization",
                      ...) {
  dots <- list(...)
  if (any(class(x) %in% "dna_connection")) {
    dots_network <- dots[names(dots) %in% names(formals("dna_network"))]
    dta <- do.call("dna_network",
                   c(list(x,
                          networkType = "eventlist",
                          variable1 = variable,
                          verbose = FALSE
                   ), dots_network))
  } else if (any(class(x) %in% "dna_eventlist")) {
    if (any(names(dots) %in% names(formals("dna_network")))) {
      message("Since 'x' is already a network object, arguments for dna_network() provided through '...' are ignored")
    }
    dta <- x
    args <- c(as.list(attributes(x)$call)[-1],
              formals("dna_network"))
    args <- args[!duplicated(names(args))]
    variable <- args$variable1
    x <- eval(args$connection)
  } else {
    stop("x must be an object of class 'dna_connection' or 'dna_eventlist'")
  }
  att <- dna_getAttributes(x, variable = variable)
  att$id <- NULL
  dta <- merge(dta, att, by = variable, by.y = "value", all.x = TRUE, all.y = FALSE)
  dta$date <- as.Date(dta$time)
  dta$year <- as.numeric(format(dta$date, "%Y"))
  dta$weekdays <- weekdays(dta$date)
  dots_sequence <- dots[names(dots) %in% names(formals("eventSequence"))]
  do.call("eventSequence",
          c(list(dta$date,
                 dateformat = "%Y-%m-%d",
                 data = dta,
                 returnData = TRUE,
                 sortData = TRUE
          ), dots_sequence))
}

#' Convert DNA networks to network objects
#'
#' This function can convert objects of class 'dna_network_onemode' and
#' 'dna_network_twomode' to network objects as used in the \link{network}
#' package.
#'
#' @param x A dna_network.
#' @param ... Additional arguments passed to \link[network]{as.network.matrix}.
#'
#' @author Johannes B. Gruber
#'
#' @export
#' @importFrom network as.network.matrix
#'
#' @examples
#' dna_init()
#' conn <- dna_connection(dna_sample())
#' nw <- dna_network(conn, networkType = "onemode")
#' network <- dna_toNetwork(nw)
#'
#' \dontrun{
#' library("statnet")
#' plot(network)
#' nw <- dna_network(conn, networkType = "twomode")
#' network <- dna_toNetwork(nw)
#' plot(network,
#'      displaylabels = TRUE,
#'      label.cex = 0.5,
#'      usearrows = FALSE,
#'      edge.col = "gray")
#' }
dna_toNetwork <- function(x,
                          ...) {
  if (any(class(x) %in% "dna_network_onemode")) {
    nw <- as.network.matrix(x,
                            matrix.type = "adjacency",
                            directed = FALSE,
                            bipartite = FALSE,
                            ...)
  } else if (any(class(x) %in% "dna_network_twomode")) {
    nw <- as.network.matrix(x,
                            matrix.type = "incidence",
                            directed = FALSE,
                            bipartite = TRUE,
                            ...)
  } else {
    stop("Only takes objects of class 'dna_network_onemode' or 'dna_network_twomode'.")
  }
  return(nw)
}


# Visualisation ----------------------------------------------------------------

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
#' @param what Choose either "MDS" to plot the results of multidimensional
#'   scaling or "FA" to plot two factors of the factor analysis.
#' @param dimensions Provide two numeric values to determine which dimensions to
#'   plot. The default, c(1, 2), will plot dimension 1 and dimension 2.
#' @param draw_polygons Logical. Should clusters be highlighted with colored
#'   polygons?
#' @param custom_colors Manually provide colors for the points and polygons.
#' @param custom_shape Manually provide shapes to use for the scatterplot.
#' @param alpha The alpha level of the polygons drawn when \code{draw.clusters =
#'   "polygon"}.
#' @param jitter Takes either one value, to control the width of the jittering
#'   of points, two values to control width and height of the jittering of
#'   points (e.g., c(.l, .2)) or \code{character()} to turn off the jittering of
#'   points.
#' @param seed Seed for jittering.
#' @param label Logical. Should labels be plotted?
#' @param label_size,font_color,label_background Control the label size, font
#'   color of the labels and if a background should be displayed when
#'   \code{label = TRUE}. label_size takes numeric values, font_color takes a
#'   character string with a valid color value and label_background can be
#'   either TRUE or FALSE.
#' @param point_size Size of the points in the scatterplot.
#' @param expand Expand x- and y-axis (e.g., to make room for labels). The first
#'   value is the units by which the x-axis is expanded in both directions, the
#'   second controls expansion of the y axis.
#' @param stress Should stress from the MDS be displayed on the plot.
#' @param axis_labels Provide custom axis labels.
#' @param clust_method Can be either \code{pam} for \link[cluster]{pam},
#'   \code{"louvain"} for \link[igraph]{cluster_louvain} or \code{"inherit"} to
#'   use the method provided by the call to \link{dna_cluster}.
#' @param truncate Sets the number of characters to which labels should be
#'   truncated. Value \code{Inf} turns off truncation.
#' @param title Title of the MDS plot.
#' @param ... Not used. If you want to add more plot options use \code{+} and
#'   the ggplot2 logic (see example).
#' @examples
#' dna_init()
#' conn <- dna_connection(dna_sample())
#' clust <- dna_cluster(conn)
#' mds <- dna_plotCoordinates(clust)
#'
#' # Flip plot with ggplot2 command
#' library("ggplot2")
#' mds + coord_flip()
#'
#' @author Johannes B. Gruber
#'
#' @export
#' @import ggplot2
#' @importFrom ggrepel geom_label_repel
dna_plotCoordinates <- function(clust,
                                what = "MDS",
                                dimensions = c(1, 2),
                                draw_polygons = TRUE,
                                alpha = .25,
                                jitter = NULL,
                                seed = 12345,
                                label = FALSE,
                                label_size = 3.5,
                                point_size = 1,
                                label_background = FALSE,
                                font_color = "black",
                                expand = 0,
                                stress = TRUE,
                                truncate = 40,
                                custom_colors = character(),
                                custom_shape = character(),
                                axis_labels = character(),
                                clust_method = "pam",
                                title = "auto",
                                ...) {
  dots <- list(...)
  if ("font_colour" %in% names(dots)) {
    font_color <- dots[["font_colour"]]
  }
  if ("custom_colours" %in% names(dots)) {
    custom_colors <- dots[["custom_colours"]]
  }
  if (any(!is.vector(dimensions, mode = "numeric"),
          !any(grepl(paste0("Dimension_", dimensions[1]), colnames(clust[["mds"]]))),
          !any(grepl(paste0("Dimension_", dimensions[2]), colnames(clust[["mds"]]))))) {
    stop("Please provide two valid dimensions to plot as a numeric vector (e.g., 'c(1, 2)')")
  }
  if (what == "MDS") {
    df <- clust[["mds"]]
    dim1 <- paste0("Dimension_", dimensions[1])
    dim2 <- paste0("Dimension_", dimensions[2])
  } else if (what == "FA") {
    df <- clust[["fa"]]$loadings[, dimensions]
    dim1 <- paste0("Factor", dimensions[1])
    dim2 <- paste0("Factor", dimensions[2])
    df <- data.frame(df,
                     variable = row.names(df),
                     cluster_pam = clust[["mds"]]$cluster_pam,
                     cluster_louvain = clust[["mds"]]$cluster_louvain)
  } else {
    stop("This function can either plot MDS or factor analysis data. Please select 'MDS' or 'FA' as 'what'." )
  }

  # jitter if selected
  if (length(jitter) > 0) {
    set.seed(seed)
    df[[dim1]] <- jitter(df[[dim1]], amount = jitter[1])
    if (length(jitter) > 1) {
      df[[dim2]] <- jitter(df[[dim2]], amount = jitter[2])
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
  g <- ggplot(df, aes_string(x = dim1,
                             y = dim2,
                             fill = "cluster",
                             label = "variable"))
  g <- g +
    geom_point(aes_string(color = "cluster",
                          shape = "cluster"),
               size = point_size)
  if (draw_polygons) {
    polygons <- lapply(unique(df$cluster), function(i) {
      df[df$cluster == i, ][grDevices::chull(x = df[df$cluster == i, ][[dim1]],
                                             y = df[df$cluster == i, ][[dim2]]), ]
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
                                  color = font_color,
                                  show.legend = FALSE)
    } else {
      g <- g +
        ggrepel::geom_text_repel(size = label_size,
                                 color = font_color,
                                 show.legend = FALSE)
    }
  }
  if (length(expand) > 0) {
    g <- g +
      scale_x_continuous(limits = c(min(df[[dim1]]) - expand[1],
                                    max(df[[dim1]]) + expand[1]))
    if (length(expand) > 1) {
      g <- g +
        scale_y_continuous(limits = c(min(df[[dim2]]) - expand[2],
                                      max(df[[dim2]]) + expand[2]))
    } else {
      expand[2] <- 0
    }
  } else {
    expand[1] <- 0
  }
  if (length(custom_colors) > 0) {
    g <- g +
      scale_color_manual(values = custom_colors) +
      scale_fill_manual(values = custom_colors)
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
    if (title == "auto") {
      if (what == "MDS") {
        title <- "Non-metric Multidimensional Scaling"
      } else if (what == "FA") {
        title <- "Factor analysis"
      }
    }
    g <- g +
      ggtitle(title)
  }
  if (stress & what == "MDS") {
    a <- data.frame(x = max(df[[dim1]]) + expand[1],
                    y = max(df[[dim2]]) + expand[2],
                    label = paste("Stress:", round(attributes(df)$stress, digits = 6)))
    g <- g +
      geom_text(data = a, aes(x = x, y = y, label = label),
                inherit.aes = FALSE, hjust = 1)
  }
  return(g)
}


#' Plots a dendrogram from dna_cluster objects
#'
#' Plots a dendrogram from objects derived via \link{dna_cluster}.
#'
#' This function is a convenience wrapper for several different dendrogram
#' types, which can be plotted using the \pkg{ggraph} package.
#'
#' @param clust A \code{dna_cluster} object created by the \link{dna_cluster}
#'   function.
#' @param shape The shape of the dendrogram. Available options are \code{elbows},
#'   \code{link}, \code{diagonal}, \code{arc}, and \code{fan}. See
#'   \link[ggraph]{layout_dendrogram_auto}.
#' @param activity Should activity of variable in \link{dna_cluster} be used to
#'   determine size of leaf_ends (logical). Activity means the number of
#'   statements which remained after duplicates were removed.
#' @param leaf_colors Determines which data is used to color the leafs of the
#'   dendrogram. Can be either \code{attribute1}, \code{attribute2}or \code{group}. Set to
#'   \code{character()} leafs-lines should not be colored.
#' @param colors There are three options from where to derive the colors in
#'   the plot: (1.) \code{identity} tries to use the names of variables as colors
#'   (e.g., if you retrieved the names as attribute from DNA), fails if names
#'   are not plottable colors; (2.) \code{manual} provide colors via
#'   custom_colors; (3.) \code{brewer} automatically select nice colors from a
#'   \code{RColorBrewer} palette (palettes can be set in custom_colors,
#'   defaults to \code{Set3}).
#' @param custom_colors Either provide enough colors to manually set the
#'   colors in the plot (if \code{colors = "manual"}) or select a palette from
#'   \code{RColorBrewer} (if \code{colors = "brewer"}).
#' @param branch_color Provide one color in which all branches are colored.
#' @param line_width Width of all lines.
#' @param line_alpha Alpha of all lines.
#' @param ends_size If \code{activity = FALSE}, the size of the lineend symbols
#'   can be set to one size for the whole plot.
#' @param leaf_ends Determines which data is used to color the leaf_ends of the
#'   dendrogram. Can be either \code{attribute1}, \code{attribute2} or \code{group}. Set to
#'   \code{character()} if no line ends should be displayed.
#' @param custom_shapes If shapes are provided, those are used for leaf_ends
#'   instead of the standard ones. Available shapes range from 0:25 and 32:127.
#' @param ends_alpha Alpha of all leaf_ends.
#' @param rectangles If a color is provided, this will draw rectangles in given
#'   color around the groups.
#' @param leaf_linetype,branch_linetype Determines which lines are used for
#'   leafs and branches. Takes \code{a} for straight line or \code{b} for dotted line.
#' @param font_size Set the font size for the entire plot.
#' @param theme See themes in \code{ggplot2}. The theme \code{bw} was customized to
#'   look best with dendrograms. Leave empty to use standard ggplot theme.
#'   customize the theme by adding \code{+ theme_*} after this function...
#' @param truncate Sets the number of characters to which labels should be
#'   truncated. Value \code{Inf} turns off truncation.
#' @param leaf_labels Either \code{ticks} to display the labels as axis ticks or
#'   \code{node} to label nodes directly. Node labels are also take the same color
#'   as the leaf the label.
#' @param circular Logical. Should the layout be transformed to a circular
#'   representation. See \link[ggraph]{layout_dendrogram_auto}.
#' @param show_legend Logical. Should a legend be displayed.
#' @param ... Not used. If you want to add more plot options use \code{+} and
#'   the ggplot2 logic (see example).
#'
#' @examples
#' dna_init()
#' conn <- dna_connection(dna_sample())
#' clust <- dna_cluster(conn)
#' dend <- dna_plotDendro(clust)
#'
#' # Flip plot with ggplot2 command
#' library("ggplot2")
#' dend + coord_flip()
#'
#' @author Johannes B. Gruber
#'
#' @export
#' @import ggraph
#' @importFrom stats as.dendrogram is.leaf dendrapply aggregate
dna_plotDendro <- function(clust,
                           shape = "elbows",
                           activity = FALSE,
                           leaf_colors = "attribute1",
                           branch_color = "#636363",
                           colors = "identity",
                           custom_colors = character(),
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
  dots <- list(...)
  if ("leaf_colours" %in% names(dots)) {
    leaf_colors <- dots[["leaf_colours"]]
  }
  if ("branch_colour" %in% names(dots)) {
    branch_color <- dots[["branch_colour"]]
  }
  if ("colours" %in% names(dots)) {
    colors <- dots[["colours"]]
  }
  if ("custom_colours" %in% names(dots)) {
    custom_colors <- dots[["custom_colours"]]
  }

  # truncate lables
  clust$labels_short <- ifelse(nchar(clust$labels) > truncate,
                               paste0(gsub("\\s+$", "",
                                           strtrim(clust$labels, width = truncate)),
                                      "..."),
                               clust$labels)
  # format as dendrogram
  hierarchy <- stats::as.dendrogram(clust)
  # Add colors
  hierarchy <- stats::dendrapply(hierarchy, function(x) {
    if (stats::is.leaf(x)) {
      if (length(leaf_colors) > 0) {
        attr(x, "Color1") <- as.character(clust[[leaf_colors]][match(as.character(labels(x)),
                                                                     clust$labels)])
      } else {
        attr(x, "Color1") <- ""
      }
      if (length(leaf_ends) > 0) {
        attr(x, "Color2") <- as.character(clust[[leaf_ends]][match(as.character(labels(x)),
                                                                   clust$labels)])
      } else {
        attr(x, "Color2") <- ""
      }
      attr(x, "Activity") <- clust$activities[clust$order[match(as.character(labels(x)),
                                                                clust$labels)]]
      attr(x, "labels_short") <- clust$labels_short[match(as.character(labels(x)),
                                                          clust$labels)]
      attr(x, "linetype") <- leaf_linetype
    } else {
      if (length(leaf_colors) > 0) {
        attr(x, "Color1") <- branch_color
      } else {
        attr(x, "Color1") <- ""
      }
      if (length(leaf_ends) > 0) {
        attr(x, "Color2") <- branch_color
      } else {
        attr(x, "Color2") <- ""
      }
      attr(x, "Activity") <- 0
      attr(x, "labels_short") <- ""
      attr(x, "linetype") <- branch_linetype
    }
    attr(x, "edgePar") <- list(cols1 = attr(x, "Color1"),
                               cols2 = attr(x, "Color2"),
                               linetype = attr(x, "linetype"))
    attr(x, "nodePar") <- list(cols3 = attr(x, "Color1"),
                               cols4 = attr(x, "Color2"),
                               Activity = attr(x, "Activity"),
                               labels_short = attr(x, "labels_short"))
    x
  })
  # create dedrogram
  dg <- ggraph(graph = hierarchy,
               layout = "dendrogram",
               circular = circular)
  # Recode show_legend
  show_legend <- ifelse(show_legend, # ggplot wants this recoding for some reason
                        NA,
                        show_legend)
  # shape
  if (shape == "elbows") {
    dg <- dg +
      geom_edge_elbow(aes_string(color = "cols1",
                                 edge_linetype = "linetype"),
                      show.legend = show_legend,
                      width = line_width,
                      alpha = line_alpha)
  } else if (shape == "link") {
    dg <- dg +
      geom_edge_link(aes_string(color = "cols1",
                                edge_linetype = "linetype"),
                     show.legend = show_legend,
                     width = line_width,
                     alpha = line_alpha)
  } else if (shape == "diagonal") {
    dg <- dg +
      geom_edge_diagonal(aes_string(color = "cols1",
                                    edge_linetype = "linetype"),
                         show.legend = show_legend,
                         width = line_width,
                         alpha = line_alpha)
  } else if (shape == "arc") {
    dg <- dg +
      geom_edge_arc(aes_string(color = "cols1",
                               edge_linetype = "linetype"),
                    show.legend = show_legend,
                    width = line_width,
                    alpha = line_alpha)
  } else if (shape == "fan") {
    dg <- dg +
      geom_edge_fan(aes_string(color = "cols1",
                               edge_linetype = "linetype"),
                    show.legend = show_legend,
                    width = line_width,
                    alpha = line_alpha)
  }
  dg <- dg +
    scale_edge_linetype_discrete(guide = "none")
  if (length(leaf_colors) > 0) {
    clust[[leaf_colors]] <- as.factor(clust[[leaf_colors]])
    autoCols <- c(branch_color, levels(clust[[leaf_colors]]))
    if (is.na(show_legend)) {
      guide <- "legend"
      if (leaf_colors == "attribute1") guidename <- attr(clust, "colors")[1]
      if (leaf_colors == "attribute2") guidename <- attr(clust, "colors")[2]
      if (leaf_colors == "group") guidename <- "group"
      guidename <- paste0(toupper(substr(guidename, 1, 1)),
                          substr(guidename, 2, nchar(guidename)))
    } else {
      guide <- "none"
      guidename <- waiver()
    }
  }
  if (colors == "identity" & length(leaf_colors) > 0) {
    autoCols <- setNames(autoCols, nm = c(branch_color, levels(clust[[leaf_colors]])))
    dg <- dg +
      scale_edge_color_manual(breaks = autoCols[-1],
                              values = autoCols,
                              guide = guide,
                              name = guidename)
  } else if (colors == "manual" & length(leaf_colors) > 0) {
    manCols <- c(branch_color, custom_colors)
    manCols <- setNames(manCols, nm = c(branch_color, levels(clust[[leaf_colors]])))
    dg <- dg +
      scale_edge_color_manual(breaks = autoCols[-1],
                              values = manCols,
                              guide = guide,
                              name = guidename)
  } else if (colors == "brewer" & length(leaf_colors) > 0) {
    if (length(custom_colors) == 0) {
      custom_colors <- "Set3"
    }
    brewCols <- c(branch_color,
                  scales::brewer_pal(type = "div",
                                     palette = custom_colors)(length(levels(clust[[leaf_colors]]))))
    brewCols <- setNames(brewCols, nm = c(branch_color, levels(clust[[leaf_colors]])))
    dg <- dg +
      scale_edge_color_manual(breaks = autoCols[-1],
                              values = brewCols,
                              guide = guide,
                              name = guidename)
  }
  if (length(leaf_colors) == 0) {
    dg <- dg +
      scale_edge_color_manual(values = "black", guide = "none")
  }
  # theme
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
  # labels
  if (leaf_labels == "ticks") {
    dg <- dg +
      scale_x_continuous(breaks = seq(0, length(clust$labels) - 1, by = 1),
                         labels = clust$labels_short[clust$order])
  } else if (leaf_labels == "nodes") {
    if (circular == FALSE) {
      dg <- dg +
        geom_node_text(aes_string(label = "labels_short",
                                  filter = "leaf",
                                  color = "cols3"),
                       angle = 270,
                       hjust = 0,
                       nudge_y = -0.02,
                       size = (font_size / .pt),
                       show.legend = FALSE) +
        expand_limits(y = c(-2.3, 2.3))
      #circular plots
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
                           color = cols3),
                       size = (font_size / .pt),
                       show.legend = FALSE) +
        expand_limits(x = c(-2.3, 2.3), y = c(-2.3, 2.3))
    }
  }
  # line ends
  if (length(leaf_ends) > 0) {
    if (is.na(show_legend)) {
      guide <- "legend"
      if (leaf_ends == "attribute1") legendname <- attr(clust, "colors")[1]
      if (leaf_ends == "attribute2") legendname <- attr(clust, "colors")[2]
      if (leaf_ends == "group") legendname <- "group"
      legendname <- paste0(toupper(substr(legendname, 1, 1)),
                           substr(legendname, 2, nchar(legendname)))
    } else {
      legendname <- waiver()
    }
    if (activity) {
      dg <- dg +
        geom_node_point(aes_string(filter = "leaf",
                                   color = "cols3",
                                   size = "Activity",
                                   shape = "cols4"),
                        show.legend = show_legend,
                        alpha = ends_alpha)
    } else {
      dg <- dg +
        geom_node_point(aes_string(filter = "leaf",
                                   color = "cols3",
                                   shape = "cols4"),
                        size = ends_size,
                        show.legend = show_legend,
                        alpha = ends_alpha)
    }
    # custom_shapes
    if (length(custom_shapes) > 0) {
      dg <- dg +
        scale_shape_manual(values = custom_shapes,
                           name = legendname)
    } else {
      dg <- dg +
        scale_shape_discrete(name = legendname)
    }
  }
  # rectangles
  if (length(rectangles) > 0 & !circular) {
    rect <- data.frame(label = clust$labels_short[clust$order],
                       cluster = clust$group[clust$order],
                       y = min(clust$height),
                       x = seq_along(clust$labels_short) - 1)
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
  # color node text and points
  if (length(leaf_colors) > 0) {
    if (colors == "identity") {
      dg <- dg +
        scale_color_identity(guide = "none")
    } else if (colors == "manual") {
      dg <- dg +
        scale_color_manual(values = manCols[-1],
                           guide = guide,
                           name = guidename)
    } else if (colors == "brewer") {
      dg <- dg +
        scale_color_manual(values = brewCols[-1],
                           guide = guide,
                           name = guidename)
    }
  }else {
    dg <- dg +
      scale_color_manual(values = "black",
                         guide = "none",
                         name = waiver())
  }
  return(dg)
}

#' Create a cluster dendrogram for a DNA database
#'
#' Create a cluster dendrogram based on a DNA database.
#'
#' This function serves to conduct a cluster analysis of a DNA dataset and
#' visualize the results as a dendrogram. The user can either select a specific
#' clustering method or let the \code{\link{dna_multiclust}} function determine
#' the best cluster solution. The following clustering methods are available for
#' creating dendrograms:
#' \itemize{
#'  \item Hierarchical clustering with single linkage.
#'  \item Hierarchical clustering with average linkage.
#'  \item Hierarchical clustering with complete linkage.
#'  \item Hierarchical clustering with Ward's algorithm.
#'  \item Fast & greedy community detection.
#'  \item Walktrap community detection.
#'  \item Leading eigenvector community detection.
#'  \item Edge betweenness community detection (Girvan-Newman algorithm).
#' }
#' The resulting dendrograms can have different label, leaf, and symbol
#' properties as well as rectangles for the clusters and other customization
#' options. It is possible to return the underlying \code{\link{dna_multiclust}}
#' object instead of the plot by using the \code{return.multiclust} argument.
#'
#' @inheritParams dna_multiclust
#' @inheritParams dna_network
#' @param method This argument represents the clustering method to be used for
#'   the dendrogram. Only hierarchical clustering methods are compatible with
#'   dendrograms. The following values are permitted:
#'   \describe{
#'     \item{"best"}{Automatically choose the best clustering method with a given
#'       number of clusters \code{k} (or between 2 and \code{k.max} if
#'       \code{k = 0}). The selection is based on network modularity of a given
#'       cluster solution in the subtract network.}
#'     \item{"single"}{Hierarchical clustering with single linkage.}
#'     \item{"average"}{Hierarchical clustering with average linkage.}
#'     \item{"complete"}{Hierarchical clustering with complete linkage.}
#'     \item{"ward"}{Hierarchical clustering with Ward's algorithm.}
#'     \item{"fastgreedy"}{Fast & greedy community detection.}
#'     \item{"walktrap"}{Walktrap community detection.}
#'     \item{"leading_eigen"}{Leading eigenvector community detection.}
#'     \item{"edge_betweenness"}{Edge betweenness community detection
#'       (Girvan-Newman algorithm).}
#'   }
#' @param k If \code{method = "best"} is selected, \code{k} is used to determine
#'   at which level the respective clustering method works best. For example, if
#'   \code{k = 3} is supplied, the algorithm will compare the modularity of the
#'   different cluster solutions with three clusters each time to determine the
#'   best-fitting cluster solution. If \code{k = 0} is supplied (the default),
#'   all solutions between one and \code{k.max} clusters will be attempted. The
#'   \code{k} argument also determines the number of rectangles that are drawn
#'   if the \code{rectangle.colors} argument is used. If \code{k = 0}, the
#'   actual number of clusters that works best will be used instead.
#' @param k.max The maximal number of clusters to try if \code{method = "best"}
#'   is used.
#' @param rectangle.colors If \code{NULL}, no rectangles are drawn. If a single
#'   color is provided (for example, \code{"purple"} or \code{"#AA9900"}),
#'   \code{k} rectangles (one per cluster) will be drawn, and they will all be
#'   in the same color. If \code{k} colors are provided as a vector, each
#'   rectangle will be in a separate color. If \code{rectangle.colors =
#'   "cluster"}, different colors will be picked for the rectangles based on
#'   cluster membership.
#' @param labels Which labels to use for variable 1. These can be:
#'   \describe{
#'     \item{"value"}{The actual variable values for \code{variable1} as extracted
#'       from the "value" column in a \code{\link{dna_getAttributes}} call.}
#'     \item{"color"}{The color designated for \code{variable1} in DNA, as
#'       extracted from the "color" column in a \code{\link{dna_getAttributes}}
#'       call.}
#'     \item{"type"}{The type designated for \code{variable1} in DNA, as
#'       extracted from the "type" column in a \code{\link{dna_getAttributes}}
#'       call.}
#'     \item{"alias"}{The alias designated for \code{variable1} in DNA, as
#'       extracted from the "alias" column in a \code{\link{dna_getAttributes}}
#'       call.}
#'     \item{"notes"}{The notes designated for \code{variable1} in DNA, as
#'       extracted from the "notes" column in a \code{\link{dna_getAttributes}}
#'       call.}
#'     \item{a vector of \code{character} objects}{A vector with as many labels
#'       as there are rows in a \code{\link{dna_getAttributes}} call, in the
#'       same order (i.e., replacing the original labels that are in
#'       alphabetical order).}
#'   }
#' @param label.colors Colors for the labels. Numbers, string colors, or
#'   hexadecimal strings are allowed. The following values are permitted:
#'   \describe{
#'     \item{a single color}{A single color for all labels.}
#'     \item{a vector of \code{k} colors}{A separate color for the labels in
#'       each cluster.}
#'     \item{a vector of as many colors as labels}{A separate color for each
#'       single label.}
#'     \item{"cluster"}{A separate color is chosen automatically for the labels
#'       in each cluster. There are \code{k} different colors.}
#'     \item{"color"}{The colors stored in the "color" column in the attribute
#'       manager of the DNA database are used, as extracted using the
#'       \code{\link{dna_getAttributes}} function.}
#'     \item{"type"}{A separate color for each value in the "type" column in the
#'       attribute manager of DNA is used.}
#'     \item{"alias"}{A separate color for each value in the "alias" column in
#'       the attribute manager of DNA is used.}
#'     \item{"notes"}{A separate color for each value in the "notes" column in
#'       the attribute manager of DNA is used.}
#'   }
#' @param label.size Font size for the labels.
#' @param label.truncate Number of characters to retain for each label. If all
#'   characters should be kept, \code{Inf} can be set.
#' @param leaf.shape The way the dendrogram leaves are drawn. The following
#'   values are permitted:
#'   \itemize{
#'     \item "elbow"
#'     \item "link"
#'     \item "diagonal"
#'     \item "arc"
#'     \item "fan"
#'   }
#' @param leaf.colors The colors of the leaves in the dendrogram. The same
#'   values as in the \code{label.colors} argument are permitted (see the
#'   description above for details).
#' @param leaf.width The line width of the leaves.
#' @param leaf.alpha The opacity of the leaves and symbols (between 0 and 1).
#' @param symbol.shapes The shapes of the leaf end symbols for each leaf. The
#'   following values are permitted:
#'   \describe{
#'     \item{a single integer}{A single \code{pch} symbol to use for all leaves,
#'       for example \code{2} or \code{19}.}
#'     \item{an integer vector with \code{k} elements}{A vector of separate
#'       \code{pch} symbol integer values for the labels in each cluster. For
#'       example, if there are three clusters, this could be \code{1:3}.}
#'     \item{an integer vector with as many elements as symbols}{A vector of
#'       separate \code{pch} values for each value in \code{variable1}, in the
#'       order as the rows produced by \code{\link{dna_getAttributes}} (i.e.,
#'       alphabetical label order).}
#'     \item{"cluster"}{A separate symbol is automatically selected for each
#'       cluster.}
#'     \item{"color"}{A separate symbol is automatically selected for each color
#'       saved in the "color" column of the output of
#'       \code{\link{dna_getAttributes}}, which represents the colors in the
#'       attribute manager in DNA.}
#'     \item{"type"}{A separate symbol is automatically selected for each type
#'       saved in the "type" column of the output of
#'       \code{\link{dna_getAttributes}}, which represents the types in the
#'       attribute manager in DNA.}
#'     \item{"alias"}{A separate symbol is automatically selected for each alias
#'       saved in the "alias" column of the output of
#'       \code{\link{dna_getAttributes}}, which represents the aliases in the
#'       attribute manager in DNA.}
#'     \item{"notes"}{A separate symbol is automatically selected for each note
#'       saved in the "notes" column of the output of
#'       \code{\link{dna_getAttributes}}, which represents the notes in the
#'       attribute manager in DNA.}
#'   }
#' @param symbol.colors The colors of the symbols at the leaf ends. The same
#'   values are permitted as in the \code{label.colors} argument (see the
#'   description there).
#' @param symbol.sizes The sizes of the symbols at the leaf ends. The default
#'   value is \code{5} for all symbols. Instead of a single number, a vector of
#'   \code{k} values (one for each cluster) or a vector with as many values
#'   as there are leafs or labels can be supplied.
#' @param circular Draw a dendrogram with a circular layout?
#' @param theme The theme to be used. See \code{\link[ggplot2]{ggtheme}} for
#'   details. Permitted values are:
#'   \itemize{
#'     \item "bw"
#'     \item "classic"
#'     \item "gray"
#'     \item "dark"
#'     \item "light"
#'     \item "minimal"
#'   }
#' @param caption Add a caption with details at the bottom of the plot? The
#'   details include the clustering method, the number of clusters, and the
#'   modularity value given the number of clusters with this method.
#' @param return.multiclust Instead of returning a \code{ggplot2} plot, return
#'   the \code{dna_multiclust} object upon which the dendrogram is based?
#' @return A \code{ggplot2} plot.
#'
#' @author Philip Leifeld, Johannes B. Gruber
#'
#' @examples
#' \dontrun{
#' library("rDNA")
#' dna_init()
#' samp <- dna_sample()
#' conn <- dna_connection(samp)
#'
#' # Single-linkage with k = 2 is chosen automatically based on modularity:
#' dna_dendrogram(conn, method = "best", k = 0)
#'
#' # Walktrap community detection with three clusters and rectangles:
#' dna_dendrogram(conn, method = "walktrap", k = 3, rectangle.colors = "purple")
#'
#' # Custom colors and shapes:
#' dna_dendrogram(conn,
#'                label.colors = "color",
#'                leaf.colors = "cluster",
#'                rectangle.colors = c("steelblue", "orange"),
#'                symbol.shapes = 17:18,
#'                symbol.colors = 3:4)
#'
#' # Circular dendrogram:
#' dna_dendrogram(conn, circular = TRUE, label.truncate = 12)
#'
#' # Modifying the underlying network, e.g., leaving out concepts:
#' dna_dendrogram(conn, excludeValues = list(concept =
#'   "There should be legislation to regulate emissions."))
#'
#' # Return the dna_multiclust object
#' mc <- dna_dendrogram(conn, k = 0, method = "best", return.multiclust = TRUE)
#' mc
#' }
#'
#' @import ggraph
#' @importFrom ggplot2 .pt aes aes_string element_text expand_limits labs theme
#'   theme_bw theme_classic theme_dark theme_grey theme_light theme_minimal
#'   scale_color_identity scale_x_continuous scale_shape_identity
#'   scale_size_identity waiver
#' @importFrom stats aggregate as.dendrogram as.hclust dendrapply is.leaf
#'   order.dendrogram
#' @export
dna_dendrogram <- function(connection,
                           statementType = "DNA Statement",
                           variable1 = "organization",
                           variable1Document = FALSE,
                           variable2 = "concept",
                           variable2Document = FALSE,
                           qualifier = "agreement",
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
                           method = "best",
                           k = 0,
                           k.max = 5,
                           rectangle.colors = NULL,
                           labels = "value",
                           label.colors = "color",
                           label.size = 12,
                           label.truncate = 30,
                           leaf.shape = "elbow",
                           leaf.colors = label.colors,
                           leaf.width = 1,
                           leaf.alpha = 1,
                           symbol.shapes = 19,
                           symbol.colors = label.colors,
                           symbol.sizes = 5,
                           circular = FALSE,
                           theme = "bw",
                           caption = TRUE,
                           return.multiclust = FALSE) {

  cl <- dna_multiclust(connection,
                       statementType = statementType,
                       variable1 = variable1,
                       variable1Document = variable1Document,
                       variable2 = variable2,
                       variable2Document = variable2Document,
                       qualifier = qualifier,
                       duplicates = duplicates,
                       start.date = start.date,
                       stop.date = stop.date,
                       start.time = start.time,
                       stop.time = stop.time,
                       timewindow = "no",
                       windowsize = 100,
                       excludeValues = excludeValues,
                       excludeAuthors = excludeAuthors,
                       excludeSources = excludeSources,
                       excludeSections = excludeSections,
                       excludeTypes = excludeTypes,
                       invertValues = invertValues,
                       invertAuthors = invertAuthors,
                       invertSources = invertSources,
                       invertSections = invertSections,
                       invertTypes = invertTypes,
                       saveObjects = TRUE,
                       k = k,
                       k.max = k.max,
                       single = ifelse(method %in% c("best", "single"), TRUE, FALSE),
                       average = ifelse(method %in% c("best", "average"), TRUE, FALSE),
                       complete = ifelse(method %in% c("best", "complete"), TRUE, FALSE),
                       ward = ifelse(method %in% c("best", "ward"), TRUE, FALSE),
                       kmeans = FALSE, # not a hierarchical method
                       pam = FALSE, # not a hierarchical method
                       equivalence = FALSE, # not implemented as a hierarchical method
                       concor_one = FALSE, # not implemented as a hierarchical method
                       concor_two = FALSE, # not implemented as a hierarchical method
                       louvain = FALSE, # not a hierarchical method
                       fastgreedy = ifelse(method %in% c("best", "fastgreedy"), TRUE, FALSE),
                       walktrap = ifelse(method %in% c("best", "walktrap"), TRUE, FALSE),
                       leading_eigen = ifelse(method %in% c("best", "leading_eigen"), TRUE, FALSE),
                       edge_betweenness = ifelse(method %in% c("best", "edge_betweenness"), TRUE, FALSE),
                       infomap = FALSE, # not a hierarchical method
                       label_prop = FALSE, # not a hierarchical method
                       spinglass = FALSE, # not a hierarchical method
                       verbose = FALSE)
  if (isTRUE(return.multiclust)) {
    return(cl)
  }
  mod <- cl$max_mod$max_mod
  meth <- cl$max_mod$max_method
  mem <- cl$memberships[cl$memberships$i == 1 & cl$memberships$method == meth, 3:4]
  hierarchy <- stats::as.dendrogram(cl$cl[[1]])
  at <- dna_getAttributes(connection = connection, statementType = statementType, variable = variable1, values = mem$node)

  # prepare labels
  if (is.null(labels) || is.na(labels)) {
    labels <- mem$node
  } else if (!is.character(labels) || !length(labels) %in% c(1, nrow(mem), length(unique(mem$cluster)))) {
    labels <- mem$node
    warning("'labels' must be a character vector with one name for each ", variable1, ". Using default names.")
  } else if (length(labels) == 1) {
    if (labels == "cluster") {
      labels <- as.character(mem$cluster)
    } else if (labels == "value") {
      labels <- mem$node
    } else if (labels == "color") {
      labels <- at$color
    } else if (labels == "type") {
      labels <- at$type
    } else if (labels == "alias") {
      labels <- at$alias
    } else if (labels == "notes") {
      labels <- at$notes
    } else {
      labels <- rep(labels, nrow(mem))
    }
  } else if (length(labels) == length(unique(mem$cluster))) {
    labels <- labels[mem$cluster]
  }

  # truncate labels
  labels_short <- ifelse(nchar(labels) > label.truncate,
                         paste0(gsub("\\s+$",
                                     "",
                                     strtrim(labels, width = label.truncate)),
                                "..."),
                         labels)

  # prepare label colors
  if (is.null(label.colors) || is.na(label.colors)) {
    label.colors <- at$color
  } else if (!length(label.colors) %in% c(1, nrow(mem), length(unique(mem$cluster)))) {
    label.colors <- at$color
    warning("'label.colors' must be a vector with one color for each ", variable1, ". Using default attribute colors.")
  } else if (length(label.colors) == 1) {
    if (label.colors == "cluster") {
      label.colors <- col2hex(mem$cluster)
    } else if (label.colors == "color") {
      label.colors <- at$color
    } else if (label.colors == "type") {
      label.colors <- col2hex(at$type)
    } else if (label.colors == "alias") {
      label.colors <- col2hex(at$alias)
    } else if (label.colors == "notes") {
      label.colors <- col2hex(at$notes)
    } else {
      label.colors <- rep(col2hex(label.colors), nrow(mem))
    }
  } else if (length(label.colors) == length(unique(mem$cluster))) {
    label.colors <- col2hex(label.colors)[mem$cluster]
  } else if (length(label.colors) == nrow(mem)) {
    label.colors <- col2hex(label.colors)
  }

  # prepare leaf colors
  if (is.null(leaf.colors) || is.na(leaf.colors)) {
    leaf.colors <- at$color
  } else if (!length(leaf.colors) %in% c(1, nrow(mem), length(unique(mem$cluster)))) {
    leaf.colors <- at$color
    warning("'leaf.colors' must be a vector with one color for each ", variable1, ". Using default attribute colors.")
  } else if (length(leaf.colors) == 1) {
    if (leaf.colors == "cluster") {
      leaf.colors <- col2hex(mem$cluster)
    } else if (leaf.colors == "color") {
      leaf.colors <- at$color
    } else if (leaf.colors == "type") {
      leaf.colors <- col2hex(at$type)
    } else if (leaf.colors == "alias") {
      leaf.colors <- col2hex(at$alias)
    } else if (leaf.colors == "notes") {
      leaf.colors <- col2hex(at$notes)
    } else {
      leaf.colors <- rep(col2hex(leaf.colors), nrow(mem))
    }
  } else if (length(leaf.colors) == length(unique(mem$cluster))) {
    leaf.colors <- col2hex(leaf.colors)[mem$cluster]
  } else if (length(leaf.colors) == nrow(mem)) {
    leaf.colors <- col2hex(leaf.colors)
  }

  # prepare symbol colors
  if (is.null(symbol.colors) || is.na(symbol.colors)) {
    symbol.colors <- at$color
  } else if (!length(symbol.colors) %in% c(1, nrow(mem), length(unique(mem$cluster)))) {
    symbol.colors <- at$color
    warning("'symbol.colors' must be a vector with one color for each ", variable1, ". Using default attribute colors.")
  } else if (length(symbol.colors) == 1) {
    if (symbol.colors == "cluster") {
      symbol.colors <- col2hex(mem$cluster)
    } else if (symbol.colors == "color") {
      symbol.colors <- at$color
    } else if (symbol.colors == "type") {
      symbol.colors <- col2hex(at$type)
    } else if (symbol.colors == "alias") {
      symbol.colors <- col2hex(at$alias)
    } else if (symbol.colors == "notes") {
      symbol.colors <- col2hex(at$notes)
    } else {
      symbol.colors <- rep(col2hex(symbol.colors), nrow(mem))
    }
  } else if (length(symbol.colors) == length(unique(mem$cluster))) {
    symbol.colors <- col2hex(symbol.colors)[mem$cluster]
  } else if (length(symbol.colors) == nrow(mem)) {
    symbol.colors <- col2hex(symbol.colors)
  }

  # prepare symbol sizes
  if (is.null(symbol.sizes) || is.na(symbol.sizes)) {
    symbol.sizes <- rep(5, length(symbol.colors))
  } else if (!length(symbol.sizes) %in% c(1, nrow(mem), length(unique(mem$cluster)))) {
    symbol.sizes <- rep(5, length(symbol.colors))
    warning("'symbol.sizes' must be a vector with one symbol size for each ", variable1, ". Using default value of 5.")
  } else if (length(symbol.sizes) == 1) {
    symbol.sizes <- rep(symbol.sizes, nrow(mem))
  } else if (length(symbol.sizes) == length(unique(mem$cluster))) {
    symbol.sizes <- symbol.sizes[mem$cluster]
  } else if (length(symbol.sizes) == nrow(mem)) {
    # keep them as they are
  }

  # prepare symbol shapes
  if (is.null(symbol.shapes) || is.na(symbol.shapes)) {
    symbol.shapes <- rep(19, length(symbol.colors))
  } else if (!length(symbol.shapes) %in% c(1, nrow(mem), length(unique(mem$cluster)))) {
    symbol.shapes <- rep(19, length(symbol.colors))
    warning("'symbol.shapes' must be a vector with one symbol size for each ", variable1, ". Using default value of 19.")
  } else if (length(symbol.shapes) == 1) {
    if (symbol.shapes == "cluster") {
      if (length(unique(mem$cluster)) < 5) { # use opaque symbols (but there are only four suitable shapes)
        symbol.shapes <- mem$cluster
        symbol.shapes[mem$cluster == 1] <- 19
        symbol.shapes[mem$cluster == 2] <- 15
        symbol.shapes[mem$cluster == 3] <- 17
        symbol.shapes[mem$cluster == 4] <- 18
      } else {
        symbol.shapes <- mem$cluster - 1 # use the full range of pch symbols, which start with 0
      }
    } else if (symbol.shapes %in% c("color", "type", "alias", "notes")) {
      if (symbol.shapes == "color") {
        values <- sapply(at$color, function(x) which(sort(unique(at$color)) == x))
      } else if (symbol.shapes == "type") {
        values <- sapply(at$type, function(x) which(sort(unique(at$type)) == x))
      } else if (symbol.shapes == "alias") {
        values <- sapply(at$alias, function(x) which(sort(unique(at$alias)) == x))
      } else if (symbol.shapes == "notes") {
        values <- sapply(at$notes, function(x) which(sort(unique(at$notes)) == x))
      }
      if (length(unique(values)) < 5) { # use opaque symbols (but there are only four suitable shapes)
        symbol.shapes <- numeric(length(values))
        symbol.shapes[values == 1] <- 19
        symbol.shapes[values == 2] <- 15
        symbol.shapes[values == 3] <- 17
        symbol.shapes[values == 4] <- 20
      } else {
        symbol.shapes <- values - 1 # use the full range of pch symbols, which start with 0
      }
    } else {
      symbol.shapes <- rep(symbol.shapes, nrow(mem))
    }
  } else if (length(symbol.shapes) == length(unique(mem$cluster))) {
    symbol.shapes <- symbol.shapes[mem$cluster]
  } else if (length(symbol.shapes) == nrow(mem)) {
    # keep them as they are
  }

  # prepare rectangle colors
  if (is.null(rectangle.colors) || is.na(rectangle.colors)) {
    rectangle.colors <- NULL
  } else if (!length(rectangle.colors) %in% c(1, length(unique(mem$cluster)))) {
    rectangle.colors <- rep(col2hex("red"), length(unique(mem$cluster)))
    warning("'rectangle.colors' must be a vector with one color for each cluster or just a single color.")
  } else if (length(rectangle.colors) == 1) {
    if (rectangle.colors == "cluster") {
      rectangle.colors <- col2hex(sort(unique(mem$cluster)))
    } else {
      rectangle.colors <- rep(col2hex(rectangle.colors), length(unique(mem$cluster)))
    }
  } else if (length(rectangle.colors) == length(unique(mem$cluster))) {
    # keep colors as they are
  }

  # add labels, colors etc. to the dendrogram
  hierarchy <- stats::dendrapply(hierarchy, function(node) {
    index <- which(mem$node == attributes(node)$label)
    if (stats::is.leaf(node)) {
      attr(node, "label") <- labels_short[index]
      attr(node, "label_color") <- label.colors[index]
    } else {
      attr(node, "label_color") <- "#636363"
    }
    l <- list(label_long = attr(node, "label"),
              label_short = ifelse(is.leaf(node), labels_short[index], NA),
              symbol_color = ifelse(is.leaf(node), symbol.colors[index], NA),
              leaf_color = ifelse(is.leaf(node), leaf.colors[index], "#636363"),
              label_color = ifelse(is.leaf(node), label.colors[index], NA),
              symbol_size = ifelse(is.leaf(node), symbol.sizes[index], NA),
              symbol_shape = ifelse(is.leaf(node), symbol.shapes[index], NA))
    attr(node, "nodePar") <- l
    attr(node, "edgePar") <- l
    node
  })

  # prepare basic dendrogram plot
  dg <- ggraph(graph = hierarchy,
               layout = "dendrogram",
               circular = circular)

  # add stems and leaves to the plot
  if (is.null(leaf.shape) || is.na(leaf.shape) || length(leaf.shape) != 1 || !is.character(leaf.shape)) {
    stop("'leaf.shape' can take the values 'elbow', 'link', 'diagonal', 'arc', or 'fan'.")
  } else if (leaf.shape == "elbow") {
    dg <- dg + geom_edge_elbow(aes(color = leaf_color),
                               width = leaf.width,
                               alpha = leaf.alpha)
  } else if (leaf.shape == "link") {
    dg <- dg + geom_edge_link(aes(color = leaf_color),
                              width = leaf.width,
                              alpha = leaf.alpha)
  } else if (leaf.shape == "diagonal") {
    dg <- dg + geom_edge_diagonal(aes(color = leaf_color),
                                  width = leaf.width,
                                  alpha = leaf.alpha)
  } else if (leaf.shape == "arc") {
    dg <- dg + geom_edge_arc(aes(color = leaf_color),
                             width = leaf.width,
                             alpha = leaf.alpha)
  } else if (leaf.shape == "fan") {
    dg <- dg + geom_edge_fan(aes(color = leaf_color),
                             width = leaf.width,
                             alpha = leaf.alpha)
  } else {
    stop("'leaf.shape' can take the values 'elbow', 'link', 'diagonal', 'arc', or 'fan'.")
  }

  # replace colors
  dg <- dg + scale_edge_linetype_discrete(guide = "none")
  leaf.colors <- as.factor(leaf.colors)
  autoCols <- c("#636363", levels(leaf.colors))
  guide <- "none"
  guidename <- waiver()
  autoCols <- setNames(autoCols, nm = c("#636363", levels(leaf.colors)))
  dg <- dg + scale_edge_color_manual(breaks = autoCols[-1],
                                     values = autoCols,
                                     guide = guide,
                                     name = guidename)

  # theme and label orientation
  if (theme == "bw") {
    dg <- dg + theme_bw()
  } else if (theme == "light") {
    dg <- dg + theme_light()
  } else if (theme %in% c("grey", "gray")) {
    dg <- dg + theme_grey()
  } else if (theme == "dark") {
    dg <- dg + theme_dark()
  } else if (theme == "minimal") {
    dg <- dg + theme_minimal()
  } else if (theme == "classic") {
    dg <- dg + theme_classic()
  }
  if (isTRUE(circular)) {
    dg <- dg +
      theme(panel.border = element_blank(),
            axis.title = element_blank(),
            panel.grid.major = element_blank(),
            panel.grid.minor = element_blank(),
            text = element_text(size = label.size),
            axis.line = element_blank(),
            axis.ticks = element_blank(),
            axis.text = element_blank())
  } else {
    dg <- dg +
      theme(panel.border = element_blank(),
            axis.title = element_blank(),
            panel.grid.major = element_blank(),
            panel.grid.minor = element_blank(),
            text = element_text(size = label.size),
            axis.line = element_blank(),
            axis.ticks.x = element_blank(),
            axis.text.x = element_text(angle = 90, vjust = 0.5, hjust = 1))
  }

  # labels
  if (isTRUE(circular)) {
    dg <- dg +
      geom_node_text(aes(filter = leaf,
                         angle = ifelse(node_angle(x, y) < 270 & node_angle(x, y) > 90, node_angle(x, y) + 180, node_angle(x, y)),
                         label = label_short,
                         hjust = ifelse(node_angle(x, y) < 270 & node_angle(x, y) > 90, 1.05, -0.05),
                         color = label_color),
                     size = (label.size / .pt),
                     show.legend = FALSE) +
      expand_limits(x = c(-2.3, 2.3), y = c(-2.3, 2.3))
  } else {
    dg <- dg +
      scale_x_continuous(breaks = seq(0, length(labels) - 1, by = 1),
                         labels = labels_short[order.dendrogram(hierarchy)]) +
      theme(axis.text.x = element_text(colour = label.colors[order.dendrogram(hierarchy)]))
  }

  # caption
  if (isTRUE(caption)) {
    dg <- dg + labs(caption = paste0("Method: ", meth, ". Modularity: ", round(mod, 3), " at k = ", length(unique(mem$cluster)), "."))
  }

  # symbols (line ends)
  dg <- dg +
    geom_node_point(aes_string(filter = "leaf",
                               color = "symbol_color",
                               size = "symbol_size",
                               shape = "symbol_shape"),
                    alpha = leaf.alpha) +
    scale_shape_identity() +
    scale_size_identity() +
    scale_color_identity()

  # rectangles
  if (!circular && !is.null(rectangle.colors)) {
    h <- stats::as.hclust(hierarchy)$height
    y_offset <- h[length(h)] / 30
    rect <- data.frame(label = labels_short[order.dendrogram(hierarchy)],
                       cluster = mem$clust[order.dendrogram(hierarchy)],
                       y = min(h),
                       x = seq_along(labels_short[order.dendrogram(hierarchy)]) - 1)
    rect <- stats::aggregate(x ~ cluster, rect, range)
    rect$xmin <- rect$x[, 1] - 0.25
    rect$xmax <- rect$x[, 2] + 0.25
    rect$ymax <- rep(h[length(h) - nrow(rect) + 1], nrow(rect)) + y_offset
    rect$ymin <- -y_offset
    rect$color <- rectangle.colors
    dg <- dg +
      geom_rect(data = rect,
                aes_string(xmin = "xmin",
                           xmax = "xmax",
                           ymin = "ymin",
                           ymax = "ymax",
                           color = "color"),
                fill = NA)
  }

  return(dg)
}


#' Plots a heatmap from dna_cluster objects
#'
#' Plots a heatmap with dendrograms from objects derived via \link{dna_cluster}.
#'
#' This function plots a heatmap including dendrograms on the x- and y-axis of
#' the heatmap plot. The available options for coloring the tiles can be
#' displayed using \code{RColorBrewer::display.brewer.all()} (RColorBrewer needs
#' to be installed).
#'
#' @param clust A \code{dna_cluster} object created by the \link{dna_cluster}
#'   function.
#' @param truncate Sets the number of characters to which labels should be
#'   truncated. Value \code{Inf} turns off truncation.
#' @param values If TRUE, will display the values in the tiles of the heatmap.
#' @param colors There are two options: When \code{"brewer"} is selected, the function
#'   \link[ggplot2]{scale_fill_distiller} is used to color the heatmap tiles.
#'   When \code{"gradient"} is selected, \link[ggplot2]{scale_fill_gradient} will be
#'   used. The color palette and low/high values can be supplied using the
#'   argument \code{custom_colors}.
#' @param custom_colors For \code{colors = "brewer"} you can use either a
#'   string with a palette name or the index number of a brewer palette (see
#'   details). If \code{colors = "gradient"} you need to supply at least two
#'   values. Colors are then derived from a sequential color gradient palette.
#'   \link[ggplot2]{scale_fill_gradient}. If more than two colors are provided
#'   \link[ggplot2]{scale_fill_gradientn} is used instead.
#' @param square If TRUE, will make the tiles of the heatmap quadratic.
#' @param dendro_x If TRUE, will draw a dendrogram on the x-axis.
#' @param dendro_x_size,dendro_y_size Control the size of the dendrograms on the
#'   x- and y-axis.
#' @param qualifierLevels Takes a list with integer values of the qualifier
#'   levels (as characters) as names and character values as labels (See
#'   example).
#' @param show_legend Logical. Should a legend be displayed.
#' @param ... Additional arguments passed to \link{dna_plotDendro}.
#'
#' @examples
#' dna_init()
#' conn <- dna_connection(dna_sample())
#' clust <- dna_cluster(conn)
#' dend <- dna_plotHeatmap(clust,
#' qualifierLevels = list("0" = "no", "1" = "yes"))
#'
#' @author Johannes B. Gruber
#'
#' @export
#' @import ggplot2
#' @importFrom cowplot ggdraw insert_yaxis_grob insert_xaxis_grob
#' @importFrom reshape2 melt
dna_plotHeatmap <- function(clust,
                            truncate = 40,
                            values = FALSE,
                            colors = character(),
                            custom_colors = character(),
                            square = TRUE,
                            dendro_x = TRUE,
                            dendro_x_size = 0.2,
                            dendro_y_size = 0.2,
                            qualifierLevels = list("0" = "no",
                                                   "1" = "yes"),
                            show_legend = TRUE,
                            ...) {
  nw <- clust[["network"]]
  # construct column labels
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
  if (any(unlist(sapply(unique(pn), function(i) {
    duplicated(colnames(nw)[pn == i])
  })))) {
    warning(paste0("After truncation, some column labels are now exactly the same.",
                   "Those are followed by # + number now. Consider increasing the 'truncation' value."))
    colnames(nw) <- paste0("L", pn, colnames(nw))
    d <- grepl("\\...$", colnames(nw))
    colnames(nw) <- make.unique(sub("\\...$", "", colnames(nw)), sep = " #")
    colnames(nw)[duplicated(sub(" #[[:digit:]]$", "", colnames(nw)))] <-
      sapply(colnames(nw)[duplicated(sub(" #[[:digit:]]$", "", colnames(nw)))], function(i) {
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
  if (length(qualifierLevels) > 0) {
    for (l in seq_len(length(qualifierLevels))) {
      colnames(nw) <- gsub(paste0(names(qualifierLevels[l]), "$"),
                           qualifierLevels[l],
                           colnames(nw))
    }
  }
  # truncate row labels
  row.names(nw) <- trim(row.names(nw),
                        truncate)
  if (any(duplicated(row.names(nw)))) {
    warning(paste0("After truncation, some row labels are now exactly the same. Those are followed by",
                   " # + number now. Consider increasing the 'truncation' value."))
    row.names(nw) <- paste0(make.names(sub("...$", "", row.names(nw)), unique = TRUE), "...")
  }
  # re-construct clust objects
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
    if (dendro_x) {
      warning(paste0("The dendrogram on the x-axis of the ",
                     "dna_plotHeatmap cannot be made using \"",
                     args$clust.method,
                     "\". This dendro",
                     "gram is constructed using the method ",
                     "\"ward.D2\" instead."))
    }
    args$clust.method <- "ward.D2"
  }
  if (all(t(nw) %in% c(0, 1))) {
    d <- vegan::vegdist(t(nw), method = "jaccard")
  } else {
    d <- dist(t(nw), method = "euclidean")
  }
  dend_x <- hclust(d, method = args$clust.method)
  dend_x$activities <- unname(rowSums(t(nw)))
  # plot clust y
  dots <- list(...)
  if (!any(c("leaf_colors", "leaf_colors") %in% names(dots))) {
    dots <- c(dots,
              list(leaf_colors = character(),
                   theme = "void"))
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
  # plot clust x
  if (dendro_x) {
    plt_dendr_x <- do.call(dna_plotDendro,
                           c(list(clust = dend_x,
                                  leaf_labels = ""),
                             dots)) +
      scale_x_continuous(expand = c(0.0, 0.5, 0.0, 0.5))
  }
  ## heatmap
  df <- reshape2::melt(nw[dend_y$order, dend_x$order])
  df$posy <- seq_len(length(levels(df$Var1)))
  df$posx <- as.vector(sapply(seq_len(length(levels(df$Var2))),
                              rep,
                              length(levels(df$Var1))))
  plt_hmap <- ggplot(data = df, aes_string(x = "posx",
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
  ### display values
  if (values) {
    plt_hmap <- plt_hmap +
      geom_text(aes_string(label = "value"))
  }
  ## color heatmap
  if (length(colors) > 0) {
    if (colors == "brewer") {
      if (length(custom_colors) < 1) custom_colors <- 2
      plt_hmap <- plt_hmap +
        scale_fill_distiller(palette = custom_colors,
                             direction = 1)
    } else if (colors == "gradient") {
      if (length(custom_colors) < 1) {
        custom_colors <- c("gray", "blue")
      }
      plt_hmap <- plt_hmap +
        scale_fill_gradientn(colors = custom_colors)
    }
  }
  # merge plots
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
#' @param axis Takes the name of an attribute in DNA (i.e. \code{"id"},
#'   \code{"value"}, \code{"color"}, \code{"type"}, \code{"alias"},
#'   \code{"notes"} or \code{"frequency"}) or \code{"group"} to color nodes.
#'   The option \code{"group"} only makes sense if you provide group membership
#'   information to the \code{groups} argument.
#' @param sort_by Either the name of an attribute to sort by or a name vector
#'   with values to sort by. Possible values are \code{"frequency"},
#'   \code{"degree"} (to sort by in-degree of the actors) or \code{NULL} to
#'   place nodes sequentially. A named vector can be provided, with one value
#'   per actor (see example).
#' @param axis_label If \code{TRUE}, axis labels are plotted at the end of the
#'   axis and are removed from the legend.
#' @param axis_colors There are five options for coloring the axes: (1.)
#'   \code{"auto"} uses \code{"identity"} if \code{node_attribute = "color"} and
#'   leaves the standard ggplot2 colors otherwise; (2.) \code{"identity"} tries
#'   to use \code{axis} for colors (i.e., if you set \code{axis = "color"} or
#'   have provided a color name in another attribute field in DNA) but fails if
#'   names are not plottable colors; (3) \code{"manual"} lets you provide
#'   colors via custom_colors; (4.) \code{"brewer"} automatically selects nice
#'   colors from a \code{RColorBrewer} palette (palettes can be set in
#'   custom_colors); and (5.) \code{"single"} uses the first value in
#'   custom_colors for all axes
#' @param custom_colors Takes custom values to control the axes colors. The
#'   format of the necessary values depends on the setting of \code{axis}: When
#'   \code{axis = "manual"}, a character object containing the enough color
#'   names for all groups is needed; When \code{axis = "brewer"} you need to
#'   supply a a palette from \code{RColorBrewer} (otherwise defaults to "Set3");
#'   When \code{axis "single"} only a single color name is needed (defaults to
#'   "red").
#' @param edge_weight If \code{TRUE}, edge weights will be used to determine
#'   width of the lines between nodes. The minimum and maximum width can be
#'   controlled with \code{edge_size_range}.
#' @param edge_size_range Takes a numeric vector with two values: minimum and
#'   maximum \code{edge_weight}.
#' @param edge_color Provide the name of a color to use for edges.
#' @param edge_alpha Takes numeric values to control the alpha-transparency of
#'   edges. Possible values range from \code{0} (fully transparent) to \code{1}
#'   (fully visible).
#' @param node_label If \code{TRUE}, text is added next to nodes to label them.
#'   If "label", a rectangle is drawn underneath the text, often making it
#'   easier to read. If \code{FALSE} no lables are drawn.
#' @param label_repel Controls how far from the labels will be put from nodes.
#'   The exact position of text is random but overplotting is avoided.
#' @param label_lines If \code{TRUE}, draws lines between nodes and labels if
#'   labels are further away from nodes.
#' @param font_size Control the font size of the node labels.
#' @param theme Provide the name of a theme. Available options are
#'   \code{"graph"} (which is customized to look best with networks), "bw",
#'   "void", "light" and \code{"dark"}. Leave empty to use standard ggplot
#'   theme. Choose other themes or customize with tools from \link{ggplot2} by
#'   adding \code{+ theme_*} after this function.
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
#' @param ... Arguments passed on to \link[ggraph]{layout_igraph_hive}.
#'
#' @author Johannes B. Gruber
#'
#' @examples
#' \dontrun{
#' dna_init()
#' conn <- dna_connection(dna_sample())
#'
#' # Plot from one-mode network
#' nw <- dna_network(conn, networkType = "onemode")
#' dna_plotHive(nw)
#'
#' # Use custom sorting
#' sorting <- c("Alliance to Save Energy" = 1,
#'              "Energy and Environmental Analysis, Inc." = 2,
#'              "Environmental Protection Agency" = 3,
#'              "National Petrochemical & Refiners Association" = 4,
#'              "Senate" = 5,
#'              "Sierra Club" = 6,
#'              "U.S. Public Interest Research Group"= 7)
#'
#' dna_plotHive(nw, sort_by = sorting)
#'
#' # Use groups from dna_cluster
#' clust <- dna_cluster(conn, cutree.k = 2)
#' dna_plotHive(nw, axis = "group", groups = clust)
#'
#' # Use custom groups
#' groups <- c("Alliance to Save Energy" = "group 1",
#'             "Energy and Environmental Analysis, Inc." = "group 2",
#'             "Environmental Protection Agency" = "group 3",
#'             "National Petrochemical & Refiners Association" = "group 1",
#'             "Senate" = "group 2",
#'             "Sierra Club" = "group 3",
#'             "U.S. Public Interest Research Group"= "group 1")
#'
#' dna_plotHive(nw, axis = "group", groups = groups)
#' }
#'
#' @export
#' @import ggraph
#' @import igraph
#' @importFrom ggrepel geom_label_repel geom_text_repel
dna_plotHive <- function(x,
                         axis = "type",
                         sort_by = "degree",
                         axis_label = FALSE,
                         axis_colors = "auto",
                         custom_colors = character(),
                         edge_weight = TRUE,
                         edge_size_range = c(0.2, 2),
                         edge_color = "grey",
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
  dots <- list(...)
  if ("axis_colors" %in% names(dots)) {
    axis_colors <- dots[["axis_colors"]]
  }
  if ("edge_color" %in% names(dots)) {
    edge_color <- dots[["edge_color"]]
  }
  if ("node_colors" %in% names(dots)) {
    node_colors <- dots[["node_colors"]]
  }
  if ("custom_colors" %in% names(dots)) {
    custom_colors <- dots[["custom_colors"]]
  }

  layout <- "hive"
  # Make igraph
  if (any(class(x) %in% "dna_network_twomode")) {
    stop("Twomode networks are currently not allowed.")
  }
  graph <- dna_toIgraph(x)
  if (!is.null(threshold)) {
    graph <- delete.edges(graph, which(!E(graph)$weight >= threshold))
  }
  # color and attribute
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
  V(graph)$frequency <- as.character(att$frequency)[match(att$value, V(graph)$name)]
  if (axis == "group") {
    if (!length(groups) > 0) {
      groups <- rep("Group 0", length(V(graph)$name))
      names(groups) <- V(graph)$name
    } else if (any(grepl("list|character", class(groups)))) {
      groups <- groups[match(V(graph)$name, names(groups))]
    } else if (any(class(groups) %in% "dna_cluster")) {
      groups <- groups$group[match(V(graph)$name, groups$labels)]
    }
    node_attribute <- "Membership"
    V(graph)$attribute <- groups
  } else {
    V(graph)$attribute <- as.character(att[, axis])[match(att$value, V(graph)$name)]
    node_attribute <- paste0(toupper(substr(axis, 1, 1)),
                             substr(axis, 2, nchar(axis)))
  }
  if ((is.vector(sort_by, mode = "numeric") |
       is.vector(sort_by, mode = "character")) &
      length(sort_by) > 1) {
    V(graph)$sorting <- unname(sort_by[match(names(sort_by), V(graph)$name)])
    sort_by <- "sorting"
  }
  if (edge_weight) {
    E(graph)$Weight <- E(graph)$weight
  } else {
    E(graph)$Weight <- NULL
  }
  lyt <- create_layout(graph, layout = layout, axis = "attribute", sort.by = sort_by, ...)
  lyt$name_short <- trim(as.character(lyt$name), n = truncate)
  colnames(lyt) <- gsub("attribute", node_attribute, colnames(lyt))
  show_legend <- ifelse(show_legend, # ggplot wants this recoding for some reason
                        NA,
                        show_legend)
  g <- ggraph(lyt) +
    geom_edge_hive(aes_string(width = "Weight"),
                   alpha = edge_alpha,
                   color = edge_color,
                   show.legend = show_legend) +
    scale_edge_width(range = edge_size_range)
  if (axis_label) {
    yexp <- (max(lyt$y) - min(lyt$y)) / 4
    xexp <- (max(lyt$x) - min(lyt$x)) / 4
    g <- g +
      geom_axis_hive(aes_string(color = node_attribute),
                     size = 2,
                     label = TRUE,
                     show.legend = FALSE) +
      scale_y_continuous(expand = c(0, yexp, 0, yexp)) +
      scale_x_continuous(expand = c(0, xexp, 0, xexp))
  } else {
    g <- g +
      geom_axis_hive(aes_string(color = node_attribute),
                     size = 2,
                     label = FALSE,
                     show.legend = show_legend)
  }
  # add labels
  if ((is.logical(node_label) & node_label == TRUE) | node_label == "label") {
    if (node_label == "label") {
      g <- g +
        geom_label_repel(aes_string(x = "x", y = "y",
                                    label = "name_short"),
                         point.padding = label_repel,
                         box.padding = label_repel,
                         fontface = "bold",
                         size = font_size / .pt,
                         min.segment.length = ifelse(label_lines, 0.5, Inf))
    } else {
      g <- g +
        geom_text_repel(aes_string(x = "x", y = "y",
                                   label = "name_short"),
                        point.padding = label_repel,
                        box.padding = label_repel,
                        fontface = "bold",
                        size = font_size / .pt,
                        min.segment.length = ifelse(label_lines, 0.5, Inf))
    }
  }
  # theme
  if (theme == "graph") {
    g <- g +
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
  # colors
  if (axis_colors == "auto" & !node_attribute == "Membership") {
    test <- sapply(unique(att[, tolower(node_attribute)]), function(a) {
      length(unique(att$color[att[, tolower(node_attribute)] == a])) == 1
    })
    if (all(test)) {
      g <- g +
        scale_color_manual(values = unique(att$color)[order(unique(att[, tolower(node_attribute)]))])
    }
  } else {
    if (axis_colors == "identity") {
      g <- g +
        scale_color_identity()
    } else if (axis_colors == "manual" | axis_colors == "single") {
      if (length(custom_colors) == 0) {
        custom_colors <- "red"
      }
      if (axis_colors == "single") {
        custom_colors <- rep(custom_colors[1],
                             length.out = length(unique(lyt[, node_attribute])))
      }
      g <- g +
        scale_color_manual(values = custom_colors)
    } else if (axis_colors == "brewer") {
      if (length(custom_colors) == 0) {
        custom_colors <- "Set3"
      }
      g <- g +
        scale_color_brewer(palette = custom_colors)
    }
  }
  return(g)
}


#' Plot modularity values in a \code{dna_multiclust} object
#'
#' Plot modularity values in a \code{dna_multiclust} object.
#'
#' This function serves to plot the maximal modularity values saved in a
#' \code{dna_multiclust} object (as created by the \code{\link{dna_multiclust}}
#' function). For example, this can shed light on smoothed bipolarization or
#' multipolarization over time.
#'
#' This function replaces the \code{dna_plotTimeWindow} function that was
#' present in earlier versions of the package.
#'
#' @param x A \code{dna_multiclust} object, as created by the
#'   \code{\link{dna_multiclust}} function. Ideally with multiple time points.
#' @param only.max Only print the maximal modularity values, as opposed to
#'   facets for the modularity values produced by each separate clustering
#'   method?
#' @param anomalize Use the \pkg{anomalize} package for anomaly detection? This
#'   requires that the packages \pkg{anomalize} and \pkg{tibbletime} are
#'   installed on the system. In the resulting plot, outliers are annotated and
#'   ignored when drawing the time trend.
#' @param durations Plot width of the time window at the respective time point
#'   instead of the modularity? This can be useful for edge-based time windows
#'   as a measure of uncertainty around the modularity point estimates.
#' @param zero.as.na Treat all zeros as \code{NA} such that they are not
#'   plotted. This can be useful because zeros are often the result of a
#'   malfunctioning clustering algorithm.
#' @param ncol The number of columns for the facet layout if
#'   \code{only.max = FALSE} is set.
#' @param include.y Include specific y-axis value in the plot, for example
#'   \code{0}.
#' @return A \pkg{ggplot2} plot.
#'
#' @author Philip Leifeld
#' @seealso \code{\link{dna_multiclust}}, \code{\link{print.dna_multiclust}}
#'
#' @importFrom ggplot2 ggplot aes_string geom_boxplot ylab xlab theme theme_bw
#'   geom_line expand_limits geom_smooth facet_wrap
#' @importFrom dplyr arrange group_by sym
#' @export
dna_plotModularity <- function(x,
                               only.max = TRUE,
                               anomalize = FALSE,
                               durations = FALSE,
                               zero.as.na = FALSE,
                               ncol = 3,
                               include.y = NULL) {
  if (class(x) != "dna_multiclust") {
    stop("Not a 'dna_multiclust' object. Use the 'dna_cluster' function.")
  }
  if (isTRUE(anomalize)) {
    if (!requireNamespace("anomalize", quietly = TRUE)) {
      stop("The 'anomalize' package needs to be installed for using 'anomalize = TRUE'.\nTry 'install.packages(\"anomalize\").")
    }
    if (!requireNamespace("tibbletime", quietly = TRUE)) {
      stop("The 'tibbletime' package needs to be installed for using 'anomalize = TRUE'.\nTry 'install.packages(\"tibbletime\").")
    }
  }
  if (nrow(x$max_mod) == 1) { # only one time point (no time window): boxplot of modularity measures
    if (isTRUE(zero.as.na)) {
      x$modularity$modularity[x$modularity$modularity == 0] <- NA
    }
    if (isTRUE(anomalize)) {
      warning("'anomalize' is not possible because there is only one time point.")
    }
    ggplot2::ggplot(x$modularity, ggplot2::aes_string(y = "modularity")) +
      geom_boxplot() +
      ylab("Modularity") +
      theme_bw() +
      theme(axis.title.x = element_blank(),
            axis.text.x = element_blank(),
            axis.ticks.x = element_blank())
  } else if (isTRUE(only.max)) { # only plot a single facet with the maximal modularity value over time
    if (isTRUE(zero.as.na)) {
      x$max_mod$max_mod[x$max_mod$max_mod == 0] <- NA
    }
    x$max_mod$duration <- as.numeric(x$max_mod$stop.date - x$max_mod$start.date)
    if (isTRUE(anomalize)) {
      dat <- x$max_mod[, c("middle.date", ifelse(isTRUE(durations), "duration", "max_mod"))]
      tibbletime::as_tbl_time(dat, index = !!dplyr::sym("middle.date")) %>%
        dplyr::arrange(!!dplyr::sym("middle.date")) %>%
        anomalize::time_decompose(ifelse(isTRUE(durations), "duration", "max_mod")) %>%
        anomalize::anomalize("remainder") %>%
        anomalize::time_recompose() %>%
        anomalize::plot_anomalies(time_recomposed = TRUE, alpha_dots = 0.25) +
        ylab(ifelse(isTRUE(durations), "Time window duration", "Modularity")) +
        xlab("Time") +
        expand_limits(y = include.y)
    } else {
      ggplot2::ggplot(x$max_mod, ggplot2::aes_string(x = "middle.date", y = ifelse(isTRUE(durations), "duration", "max_mod"))) +
        geom_line() +
        geom_smooth(se = FALSE, stat = "smooth", method = "gam", formula = y ~ s(x, bs = "cs")) +
        ylab(ifelse(isTRUE(durations), "Time window duration", "Modularity")) +
        xlab("Time") +
        expand_limits(y = include.y) +
        theme_bw()
    }
  } else { # plot modularity for all measures, including maximal
    if (isTRUE(zero.as.na)) {
      x$modularity$modularity[x$modularity$modularity == 0] <- NA
      x$max_mod$max_mod[x$max_mod$max_mod == 0] <- NA
    }
    m <- merge(x = x$modularity, y = x$max_mod, by = c("i", "k"), all.x = TRUE, all.y = FALSE)
    best <- x$max_mod
    best$method <- rep("Maximum value", nrow(best))
    best$modularity <- best$max_mod
    best <- best[, c(1, 7:9, 2:6)]
    m <- rbind(best, m)
    m$method <- factor(m$method, levels = c("Maximum value", sort(unique(x$modularity$method))))
    m$duration <- as.numeric(m$stop.date - m$start.date)
    if (isTRUE(anomalize)) {
      dat <- m[, c("i", "middle.date", ifelse(isTRUE(durations), "duration", "modularity"), "method")]
      # use !!sym() to avoid problems during R CMD check
      tibbletime::as_tbl_time(dat, index = !!dplyr::sym("middle.date")) %>%
        dplyr::arrange(!!dplyr::sym("middle.date")) %>%
        dplyr::group_by(!!dplyr::sym("method")) %>%
        anomalize::time_decompose(ifelse(isTRUE(durations), "duration", "modularity")) %>%
        anomalize::anomalize("remainder") %>%
        anomalize::time_recompose() %>%
        anomalize::plot_anomalies(time_recomposed = TRUE, ncol = ncol, alpha_dots = 0.25) +
        ylab(ifelse(isTRUE(durations), "Time window duration", "Modularity")) +
        xlab("Time") +
        expand_limits(y = include.y)
    } else {
      ggplot2::ggplot(m, ggplot2::aes_string(x = "middle.date", y = ifelse(isTRUE(durations), "duration", "modularity"))) +
        geom_line() +
        geom_smooth(se = FALSE, stat = "smooth", method = "gam", formula = y ~ s(x, bs = "cs")) +
        ylab(ifelse(isTRUE(durations), "Time window duration", "Modularity")) +
        xlab("Time") +
        expand_limits(y = include.y) +
        facet_wrap(~ method, ncol = ncol) +
        theme_bw()
    }
  }
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
#' available as \code{custom_colors} when \code{colors = "brewer"}.
#'
#' @param x A \code{dna_network} object created by the \link{dna_network}
#'   function.
#' @param layout The type of layout to use. Available layouts include
#'   \code{"nicely"} (which tries to choose a suiting layout),
#'   \code{"bipartite"} (for two-mode networks), \code{"circle"}, \code{"dh"},
#'   \code{"drl"}, \code{"fr"}, \code{"gem"}, \code{"graphopt"}, \code{"kk"},
#'   \code{"lgl"}, \code{"mds"}, \code{"randomly"} and \code{"star"}. The
#'   default, \code{"auto"} chooses \code{"nicely"} if \code{x} is a one-mode
#'   network and \code{"bipartite"} in case of two-mode networks. Other layouts
#'   might be available (see \link[ggraph]{layout_igraph_auto} for details).
#' @param edges When set to \code{"link"} (default) straight lines are used to
#'   connect nodes. Other available options are \code{"arc"}, \code{"diagonal"}
#'   and \code{"fan"}.
#' @param edge_weight If \code{TRUE}, edge weights will be used to determine the
#'   width of the lines. The minimum and maximum width can be controlled with
#'   \code{edge_size_range}.
#' @param edge_size_range Takes a numeric vector with two values: minimum and
#'   maximum \code{edge_weight}.
#' @param edge_color Provide the name of a color to use for edges. Defaults to
#'   \code{"grey"}.
#' @param edge_alpha Takes numeric values to control the alpha-transparency of
#'   edges. Possible values range from \code{0} (fully transparent) to \code{1}
#'   (fully visible).
#' @param node_size Takes positive numeric values to control the size of nodes
#'   (defaults to \code{6}). Usually values between \code{1} and \code{20} look
#'   best.
#' @param node_attribute Takes the name of an attribute in DNA (i.e.
#'   \code{"id"}, \code{"value"}, \code{"color"}, \code{"type"}, \code{"alias"},
#'   \code{"notes"} or \code{"frequency"}) or \code{"group"} to color nodes.
#'   The option \code{"group"} only makes sense if you provide group membership
#'   information to the \code{groups} argument.
#' @param node_colors There are five options for coloring the nodes: (1.)
#'   \code{"auto"} uses \code{"identity"} if \code{node_attribute = "color"} and
#'   leaves the standard ggplot2 colors otherwise; (2.) \code{"identity"} tries
#'   to use \code{node_attribute} for colors (i.e., if you set
#'   \code{node_attribute = "color"} or have provided a color name in another
#'   attribute field in DNA) but fails if names are not plottable colors; (3)
#'   \code{"manual"} lets you provide colors via custom_colors; (4.)
#'   \code{"brewer"} automatically selects nice colors from a
#'   \code{RColorBrewer} palette (palettes can be set in custom_colors); and
#'   (5.) \code{"single"} uses the first value in custom_colors for all nodes.
#' @param custom_colors Takes custom values to control the node colors. The
#'   format of the necessary values depends on the setting of
#'   \code{node_colors}: When \code{node_colors = "manual"}, a character
#'   object containing the enough color names for all groups is needed; When
#' \code{node_colors = "brewer"} you need to supply a a palette from
#' \code{RColorBrewer} (defaults to \code{"Set3"} if \code{custom_colors} is
#'   left empty); When \code{node_colors "single"} only a single color name is
#'   needed (defaults to \code{"red"}).
#' @param node_shape Controls the node shape. Available shapes range from
#'   \code{0:25} and \code{32:127}.
#' @param node_label If \code{TRUE}, text is added next to nodes to label them.
#'   If \code{"label"}, a rectangle is drawn underneath the text, often making
#'   it easier to read. If \code{FALSE} no lables are drawn.
#' @param font_size Controls the font size of the node labels. The default,
#'   \code{6}, looks best on many viewers and knitr reports.
#' @param theme Provide the name of a theme. Available options are
#'   \code{"graph"} (which is customized to look best with networks),
#'   \code{"bw"}, \code{"void"}, \code{"light"} and \code{"dark"}. Leave empty
#'   to use standard ggplot theme. Choose other themes or customize with tools
#'   from \link{ggplot2} by adding \code{+ theme_*} after this function.
#' @param label_repel Controls how far labels will be put from nodes. The exact
#'   position of text is random but overplotting is avoided.
#' @param label_lines If \code{TRUE}, draws lines between nodes and labels if
#'   labels are further away from nodes.
#' @param truncate Sets the number of characters to which labels should be
#'   truncated. Value \code{Inf} turns off truncation.
#' @param groups Takes a \code{dna_cluster} object or a named list or character
#'   object. In case of a named list or character object, the names must match
#'   the values of \code{variable1} used during network construction (see
#'   example).
#' @param threshold Minimum threshold for which edges should be plotted.
#' @param seed Numeric value passed to \link{set.seed}. The default is as good
#'   as any other value but provides that plots are always reproducible.
#' @param show_legend If \code{TRUE}, displays a legend.
#' @param ... Arguments passed on to the layout function (see
#'   \link[ggraph]{layout_igraph_auto}). If you want to add more plot options
#'   use \code{+} and ggplot2 functions.
#' @examples
#' \dontrun{
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
#'             "U.S. Public Interest Research Group"= "group 1")
#' dna_plotNetwork(nw, node_attribute = "group", groups = groups)
#' }
#'
#' @author Johannes B. Gruber
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
                            edge_color = "grey",
                            edge_alpha = 1,
                            node_size = 6,
                            node_attribute = "color",
                            node_colors = "auto",
                            custom_colors = character(),
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
  dots <- list(...)
  if ("edge_color" %in% names(dots)) {
    edge_color <- dots[["edge_color"]]
  }
  if ("node_colors" %in% names(dots)) {
    node_colors <- dots[["node_colors"]]
  }
  if ("custom_colors" %in% names(dots)) {
    custom_colors <- dots[["custom_colors"]]
  }

  # Make igraph object
  set.seed(seed)
  if (any(class(x) %in% "dna_network_twomode")) {
    if (layout == "auto") {
      layout <- "bipartite"
      message("Using `bipartite` as default layout")
    }
  }
  graph <- dna_toIgraph(x)
  # Groups-
  if (!length(groups) > 0) {
    groups <- rep("Group 0", length(V(graph)$name))
    names(groups) <- V(graph)$name
  } else if (any(grepl("list|character", class(groups)))) {
    V(graph)$group <- groups[match(V(graph)$name, names(groups))]
  } else if (any(grepl("dna_cluster", class(groups)))) {
    V(graph)$group <- groups$group[match(V(graph)$name, groups$labels)]
  }
  # color and attribute
  args <- c(as.list(attributes(x)$call)[-1])
  args["networkType"] <- "eventlist"
  if (is.null(args[["statementType"]])) {
    args[["statementType"]] <- formals("dna_network")[["statementType"]]
  }
  if (is.null(args[["variable1"]])) {
    args[["variable1"]] <- formals("dna_network")[["variable1"]]
  }
  att <- dna_getAttributes(eval(args[["connection"]]), statementType = args[["statementType"]],
                           variable = args[["variable1"]], values = row.names(x))
  V(graph)$color <- as.character(att$color)[match(V(graph)$name, att$value)]
  if (node_attribute == "group") {
    V(graph)$attribute <- V(graph)$group
  } else {
    if (!any(node_attribute %in% colnames(att))) {
      stop(paste0("Not a possible 'node_attribute'. Please choose one of: 'group', '",
                  paste(colnames(att), collapse = "', '"), "'."))
    }
    V(graph)$attribute <- as.character(att[, node_attribute])[match(V(graph)$name, att$value)]
  }
  if (edge_weight) {
    E(graph)$Weight <- E(graph)$weight
  } else {
    E(graph)$Weight <- 1
  }
  if (!is.null(threshold)) {
    graph <- delete.edges(graph, which(!E(graph)$weight >= threshold))
  }
  # start the plot
  lyt <- create_layout(graph, layout = layout, ...)
  if (node_attribute == "group") {
    node_attribute <- "Membership"
  } else {
    node_attribute <- paste0(toupper(substr(node_attribute, 1, 1)),
                             substr(node_attribute, 2, nchar(node_attribute)))
  }
  lyt$name_short <- trim(as.character(lyt$name), n = truncate)
  if (any(class(x) %in% "dna_network_twomode")) {
    lyt$attribute <- as.character(lyt$attribute)
    if (node_colors == "auto" & node_attribute == "Color") {
      att <- dna_getAttributes(eval(args[["connection"]]),
                               statementType = args[["statementType"]],
                               variable = "concept")
      lyt$attribute[is.na(lyt$attribute)] <-
        as.character(att$color)[match(lyt$name[is.na(lyt$attribute)],
                                      att$value)]
    } else {
      lyt$attribute[is.na(lyt$attribute)] <- "Concept"
    }
  }
  colnames(lyt) <- gsub("attribute", node_attribute, colnames(lyt))
  g <- ggraph(lyt)
  # add lines
  if (edges == "link") {
    g <- g +
      geom_edge_link(aes_string(width = "Weight"), alpha = edge_alpha,
                     color = edge_color,
                     show.legend = show_legend)
  } else if (edges == "arc") {
    g <- g +
      geom_edge_arc(aes_string(width = "Weight"), alpha = edge_alpha,
                    color = edge_color,
                    show.legend = show_legend)
  } else if (edges == "diagonal") {
    g <- g +
      geom_edge_diagonal(aes_string(width = "Weight"), alpha = edge_alpha,
                         color = edge_color,
                         show.legend = show_legend)
  } else if (edges == "fan") {
    g <- g +
      geom_edge_fan(aes_string(width = "Weight"),
                    alpha = edge_alpha,
                    color = edge_color,
                    show.legend = show_legend)
  }
  g <- g +
    scale_edge_width(range = edge_size_range)
  # add nodes
  show_legend <- ifelse(show_legend,
                        NA,
                        show_legend)
  if (node_colors == "single") {
    if (length(custom_colors) == 0) {
      custom_colors <- "red"
    }
    g <- g +
      geom_node_point(color = custom_colors,
                      size = node_size,
                      shape = node_shape,
                      show.legend = show_legend) +
      scale_color_identity()
  } else {
    g <- g +
      geom_node_point(aes_string(color = node_attribute),
                      size = node_size,
                      shape = node_shape,
                      show.legend = show_legend)
  }
  # add labels
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
                         fontface = "bold",
                         size = font_size / .pt,
                         min.segment.length = ifelse(label_lines, 0.5, Inf))
    } else {
      g <- g +
        geom_text_repel(aes_string(x = "x", y = "y",
                                   label = "name_short"),
                        point.padding = label_repel,
                        box.padding = label_repel,
                        fontface = "bold",
                        size = font_size / .pt,
                        min.segment.length = ifelse(label_lines, 0.5, Inf))
    }
  }
  # theme
  if (theme == "graph") {
    g <- g +
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
  # colors
  if (!node_colors == "single") {
    if (node_colors == "auto" & node_attribute == "Color") {
      node_colors <- "identity"
    }
    if (node_colors == "auto" & !node_attribute == "Membership") {
      test <- sapply(unique(att[, tolower(node_attribute)]), function(a) {
        length(unique(att$color[att[, tolower(node_attribute)] == a])) == 1
      })
      if (all(test)) {
        g <- g +
          scale_color_manual(labels = att[, tolower(node_attribute)],
                             values = att$color)
      } else {
        warning("Inconsistent assignment of colors to '", node_attribute, "'.")
      }
    } else if (node_colors == "identity") {
      g <- g +
        scale_color_identity()
    } else if (node_colors == "manual") {
      g <- g +
        scale_color_manual(values = custom_colors)
    } else if (node_colors == "brewer") {
      if (length(custom_colors) == 0) {
        custom_colors <- "Set3"
      }
      g <- g +
        scale_color_brewer(palette = custom_colors)
    }
  }
  return(g)
}


#' Plot ideological ideal points from a dna_scale object
#'
#' Plots ideological ideal points with the results of the MCMC scaling
#' performed in the \code{dna_scale} functions.
#'
#' This function is a convenience wrapper for the \code{ggplot2} package to
#' plot ideological ideal points from \code{dna_scale} objects. Two different
#' variables can be plotted:
#'
#' Firstly, you can plot the ideological ideal points of subjects (e.g.,
#' \code{"organizations"}) which you have provided in \code{variable1} of the
#' \code{dna_scale} functions. The ideal point, which is the mean value of the
#' MCMC sample parameters, serves as the ideological position of an actor in an
#' e.g., ideological or policy dimension.
#'
#' Secondly, you can plot the item discrimination parameter of the variable
#' provided in \code{variable2} of the \code{dna_scale} functions (e.g.,
#' \code{"concepts"}). The item discrimination parameter indicates how
#' good for example a specific \code{"concept"} separates actors in the
#' ideological space.
#'
#' Plotting all actors or concepts can create chaotic plots, you can, therefore,
#' limit the plot by including only the most active actors or most prominent
#' concepts with \code{exclude_min}. Specific entries can be excluded with the
#' \code{exclude} argument. Furthermore, the plot can be split into several
#' facets according to the attributes of the variables.
#'
#' @param dna_scale A \code{dna_scale} object.
#' @param variable Variable to be plotted.
#' @param dimensions Number of dimensions to be plotted. Valid values are
#'   \code{1} and \code{2} for a one- or two-dimensional plot, and \code{2.1}
#'   or \code{2.2} for the first or second dimension of a two-dimensional
#'   scaling.
#' @param hpd A numeric scalar specifying the size of the Highest Posterior
#'   Density intervals (HPD). Defaults to \code{0.95}. \code{NULL} turns off
#'   HPDs.
#' @param label Logical. Should labels be plotted? Defaults to \code{TRUE}.
#' @param label_size,point_size Label and point size in pts.
#' @param label_colors,point_colors,hpd_colors Colors for the labels, points
#'   and Highest Posterior Densities of the plot. \code{TRUE} colors the
#'   variables according to the attributes in the object and \code{FALSE} sets
#'   colors to black. You can also provide customized colors. Possible
#'   options are either providing a single character vector (if you wish to
#'   color a plot element in only one color), or a character or numeric vector
#'   or data frame of at least the same length as values to be plotted. If you
#'   use a data frame, please provide one column named \code{"names"} that
#'   indicates the names of the values and one column named \code{"colors"}
#'   that specifies the value colors. Defaults to \code{TRUE}.
#' @param hpd_lwd Highest Posterior Density interval linewidth in pts.
#' @param intercept_lwd Linewidth of the intercept (a vertical bar indicating
#'   zero) in pts.
#' @param intercept_color Color for the intercept.
#' @param intercept_lty A character vector providing the linetype of the
#'   intercept. Valid values are \code{"solid"}, \code{"dashed"},
#'   \code{"dotted"}, \code{"twodash"}, \code{"dotdash"}, \code{"longdash"} or
#'   \code{"blank"}. Defaults to \code{"dashed"}. \code{"blank"} turns off
#'   intercept.
#' @param intercept_alpha A numeric value indicating the alpha level of the
#'   intercept.
#' @param facet A character vector specifying which attribute of the variable
#'   should be used for the facet plot. Valid values are \code{"type"},
#'   \code{"alias"} or \code{"notes"}.
#' @param truncate Sets the number of characters to which labels should be
#'   truncated.
#' @param x_axis_label,y_axis_label A character vector specifying the x and y
#'   axis label name(s).
#' @param exclude_min A numeric value reducing the plot to actors/concepts
#'   with a minimum frequency of statements.
#' @param exclude A character vector to exclude actors/concepts from the plot.
#'
#' @examples
#' \dontrun{
#' dna_init()
#' conn <- dna_connection(dna_sample())
#' dna_scale <- dna_scale1dbin(conn,
#'                             variable1 = "organization",
#'                             variable2 = "concept",
#'                             qualifier = "agreement",
#'                             threshold = 0.51,
#'                             mcmc_iterations = 20000,
#'                             mcmc_burnin = 2000,
#'                             mcmc_thin = 10,
#'                             store_variables = "both")
#' dna_plotScale(dna_scale,
#'               variable = "organization",
#'               dimensions = 1,
#'               hpd = 0.95,
#'               point_colors = TRUE,
#'               exclude = c("Environmental Protection Agency", "Senate"))
#' }
#'
#' @author Tim Henrichsen, Johannes B. Gruber
#'
#' @export
#' @import ggplot2
#' @importFrom coda HPDinterval
#' @importFrom ggrepel geom_text_repel
#' @importFrom dplyr arrange
dna_plotScale <- function(dna_scale,
                          variable = "organization",
                          dimensions = 1,
                          hpd = 0.95,
                          label = TRUE,
                          label_size = 10,
                          label_colors = FALSE,
                          point_size = 4,
                          point_colors = TRUE,
                          hpd_lwd = 0.8,
                          hpd_colors = TRUE,
                          intercept_lwd = 0.8,
                          intercept_color = "#525252",
                          intercept_lty = "dashed",
                          intercept_alpha = 0.4,
                          facet = NULL,
                          truncate = 60,
                          x_axis_label = NULL,
                          y_axis_label = NULL,
                          exclude_min = 1,
                          exclude = NULL) {
  if (!any(grepl("dna_scale", class(dna_scale), fixed = TRUE))) {
    stop ("This is not a dna_scale object.")
  }
  if (dimensions >= 2 & ("dna_scale1dbin" %in% class(dna_scale) |
                         "dna_scale1dord" %in% class(dna_scale))) {
    stop("This is not a two-dimensional dna_scale object.")
  }
  if (!(variable == dna_scale$call$store_variables |
        dna_scale$call$store_variables == "both")) {
    stop("Variable to be plotted was not stored in dna_scale object.")
  }
  if (!(variable == dna_scale$call$variable1 |
        variable == dna_scale$call$variable2)) {
    stop("Variable to be plotted cannot be found in dna_scale object.")
  }
  if (!is.character(variable)) {
    stop ("'variable' must be provided as a character object.")
  }
  if (dimensions > 2.2) {
    stop("Only two-dimensional plotting is currently supported.")
  }
  if (dimensions == 1 & ("dna_scale2dbin" %in% class(dna_scale) |
                         "dna_scale2dord" %in% class(dna_scale))) {
    stop("Please insert either 'dimensions = 2.1' for the first dimension ",
         "of the two-dimensional scaling or 'dimensions = 2.2' for ",
         "the second dimension.")
  }
  if (variable == dna_scale$call$variable1) {
    x <- dna_scale$attributes[dna_scale$attributes$variable == "actor", ]
  } else if (variable == dna_scale$call$variable2) {
    x <- dna_scale$attributes[dna_scale$attributes$variable == "concept", ]
  }
  if (!is.null(hpd)) {
    # Compute highest posterior densities
    hpd_x <- coda::HPDinterval(dna_scale$sample, prob = hpd)
    if (variable == dna_scale$call$variable1) {
      if ("dna_scale1dbin" %in% class(dna_scale)) {
        rownames(hpd_x) <- gsub("^theta.", "", rownames(hpd_x))
      } else if ("dna_scale1dord" %in% class(dna_scale)) {
        rownames(hpd_x) <- gsub("^phi.|.2$", "", rownames(hpd_x))
      } else if ("dna_scale2dbin" %in% class(dna_scale)) {
        hpd_x <- hpd_x[grepl("^theta.", rownames(hpd_x)), ]
        hpd_x1 <- hpd_x[grepl(".1$", rownames(hpd_x)), ]
        rownames(hpd_x1) <- gsub("^theta.|.1$", "", rownames(hpd_x1))
        hpd_x2 <- hpd_x[grepl(".2$", rownames(hpd_x)), ]
        rownames(hpd_x2) <- gsub("^theta.|.2$", "", rownames(hpd_x2))
        hpd_x <- merge(hpd_x1,
                       hpd_x2,
                       by = "row.names",
                       suffixes = c("_dim1", "_dim2"))
      } else if ("dna_scale2dord" %in% class(dna_scale)) {
        hpd_x <- hpd_x[grepl("^phi.", rownames(hpd_x)), ]
        hpd_x1 <- hpd_x[grepl(".2$", rownames(hpd_x)), ]
        rownames(hpd_x1) <- gsub("^phi.|.2$", "", rownames(hpd_x1))
        hpd_x2 <- hpd_x[grepl(".3$", rownames(hpd_x)), ]
        rownames(hpd_x2) <- gsub("^phi.|.3$", "", rownames(hpd_x2))
        hpd_x <- merge(hpd_x1,
                       hpd_x2,
                       by = "row.names",
                       suffixes = c("_dim1", "_dim2"))
      }
    } else if (variable == dna_scale$call$variable2) {
      if ("dna_scale1dbin" %in% class(dna_scale)) {
        rownames(hpd_x) <- gsub("^beta.", "", rownames(hpd_x))
      } else if ("dna_scale1dord" %in% class(dna_scale)) {
        rownames(hpd_x) <- gsub("^Lambda|.2$", "", rownames(hpd_x))
      } else if ("dna_scale2dbin" %in% class(dna_scale)) {
        hpd_x <- hpd_x[grepl("^beta.", rownames(hpd_x)), ]
        hpd_x1 <- hpd_x[grepl(".1$", rownames(hpd_x)), ]
        rownames(hpd_x1) <- gsub("^beta.|.1$", "", rownames(hpd_x1))
        hpd_x2 <- hpd_x[grepl(".2$", rownames(hpd_x)), ]
        rownames(hpd_x2) <- gsub("^beta.|.2$", "", rownames(hpd_x2))
        hpd_x <- merge(hpd_x1,
                       hpd_x2,
                       by = "row.names",
                       suffixes = c("_dim1", "_dim2"))
      } else if ("dna_scale2dord" %in% class(dna_scale)) {
        hpd_x <- hpd_x[grepl("^Lambda", rownames(hpd_x)), ]
        hpd_x1 <- hpd_x[grepl(".2$", rownames(hpd_x)), ]
        rownames(hpd_x1) <- gsub("^Lambda|.2$", "", rownames(hpd_x1))
        hpd_x2 <- hpd_x[grepl(".3$", rownames(hpd_x)), ]
        rownames(hpd_x2) <- gsub("^Lambda|.3$", "", rownames(hpd_x2))
        hpd_x <- merge(hpd_x1,
                       hpd_x2,
                       by = "row.names",
                       suffixes = c("_dim1", "_dim2"))
      }
    }
    if ("dna_scale1dbin" %in% class(dna_scale) |
        "dna_scale1dord" %in% class(dna_scale)) {
      x <- merge(x, hpd_x, by.x = "Row.names", by.y = 0)
    } else {
      x <- merge(x, hpd_x, by = "Row.names")
    }
  }
  if (!is.null(exclude)) {
    if (any(!exclude %in% dna_scale$attributes$Row.names)) {
      warning("The following exclude values could not be found in dna_scale ",
              "object:\n",
              paste(exclude[!exclude %in% dna_scale$attributes$Row.names],
                    collapse = "\n"))
    }
    x <- x[!x$Row.names %in% exclude, ]
  }
  if (isTRUE(exclude_min > 1)) {
    x <- x[x$frequency >= exclude_min, ]
  }
  if (isTRUE(point_colors)) {
    x$colors_point <- x$color
  } else if (isTRUE(point_colors == FALSE)){
    x$colors_point <- "#000000"
  } else {
    if (is.data.frame(point_colors)) {
      if (!("names" %in% colnames(point_colors) |
            "colors" %in% colnames(point_colors))) {
        stop("Cannot find column names specified as \"names\" or \"colors\" ",
             "in 'point_colors' object. Please provide both columns with ",
             "matching values.")
      }
      if (nrow(point_colors) < nrow(x)) {
        stop("Values in 'point_colors' are not equal to values in dna_scale ",
             "object. Please add the following values:\n",
             paste(x$Row.names[!x$Row.names %in% point_colors$names],
                   collapse = "\n"))
      }
      if (!(all(x$Row.names %in% point_colors$names))) {
        stop("Not all dna_scale values are included in the 'point_colors' ",
             "object. Please add the following values:\n",
             paste(x$Row.names[!x$Row.names %in% point_colors$names],
                   collapse = "\n"))
      }
      x$colors_point <- point_colors$colors[match(x$Row.names, point_colors$names)]
    } else if (is.character(point_colors) | is.numeric(point_colors)) {
      if (length(point_colors) == 1) {
        x$colors_point <- point_colors
      } else if (any(x$Row.names %in% names(point_colors))) {
        if (length(point_colors) < nrow(x)) {
          stop("Values in 'point_colors' are not equal to values in dna_scale ",
               "object. Please add the following values:\n",
               paste(x$Row.names[!x$Row.names %in% names(point_colors)],
                     collapse = "\n"))
        }
        if (!(all(x$Row.names %in% names(point_colors)))) {
          stop("Not all dna_scale values are included in the 'point_colors' ",
               "object. Please add the following values:\n",
               paste(x$Row.names[!x$Row.names %in% names(point_colors)],
                     collapse = "\n"))
        }
        x$colors_point <- point_colors[match(x$Row.names, names(point_colors))]
      } else {
        if (length(point_colors) != nrow(x)) {
          stop(paste0("Values of 'point_colors' must equal values in dna_scale",
                      " object (", nrow(x), ")."))
        }
        x$colors_point <- point_colors
      }
    }
  }
  if (isTRUE(hpd_colors)) {
    x$color_hpd <- x$color
  } else if (isTRUE(hpd_colors == FALSE)) {
    x$color_hpd <- "#000000"
  } else {
    if (is.data.frame(hpd_colors)) {
      if (!("names" %in% colnames(hpd_colors) |
            "colors" %in% colnames(hpd_colors))) {
        stop("Cannot find column names specified as \"names\" or \"colors\" ",
             "in 'hpd_colors' object. Please provide both columns with ",
             "matching values.")
      }
      if (nrow(hpd_colors) < nrow(x)) {
        stop("Values in 'hpd_colors' are not equal to values in dna_scale ",
             "object. Please add the following values:\n",
             paste(x$Row.names[!x$Row.names %in% hpd_colors$names],
                   collapse = "\n"))
      }
      if (!(all(x$Row.names %in% hpd_colors$names))) {
        stop("Not all dna_scale values are included in the 'hpd_colors' ",
             "object. Please add the following values:\n",
             paste(x$Row.names[!x$Row.names %in% hpd_colors$names],
                   collapse = "\n"))
      }
      x$color_hpd <- hpd_colors$colors[match(x$Row.names, hpd_colors$names)]
    } else if (is.character(hpd_colors) | is.numeric(hpd_colors)) {
      if (length(hpd_colors) == 1) {
        x$color_hpd <- hpd_colors
      } else if (any(x$Row.names %in% names(hpd_colors))) {
        if (length(hpd_colors) < nrow(x)) {
          stop("Values in 'hpd_colors' are not equal to values in dna_scale ",
               "object. Please add the following values:\n",
               paste(x$Row.names[!x$Row.names %in% names(hpd_colors)],
                     collapse = "\n"))
        }
        if (!(all(x$Row.names %in% names(hpd_colors)))) {
          stop("Not all dna_scale values are included in the 'hpd_colors' ",
               "object. Please add the following values:\n",
               paste(x$Row.names[!x$Row.names %in% names(hpd_colors)],
                     collapse = "\n"))
        }
        x$color_hpd <- hpd_colors[match(x$Row.names, names(hpd_colors))]
      } else {
        if (length(hpd_colors) != nrow(x)) {
          stop(paste0("Values of 'hpd_colors' must equal values in dna_scale ",
                      "object (", nrow(x), ")."))
        }
        x$color_hpd <- hpd_colors
      }
    }
  }
  if (isTRUE(label_colors)) {
    x$color_label <- x$color
  } else if (isTRUE(label_colors == FALSE)) {
    if (dimensions == 2) {
      x$color_label <- "#000000"
    } else {
      x$color_label <- NULL
    }
  } else {
    if (is.data.frame(label_colors)) {
      if (!("names" %in% colnames(label_colors) |
            "colors" %in% colnames(label_colors))) {
        stop("Cannot find column names specified as \"names\" or \"colors\" ",
             "in 'label_colors' object. Please provide both columns with ",
             "matching values.")
      }
      if (nrow(label_colors) < nrow(x)) {
        stop("Values in 'label_colors' are not equal to values in dna_scale ",
             "object. Please add the following values:\n",
             paste(x$Row.names[!x$Row.names %in% label_colors$names],
                   collapse = "\n"))
      }
      if (!(all(x$Row.names %in% label_colors$names))) {
        stop("Not all dna_scale values are included in the 'label_colors' ",
             "object. Please add the following values:\n",
             paste(x$Row.names[!x$Row.names %in% label_colors$names],
                   collapse = "\n"))
      }
      x$color_label <- label_colors$colors[match(x$Row.names, label_colors$names)]
    } else if (is.character(label_colors) | is.numeric(label_colors)) {
      if (length(label_colors) == 1) {
        x$color_label <- label_colors
      } else if (any(x$Row.names %in% names(label_colors))) {
        if (length(label_colors) < nrow(x)) {
          stop("Values in 'label_colors' are not equal to values in dna_scale ",
               "object. Please add the following values:\n",
               paste(x$Row.names[!x$Row.names %in% names(label_colors)],
                     collapse = "\n"))
        }
        if (!(all(x$Row.names %in% names(label_colors)))) {
          stop("Not all dna_scale values are included in the 'label_colors' ",
               "object. Please add the following values:\n",
               paste(x$Row.names[!x$Row.names %in% names(label_colors)],
                     collapse = "\n"))
        }
        x$color_label <- label_colors[match(x$Row.names, names(label_colors))]
      } else {
        if (length(label_colors) != nrow(x)) {
          stop(paste0("Values of 'label_colors' must equal values in dna_scale",
                      " object (", nrow(x), ")."))
        }
        x$color_label <- label_colors
      }
    }
  }
  if (!is.null(facet)) {
    x$facet <- x[, facet]
  }
  if (!is.null(truncate)) {
    x$Row.names <- trim(x$Row.names, truncate)
    if (any(duplicated(x$Row.names))) {
      x$Row.names <- paste0(make.names(sub("...$", "", x$Row.names),
                                       unique = TRUE), "...")
      if (is.null(facet)) {
        warning(paste0("After truncation, some row labels are now exactly the ",
                       "same. These are followed by the name + number now. ",
                       "Consider increasing the 'truncate' value."))
      }
    }
  }
  if (dimensions == 1) {
    x <- dplyr::arrange(x, mean)
    x$Row.names <- factor(x$Row.names, levels = x$Row.names[order(x$mean)])
  } else if (dimensions == 2.1) {
    x <- dplyr::arrange(x, mean_dim1)
    x$Row.names <- factor(x$Row.names, levels = x$Row.names[order(x$mean_dim1)])
  } else if (dimensions == 2.2) {
    x <- dplyr::arrange(x, mean_dim2)
    x$Row.names <- factor(x$Row.names, levels = x$Row.names[order(x$mean_dim2)])
  }
  if (dimensions != 2) {
    if (dimensions == 1) {
      g <- ggplot(x, aes_string(x = "mean",
                                y = "Row.names",
                                color = "colors_point")) +
        geom_vline(xintercept = 0,
                   size = intercept_lwd,
                   linetype = intercept_lty,
                   color = intercept_color,
                   alpha = intercept_alpha)
      if (!is.null(hpd)) {
        g <- g +
          geom_errorbarh(aes_string(xmin = "lower",
                                    xmax = "upper",
                                    color = "color_hpd"),
                         height = 0, size = hpd_lwd)
      }
    } else if (dimensions == 2.1) {
      g <- ggplot(x, aes_string(x = "mean_dim1",
                                y = "Row.names",
                                color = "colors_point")) +
        geom_vline(xintercept = 0,
                   size = intercept_lwd,
                   linetype = intercept_lty,
                   color = intercept_color,
                   alpha = intercept_alpha)
      if (!is.null(hpd)) {
        g <- g +
          geom_errorbarh(aes_string(xmin = "lower_dim1",
                                    xmax = "upper_dim1",
                                    color = "color_hpd"),
                         height = 0, size = hpd_lwd)
      }
    } else if (dimensions == 2.2) {
      g <- ggplot(x, aes_string(x = "mean_dim2",
                                y = "Row.names",
                                color = "colors_point")) +
        geom_vline(xintercept = 0,
                   size = intercept_lwd,
                   linetype = intercept_lty,
                   color = intercept_color,
                   alpha = intercept_alpha)
      if (!is.null(hpd)) {
        g <- g +
          geom_errorbarh(aes_string(xmin = "lower_dim2",
                                    xmax = "upper_dim2",
                                    color = "color_hpd"),
                         height = 0, size = hpd_lwd)
      }
    }
    g <- g +
      geom_point(size = point_size, show.legend = FALSE) +
      labs(x = x_axis_label, y = y_axis_label) +
      scale_color_identity() +
      scale_y_discrete()
    if (label) {
      g <- g +
        theme(axis.text.y = element_text(size = label_size,
                                         color = x$color_label))
    } else {
      g <- g +
        theme(axis.text.y = element_blank(), axis.ticks = element_blank())
    }
    if (!is.null(facet)) {
      g <- g +
        facet_wrap(~ facet) +
        theme(axis.text.y = element_blank(), axis.ticks.y = element_blank())
    }
  } else if (dimensions == 2) {
    g <- ggplot(x, aes_string(x = "mean_dim1",
                              y = "mean_dim2",
                              color = "colors_point"))
    if (!is.null(hpd)) {
      g <- g +
        geom_errorbarh(aes_string(xmin = "lower_dim1",
                                  xmax = "upper_dim1",
                                  color = "color_hpd"),
                       height = 0, size = hpd_lwd) +
        geom_errorbar(aes_string(ymin = "lower_dim2",
                                 ymax = "upper_dim2",
                                 color = "color_hpd"),
                      width = 0, size = hpd_lwd)
    }
    g <- g +
      geom_point(size = point_size, show.legend = FALSE) +
      labs(x = x_axis_label, y = y_axis_label) +
      scale_color_identity() +
      scale_y_continuous() +
      geom_vline(xintercept = 0,
                 size = intercept_lwd,
                 linetype = intercept_lty,
                 color = intercept_color,
                 alpha = intercept_alpha) +
      geom_hline(yintercept = 0,
                 size = intercept_lwd,
                 linetype = intercept_lty,
                 color = intercept_color,
                 alpha = intercept_alpha)
    if (is.null(facet) & label) {
      g <- g +
        ggrepel::geom_text_repel(aes_string(label = "Row.names"),
                                 show.legend = FALSE,
                                 size = label_size,
                                 color = x$color_label)
    }
    if (!is.null(facet)) {
      g <- g +
        facet_wrap(~ facet) +
        labs(y = y_axis_label, x = x_axis_label) +
        theme(axis.text.y = element_blank(), axis.ticks.y = element_blank())
    }
  }
  return(g)
}



# Summary plots ----------------------------------------------------------------

#' Plot agreement and disagreement
#'
#' Plot agreement and disagreement towards statements.
#'
#' This function plots agreement and disagreement towards DNA Statements for
#' different categories such as "concept", "person" or "docTitle". The goal is
#' to determine the centrality of claims. If, for example, concepts are not very
#' contested, this may mask the extent of polarization with regard to the other
#' concepts. It often makes sense to exclude those concept in further analysis.
#'
#' @param connection A \code{dna_connection} object created by the
#'   \link{dna_connection} function.
#' @param of Category over which (dis-)agreement will be plotted. Most useful
#'   categories are \code{"concept"} and \code{"organization"} but document categories
#'   can be used.
#' @param lab.pos,lab.neg Names for (dis-)agreement labels.
#' @param lab Determines whether (dis-)agreement labels and title are displayed.
#' @param colors If \code{TRUE}, statement colors will be used to fill the
#'   bars. Not possible for all categories.
#' @param fontSize Text size in pts.
#' @param barWidth Thickness of the bars. bars will touch when set to \code{1}.
#'   When set to \code{0.5}, space between two bars is the same as thickness of
#'   bars.
#' @param axisWidth Thickness of the x-axis which separates agreement from
#'   disagreement.
#' @param truncate Sets the number of characters to which axis labels (i.e. the
#'   categories of "of") should be truncated.
#' @param ... Additional arguments passed to \link{dna_network}.
#'
#' @examples
#' \dontrun{
#' dna_init()
#' conn <- dna_connection(dna_sample())
#'
#' dna_barplot(connection = conn,
#'             of = "concept",
#'             colors = FALSE,
#'             barWidth = 0.5)
#' }
#'
#' @author Johannes B. Gruber
#'
#' @export
#' @import ggplot2
dna_barplot <- function(connection,
                        of = "concept",
                        lab.pos = "Agreement",
                        lab.neg = "Disagreement",
                        lab = TRUE,
                        colors = FALSE,
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
  if ("colors" %in% names(dots)) {
    colors <- dots[["colors"]]
  }
  # test validity of "of"-value
  if (!of %in% colnames(dta) | of %in% c("id", "agreement")) {
    stop(
      paste0("\"", of, "\" is not a valid \"of\" value. Choose one of the following:\n",
             paste0("\"", colnames(dta)[!colnames(dta) %in% c("id", "agreement")], "\"", collapse = ",\n"))
    )
  }
  if (of %in% c("time", "docId", "docTitle", "docAuthor", "docSource", "docSection", "docType")) {
    warning(
      paste0("\"colors = TRUE\" not possible for \"of = \"", of, "\"\".", collapse = ",\n")
    )
    colors <- FALSE
  }
  if ("qualifier" %in% names(dots)) {
    colnames(dta) <- gsub(dots$qualifier, "agreement", colnames(dta))
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
  dta2 <- stats::aggregate(absFrequency ~ of, sum, data = dta)
  dta2 <- dta2[order(dta2$absFrequency, decreasing = TRUE), ]
  # replicate order of dta2$of to dta
  dta$of <- factor(dta$of, levels = rev(dta2$of))
  # get bar colors
  if (colors) {
    if (!"statementType" %in% names(dots)) {
      dots$statementType <- "DNA Statement"
    }
    col <- dna_getAttributes(connection = connection, statementType = dots$statementType,
                             variable = of, values = NULL)
    dta$color <- as.character(col$color[match(dta$of, col$value)])
    dta$text_color <- "black"
    dta$text_color[sum(grDevices::col2rgb(dta$color) * c(299, 587, 114)) / 1000 < 123] <- "white"
  } else {
    dta$color <- "white"
    dta$text_color <- "black"
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
    if (offset > abs(min(dta$Frequency))) {offset <- abs(min(dta$Frequency))}
    if (offset > max(dta$Frequency)) {offset <- abs(min(dta$Frequency))}
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
      if (all(dta$Frequency[dta$of == c] < 0)) {
        dta <- rbind(dta,
                     dta[dta$of == c, ][1, ])
        dta[nrow(dta), c(1, 3, 4)] <- 0
      }
      if (all(dta$Frequency[dta$of == c] > 0)) {
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
                        decreasing = TRUE), ],
              aes_string(x = "of",
                         y = "Frequency",
                         fill = "agreement",
                         label = "label")) +
    geom_bar(aes_string(fill = "color",
                        color = "text_color"),
             stat = "identity",
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
    scale_fill_identity() +
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
      geom_text(aes_string(color = "text_color"),
                size = (fontSize / .pt),
                position = position_stack(vjust = 0.5),
                inherit.aes = TRUE)
  }
  if (lab) {
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


#' Convergence diagnostics for \code{dna_scale} objects
#'
#' Convergence diagnostics for the MCMC chain created by the \code{dna_scale}
#' functions.
#'
#' This function offers several convergence diagnostics for the MCMC chain
#' created by the \code{dna_scale} functions. Note that for the values
#' indicated in \code{variable2}, only the item discrimination parameters are
#' evaluated. There are three possible ways of assessing the mixing of a chain:
#'
#' \code{"trace"} is a graphic inspection of the sampled values by iteration.
#' Once the chain has reached its stationary distribution, the parameter values
#' should look like a hairy caterpillar, meaning that the chain should not stay
#' in the same state for too long or have too many consecutive steps in one
#' direction.
#'
#' \code{"density"} visually analyzes the cumulative density of the sampled
#' values for each parameter. Unimodality should indicate convergence of the
#' chain, while multimodality might indicate an identification problem leading
#' to non-convergence.
#'
#' Similar to the T-Test, \code{"geweke"} conducts a difference of means test
#' for the sampled values for two sections of the chain, by comparing the means
#' of the first 10 percent of iterations with the final 50 percent of
#' iterations. This is done under the assumption that only the last half of
#' the chain has converged to the target distribution. The returned test
#' statistic is a standard Z-score. All values should be below the 1.96 value
#' which indicates significance at the p =< 0.05 level, meaning that the chain
#' has converged.
#'
#' In case your chain has not converged, a first solution could be to increase
#' the \code{iterations} and the \code{burn-in} phase of your scaling. Other
#' options can be to reduce the scaling to only prominent actors and/or
#' concepts with the \code{drop_min_actors} and/or \code{drop_min_concepts}
#' arguments in the respective \code{dna_scale} functions. Setting
#' \code{constraints} or changing \code{priors} provide another possibility to
#' improve your results and achieve chain convergence.
#'
#' @param dna_scale A \code{dna_scale} object.
#' @param variable Variable for assessing convergence diagnostics. Can either
#'   be the value provided in \code{variable1} or \code{variable2} of the
#'   \code{dna_scale} functions, or \code{"both"} if both stored variables
#'   should be analyzed. Defaults to \code{"both"}.
#' @param method Method for the convergence diagnostics. Supported are
#'   \code{"geweke"}, \code{"density"} and \code{"trace"}. Defaults to
#'   \code{"geweke"}.
#' @param colors Colors for either the \code{density} or \code{trace} plots.
#'   \code{TRUE} colors the variables according to the attributes in the
#'   object and \code{FALSE} sets colors to black. You can also provide
#'   customized colors. Possible options are either providing a single
#'   character vector (if you wish to color values in only one color), or a
#'   character or numeric vector or data frame of at least the same length as
#'   values in the object. If you use a data frame, please provide one column
#'   named \code{"names"} that indicates the names of the values and one column
#'   named \code{"colors"} that specifies the value colors. Defaults to
#'   \code{TRUE}.
#' @param trace_size Size of the trace lines for the \code{traceplots}.
#' @param nrow Number of rows for the facet plot.
#' @param ncol Number of columns for the facet plot.
#' @param facet_page If the number of values to be plotted exceeds the
#'   specified number of columns and rows of the facet plot, the plot is split
#'   into several pages. \code{facet_page} indicates the page you wish to plot.
#' @param value Optional character vector if only specific values should be
#'   analyzed. If specified, \code{variable} will be ignored.
#'
#' @examples
#' \dontrun{
#' dna_init()
#' conn <- dna_connection(dna_sample())
#' dna_scale <- dna_scale1dbin(conn,
#'                             variable1 = "organization",
#'                             variable2 = "concept",
#'                             qualifier = "agreement",
#'                             threshold = 0.51,
#'                             mcmc_iterations = 20000,
#'                             mcmc_burnin = 2000,
#'                             mcmc_thin = 10,
#'                             store_variables = "both")
#'
#' dna_convergenceScale(dna_scale,
#'                      variable = "both",
#'                      method = "trace",
#'                      colors = TRUE,
#'                      nrow = 4,
#'                      ncol = 4)
#'
#' dna_convergenceScale(dna_scale,
#'                      method = "density",
#'                      colors = TRUE,
#'                      nrow = 1,
#'                      ncol = 2,
#'                      value = c("Senate", "Sierra Club"))
#' }
#'
#' @author Tim Henrichsen, Johannes B. Gruber
#'
#' @export
#' @import ggplot2
#' @importFrom coda geweke.diag
#' @importFrom reshape2 melt
#' @importFrom dplyr group_by
#' @importFrom ggforce facet_wrap_paginate
#' @importFrom utils menu
dna_convergenceScale <- function(dna_scale,
                                 variable = "both",
                                 method = "geweke",
                                 colors = TRUE,
                                 trace_size = 0.5,
                                 nrow = 3,
                                 ncol = 3,
                                 facet_page = 1,
                                 value = NULL) {
  if (!any(grepl("dna_scale", class(dna_scale), fixed = TRUE))) {
    stop ("This is not a dna_scale object.")
  }
  if (is.null(variable) & is.null(value)) {
    stop("Please specify variable from dna_scale object or choose 'both' to ",
         "use both variables. If you only want to analyze a specific value, ",
         "please indicate the name in 'value'.")
  }
  if (!is.character(variable)) {
    stop ("'variable' must be provided as a character object.")
  }
  if (!(variable %in% dna_scale$call$store_variables |
        dna_scale$call$store_variables == "both")) {
    stop("Variable to be analyzed was not stored in dna_scale object.")
  }
  if (!(variable == dna_scale$call$variable1 |
        variable == dna_scale$call$variable2 |
        variable == "both")) {
    stop("Variable to be analyzed cannot be found in dna_scale object.")
  }
  if (!(method == "geweke" | method == "trace" | method == "density")) {
    stop(paste(method, "is not a valid method in 'dna_convergenceScale'. ",
               "Please use either 'density','trace' or 'geweke'."))
  }
  if ((is.null(nrow) | is.null(ncol)) & !method == "geweke" &
      (length(value) > 1 | !is.null(variable))) {
    stop(paste("Please specify the number of rows and columns to facet the",
               method, "plot"))
  }
  if (!is.null(value)) {
    if (isTRUE(!value %in% dna_scale$attributes$Row.names)) {
      stop("'value' could not be found in dna_scale object.")
    }
    if (any(!value %in% dna_scale$attributes$Row.names)) {
      warning("The following values could not be found in dna_scale object:",
              "\n", paste(value[!value %in% dna_scale$attributes$Row.names],
                          collapse = "\n"))
    }
    if ("dna_scale1dbin" %in% class(dna_scale)) {
      colnames(dna_scale$sample) <- gsub("^theta.|^beta.", "",
                                         colnames(dna_scale$sample))
      x <- as.data.frame(dna_scale$sample[, colnames(dna_scale$sample) %in% value, drop = FALSE])
    } else if ("dna_scale1dord" %in% class(dna_scale)) {
      colnames(dna_scale$sample) <- gsub("^Lambda|^phi.|.2$", "", colnames(dna_scale$sample))
      x <- as.data.frame(dna_scale$sample[, colnames(dna_scale$sample) %in% value, drop = FALSE])
    } else if ("dna_scale2dbin" %in% class(dna_scale)) {
      colnames(dna_scale$sample) <- gsub("^theta.|^beta.", "", colnames(dna_scale$sample))
      colnames(dna_scale$sample) <- gsub(".1$", "_dim1", colnames(dna_scale$sample))
      colnames(dna_scale$sample) <- gsub(".2$", "_dim2", colnames(dna_scale$sample))
      value2 <- value
      value <- paste0(value, "_dim1")
      value2 <- paste0(value2, "_dim2")
      x <- as.data.frame(dna_scale$sample[, colnames(dna_scale$sample) %in% value, drop = FALSE])
      x <- cbind(x, dna_scale$sample[, colnames(dna_scale$sample) %in% value2, drop = FALSE])
    } else if ("dna_scale2dord" %in% class(dna_scale)) {
      colnames(dna_scale$sample) <- gsub("^phi.|^Lambda", "", colnames(dna_scale$sample))
      colnames(dna_scale$sample) <- gsub(".2$", "_dim1", colnames(dna_scale$sample))
      colnames(dna_scale$sample) <- gsub(".3$", "_dim2", colnames(dna_scale$sample))
      value2 <- value
      value <- paste0(value, "_dim1")
      value2 <- paste0(value2, "_dim2")
      x <- as.data.frame(dna_scale$sample[, colnames(dna_scale$sample) %in% value, drop = FALSE])
      x <- cbind(x, dna_scale$sample[, colnames(dna_scale$sample) %in% value2, drop = FALSE])
    }
  } else {
    # Filter actors and/or concepts out of dna_scale object
    if (variable == "both") {
      x <- dna_scale$sample[, grepl("^theta.|^phi.|^beta.|^Lambda", colnames(dna_scale$sample))]
    } else if (variable %in% dna_scale$call$variable1) {
      x <- dna_scale$sample[, grepl("^theta.|^phi.", colnames(dna_scale$sample))]
    } else if (variable %in% dna_scale$call$variable2) {
      x <- dna_scale$sample[, grepl("^beta.|^Lambda", colnames(dna_scale$sample))]
    }
    if ("dna_scale1dord" %in% class(dna_scale) &
        (variable %in% dna_scale$call$variable2 |
         variable == "both")) {
      x <- x[, grepl(".2$", colnames(x))]
    } else if ("dna_scale2dord" %in% class(dna_scale) &
               (variable %in% dna_scale$call$variable2 |
                variable == "both")) {
      x <- x[, grepl(".2$|.3$", colnames(x))]
    }
    if ("dna_scale1dbin" %in% class(dna_scale)) {
      # Rename
      if (variable == "both") {
        colnames(x) <- gsub("^theta.|^beta.", "", colnames(x))
      } else if (variable %in% dna_scale$call$variable1) {
        colnames(x) <- gsub("^theta.", "", colnames(x))
      } else if (variable %in% dna_scale$call$variable2) {
        colnames(x) <- gsub("^beta.", "", colnames(x))
      }
    } else if ("dna_scale1dord" %in% class(dna_scale)) {
      if (variable == "both") {
        colnames(x) <- gsub("^Lambda|^phi.|.2$", "", colnames(x))
      } else if (variable %in% dna_scale$call$variable1) {
        colnames(x) <- gsub("^phi.|.2$", "", colnames(x))
      } else if (variable %in% dna_scale$call$variable2) {
        colnames(x) <- gsub("^Lambda|.2$", "", colnames(x))
      }
    } else if ("dna_scale2dbin" %in% class(dna_scale)) {
      colnames(x) <- gsub(".1$", "_dim1", colnames(x))
      colnames(x) <- gsub(".2$", "_dim2", colnames(x))
      if (variable %in% dna_scale$call$variable1 |
          variable == "both") {
        colnames(x) <- gsub("^theta.", "", colnames(x))
      }
      if (variable %in% dna_scale$call$variable2 |
          variable == "both") {
        colnames(x) <- gsub("^beta.", "", colnames(x))
      }
    } else if ("dna_scale2dord" %in% class(dna_scale)) {
      colnames(x) <- gsub(".2$", "_dim1", colnames(x))
      colnames(x) <- gsub(".3$", "_dim2", colnames(x))
      if (variable %in% dna_scale$call$variable1 |
          variable == "both") {
        colnames(x) <- gsub("^phi.", "", colnames(x))
      }
      if (variable %in% dna_scale$call$variable2 |
          variable == "both") {
        colnames(x) <- gsub("^Lambda", "", colnames(x))
      }
    }
  }
  if (method == "geweke") {
    g <- sort(abs(coda::geweke.diag(x)$z), decreasing = FALSE)
  } else if (method == "trace" | method == "density") {
    suppressMessages(z <- reshape2::melt(as.data.frame(x)))
    if (!is.null(colors)) {
      if ("dna_scale1dbin" %in% class(dna_scale) |
          "dna_scale1dord" %in% class(dna_scale)) {
        z <- dplyr::group_by(z, variable)
      } else {
        z$name <- gsub("_dim1$|_dim2$", "", z$variable)
        z <- dplyr::group_by(z, name)
      }
      if ("dna_scale1dbin" %in% class(dna_scale) |
          "dna_scale1dord" %in% class(dna_scale)) {
        if (isTRUE(colors)) {
          z$color <- dna_scale$attributes$color[match(z$variable, dna_scale$attributes$Row.names)]
        } else if (isTRUE(colors == FALSE)) {
          z$color <- "#000000"
        } else if (is.data.frame(colors)) {
          if (!("names" %in% colnames(colors) |
                "colors" %in% colnames(colors))) {
            stop("Cannot find column names specified as \"names\" or ",
                 "\"colors\" in 'colors' object. Please provide both columns ",
                 "with matching values.")
          }
          if (nrow(colors) < length(unique(z$variable))) {
            stop("Values in 'colors' are not equal to values in dna_scale ",
                 "object. Please add the following values:\n",
                 paste(unique(z$variable)[!unique(z$variable) %in% colors$names],
                       collapse = "\n"))
          }
          if (!(all(unique(z$variable) %in% colors$names))) {
            stop("Not all dna_scale values are included in the 'colors' ",
                 "object. Please add the following values:\n",
                 paste(unique(z$variable)[!unique(z$variable) %in% colors$names],
                       collapse = "\n"))
          }
          z$color <- colors$colors[match(z$variable, colors$names)]
        } else if (is.character(colors) | is.numeric(colors)) {
          if (length(colors) == 1) {
            z$color <- colors
          } else if (any(z$variable %in% names(colors))) {
            if (length(colors) < length(unique(z$variable))) {
              stop("Values in 'colors' are not equal to values in dna_scale ",
                   "object. Please add the following values:\n",
                   paste(unique(z$variable)[!unique(z$variable) %in% names(colors)],
                         collapse = "\n"))
            }
            if (!(all(unique(z$variable) %in% names(colors)))) {
              stop("Not all dna_scale values are included in the 'colors' ",
                   "object. Please add the following values:\n",
                   paste(unique(z$variable)[!unique(z$variable) %in% names(colors)],
                         collapse = "\n"))
            }
            z$color <- colors[match(z$variable, names(colors))]
          } else {
            if (length(colors) != length(unique(z$variable))) {
              stop(paste0("Values of 'colors' must equal values in dna_scale",
                          " object (", length(unique(z$variable)), ")."))
            }
            z$color <- rep(colors, each = nrow(dna_scale$sample))
          }
        }
      } else {
        if (isTRUE(colors)) {
          z$color <- dna_scale$attributes$color[match(z$name, dna_scale$attributes$Row.names)]
        } else if (isTRUE(colors == FALSE)) {
          z$color <- "#000000"
        } else if (is.data.frame(colors)) {
          if (!("names" %in% colnames(colors) |
                "colors" %in% colnames(colors))) {
            stop("Cannot find column names specified as \"names\" or ",
                 "\"colors\" in 'colors' object. Please provide both columns ",
                 "with matching values.")
          }
          if (nrow(colors) < length(unique(z$name))) {
            stop("Values in 'colors' are not equal to values in dna_scale ",
                 "object. Please add the following values:\n",
                 paste(unique(z$name)[!unique(z$name) %in% colors$names],
                       collapse = "\n"))
          }
          if (!(all(unique(z$name) %in% colors$names))) {
            stop("Not all dna_scale values are included in the 'colors' ",
                 "object. Please add the following values:\n",
                 paste(unique(z$name)[!unique(z$name) %in% colors$names],
                       collapse = "\n"))
          }
          z$color <- colors$colors[match(z$name, colors$names)]
        } else if (is.character(colors) | is.numeric(colors)) {
          if (length(colors) == 1) {
            z$color <- colors
          } else if (any(z$name %in% names(colors))) {
            if (length(colors) < length(unique(z$name))) {
              stop("Values in 'colors' are not equal to values in dna_scale ",
                   "object. Please add the following values:\n",
                   paste(unique(z$name)[!unique(z$name) %in% names(colors)],
                         collapse = "\n"))
            }
            if (!(all(unique(z$name) %in% names(colors)))) {
              stop("Not all dna_scale values are included in the 'colors' ",
                   "object. Please add the following values:\n",
                   paste(unique(z$name)[!unique(z$name) %in% names(colors)],
                         collapse = "\n"))
            }
            z$color <- colors[match(z$name, names(colors))]
          } else {
            if (length(colors) != length(unique(z$name))) {
              stop(paste0("Values of 'colors' must equal values in dna_scale",
                          " object (", length(unique(z$name)), ")."))
            }
            z$color <- rep(colors, each = nrow(dna_scale$sample))
          }
        }
      }
    }
    if (method == "density") {
      g <- ggplot(z, aes(x = value)) +
        geom_density(aes(color = color, fill = color),
                     alpha = 0.3) +
        theme(axis.text.y = element_blank(),
              axis.ticks.y = element_blank(),
              axis.title = element_blank())
      if (length(unique(z$variable)) == 1) {
        g <- g + ggtitle(z$variable)
      } else if (length(unique(z$variable)) > 1 &
                 length(unique(z$variable)) <= (nrow * ncol)) {
        g <- g + facet_wrap(~variable, scales = "free")
      } else if (length(unique(z$variable)) > (nrow * ncol)) {
        # Calculate number of pages for facets
        n_pages <- ceiling(length(unique(z$variable)) / (nrow * ncol))
        if (facet_page > n_pages) {
          stop(paste0("The specified 'facet_page' is higher than the maximum ",
                      "number of pages (", n_pages, ")."))
        }
        g <- g + ggforce::facet_wrap_paginate(~variable,
                                              scales = "free",
                                              nrow = nrow,
                                              ncol = ncol,
                                              page = facet_page)
      }
    } else if (method == "trace") {
      z$it <- 1:nrow(dna_scale$sample)
      g <- ggplot(z, aes(y = value, x = it)) +
        geom_line(color = z$color, size = trace_size) +
        theme(axis.title = element_blank())
      if (length(unique(z$variable)) == 1) {
        g <- g + ggtitle(z$variable)
      } else if (length(unique(z$variable)) > 1 &
                 length(unique(z$variable)) <= (nrow * ncol)) {
        g <- g + facet_wrap(~variable,
                            scales = "free",
                            nrow = nrow,
                            ncol = ncol)
      } else if (length(unique(z$variable)) > (nrow * ncol)) {
        # Calculate number of pages for facets
        n_pages <- ceiling(length(unique(z$variable)) / (nrow * ncol))
        if (facet_page > n_pages) {
          stop(paste0("The specified 'facet_page' is higher than the maximum ",
                      "number of pages (", n_pages, ")."))
        }
        g <- g + ggforce::facet_wrap_paginate(~variable,
                                              scales = "free",
                                              nrow = nrow,
                                              ncol = ncol,
                                              page = facet_page)
      }
    }
    if (!is.null(colors)) {
      g <- g + scale_color_identity() + scale_fill_identity()
    }
  }
  return(g)
}


#' Plots frequency of statements over time.
#'
#' This function plots frequency of statements over time from a DNA connection
#' established with \link{dna_connection}. A second variable can be used to
#' group the bars in the resulting barplot..
#'
#' @param connection A \code{dna_connection} object created by the
#'   \link{dna_connection} function.
#' @param of A variable which is used to group the bars. Can be
#'   \code{"agreement"}, \code{"organization"}, \code{"person"},
#'   \code{"concept"} or \code{NULL} to disregard differences.
#' @param timewindow Bars represent all statements made during certain
#'   timewindow. This can be \code{"days"}, \code{"months"} or \code{"years"}.
#' @param bar Determines if bars should be stacked (\code{"stacked"}) or
#'   side-by-side (\code{"side"}).
#' @param ... Additional arguments passed to \link{dna_network}.
#'
#' @examples
#' \dontrun{
#' dna_init()
#' conn <- dna_connection(dna_sample())
#'
#' dna_plotFrequency(connection = conn,
#'                   of = "agreement",
#'                   timewindow = "days",
#'                   bar = "stacked")
#' }
#'
#' @author Johannes B. Gruber
#'
#' @export
#' @import ggplot2
dna_plotFrequency <- function(connection,
                              of = "agreement",
                              timewindow = "days",
                              bar = "stacked",
                              ...) {
  att <- dna_getAttributes(connection)
  att$id <- NULL
  dta <- dna_network(connection = connection,
                     networkType = "eventlist",
                     verbose = FALSE,
                     ...)
  if (timewindow == "days") {
    dta$date <- as.Date(format.POSIXct(dta$time,
                                       format = "%Y-%m-%d"))
  } else if (timewindow == "months") {
    dta$date <- as.Date(paste0(format.POSIXct(dta$time,
                                              format = "%Y-%m"),
                               "-01"))
  }else if (timewindow == "years") {
    dta$date <- as.Date(paste0(format.POSIXct(dta$time,
                                              format = "%Y"),
                               "-01-01"))
  }
  dta_count <- as.data.frame(table(dta[, c("date",
                                           of)]))
  colnames(dta_count)[c(1, length(colnames(dta_count)))] <-
    c("Date", "Frequency")
  if (ncol(dta_count) > 2) {
    g <- ggplot(dta_count,
                aes_string(x = "Date",
                           y = "Frequency",
                           fill = of))
  } else {
    g <- ggplot(dta_count,
                aes_string(x = "Date",
                           y = "Frequency"))
  }
  if (bar == "stacked") {
    g <- g +
      geom_bar(stat = "identity")
  } else if (bar == "side") {
    g <- g +
      geom_bar(stat = "identity",
               position = "dodge")
  }
  return(g)
}


#' Convert colors to hex RGB
#'
#' This function accepts one or more colors, provided as integer numeric values
#' or character values, and converts them into hexadecimal RGB colors, such as
#' "#00FF00".
#'
#' @param x A single or multiple color names or numeric values.
#' @return A single or multiple hexadecimal color strings.
#'
#' @importFrom grDevices col2rgb rgb
#' @author Johannes B. Gruber, Philip Leifeld
#'
#' @export
col2hex <- function(x) {
  x <- tryCatch({
    grDevices::col2rgb(x)
  }, error = function(e) {
    if (is.character(x)) {
      x <- as.factor(x)
    }
    if (is.factor(x)) {
      if (length(unique(x)) > 8) {
        warning("Too many unique values for converting factor levels into colors. A maximum of 8 is sensible. Repeating colors now.")
      }
      x <- as.numeric(x)
    }
    grDevices::col2rgb(x)
  })
  x <- apply(x, 2, function (x) grDevices::rgb(red = x[1] / 255, green = x[2] / 255, blue = x[3] / 255))
  return(x)
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
#' @noRd
#'
#' @author Johannes B. Gruber
trim <- function(x, n, e = "...") {
  ifelse(nchar(x) > n,
         paste0(gsub("\\s+$", "",
                     strtrim(x, width = n)),
                e),
         x)
}

#' @noRd
bin_recode <- function(connection,
                       variable1,
                       variable2,
                       qualifier,
                       threshold,
                       drop_min_actors,
                       drop_min_concepts,
                       store_variables,
                       dots) {
  if ("excludeValues" %in% names(dots)) {
    excludeValues <- dots["excludeValues"][[1]]
    dots["excludeValues"] <- NULL
  } else {
    excludeValues <- list()
  }
  if ("invertValues" %in% names(dots)) {
    invertValues <- dots["invertValues"][[1]]
    dots["invertValues"] <- NULL
  } else {
    invertValues <- FALSE
  }
  if ("normalization" %in% names(dots)) {
    dots["normalization"] <- NULL
    warning("'normalization' is not supported in dna_scale and will be ",
            "ignored.")
  }
  if ("qualifierAggregation" %in% names(dots)) {
    dots["qualifierAggregation"] <- NULL
    warning("'qualifierAggregation' is not supported in dna_scale and ",
            "will be ignored.")
  }
  if (any(names(formals(dna_network)) %in% names(dots))) {
    dots_nw <- dots[names(dots) %in% names(formals(dna_network))]
    dots[names(dots) %in% names(formals(dna_network))] <- NULL
  } else {
    dots_nw <- list()
  }
  if (!is.character(variable1) | !is.character(variable2)) {
    stop ("'variable1' and 'variable2' must be provided as character objects.")
  }
  if (!is.character(store_variables)) {
    stop ("'store_variables' must be provided as a character object.")
  }
  if (isTRUE(threshold > 1)) {
    threshold <- threshold / 100
  }
  if (!(store_variables == "both" |
        store_variables == variable1 |
        store_variables == variable2)) {
    stop ("'store_variables' does not match with 'variable1' or 'variable2'. ",
          "Please match 'store_variables' with variables in 'variable1' or ",
          "'variable2', or use \"both\" in case you want to store both ",
          "variables.")
  }
  # Check if non-binary structure in agreement
  nw <- do.call("dna_network", c(list(connection = connection,
                                      networkType = "eventlist",
                                      variable1 = variable1,
                                      variable2 = variable2,
                                      qualifier = qualifier,
                                      verbose = FALSE,
                                      excludeValues = excludeValues,
                                      invertValues = invertValues),
                                 dots_nw))
  if (!all(unique(nw[, qualifier])) %in% c(0, 1)) {
    nw <- do.call("dna_network", c(list(connection = connection,
                                        networkType = "twomode",
                                        variable1 = variable1,
                                        variable2 = variable2,
                                        qualifier = qualifier,
                                        qualifierAggregation = "ignore",
                                        verbose = FALSE,
                                        excludeValues = excludeValues,
                                        invertValues = invertValues),
                                   dots_nw))
    nw2 <- as.vector(nw)
    nw2 <- scales::rescale (nw2, to = c(0, 1))
    nw2 <- matrix(nw2,
                  ncol = ncol(nw),
                  nrow = nrow(nw),
                  dimnames = list(rownames(nw),
                                  colnames(nw)))
    nw2[nw2 >= 0.5] <- 1
    nw2[nw2 < 0.5] <- 0
    if (!is.null(threshold)) {
      warning("'threshold' is not supported and will be ignored.")
    }
  } else {
    # retrieve data from network
    nw <- do.call("dna_network", c(list(connection = connection,
                                        networkType = "twomode",
                                        variable1 = variable1,
                                        variable2 = variable2,
                                        qualifier = qualifier,
                                        qualifierAggregation = "combine",
                                        verbose = FALSE,
                                        excludeValues = excludeValues,
                                        invertValues = invertValues),
                                   dots_nw))
    if (is.null(threshold)) {
      # change structure of network according to scaling type
      nw2 <- nw
      nw2[nw == 0 | nw == 3] <- NA
      nw2[nw == 2] <- 0
    } else {
      # Include threshold in export of network
      nw_pos <- do.call("dna_network", c(list(
        connection = connection,
        networkType = "twomode",
        variable1 = variable1,
        variable2 = variable2,
        qualifier = qualifier,
        qualifierAggregation = "ignore",
        isolates = TRUE,
        verbose = FALSE,
        excludeValues = c(list("agreement" =
                                 ifelse(invertValues, 1, 0)),
                          excludeValues),
        invertValues = invertValues),
        dots_nw))
      nw_neg <- do.call("dna_network", c(list(
        connection = connection,
        networkType = "twomode",
        variable1 = variable1,
        variable2 = variable2,
        qualifier = qualifier,
        qualifierAggregation = "ignore",
        isolates = TRUE,
        verbose = FALSE,
        excludeValues = c(list("agreement" =
                                 ifelse(invertValues, 0, 1)),
                          excludeValues),
        invertValues = invertValues),
        dots_nw))
      nw_com <- (nw_pos - nw_neg) / (nw_pos + nw_neg)
      nw2 <- nw_com
      threshold <- threshold * 2 - 1
      nw2[is.nan(nw_com)] <- NA
      nw2[nw_com < threshold &
            nw_com > -threshold] <- NA
      nw2[nw_com <= -threshold] <- 0
      nw2[nw_com >= threshold] <- 1
      nw2 <- nw2[match(rownames(nw), rownames(nw2)),
                 match(colnames(nw), colnames(nw2))]
    }
  }
  if (isTRUE(drop_min_actors > 1) | isTRUE(drop_min_concepts > 1)) {
    nw_exclude <- nw2
    nw_exclude[nw_exclude == 0] <- 1
    nw_exclude[is.na(nw_exclude)] <- 0
    if (isTRUE(drop_min_actors > 1)) {
      if (drop_min_actors > max(rowSums(nw_exclude))) {
        stop(paste0("The specified number in 'drop_min_actors' is higher than ",
                    "the maximum number of concepts mentioned by an actor (",
                    max(rowSums(nw_exclude))), ").")
      }
      nw2 <- nw2[rowSums(nw_exclude) >= drop_min_actors, ]
    }
    if (isTRUE(drop_min_concepts > 1)) {
      if (drop_min_concepts > max(colSums(nw_exclude))) {
        stop(paste0("The specified number in 'drop_min_concepts' is higher ",
                    "than the maximum number of jointly mentioned concepts (",
                    max(colSums(nw_exclude))), ").")
      }
      nw2 <- nw2[, colSums(nw_exclude) >= drop_min_concepts]
    }
  }
  # Test if actor is without any statements
  filter_actor <- sapply(rownames(nw2), function(c) {
    !sum(is.na(nw2[c, ]) * 1) >= ncol(nw2)
  })
  # Test if only one concept used by actor
  filter_concept <- sapply(colnames(nw2), function(c) {
    !sum(is.na(nw2[, c]) * 1) >= nrow(nw2) - 1
  })
  nw2 <- nw2[filter_actor, filter_concept]
  if ("FALSE" %in% filter_concept) {
    if (drop_min_actors > 1 & drop_min_concepts >= 2) {
      warning("After deleting actors with 'drop_min_actors', some concepts ",
              "are now mentioned by less than the two required actors. The ",
              "follwing concepts have therefore not been included in the ",
              "scaling:\n",
              paste(names(filter_concept[filter_concept == FALSE]),
                    collapse = "\n"))
    } else {
      warning("dna_scale requires concepts mentioned by at least two actors. ",
              "The following concepts have therefore not been included in the ",
              "scaling:\n",
              paste(names(filter_concept[filter_concept == FALSE]),
                    collapse = "\n"))
    }
  }
  if ("FALSE" %in% filter_actor) {
    if (drop_min_concepts >= 1) {
      warning("After deleting concepts with 'drop_min_concepts', some actors ",
              "now have less than one statement. The following actors have ",
              "therefore not been included in the scaling:\n",
              paste(names(filter_actor[filter_actor == FALSE]),
                    collapse = "\n"))
    } else {
      warning("Some actors do not have any statements and were not included in",
              " the scaling. Setting or lowering the 'threshold' might include",
              " them:\n",
              paste(names(filter_actor[filter_actor == FALSE]),
                    collapse = "\n"))
    }
  }
  out <- list(nw2 = nw2,
              dots = dots,
              dots_nw = dots_nw,
              excludeValues = excludeValues,
              invertValues = invertValues)
  return(out)
}


#' @noRd
ord_recode <- function(connection,
                       variable1,
                       variable2,
                       qualifier,
                       zero_as_na,
                       threshold,
                       drop_min_actors,
                       drop_min_concepts,
                       store_variables,
                       dots) {
  if ("excludeValues" %in% names(dots)) {
    excludeValues <- dots["excludeValues"][[1]]
    dots["excludeValues"] <- NULL
  } else {
    excludeValues <- list()
  }
  if ("invertValues" %in% names(dots)) {
    invertValues <- dots["invertValues"][[1]]
    dots["invertValues"] <- NULL
  } else {
    invertValues <- FALSE
  }
  if ("normalization" %in% names(dots)) {
    dots["normalization"] <- NULL
    warning("'normalization' is not supported in dna_scale and will be ",
            "ignored.")
  }
  if ("qualifierAggregation" %in% names(dots)) {
    dots["qualifierAggregation"] <- NULL
    warning("'qualifierAggregation' is not supported in dna_scale and will be ",
            "ignored.")
  }
  if (any(names(formals(dna_network)) %in% names(dots))) {
    dots_nw <- dots[names(dots) %in% names(formals(dna_network))]
    dots[names(dots) %in% names(formals(dna_network))] <- NULL
  } else {
    dots_nw <- list()
  }
  if (!is.character(variable1) | !is.character(variable2)) {
    stop ("'variable1' and 'variable2' must be provided as character objects.")
  }
  if (!is.character(store_variables)) {
    stop ("'store_variables' must be provided as a character object.")
  }
  if (isTRUE(threshold > 1)) {
    threshold <- threshold / 100
  }
  if (!(store_variables == "both" |
        store_variables == variable1 |
        store_variables == variable2)) {
    stop ("'store_variables' does not match with 'variable1' or 'variable2'. ",
          "Please match 'store_variables' with variables in 'variable1' or ",
          "'variable2', or use \"both\" in case you want to store both ",
          "variables.")
  }
  # Check if non-binary structure in agreement
  nw <- do.call("dna_network", c(list(connection = connection,
                                      networkType = "eventlist",
                                      variable1 = variable1,
                                      variable2 = variable2,
                                      qualifier = qualifier,
                                      verbose = FALSE,
                                      excludeValues = excludeValues,
                                      invertValues = invertValues),
                                 dots_nw))
  if (!all(unique(nw[, qualifier])) %in% c(0, 1)) {
    nw <- do.call("dna_network", c(list(connection = connection,
                                        networkType = "twomode",
                                        variable1 = variable1,
                                        variable2 = variable2,
                                        qualifier = qualifier,
                                        qualifierAggregation = "ignore",
                                        verbose = FALSE,
                                        excludeValues = excludeValues,
                                        invertValues = invertValues),
                                   dots_nw))
    if (zero_as_na == TRUE) {
      nw[nw == 0] <- NA
    }
    if (!is.null(threshold)) {
      warning("'threshold' is not supported and will be ignored.")
    }
  } else {
    # retrieve data from network
    nw <- do.call("dna_network", c(list(connection = connection,
                                        networkType = "twomode",
                                        variable1 = variable1,
                                        variable2 = variable2,
                                        qualifier = qualifier,
                                        qualifierAggregation = "combine",
                                        verbose = FALSE,
                                        excludeValues = excludeValues,
                                        invertValues = invertValues),
                                   dots_nw))
    if (is.null(threshold)) {
      # change structure of network according to scaling type
      nw2 <- nw
      if (zero_as_na == TRUE) {
        nw2[nw == 0] <- NA
        nw2[nw == 1] <- 3
        nw2[nw == 2] <- 1
        nw2[nw == 3] <- 2
      } else if (zero_as_na == FALSE) {
        nw2[nw == 0] <- 2
        nw2[nw == 1] <- 3
        nw2[nw == 2] <- 1
        nw2[nw == 3] <- 2
      }
    } else {
      # Include threshold in export of network
      nw_pos <- do.call("dna_network", c(list(
        connection = connection,
        networkType = "twomode",
        variable1 = variable1,
        variable2 = variable2,
        qualifier = qualifier,
        qualifierAggregation = "ignore",
        isolates = TRUE,
        verbose = FALSE,
        excludeValues = c(list("agreement" =
                                 ifelse(invertValues, 1, 0)),
                          excludeValues),
        invertValues = invertValues),
        dots_nw))
      nw_neg <- do.call("dna_network", c(list(
        connection = connection,
        networkType = "twomode",
        variable1 = variable1,
        variable2 = variable2,
        qualifier = qualifier,
        qualifierAggregation = "ignore",
        isolates = TRUE,
        verbose = FALSE,
        excludeValues = c(list("agreement" =
                                 ifelse(invertValues, 0, 1)),
                          excludeValues),
        invertValues = invertValues),
        dots_nw))
      nw_com <- (nw_pos - nw_neg) / (nw_pos + nw_neg)
      nw2 <- nw_com
      threshold <- threshold * 2 - 1
      if (zero_as_na == TRUE) {
        nw2[is.nan(nw_com)] <- NA
        nw2[nw_com < threshold &
              nw_com > -threshold] <- 2
        nw2[nw_com <= -threshold] <- 1
        nw2[nw_com >= threshold] <- 3
      } else {
        nw2[is.nan(nw_com)] <- 2
        nw2[nw_com < threshold &
              nw_com > -threshold] <- 2
        nw2[nw_com <= -threshold] <- 1
        nw2[nw_com >= threshold] <- 3
      }
      nw2 <- nw2[match(rownames(nw), rownames(nw2)),
                 match(colnames(nw), colnames(nw2))]
    }
  }
  if (isTRUE(drop_min_actors > 1) | isTRUE(drop_min_concepts > 1)) {
    if (zero_as_na == FALSE) {
      if (is.null(threshold)) {
        nw_exclude <- nw
        nw_exclude[nw_exclude > 1] <- 1
      } else {
        nw_exclude <- nw_com
        nw_exclude[!is.nan(nw_exclude)] <- 1
        nw_exclude[is.nan(nw_exclude)] <- 0
      }
    } else {
      nw_exclude <- nw2
      nw_exclude[nw_exclude > 1] <- 1
      nw_exclude[is.na(nw_exclude)] <- 0
    }
    if (isTRUE(drop_min_actors > 1)) {
      if (drop_min_actors > max(rowSums(nw_exclude))) {
        stop(paste0("The specified number in 'drop_min_actors' is higher than ",
                    "the maximum number of concepts mentioned by an actor (",
                    max(rowSums(nw_exclude))), ").")
      }
      nw2 <- nw2[rowSums(nw_exclude) >= drop_min_actors, ]
    }
    if (isTRUE(drop_min_concepts > 1)) {
      if (drop_min_concepts > max(colSums(nw_exclude))) {
        stop(paste0("The specified number in 'drop_min_concepts' is higher ",
                    "than the maximum number of jointly mentioned concepts (",
                    max(colSums(nw_exclude))), ").")
      }
      nw2 <- nw2[, colSums(nw_exclude) >= drop_min_concepts]
    }
  }
  # Test if actor is without any statements
  filter_actor <- sapply(rownames(nw2), function(c) {
    !sum(is.na(nw2[c, ]) * 1) >= ncol(nw2)
  })
  # Test if only one concept used by actor
  filter_concept <- sapply(colnames(nw2), function(c) {
    !sum(is.na(nw2[, c]) * 1) >= nrow(nw2) - 1
  })
  nw2 <- nw2[filter_actor, filter_concept]
  if ("FALSE" %in% filter_concept) {
    if (drop_min_actors > 1 & drop_min_concepts >= 2) {
      warning("After deleting actors with 'drop_min_actors', some concepts ",
              "are now mentioned by less than the two required actors. The ",
              "follwing concepts have therefore not been included in the ",
              "scaling:\n",
              paste(names(filter_concept[filter_concept == FALSE]),
                    collapse = "\n"))
    } else {
      warning("dna_scale requires concepts mentioned by at least two actors. ",
              "The following concepts have therefore not been included in the ",
              "scaling:\n",
              paste(names(filter_concept[filter_concept == FALSE]),
                    collapse = "\n"))
    }
  }
  if ("FALSE" %in% filter_actor) {
    if (drop_min_concepts >= 1) {
      warning("After deleting concepts with 'drop_min_concepts', some actors ",
              "now have less than one statement. The following actors have ",
              "therefore not been included in the scaling:\n",
              paste(names(filter_actor[filter_actor == FALSE]),
                    collapse = "\n"))
    } else {
      warning("Some actors do not have any statements and were not included in",
              " the scaling. Setting or lowering the 'threshold' might include",
              " them:\n",
              paste(names(filter_actor[filter_actor == FALSE]),
                    collapse = "\n"))
    }
  }
  out <- list(nw2 = nw2,
              dots = dots,
              dots_nw = dots_nw,
              excludeValues = excludeValues,
              invertValues = invertValues)
  return(out)
}
