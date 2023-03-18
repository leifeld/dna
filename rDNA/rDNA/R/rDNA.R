# Startup ----------------------------------------------------------------------

dnaEnvironment <- new.env(hash = TRUE, parent = emptyenv())

#' Display version number and date when the package is loaded.
#' @importFrom utils packageDescription
#' @noRd
.onAttach <- function(libname, pkgname) {
  desc <- packageDescription(pkgname, libname)
  packageStartupMessage(
    'Version:      ', desc$Version, '\n',
    'Date:         ', desc$Date, '\n',
    'Author:       Philip Leifeld (University of Essex)\n',
    'Contributors: Tim Henrichsen (University of Warwick),\n',
    '              Johannes B. Gruber (Vrije Universiteit Amsterdam)\n',
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

#' Identify and/or download and install the correct DNA jar file
#'
#' Identify and/or download and install the correct DNA jar file.
#'
#' rDNA requires the installation of a DNA jar file to run properly. While it is
#' possible to store the jar file in the respective working directory, it is
#' preferable to install it in the rDNA library installation directory under
#' \code{inst/java/}. The \code{dna_jar} function attempts to find the version
#' of the jar file that matches the installed \pkg{rDNA} version in the
#' \code{inst/java/} sub-directory of the package library path and return the
#' jar file name including its full path. If this fails, it will try to find the
#' jar file in the current working directory and return its file name. If this
#' fails as well, it will attempt to download the matching jar file from GitHub
#' and store it in the library path and return its file name. If this fails, it
#' will attempt to store the downloaded jar file in the working directory and
#' return its file name. If this fails as well, it will clone the current DNA
#' master code from GitHub to a local temporary directory, build the jar file
#' from source, and attempt to store the built jar file in the library path or,
#' if this fails, in the working directory and return the file name of the jar
#' file. If all of this fails, an error message is thrown.
#'
#' @return The file name of the jar file that matches the installed \pkg{rDNA}
#'   version, including full path.
#'
#' @author Philip Leifeld
#'
#' @importFrom utils download.file unzip packageVersion
#' @export
dna_jar <- function() {
  # detect package version
  v <- as.character(packageVersion("rDNA"))

  # try to locate jar file in library path and return jar file path
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
    jar <- paste0(getwd(), "/inst/java/dna-", v, ".jar")
    if (file.exists(jar)) {
      message("Jar file found in working directory.")
      return(jar)
    }
  }, error = function(e) {success <- FALSE})

  # try to download from GitHub release directory to library path
  tryCatch({
    rdna_dir <- dirname(system.file(".", package = "rDNA"))
    f <- paste0("https://github.com/leifeld/dna/releases/download/v", v, "/dna-", v, ".jar")
    dest <- paste0(rdna_dir, "/inst/java/dna-", v, ".jar")
    targetdir <- paste0(rdna_dir, "/", "inst/java/")
    dir.create(targetdir, recursive = TRUE, showWarnings = FALSE)
    suppressWarnings(download.file(url = f,
                                   destfile = dest,
                                   mode = "wb",
                                   cacheOK = FALSE,
                                   quiet = TRUE))
    if (file.exists(dest)) {
      message("Jar file downloaded from GitHub to library path.")
      return(dest)
    }
  }, error = function(e) {success <- FALSE})

  # try to download from GitHub release directory to working directory
  tryCatch({
    rdna_dir <- dirname(system.file(".", package = "rDNA"))
    f <- paste0("https://github.com/leifeld/dna/releases/download/v", v, "/dna-", v, ".jar")
    dest <- paste0(getwd(), "/dna-", v, ".jar")
    suppressWarnings(download.file(url = f,
                                   destfile = dest,
                                   mode = "wb",
                                   cacheOK = FALSE,
                                   quiet = TRUE))
    if (file.exists(dest)) {
      message("Jar file downloaded from GitHub to working directory.")
      return(dest)
    }
  }, error = function(e) {success <- FALSE})

  # try to download and build from source
  tryCatch({
    td <- tempdir()
    dest <- paste0(td, "/master.zip")
    suppressWarnings(download.file(url = "https://github.com/leifeld/dna/archive/master.zip",
                                   destfile = dest,
                                   mode = "wb",
                                   cacheOK = FALSE,
                                   quiet = TRUE))
    unzip(zipfile = dest, overwrite = TRUE, exdir = td)
    output <- file.remove(dest)
    gradle <- paste0(td, "/dna-master/gradlew")
    Sys.chmod(gradle, mode = "0777", use_umask = TRUE)
    oldwd <- getwd()
    setwd(paste0(td, "/dna-master/"))
    system(paste0(gradle, " build"), ignore.stdout = TRUE, ignore.stderr = TRUE)
    setwd(oldwd)
    builtjar <- paste0(td, "/dna-master/dna/build/libs/dna-", v, ".jar")
    if (file.exists(builtjar)) {
      message("DNA source code downloaded and jar file built successfully.")
    }
  }, error = function(e) {success <- FALSE})

  # try to copy built jar to library path
  tryCatch({
    targetdir <- paste0(find.package("rDNA"), "/", "inst/java/")
    dir.create(targetdir, recursive = TRUE, showWarnings = FALSE)
    dest <- paste0(targetdir, "dna-", v, ".jar")
    file.copy(from = builtjar, to = targetdir)
    if (file.exists(dest)) {
      unlink(paste0(td, "/dna-master"), recursive = TRUE)
      message("Jar file copied to library path.")
      return(dest)
    }
  }, error = function(e) {success <- FALSE})

  # try to copy built jar to working directory
  tryCatch({
    dest <- paste0(getwd(), "/dna-", v, ".jar")
    file.copy(from = builtjar, to = dest)
    if (file.exists(dest)) {
      unlink(paste0(td, "/dna-master"), recursive = TRUE)
      message("Jar file copied to working directory.")
      return(dest)
    }
  }, error = function(e) {success <- FALSE})

  stop("DNA jar file could not be identified or downloaded. Please download ",
       "the DNA jar file matching the version number of rDNA and store it in ",
       "the inst/java/ directory of your rDNA library installation path or in ",
       "your working directory. Your current rDNA version is ", v, ".")
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
#' @export
#' @importFrom rJava .jcall
dna_printDetails <- function() {
  .jcall(dnaEnvironment[["dna"]]$headlessDna, "V", "printDatabaseDetails")
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
#' @export
#' @importFrom rJava .jcall
dna_closeDatabase <- function() {
  .jcall(dnaEnvironment[["dna"]]$headlessDna, "V", "closeDatabase")
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
                "Lexport/DataFrame;",
                "getAttributes",
                as.integer(variableId))
  } else if (variableValid && statementTypeIdValid) {
    a <- .jcall(dnaEnvironment[["dna"]]$headlessDna,
                "Lexport/DataFrame;",
                "getAttributes",
                as.integer(statementTypeId),
                variable)
  } else if (variableValid && statementTypeValid) {
    a <- .jcall(dnaEnvironment[["dna"]]$headlessDna,
                "Lexport/DataFrame;",
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
#' @param networkType The kind of network to be computed. Can be
#'   \code{"twomode"}, \code{"onemode"}, or \code{"eventlist"}.
#' @param statementType The name of the statement type in which the variable
#'   of interest is nested. For example, \code{"DNA Statement"}.
#' @param variable1 The first variable for network construction. In a one-mode
#'   network, this is the variable for both the rows and columns. In a
#'   two-mode network, this is the variable for the rows only. In an event
#'   list, this variable is only used to check for duplicates (depending on
#'   the setting of the \code{duplicates} argument).
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
#'   setting of the \code{duplicates} argument).
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
#'   on the scale of the integer variable. With a short text variable as a
#'   qualifier, agreement on common categorical values of the qualifier is
#'   required, for example a tie is established (or a tie weight increased) if
#'   two actors both refer to the same value on the second variable AND match on
#'   the categorical qualifier, for example the type of referral.
#'
#'   In a two-mode network, the qualifier variable can be used to retain only
#'   positive or only negative statements or subtract negative from positive
#'   mentions. All of this depends on the setting of the
#'   \code{qualifierAggregation} argument. For event lists, the qualifier
#'   variable is only used for filtering out duplicates (depending on the
#'   setting of the \code{duplicates} argument.
#'
#'   The qualifier can also be \code{NULL}, in which case it is ignored, meaning
#'   that values in \code{variable1} and \code{variable2} are unconditionally
#'   associated with each other in the network when they co-occur. This is
#'   identical to selecting a qualifier variable and setting
#'   \code{qualifierAggregation = "ignore"}.
#' @param qualifierDocument A boolean value indicating whether the qualifier
#'   variable is at the document level (i.e., \code{"author"},
#'   \code{"source"}, \code{"section"}, \code{"type"}, \code{"id"}, or
#'   \code{"title"}
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
#'   \code{"average"} (for average activity normalization), \code{"jaccard"}
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
#'   \code{"dd.mm.yyyy"}. All statements before this date will be excluded.
#' @param start.time The start time for network construction on the specified
#'   \code{start.date}. All statements before this time on the specified date
#'   will be excluded.
#' @param stop.date The stop date for network construction in the format
#'   \code{"dd.mm.yyyy"}. All statements after this date will be excluded.
#' @param stop.time The stop time for network construction on the specified
#'   \code{stop.date}. All statements after this time on the specified date
#'   will be excluded.
#' @param timeWindow Possible values are \code{"no"}, \code{"events"},
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
#'   \code{windowSize} argument. For example, this could be used to create a
#'   time window of 6 months which moves forward by one month each time, thus
#'   creating time windows that overlap by five months. If \code{"events"} is
#'   used instead of a natural time unit, the time window will comprise
#'   exactly as many statements as defined in the \code{windowSize} argument.
#'   However, if the start or end statement falls on a date and time where
#'   multiple events happen, those additional events that occur simultaneously
#'   are included because there is no other way to decide which of the
#'   statements should be selected. Therefore the window size is sometimes
#'   extended when the start or end point of a time window is ambiguous in
#'   event time.
#' @param windowSize The number of time units of which a moving time window is
#'   comprised. This can be the number of statement events, the number of days
#'   etc., as defined in the \code{"timeWindow"} argument.
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
#'   files).
#' @param outfile An optional output file name for saving the resulting
#'   network(s) to a file instead of returning an object.
#'
#' @examples
#' \dontrun{
#' dna_init()
#' dna_sample()
#' dna_openDatabase("sample.dna", coderId = 1, coderPassword = "sample")
#' nw <- dna_network(networkType = "onemode",
#'   variable1 = "organization",
#'   variable2 = "concept",
#'   qualifier = "agreement",
#'   qualifierAggregation = "congruence",
#'   normalization = "average",
#'   excludeValues = list("concept" =
#'     c("There should be legislation to regulate emissions.")))
#' }
#'
#' @author Philip Leifeld
#'
#' @importFrom rJava .jarray
#' @importFrom rJava .jcall
#' @importFrom rJava .jnull
#' @importFrom rJava J
#' @export
dna_network <- function(networkType = "twomode",
                        statementType = "DNA Statement",
                        variable1 = "organization",
                        variable1Document = FALSE,
                        variable2 = "concept",
                        variable2Document = FALSE,
                        qualifier = "agreement",
                        qualifierDocument = FALSE,
                        qualifierAggregation = "ignore",
                        normalization = "no",
                        isolates = FALSE,
                        duplicates = "include",
                        start.date = "01.01.1900",
                        stop.date = "31.12.2099",
                        start.time = "00:00:00",
                        stop.time = "23:59:59",
                        timeWindow = "no",
                        windowSize = 100,
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
                        outfile = NULL) {

  # wrap the vectors of exclude values for document variables into Java arrays
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
  var <- .jarray(var) # array of variable names of each excluded value
  val <- .jarray(val) # array of values to be excluded

  # encode R NULL as Java null value if necessary
  if (is.null(qualifier) || is.na(qualifier)) {
    qualifier <- .jnull(class = "java/lang/String")
  }
  if (is.null(fileFormat)) {
    fileFormat <- .jnull(class = "java/lang/String")
  }
  if (is.null(outfile)) {
    outfile <- .jnull(class = "java/lang/String")
  }

  # call rNetwork function to compute results
  .jcall(dnaEnvironment[["dna"]]$headlessDna,
         "V",
         "rNetwork",
         networkType,
         statementType,
         variable1,
         variable1Document,
         variable2,
         variable2Document,
         qualifier,
         qualifierDocument,
         qualifierAggregation,
         normalization,
         isolates,
         duplicates,
         start.date,
         stop.date,
         start.time,
         stop.time,
         timeWindow,
         as.integer(windowSize),
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
         fileFormat
  )

  exporter <- .jcall(dnaEnvironment[["dna"]]$headlessDna, "Lexport/Exporter;", "getExporter") # get a reference to the Exporter object, in which results are stored

  if (networkType == "eventlist") { # assemble an event list in the form of a data frame of filtered statements
    f <- J(exporter, "getFilteredStatements", simplify = TRUE) # array list of filtered export statements; use J because array list return type not recognized using .jcall
    l <- list() # create a list for filtered statements, later to be converted to data frame, with one row per statement
    for (i in seq(.jcall(f, "I", "size")) - 1) { # loop through filtered statements, starting at 0
      fi <- f$get(as.integer(i)) # retrieve filtered statement i
      row <- list() # each filtered export statement is represented by a list, with multiple slots for the variables etc.
      row$statement_id <- .jcall(fi, "I", "getId") # store the statement ID
      row$time <- .jcall(fi, "J", "getDateTimeLong") # store the date/time in seconds since 1 January 1970; will be converted to POSIXct later because the conversion to data frame otherwise converts it back to long anyway
      values <- J(fi, "getValues") # array list of variables with values; use J instead of .jcall because array list return type not recognized using .jcall
      for (j in seq(.jcall(values, "I", "size")) - 1) { # loop through the variables
        vi <- values$get(as.integer(j)) # save variable/value j temporarily to access its contents
        dataType <- .jcall(vi, "S", "getDataType") # the data type of value j
        if (dataType == "long text") {
          row[[.jcall(vi, "S", "getKey")]] <- .jcall(vi, "S", "getValue") # store as character object under variable name if long text
        } else if (dataType == "short text") {
          row[[.jcall(vi, "S", "getKey")]] <- vi$getValue()$getValue() # extract character object from Entity object and store under variable name if short text
        } else {
          row[[.jcall(vi, "S", "getKey")]] <- vi$getValue() # store as integer under variable name if boolean or integer data type
        }
      }
      row$start_position <- .jcall(fi, "I", "getStart") # store start caret in document text
      row$stop_position <- .jcall(fi, "I", "getStop") # store end caret in document text
      row$text <- .jcall(fi, "S", "getText") # text of the statement between start and end caret
      row$coder <- .jcall(fi, "I", "getCoderId") # store coder ID; the user can merge this with other coder details like name and color later if needed
      row$document_id <- .jcall(fi, "I", "getDocumentId") # store the document ID of the document the statement is contained in
      row$document_title <- .jcall(fi, "S", "getTitle") # store the document title
      row$document_author <- .jcall(fi, "S", "getAuthor") # store the document author
      row$document_source <- .jcall(fi, "S", "getSource") # store the document source
      row$document_section <- .jcall(fi, "S", "getSection") # store the document section
      row$document_type <- .jcall(fi, "S", "getType") # store the document type
      l[[i + 1]] <- row # add the row to the list
    }
    d <- do.call(rbind.data.frame, l) # convert the list of lists to data frame
    d$time <- as.POSIXct(d$time, origin = "1970-01-01 00:00:00") # convert long date/time to POSIXct
    return(d)
  } else { # assemble a one-mode or two-mode matrix with attributes or a list of matrices (if time window)
    m <- .jcall(exporter, "[Lexport/Matrix;", "getMatrixResultsArray") # get list of Matrix objects from Exporter object
    l <- list() # create a list in which each result is stored; can be of length 1 if no time window is used
    for (t in 1:length(m)) { # loop through the matrices
      mat <- .jcall(m[[t]], "[[D", "getMatrix", simplify = TRUE) # get the resulting matrix at step t as a double[][] object and save as matrix
      rownames(mat) <- .jcall(m[[t]], "[S", "getRowNames", simplify = TRUE) # add the row names to the matrix
      colnames(mat) <- .jcall(m[[t]], "[S", "getColumnNames", simplify = TRUE) # add the column names to the matrix
      attributes(mat)$start <- as.POSIXct(.jcall(m[[t]], "J", "getStartLong"), origin = "1970-01-01") # add the start date/time of the result as an attribute to the matrix
      attributes(mat)$stop <- as.POSIXct(.jcall(m[[t]], "J", "getStopLong"), origin = "1970-01-01") # add the end date/time of the result as an attribute to the matrix
      if (length(m) > 1) {
        attributes(mat)$middle <- as.POSIXct(.jcall(m[[t]], "J", "getDateTimeLong"), origin = "1970-01-01") # add the mid-point date/time around which the time window is centered if the time window algorithm was used
      }
      attributes(mat)$numStatements <- .jcall(m[[t]], "I", "getNumStatements") # add the number of filtered statements the matrix is based on as an attribute to the matrix
      attributes(mat)$call <- match.call() # add the arguments of the call as an attribute to the matrix
      class(mat) <- c(paste0("dna_network_", networkType), class(mat)) # add "dna_network_onemode" or "dna_network_twomode" as a class label in addition to "matrix"
      l[[t]] <- mat # add the matrix to the list
    }
    if (length(m) == 1) {
      return(l[[1]]) # return the first matrix in the list if no time window was used
    } else {
      attributes(l)$call <- match.call() # add arguments of the call as an attribute also to the list, not just each network matrix
      class(l) <- c(paste0("dna_network_", networkType, "_timewindows"), class(l)) # add "dna_network_onemode_timewindows" or "dna_network_twomode_timewindows" to class label
      return(l) # return the list of network matrices
    }
  }
}

#' Print a \code{dna_network_onemode} object
#'
#' Show details of a \code{dna_network_onemode} object.
#'
#' Print a one-mode network matrix and its attributes.
#'
#' @param x A \code{dna_network_onemode} object, as returned by the
#'   \code{\link{dna_network}} function.
#' @param trim Number of maximum characters to display in row and column labels
#'   of the matrix. Labels with more characters are truncated, and the last
#'   character is replaced by an asterisk (\code{*}).
#' @param attr Display attributes, such as the start and stop date and time, the
#'   number of statements on which the matrix is based, the function call and
#'   arguments on which the network matrix is based, and the full labels without
#'   truncation.
#' @param ... Additional arguments. Currently not in use.
#'
#' @author Philip Leifeld
#'
#' @seealso \link{as.matrix.dna_network_onemode}, \link{dna_network},
#'   \link{print.dna_network_onemode}
#' @export
print.dna_network_onemode <- function(x, trim = 5, attr = TRUE, ...) {
  rn <- rownames(x)
  cn <- colnames(x)
  rownames(x) <- sapply(rownames(x), function(r) if (nchar(r) > trim) paste0(substr(r, 1, trim - 1), "*") else r)
  colnames(x) <- sapply(colnames(x), function(r) if (nchar(r) > trim) paste0(substr(r, 1, trim - 1), "*") else r)
  x <- round(x, 2)
  if ("dna_network_onemode" %in% class(x)) {
    onemode <- TRUE
    class(x) <- class(x)[class(x) != "dna_network_onemode"]
  } else {
    onemode <- FALSE
    class(x) <- class(x)[class(x) != "dna_network_twomode"]
  }
  start <- attr(x, "start")
  attr(x, "start") <- NULL
  stop <- attr(x, "stop")
  attr(x, "stop") <- NULL
  ns <- attr(x, "numStatements")
  attr(x, "numStatements") <- NULL
  cl <- deparse(attr(x, "call"))
  attr(x, "call") <- NULL
  attr(x, "class") <- NULL
  print(x)
  if (attr) {
    cat("\nStart:", as.character(start))
    cat("\nStop: ", as.character(stop))
    cat("\nStatements:", ns)
    cat("\nCall:", trimws(cl))
    if (onemode) {
      cat("\n\nLabels:\n")
      cat(paste(1:length(rn), rn), sep = "\n")
    } else {
      cat("\n\nRow labels:\n")
      cat(paste(1:length(rn), rn), sep = "\n")
      cat("\nColumn labels:\n")
      cat(paste(1:length(cn), cn), sep = "\n")
    }
  }
}

#' Print a \code{dna_network_twomode} object
#'
#' Show details of a \code{dna_network_twomode} object.
#'
#' Print a two-mode network matrix and its attributes.
#'
#' @inheritParams print.dna_network_onemode
#'
#' @author Philip Leifeld
#'
#' @seealso \link{as.matrix.dna_network_twomode}, \link{dna_network},
#'   \link{print.dna_network_onemode}
#' @export
print.dna_network_twomode <- print.dna_network_onemode

#' Convert a \code{dna_network_onemode} object to a matrix
#'
#' Convert a \code{dna_network_onemode} object to a matrix.
#'
#' Remove the attributes and \code{"dna_network_onemode"} class label from a
#' \code{dna_network_onemode} object and return it as a numeric matrix.
#'
#' @param x The \code{dna_network_onemode} object, as returned by the
#'   \code{\link{dna_network}} function.
#' @param ... Additional arguments. Currently not in use.
#'
#' @author Philip Leifeld
#'
#' @seealso \link{print.dna_network_onemode}, \link{dna_network},
#'   \link{as.matrix.dna_network_twomode}
#' @export
as.matrix.dna_network_onemode <- function(x, ...) {
  attr(x, "start") <- NULL
  attr(x, "stop") <- NULL
  attr(x, "numStatements") <- NULL
  attr(x, "call") <- NULL
  attr(x, "class") <- NULL
  return(x)
}

#' Convert a \code{dna_network_twomode} object to a matrix
#'
#' Convert a \code{dna_network_twomode} object to a matrix.
#'
#' Remove the attributes and \code{"dna_network_twomode"} class label from a
#' \code{dna_network_twomode} object and return it as a numeric matrix.
#'
#' @param x The \code{dna_network_twomode} object, as returned by the
#'   \code{\link{dna_network}} function.
#' @param ... Additional arguments. Currently not in use.
#'
#' @author Philip Leifeld
#'
#' @seealso \link{print.dna_network_twomode}, \link{dna_network},
#'   \link{as.matrix.dna_network_onemode}
#' @export
as.matrix.dna_network_twomode <- as.matrix.dna_network_onemode


#' Plot \code{dna_network_onemode} object.
#'
#' Plot a one-mode network generated from \code{\link{dna_network}}.
#'
#' This function plots \code{dna_network_onemode} objects generated by the
#' \code{\link{dna_network}} function.
#'
#' In order to use this function, please install the \code{igraph} and
#' \code{ggraph} packages.
#'
#' Available layouts include: \code{"stress"} (the default), \code{"backbone"},
#' \code{"circle"}, \code{"dh"}, \code{"drl"}, \code{"fr"}, \code{"gem"},
#' \code{"graphopt"}, \code{"kk"}, \code{"lgl"}, \code{"mds"}, \code{"nicely"},
#' \code{"randomly"}, and \code{"star"}. Other layouts might be available (see
#' \link[ggraph]{layout_tbl_graph_auto} for more details).
#'
#' @param object A \code{dna_network} object.
#' @param atts A \code{dna_attributes} object generated by
#'   \code{\link{dna_getAttributes}}. Provide this object and matching
#'   attributes when plotting custom node colors, node labels and/or node sizes.
#' @param layout The type of layout to use. See Details for available layouts.
#' @param edge_size_range Two values indicating the minimum and maximum value
#'   to scale edge widths.
#' @param edge_color Provide the name of a color for edge colors. The default
#'   \code{"NULL"} colors edges in line with the specified
#'   \code{qualifierAggregation} in \link{\code{dna_network}}.
#' @param edge_alpha Takes numeric values to control the alpha-transparency of
#'   edges. Possible values range from \code{0} (fully transparent) to \code{1}
#'   (fully visible).
#' @param node_size Takes positive numeric values to control the size of nodes
#'   (defaults to \code{7}). Also accepts numeric values matching an attribute
#'   of the \code{atts} object (see examples).
#' @param node_colors Provide the name of a color or use an attribute from the
#'   \code{atts} object for node colors (see examples). Defaults to
#'   \code{"black"}.
#' @param node_label If \code{TRUE}, the row names of the network matrix are
#'   used for node labels. Also accepts character objects matching one of the
#'   attribute variables of the \code{atts} object (see examples). \code{FALSE}
#'   turns off node labels.
#' @param font_size Controls the font size of the node labels.
#' @param truncate Sets the number of characters to which node labels should be
#'   truncated.
#' @param threshold Minimum threshold for which edges should be plotted.
#' @param giant_component Only plot the giant component (the biggest connected
#'   cluster) of the network. Defaults to \code{FALSE}.
#' @param exclude_isolates Exclude isolates (nodes with no connection to other
#'   nodes) from the plot. Defaults to \code{FALSE}.
#' @param max_overlaps Value to exclude node labels that overlap with too many
#'   other node labels (see \link[ggrepel]{\code{geom_label_repel}}. Defaults
#'   to 10.
#' @param seed Numeric value passed to \link{set.seed}. Ensures that plots are
#'   reproducible.
#'
#'
#' @examples
#' \dontrun{
#' dna_init()
#' dna_sample()
#'
#' dna_openDatabase("sample.dna", coderId = 1, coderPassword = "sample")
#'
#' # compute network matrix
#' nw <- dna_network(networkType = "onemode",
#'                   qualifierAggregation = "subtract",
#'                   normalization = "average")
#'
#' # plot network
#' library("ggplot2")
#' autoplot(nw)
#'
#' ## use entity colours (here: colors of organizations as an illustration)
#' nw <- dna_network(networkType = "onemode",
#'                   qualifierAggregation = "subtract",
#'                   normalization = "average")
#'
#' # compute dna_attributes object
#' atts <- dna_getAttributes(variableId = 2)
#'
#' # include color attribute and color organizations
#' autoplot(nw, atts, node_colors = "color", layout = "fr")
#'
#' # edit the colors before plotting
#' atts$color[atts$Type == "NGO"] <- "red"         # change NGO color to red
#' atts$color[atts$Type == "Government"] <- "blue" # change government color to blue
#'
#' # plot the network with custom colors
#' autoplot(nw, atts, node_colors = "color")
#'
#'
#' ## use an attribute, such as type, to plot node labels
#' nw <- dna_network(networkType = "onemode",
#'                   qualifierAggregation = "subtract",
#'                   normalization = "average")
#'
#' # compute dna_attributes object
#' atts <- dna_getAttributes(variableId = 2)
#'
#' autoplot(nw, atts, node_label = "Type")
#'
#'
#' ## Plot node sizes according to the number of statements of entities
#'
#' # compute network matrix for plotting
#' nw <- dna_network(networkType = "onemode",
#'                   qualifierAggregation = "subtract",
#'                   normalization = "average")
#'
#' # compute dna_attributes object
#' atts <- dna_getAttributes(variableId = 2)
#'
#' # compute additional matrix to calculate the number of statements
#' nw_freq <- dna_network(networkType = "onemode",
#'                        qualifierAggregation = "ignore",
#'                        normalization = "no")
#'
#'
#' # add frequency of statements as an attribute
#' atts$freq <- rowSums(nw_freq)[match(atts$value, rownames(nw_freq))]
#'
#' # plot network with node sizes matching statement frequencies
#' autoplot(nw, atts, node_size = "freq", node_colors = "color")
#'
#'
#' ## use igraph community detection for the identification of network clusters
#' nw <- dna_network(networkType = "onemode",
#'                   qualifierAggregation = "subtract",
#'                   normalization = "average")
#'
#' # compute dna_attributes object
#' atts <- dna_getAttributes(variableId = 2)
#'
#' # remove negative edge weights
#' nw[nw < 0] <- 0
#'
#' # convert dna_network to igraph object
#' graph <- igraph::graph_from_adjacency_matrix(nw,
#'                                              mode = "undirected",
#'                                              weighted = TRUE,
#'                                              diag = FALSE,
#'                                              add.colnames = NULL,
#'                                              add.rownames = NA)
#'
#'
#' # compute communities using igraph cluster algorithms
#' # (here: fast and greedy as an illustration))
#' com <- igraph::cluster_fast_greedy(graph)
#'
#' # add node community membership as an attribute
#' atts$membership <- com$membership[match(atts$value, com$names)]
#'
#' # use community membership as node color
#' autoplot(nw, atts, node_colors = "membership")
#'
#' ## or plot ellipses using ggforce
#' library("ggforce")
#' autoplot(nw, atts, node_colors = "color") +
#'   geom_mark_ellipse(aes(x = x, y = y, group = com$membership, fill = com$membership),
#'                     show.legend = FALSE)
#'
#'
#' ## add legend to the network plot (here colors mapped to type attribute)
#' autoplot(nw, atts, node_colors = "color") +
#' # Add legend
#' scale_color_identity(name = "",
#'                      labels = c("Government", "NGO", "Business"),
#'                      guide = "legend") +
#' theme(legend.position = "bottom", # Change legend position
#'       legend.text = element_text(size = 10)) # change legend text font size
#'
#' }
#'
#' @author Tim Henrichsen
#'
#' @seealso \link{dna_network}
#'
#' @importFrom igraph graph_from_adjacency_matrix
#' @importFrom igraph graph_from_incidence_matrix
#' @importFrom igraph V
#' @importFrom igraph delete.edges
#' @importFrom igraph E
#' @importFrom igraph vcount
#' @importFrom igraph ecount
#' @importFrom igraph delete.vertices
#' @importFrom igraph degree
#' @importFrom igraph clusters
#' @importFrom igraph induced_subgraph
#' @importFrom ggraph ggraph
#' @importFrom ggraph geom_edge_link
#' @importFrom ggraph scale_edge_width
#' @importFrom ggraph geom_node_point
#' @importFrom ggraph geom_node_text
#' @importFrom ggraph theme_graph
#' @importFrom ggraph scale_edge_color_identity
#' @importFrom ggplot2 autoplot
#' @importFrom ggplot2 aes
#' @importFrom ggplot2 aes_string
#' @importFrom ggplot2 scale_color_identity
#' @export


autoplot.dna_network_onemode <- function(object,
                                         atts = NULL,
                                         layout = "auto",
                                         edge_size_range = c(0.2, 2),
                                         edge_color = NULL,
                                         edge_alpha = 1,
                                         node_size = 7,
                                         node_colors = "black",
                                         node_label = TRUE,
                                         font_size = 6,
                                         truncate = 50,
                                         threshold = NULL,
                                         giant_component = FALSE,
                                         exclude_isolates = FALSE,
                                         max_overlaps = 10,
                                         seed = 12345) {
  
  out <- plot_dna_network(object = object,
                          atts = atts,
                          layout = layout,
                          edge_size_range = edge_size_range,
                          edge_color = edge_color,
                          edge_alpha = edge_alpha,
                          node_size = node_size,
                          node_colors = node_colors,
                          node_label = node_label,
                          font_size = font_size,
                          truncate = truncate,
                          threshold = threshold,
                          giant_component = giant_component,
                          exclude_isolates = exclude_isolates,
                          max_overlaps = max_overlaps,
                          seed = seed)
  
  return(out)
}

#' Plot \code{dna_network_twomode} object.
#'
#' Plot a two-mode network generated from \code{\link{dna_network}}.
#'
#' This function plots \code{dna_network} objects generated by the
#' \code{\link{dna_network}} function.
#'
#' In order to use this function, please install the \code{igraph} and
#' \code{ggraph} packages.
#'
#' Available layouts include: \code{"stress"} (the default), \code{"bipartite"}
#' \code{"backbone"}, \code{"circle"}, \code{"dh"}, \code{"drl"}, \code{"fr"},
#' \code{"gem"}, \code{"graphopt"}, \code{"kk"}, \code{"lgl"}, \code{"mds"},
#' \code{"nicely"}, \code{"randomly"}, and \code{"star"}. Other layouts might
#' be available (see \link[ggraph]{layout_tbl_graph_auto} for more details).
#'
#' @param object A \code{dna_network} object.
#' @param atts A \code{dna_attributes} object generated by
#'   \code{\link{dna_getAttributes}}. Provide this object and matching
#'   attributes when plotting custom node colors, node labels and/or node sizes.
#' @param layout The type of layout to use. See Details for available layouts.
#' @param edge_size_range Two values indicating the minimum and maximum value
#'   to scale edge widths.
#' @param edge_color Provide the name of a color for edge colors. The default
#'   \code{"NULL"} colors edges in line with the specified
#'   \code{qualifierAggregation} in \link{\code{dna_network}}.
#' @param edge_alpha Takes numeric values to control the alpha-transparency of
#'   edges. Possible values range from \code{0} (fully transparent) to \code{1}
#'   (fully visible).
#' @param node_size Takes positive numeric values to control the size of nodes
#'   (defaults to \code{7}). Also accepts numeric values matching an attribute
#'   of the \code{atts} object (see examples).
#' @param node_colors Provide the name of a color or use an attribute from the
#'   \code{atts} object for node colors (see examples). Defaults to
#'   \code{"black"}.
#' @param node_label If \code{TRUE}, the row- and column names of the network
#'   matrix are used for node labels. Also accepts character objects matching
#'   one of the attribute variables of the \code{atts} object (see examples).
#'   \code{FALSE} turns off node labels.
#' @param font_size Controls the font size of the node labels.
#' @param truncate Sets the number of characters to which node labels should be
#'   truncated.
#' @param threshold Minimum threshold for which edges should be plotted.
#' @param giant_component Only plot the giant component (the biggest connected
#'   cluster) of the network. Defaults to \code{FALSE}.
#' @param exclude_isolates Exclude isolates (nodes with no connection to other
#'   nodes) from the plot. Defaults to \code{FALSE}.
#' @param max_overlaps Value to exclude node labels that overlap with too many
#'   other node labels (see \link[ggrepel]{\code{geom_label_repel}}. Defaults
#'   to 10.
#' @param seed Numeric value passed to \link{set.seed}. Ensures that plots are
#'   reproducible.
#'
#'
#' @examples
#' \dontrun{
#' dna_init()
#' dna_sample()
#'
#' dna_openDatabase("sample.dna", coderId = 1, coderPassword = "sample")
#'
#' # compute network matrix
#' nw <- dna_network(networkType = "twomode",
#'                   qualifierAggregation = "combine")
#'
#' # plot network
#' library("ggplot2")
#' autoplot(nw)
#'
#' ## use entity colours (here: colors of organizations as an illustration)
#' # compute dna_attributes object for first-mode entities
#' # (in this example "organization")
#' atts <- dna_getAttributes(variableId = 2)
#'
#' # compute dna_attributes object for second-mode entities
#' # (in this example "concept")
#' atts2 <- dna_getAttributes(variableId = 3)
#'
#' # combine both attribute objects
#' atts <- rbind(atts, atts2)
#'
#' # plot the network using the attributes of both variables
#' autoplot(nw, atts, node_colors = "color", layout = "bipartite",
#'          max_overlaps = 20)
#'
#' # edit the colors before plotting
#' atts$color[atts$Type == "NGO"] <- "red"         # change NGO color to red
#' atts$color[atts$Type == "Government"] <- "blue" # change government color to blue
#'
#' # plot the network with custom colors
#' autoplot(nw, atts, node_colors = "color")
#'
#' ## Use an attribute, such as type, to plot node labels
#' nw <- dna_network(networkType = "twomode",
#'                   qualifierAggregation = "subtract",
#'                   normalization = "activity")
#'
#' # compute dna_attributes objects and combine them
#' atts <- dna_getAttributes(variableId = 2)
#' atts2 <- dna_getAttributes(variableId = 3)
#' atts <- rbind(atts, atts2)
#'
#' autoplot(nw, atts, node_label = "Type")
#'
#'
#' ## Plot node sizes according the number of statements of entities
#'
#' # compute network matrix for plotting
#' nw <- dna_network(networkType = "twomode",
#'                   qualifierAggregation = "subtract",
#'                   normalization = "activity")
#'
#' # compute dna_attributes objects
#' atts <- dna_getAttributes(variableId = 2)
#' atts2 <- dna_getAttributes(variableId = 3)
#'
#' # compute additional matrix to calculate the number of statements
#' nw_freq <- dna_network(networkType = "twomode",
#'                        qualifierAggregation = "ignore",
#'                        normalization = "no")
#'
#'
#' # add frequency of statements as attribute
#'
#' # Compute statement frequencies of first-mode entities
#' atts$freq <- rowSums(nw_freq)[match(atts$value, rownames(nw_freq))]
#'
#' # Compute statement frequencies of second-mode entities
#' atts2$freq <- colSums(nw_freq)[match(atts2$value, colnames(nw_freq))]
#'
#' # Combine both attribute objects
#' atts <- rbind(atts, atts2)
#'
#' # plot network with node sizes matching statement frequencies
#' autoplot(nw, atts, node_size = "freq", node_colors = "color")
#'
#'
#' ## use igraph community detection for the identification of network clusters
#' nw <- dna_network(networkType = "twomode",
#'                   qualifierAggregation = "subtract",
#'                   normalization = "activity")
#'
#' # compute dna_attributes objects and combine them
#' atts <- dna_getAttributes(variableId = 2)
#' atts2 <- dna_getAttributes(variableId = 3)
#' atts <- rbind(atts, atts2)
#'
#' # remove negative edge weights
#' nw[nw < 0] <- 0
#'
#' # convert dna_network to igraph object
#' graph <- igraph::graph_from_incidence_matrix(nw,
#'                                              directed = FALSE,
#'                                              weighted = TRUE,
#'                                              add.names = NULL)
#'
#'
#' # compute communities using igraph cluster algorithms
#' # (here: fast and greedy as an illustration))
#' com <- igraph::cluster_fast_greedy(graph)
#'
#' # add node community membership as an attribute
#' atts$membership <- com$membership[match(atts$value, com$names)]
#'
#' # use community membership as node color
#' autoplot(nw, atts, node_colors = "membership")
#'
#' ## or plot ellipses using ggforce
#' library("ggforce")
#' autoplot(nw, atts, node_colors = "color") +
#'   geom_mark_ellipse(aes(x = x, y = y, group = com$membership, fill = com$membership),
#'                     show.legend = FALSE)
#'
#' }
#'
#' @author Tim Henrichsen
#'
#' @seealso \link{dna_network}
#'
#' @importFrom igraph graph_from_adjacency_matrix
#' @importFrom igraph graph_from_incidence_matrix
#' @importFrom igraph V
#' @importFrom igraph delete.edges
#' @importFrom igraph E
#' @importFrom igraph vcount
#' @importFrom igraph ecount
#' @importFrom igraph delete.vertices
#' @importFrom igraph degree
#' @importFrom igraph clusters
#' @importFrom igraph induced_subgraph
#' @importFrom ggraph ggraph
#' @importFrom ggraph geom_edge_link
#' @importFrom ggraph scale_edge_width
#' @importFrom ggraph geom_node_point
#' @importFrom ggraph geom_node_text
#' @importFrom ggraph theme_graph
#' @importFrom ggraph scale_edge_color_identity
#' @importFrom ggplot2 autoplot
#' @importFrom ggplot2 aes
#' @importFrom ggplot2 aes_string
#' @importFrom ggplot2 scale_color_identity
#' @export


autoplot.dna_network_twomode <- function(object,
                                         atts = NULL,
                                         layout = "auto",
                                         edge_size_range = c(0.2, 2),
                                         edge_color = NULL,
                                         edge_alpha = 1,
                                         node_size = 7,
                                         node_colors = "black",
                                         node_label = TRUE,
                                         font_size = 6,
                                         truncate = 50,
                                         threshold = NULL,
                                         giant_component = FALSE,
                                         exclude_isolates = FALSE,
                                         max_overlaps = 10,
                                         seed = 12345) {
  
  out <- plot_dna_network(object = object,
                          atts = atts,
                          layout = layout,
                          edge_size_range = edge_size_range,
                          edge_color = edge_color,
                          edge_alpha = edge_alpha,
                          node_size = node_size,
                          node_colors = node_colors,
                          node_label = node_label,
                          font_size = font_size,
                          truncate = truncate,
                          threshold = threshold,
                          giant_component = giant_component,
                          exclude_isolates = exclude_isolates,
                          max_overlaps = max_overlaps,
                          seed = seed)
  
  return(out)
}

#' @noRd
plot_dna_network <- function(object,
                             atts,
                             layout,
                             edge_size_range,
                             edge_color,
                             edge_alpha,
                             node_size,
                             node_colors,
                             node_label,
                             font_size,
                             truncate,
                             threshold,
                             giant_component,
                             exclude_isolates,
                             max_overlaps,
                             seed) {
  set.seed(seed)
  
  if (!grepl("dna_network", class(object)[1])) {
    stop("Invalid data object. Please compute a dna_network object via the ",
         "dna_network() function before plotting.")
  }
  
  if (!requireNamespace("igraph", quietly = TRUE)) {
    stop("The autoplot function requires the 'igraph' package to be installed.\n",
         "To do this, enter 'install.packages(\"igraph\")'.")
  }
  
  if (!requireNamespace("ggraph", quietly = TRUE)) {
    stop("The autoplot function requires the 'ggraph' package to be installed.\n",
         "To do this, enter 'install.packages(\"ggraph\")'.")
  }
  
  if (!is.null(atts) & !"dna_attributes" %in% class(atts)) {
    stop("Object provided in 'atts' is not a dna_attributes object. Please ",
         "provide a dna_attributes object via dna_getAttributes() or set atts ",
         "to 'NULL' if you do not want to use DNA attributes.")
  }
  
  if (!is.numeric(truncate)) {
    truncate <- Inf
    warning("No numeric value provided for trimming of entities. Truncation ",
            "will be ignored.")
  }
  
  # Convert network matrix to igraph network
  if ("dna_network_onemode" %in% class(object)) {
    graph <- igraph::graph_from_adjacency_matrix(object,
                                                 mode = "undirected",
                                                 weighted = TRUE,
                                                 diag = FALSE,
                                                 add.colnames = NULL,
                                                 add.rownames = NA)
    igraph::V(graph)$shape <- "circle"
  } else if ("dna_network_twomode" %in% class(object)) {
    graph <- igraph::graph_from_incidence_matrix(object,
                                                 directed = FALSE,
                                                 weighted = TRUE,
                                                 add.names = NULL)
    igraph::V(graph)$shape <- ifelse(igraph::V(graph)$type, "square", "circle")
  }
  
  # Check if all entities are included in attributes object (if provided)
  if (!is.null(atts) & !(all(igraph::V(graph)$name %in% atts$value))) {
    miss <- which(!igraph::V(graph)$name %in% atts$value)
    stop("Some network entities are missing in the attributes object:\n",
         paste(igraph::V(graph)$name[miss], collapse = "\n"))
  }
  
  # Remove tie weights below threshold
  if (!is.null(threshold)) {
    graph <- igraph::delete.edges(graph, which(!igraph::E(graph)$weight >= threshold))
  }
  
  # Add node colors
  if (is.character(node_colors)) {
    if (!is.null(atts) & length(node_colors) == 1 && node_colors %in% colnames(atts)) {
      col_pos <- which(colnames(atts) == node_colors)
      igraph::V(graph)$color <- atts[match(igraph::V(graph)$name, atts$value), col_pos]
    } else if (length(node_colors) > 1 & length(node_colors) != igraph::vcount(graph)) {
      stop("Number of custom colors does not equal number of nodes in the network.")
    } else {
      igraph::V(graph)$color <- node_colors
    }
  } else {
    igraph::V(graph)$color <- "black"
  }
  
  # Add edge colors
  if (is.null(edge_color)) {
    if ("combine" %in% as.character(attributes(object)$call)) {
      igraph::E(graph)$color <- "green"
      igraph::E(graph)$color[igraph::E(graph)$weight == 2] <- "red"
      igraph::E(graph)$color[igraph::E(graph)$weight == 3] <- "blue"
      # Change edge weight for networks with combine aggregation
      igraph::E(graph)$weight[igraph::E(graph)$weight > 0] <- 1
    } else if ("subtract" %in% as.character(attributes(object)$call)) {
      igraph::E(graph)$color <- "green"
      igraph::E(graph)$color[igraph::E(graph)$weight < 0] <- "red"
    } else if ("congruence" %in% as.character(attributes(object)$call)) {
      igraph::E(graph)$color <- "green"
    } else if ("conflict" %in% as.character(attributes(object)$call)) {
      igraph::E(graph)$color <- "red"
    } else {
      igraph::E(graph)$color <- "gray"
    }
  } else if (!all(is.na(edge_color))) {
    if (length(edge_color) > 1 & length(edge_color) != igraph::ecount(graph)) {
      igraph::E(graph)$color <- "gray"
      warning("Number of custom edge_colors does not match number of edges ",
              "in the network. Will set edge_color to default (gray).")
    } else {
      igraph::E(graph)$color <- edge_color
    }
  } else {
    igraph::E(graph)$color <- "gray"
  }
  
  # Add node size(s)
  if (length(node_size) > 1 & length(node_size) != igraph::vcount(graph)) {
    igraph::V(graph)$size <- 7
    warning("Number of provided node size values does not equal number of ",
            "nodes in the network. node_size will be set to default value (7).")
  } else if (is.character(node_size) & length(node_size) == 1 & !is.null(atts) && node_size %in% colnames(atts)) {
    col_pos <- which(colnames(atts) == node_size)
    igraph::V(graph)$size <- atts[match(igraph::V(graph)$name, atts$value), col_pos]
  } else if (is.numeric(node_size)) {
    igraph::V(graph)$size <- node_size
  }
  
  # Add labels
  if (!is.logical(node_label)) {
    if (is.character(node_label) & length(node_label) == 1 & !is.null(atts) && node_label %in% colnames(atts)) {
      col_pos <- which(colnames(atts) == node_label)
      igraph::V(graph)$name <- atts[match(igraph::V(graph)$name, atts$value), col_pos]
    } else if (!is.null(node_label)) {
      if (length(node_label) > 1 & length(node_label) != igraph::vcount(graph)) {
        stop("Number of custom labels does not equal number of nodes in the network.")
      }
      igraph::V(graph)$name <- node_label
    }
  }
  
  # Remove isolates
  if (exclude_isolates) {
    graph <- igraph::delete.vertices(graph, igraph::degree(graph) == 0)
  }
  
  # Only plot giant component of network. Useful for some plotting algorithms.
  if (giant_component) {
    # Get giant component
    components <- igraph::clusters(graph)
    biggest_cluster_id <- which.max(components$csize)
    
    # Get members of giant component
    vert_ids <- igraph::V(graph)[components$membership == biggest_cluster_id]
    
    # Create subgraph
    graph <- igraph::induced_subgraph(graph, vert_ids)
  }
  
  
  # Truncate labels of entities
  igraph::V(graph)$name <- sapply(igraph::V(graph)$name, function(e) if (nchar(e) > truncate) paste0(substr(e, 1, truncate - 1), "*") else e)
  
  # Use absolute edge weight values for plotting
  igraph::E(graph)$weight <- abs(igraph::E(graph)$weight)
  
  # Start network plot
  g <- ggraph::ggraph(graph, layout = layout) +
    ggraph::geom_edge_link(ggplot2::aes_string(width = "weight", color = "color"),
                           alpha = edge_alpha,
                           show.legend = FALSE) + # add edges
    ggraph::scale_edge_width(range = edge_size_range) + # add edge scale
    ggraph::geom_node_point(ggplot2::aes_string(color = "color"), # add nodes
                            size = igraph::V(graph)$size,
                            shape = igraph::V(graph)$shape,
                            show.legend = NA)
  # Add labels
  if ((!is.null(node_label) && !all(is.na(node_label))) && (is.character(node_label) || node_label == TRUE)) {
    g <- g +
      ggraph::geom_node_text(ggplot2::aes(label = igraph::V(graph)$name),
                             repel = TRUE,
                             max.overlaps = max_overlaps,
                             show.legend = FALSE)
  }
  
  # Add theme and set node colors and edges to identity
  g <- g +
    ggraph::theme_graph(base_family = "", base_size = font_size) +
    ggplot2::scale_color_identity() +
    ggraph::scale_edge_color_identity()
  
  return(g)
}


#' Compute and retrieve the backbone and redundant set
#'
#' Compute and retrieve the backbone and redundant set of a discourse network.
#'
#' This function applies a simulated annealing algorithm to the discourse
#' network to partition the set of second-mode entities (e.g., concepts) into a
#' backbone set and a complementary redundant set.
#'
#' @param penalty The penalty parameter for large backbone sets. The larger the
#'   value, the more strongly larger backbone sets are punished and the smaller
#'   the resulting backbone is. Try out different values to find the right size
#'   of the backbone set. Reasonable values could be \code{2.5}, \code{5},
#'   \code{7.5}, or \code{12}, for example. The minimum is \code{0.0}, which
#'   imposes no penalty on the size of the backbone set and produces a redundant
#'   set with only one element. Start with \code{0.0} if you want to weed out a
#'   single concept and subsequently increase the penalty to include more items
#'   in the redundant set and shrink the backbone further.
#' @param iterations The number of iterations of the simulated annealing
#'   algorithm. More iterations take more time but may lead to better
#'   optimization results.
#' @param qualifierAggregation The aggregation rule for the \code{qualifier}
#'   variable. This must be \code{"ignore"} (for ignoring the qualifier
#'   variable), \code{"congruence"} (for recording a network tie only if both
#'   nodes have the same qualifier value in the binary case or for recording the
#'   similarity between the two nodes on the qualifier variable in the integer
#'   case), \code{"conflict"} (for recording a network tie only if both nodes
#'   have a different qualifier value in the binary case or for recording the
#'   distance between the two nodes on the qualifier variable in the integer
#'   case), or \code{"subtract"} (for subtracting the conflict tie value from
#'   the congruence tie value in each dyad; note that negative values will be
#'   replaced by \code{0} in the backbone calculation).
#' @param normalization Normalization of edge weights. Valid settings are
#'   \code{"no"} (for switching off normalization), \code{"average"} (for
#'   average activity normalization), \code{"jaccard"} (for Jaccard coefficient
#'   normalization), and \code{"cosine"} (for cosine similarity normalization).
#' @param fileFormat An optional file format specification for saving the
#'   backbone results to a file instead of returning an object. Valid values
#'   are \code{"json"}, \code{"xml"}, and \code{NULL} (for returning the results
#'   instead of writing them to a file).
#' @inheritParams dna_network
#'
#' @examples
#' \dontrun{
#' dna_init()
#' dna_sample()
#' dna_openDatabase("sample.dna", coderId = 1, coderPassword = "sample")
#'
#' # compute backbone and redundant set
#' b <- dna_backbone(penalty = 3.5,
#'                   iterations = 10000,
#'                   variable1 = "organization",
#'                   variable2 = "concept",
#'                   qualifier = "agreement",
#'                   qualifierAggregation = "subtract",
#'                   normalization = "average")
#'
#' b # display main results
#'
#' # extract results from the object
#' b$backbone # show the set of backbone concepts
#' b$redundant # show the set of redundant concepts
#' b$unpenalized_backbone_loss # spectral loss between full and backbone network
#' b$unpenalized_redundant_loss # spectral loss of redundant network
#' b$backbone_network # show the backbone network
#' b$redundant_network # show the redundant network
#' b$full_network # show the full network
#'
#' # plot diagnostics with base R
#' plot(b, ma = 500)
#'
#' # arrange plots in a 2 x 2 view
#' par(mfrow = c(2, 2))
#' plot(b)
#'
#' # plot diagnostics with ggplot2
#' library("ggplot2")
#' p <- autoplot(b)
#' p
#'
#' # pick a specific diagnostic
#' p[[3]]
#'
#' # use the patchwork package to arrange the diagnostics in a single plot
#' library("patchwork")
#' new_plot <- p[[1]] + p[[2]] + p[[3]] + p[[4]]
#' new_plot & theme_grey() + theme(legend.position = "bottom")
#'
#' # use the gridExtra package to arrange the diagnostics in a single plot
#' library("gridExtra")
#' grid.arrange(p[[1]], p[[2]], p[[3]], p[[4]])
#' }
#'
#' @author Philip Leifeld, Tim Henrichsen
#'
#' @importFrom rJava .jarray
#' @importFrom rJava .jcall
#' @importFrom rJava .jnull
#' @importFrom rJava J
#' @noRd
dna_backbone <- function(penalty = 3.5,
                         iterations = 10000,
                         statementType = "DNA Statement",
                         variable1 = "organization",
                         variable1Document = FALSE,
                         variable2 = "concept",
                         variable2Document = FALSE,
                         qualifier = "agreement",
                         qualifierDocument = FALSE,
                         qualifierAggregation = "subtract",
                         normalization = "average",
                         duplicates = "document",
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
                         fileFormat = NULL,
                         outfile = NULL) {

  # wrap the vectors of exclude values for document variables into Java arrays
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
  var <- .jarray(var) # array of variable names of each excluded value
  val <- .jarray(val) # array of values to be excluded

  # encode R NULL as Java null value if necessary
  if (is.null(qualifier) || is.na(qualifier)) {
    qualifier <- .jnull(class = "java/lang/String")
  }
  if (is.null(fileFormat)) {
    fileFormat <- .jnull(class = "java/lang/String")
  }
  if (is.null(outfile)) {
    outfile <- .jnull(class = "java/lang/String")
  }

  # call rBackbone function to compute results
  .jcall(dnaEnvironment[["dna"]]$headlessDna,
         "V",
         "rBackbone",
         as.double(penalty),
         as.integer(iterations),
         statementType,
         variable1,
         variable1Document,
         variable2,
         variable2Document,
         qualifier,
         qualifierDocument,
         qualifierAggregation,
         normalization,
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
         outfile,
         fileFormat
  )

  exporter <- .jcall(dnaEnvironment[["dna"]]$headlessDna, "Lexport/Exporter;", "getExporter") # get a reference to the Exporter object, in which results are stored
  result <- .jcall(exporter, "Lexport/BackboneResult;", "getBackboneResult", simplify = TRUE)
  if (!is.null(outfile) && !is.null(fileFormat) && is.character(outfile) && is.character(fileFormat) && fileFormat %in% c("json", "xml")) {
    message("File exported.")
  } else {
    # create a list with various results
    l <- list()
    l$penalty <- .jcall(result, "D", "getPenalty")
    l$iterations <- .jcall(result, "I", "getIterations")
    l$backbone <- .jcall(result, "[S", "getBackboneEntities")
    l$redundant <- .jcall(result, "[S", "getRedundantEntities")
    l$unpenalized_backbone_loss <- .jcall(result, "D", "getUnpenalizedBackboneLoss")
    l$unpenalized_redundant_loss <- .jcall(result, "D", "getUnpenalizedRedundantLoss")
    rn <- .jcall(result, "[S", "getLabels")

    # store the three matrices in the result list
    fullmat <- .jcall(result, "[[D", "getFullNetwork", simplify = TRUE)
    rownames(fullmat) <- rn
    colnames(fullmat) <- rn
    l$full_network <- fullmat
    backbonemat <- .jcall(result, "[[D", "getBackboneNetwork", simplify = TRUE)
    rownames(backbonemat) <- rn
    colnames(backbonemat) <- rn
    l$backbone_network <- backbonemat
    redundantmat <- .jcall(result, "[[D", "getRedundantNetwork", simplify = TRUE)
    rownames(redundantmat) <- rn
    colnames(redundantmat) <- rn
    l$redundant_network <- redundantmat

    # store diagnostics per iteration as a data frame
    d <- data.frame(iteration = 1:.jcall(result, "I", "getIterations"),
                    temperature = .jcall(result, "[D", "getTemperature"),
                    acceptance_prob = .jcall(result, "[D", "getAcceptanceProbability"),
                    acceptance = .jcall(result, "[I", "getAcceptance"),
                    penalized_backbone_loss = .jcall(result, "[D", "getPenalizedBackboneLoss"),
                    proposed_backbone_size = .jcall(result, "[I", "getProposedBackboneSize"),
                    current_backbone_size = .jcall(result, "[I", "getCurrentBackboneSize"),
                    optimal_backbone_size = .jcall(result, "[I", "getOptimalBackboneSize"),
                    acceptance_ratio_ma = .jcall(result, "[D", "getAcceptanceRatioMovingAverage"))

    l$diagnostics <- d

    # store start date/time, end date/time, number of statements, call, and class label in each network matrix
    start <- as.POSIXct(.jcall(result, "J", "getStart"), origin = "1970-01-01") # add the start date/time of the result as an attribute to the matrices
    attributes(l$full_network)$start <- start
    attributes(l$backbone_network)$start <- start
    attributes(l$redundant_network)$start <- start
    stop <- as.POSIXct(.jcall(result, "J", "getStop"), origin = "1970-01-01") # add the end date/time of the result as an attribute to the matrices
    attributes(l$full_network)$stop <- stop
    attributes(l$backbone_network)$stop <- stop
    attributes(l$redundant_network)$stop <- stop
    attributes(l$full_network)$numStatements <- .jcall(result, "I", "getNumStatements") # add the number of filtered statements the matrix is based on as an attribute to the matrix
    attributes(l$full_network)$call <- match.call()
    attributes(l$backbone_network)$call <- match.call()
    attributes(l$redundant_network)$call <- match.call()
    class(l$full_network) <- c("dna_network_onemode", class(l$full_network))
    class(l$backbone_network) <- c("dna_network_onemode", class(l$backbone_network))
    class(l$redundant_network) <- c("dna_network_onemode", class(l$redundant_network))

    class(l) <- c("dna_backbone", class(l))
    return(l)
  }
}

#' @param ma Number of iterations to compute moving average.
#' @rdname dna_backbone
#' @importFrom graphics lines
#' @importFrom stats filter
#' @noRd
plot.dna_backbone <- function(x, ma = 500, ...) {
  # temperature and acceptance probability
  plot(x = x$diagnostics$iteration,
       y = x$diagnostics$temperature,
       col = "red",
       type = "l",
       lwd = 3,
       xlab = "Iteration",
       ylab = "Acceptance probability",
       main = "Temperature and acceptance probability")
  # note that better solutions are coded as -1 and need to be skipped:
  lines(x = x$diagnostics$iteration[x$diagnostics$acceptance_prob >= 0],
        y = x$diagnostics$acceptance_prob[x$diagnostics$acceptance_prob >= 0])

  # spectral distance between full network and backbone network per iteration
  bb_loss <- stats::filter(x$diagnostics$penalized_backbone_loss,
                           rep(1 / ma, ma),
                           sides = 1)
  plot(x = x$diagnostics$iteration,
       y = bb_loss,
       type = "l",
       xlab = "Iteration",
       ylab = "Penalized backbone loss",
       main = "Penalized spectral backbone distance")

  # number of concepts in the backbone solution per iteration
  current_size_ma <- stats::filter(x$diagnostics$current_backbone_size,
                                   rep(1 / ma, ma),
                                   sides = 1)
  optimal_size_ma <- stats::filter(x$diagnostics$optimal_backbone_size,
                                   rep(1 / ma, ma),
                                   sides = 1)
  plot(x = x$diagnostics$iteration,
       y = current_size_ma,
       ylim = c(min(c(current_size_ma, optimal_size_ma), na.rm = TRUE),
                max(c(current_size_ma, optimal_size_ma), na.rm = TRUE)),
       type = "l",
       xlab = "Iteration",
       ylab = paste0("Number of elements (MA, last ", ma, ")"),
       main = "Backbone size (red = best)")
  lines(x = x$diagnostics$iteration, y = optimal_size_ma, col = "red")

  # ratio of recent acceptances
  accept_ratio <- stats::filter(x$diagnostics$acceptance,
                                rep(1 / ma, ma),
                                sides = 1)
  plot(x = x$diagnostics$iteration,
       y = accept_ratio,
       type = "l",
       xlab = "Iteration",
       ylab = paste("Acceptance ratio in the last", ma, "iterations"),
       main = "Acceptance ratio")
}

#' @rdname dna_backbone
#' @param x A \code{"dna_backbone"} object.
#' @noRd
print.dna_backbone <- function(x, ...) {
  cat(paste0("Penalty: ", x$penalty, ". Iterations: ", x$iterations, ".\n\n"))
  cat(paste0("Backbone set (loss: ", round(x$unpenalized_backbone_loss, 4), "):\n"))
  cat(paste(1:length(x$backbone), x$backbone), sep = "\n")
  cat(paste0("\nRedundant set (loss: ", round(x$unpenalized_redundant_loss, 4), "):\n"))
  cat(paste(1:length(x$redundant), x$redundant), sep = "\n")
}

#' @rdname dna_backbone
#' @param object A \code{"dna_backbone"} object.
#' @param ... Additional arguments.
#' @importFrom ggplot2 autoplot
#' @importFrom ggplot2 ggplot
#' @importFrom ggplot2 aes_string
#' @importFrom ggplot2 geom_line
#' @importFrom ggplot2 ylab
#' @importFrom ggplot2 xlab
#' @importFrom ggplot2 ggtitle
#' @importFrom ggplot2 theme_bw
#' @importFrom ggplot2 theme
#' @noRd
autoplot.dna_backbone <- function(object, ..., ma = 500) {
  bd <- object$diagnostics
  bd$bb_loss <- stats::filter(bd$penalized_backbone_loss, rep(1 / ma, ma), sides = 1)
  bd$current_size_ma <- stats::filter(bd$current_backbone_size, rep(1 / ma, ma), sides = 1)
  bd$optimal_size_ma <- stats::filter(bd$optimal_backbone_size, rep(1 / ma, ma), sides = 1)
  bd$accept_ratio <- stats::filter(bd$acceptance, rep(1 / ma, ma), sides = 1)

  # temperature and acceptance probability
  g_accept <- ggplot2::ggplot(bd, ggplot2::aes_string(y = "temperature", x = "iteration")) +
    ggplot2::geom_line(color = "#a50f15") +
    ggplot2::geom_line(data = bd[bd$acceptance_prob >= 0, ],
                       ggplot2::aes_string(y = "acceptance_prob", x = "iteration")) +
    ggplot2::ylab("Acceptance probability") +
    ggplot2::xlab("Iteration") +
    ggplot2::ggtitle("Temperature and acceptance probability") +
    ggplot2::theme_bw()

  # spectral distance between full network and backbone network per iteration
  g_loss <- ggplot2::ggplot(bd, ggplot2::aes_string(y = "bb_loss", x = "iteration")) +
    ggplot2::geom_line() +
    ggplot2::ylab("Penalized backbone loss") +
    ggplot2::xlab("Iteration") +
    ggplot2::ggtitle("Penalized spectral backbone distance") +
    ggplot2::theme_bw()

  # number of concepts in the backbone solution per iteration
  d <- data.frame(iteration = rep(bd$iteration, 2),
                  size = c(bd$current_size_ma, bd$optimal_size_ma),
                  Criterion = c(rep("Current iteration", nrow(bd)),
                                rep("Best solution", nrow(bd))))
  g_size <- ggplot2::ggplot(d, ggplot2::aes_string(y = "size", x = "iteration", color = "Criterion")) +
    ggplot2::geom_line() +
    ggplot2::ylab(paste0("Number of elements (MA, last ", ma, ")")) +
    ggplot2::xlab("Iteration") +
    ggplot2::ggtitle("Backbone size") +
    ggplot2::theme_bw() +
    ggplot2::theme(legend.position = "bottom")

  # ratio of recent acceptances
  g_ar <- ggplot2::ggplot(bd, ggplot2::aes_string(y = "accept_ratio", x = "iteration")) +
    ggplot2::geom_line() +
    ggplot2::ylab(paste("Acceptance ratio in the last", ma, "iterations")) +
    ggplot2::xlab("Iteration") +
    ggplot2::ggtitle("Acceptance ratio") +
    ggplot2::theme_bw()

  # wrap in list
  plots <- list(g_accept, g_loss, g_size, g_ar)
  return(plots)
}

#' Generate the data necessary for creating a barplot for a variable
#'
#' Generate the data necessary for creating a barplot for a variable.
#'
#' Create a \code{dna_barplot} object, which contains a data frame with
#' entity value frequencies grouped by the levels of a qualifier variable.
#' The qualifier variable is optional.
#'
#' @param variable The variable for which the barplot will be generated. There
#'   will be one bar per entity label of this variable.
#' @param qualifier A boolean (binary) or integer variable to group the value
#'   frequencies by. Can be \code{NULL} to skip the grouping.
#' @inheritParams dna_network
#'
#' @examples
#' \dontrun{
#' dna_init()
#' dna_sample()
#' dna_openDatabase("sample.dna", coderId = 1, coderPassword = "sample")
#'
#' # compute barplot data
#' b <- dna_barplot(statementType = "DNA Statement",
#'                  variable = "concept",
#'                  qualifier = "agreement")
#' b
#' }
#'
#' @author Philip Leifeld
#'
#' @seealso \link{print.dna_barplot}, \link{autoplot.dna_barplot}
#'
#' @importFrom rJava .jarray
#' @importFrom rJava .jcall
#' @importFrom rJava .jevalArray
#' @importFrom rJava .jnull
#' @importFrom rJava is.jnull
#' @export
dna_barplot <- function(statementType = "DNA Statement",
                        variable = "concept",
                        qualifier = "agreement",
                        duplicates = "document",
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
                        invertTypes = FALSE) {

  # wrap the vectors of exclude values for document variables into Java arrays
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
  var <- .jarray(var) # array of variable names of each excluded value
  val <- .jarray(val) # array of values to be excluded

  # encode R NULL as Java null value if necessary
  if (is.null(qualifier) || is.na(qualifier)) {
    qualifier <- .jnull(class = "java/lang/String")
  }

  # call rBarplotData function to compute results
  b <- .jcall(dnaEnvironment[["dna"]]$headlessDna,
              "Lexport/BarplotResult;",
              "rBarplotData",
              statementType,
              variable,
              qualifier,
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
              simplify = TRUE)

  at <- .jcall(b, "[[Ljava/lang/String;", "getAttributes")
  at <- t(sapply(at, FUN = .jevalArray))

  counts <- .jcall(b, "[[I", "getCounts")
  counts <- t(sapply(counts, FUN = .jevalArray))
  if (nrow(counts) < nrow(at)) {
    counts <- t(counts)
  }

  results <- data.frame(.jcall(b, "[S", "getValues"),
                        counts,
                        at)

  intValues <- .jcall(b, "[I", "getIntValues")
  intColNames <- intValues
  if (is.jnull(qualifier)) {
    intValues <- integer(0)
    intColNames <- "Frequency"
  }

  atVar <- .jcall(b, "[S", "getAttributeVariables")

  colnames(results) <- c("Entity", intColNames, atVar)

  attributes(results)$variable <- .jcall(b, "S", "getVariable")
  attributes(results)$intValues <- intValues
  attributes(results)$attributeVariables <- atVar

  class(results) <- c("dna_barplot", class(results))

  return(results)
}

#' Print a \code{dna_barplot} object
#'
#' Show details of a \code{dna_barplot} object.
#'
#' Print the data frame returned by the \code{\link{dna_barplot}} function.
#'
#' @param x A \code{dna_barplot} object, as returned by the
#'   \code{\link{dna_barplot}} function.
#' @param trim Number of maximum characters to display in entity labels.
#'   Entities with more characters are truncated, and the last character is
#'   replaced by an asterisk (\code{*}).
#' @param attr Display attributes, such as the name of the variable and the
#'   levels of the qualifier variable if available.
#' @param ... Additional arguments. Currently not in use.
#'
#' @author Philip Leifeld
#'
#' @seealso \link{dna_barplot}, \link{autoplot.dna_barplot}
#' @export
print.dna_barplot <- function(x, trim = 30, attr = TRUE, ...) {
  x2 <- x
  if (isTRUE(attr)) {
    cat("Variable:", attr(x2, "variable"))
    intVal <- attr(x2, "intValues")
    if (length(intVal) > 0) {
      cat(".\nQualifier levels:", paste(intVal, collapse = ", "))
    } else {
      cat(".\nNo qualifier variable")
    }
    cat(".\n")
  }
  x2$Entity <- sapply(x2$Entity, function(e) if (nchar(e) > trim) paste0(substr(e, 1, trim - 1), "*") else e)
  class(x2) <- "data.frame"
  print(x2)
}

#' Plot \code{dna_barplot} object.
#'
#' Plot a barplot generated from \code{\link{dna_barplot}}.
#'
#' This function plots \code{dna_barplot} objects generated by the
#' \code{\link{dna_barplot}} function. It plots agreement and disagreement with
#' DNA statements for different entities such as \code{"concept"},
#' \code{"organization"}, or \code{"person"}. Colors can be modified before
#' plotting (see examples).
#'
#' @param object A \code{dna_barplot} object.
#' @param lab.pos,lab.neg Names for (dis-)agreement labels.
#' @param lab Should (dis-)agreement labels and title be displayed?
#' @param colors If \code{TRUE}, the \code{Colors} column in the
#'   \code{dna_barplot} object will be used to fill the bars. Also accepts
#'   character objects matching one of the attribute variables of the
#'   \code{dna_barplot} object.
#' @param fontSize Text size in pt.
#' @param barWidth Thickness of the bars. Bars will touch when set to \code{1}.
#'   When set to \code{0.5}, space between two bars is the same as thickness of
#'   bars.
#' @param axisWidth Thickness of the x-axis which separates agreement from
#'   disagreement.
#' @param truncate Sets the number of characters to which axis labels should be
#'   truncated.
#' @param exclude.min Reduces the plot to entities with a minimum frequency of
#'   statements.
#'
#' @examples
#' \dontrun{
#' dna_init()
#' dna_sample()
#'
#' dna_openDatabase("sample.dna", coderId = 1, coderPassword = "sample")
#'
#' # compute barplot data
#' b <- dna_barplot(statementType = "DNA Statement",
#'                  variable = "concept",
#'                  qualifier = "agreement")
#'
#' # plot barplot with ggplot2
#' library("ggplot2")
#' autoplot(b)
#'
#' # use entity colours (here: colors of organizations as an illustration)
#' b <- dna_barplot(statementType = "DNA Statement",
#'                  variable = "organization",
#'                  qualifier = "agreement")
#' autoplot(b, colors = TRUE)
#'
#' # edit the colors before plotting
#' b$Color[b$Type == "NGO"] <- "red"         # change NGO color to red
#' b$Color[b$Type == "Government"] <- "blue" # change government color to blue
#' autoplot(b, colors = TRUE)
#'
#' # use an attribute, such as type, to color the bars
#' autoplot(b, colors = "Type") +
#'   scale_colour_manual(values = "black")
#'
#' # replace colors for the three possible actor types with custom colors
#' autoplot(b, colors = "Type") +
#'   scale_fill_manual(values = c("red", "blue", "green")) +
#'   scale_colour_manual(values = "black")
#' }
#'
#' @author Johannes B. Gruber, Tim Henrichsen
#'
#' @seealso \link{dna_barplot}
#'
#' @importFrom ggplot2 autoplot
#' @importFrom ggplot2 ggplot
#' @importFrom ggplot2 aes_string
#' @importFrom ggplot2 geom_line
#' @importFrom ggplot2 theme_minimal
#' @importFrom ggplot2 theme
#' @importFrom ggplot2 geom_bar
#' @importFrom ggplot2 position_stack
#' @importFrom ggplot2 coord_flip
#' @importFrom ggplot2 element_blank
#' @importFrom ggplot2 element_text
#' @importFrom ggplot2 scale_color_identity
#' @importFrom ggplot2 scale_fill_identity
#' @importFrom ggplot2 geom_text
#' @importFrom ggplot2 .pt
#' @importFrom ggplot2 annotate
#' @importFrom ggplot2 scale_x_discrete
#' @importFrom utils stack
#' @importFrom grDevices col2rgb
#' @export
autoplot.dna_barplot <- function(object,
                                 lab.pos = "Agreement",
                                 lab.neg = "Disagreement",
                                 lab = TRUE,
                                 colors = FALSE,
                                 fontSize = 12,
                                 barWidth = 0.6,
                                 axisWidth = 1.5,
                                 truncate = 40,
                                 exclude.min = NULL) {


  if (!("dna_barplot" %in% class(object))) {
    stop("Invalid data object. Please compute a dna_barplot object via the ",
         "dna_barplot function before plotting.")
  }

  if (!("Entity" %in% colnames(object))) {
    stop("dna_barplot object does not have a \'Entity\' variable. Please ",
         "compute a new dna_barplot object via the dna_barplot function before",
         " plotting.")
  }

  if (isTRUE(colors) & !("Color" %in% colnames(object)) |
      is.character(colors) & !(colors %in% colnames(object))) {
    colors <- FALSE
    warning("No color variable found in dna_barplot object. Colors will be",
            " ignored.")
  }

  if (!is.numeric(truncate)) {
    truncate <- Inf
    warning("No numeric value provided for trimming of entities. Truncation ",
            "will be ignored.")
  }

  # Get qualifier values
  w <- attr(object, "intValues")

  if (!all(w %in% colnames(object))) {
    stop("dna_barplot object does not include all qualifier values of the ",
         "statement type. Please compute a new dna_barplot object via the ",
         "dna_barplot function.")
  }

  # Check if qualifier is binary
  binary <- all(w %in% c(0, 1))

  # Compute total values per entity
  object$sum <- rowSums(object[, colnames(object) %in% w])

  # Exclude minimum number of statements per entity
  if (is.numeric(exclude.min)) {
    if (exclude.min > max(object$sum)) {
      exclude.min <- NULL
      warning("Value provided in exclude.min is higher than maximum frequency ",
              "of entity (", max(object$sum), "). Will ignore exclude.min.")
    } else {
      object <- object[object$sum >= exclude.min, ]
    }
  }

  # Stack agreement and disagreement
  object2 <- cbind(object$Entity, utils::stack(object, select = colnames(object) %in% w))
  colnames(object2) <- c("entity", "frequency", "agreement")

  object <- object[order(object$sum, decreasing = TRUE), ]

  object2$entity <- factor(object2$entity, levels = rev(object$Entity))

  # Get colors
  if (isTRUE(colors)) {
    object2$color <- object$Color[match(object2$entity, object$Entity)]
    object2$text_color <- "black"
    # Change text color to white in case of dark bar colors
    object2$text_color[sum(grDevices::col2rgb(object2$color) * c(299, 587, 114)) / 1000 < 123] <- "white"
  } else if (is.character(colors)) {
    object2$color <- object[, colors][match(object2$entity, object$Entity)]
    object2$text_color <- "black"
  } else {
    object2$color <- "white"
    object2$text_color <- "black"
  }


  if (binary) {
    # setting disagreement as -1 instead 0
    object2$agreement <- ifelse(object2$agreement == 0, -1, 1)
    # recode frequency in positive and negative
    object2$frequency <- object2$frequency * as.integer(object2$agreement)

    # generate position of bar labels
    offset <- (max(object2$frequency) + abs(min(object2$frequency))) * 0.05
    offset <- ifelse(offset < 0.5, 0.5, offset) # offset should be at least 0.5
    if (offset > abs(min(object2$frequency))) {
      offset <- abs(min(object2$frequency))
    }
    if (offset > max(object2$frequency)) {
      offset <- abs(min(object2$frequency))
    }
    object2$pos <- ifelse(object2$frequency > 0,
                          object2$frequency + offset,
                          object2$frequency - offset)

    # move 0 labels where necessary
    object2$pos[object2$frequency == 0] <- ifelse(object2$agreement[object2$frequency == 0] == 1,
                                                  object2$pos[object2$frequency == 0] * -1,
                                                  object2$pos[object2$frequency == 0])
    object2$label <- as.factor(abs(object2$frequency))
  } else {
    object2$count <- object2$frequency
    # set frequency of negative qualifiers to negative values
    object2$frequency <- ifelse(as.numeric(as.character(object2$agreement)) >= 0, object2$frequency,
                                object2$frequency * -1)
    # remove zero frequencies
    object2 <- object2[object2$frequency != 0, ]
    # generate position of bar labels
    object2$pos <- ifelse(object2$frequency > 0,
                          1.1,
                          -0.1)
    # Add labels
    object2$label <- paste(object2$count, object2$agreement, sep = " x ")
  }

  offset <- (max(object2$frequency) + abs(min(object2$frequency))) * 0.05
  offset <- ifelse(offset < 0.5, 0.5, offset)
  yintercepts <- data.frame(x = c(0.5, length(unique(object2$entity)) + 0.5),
                            y = c(0, 0))
  high <- yintercepts$x[2] + 0.25

  object2 <- object2[order(as.numeric(as.character(object2$agreement)),
                           decreasing = FALSE), ]
  object2$agreement <- factor(object2$agreement, levels = w)

  # Plot
  g <- ggplot2::ggplot(object2,
                       ggplot2::aes_string(x = "entity",
                                           y = "frequency",
                                           fill = "agreement",
                                           group = "agreement",
                                           label = "label"))
  if (binary) { # Bars for the binary case
    g <- g + ggplot2::geom_bar(ggplot2::aes_string(fill = "color",
                                                   color = "text_color"),
                               stat = "identity",
                               width = barWidth,
                               show.legend = FALSE)
    # For the integer case with positive and negative values
  } else if (max(w) > 0 & min(w) < 0) {
    g <- g + ggplot2::geom_bar(ggplot2::aes_string(fill = "color",
                                                   color = "text_color"),
                               stat = "identity",
                               width = barWidth,
                               show.legend = FALSE,
                               data = object2[as.numeric(as.character(object2$agreement)) >= 0, ],
                               position = ggplot2::position_stack(reverse = TRUE)) +
      ggplot2::geom_bar(ggplot2::aes_string(fill = "color",
                                            color = "text_color"),
                        stat = "identity",
                        width = barWidth,
                        show.legend = FALSE,
                        data = object2[as.numeric(as.character(object2$agreement)) < 0, ])
    # For the integer case with positive values only
  } else if (min(w) >= 0) {
    g <- g + ggplot2::geom_bar(ggplot2::aes_string(fill = "color",
                                                   color = "text_color"),
                               stat = "identity",
                               width = barWidth,
                               show.legend = FALSE,
                               position = ggplot2::position_stack(reverse = TRUE))
    # For the integer case with negative values only
  } else {
    g <- g + ggplot2::geom_bar(ggplot2::aes_string(fill = "color",
                                                   color = "text_color"),
                               stat = "identity",
                               width = barWidth,
                               show.legend = FALSE)
  }
  g <- g + ggplot2::coord_flip() +
    ggplot2::theme_minimal() +
    # Add intercept line
    ggplot2::geom_line(ggplot2::aes_string(x = "x", y = "y"),
                       data = yintercepts,
                       linewidth = axisWidth,
                       inherit.aes = FALSE) +
    # Remove all panel grids, axis titles and axis ticks and text for x-axis
    ggplot2::theme(panel.grid.major = ggplot2::element_blank(),
                   panel.grid.minor = ggplot2::element_blank(),
                   axis.title = ggplot2::element_blank(),
                   axis.ticks.y = ggplot2::element_blank(),
                   axis.text.x = ggplot2::element_blank(),
                   axis.text.y = ggplot2::element_text(size = fontSize)) #+
  if (is.logical(colors)) {
    g <- g + ggplot2::scale_fill_identity() +
      ggplot2::scale_color_identity()
  }
  if (binary) { # Add entity labels for binary case
    g <- g +
      ggplot2::geom_text(ggplot2::aes_string(x = "entity",
                                             y = "pos",
                                             label = "label"),
                         size = (fontSize / ggplot2::.pt),
                         inherit.aes = FALSE,
                         data = object2)
    # Add entity labels for integer case with positive and negative values
  } else if (max(w) > 0 & min(w) < 0) {
    g <- g +
      ggplot2::geom_text(ggplot2::aes_string(color = "text_color"),
                         size = (fontSize / ggplot2::.pt),
                         position = ggplot2::position_stack(vjust = 0.5, reverse = TRUE),
                         inherit.aes = TRUE,
                         data = object2[object2$frequency >= 0, ]) +
      ggplot2::geom_text(ggplot2::aes_string(color = "text_color"),
                         size = (fontSize / ggplot2::.pt),
                         position = ggplot2::position_stack(vjust = 0.5),
                         inherit.aes = TRUE,
                         data = object2[object2$frequency < 0, ])
    # Add entity labels for integer case with positive values only
  } else if (min(w) >= 0) {
    g <- g +
      ggplot2::geom_text(ggplot2::aes_string(color = "text_color"),
                         size = (fontSize / ggplot2::.pt),
                         position = ggplot2::position_stack(vjust = 0.5, reverse = TRUE),
                         inherit.aes = TRUE)
  } else {
    g <- g +
      ggplot2::geom_text(ggplot2::aes_string(color = "text_color"),
                         size = (fontSize / ggplot2::.pt),
                         position = ggplot2::position_stack(vjust = 0.5),
                         inherit.aes = TRUE)
  }
  if (lab) { # Add (dis-)agreement labels
    g <- g +
      ggplot2::annotate("text",
                        x = high,
                        y = offset * 2,
                        hjust = 0,
                        label = lab.pos,
                        size = (fontSize / ggplot2::.pt)) +
      ggplot2::annotate("text",
                        x = high,
                        y = 0 - offset * 2,
                        hjust = 1,
                        label = lab.neg,
                        size = (fontSize / ggplot2::.pt)) +
      # Truncate labels of entities
      ggplot2::scale_x_discrete(labels = sapply(as.character(object2$entity), function(e) if (nchar(e) > truncate) paste0(substr(e, 1, truncate - 1), "*") else e),
                                expand = c(0, 2, 0, 2),
                                limits = levels(object2$entity))
  } else {
    g <- g +
      # Truncate labels of entities
      ggplot2::scale_x_discrete(labels = sapply(as.character(object2$entity), function(e) if (nchar(e) > truncate) paste0(substr(e, 1, truncate - 1), "*") else e),
                                limits = levels(object2$entity))
  }
  return(g)
}
