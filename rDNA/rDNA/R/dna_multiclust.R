#' Compute multiple cluster solutions for a discourse network
#'
#' Compute multiple cluster solutions for a discourse network.
#'
#' This function applies a number of different graph clustering techniques to
#' a discourse network dataset. The user provides many of the same arguments as
#' in the \code{\link{dna_network}} function and a few additional arguments that
#' determine which kinds of clustering methods should be used and how. In
#' particular, the \code{k} argument can be \code{0} (for arbitrary numbers of
#' clusters) or any positive integer value (e.g., \code{2}, for constraining the
#' number of clusters to exactly \code{k} groups). This is useful for assessing
#' the polarization of a discourse network.
#'
#' In particular, the function can be used to compute the maximal modularity of
#' a smoothed time series of discourse networks using the \code{timeWindow} and
#' \code{windowSize} arguments for a given \code{k} across a number of
#' clustering methods.
#'
#' It is also possible to switch off all but one clustering method using the
#' respective arguments and carry out a simple cluster analysis with the method
#' of choice for a certain time span of the discourse network, without any time
#' window options.
#'
#' @param saveObjects Store the original output of the respective clustering
#'   method in the \code{cl} slot of the return object? If \code{TRUE}, one
#'   cluster object per time point will be saved, for all time points for which
#'   network data are available. At each time point, only the cluster object
#'   with the highest modularity score will be saved, all others discarded. The
#'   \code{max_mod} slot of the object contains additional information on which
#'   measure was saved at each time point and what the corresponding modularity
#'   score is.
#' @param k The number of clusters to compute. This constrains the choice of
#'   clustering methods because some methods require a predefined \code{k} while
#'   other methods do not. To permit arbitrary numbers of clusters, depending on
#'   the respective algorithm (or the value of modularity in some cases), choose
#'   \code{k = 0}. This corresponds to the theoretical notion of
#'   "multipolarization". For "bipolarization", choose \code{k = 2} in order to
#'   constrain the cluster solutions to exactly two groups.
#' @param k.max If \code{k = 0}, there can be arbitrary numbers of clusters. In
#'   this case, \code{k.max} sets the maximal number of clusters that can be
#'   identified.
#' @param single Include hierarchical clustering with single linkage in the pool
#'   of clustering methods? The \code{\link[stats]{hclust}} function from
#'   the \pkg{stats} package is applied to Jaccard distances in the affiliation
#'   network for this purpose. Only valid if \code{k > 1}.
#' @param average Include hierarchical clustering with average linkage in the
#'   pool of clustering methods? The \code{\link[stats]{hclust}} function from
#'   the \pkg{stats} package is applied to Jaccard distances in the affiliation
#'   network for this purpose. Only valid if \code{k > 1}.
#' @param complete Include hierarchical clustering with complete linkage in the
#'   pool of clustering methods? The \code{\link[stats]{hclust}} function from
#'   the \pkg{stats} package is applied to Jaccard distances in the affiliation
#'   network for this purpose. Only valid if \code{k > 1}.
#' @param ward Include hierarchical clustering with Ward's algorithm in the
#'   pool of clustering methods? The \code{\link[stats]{hclust}} function from
#'   the \pkg{stats} package is applied to Jaccard distances in the affiliation
#'   network for this purpose. If \code{k = 0} is selected, different solutions
#'   with varying \code{k} are attempted, and the solution with the highest
#'   modularity is retained.
#' @param kmeans Include k-means clustering in the pool of clustering methods?
#'   The \code{\link[stats]{kmeans}} function from the \pkg{stats} package is
#'   applied to Jaccard distances in the affiliation network for this purpose.
#'   If \code{k = 0} is selected, different solutions with varying \code{k} are
#'   attempted, and the solution with the highest modularity is retained.
#' @param pam Include partitioning around medoids in the pool of clustering
#'   methods? The \code{\link[cluster]{pam}} function from the \pkg{cluster}
#'   package is applied to Jaccard distances in the affiliation network for this
#'   purpose. If \code{k = 0} is selected, different solutions with varying
#'   \code{k} are attempted, and the solution with the highest modularity is
#'   retained.
#' @param equivalence Include equivalence clustering (as implemented in the
#'   \code{\link[sna]{equiv.clust}} function in the \pkg{sna} package), based on
#'   shortest path distances between nodes (as implemented in the
#'   \code{\link[sna]{sedist}} function in the \pkg{sna} package) in the
#'   positive subtract network? If \code{k = 0} is selected, different solutions
#'   with varying \code{k} are attempted, and the solution with the highest
#'   modularity is retained.
#' @param concor_one Include CONvergence of iterative CORrelations (CONCOR) in
#'   the pool of clustering methods? The algorithm is applied to the positive
#'   subtract network to identify \code{k = 2} clusters. The method is omitted
#'   if \code{k != 2}.
#' @param concor_two Include CONvergence of iterative CORrelations (CONCOR) in
#'   the pool of clustering methods? The algorithm is applied to the affiliation
#'   network to identify \code{k = 2} clusters. The method is omitted
#'   if \code{k != 2}.
#' @param louvain Include the Louvain community detection algorithm in the pool
#'   of clustering methods? The \code{\link[igraph]{cluster_louvain}} function
#'   in the \pkg{igraph} package is applied to the positive subtract network for
#'   this purpose.
#' @param fastgreedy Include the fast and greedy community detection algorithm
#'   in the pool of clustering methods? The
#'   \code{\link[igraph]{cluster_fast_greedy}} function in the \pkg{igraph}
#'   package is applied to the positive subtract network for this purpose.
#' @param walktrap Include the Walktrap community detection algorithm
#'   in the pool of clustering methods? The
#'   \code{\link[igraph]{cluster_walktrap}} function in the \pkg{igraph}
#'   package is applied to the positive subtract network for this purpose.
#' @param leading_eigen Include the leading eigenvector community detection
#'   algorithm in the pool of clustering methods? The
#'   \code{\link[igraph]{cluster_leading_eigen}} function in the \pkg{igraph}
#'   package is applied to the positive subtract network for this purpose.
#' @param edge_betweenness Include the edge betweenness community detection
#'   algorithm by Girvan and Newman in the pool of clustering methods? The
#'   \code{\link[igraph]{cluster_edge_betweenness}} function in the \pkg{igraph}
#'   package is applied to the positive subtract network for this purpose.
#' @param infomap Include the infomap community detection algorithm
#'   in the pool of clustering methods? The
#'   \code{\link[igraph]{cluster_infomap}} function in the \pkg{igraph}
#'   package is applied to the positive subtract network for this purpose.
#' @param label_prop Include the label propagation community detection algorithm
#'   in the pool of clustering methods? The
#'   \code{\link[igraph]{cluster_label_prop}} function in the \pkg{igraph}
#'   package is applied to the positive subtract network for this purpose.
#' @param spinglass Include the spinglass community detection algorithm
#'   in the pool of clustering methods? The
#'   \code{\link[igraph]{cluster_spinglass}} function in the \pkg{igraph}
#'   package is applied to the positive subtract network for this purpose. Note
#'   that this method is disabled by default because it is relatively slow.
#' @inheritParams dna_network
#'
#' @return The function creates a \code{dna_multiclust} object, which contains
#'   the following items:
#' \describe{
#'   \item{k}{The number of clusters determined by the user.}
#'   \item{cl}{Cluster objects returned by the respective cluster function. If
#'     multiple methods are used, this returns the object with the highest
#'     modularity.}
#'   \item{max_mod}{A data frame with one row per time point (that is, only one
#'     row in the default case and multiple rows if time windows are used) and
#'     the maximal modularity for the given time point across all cluster
#'     methods.}
#'   \item{modularity}{A data frame with the modularity values for all separate
#'     cluster methods and all time points.}
#'   \item{membership}{A large data frame with all nodes' membership information
#'     for each time point and each clustering method.}
#' }
#'
#' @author Philip Leifeld
#'
#' @examples
#' \dontrun{
#' library("rDNA")
#' dna_init()
#' samp <- dna_sample()
#' dna_openDatabase(samp, coderId = 1, coderPassword = "sample")
#'
#' # example 1: compute 12 cluster solutions for one time point
#' mc1 <- dna_multiclust(variable1 = "organization",
#'                       variable2 = "concept",
#'                       qualifier = "agreement",
#'                       duplicates = "document",
#'                       k = 0,                # flexible numbers of clusters
#'                       saveObjects = TRUE)   # retain hclust object
#'
#' mc1$modularity      # return modularity scores for 12 clustering methods
#' mc1$max_mod         # return the maximal value of the 12, along with dates
#' mc1$memberships     # return cluster memberships for all 12 cluster methods
#' plot(mc1$cl[[1]])   # plot hclust dendrogram
#'
#' # example 2: compute only Girvan-Newman edge betweenness with two clusters
#' set.seed(12345)
#' mc2 <- dna_multiclust(k = 2,
#'                       single = FALSE,
#'                       average = FALSE,
#'                       complete = FALSE,
#'                       ward = FALSE,
#'                       kmeans = FALSE,
#'                       pam = FALSE,
#'                       equivalence = FALSE,
#'                       concor_one = FALSE,
#'                       concor_two = FALSE,
#'                       louvain = FALSE,
#'                       fastgreedy = FALSE,
#'                       walktrap = FALSE,
#'                       leading_eigen = FALSE,
#'                       edge_betweenness = TRUE,
#'                       infomap = FALSE,
#'                       label_prop = FALSE,
#'                       spinglass = FALSE)
#' mc2$memberships     # return membership in two clusters
#' mc2$modularity      # return modularity of the cluster solution
#'
#' # example 3: smoothed modularity using time window algorithm
#' mc3 <- dna_multiclust(k = 2,
#'                       timeWindow = "events",
#'                       windowSize = 28)
#' mc3$max_mod         # maximal modularity and method per time point
#' }
#'
#' @rdname dna_multiclust
#' @importFrom stats as.dist cor hclust cutree kmeans
#' @importFrom utils packageVersion
#' @export
dna_multiclust <- function(statementType = "DNA Statement",
                           variable1 = "organization",
                           variable1Document = FALSE,
                           variable2 = "concept",
                           variable2Document = FALSE,
                           qualifier = "agreement",
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
                           saveObjects = FALSE,
                           k = 0,
                           k.max = 5,
                           single = TRUE,
                           average = TRUE,
                           complete = TRUE,
                           ward = TRUE,
                           kmeans = TRUE,
                           pam = TRUE,
                           equivalence = TRUE,
                           concor_one = TRUE,
                           concor_two = TRUE,
                           louvain = TRUE,
                           fastgreedy = TRUE,
                           walktrap = TRUE,
                           leading_eigen = TRUE,
                           edge_betweenness = TRUE,
                           infomap = TRUE,
                           label_prop = TRUE,
                           spinglass = FALSE) {

  # check dependencies
  if (!requireNamespace("igraph", quietly = TRUE)) { # version 0.8.1 required for edge betweenness to work fine.
    stop("The 'dna_multiclust' function requires the 'igraph' package to be installed.\n",
         "To do this, enter 'install.packages(\"igraph\")'.")
  } else if (packageVersion("igraph") < "0.8.1" && edge_betweenness) {
    warning("Package version of 'igraph' < 0.8.1. If edge betweenness algorithm encounters an empty network matrix, this will let R crash. See here: https://github.com/igraph/rigraph/issues/336. Consider updating 'igraph' to the latest version.")
  }
  if (pam && !requireNamespace("cluster", quietly = TRUE)) {
    pam <- FALSE
    warning("Argument 'pam = TRUE' requires the 'cluster' package, which is not installed.\nSetting 'pam = FALSE'. Consider installing the 'cluster' package.")
  }
  if (equivalence && !requireNamespace("sna", quietly = TRUE)) {
    equivalence <- FALSE
    warning("Argument 'equivalence = TRUE' requires the 'sna' package, which is not installed.\nSetting 'equivalence = FALSE'. Consider installing the 'sna' package.")
  }

  # check argument validity
  if (is.null(k) || is.na(k) || !is.numeric(k) || length(k) > 1 || is.infinite(k) || k < 0) {
    stop("'k' must be a non-negative integer number. Can be 0 for flexible numbers of clusters.")
  }
  if (is.null(k.max) || is.na(k.max) || !is.numeric(k.max) || length(k.max) > 1 || is.infinite(k.max) || k.max < 1) {
    stop("'k.max' must be a positive integer number.")
  }
  if (k == 1) {
    k <- 0
    warning("'k' must be 0 (for arbitrary numbers of clusters) or larger than 1 (to constrain number of clusters). Using 'k = 0'.")
  }

  # determine what kind of two-mode network to create
  if (is.null(qualifier) || is.na(qualifier) || !is.character(qualifier)) {
    qualifierAggregation <- "ignore"
  } else {
    v <- dna_getVariables(statementType = statementType)
    if (v$type[v$label == qualifier] == "boolean") {
      qualifierAggregation <- "combine"
    } else {
      qualifierAggregation <- "subtract"
    }
  }

  nw_aff <- dna_network(networkType = "twomode",
                        statementType = statementType,
                        variable1 = variable1,
                        variable1Document = variable1Document,
                        variable2 = variable2,
                        variable2Document = variable2Document,
                        qualifier = qualifier,
                        qualifierAggregation = qualifierAggregation,
                        normalization = "no",
                        duplicates = duplicates,
                        start.date = start.date,
                        stop.date = stop.date,
                        start.time = start.time,
                        stop.time = stop.time,
                        timeWindow = timeWindow,
                        windowSize = windowSize,
                        excludeValues = excludeValues,
                        excludeAuthors = excludeAuthors,
                        excludeSources = excludeSources,
                        excludeSections = excludeSections,
                        excludeTypes = excludeTypes,
                        invertValues = invertValues,
                        invertAuthors = invertAuthors,
                        invertSources = invertSources,
                        invertSections = invertSections,
                        invertTypes = invertTypes)
  nw_sub <- dna_network(networkType = "onemode",
                        statementType = statementType,
                        variable1 = variable1,
                        variable1Document = variable1Document,
                        variable2 = variable2,
                        variable2Document = variable2Document,
                        qualifier = qualifier,
                        qualifierAggregation = "subtract",
                        normalization = "average",
                        duplicates = duplicates,
                        start.date = start.date,
                        stop.date = stop.date,
                        start.time = start.time,
                        stop.time = stop.time,
                        timeWindow = timeWindow,
                        windowSize = windowSize,
                        excludeValues = excludeValues,
                        excludeAuthors = excludeAuthors,
                        excludeSources = excludeSources,
                        excludeSections = excludeSections,
                        excludeTypes = excludeTypes,
                        invertValues = invertValues,
                        invertAuthors = invertAuthors,
                        invertSources = invertSources,
                        invertSections = invertSections,
                        invertTypes = invertTypes)

  if (timeWindow == "no") {
    dta <- list()
    dta$networks <- list(nw_sub)
    nw_sub <- dta
    dta <- list()
    dta$networks <- list(nw_aff)
    nw_aff <- dta
  }

  obj <- list()
  if (isTRUE(saveObjects)) {
    obj$cl <- list()
  }
  dta_dat <- list()
  dta_mem <- list()
  dta_mod <- list()
  counter <- 1
  if ("dna_network_onemode_timewindows" %in% class(nw_sub)) {
    num_networks <- length(nw_sub)
  } else {
    num_networks <- 1
  }
  for (i in 1:num_networks) {

    # prepare dates
    if (timeWindow == "no") {
      dta_dat[[i]] <- data.frame(i = i,
                                 start = attributes(nw_sub$networks[[i]])$start,
                                 stop = attributes(nw_sub$networks[[i]])$stop)
    } else {
      dta_dat[[i]] <- data.frame(i = i,
                                 start.date = attributes(nw_sub[[i]])$start,
                                 middle.date = attributes(nw_sub[[i]])$middle,
                                 stop.date = attributes(nw_sub[[i]])$stop)
    }

    # prepare two-mode network
    if ("dna_network_onemode_timewindows" %in% class(nw_sub)) {
      x <- nw_aff[[i]]
    } else {
      x <- nw_aff$networks[[i]]
    }
    if (qualifierAggregation == "combine") {
      combined <- cbind(apply(x, 1:2, function(x) ifelse(x %in% c(1, 3), 1, 0)),
                        apply(x, 1:2, function(x) ifelse(x %in% c(2, 3), 1, 0)))
    } else {
      combined <- x
    }
    combined <- combined[rowSums(combined) > 0, , drop = FALSE]
    rn <- rownames(combined)

    # Jaccard distances for two-mode network (could be done using vegdist function in vegan package, but saving the dependency)
    combined <- matrix(as.integer(combined > 0), nrow = nrow(combined)) # probably not necessary, but ensure it's an integer matrix
    intersections <- tcrossprod(combined) # compute intersections using cross-product
    row_sums <- rowSums(combined) # compute row sums
    unions <- matrix(outer(row_sums, row_sums, `+`), ncol = length(row_sums)) - intersections # compute unions
    jaccard_similarities <- intersections / unions # calculate Jaccard similarities
    jaccard_similarities[is.nan(jaccard_similarities)] <- 0 # avoid division by zero
    jaccard_distances <- 1 - jaccard_similarities # convert to Jaccard distances
    rownames(jaccard_distances) <- rn # re-attach the row names
    jac <- stats::as.dist(jaccard_distances) # convert to dist object

    # prepare one-mode network
    if ("dna_network_onemode_timewindows" %in% class(nw_sub)) {
      y <- nw_sub[[i]]
    } else {
      y <- nw_sub$networks[[i]]
    }
    y[y < 0] <- 0
    class(y) <- "matrix"
    g <- igraph::graph_from_adjacency_matrix(y, mode = "undirected", weighted = TRUE)

    if (nrow(combined) > 1) {
      counter_current <- 1
      current_cl <- list()
      current_mod <- numeric()

      # Hierarchical clustering with single linkage
      if (isTRUE(single) && k > 1) {
        try({
          suppressWarnings(cl <- stats::hclust(jac, method = "single"))
          mem <- stats::cutree(cl, k = k)
          dta_mem[[counter]] <- data.frame(i = rep(i, length(mem)),
                                           method = rep("Hierarchical (Single)", length(mem)),
                                           node = rownames(x),
                                           cluster = mem,
                                           stringsAsFactors = FALSE)
          dta_mod[[counter]] <- data.frame(i = i,
                                           method = "Hierarchical (Single)",
                                           k = k,
                                           modularity = igraph::modularity(x = g, membership = mem),
                                           stringsAsFactors = FALSE)
          if (isTRUE(saveObjects)) {
            current_cl[[counter_current]] <- cl
            current_mod[counter_current] <- dta_mod[[counter]]$modularity[nrow(dta_mod[[counter]])]
            counter_current <- counter_current + 1
          }
          counter <- counter + 1
        }, silent = TRUE)
      }

      # Hierarchical clustering with single linkage with optimal k
      if (isTRUE(single) && k < 2) {
        try({
          suppressWarnings(cl <- stats::hclust(jac, method = "single"))
          opt_k <- lapply(2:k.max, function(x) {
            mem <- stats::cutree(cl, k = x)
            mod <- igraph::modularity(x = g, membership = mem)
            return(list(mem = mem, mod = mod))
          })
          mod <- sapply(opt_k, function(x) x$mod)
          kk <- which.max(mod)
          mod <- max(mod)
          mem <- opt_k[[kk]]$mem
          dta_mem[[counter]] <- data.frame(i = rep(i, length(mem)),
                                           method = rep("Hierarchical (Single)", length(mem)),
                                           node = rownames(x),
                                           cluster = mem,
                                           stringsAsFactors = FALSE)
          dta_mod[[counter]] <- data.frame(i = i,
                                           method = "Hierarchical (Single)",
                                           k = kk + 1, # add one because the series started with k = 2
                                           modularity = mod,
                                           stringsAsFactors = FALSE)
          if (isTRUE(saveObjects)) {
            current_cl[[counter_current]] <- cl
            current_mod[counter_current] <- dta_mod[[counter]]$modularity[nrow(dta_mod[[counter]])]
            counter_current <- counter_current + 1
          }
          counter <- counter + 1
        }, silent = TRUE)
      }

      # Hierarchical clustering with average linkage
      if (isTRUE(average) && k > 1) {
        try({
          suppressWarnings(cl <- stats::hclust(jac, method = "average"))
          mem <- stats::cutree(cl, k = k)
          dta_mem[[counter]] <- data.frame(i = rep(i, length(mem)),
                                           method = rep("Hierarchical (Average)", length(mem)),
                                           node = rownames(x),
                                           cluster = mem,
                                           stringsAsFactors = FALSE)
          dta_mod[[counter]] <- data.frame(i = i,
                                           method = "Hierarchical (Average)",
                                           k = k,
                                           modularity = igraph::modularity(x = g, membership = mem),
                                           stringsAsFactors = FALSE)
          if (isTRUE(saveObjects)) {
            current_cl[[counter_current]] <- cl
            current_mod[counter_current] <- dta_mod[[counter]]$modularity[nrow(dta_mod[[counter]])]
            counter_current <- counter_current + 1
          }
          counter <- counter + 1
        }, silent = TRUE)
      }

      # Hierarchical clustering with average linkage with optimal k
      if (isTRUE(average) && k < 2) {
        try({
          suppressWarnings(cl <- stats::hclust(jac, method = "average"))
          opt_k <- lapply(2:k.max, function(x) {
            mem <- stats::cutree(cl, k = x)
            mod <- igraph::modularity(x = g, membership = mem)
            return(list(mem = mem, mod = mod))
          })
          mod <- sapply(opt_k, function(x) x$mod)
          kk <- which.max(mod)
          mod <- max(mod)
          mem <- opt_k[[kk]]$mem
          dta_mem[[counter]] <- data.frame(i = rep(i, length(mem)),
                                           method = rep("Hierarchical (Average)", length(mem)),
                                           node = rownames(x),
                                           cluster = mem,
                                           stringsAsFactors = FALSE)
          dta_mod[[counter]] <- data.frame(i = i,
                                           method = "Hierarchical (Average)",
                                           k = kk + 1, # add one because the series started with k = 2
                                           modularity = mod,
                                           stringsAsFactors = FALSE)
          if (isTRUE(saveObjects)) {
            current_cl[[counter_current]] <- cl
            current_mod[counter_current] <- dta_mod[[counter]]$modularity[nrow(dta_mod[[counter]])]
            counter_current <- counter_current + 1
          }
          counter <- counter + 1
        }, silent = TRUE)
      }

      # Hierarchical clustering with complete linkage
      if (isTRUE(complete) && k > 1) {
        try({
          suppressWarnings(cl <- stats::hclust(jac, method = "complete"))
          mem <- stats::cutree(cl, k = k)
          dta_mem[[counter]] <- data.frame(i = rep(i, length(mem)),
                                           method = rep("Hierarchical (Complete)", length(mem)),
                                           node = rownames(x),
                                           cluster = mem,
                                           stringsAsFactors = FALSE)
          dta_mod[[counter]] <- data.frame(i = i,
                                           method = "Hierarchical (Complete)",
                                           k = k,
                                           modularity = igraph::modularity(x = g, membership = mem),
                                           stringsAsFactors = FALSE)
          if (isTRUE(saveObjects)) {
            current_cl[[counter_current]] <- cl
            current_mod[counter_current] <- dta_mod[[counter]]$modularity[nrow(dta_mod[[counter]])]
            counter_current <- counter_current + 1
          }
          counter <- counter + 1
        }, silent = TRUE)
      }

      # Hierarchical clustering with complete linkage with optimal k
      if (isTRUE(complete) && k < 2) {
        try({
          suppressWarnings(cl <- stats::hclust(jac, method = "complete"))
          opt_k <- lapply(2:k.max, function(x) {
            mem <- stats::cutree(cl, k = x)
            mod <- igraph::modularity(x = g, membership = mem)
            return(list(mem = mem, mod = mod))
          })
          mod <- sapply(opt_k, function(x) x$mod)
          kk <- which.max(mod)
          mod <- max(mod)
          mem <- opt_k[[kk]]$mem
          dta_mem[[counter]] <- data.frame(i = rep(i, length(mem)),
                                           method = rep("Hierarchical (Complete)", length(mem)),
                                           node = rownames(x),
                                           cluster = mem,
                                           stringsAsFactors = FALSE)
          dta_mod[[counter]] <- data.frame(i = i,
                                           method = "Hierarchical (Complete)",
                                           k = kk + 1, # add one because the series started with k = 2
                                           modularity = mod,
                                           stringsAsFactors = FALSE)
          if (isTRUE(saveObjects)) {
            current_cl[[counter_current]] <- cl
            current_mod[counter_current] <- dta_mod[[counter]]$modularity[nrow(dta_mod[[counter]])]
            counter_current <- counter_current + 1
          }
          counter <- counter + 1
        }, silent = TRUE)
      }

      # Hierarchical clustering with the Ward algorithm
      if (isTRUE(ward) && k > 1) {
        try({
          suppressWarnings(cl <- stats::hclust(jac, method = "ward.D2"))
          mem <- stats::cutree(cl, k = k)
          dta_mem[[counter]] <- data.frame(i = rep(i, length(mem)),
                                           method = rep("Hierarchical (Ward)", length(mem)),
                                           node = rownames(x),
                                           cluster = mem,
                                           stringsAsFactors = FALSE)
          dta_mod[[counter]] <- data.frame(i = i,
                                           method = "Hierarchical (Ward)",
                                           k = k,
                                           modularity = igraph::modularity(x = g, membership = mem),
                                           stringsAsFactors = FALSE)
          if (isTRUE(saveObjects)) {
            current_cl[[counter_current]] <- cl
            current_mod[counter_current] <- dta_mod[[counter]]$modularity[nrow(dta_mod[[counter]])]
            counter_current <- counter_current + 1
          }
          counter <- counter + 1
        }, silent = TRUE)
      }

      # Hierarchical clustering with the Ward algorithm with optimal k
      if (isTRUE(ward) && k < 2) {
        try({
          suppressWarnings(cl <- stats::hclust(jac, method = "ward.D2"))
          opt_k <- lapply(2:k.max, function(x) {
            mem <- stats::cutree(cl, k = x)
            mod <- igraph::modularity(x = g, membership = mem)
            return(list(mem = mem, mod = mod))
          })
          mod <- sapply(opt_k, function(x) x$mod)
          kk <- which.max(mod)
          mod <- max(mod)
          mem <- opt_k[[kk]]$mem
          dta_mem[[counter]] <- data.frame(i = rep(i, length(mem)),
                                           method = rep("Hierarchical (Ward)", length(mem)),
                                           node = rownames(x),
                                           cluster = mem,
                                           stringsAsFactors = FALSE)
          dta_mod[[counter]] <- data.frame(i = i,
                                           method = "Hierarchical (Ward)",
                                           k = kk + 1, # add one because the series started with k = 2
                                           modularity = mod,
                                           stringsAsFactors = FALSE)
          if (isTRUE(saveObjects)) {
            current_cl[[counter_current]] <- cl
            current_mod[counter_current] <- dta_mod[[counter]]$modularity[nrow(dta_mod[[counter]])]
            counter_current <- counter_current + 1
          }
          counter <- counter + 1
        }, silent = TRUE)
      }

      # k-means
      if (isTRUE(kmeans) && k > 1) {
        try({
          suppressWarnings(cl <- stats::kmeans(jac, centers = k))
          mem <- cl$cluster
          dta_mem[[counter]] <- data.frame(i = rep(i, length(mem)),
                                           method = rep("k-Means", length(mem)),
                                           node = rownames(x),
                                           cluster = mem,
                                           stringsAsFactors = FALSE)
          dta_mod[[counter]] <- data.frame(i = i,
                                           method = "k-Means",
                                           k = k,
                                           modularity = igraph::modularity(x = g, membership = mem),
                                           stringsAsFactors = FALSE)
          if (isTRUE(saveObjects)) {
            current_cl[[counter_current]] <- cl
            current_mod[counter_current] <- dta_mod[[counter]]$modularity[nrow(dta_mod[[counter]])]
            counter_current <- counter_current + 1
          }
          counter <- counter + 1
        }, silent = TRUE)
      }

      # k-means with optimal k
      if (isTRUE(kmeans) && k < 2) {
        try({
          opt_k <- lapply(2:k.max, function(x) {
            suppressWarnings(cl <- stats::kmeans(jac, centers = x))
            mem <- cl$cluster
            mod <- igraph::modularity(x = g, membership = mem)
            return(list(cl = cl, mem = mem, mod = mod))
          })
          mod <- sapply(opt_k, function(x) x$mod)
          kk <- which.max(mod)
          mod <- max(mod)
          mem <- opt_k[[kk]]$mem
          dta_mem[[counter]] <- data.frame(i = rep(i, length(mem)),
                                           method = rep("k-Means", length(mem)),
                                           node = rownames(x),
                                           cluster = mem,
                                           stringsAsFactors = FALSE)
          dta_mod[[counter]] <- data.frame(i = i,
                                           method = "k-Means",
                                           k = kk + 1, # add one because the series started with k = 2
                                           modularity = mod,
                                           stringsAsFactors = FALSE)
          if (isTRUE(saveObjects)) {
            cl <- opt_k[[kk]]$cl
            current_cl[[counter_current]] <- cl
            current_mod[counter_current] <- dta_mod[[counter]]$modularity[nrow(dta_mod[[counter]])]
            counter_current <- counter_current + 1
          }
          counter <- counter + 1
        }, silent = TRUE)
      }

      # pam
      if (isTRUE(pam) && k > 1) {
        try({
          suppressWarnings(cl <- cluster::pam(jac, k = k))
          mem <- cl$cluster
          dta_mem[[counter]] <- data.frame(i = rep(i, length(mem)),
                                           method = rep("Partitioning around Medoids", length(mem)),
                                           node = rownames(x),
                                           cluster = mem,
                                           stringsAsFactors = FALSE)
          dta_mod[[counter]] <- data.frame(i = i,
                                           method = "Partitioning around Medoids",
                                           k = k,
                                           modularity = igraph::modularity(x = g, membership = mem),
                                           stringsAsFactors = FALSE)
          if (isTRUE(saveObjects)) {
            current_cl[[counter_current]] <- cl
            current_mod[counter_current] <- dta_mod[[counter]]$modularity[nrow(dta_mod[[counter]])]
            counter_current <- counter_current + 1
          }
          counter <- counter + 1
        }, silent = TRUE)
      }

      # pam with optimal k
      if (isTRUE(pam) && k < 2) {
        try({
          opt_k <- lapply(2:k.max, function(x) {
            suppressWarnings(cl <- cluster::pam(jac, k = x))
            mem <- cl$cluster
            mod <- igraph::modularity(x = g, membership = mem)
            return(list(cl = cl, mem = mem, mod = mod))
          })
          mod <- sapply(opt_k, function(x) x$mod)
          kk <- which.max(mod)
          mod <- max(mod)
          mem <- opt_k[[kk]]$mem
          dta_mem[[counter]] <- data.frame(i = rep(i, length(mem)),
                                           method = rep("Partitioning around Medoids", length(mem)),
                                           node = rownames(x),
                                           cluster = mem,
                                           stringsAsFactors = FALSE)
          dta_mod[[counter]] <- data.frame(i = i,
                                           method = "Partitioning around Medoids",
                                           k = kk + 1, # add one because the series started with k = 2
                                           modularity = mod,
                                           stringsAsFactors = FALSE)
          if (isTRUE(saveObjects)) {
            cl <- opt_k[[kk]]$cl
            current_cl[[counter_current]] <- cl
            current_mod[counter_current] <- dta_mod[[counter]]$modularity[nrow(dta_mod[[counter]])]
            counter_current <- counter_current + 1
          }
          counter <- counter + 1
        }, silent = TRUE)
      }

      # Equivalence clustering
      if (isTRUE(equivalence) && k > 1) {
        try({
          suppressWarnings(cl <- sna::equiv.clust(y, equiv.dist = sna::sedist(y, method = "euclidean")))
          mem <- stats::cutree(cl$cluster, k = k)
          dta_mem[[counter]] <- data.frame(i = rep(i, length(mem)),
                                           method = rep("Equivalence", length(mem)),
                                           node = rownames(x),
                                           cluster = mem,
                                           stringsAsFactors = FALSE)
          dta_mod[[counter]] <- data.frame(i = i,
                                           method = "Equivalence",
                                           k = k,
                                           modularity = igraph::modularity(x = g, membership = mem),
                                           stringsAsFactors = FALSE)
          if (isTRUE(saveObjects)) {
            current_cl[[counter_current]] <- cl
            current_mod[counter_current] <- dta_mod[[counter]]$modularity[nrow(dta_mod[[counter]])]
            counter_current <- counter_current + 1
          }
          counter <- counter + 1
        }, silent = TRUE)
      }

      # Equivalence clustering with optimal k
      if (isTRUE(equivalence) && k < 2) {
        try({
          suppressWarnings(cl <- sna::equiv.clust(y, equiv.dist = sna::sedist(y, method = "euclidean")))
          opt_k <- lapply(2:k.max, function(x) {
            mem <- stats::cutree(cl$cluster, k = x)
            mod <- igraph::modularity(x = g, membership = mem)
            return(list(mem = mem, mod = mod))
          })
          mod <- sapply(opt_k, function(x) x$mod)
          kk <- which.max(mod)
          mod <- max(mod)
          mem <- opt_k[[kk]]$mem
          dta_mem[[counter]] <- data.frame(i = rep(i, length(mem)),
                                           method = rep("Equivalence", length(mem)),
                                           node = rownames(x),
                                           cluster = mem,
                                           stringsAsFactors = FALSE)
          dta_mod[[counter]] <- data.frame(i = i,
                                           method = "Equivalence",
                                           k = kk + 1, # add one because the series started with k = 2
                                           modularity = mod,
                                           stringsAsFactors = FALSE)
          if (isTRUE(saveObjects)) {
            current_cl[[counter_current]] <- cl
            current_mod[counter_current] <- dta_mod[[counter]]$modularity[nrow(dta_mod[[counter]])]
            counter_current <- counter_current + 1
          }
          counter <- counter + 1
        }, silent = TRUE)
      }

      # CONCOR based on the positive subtract network
      if (isTRUE(concor_one) && k %in% c(0, 2)) {
        try({
          suppressWarnings(mi <- stats::cor(y))
          iter <- 1
          while (any(abs(mi) <= 0.999) & iter <= 50) {
            mi[is.na(mi)] <- 0
            mi <- stats::cor(mi)
            iter <- iter + 1
          }
          mem <- ((mi[, 1] > 0) * 1) + 1
          dta_mem[[counter]] <- data.frame(i = rep(i, length(mem)),
                                           method = rep("CONCOR (One-Mode)", length(mem)),
                                           node = rownames(x),
                                           cluster = mem,
                                           stringsAsFactors = FALSE)
          dta_mod[[counter]] <- data.frame(i = i,
                                           method = "CONCOR (One-Mode)",
                                           k = 2,
                                           modularity = igraph::modularity(x = g, membership = mem),
                                           stringsAsFactors = FALSE)
          if (isTRUE(saveObjects)) {
            current_cl[[counter_current]] <- mem
            current_mod[counter_current] <- dta_mod[[counter]]$modularity[nrow(dta_mod[[counter]])]
            counter_current <- counter_current + 1
          }
          counter <- counter + 1
        }, silent = TRUE)
      }

      # CONCOR based on the combined affiliation network
      if (isTRUE(concor_two) && k %in% c(0, 2)) {
        try({
          suppressWarnings(mi <- stats::cor(t(combined)))
          iter <- 1
          while (any(abs(mi) <= 0.999) & iter <= 50) {
            mi[is.na(mi)] <- 0
            mi <- stats::cor(mi)
            iter <- iter + 1
          }
          mem <- ((mi[, 1] > 0) * 1) + 1
          dta_mem[[counter]] <- data.frame(i = rep(i, length(mem)),
                                           method = rep("CONCOR (Two-Mode)", length(mem)),
                                           node = rownames(x),
                                           cluster = mem,
                                           stringsAsFactors = FALSE)
          dta_mod[[counter]] <- data.frame(i = i,
                                           method = "CONCOR (Two-Mode)",
                                           k = 2,
                                           modularity = igraph::modularity(x = g, membership = mem),
                                           stringsAsFactors = FALSE)
          if (isTRUE(saveObjects)) {
            current_cl[[counter_current]] <- mem
            current_mod[counter_current] <- dta_mod[[counter]]$modularity[nrow(dta_mod[[counter]])]
            counter_current <- counter_current + 1
          }
          counter <- counter + 1
        }, silent = TRUE)
      }

      # Louvain clustering
      if (isTRUE(louvain) && k < 2) {
        try({
          suppressWarnings(cl <- igraph::cluster_louvain(g))
          mem <- igraph::membership(cl)
          dta_mem[[counter]] <- data.frame(i = rep(i, length(mem)),
                                           method = rep("Louvain", length(mem)),
                                           node = rownames(x),
                                           cluster = as.numeric(mem),
                                           stringsAsFactors = FALSE)
          dta_mod[[counter]] <- data.frame(i = i,
                                           method = "Louvain",
                                           k = max(as.numeric(mem)),
                                           modularity = igraph::modularity(x = g, membership = mem),
                                           stringsAsFactors = FALSE)
          if (isTRUE(saveObjects)) {
            current_cl[[counter_current]] <- cl
            current_mod[counter_current] <- dta_mod[[counter]]$modularity[nrow(dta_mod[[counter]])]
            counter_current <- counter_current + 1
          }
          counter <- counter + 1
        }, silent = TRUE)
      }

      # Fast & Greedy community detection (with or without cut)
      if (isTRUE(fastgreedy)) {
        try({
          suppressWarnings(cl <- igraph::cluster_fast_greedy(g, merges = TRUE))
          if (k == 0) {
            mem <- igraph::membership(cl)
          } else {
            mem <- suppressWarnings(igraph::cut_at(cl, no = k))
            if ((k + 1) %in% as.numeric(mem)) {
              stop()
            }
          }
          dta_mem[[counter]] <- data.frame(i = rep(i, length(mem)),
                                           method = rep("Fast & Greedy", length(mem)),
                                           node = rownames(x),
                                           cluster = as.numeric(mem),
                                           stringsAsFactors = FALSE)
          dta_mod[[counter]] <- data.frame(i = i,
                                           method = "Fast & Greedy",
                                           k = ifelse(k == 0, max(as.numeric(mem)), k),
                                           modularity = igraph::modularity(x = g, membership = mem),
                                           stringsAsFactors = FALSE)
          if (isTRUE(saveObjects)) {
            current_cl[[counter_current]] <- cl
            current_mod[counter_current] <- dta_mod[[counter]]$modularity[nrow(dta_mod[[counter]])]
            counter_current <- counter_current + 1
          }
          counter <- counter + 1
        }, silent = TRUE)
      }

      # Walktrap community detection (with or without cut)
      if (isTRUE(walktrap)) {
        try({
          suppressWarnings(cl <- igraph::cluster_walktrap(g, merges = TRUE))
          if (k == 0) {
            mem <- igraph::membership(cl)
          } else {
            mem <- suppressWarnings(igraph::cut_at(cl, no = k))
            if ((k + 1) %in% as.numeric(mem)) {
              stop()
            }
          }
          dta_mem[[counter]] <- data.frame(i = rep(i, length(mem)),
                                           method = rep("Walktrap", length(mem)),
                                           node = rownames(x),
                                           cluster = as.numeric(mem),
                                           stringsAsFactors = FALSE)
          dta_mod[[counter]] <- data.frame(i = i,
                                           method = "Walktrap",
                                           k = ifelse(k == 0, max(as.numeric(mem)), k),
                                           modularity = igraph::modularity(x = g, membership = mem),
                                           stringsAsFactors = FALSE)
          if (isTRUE(saveObjects)) {
            current_cl[[counter_current]] <- cl
            current_mod[counter_current] <- dta_mod[[counter]]$modularity[nrow(dta_mod[[counter]])]
            counter_current <- counter_current + 1
          }
          counter <- counter + 1
        }, silent = TRUE)
      }

      # Leading Eigenvector community detection (only without cut)
      if (isTRUE(leading_eigen) && k < 2) { # it *should* work with cut_at because is.hierarchical(cl) returns TRUE, but it never works...
        try({
          suppressWarnings(cl <- igraph::cluster_leading_eigen(g))
          mem <- igraph::membership(cl)
          dta_mem[[counter]] <- data.frame(i = rep(i, length(mem)),
                                           method = rep("Leading Eigenvector", length(mem)),
                                           node = rownames(x),
                                           cluster = as.numeric(mem),
                                           stringsAsFactors = FALSE)
          dta_mod[[counter]] <- data.frame(i = i,
                                           method = "Leading Eigenvector",
                                           k = max(as.numeric(mem)),
                                           modularity = igraph::modularity(x = g, membership = mem),
                                           stringsAsFactors = FALSE)
          if (isTRUE(saveObjects)) {
            current_cl[[counter_current]] <- cl
            current_mod[counter_current] <- dta_mod[[counter]]$modularity[nrow(dta_mod[[counter]])]
            counter_current <- counter_current + 1
          }
          counter <- counter + 1
        }, silent = TRUE)
      }

      # Edge Betweenness community detection (with or without cut)
      if (isTRUE(edge_betweenness)) {
        try({
          suppressWarnings(cl <- igraph::cluster_edge_betweenness(g, merges = TRUE))
          if (k == 0) {
            mem <- igraph::membership(cl)
          } else {
            mem <- suppressWarnings(igraph::cut_at(cl, no = k))
            if ((k + 1) %in% as.numeric(mem)) {
              stop()
            }
          }
          dta_mem[[counter]] <- data.frame(i = rep(i, length(mem)),
                                           method = rep("Edge Betweenness", length(mem)),
                                           node = rownames(x),
                                           cluster = as.numeric(mem),
                                           stringsAsFactors = FALSE)
          dta_mod[[counter]] <- data.frame(i = i,
                                           method = "Edge Betweenness",
                                           k = ifelse(k == 0, max(as.numeric(mem)), k),
                                           modularity = igraph::modularity(x = g, membership = mem),
                                           stringsAsFactors = FALSE)
          if (isTRUE(saveObjects)) {
            current_cl[[counter_current]] <- cl
            current_mod[counter_current] <- dta_mod[[counter]]$modularity[nrow(dta_mod[[counter]])]
            counter_current <- counter_current + 1
          }
          counter <- counter + 1
        }, silent = TRUE)
      }

      # Infomap community detection
      if (isTRUE(infomap) && k < 2) {
        try({
          suppressWarnings(cl <- igraph::cluster_infomap(g))
          mem <- igraph::membership(cl)
          dta_mem[[counter]] <- data.frame(i = rep(i, length(mem)),
                                           method = rep("Infomap", length(mem)),
                                           node = rownames(x),
                                           cluster = as.numeric(mem),
                                           stringsAsFactors = FALSE)
          dta_mod[[counter]] <- data.frame(i = i,
                                           method = "Infomap",
                                           k = max(as.numeric(mem)),
                                           modularity = igraph::modularity(x = g, membership = mem),
                                           stringsAsFactors = FALSE)
          if (isTRUE(saveObjects)) {
            current_cl[[counter_current]] <- cl
            current_mod[counter_current] <- dta_mod[[counter]]$modularity[nrow(dta_mod[[counter]])]
            counter_current <- counter_current + 1
          }
          counter <- counter + 1
        }, silent = TRUE)
      }

      # Label Propagation community detection
      if (isTRUE(label_prop) && k < 2) {
        try({
          suppressWarnings(cl <- igraph::cluster_label_prop(g))
          mem <- igraph::membership(cl)
          dta_mem[[counter]] <- data.frame(i = rep(i, length(mem)),
                                           method = rep("Label Propagation", length(mem)),
                                           node = rownames(x),
                                           cluster = as.numeric(mem),
                                           stringsAsFactors = FALSE)
          dta_mod[[counter]] <- data.frame(i = i,
                                           method = "Label Propagation",
                                           k = max(as.numeric(mem)),
                                           modularity = igraph::modularity(x = g, membership = mem),
                                           stringsAsFactors = FALSE)
          if (isTRUE(saveObjects)) {
            current_cl[[counter_current]] <- cl
            current_mod[counter_current] <- dta_mod[[counter]]$modularity[nrow(dta_mod[[counter]])]
            counter_current <- counter_current + 1
          }
          counter <- counter + 1
        }, silent = TRUE)
      }

      # Spinglass community detection
      if (isTRUE(spinglass) && k < 2) {
        try({
          suppressWarnings(cl <- igraph::cluster_spinglass(g))
          mem <- igraph::membership(cl)
          dta_mem[[counter]] <- data.frame(i = rep(i, length(mem)),
                                           method = rep("Spinglass", length(mem)),
                                           node = rownames(x),
                                           cluster = as.numeric(mem),
                                           stringsAsFactors = FALSE)
          dta_mod[[counter]] <- data.frame(i = i,
                                           method = "Spinglass",
                                           k = max(as.numeric(mem)),
                                           modularity = igraph::modularity(x = g, membership = mem),
                                           stringsAsFactors = FALSE)
          if (isTRUE(saveObjects)) {
            current_cl[[counter_current]] <- cl
            current_mod[counter_current] <- dta_mod[[counter]]$modularity[nrow(dta_mod[[counter]])]
            counter_current <- counter_current + 1
          }
          counter <- counter + 1
        }, silent = TRUE)
      }

      # retain cluster object where modularity was maximal
      if (isTRUE(saveObjects) && length(current_cl) > 0) {
        obj$cl[[i]] <- current_cl[[which.max(current_mod)]]
      }
    }
  }
  obj$cl <- obj$cl[!sapply(obj$cl, is.null)] # remove NULL objects that may occur when the network is empty
  obj$k <- k
  obj$max_mod <- do.call(rbind, dta_dat)
  memberships <- do.call(rbind, dta_mem)
  rownames(memberships) <- NULL
  obj$memberships <- memberships
  obj$modularity <- do.call(rbind, dta_mod)
  if (nrow(obj$modularity) == 0) {
    stop("No output rows. Either you switched all clustering methods off, or all methods you used produced errors.")
  }
  obj$max_mod <- obj$max_mod[obj$max_mod$i %in% obj$modularity$i, ] # remove date entries where the network is empty
  obj$max_mod$max_mod <- sapply(obj$max_mod$i, function(x) max(obj$modularity$modularity[obj$modularity$i == x], na.rm = TRUE)) # attach max_mod to $max_mod
  # attach max_method to $max_mod
  obj$max_mod$max_method <- sapply(obj$max_mod$i,
                                   function(x) obj$modularity$method[obj$modularity$i == x & obj$modularity$modularity == max(obj$modularity$modularity[obj$modularity$i == x], na.rm = TRUE)][1])
  # attach k to max_mod
  obj$max_mod$k <- sapply(obj$max_mod$i, function(x) max(obj$modularity$k[obj$modularity$i == x], na.rm = TRUE))

  # diagnostics
  if (isTRUE(single) && !"Hierarchical (Single)" %in% obj$modularity$method && k > 1) {
    warning("'single' omitted due to an unknown problem.")
  }
  if (isTRUE(average) && !"Hierarchical (Average)" %in% obj$modularity$method && k > 1) {
    warning("'average' omitted due to an unknown problem.")
  }
  if (isTRUE(complete) && !"Hierarchical (Complete)" %in% obj$modularity$method && k > 1) {
    warning("'complete' omitted due to an unknown problem.")
  }
  if (isTRUE(ward) && !"Hierarchical (Ward)" %in% obj$modularity$method) {
    warning("'ward' omitted due to an unknown problem.")
  }
  if (isTRUE(kmeans) && !"k-Means" %in% obj$modularity$method) {
    warning("'kmeans' omitted due to an unknown problem.")
  }
  if (isTRUE(pam) && !"Partitioning around Medoids" %in% obj$modularity$method) {
    warning("'pam' omitted due to an unknown problem.")
  }
  if (isTRUE(equivalence) && !"Equivalence" %in% obj$modularity$method) {
    warning("'equivalence' omitted due to an unknown problem.")
  }
  if (isTRUE(concor_one) && !"CONCOR (One-Mode)" %in% obj$modularity$method && k %in% c(0, 2)) {
    warning("'concor_one' omitted due to an unknown problem.")
  }
  if (isTRUE(concor_two) && !"CONCOR (Two-Mode)" %in% obj$modularity$method && k %in% c(0, 2)) {
    warning("'concor_two' omitted due to an unknown problem.")
  }
  if (isTRUE(louvain) && !"Louvain" %in% obj$modularity$method && k < 2) {
    warning("'louvain' omitted due to an unknown problem.")
  }
  if (isTRUE(fastgreedy) && !"Fast & Greedy" %in% obj$modularity$method) {
    warning("'fastgreedy' omitted due to an unknown problem.")
  }
  if (isTRUE(walktrap) && !"Walktrap" %in% obj$modularity$method) {
    warning("'walktrap' omitted due to an unknown problem.")
  }
  if (isTRUE(leading_eigen) && !"Leading Eigenvector" %in% obj$modularity$method && k < 2) {
    warning("'leading_eigen' omitted due to an unknown problem.")
  }
  if (isTRUE(edge_betweenness) && !"Edge Betweenness" %in% obj$modularity$method) {
    warning("'edge_betweenness' omitted due to an unknown problem.")
  }
  if (isTRUE(infomap) && !"Infomap" %in% obj$modularity$method && k < 2) {
    warning("'infomap' omitted due to an unknown problem.")
  }
  if (isTRUE(label_prop) && !"Label Propagation" %in% obj$modularity$method && k < 2) {
    warning("'label_prop' omitted due to an unknown problem.")
  }
  if (isTRUE(spinglass) && !"Spinglass" %in% obj$modularity$method && k < 2) {
    warning("'spinglass' omitted due to an unknown problem.")
  }

  class(obj) <- "dna_multiclust"
  return(obj)
}

