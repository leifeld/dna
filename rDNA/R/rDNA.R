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
    'Author:       Philip Leifeld  (University of Essex)\n',
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
    dir.create(targetdir, showWarnings = FALSE)
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
#' conn <- dna_connection(dna_sample())
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
    m <- .jcall(exporter, "[Lmodel/Matrix;", "getMatrixResultsArray") # get list of Matrix objects from Exporter object
    l <- list() # create a list in which each result is stored; can be of length 1 if no time window is used
    for (t in 1:length(m)) { # loop through the matrices
      mat <- .jcall(m[[1]], "[[D", "getMatrix", simplify = TRUE) # get the resulting matrix at step t as a double[][] object and save as matrix
      rownames(mat) <- .jcall(m[[1]], "[S", "getRowNames", simplify = TRUE) # add the row names to the matrix
      colnames(mat) <- .jcall(m[[1]], "[S", "getColumnNames", simplify = TRUE) # add the column names to the matrix
      attributes(mat)$start <- as.POSIXct(.jcall(m[[1]], "J", "getStartLong"), origin = "1970-01-01") # add the start date/time of the result as an attribute to the matrix
      attributes(mat)$stop <- as.POSIXct(.jcall(m[[1]], "J", "getStopLong"), origin = "1970-01-01") # add the end date/time of the result as an attribute to the matrix
      if (length(m) > 1) {
        attributes(mat)$middle <- as.POSIXct(.jcall(m[[1]], "J", "getDateTimeLong"), origin = "1970-01-01") # add the mid-point date/time around which the time window is centered if the time window algorithm was used
      }
      attributes(mat)$numStatements <- .jcall(m[[1]], "I", "getNumStatements") # add the number of filtered statements the matrix is based on as an attribute to the matrix
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