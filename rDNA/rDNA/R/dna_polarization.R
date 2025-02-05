#' Calculate DNA Polarization
#'
#' This function calculates the polarization scores for a single network or a
#' (kernel-smoothed) series of time windows.
#'
#' @inheritParams dna_network
#' @param timeWindow A character string specifying the time window. Can be
#'  \code{"minutes"}, \code{"hours"}, \code{"days"}, \code{"weeks"},
#'  \code{"months"}, or \code{"years"}, or \code{"no"} for no time window,
#'  in which case a single polarization score is calculated for the entire
#'  timeline.
#' @param windowSize An integer specifying the size of the time window. For
#'  example, if the time window is "days" and the window size is \code{22}, the
#'  first time window will be centered around the 12th day of the timeline and
#'  range from the 1st to the 22nd day. The second time window will be centered
#'  around the 13th day and range from the 2nd to the 23rd day, and so on. If
#'  the time window is \code{"no"}, the window size must be \code{0}.
#' @param indentTime If \code{TRUE}, the sequence of time slices under the time
#'   window algorithm starts with the first network and ends with the last
#'   network that are entirely covered within the timeline defined by the start
#'   and stop dates and times. For example, if the start date is 1 February, the
#'   stop date is 31 December, and the time window duration is 21 days, the
#'   mid-point of the first time window will be 11 February (to ensure the first
#'   network entirely fits into the timeline), and the last network will be
#'   centered around 20 December (to ensure the last network entirely fits into
#'   the timeline). If \code{FALSE}, the start and stop dates and times are used
#'   as the first and last mid-points. In that case, the first and last few
#'   networks may contain fewer statements than other time slices and may,
#'   therefore, be more similar to each other.
#' @param algorithm The algorithm to compute polarization. Can be "greedy" (for
#'   a greedy algorithm) or "genetic" (for a genetic algorithm).
#' @param normalizeScores A logical specifying whether the polarization scores
#'   should be normalized by edge mass per network to take away the effect of
#'   networks over time having different activity levels.
#' @param numClusters An integer specifying the number of clusters k. Default is
#'   \code{2}.
#' @param numParents Only for the genetic algorithm: An integer specifying the
#'   number of cluster solutions ("parents"). For example, \code{30} or
#'   \code{50}.
#' @param numIterations Only for the genetic algorithm: Number of iterations of
#'   the genetic algorithm. Often, \code{50} or \code{100} is enough, but since
#'   there is a built-in convergence check, it is recommended to keep this
#'   number large. The default is \code{1000}.
#' @param elitePercentage Only for the genetic algorithm: A double specifying
#'   the percentage of the best solutions that are kept for the next generation.
#'   Useful values range between 0.05 and 0.2.
#' @param mutationPercentage Only for the genetic algorithm: A double specifying
#'   the percentage of the solutions that are mutated. Useful values range
#'   between 0.05 and 0.2.
#' @param randomSeed Only for the genetic algorithm: An integer specifying the
#'   random seed for reproducibility of exact findings. The default is \code{0},
#'   which means the algorithm generates the random seed (= no reproducibility).
#'
#' @return An object representing the polarization of actors and the results of
#'   the algorithm for all time steps and iterations.
#'
#' @examples
#' \dontrun{
#' library("ggplot2")
#' dna_init()
#' dna_sample()
#' dna_openDatabase("sample.dna", coderId = 1, coderPassword = "sample")
#'
#' p <- dna_polarization(timeWindow = "days",
#'                       windowSize = 8,
#'                       kernel = "gaussian",
#'                       normalizeScores = FALSE)
#' str(p)
#' autoplot(p)
#'
#' p2 <- dna_polarization(timeWindow = "no",
#'                        normalizeScores = TRUE,
#'                        algorithm = "genetic")
#' p2$finalMaxQs # polarization between 0 and 1
#' }
#'
#' @author Philip Leifeld
#'
#' @rdname dna_polarization
#' @importFrom rJava .jarray
#' @importFrom rJava .jcall
#' @importFrom rJava .jevalArray
#' @importFrom rJava .jnull
#' @importFrom rJava .jlong
#'
#' @export
dna_polarization <- function(statementType = "DNA Statement",
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
                             algorithm = "greedy",
                             normalizeScores = FALSE,
                             numClusters = 2,
                             numParents = 50,
                             numIterations = 1000,
                             elitePercentage = 0.1,
                             mutationPercentage = 0.1,
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
    qualifier <- .jnull(class = "Ljava/lang/String;")
  }

  # call rNetwork function to compute results
  polarizationObject <- .jcall(dna_getHeadlessDna(),
                               "Ldna/export/PolarizationResultTimeSeries;",
                               "rPolarization",
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
                               FALSE,
                               FALSE,
                               FALSE,
                               FALSE,
                               FALSE,
                               algorithm,
                               normalizeScores,
                               as.integer(numClusters),
                               as.integer(numParents),
                               as.integer(numIterations),
                               as.double(elitePercentage),
                               as.double(mutationPercentage),
                               .jlong(randomSeed)
  )

  l <- list()
  l$finalMaxQs <- .jcall(polarizationObject, "[D", "getFinalMaxQs")
  l$earlyConvergence <- .jcall(polarizationObject, "[Z", "getEarlyConvergence")
  l$maxQs <- lapply(.jcall(polarizationObject, "[[D", "getMaxQs"), .jevalArray)
  l$sdQs <- lapply(.jcall(polarizationObject, "[[D", "getSdQs"), .jevalArray)
  l$avgQs <- lapply(.jcall(polarizationObject, "[[D", "getAvgQs"), .jevalArray)
  dateTimes <- sapply(.jcall(polarizationObject, "[[I", "getDateTimeArray"), .jevalArray)
  l$startDates <- do.call("c", apply(dateTimes, 2, function(x) {
    return(ISOdatetime(x[1], x[2], x[3], x[4], x[5], x[6], tz = "UTC"))
  }, simplify = FALSE))
  l$middleDates <- do.call("c", apply(dateTimes, 2, function(x) {
    return(ISOdatetime(x[7], x[8], x[9], x[10], x[11], x[12], tz = "UTC"))
  }, simplify = FALSE))
  l$stopDates <- do.call("c", apply(dateTimes, 2, function(x) {
    return(ISOdatetime(x[13], x[14], x[15], x[16], x[17], x[18], tz = "UTC"))
  }, simplify = FALSE))
  l$memberships <- lapply(.jcall(polarizationObject, "[[I", "getMemberships"), .jevalArray)
  l$labels <- lapply(.jcall(polarizationObject, "[[Ljava/lang/String;", "getNames"), .jevalArray)
  class(l) <- "dna_polarization"
  return(l)
}

