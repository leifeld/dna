
dnaEnvironment <- new.env(hash = TRUE, parent = emptyenv())

dna.init <- function(dna.jar.file) {
  assign("dnaJarString", dna.jar.file, pos = dnaEnvironment)
  .jinit(dna.jar.file, force.init = TRUE)
}

dna.gui <- function(memory = 1024){
  djs <- dnaEnvironment[["dnaJarString"]]
  if (is.null(djs)) {
    stop("You should run dna.init() first. See ?dna.init for details.")
  } else {
    system(paste("java -jar -Xmx", memory, "M ", djs, sep = ""))
  }
}

dna.network <- function(infile, algorithm = "cooccurrence", 
    agreement = "combined", start.date = "01.01.1900", stop.date = "31.12.2099",
    two.mode.type = "oc", one.mode.type = "organizations", via = "categories", 
    ignore.duplicates = TRUE, include.isolates = FALSE, normalization = FALSE, 
    window.size = 100, step.size = 1, alpha = 100, lambda = 0.1, 
    ignore.agreement = FALSE, exclude.persons = c(""), 
    exclude.organizations = c(""), exclude.categories = c(""), 
    invert.persons = FALSE, invert.organizations = FALSE, 
    invert.categories = FALSE, verbose = TRUE) {
  if (algorithm == "sonia") {
    stop(paste("SoNIA and rSoNIA are not supported. Please ", 
    "use the GUI of DNA instead."), sep = "")
  } else if (algorithm == "dynamic") {
    stop(paste("Dynamic algorithms like SoNIA and Commetrix ", 
    "are not supported. Please use the GUI of DNA instead."), sep = "")
  } else if (algorithm == "commetrix") {
    stop(paste("Commetrix is not supported in the R version of DNA. ", 
    "Please use the GUI of DNA instead."), sep = "")
  } else {
    if (length(exclude.persons) == 0) {
      exclude.persons <- c("", "")
    } else if (length(exclude.persons) == 1) {
      exclude.persons <- c(exclude.persons, "")
    }
    if (length(exclude.organizations) == 0) {
      exclude.organizations <- c("", "")
    } else if (length(exclude.organizations) == 1) {
      exclude.organizations <- c(exclude.organizations, "")
    }
    if (length(exclude.categories) == 0) {
      exclude.categories <- c("", "")
    } else if (length(exclude.categories) == 1) {
      exclude.categories <- c(exclude.categories, "")
    }

    #initialize the export class and parse the .dna file
    export <- .jnew("dna/Export", infile, exclude.persons, 
      exclude.organizations, exclude.categories, start.date, stop.date, 
      agreement, algorithm, two.mode.type, one.mode.type, via, 
      include.isolates, ignore.duplicates, normalization, window.size, 
      step.size, 100.0, lambda, ignore.agreement, 100, 100, "DNA_CMX", 100.0, 
      invert.persons, invert.organizations, invert.categories, verbose)
    
    #pull the Java network data into a list of vectors in R
    matObj <- .jcall(export, "[[D", "matrixObject")
    num.rows <- length(matObj) #calculate the number of rows of the sociomatrix
    num.cols <- length(matObj[[1]]) #calculate the number of columns of matrix
    #create a matrix with these dimensions
    mat <- matrix(nrow = num.rows, ncol = num.cols)
    for (i in 1:length(matObj)) {
      mat[i, ] <- .jevalArray(matObj[[i]]) #fill matrix with rows from the list
    }
    #pull the row labels into R
    row.labels <- .jcall(export, "[S", "getMatrixLabels", TRUE)
    #pull the column labels into R
    col.labels <- .jcall(export, "[S", "getMatrixLabels", FALSE)
    rownames(mat) <- row.labels #assign the row labels to the matrix
    colnames(mat) <- col.labels #assign the column labels to the matrix
    
    clean <- .jcall(export, "V", "cleanUp")
    
    return(mat) #return the matrix
    }
}

