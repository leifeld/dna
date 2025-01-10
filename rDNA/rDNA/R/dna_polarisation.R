#' Calculate DNA Polarisation
#'
#' This function calculates the polarisation scores for a series of time windows.
#'
#' @inheritParams dna_network
#' @param statementType A character string specifying the type of DNA statement. Default is "DNA Statement".
#' @param k An integer specifying the number of clusters. Default is 2.
#' @param numParents An integer specifying the number of cluster solutions ("parents"). For example, 30 or 50. Default is 50.
#' @param iterations Number of iterations of the genetic algorithm. Often, 50 or 100 is enough, but since there is a built-in convergence check, it is recommended to keep this number large. Default is 1000.
#' @param elitePercentage A double specifying the percentage of the best solutions that are kept for the next generation. Useful values range between 0.05 and 0.2. Default is 0.1.
#' @param mutationPercentage A double specifying the percentage of the solutions that are mutated. Useful values range between 0.05 and 0.2. Default is 0.1.
#' @param qualityFunction A character string specifying the quality function to be used. Can be "modularity", "eiIndex", or "absdiff". Default is "absdiff".
#' @param normaliseMatrices A logical specifying whether the matrices should be normalised. Default is FALSE.
#' @param randomSeed An integer specifying the random seed for reproducibility of exact findings. Default is 0, which means the algorithm generates the random seed (= no reproducibility).
#'
#' @return An object representing the polarisation of actors and the results of the genetic algorithm for all time steps and iterations.
#'
#' @examples
#' \dontrun{
#' p <- dna_polarisation()
#' autoplot(p)
#' }
#'
#' @author Philip Leifeld
#'
#' @rdname dna_polarisation
#' @importFrom rJava .jarray
#' @importFrom rJava .jcall
#' @importFrom rJava .jevalArray
#' @importFrom rJava .jnull
#' @importFrom rJava .jlong
#'
#' @export
dna_polarisation <- function(statementType = "DNA Statement",
                             variable1 = "organization",
                             variable1Document = FALSE,
                             variable2 = "concept",
                             variable2Document = FALSE,
                             qualifier = "agreement",
                             duplicates = "include",
                             start.date = "01.01.1900",
                             stop.date = "31.12.2099",
                             timeWindow = "days",
                             windowSize = 100,
                             kernel = "uniform",
                             indentTime = FALSE,
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
                             k = 2,
                             numParents = 50,
                             iterations = 1000,
                             elitePercentage = 0.1,
                             mutationPercentage = 0.1,
                             qualityFunction = "absdiff",
                             normaliseMatrices = FALSE,
                             randomSeed = 0) {
  
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
    qualifier <- .jnull(class = "java/lang/String;")
  }
  
  # call rNetwork function to compute results
  polarisationObject <- .jcall(dna_getHeadlessDna(),
                               "Ldna/export/PolarisationResultTimeSeries;",
                               "rPolarisation",
                               statementType,
                               variable1,
                               variable1Document,
                               variable2,
                               variable2Document,
                               qualifier,
                               duplicates,
                               start.date,
                               stop.date,
                               timeWindow,
                               as.integer(windowSize),
                               kernel,
                               indentTime,
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
                               as.integer(k),
                               as.integer(numParents),
                               as.integer(iterations),
                               as.double(elitePercentage),
                               as.double(mutationPercentage),
                               qualityFunction,
                                normaliseMatrices,
                               .jlong(randomSeed)
  )
  
  l <- list()
  l$finalMaxQs <- .jcall(polarisationObject, "[D", "getFinalMaxQs")
  l$earlyConvergence <- .jcall(polarisationObject, "[Z", "getEarlyConvergence")
  l$maxQs <- lapply(.jcall(polarisationObject, "[[D", "getMaxQs"), .jevalArray)
  l$sdQs <- lapply(.jcall(polarisationObject, "[[D", "getSdQs"), .jevalArray)
  l$avgQs <- lapply(.jcall(polarisationObject, "[[D", "getAvgQs"), .jevalArray)
  dateTimes <- sapply(.jcall(polarisationObject, "[[I", "getDateTimeArray"), .jevalArray)
  l$startDates <- do.call("c", apply(dateTimes, 2, function(x) {
    return(ISOdatetime(x[1], x[2], x[3], x[4], x[5], x[6], tz = "UTC"))
  }, simplify = FALSE))
  l$middleDates <- do.call("c", apply(dateTimes, 2, function(x) {
    return(ISOdatetime(x[7], x[8], x[9], x[10], x[11], x[12], tz = "UTC"))
  }, simplify = FALSE))
  l$stopDates <- do.call("c", apply(dateTimes, 2, function(x) {
    return(ISOdatetime(x[13], x[14], x[15], x[16], x[17], x[18], tz = "UTC"))
  }, simplify = FALSE))
  l$memberships <- lapply(.jcall(polarisationObject, "[[I", "getMemberships"), .jevalArray)
  l$labels <- lapply(.jcall(polarisationObject, "[[Ljava/lang/String;", "getNames"), .jevalArray)
  class(l) <- "dna_polarisation"
  return(l)
}

