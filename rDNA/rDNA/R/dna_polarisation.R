dna_polarisation <- function(statementType = "DNA Statement",
                             variable1 = "organization",
                             variable1Document = FALSE,
                             variable2 = "concept",
                             variable2Document = FALSE,
                             qualifier = "agreement",
                             normalization = "average",
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
                             qualityFunction = "modularity",
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
    qualifier <- .jnull(class = "java/lang/String")
  }
  
  # call rNetwork function to compute results
  polarisationObject <- .jcall(dna_getHeadlessDna(),
                               "Ljava/util/ArrayList;",
                               "rPolarisation",
                               statementType,
                               variable1,
                               variable1Document,
                               variable2,
                               variable2Document,
                               qualifier,
                               normalization,
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
                               .jlong(randomSeed)
  )
  
  l <- list()
  for (i in 0:(polarisationObject$size() - 1)) {
    l[[i + 1]] <- list()
    
    start_year <- .jcall(polarisationObject$get(as.integer(i))$getStart(), "I", "getYear")
    start_month <- .jcall(polarisationObject$get(as.integer(i))$getStart(), "I", "getMonthValue")
    start_day <- .jcall(polarisationObject$get(as.integer(i))$getStart(), "I", "getDayOfMonth")
    start_hour <- .jcall(polarisationObject$get(as.integer(i))$getStart(), "I", "getHour")
    start_minute <- .jcall(polarisationObject$get(as.integer(i))$getStart(), "I", "getMinute")
    start_second <- .jcall(polarisationObject$get(as.integer(i))$getStart(), "I", "getSecond")
    l[[i + 1]]$start <- ISOdatetime(start_year, start_month, start_day, start_hour, start_minute, start_second, tz = "UTC")
    
    middle_year <- .jcall(polarisationObject$get(as.integer(i))$getMiddle(), "I", "getYear")
    middle_month <- .jcall(polarisationObject$get(as.integer(i))$getMiddle(), "I", "getMonthValue")
    middle_day <- .jcall(polarisationObject$get(as.integer(i))$getMiddle(), "I", "getDayOfMonth")
    middle_hour <- .jcall(polarisationObject$get(as.integer(i))$getMiddle(), "I", "getHour")
    middle_minute <- .jcall(polarisationObject$get(as.integer(i))$getMiddle(), "I", "getMinute")
    middle_second <- .jcall(polarisationObject$get(as.integer(i))$getMiddle(), "I", "getSecond")
    l[[i + 1]]$middle <- ISOdatetime(middle_year, middle_month, middle_day, middle_hour, middle_minute, middle_second, tz = "UTC")
    
    stop_year <- .jcall(polarisationObject$get(as.integer(i))$getStop(), "I", "getYear")
    stop_month <- .jcall(polarisationObject$get(as.integer(i))$getStop(), "I", "getMonthValue")
    stop_day <- .jcall(polarisationObject$get(as.integer(i))$getStop(), "I", "getDayOfMonth")
    stop_hour <- .jcall(polarisationObject$get(as.integer(i))$getStop(), "I", "getHour")
    stop_minute <- .jcall(polarisationObject$get(as.integer(i))$getStop(), "I", "getMinute")
    stop_second <- .jcall(polarisationObject$get(as.integer(i))$getStop(), "I", "getSecond")
    l[[i + 1]]$stop <- ISOdatetime(stop_year, stop_month, stop_day, stop_hour, stop_minute, stop_second, tz = "UTC")
    
    l[[i + 1]]$memberships <- .jcall(polarisationObject$get(as.integer(i)), "[I", "getMemberships")
    l[[i + 1]]$labels <- .jcall(polarisationObject$get(as.integer(i)), "[S", "getNames")
    l[[i + 1]]$maxQ <- .jcall(polarisationObject$get(as.integer(i)), "[D", "getMaxQ")
    l[[i + 1]]$avgQ <- .jcall(polarisationObject$get(as.integer(i)), "[D", "getAvgQ")
    l[[i + 1]]$sdQ <- .jcall(polarisationObject$get(as.integer(i)), "[D", "getSdQ")
  }
  return(l)
}