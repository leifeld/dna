
# some settings
dnaEnvironment <- new.env(hash = TRUE, parent = emptyenv())

# function for initializing the connection with DNA
dna_init <- function(jarfile = "dna-2.0-beta19.jar") {
  assign("dnaJarString", jarfile, pos = dnaEnvironment)
  message(paste("Jar file:", dnaEnvironment[["dnaJarString"]]))
  .jinit(dnaEnvironment[["dnaJarString"]], force.init = TRUE)
}

# function for opening the DNA GUI
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

# function for establishing a database connection
dna_connection <- function(infile, login = NULL, password = NULL, verbose = TRUE) {
  if (is.null(login) || is.null(password)) {
    export <- .jnew("dna.export/Exporter", "sqlite", infile, "", "", verbose)
  } else {
    export <- .jnew("dna.export/Exporter", "mysql", infile, login, password, verbose)
  }
  obj <- list(dna_connection = export)
  class(obj) <- "dna_connection"
  return(obj)
}

# print a summary of a dna_connection object
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