#' Autoplot DNA Polarization
#'
#' This function generates various plots for a DNA polarization object.
#'
#' @param object An object of class `dna_polarization`.
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
#'
#' @author Philip Leifeld
#'
#' @rdname dna_polarization
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
#' @export
autoplot.dna_polarization <- function(object, ..., plots = c("hair", "hist", "time_series")) {
  l <- list()

  # hair diagnostic convergence plot
  if ("hair" %in% plots) {
    hairData <- data.frame("Polarization" = do.call("c", object$maxQs),
                           "Iteration" = do.call("c", lapply(object$maxQs, function(x) 1:length(x))),
                           "Time" = rep(object$middleDates, times = sapply(object$maxQs, length)))
    gg_ha <- ggplot2::ggplot(hairData, ggplot2::aes(x = .data[["Iteration"]], y = .data[["Polarization"]], group = .data[["Time"]])) +
      ggplot2::geom_line(alpha = 0.3) +
      ggplot2::ylab("Maximal polarization") +
      ggplot2::theme_minimal()
      l$hair <- gg_ha
  }

  # histogram diagnostic convergence plot
  if ("hist" %in% plots && length(object$finalMaxQs) > 1) {
    histData <- data.frame("Iterations" = sapply(object$maxQs, length),
                           "Time" = object$middleDates)
    gg_hi <- ggplot2::ggplot(histData, ggplot2::aes(x = .data[["Iterations"]])) +
      ggplot2::geom_histogram() +
      ggplot2::labs(y = "Number of time windows", x = "Iterations until convergence") +
      ggplot2::theme_minimal()
    l$histogram <- gg_hi
  }

  # time series plot
  if ("time_series" %in% plots) {
    timeSeriesData <- data.frame("Time" = object$middleDates, "Polarization" = object$finalMaxQs)
    if (length(object$finalMaxQs) == 1) { # plot point instead of line if no time window
      gg_ts <- ggplot2::ggplot(timeSeriesData, ggplot2::aes(x = .data[["Time"]], y = .data[["Polarization"]])) +
          ggplot2::geom_point() +
          ggplot2::scale_x_datetime(date_breaks = "6 months", date_labels = "%b %Y") +
          ggplot2::labs(y = "Polarization") +
          ggplot2::theme_minimal() +
          ggplot2::theme(axis.text.x = ggplot2::element_text(angle = 45, vjust = 1, hjust = 1))
    } else {
      gg_ts <- ggplot2::ggplot(timeSeriesData, ggplot2::aes(x = .data[["Time"]], y = .data[["Polarization"]])) +
          ggplot2::geom_line() +
          ggplot2::scale_x_datetime(date_breaks = "6 months", date_labels = "%b %Y") +
          ggplot2::labs(y = "Polarization") +
          ggplot2::theme_minimal() +
          ggplot2::theme(axis.text.x = ggplot2::element_text(angle = 45, vjust = 1, hjust = 1))
    }
    l$polarization <- gg_ts
  }

  return(l)
}