#' Print the summary of a \code{dna_multiclust} object
#'
#' Show details of a \code{dna_multiclust} object.
#'
#' Print abbreviated contents for the slots of a \code{dna_multiclust} object,
#' which can be created using the \link{dna_multiclust} function.
#'
#' @param x A \code{dna_multiclust} object.
#' @param ... Further options (currently not used).
#'
#' @author Philip Leifeld
#'
#' @rdname dna_multiclust
#' @importFrom utils head
#' @export
print.dna_multiclust <- function(x, ...) {
  cat(paste0("$k\n", x$k, "\n"))
  if ("cl" %in% names(x)) {
    cat(paste0("\n$cl\n", length(x$cl), " cluster object(s) embedded.\n"))
  }
  cat("\n$max_mod\n")
  print(utils::head(x$max_mod))
  if (nrow(x$max_mod) > 6) {
    cat(paste0("[... ", nrow(x$max_mod), " rows]\n"))
  }
  cat("\n$modularity\n")
  print(utils::head(x$modularity))
  if (nrow(x$modularity) > 6) {
    cat(paste0("[... ", nrow(x$modularity), " rows]\n"))
  }
  cat("\n$memberships\n")
  print(utils::head(x$memberships))
  if (nrow(x$memberships) > 6) {
    cat(paste0("[... ", nrow(x$memberships), " rows]\n"))
  }
}