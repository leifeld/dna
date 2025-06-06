# Package startup --------------------------------------------------------------

dnaEnvironment <- new.env(hash = TRUE, parent = emptyenv())

#' Display version number and date when the package is loaded.
#' @importFrom utils packageDescription
#' @noRd
.onAttach <- function(libname, pkgname) {
  desc <- packageDescription(pkgname, libname)
  packageStartupMessage(
    'Version:      ', desc$Version, '\n',
    'Date:         ', desc$Date, '\n',
    'Author:       Philip Leifeld (University of Manchester)\n',
    'Contributors: Tim Henrichsen (University of Birmingham),\n',
    '              Johannes B. Gruber (Vrije Universiteit Amsterdam)\n',
    '              Kristijan Garic (University of Essex)\n',
    'Project home: github.com/leifeld/dna'
  )
}

#' Initialize the connection with DNA
#'
#' Establish a connection between \pkg{rDNA} and the DNA software.
#'
#' To use \pkg{rDNA}, DNA first needs to be initialized. This means that
#' \pkg{rDNA} needs to be told where the DNA executable file, i.e., the jar
#' file, is located. When the \code{dna_init} function is used, the connection
#' to the DNA software is established, and this connection is valid for the rest
#' of the \R session. To initialize a connection with a different DNA version or
#' path, the \R session would need to be restarted first.
#'
#' @param jarfile The file name of the DNA jar file, e.g.,
#'   \code{"dna-3.0.7.jar"}. Can be auto-detected using the
#'   \code{\link{dna_jar}} function, which looks for a version matching the
#'   installed \pkg{rDNA} version in the library path and working directory.
#' @param memory The amount of memory in megabytes to allocate to DNA, for
#'   example \code{1024} or \code{4096}.
#' @param returnString Return a character object representing the jar file name?
#'
#' @author Philip Leifeld
#'
#' @examples
#' \dontrun{
#' dna_init()
#' }
#'
#' @family startup
#'
#' @export
#' @importFrom rJava .jinit .jnew .jarray
dna_init <- function(jarfile = dna_jar(), memory = 1024, returnString = FALSE) {
  if (is.null(jarfile) || length(jarfile) == 0 || is.na(jarfile)) {
    stop("Invalid jar file name.")
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
  assign("jar", jarfile, pos = dnaEnvironment)
  message(paste("Jar file:", dnaEnvironment[["jar"]]))
  .jinit(dnaEnvironment[["jar"]],
         force.init = TRUE,
         parameters = paste0("-Xmx", memory, "m"))
  dnaEnvironment[["dna"]] <- .jnew("dna.Dna", .jarray("headless"))
  message("DNA connection established.")
  if (isTRUE(returnString)) {
    return(jarfile)
  }
}

#' Get headless DNA
#'
#' Get headless DNA.
#'
#' This function returns a reference to the Java class that represents the
#' R interface of DNA, i.e., the headless API. Headless means that no graphical
#' user interface is necessary to use the functions in DNA. This is the raw API
#' interface and should only be used by expert users or to debug other rDNA
#' functions. You can save the headless DNA object to an object in R and access
#' its Java functions directly as slots using the "$" operator, for example
#' \code{dna_getHeadlessDna()$getExport()} will return a reference to the
#' \code{Exporter} class, which does all the network computations, and
#' \code{dna_getHeadlessDna()$getAttributes()} will return attributes etc. You
#' can view the different functions that are available by using the tab key to
#' auto-complete function access after typing the "$" if the headless reference
#' has been saved to an object in the workspace.
#'
#' @return Headless DNA reference.
#'
#' @author Philip Leifeld
#'
#' @family startup
#'
#' @export
dna_getHeadlessDna <- function() {
  dnaEnvironment[["dna"]]$headlessDna
}

#' Find the DNA jar file
#'
#' Find the DNA jar file in the library path or working directory.
#'
#' rDNA requires the installation of a DNA jar file to run properly. The jar
#' file is shipped with the rDNA package and is installed in the \code{java/}
#' directory of the package installation directory in the R library tree. The
#' version number of the jar file and the rDNA package must match for DNA and
#' rDNA to be able to work together. The \code{dna_jar} function looks for
#' the jar file in the package installation directory sub-directory and
#' returns its file name with its absolute path. If it cannot be found in the
#' installation directory, the function looks in the current working
#' directory. The function is also called by \code{\link{dna_init}} if the
#' location of the jar file is not provided explicitly.
#'
#' @return The file name of the jar file that matches the installed \pkg{rDNA}
#'   version, including full path.
#'
#' @author Philip Leifeld
#'
#' @family startup
#'
#' @importFrom utils download.file unzip packageVersion
#' @export
dna_jar <- function() {
  # detect package version
  v <- as.character(packageVersion("rDNA"))

  # try to locate jar file in library path under java/ and return jar file path
  tryCatch({
    rdna_dir <- dirname(system.file(".", package = "rDNA"))
    jar <- paste0(rdna_dir, "/java/dna-", v, ".jar")
    if (file.exists(jar)) {
      message("Jar file found in library path.")
      return(jar)
    }
  }, error = function(e) {success <- FALSE})

  # try to locate jar file in library path under inst/java/ and return jar file path
  tryCatch({
    rdna_dir <- dirname(system.file(".", package = "rDNA"))
    jar <- paste0(rdna_dir, "/inst/java/dna-", v, ".jar")
    if (file.exists(jar)) {
      message("Jar file found in library path.")
      return(jar)
    }
  }, error = function(e) {success <- FALSE})

  # try to locate jar file in working directory and return jar file path
  tryCatch({
    jar <- paste0(getwd(), "/dna-", v, ".jar")
    if (file.exists(jar)) {
      message("Jar file found in working directory.")
      return(jar)
    }
  }, error = function(e) {success <- FALSE})

  stop("DNA jar file could not be found in the library path or working ",
       "directory. Your current rDNA version is ", v, ".")
}

#' Provides a small sample database
#'
#' A small sample database to test the functions of rDNA.
#'
#' Copies a small .dna sample file to the current working directory and returns
#' the location of this newly created file.
#'
#' @param overwrite Logical. Should \code{sample.dna} be overwritten if found in
#'   the current working directory?
#'
#' @examples
#' \dontrun{
#' dna_init()
#' s <- dna_sample()
#' dna_openDatabase(s)
#' }
#'
#' @author Johannes B. Gruber, Philip Leifeld
#'
#' @family startup
#'
#' @export
dna_sample <- function(overwrite = FALSE) {
  if (file.exists(paste0(getwd(), "/sample.dna")) & overwrite == FALSE) {
    warning("Sample file already exists in working directory. ",
            "Use 'overwrite = TRUE' to revert changes in the sample file.")
  } else {
    file.copy(from = system.file("extdata", "sample.dna", package = "rDNA"),
              to = paste0(getwd(), "/sample.dna"),
              overwrite = overwrite)
  }
  return(paste0(getwd(), "/sample.dna"))
}


# Database connections ---------------------------------------------------------

#' Open a database
#'
#' Open a database in DNA.
#'
#' Open a database in DNA. This can be a SQLite, MySQL, or PostgreSQL database.
#' The database must already have the table structure required for DNA. You must
#' provide the coder ID and password along with the database credentials. To
#' look up coder IDs, use the \code{\link{dna_queryCoders}} function.
#'
#' @param coderId The coder ID of the coder who is opening the database. If an
#'   invalid coder ID is supplied (i.e., \code{-1} or similar), the coder ID is
#'   queried interactively from the user.
#' @param coderPassword The coder password of the coder who is opening the
#'   database. If an empty password is provided (e.g., \code{""}), the password
#'   is queried interactively from the user.
#' @param db_url The URL for accessing the database (for remote databases) or
#'   the path of the SQLite database file, including file extension.
#' @param db_type The type of database. Valid values are \code{"sqlite"},
#'   \code{postgresql}, and \code{postgresql}.
#' @param db_name The name of the database at the given URL or path. Can be a
#'   zero-length character object (\code{""}) for file-based SQLite databases.
#' @param db_port The connection port for the database connection. No port is
#'   required (\code{db_port = -1}) for SQLite databases. MySQL databases often
#'   use port \code{3306}. PostgreSQL databases often use port \code{5432}. If
#'   \code{db_port = NULL}, one of these default values will be selected based
#'   on the \code{db_type} argument.
#' @param db_login The login user name for the database. This is the database
#'   login user name, not the coder name. Can be a zero-length character object
#'   (\code{""}) for SQLite databases.
#' @param db_password The password for the database. This is the database
#'   password, not the coder password. Can be a zero-length character object
#'   (\code{""}) for SQLite databases.
#'
#' @author Philip Leifeld
#'
#' @family database
#' @seealso \code{\link{dna_queryCoders}}
#'
#' @examples
#' \dontrun{
#' dna_init()
#' dna_sample()
#' dna_openDatabase(coderId = 1,
#'                  coderPassword = "sample",
#'                  db_url = "sample.dna")
#' }
#'
#' @export
#' @importFrom rJava .jcall
dna_openDatabase <- function(db_url,
                             coderId = 1,
                             coderPassword = "",
                             db_type = "sqlite",
                             db_name = "",
                             db_port = -1,
                             db_login = "",
                             db_password = "") {
  if (is.null(db_port) && !is.null(db_type)) {
    if (db_type == "sqlite") {
      db_port <- as.integer(-1)
    } else if (db_type == "mysql") {
      db_port <- as.integer(3306)
    } else if (db_type == "postgresql") {
      db_port <- as.integer(5432)
    }
  } else {
    db_port <- as.integer(db_port)
  }
  if (db_type == "sqlite") {
    if (file.exists(db_url)) {
      db_url <- normalizePath(db_url)
    } else {
      stop("Database file not found.")
    }
  }
  if (is.null(coderId) || !is.numeric(coderId) || coderId < 1) {
    if (!requireNamespace("askpass", quietly = TRUE)) {
      coderId <- as.integer(readline("Coder ID: "))
    } else {
      coderId <- as.integer(askpass::askpass("Coder ID: "))
    }
  }
  if (is.null(coderId) || length(coderId) == 0) {
    coderId <- -1
  }
  if (is.null(coderPassword) || !is.character(coderPassword) || coderPassword == "") {
    if (!requireNamespace("askpass", quietly = TRUE)) {
      coderPassword <- readline("Coder password: ")
    } else {
      coderPassword <- askpass::askpass("Coder password: ")
    }
  }
  if (is.null(coderPassword) || length(coderPassword) == 0) {
    coderPassword <- ""
  }
  q <- .jcall(dnaEnvironment[["dna"]]$headlessDna,
              "Z",
              "openDatabase",
              as.integer(coderId),
              coderPassword,
              db_type,
              db_url,
              db_name,
              db_port,
              db_login,
              db_password)
}

#' Close the open DNA database (if any).
#'
#' Close the DNA database that is currently active (if any).
#'
#' Close the currently active DNA database and display a message confirming that
#' the database was closed.
#'
#' @author Philip Leifeld
#'
#' @examples
#' \dontrun{
#' dna_init()
#' dna_sample()
#' dna_openDatabase(coderId = 1,
#'                  coderPassword = "sample",
#'                  db_url = "sample.dna")
#' dna_closeDatabase()
#' }
#'
#' @family database
#'
#' @export
#' @importFrom rJava .jcall
dna_closeDatabase <- function() {
  .jcall(dnaEnvironment[["dna"]]$headlessDna, "V", "closeDatabase")
}

#' Open a connection profile
#'
#' Open a connection profile and establish a connection to the database.
#'
#' Load a connection profile from a \code{.dnc} file. The file contains
#' connection details for a database (like a bookmark) along with the coder ID
#' of the coder who saved the connection profile. By loading the connection
#' profile, a connection to the database will be established by DNA, and the
#' coder saved in the connection profile will be activated. The coder password
#' the user needs to provide is the coder password for the coder saved in the
#' connection profile. It serves to decrypt the information stored in the file
#' and activate the coder in the database connection. If an empty character
#' object is provided as the password (\code{""}), the user will be prompted
#' interactively for a password. If the \pkg{askpass} package is installed, this
#' package will be used to mask the user input; otherwise the password is
#' visible in clear text. Installing the \pkg{askpass} package is strongly
#' recommended.
#'
#' @param file The file name of the connection profile to open.
#' @param coderPassword The clear text coder password. If a zero-length
#'   character object (\code{""}) is provided, the user will be prompted
#'   for a password interactively.
#'
#' @author Philip Leifeld
#'
#' @examples
#' \dontrun{
#' dna_init()
#' dna_sample()
#' dna_openDatabase(coderId = 1,
#'                  coderPassword = "sample",
#'                  db_url = "sample.dna")
#' dna_saveConnectionProfile(file = "my profile.dnc", coderPassword = "sample")
#' dna_closeDatabase()
#' dna_openConnectionProfile(file = "my profile.dnc", coderPassword = "sample")
#' }
#'
#' @family database
#'
#' @export
#' @importFrom rJava .jcall
dna_openConnectionProfile <- function(file, coderPassword = "") {
  if (is.null(file) || !is.character(file) || length(file) != 1) {
    stop("Please provide a file name for the connection profile.")
  }
  if (!file.exists(file)) {
    stop("File does not exist.")
  } else {
    file <- normalizePath(file)
  }
  if (is.null(coderPassword) || !is.character(coderPassword) || coderPassword == "") {
    if (!requireNamespace("askpass", quietly = TRUE)) {
      coderPassword <- readline("Coder password: ")
    } else {
      coderPassword <- askpass::askpass("Coder password: ")
    }
  }
  if (is.null(coderPassword) || length(coderPassword) == 0) {
    coderPassword <- ""
  }
  s <- .jcall(dnaEnvironment[["dna"]]$headlessDna,
              "Z",
              "openConnectionProfile",
              file,
              coderPassword)
}

#' Save a connection profile to a file
#'
#' Save connection profile for the current coder and database to disk
#'
#' Save the current database URL/path, user name, password, port, database name,
#' and coder to an encrypted JSON file with the extension \code{.dnc}. This file
#' is called a connection profile. It serves as a bookmark and saves you from
#' having to enter and store the full connection details each time you want to
#' access the database. Please make sure you enter the file name with the
#' extension. You are asked to provide the coder password of the currently
#' active coder again, for whom the connection profile is saved. This is just
#' for security reasons. If you do not provide a coder password (e.g., your
#' password is a zero-length character object \code{""}), you are asked to enter
#' the password interactively. If the \pkg{askpass} package is installed, this
#' package will be used to mask the user input; otherwise the password is
#' visible in clear text. Installing the \pkg{askpass} package is strongly
#' recommended.
#'
#' @param file The file name of the connection profile to save.
#' @param coderPassword The clear text coder password. If a zero-length
#'   character object (\code{""}) is provided, the user will be prompted
#'   for a password interactively.
#'
#' @author Philip Leifeld
#'
#' @family database
#'
#' @examples
#' \dontrun{
#' dna_init()
#' dna_sample()
#' dna_openDatabase(coderId = 1,
#'                  coderPassword = "sample",
#'                  db_url = "sample.dna")
#' dna_saveConnectionProfile(file = "my profile.dnc", coderPassword = "sample")
#' }
#'
#' @export
#' @importFrom rJava .jcall
dna_saveConnectionProfile <- function(file, coderPassword = "") {
  if (is.null(file) || !is.character(file) || length(file) != 1) {
    stop("Please provide a file name for the connection profile.")
  }
  if (is.null(coderPassword) || !is.character(coderPassword) || coderPassword == "") {
    if (!requireNamespace("askpass", quietly = TRUE)) {
      coderPassword <- readline("Coder password: ")
    } else {
      coderPassword <- askpass::askpass("Coder password: ")
    }
  }
  if (is.null(coderPassword) || length(coderPassword) == 0) {
    coderPassword <- ""
  }
  s <- .jcall(dnaEnvironment[["dna"]]$headlessDna,
              "Z",
              "saveConnectionProfile",
              file,
              coderPassword)
}

#' Print database details
#'
#' Print number of documents and statements and active coder.
#'
#' For the DNA database that is currently open, print the number of documents
#' and statements, the URL, statement types (and their statement counts), and
#' the active coder to the console.
#'
#' @author Philip Leifeld
#'
#' @examples
#' \dontrun{
#' dna_init()
#' dna_sample()
#' dna_openDatabase(coderId = 1,
#'                  coderPassword = "sample",
#'                  db_url = "sample.dna")
#' dna_printDetails()
#' }
#'
#' @family database
#'
#' @export
#' @importFrom rJava .jcall
dna_printDetails <- function() {
  .jcall(dnaEnvironment[["dna"]]$headlessDna, "V", "printDatabaseDetails")
}

#' Get a reference to the headless Java class for R (API)
#'
#' Get a reference to the headless Java class for R (API).
#'
#' This function returns a Java object reference to the instance of the
#' \code{Dna/HeadlessDna} class in the DNA JAR file that is held in the rDNA
#' package environment and used by the functions in the package to exchange data
#' with the Java application. You can use the \pkg{rJava} package to access the
#' available functions in this class directly. API access requires detailed
#' knowledge of the DNA JAR classes and functions and is recommended for
#' developers and advanced users only.
#'
#' @return A Java object reference to the \code{Dna/HeadlessDna} class.
#'
#' @author Philip Leifeld
#'
#' @examples
#' \dontrun{
#' library("rJava") # load rJava package to use functions in the Java API
#' dna_init()
#' dna_sample()
#' dna_openDatabase(coderId = 1,
#'                  coderPassword = "sample",
#'                  db_url = "sample.dna")
#' api <- dna_api()
#'
#' # use the \code{getVariables} function to retrieve variables
#' variable_references <- api$getVariables("DNA Statement")
#'
#' # iterate through variable references and print their data type
#' for (i in seq(variable_references$size()) - 1) {
#'   print(variable_references$get(as.integer(i))$getDataType())
#' }
#' }
#'
#' @family database
#'
#' @export
dna_api <- function() {
  return(dnaEnvironment[["dna"]]$headlessDna)
}

# Coder management--------------------------------------------------------------

#' Query the coders in a database
#'
#' Display the coder IDs, names, and colors present in a DNA database.
#'
#' Some functions require knowing the coder ID with which changes should be
#' made. This function queries any database, which does not have to be opened,
#' for their coder IDs, names, and colors, and returns them as a data frame.
#'
#' @param db_url The URL or full path of the database.
#' @param db_type The type of database. Valid values are \code{"sqlite"},
#'   \code{postgresql}, and \code{postgresql}.
#' @param db_name The name of the database at the given URL or path. Can be a
#'   zero-length character object (\code{""}) for file-based SQLite databases.
#' @param db_port The connection port for the database connection. No port is
#'   required (\code{db_port = -1}) for SQLite databases. MySQL databases often
#'   use port \code{3306}. PostgreSQL databases often use port \code{5432}. If
#'   \code{db_port = NULL}, one of these default values will be selected based
#'   on the \code{db_type} argument.
#' @param db_login The login user name for the database. This is the database
#'   login user name, not the coder name. Can be a zero-length character object
#'   (\code{""}) for SQLite databases.
#' @param db_password The password for the database. This is the database
#'   password, not the coder password. Can be a zero-length character object
#'   (\code{""}) for SQLite databases.
#'
#' @author Philip Leifeld
#'
#' @examples
#' \dontrun{
#' dna_init()
#' dna_sample()
#' dna_queryCoders("sample.dna")
#' }
#'
#' @export
#' @importFrom rJava .jcall .jevalArray
dna_queryCoders <- function(db_url,
                            db_type = "sqlite",
                            db_name = "",
                            db_port = NULL,
                            db_login = "",
                            db_password = "") {
  if (is.null(db_port) && !is.null(db_type)) {
    if (db_type == "sqlite") {
      db_port <- as.integer(-1)
    } else if (db_type == "mysql") {
      db_port <- as.integer(3306)
    } else if (db_type == "postgresql") {
      db_port <- as.integer(5432)
    }
  } else {
    db_port <- as.integer(db_port)
  }
  q <- .jcall(dnaEnvironment[["dna"]]$headlessDna,
              "[Ljava/lang/Object;",
              "queryCoders",
              db_type,
              ifelse(db_type == "sqlite", normalizePath(db_url), db_url),
              db_name,
              db_port,
              db_login,
              db_password)
  names(q) <- c("ID", "Name", "Color")
  q <- lapply(q, .jevalArray)
  q <- as.data.frame(q, stringsAsFactors = FALSE)
  return(q)
}


# Variables --------------------------------------------------------------------

#' Retrieve a dataframe with all variables for a statement type
#'
#' Retrieve a dataframe with all variables defined in a given statement type.
#'
#' For a given statement type ID or label, this function creates a data frame
#' with one row per variable and contains columns for the variable ID, name and
#' data type.
#'
#' @param statementType The statement type for which statements should be
#'   retrieved. The statement type can be supplied as an integer or character
#'   string, for example \code{1} or \code{"DNA Statement"}.
#'
#' @examples
#' \dontrun{
#' dna_init()
#' samp <- dna_sample()
#' dna_openDatabase(samp, coderId = 1, coderPassword = "sample")
#' variables <- dna_getVariables("DNA Statement")
#' variables
#' }
#'
#' @author Philip Leifeld
#'
#' @importFrom rJava J .jcall
#' @export
dna_getVariables <- function(statementType) {
  if (is.null(statementType) || is.na(statementType) || length(statementType) != 1) {
    stop("'statementType' must be an integer or character object of length 1.")
  }
  if (is.numeric(statementType) && !is.integer(statementType)) {
    statementType <- as.integer(statementType)
  } else if (!is.character(statementType) && !is.integer(statementType)) {
    stop("'statementType' must be an integer or character object of length 1.")
  }

  v <- J(dnaEnvironment[["dna"]]$headlessDna, "getVariables", statementType) # get an array list of Value objects representing the variables
  l <- list()
  for (i in seq(.jcall(v, "I", "size")) - 1) { # iterate through array list of Value objects
    vi <- v$get(as.integer(i)) # save current Value as vi
    row <- list() # create a list for the different slots
    row$id <- .jcall(vi, "I", "getVariableId")
    row$label <- .jcall(vi, "S", "getKey")
    row$type <- .jcall(vi, "S", "getDataType")
    l[[i + 1]] <- row # add the row to the list
  }
  d <- do.call(rbind.data.frame, l) # convert the list of lists to data frame
  attributes(d)$statementType <- statementType
  return(d)
}


# Attributes -------------------------------------------------------------------

#' Get the entities and attributes for a variable
#'
#' Retrieve the entities and their attributes for a variable in DNA
#'
#' This function retrieves the entities and their attributes for a given
#' variable from the DNA database as a \code{dna_attributes} object. Such an
#' object is an extension of a data frame and can be treated as such.
#'
#' There are three ways to use this function: by specifying only the variable
#' ID; by specifying the variable name and its statement type ID; and by
#' specifying the variable name and its statement type name.
#'
#' @param statementType The name of the statement type in which the variable is
#'   defined for which entities and values should be retrieved. Only required if
#'   \code{variableId} is not supplied. Either \code{statementType} or
#'   \code{statementTypeId} must be specified in this case.
#' @param variable The name of the variable for which the entities and
#'   attributes should be returned. In addition to this argument, either the
#'   statement type name or statement type ID must be supplied to identify the
#'   variable correctly. If the \code{variableId} a specified, the
#'   \code{variable} argument is unnecessary and the statement type need not be
#'   supplied.
#' @param statementTypeId The ID of the statement type in which the variable is
#'   defined for which entities and values should be retrieved. Only required if
#'   \code{variableId} is not supplied. Either \code{statementType} or
#'   \code{statementTypeId} must be specified in this case.
#' @param variableId The ID of the variable for which the entities and
#'   attributes should be returned. If this argument is supplied, the other
#'   three arguments are unnecessary.
#'
#' @examples
#' \dontrun{
#' dna_init()
#' dna_sample()
#' dna_openDatabase("sample.dna", coderId = 1, coderPassword = "sample")
#'
#' dna_getAttributes(variableId = 1)
#' dna_getAttributes(statementTypeId = 1, variable = "organization")
#' dna_getAttributes(statementType = "DNA Statement", variable = "concept")
#' }
#'
#' @author Philip Leifeld
#'
#' @family attributes
#'
#' @importFrom rJava .jcall
#' @importFrom rJava J
#' @export
dna_getAttributes <- function(statementType = NULL,
                              variable = NULL,
                              statementTypeId = NULL,
                              variableId = NULL) {

  # check if the arguments are valid
  statementTypeValid <- TRUE
  if (is.null(statementType) || !is.character(statementType) || length(statementType) != 1 || is.na(statementType) || statementType == "") {
    statementTypeValid <- FALSE
  }

  statementTypeIdValid <- TRUE
  if (is.null(statementTypeId) || !is.numeric(statementTypeId) || length(statementTypeId) != 1 || is.na(statementTypeId) || statementTypeId %% 1 != 0) {
    statementTypeIdValid <- FALSE
  }

  variableValid <- TRUE
  if (is.null(variable) || !is.character(variable) || length(variable) != 1 || is.na(variable) || variable == "") {
    variableValid <- FALSE
  }

  variableIdValid <- TRUE
  if (is.null(variableId) || !is.numeric(variableId) || length(variableId) != 1 || is.na(variableId) || variableId %% 1 != 0) {
    variableIdValid <- FALSE
  }

  errorString <- "Please supply 1) a variable ID or 2) a statement type name and a variable name or 3) a statement type ID and a variable name."
  if ((!variableValid && !variableIdValid) || (!statementTypeIdValid && !statementTypeValid && !variableIdValid)) {
    stop(errorString)
  }

  if (variableIdValid && variableValid) {
    variable <- NULL
    variableValid <- FALSE
    warning("Both a variable ID and a variable name were supplied. Ignoring the 'variable' argument.")
  }

  if (statementTypeIdValid && statementTypeValid && !variableIdValid && variableValid) {
    statementType <- NULL
    statementTypeValid <- FALSE
    warning("Both a statement type ID and a statement type name were supplied. Ignoring the 'statementType' argument.")
  }

  if (variableIdValid && (statementTypeIdValid || statementTypeValid)) {
    statementTypeId <- NULL
    statementTypeIdValid <- FALSE
    statementType <- NULL
    statementTypeValid <- FALSE
    warning("If a variable ID is provided, a statement type is not necessary. Ignoring the 'statementType' and 'statementTypeId' arguments.")
  }

  # get the data from the DNA database using rJava
  if (variableIdValid) {
    a <- .jcall(dnaEnvironment[["dna"]]$headlessDna,
                "Ldna/export/DataFrame;",
                "getAttributes",
                as.integer(variableId))
  } else if (variableValid && statementTypeIdValid) {
    a <- .jcall(dnaEnvironment[["dna"]]$headlessDna,
                "Ldna/export/DataFrame;",
                "getAttributes",
                as.integer(statementTypeId),
                variable)
  } else if (variableValid && statementTypeValid) {
    a <- .jcall(dnaEnvironment[["dna"]]$headlessDna,
                "Ldna/export/DataFrame;",
                "getAttributes",
                statementType,
                variable)
  } else {
    stop(errorString)
  }

  # extract the relevant information from the Java reference
  varNames <- .jcall(a, "[S", "getVariableNames")
  nr <- .jcall(a, "I", "nrow")
  nc <- .jcall(a, "I", "ncol")

  # create an empty data frame with the first (integer) column for IDs
  dat <- cbind(data.frame(ID = integer(nr)),
               matrix(character(nr), nrow = nr, ncol = nc - 1))
  # populate the data frame
  for (i in 0:(nr - 1)) {
    for (j in 0:(nc - 1)) {
      dat[i + 1, j + 1] <- J(a, "getValue", as.integer(i), as.integer(j))
    }
  }
  rownames(dat) <- NULL
  colnames(dat) <- varNames
  class(dat) <- c("dna_attributes", class(dat))
  return(dat)
}