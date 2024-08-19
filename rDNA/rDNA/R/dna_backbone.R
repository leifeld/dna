#' Compute and retrieve the backbone and redundant set
#'
#' Compute and retrieve the backbone and redundant set of a discourse network.
#'
#' The dna_backbone function applies a simulated annealing algorithm to the
#' discourse network to partition the set of second-mode entities (e.g.,
#' concepts) into a backbone set and a complementary redundant set. Three
#' methods are available:
#' \itemize{
#'   \item A simulated annealing algorithm with a penalty. You can play with
#'     different penalties and see how they affect the size of your backbone
#'     set.
#'   \item A modified simulated annealing algorithm for a fixed number of
#'     backbone entities to retain. This is computationally simpler, but you
#'     have to know how large the set should be.
#'   \item A fast and greedy nested algorithm, which evaluates all possible
#'     fixed backbone solutions, i.e., for all sizes, and provides a nested
#'     hierarchy of entities on the second mode. This algorithm may stay below
#'     the optimum and is only an approximation but provides insights into the
#'     hierarchy of concepts and their relative importance.
#' }
#'
#' The \code{autoplot} function requires the ggplot2 package and can plot
#' algorithm diagnostics and the hierarchy of entities as a dendrogram,
#' depending on the method that was chosen. The \code{plot} function can do the
#' same thing, just using base plots, not ggplot2.
#'
#' The \code{dna_evaluateBackboneSolution} function computes the spectral loss
#' for an arbitrary backbone and its complement, the redundant set, specified by
#' the user. For example, the user can evaluate how much structure would be lost
#' if the second mode was composed only of the concepts provided to this
#' function. This can be used to compare how useful different codebook models
#' are. The penalty parameter \code{p} applies a penalty factor to the spectral
#' loss. The default value of \code{0} switches off the penalty as it is usually
#' not needed to evaluate a specific solution. The backbone set can be supplied
#' as a vector of character objects, for example concepts.
#'
#' @param method The backbone algorithm used to compute the results. Several
#'  methods are available:
#'  \itemize{
#'    \item \code{"nested"}: A relatively fast, deterministic algorithm that
#'      produces the full hierarchy of entities. It starts with a complete
#'      backbone set resembling the full network. There are as many iterations
#'      as entities on the second mode. In each iteration, the entity whose
#'      removal would yield the smallest backbone loss is moved from the
#'      backbone set into the redundant set, and the (unpenalized) spectral
#'      loss is recorded. This creates a solution for all backbone sizes, where
#'      each backbone set is fully nested in the next larger backbone set. The
#'      solution usually resembles an unconstrained solution where nesting is
#'      not required, but in some cases the loss of a non-nested solution may be
#'      larger at a given level or number of elements in the backbone set.
#'    \item \code{"fixed"}: Simulated annealing with a fixed number of elements
#'      in the backbone set (i.e., only lateral changes are possible) and
#'      without penalty. This method may yield more optimal solutions than the
#'      nested algorithm because it does not require a strict hierarchy.
#'      However, it produces an approximation of the global optimum and is
#'      slower than the nested method. With this method, you can specify that
#'      backbone set should have, for example, exactly 10 concepts. Then fewer
#'      iterations are necessary than with the penalty method because the search
#'      space is smaller. The backbone set size is defined in the
#'      \code{"backboneSize"} argument.
#'    \item \code{"penalty"}: Simulated annealing with a variable number of
#'      elements in the backbone set. The solution is stabilized by a penalty
#'      parameter (see \code{"penalty"} argument). This algorithm takes longest
#'      to compute for a single solution, and it is only an approximation, but
#'      it considers slightly larger or smaller backbone sets if the solution is
#'      better, thus this algorithm adds some flexibility. It requires more
#'      iterations than the fixed method for achieving the same quality.
#'  }
#' @param backboneSize The number of elements in the backbone set, as a fixed
#'   parameter. Only used when \code{method = "fixed"}.
#' @param penalty The penalty parameter for large backbone sets. The larger the
#'   value, the more strongly larger backbone sets are punished and the smaller
#'   the resulting backbone is. Try out different values to find the right size
#'   of the backbone set. Reasonable values could be \code{2.5}, \code{5},
#'   \code{7.5}, or \code{12}, for example. The minimum is \code{0.0}, which
#'   imposes no penalty on the size of the backbone set and produces a redundant
#'   set with only one element. Start with \code{0.0} if you want to weed out a
#'   single concept and subsequently increase the penalty to include more items
#'   in the redundant set and shrink the backbone further. Only used when
#'   \code{method = "penalty"}.
#' @param iterations The number of iterations of the simulated annealing
#'   algorithm. More iterations take more time but may lead to better
#'   optimization results. Only used when \code{method = "penalty"} or
#'   \code{method = "fixed"}.
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
#' # compute backbone and redundant set using penalised spectral loss
#' b <- dna_backbone(method = "penalty",
#'                   penalty = 3.5,
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
#'
#' # compute backbone with fixed size (here: 4 concepts)
#' b <- dna_backbone(method = "fixed",
#'                   backboneSize = 4,
#'                   iterations = 2000,
#'                   variable1 = "organization",
#'                   variable2 = "concept",
#'                   qualifier = "agreement",
#'                   qualifierAggregation = "subtract",
#'                   normalization = "average")
#' b
#'
#' # compute backbone with a nested structure and plot dendrogram
#' b <- dna_backbone(method = "nested",
#'                   variable1 = "organization",
#'                   variable2 = "concept",
#'                   qualifier = "agreement",
#'                   qualifierAggregation = "subtract",
#'                   normalization = "average")
#' b
#' plot(b)
#' autoplot(b)
#' }
#'
#' @author Philip Leifeld, Tim Henrichsen
#'
#' @rdname dna_backbone
#' @importFrom rJava .jarray
#' @importFrom rJava .jcall
#' @importFrom rJava .jnull
#' @importFrom rJava J
#' @export
dna_backbone <- function(method = "nested",
                         backboneSize = 1,
                         penalty = 3.5,
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
         method,
         as.integer(backboneSize),
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
  if (!is.null(outfile) && !is.null(fileFormat) && is.character(outfile) && is.character(fileFormat) && fileFormat %in% c("json", "xml")) {
    message("File exported.")
  } else if (method[1] %in% c("penalty", "fixed")) {
    result <- .jcall(exporter, "Lexport/SimulatedAnnealingBackboneResult;", "getSimulatedAnnealingBackboneResult", simplify = TRUE)
    # create a list with various results
    l <- list()
    l$penalty <- .jcall(result, "D", "getPenalty")
    if (method[1] == "fixed") {
      l$backbone_size <- as.integer(backboneSize)
    } else {
      l$backbone_size <- as.integer(NA)
    }
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
    attributes(l)$method <- method[1]
    class(l$full_network) <- c("dna_network_onemode", class(l$full_network))
    class(l$backbone_network) <- c("dna_network_onemode", class(l$backbone_network))
    class(l$redundant_network) <- c("dna_network_onemode", class(l$redundant_network))
    class(l) <- c("dna_backbone", class(l))
    return(l)
  } else if (method[1] == "nested") {
    result <- .jcall(exporter, "Lexport/NestedBackboneResult;", "getNestedBackboneResult", simplify = TRUE)
    d <- data.frame(i = .jcall(result, "[I", "getIteration"),
                    entity = .jcall(result, "[S", "getEntities"),
                    backboneLoss = .jcall(result, "[D", "getBackboneLoss"),
                    redundantLoss = .jcall(result, "[D", "getRedundantLoss"),
                    statements = .jcall(result, "[I", "getNumStatements"))
    rownames(d) <- NULL
    attributes(d)$numStatementsFull <- .jcall(result, "I", "getNumStatementsFull")
    attributes(d)$start <- as.POSIXct(.jcall(result, "J", "getStart"), origin = "1970-01-01") # add the start date/time of the result as an attribute
    attributes(d)$stop <- as.POSIXct(.jcall(result, "J", "getStop"), origin = "1970-01-01") # add the end date/time of the result as an attribute
    attributes(d)$method <- "nested"
    class(d) <- c("dna_backbone", class(d))
    return(d)
  }
}

