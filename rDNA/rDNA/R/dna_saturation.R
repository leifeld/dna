# TODO: write roxygen2 documentation and continue writing the function
# code to test this: results <- dna_saturation(c(1, 2, 3, 4, 5, 7, 8, 9, 11, 13, 14, 15, 17, 18, 19, 20, 21, 24, 27, 28), numSamples = 20, duplicates = "include")
dna_saturation <- function(codedDocumentIds,
                           numSamples = 1,
                           maxNumDocuments = length(codedDocumentIds),
                           networkType = "twomode",
                           statementType = "DNA Statement",
                           variable1 = "organization",
                           variable1Document = FALSE,
                           variable2 = "concept",
                           variable2Document = FALSE,
                           qualifier = "agreement",
                           qualifierDocument = FALSE,
                           qualifierAggregation = "ignore",
                           normalization = "no",
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

  # call rSaturation function to compute results
  results <- .jcall(dnaEnvironment[["dna"]]$headlessDna,
                    "[[D",
                    "rSaturation",
                    .jarray(as.integer(codedDocumentIds)),
                    as.integer(numSamples),
                    as.integer(maxNumDocuments),
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
                    invertTypes
  )

  results <- as.data.frame(sapply(results, .jevalArray))

  ### TODO: This code fits a non-linear model through the first sample curve, but a better non-linear model is needed.
  ###       This page contains some ideas: https://www.statforbiology.com/nonlinearregression/usefulequations
  ###       It may be necessary to change it from cumulative Euclidean changes to Euclidean changes in Java to be able to fit this well
  # dta <- data.frame(documents = 1:nrow(results), cumulative_change = results[, 1])
  # add_nls <- nls(cumulative_change ~ log(r * documents), data = dta, start = list(r = 1))
  # summary(add_nls)
  # plot(dta$documents, dta$cumulative_change, type = "l")
  # lines(dta$documents, log(coef(add_nls)[1] * dta$documents))

  ### TODO: This code plots the curves for each sample
  # test <- reshape(results, direction = "long", varying = 1:ncol(results), v.names = "change")
  # colnames(test) <- c("Sample", "Change", "Documents")
  # rownames(test) <- NULL
  # test$Sample <- as.factor(test$Sample)
  # library("ggplot2")
  # ggplot(test, aes(x = Documents, y = Change, colour = Sample)) + geom_line()

  return(results)
}