dna.attributes <- function(infile, organizations = TRUE, verbose = TRUE) {
  file <- .jnew("dna/Export", infile, verbose)
  if (organizations == TRUE) {
    names <- .jcall(file, "[S", "exportAttributes", TRUE, 0)
    type <- .jcall(file, "[S", "exportAttributes", TRUE, 1)
    alias <- .jcall(file, "[S", "exportAttributes", TRUE, 2)
    note <- .jcall(file, "[S", "exportAttributes", TRUE, 3)
    color <- .jcall(file, "[S", "exportAttributes", TRUE, 4)
  } else {
    names <- .jcall(file, "[S", "exportAttributes", FALSE, 0)
    type <- .jcall(file, "[S", "exportAttributes", FALSE, 1)
    alias <- .jcall(file, "[S", "exportAttributes", FALSE, 2)
    note <- .jcall(file, "[S", "exportAttributes", FALSE, 3)
    color <- .jcall(file, "[S", "exportAttributes", FALSE, 4)
  }
  data <- cbind(type, alias, note, color)
  rownames(data) <- names
  
  clean <- .jcall(file, "V", "cleanUp")
  
  return(data)
}

dna.density <- function(network.matrix, partitions = "", weighted = FALSE, 
    verbose = FALSE) {
  x <- dim(network.matrix)[1]
  y <- dim(network.matrix)[2]
  numCells <- x * y
  
  if (length(partitions) <= 1) {
    density <- 0
    if (weighted == FALSE) {
      for (i in 1:length(network.matrix)) {
        if (network.matrix[i] != 0) {
          density <- density + 1
        }
      }
      density <- density / numCells
      if (verbose == TRUE) {
        cat("\nOverall (binary) density: ")
      }
    } else if (weighted == TRUE) {
      density <- sum(network.matrix) / numCells
      if (verbose == TRUE) {
        cat("\nOverall (weighted) density: ")
      }
    }
    cat(density)
    cat("\n")
    return(density)
  } else {
    if (class(partitions) != "data.frame"  && class(partitions) != "matrix" 
      && length(partitions) < 2
    ) {
      stop("Partitions should be provided as one-column data.frame or matrix.")
    }
    partitions[is.na(partitions)] <- "NA"
    if (class(partitions) != "data.frame"  && class(partitions) != "matrix") {
      partitions <- matrix(partitions)
    }
    if (length(rownames(partitions)) == 0 && length(partitions) == 
        length(network.matrix[, 1])
    ) {
      rownames(partitions) <- rownames(network.matrix)
    }
    groups <- character(0)
    for (i in 1:length(partitions)) {
      if (! partitions[i] %in% groups) {
        groups <- append(groups, partitions[i])
      }
    }
    
    groupDensityTable <- matrix(0, nrow = length(groups), ncol = length(groups))
    groupFrequencyTable <- matrix(0, nrow = length(groups), 
        ncol = length(groups))
    
    for (i in 1:x) {
      for (j in 1:y) {
        mrn <- rownames(network.matrix)[i]
        mcn <- rownames(network.matrix)[j]
        for (k in 1:length(partitions)) {
          prn <- rownames(partitions)[k]
          if (prn == mrn) {
            rGroup <- partitions[k]
            rGroupRow <- k
          }
          if (prn == mcn) {
            cGroup <- partitions[k]
            cGroupRow <- k
          }
        }
        for (k in 1:length(groups)) {
          if (groups[k] == rGroup) {
            rowCounter <- k
          }
          if (groups[k] == cGroup) {
            colCounter <- k
          }
        }
        if (weighted == TRUE) {
          groupDensityTable[rowCounter, colCounter] <- 
            groupDensityTable[rowCounter, colCounter] + network.matrix[i, j]
        } else if (weighted == FALSE) {
          if (network.matrix[i, j] > 0) {
            value <- 1
          } else {
            value <- 0
          }
          groupDensityTable[rowCounter, colCounter] <- 
            groupDensityTable[rowCounter, colCounter] + value
        }
        groupFrequencyTable[rowCounter, colCounter] <- 
          groupFrequencyTable[rowCounter, colCounter] + 1
      }
    }
    
    density <- groupDensityTable / groupFrequencyTable
    rownames(density) <- groups
    colnames(density) <- groups
    
    if (verbose == TRUE) {
      cat("\nNumber of partitions: ")
      cat(length(groups))
      cat("\n")
      for (i in 1:length(groups)) {
        cat("group ")
        cat(i)
        cat(": ")
        cat(groups[i])
        cat("\n")
      }
      cat("\n\n")
      if (weighted == TRUE) {
        cat("Weighted ")
      } else {
        cat("Binary ")
      }
      cat("within- and between-block density:")
      cat("\n\n")
      print(density)
      cat("\n")
    }
    
    return(density)
  }
}