#' @rdname dna_backbone
#' @param x A \code{"dna_backbone"} object.
#' @param trim Number of maximum characters to display in entity labels. Labels
#'   with more characters are truncated, and the last character is replaced by
#'   an asterisk (\code{*}).
#' @export
print.dna_backbone <- function(x, trim = 50, ...) {
  method <- attributes(x)$method
  cat(paste0("Backbone method: ", method, ".\n\n"))
  if (method %in% c("penalty", "fixed")) {
    if (method == "penalty") {
      cat(paste0("Penalty: ", x$penalty, ". Iterations: ", x$iterations, ".\n\n"))
    } else {
      cat(paste0("Backbone size: ", x$backbone_size, ". Iterations: ", x$iterations, ".\n\n"))
    }
    cat(paste0("Backbone set (loss: ", round(x$unpenalized_backbone_loss, 4), "):\n"))
    cat(paste(1:length(x$backbone), x$backbone), sep = "\n")
    cat(paste0("\nRedundant set (loss: ", round(x$unpenalized_redundant_loss, 4), "):\n"))
    cat(paste(1:length(x$redundant), x$redundant), sep = "\n")
  } else if (method == "nested") {
    x2 <- x
    x2$entity <- sapply(x2$entity, function(r) if (nchar(r) > trim) paste0(substr(r, 1, trim - 1), "*") else r)
    print(as.data.frame(x2), row.names = FALSE)
  }
}