#' Autoplot DNA Polarisation
#'
#' This function generates various plots for a DNA polarisation object.
#'
#' @param object An object of class `dna_polarisation`.
#' @param ... Additional arguments passed to the plotting functions.
#'   Currently not used.
#' @param plots A character vector specifying the types of plots to generate.
#'   Options are "hair", "hist", and "time_series". The hair plot
#'   shows the convergence of the maximal polarization over time.
#'   The histogram plot shows the distribution of the number of
#'   iterations until convergence. The time series plot shows the
#'   polarization over time.
#'
#' @return A list of ggplot objects corresponding to the specified plots.
#' @export
#'
#' @examples
#' \dontrun{
#' p <- dna_polarisation()
#' autoplot(p)
#' }
#'
#' @author Philip Leifeld
#'
#' @rdname dna_polarisation
#' @importFrom rlang .data
#' @importFrom ggplot2 ggplot
#' @importFrom ggplot2 geom_line
#' @importFrom ggplot2 geom_histogram
#' @importFrom ggplot2 labs
#' @importFrom ggplot2 theme_minimal
#' @importFrom ggplot2 scale_x_datetime
#' @importFrom ggplot2 theme
#' @importFrom ggplot2 element_text
#' 
#'
#' @export
autoplot.dna_polarisation <- function(object, ..., plots = c("hair", "hist", "time_series")) {
  l <- list()
  
  # hair diagnostic convergence plot
  hairData <- data.frame("Polarization" = do.call("c", object$maxQs),
                         "Iteration" = do.call("c", sapply(object$maxQs, function(x) 1:length(x))),
                         "Time" = rep(object$middleDates, times = sapply(object$maxQs, length)))
  gg_ha <- ggplot2::ggplot(hairData, ggplot2::aes(x = .data[["Iteration"]], y = .data[["Polarization"]], group = .data[["Time"]])) +
    ggplot2::geom_line(alpha = 0.3) +
    ggplot2::ylab("Maximal polarization") +
    ggplot2::theme_minimal()
  l$hair <- gg_ha
  
  # histogram diagnostic convergence plot
  histData <- data.frame("Iterations" = sapply(object$maxQs, length),
                         "Time" = object$middleDates)
  gg_hi <- ggplot2::ggplot(histData, ggplot2::aes(x = .data[["Iterations"]])) +
    ggplot2::geom_histogram() +
    ggplot2::labs(y = "Number of time windows", x = "Iterations until convergence") +
    ggplot2::theme_minimal()
  l$histogram <- gg_hi
  
  # time series plot
  timeSeriesData <- data.frame("Time" = object$middleDates, "Polarization" = object$finalMaxQs)
  gg_ts <- ggplot2::ggplot(timeSeriesData, ggplot2::aes(x = .data[["Time"]], y = .data[["Polarization"]])) +
    ggplot2::geom_line() +
    ggplot2::scale_x_datetime(date_breaks = "6 months", date_labels = "%b %Y") +
    ggplot2::labs(y = "Polarization") +
    ggplot2::theme_minimal() +
    ggplot2::theme(axis.text.x = ggplot2::element_text(angle = 45, vjust = 1, hjust=1))
  l$polarisation <- gg_ts
  
  return(l)
}