dna.timeseries <- function(infile, persons = FALSE, time.unit = "month", 
    ignore.duplicates = "article", separate.actors = TRUE, start.date = "first",
    stop.date = "last", include.persons = "all", include.organizations = "all", 
    include.categories = "all", invert.persons = FALSE, 
    invert.organizations = FALSE, invert.categories = FALSE, 
    agreement = "combined", verbose = TRUE) {
  if (time.unit != "month" && time.unit != "year" && time.unit != "total") {
    stop(parse("time.unit argument could not be parsed. Valid values ", 
      "are: \"month\", \"year\", \"total\"."), sep = "")
  }
  if (ignore.duplicates != "article" && ignore.duplicates != "month" 
    && ignore.duplicates != "off"
  ) {
    stop(paste("ignore.duplicates argument could not be parsed. Valid values ", 
      "are: \"article\", \"month\", \"off\"."), sep = "")
  }
  if (length(include.persons) == 1) {
    if (include.persons == "all") {
      include.persons <- c("all", "")
    } else if (class(include.persons) == "character") {
      include.persons <- c(include.persons, include.persons)
    } else {
      stop("Try include.persons = c(\"name 1\", \"name 2\").")
    }
  }
  if (length(include.organizations) == 1) {
    if (include.organizations == "all") {
      include.organizations <- c("all", "")
    } else if (class(include.organizations) == "character") {
      include.organizations <- c(include.organizations, include.organizations)
    } else {
      stop("Try include.organizations = c(\"name 1\", \"name 2\").")
    }
  }
  if (length(include.categories) == 1) {
    if (include.categories == "all") {
      include.categories <- c("all", "")
    } else if (class(include.categories) == "character") {
      include.categories <- c(include.categories, include.categories)
    } else {
      warning("Try include.categories=c(\"concept 1\", \"concept 2\").")
    }
  }
  if (agreement != "combined" && agreement != "yes" && agreement != "no") {
    stop(paste("agreement argument could not be parsed. Valid values ", 
      "are: \"yes\", \"no\", \"combined\"."), sep = "")
  }
  export <- .jnew("dna/TimeSeriesExporter", infile, persons, time.unit, 
    ignore.duplicates, separate.actors, start.date, stop.date, include.persons, 
    include.organizations, include.categories, invert.persons, 
    invert.organizations, invert.categories, agreement, verbose)
  
  #pull the Java network data into a list of vectors in R
  matObj <- .jcall(export, "[[I", "getMatrixObject")
  num.rows <- length(matObj) #calculate the number of rows of the sociomatrix
  num.cols <- length(matObj[[1]]) #calculate number of columns of the matrix
  #create a matrix with these dimensions
  mat <- matrix(nrow = num.rows, ncol = num.cols)
  for (i in 1:length(matObj)) {
    mat[i, ] <- .jevalArray(matObj[[i]]) #fill matrix with rows from the list
  }
  row.labels <- .jcall(export, "[S", "getRowLabels") #pull the row labels into R
  col.labels <- .jcall(export, "[S", "getColumnLabels") #pull the column labels
  rownames(mat) <- row.labels #assign the row labels to the matrix
  colnames(mat) <- col.labels #assign the column labels to the matrix
  
  clean <- .jcall(export, "V", "cleanUp")
  
  return(mat) #return the matrix
}

dna.categories <- function(infile, verbose = TRUE) {
  file <- .jnew("dna/Export", infile, verbose)
  categories <- .jcall(file, "[S", "getCategories")
  
  clean <- .jcall(file, "V", "cleanUp")
  
  return(categories)
}