#' @param ma Number of iterations to compute moving average.
#' @rdname dna_backbone
#' @importFrom graphics lines
#' @importFrom stats filter
#' @importFrom rlang .data
#' @export
plot.dna_backbone <- function(x, ma = 500, ...) {
  
  if (attr(x, "method") != "nested") {
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
    if (attributes(x)$method == "penalty") {
      yl <- "Penalized backbone loss"
      ti <- "Penalized spectral backbone distance"
    } else {
      yl <- "Backbone loss"
      ti <- "Spectral backbone distance"
    }
    plot(x = x$diagnostics$iteration,
         y = bb_loss,
         type = "l",
         xlab = "Iteration",
         ylab = yl,
         main = ti)
    
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
  } else { # create hclust object
    # define merging pattern: negative numbers are leaves, positive are merged clusters
    merges_clust <- matrix(nrow = nrow(x) - 1, ncol = 2)
    
    merges_clust[1,1] <- -nrow(x)
    merges_clust[1,2] <- -(nrow(x) - 1)
    
    for (i in 2:(nrow(x) - 1)) {
      merges_clust[i, 1] <- -(nrow(x) - i)
      merges_clust[i, 2] <- i - 1
    }
    
    # Initialize empty object
    a <- list()
    
    # Add merges
    a$merge <- merges_clust
    
    # Define merge heights
    a$height <- x$backboneLoss[1:nrow(x) - 1]
    
    # Order of leaves
    a$order <- 1:nrow(x)
    
    # Labels of leaves
    a$labels <- rev(x$entity)
    
    # Define hclust class
    class(a) <- "hclust"
    
    plot(a, ylab = "")
  }
}

