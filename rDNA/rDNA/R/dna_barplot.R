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
#' @rdname dna_barplot
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
              "Ldna/export/BarplotResult;",
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
#' @rdname dna_barplot
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
#' @param ... Additional arguments; currently not in use.
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
#' @rdname dna_barplot
#' @importFrom ggplot2 autoplot
#' @importFrom ggplot2 ggplot
#' @importFrom ggplot2 aes
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
#' @importFrom rlang .data
#' @export
autoplot.dna_barplot <- function(object,
                                 ...,
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
                       ggplot2::aes(x = .data[["entity"]],
                                    y = .data[["frequency"]],
                                    fill = .data[["agreement"]],
                                    group = .data[["agreement"]],
                                    label = .data[["label"]]))
  if (binary) { # Bars for the binary case
    g <- g + ggplot2::geom_bar(ggplot2::aes(fill = .data[["color"]],
                                            color = .data[["text_color"]]),
                               stat = "identity",
                               width = barWidth,
                               show.legend = FALSE)
    # For the integer case with positive and negative values
  } else if (max(w) > 0 & min(w) < 0) {
    g <- g + ggplot2::geom_bar(ggplot2::aes(fill = .data[["color"]],
                                            color = .data[["text_color"]]),
                               stat = "identity",
                               width = barWidth,
                               show.legend = FALSE,
                               data = object2[as.numeric(as.character(object2$agreement)) >= 0, ],
                               position = ggplot2::position_stack(reverse = TRUE)) +
      ggplot2::geom_bar(ggplot2::aes(fill = .data[["color"]],
                                     color = .data[["text_color"]]),
                        stat = "identity",
                        width = barWidth,
                        show.legend = FALSE,
                        data = object2[as.numeric(as.character(object2$agreement)) < 0, ])
    # For the integer case with positive values only
  } else if (min(w) >= 0) {
    g <- g + ggplot2::geom_bar(ggplot2::aes(fill = .data[["color"]],
                                            color = .data[["text_color"]]),
                               stat = "identity",
                               width = barWidth,
                               show.legend = FALSE,
                               position = ggplot2::position_stack(reverse = TRUE))
    # For the integer case with negative values only
  } else {
    g <- g + ggplot2::geom_bar(ggplot2::aes(fill = .data[["color"]],
                                            color = .data[["text_color"]]),
                               stat = "identity",
                               width = barWidth,
                               show.legend = FALSE)
  }
  g <- g + ggplot2::coord_flip() +
    ggplot2::theme_minimal() +
    # Add intercept line
    ggplot2::geom_line(ggplot2::aes(x = .data[["x"]], y = .data[["y"]]),
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
      ggplot2::geom_text(ggplot2::aes(x = .data[["entity"]],
                                      y = .data[["pos"]],
                                      label = .data[["label"]]),
                         size = (fontSize / ggplot2::.pt),
                         inherit.aes = FALSE,
                         data = object2)
    # Add entity labels for integer case with positive and negative values
  } else if (max(w) > 0 & min(w) < 0) {
    g <- g +
      ggplot2::geom_text(ggplot2::aes(color = .data[["text_color"]]),
                         size = (fontSize / ggplot2::.pt),
                         position = ggplot2::position_stack(vjust = 0.5, reverse = TRUE),
                         inherit.aes = TRUE,
                         data = object2[object2$frequency >= 0, ]) +
      ggplot2::geom_text(ggplot2::aes(color = .data[["text_color"]]),
                         size = (fontSize / ggplot2::.pt),
                         position = ggplot2::position_stack(vjust = 0.5),
                         inherit.aes = TRUE,
                         data = object2[object2$frequency < 0, ])
    # Add entity labels for integer case with positive values only
  } else if (min(w) >= 0) {
    g <- g +
      ggplot2::geom_text(ggplot2::aes(color = .data[["text_color"]]),
                         size = (fontSize / ggplot2::.pt),
                         position = ggplot2::position_stack(vjust = 0.5, reverse = TRUE),
                         inherit.aes = TRUE)
  } else {
    g <- g +
      ggplot2::geom_text(ggplot2::aes(color = .data[["text_color"]]),
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