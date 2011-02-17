check <- require(rJava)

dna.init <- function(dna.jar.file) {
  .jinit(dna.jar.file, force.init=TRUE)
}

dna.gui <- function() {
  .jnew("dna/Dna")
}

dna.network <- function(infile, algorithm="cooccurrence", agreement="combined", start.date="01.01.1900", stop.date="31.12.2099", two.mode.type="oc", one.mode.type="organizations", via="categories", ignore.duplicates=TRUE, include.isolates=FALSE, normalization=FALSE, window.size=100, step.size=1, exclude.persons=c(""), exclude.organizations=c(""), exclude.categories=c("")) {
  check <- require(rJava)
  if (check == FALSE) {
    warning("DNA depends on the package rJava. Please install rJava before using DNA.")
  } else if (algorithm == "sonia") {
    warning("SoNIA and rSoNIA are not supported. Please use the GUI of DNA instead.")
  } else if (algorithm == "dynamic") {
    warning("Dynamic algorithms like SoNIA and Commetrix are not supported. Please use the GUI of DNA instead.")
  } else if (algorithm == "commetrix") {
    warning("Commetrix is not supported in the R version of DNA. Please use the GUI of DNA instead.")
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

    export <- .jnew("dna/Export", infile, exclude.persons, exclude.organizations, exclude.categories, start.date, stop.date, agreement, algorithm, two.mode.type, one.mode.type, via, include.isolates, ignore.duplicates, normalization, window.size, step.size, 100, 100, "DNA_CMX", 100.0) #initialize the export class and parse the .dna file
  
    matObj <- .jcall(export, "[[D", "matrixObject") #pull the Java network data into a list of vectors in R
    num.rows <- length(matObj) #calculate the number of rows of the sociomatrix
    num.cols <- length(matObj[[1]]) #calculate the number of columns of the sociomatrix
    mat <- matrix(nrow=num.rows, ncol=num.cols) #create a matrix with these dimensions
    for (i in 1:length(matObj)) {
      mat[i,] <- .jevalArray(matObj[[i]]) #fill the matrix with the rows from the list
    }
    row.labels <- .jcall(export, "[S", "getMatrixLabels", TRUE) #pull the row labels into R
    col.labels <- .jcall(export, "[S", "getMatrixLabels", FALSE) #pull the column labels into R
    rownames(mat) <- row.labels #assign the row labels to the matrix
    colnames(mat) <- col.labels #assign the column labels to the matrix
    return(mat) #return the matrix
    }
}

dna.attributes <- function(infile, organizations=TRUE) {
  file <- .jnew("dna/Export", infile)
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
  return(data)
}