#' @rdname dna_backbone
#' @param object A \code{"dna_backbone"} object.
#' @param ... Additional arguments.
#' @importFrom ggplot2 autoplot
#' @importFrom ggplot2 ggplot
#' @importFrom ggplot2 aes
#' @importFrom ggplot2 geom_line
#' @importFrom ggplot2 ylab
#' @importFrom ggplot2 xlab
#' @importFrom ggplot2 ggtitle
#' @importFrom ggplot2 theme_bw
#' @importFrom ggplot2 theme
#' @importFrom ggplot2 coord_flip
#' @importFrom ggplot2 scale_x_continuous
#' @importFrom ggplot2 scale_y_continuous
#' @importFrom rlang .data
#' @export
autoplot.dna_backbone <- function(object, ..., ma = 500) {
  if (attr(object, "method") != "nested") {
    bd <- object$diagnostics
    bd$bb_loss <- stats::filter(bd$penalized_backbone_loss, rep(1 / ma, ma), sides = 1)
    bd$current_size_ma <- stats::filter(bd$current_backbone_size, rep(1 / ma, ma), sides = 1)
    bd$optimal_size_ma <- stats::filter(bd$optimal_backbone_size, rep(1 / ma, ma), sides = 1)
    bd$accept_ratio <- stats::filter(bd$acceptance, rep(1 / ma, ma), sides = 1)
    
    # temperature and acceptance probability
    g_accept <- ggplot2::ggplot(bd, ggplot2::aes(y = .data[["temperature"]], x = .data[["iteration"]])) +
      ggplot2::geom_line(color = "#a50f15") +
      ggplot2::geom_line(data = bd[bd$acceptance_prob >= 0, ],
                         ggplot2::aes(y = .data[["acceptance_prob"]], x = .data[["iteration"]])) +
      ggplot2::ylab("Acceptance probability") +
      ggplot2::xlab("Iteration") +
      ggplot2::ggtitle("Temperature and acceptance probability") +
      ggplot2::theme_bw()
    
    # spectral distance between full network and backbone network per iteration
    if (attributes(object)$method == "penalty") {
      yl <- "Penalized backbone loss"
      ti <- "Penalized spectral backbone distance"
    } else {
      yl <- "Backbone loss"
      ti <- "Spectral backbone distance"
    }
    g_loss <- ggplot2::ggplot(bd, ggplot2::aes(y = .data[["bb_loss"]], x = .data[["iteration"]])) +
      ggplot2::geom_line() +
      ggplot2::ylab(yl) +
      ggplot2::xlab("Iteration") +
      ggplot2::ggtitle(ti) +
      ggplot2::theme_bw()
    
    # number of concepts in the backbone solution per iteration
    d <- data.frame(iteration = rep(bd$iteration, 2),
                    size = c(bd$current_size_ma, bd$optimal_size_ma),
                    Criterion = c(rep("Current iteration", nrow(bd)),
                                  rep("Best solution", nrow(bd))))
    g_size <- ggplot2::ggplot(d, ggplot2::aes(y = .data[["size"]], x = .data[["iteration"]], color = .data[["Criterion"]])) +
      ggplot2::geom_line() +
      ggplot2::ylab(paste0("Number of elements (MA, last ", ma, ")")) +
      ggplot2::xlab("Iteration") +
      ggplot2::ggtitle("Backbone size") +
      ggplot2::theme_bw() +
      ggplot2::theme(legend.position = "bottom")
    
    # ratio of recent acceptances
    g_ar <- ggplot2::ggplot(bd, ggplot2::aes(y = .data[["accept_ratio"]], x = .data[["iteration"]])) +
      ggplot2::geom_line() +
      ggplot2::ylab(paste("Acceptance ratio in the last", ma, "iterations")) +
      ggplot2::xlab("Iteration") +
      ggplot2::ggtitle("Acceptance ratio") +
      ggplot2::theme_bw()
    
    # wrap in list
    plots <- list(g_accept, g_loss, g_size, g_ar)
    return(plots)
  } else { # create hclust object
    # define merging pattern: negative numbers are leaves, positive are merged clusters
    merges_clust <- matrix(nrow = nrow(object) - 1, ncol = 2)
    
    merges_clust[1,1] <- -nrow(object)
    merges_clust[1,2] <- -(nrow(object) - 1)
    
    for (i in 2:(nrow(object) - 1)) {
      merges_clust[i, 1] <- -(nrow(object) - i)
      merges_clust[i, 2] <- i - 1
    }
    
    # Initialize empty object
    a <- list()
    
    # Add merges
    a$merge <- merges_clust
    
    # Define merge heights
    a$height <- object$backboneLoss[1:nrow(object) - 1]
    height <- a$height
    
    # Order of leaves
    a$order <- 1:nrow(object)
    
    # Labels of leaves
    a$labels <- rev(object$entity)
    
    # Define hclust class
    class(a) <- "hclust"
    
    # ensure ggraph is installed, otherwise throw error (better than importing it to avoid hard dependency)
    if (!requireNamespace("ggraph", quietly = TRUE)) {
      stop("The 'ggraph' package is required for plotting nested backbone dendrograms with 'ggplot2' but was not found. Consider installing it.")
    }
    
    g_clust <- ggraph::ggraph(graph = a,
                              layout = "dendrogram",
                              circular = FALSE,
                              height = height) + # TODO @Tim: "height" does not seem to exist
      ggraph::geom_edge_elbow() +
      ggraph::geom_node_point(aes(filter = .data[["leaf"]])) +
      ggplot2::theme_bw() +
      ggplot2::theme(panel.border = element_blank(),
                     axis.title = element_blank(),
                     panel.grid.major = element_blank(),
                     panel.grid.minor = element_blank(),
                     axis.line = element_blank(),
                     axis.text.y = element_text(size = 6),
                     axis.ticks.y = element_blank()) +
      ggplot2::scale_x_continuous(breaks = seq(0, nrow(object) - 1, by = 1),
                                  labels = rev(object$entity)) +
      ggplot2::scale_y_continuous(expand = c(0, 0.01)) +
      ggplot2::coord_flip()
    
    return(g_clust)
  }
}

#' @param backboneEntities A vector of character values to be included in the
#'   backbone. The function will compute the spectral loss between the full
#'   network and the network composed only of those entities on the second mode
#'   that are contained in this vector.
#' @param p The penalty parameter. The default value of \code{0} means no
#'   penalty for backbone size is applied.
#' @inheritParams dna_backbone
#' @return A vector with two numeric values: the backbone and redundant loss.
#'
#' @examples
#' \dontrun{
#' dna_init()
#' dna_sample()
#' dna_openDatabase("sample.dna", coderId = 1, coderPassword = "sample")
#'
#' dna_evaluateBackboneSolution(
#'   c("There should be legislation to regulate emissions.",
#'     "Emissions legislation should regulate CO2.")
#' )
#' }
#'
#' @rdname dna_backbone
#' @importFrom rJava .jarray
#' @importFrom rJava .jcall
#' @importFrom rJava .jnull
#' @export
dna_evaluateBackboneSolution <- function(backboneEntities,
                                         p = 0,
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
  
  # call rBackbone function to compute results
  result <- .jcall(dnaEnvironment[["dna"]]$headlessDna,
                   "[D",
                   "rEvaluateBackboneSolution",
                   .jarray(backboneEntities),
                   as.integer(p),
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
  names(result) <- c("backbone loss", "redundant loss")
  return(result)
}