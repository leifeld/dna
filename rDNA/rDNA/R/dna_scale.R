#' Ideological scaling for discourse networks using item response theory
#'
#' One- or two-dimensional binary or ordinal IRT scaling for DNA.
#'
#' @section Overview:
#' This set of functions applies item response theory (IRT) to discourse
#' networks to scale actors and concepts on an underlying ideological scale.
#' Four estimation functions are available:
#' \itemize{
#'   \item \code{dna_scale1dbin} Binary scaling in one dimension.
#'   \item \code{dna_scale2dbin} Binary scaling in two dimensions.
#'   \item \code{dna_scale1dord} Ordinal scaling in one dimension.
#'   \item \code{dna_scale2dord} Ordinal scaling in two dimensions.
#' }
#' These functions are convenience wrappers for the \code{MCMCirt1d},
#' \code{MCMCirtKd}, and \code{MCMCordfactanal} functions in the MCMCpack
#' package, which use Markov Chain Monte Carlo (MCMC) methods to generate
#' posterior samples of ability, discrimination, and difficulty parameters.
#'
#' The corresponding \code{print} function prints the formatted posterior means
#' and HPD intervals to the console. The autoplot function plots the MCMC trace,
#' density, and summaries of estimates with the help of the \pkg{ggplot2}
#' package.
#'
#' @section Model interpretation:
#' One parameter for each actor and two parameters for each concept are
#' estimated:
#' \itemize{
#'   \item Ability parameter: The estimated ability parameter indicates an
#'     actor's ideological position on a left-right scale (e.g., industry vs.
#'     environment, labor vs. capital, containing vs. expanding regulation etc.)
#'     or on two dimensions. It is denoted as theta (\eqn{\theta}) in the binary
#'     model and phi (\eqn{\phi}) in the ordinal model in this implementation.
#'   \item Discrimination parameter: The estimated  discrimination parameter
#'     indicates a concept's ability to discriminate between actors on their
#'     left-right scale. The discrimination parameter measures how well an item
#'     can distinguish between actors with different levels of the latent trait
#'     (in this case, ideology). It reflects how strongly the item is related
#'     to the underlying trait being measured. A high discrimination parameter
#'     means that the item is very effective at differentiating between
#'     individuals who have slightly different levels of the underlying
#'     ideology. For instance, a highly discriminatory question will sharply
#'     differentiate between actors on either side of an ideological spectrum
#'     (e.g., conservative vs. liberal). A higher discrimination value indicates
#'     that the item is more sensitive to changes in the latent trait
#'     (ideology). A low discrimination value suggests that the item does not
#'     differentiate well between actors with different levels of ideology. In
#'     the notation used in this implementation, the discrimination parameter is
#'     denoted as beta (\eqn{\beta}) in the binary model and Lambda 2
#'     (\eqn{\Lambda_2}) in the ordinal model, though elsewhere in the literature
#'     it is often denoted as alpha (\eqn{\alpha}).
#'   \item Difficulty parameter: The difficulty parameter for a concept
#'     represents the location of the item along the ideological spectrum. The
#'     difficulty parameter helps determine where on the ideological spectrum a
#'     particular item is situated. For instance, an item with a high difficulty
#'     parameter might represent a position that only those with a
#'     strong ideological stance (e.g., very liberal or very conservative) are
#'     likely to endorse. An item with a low difficulty parameter would be
#'     endorsed by most actors, indicating that the statement is relatively easy
#'     to agree with (possibly a moderate or widely accepted stance).
#'     Conversely, an item with a high difficulty parameter would only be
#'     endorsed by those with a more extreme position on the ideology being
#'     measured. In the notation used in this implementation, the difficulty
#'     parameter is denoted as alpha (\eqn{\alpha}) in the binary model and as
#'     Lambda 1 (\eqn{\Lambda_1}) in the ordinal model, though elsewhere in the
#'     literature it is often denoted as beta (\eqn{\beta}).
#' }
#' See the help pages of the \code{MCMCirt1d} function (for the binary model)
#' and the \code{MCMCordfactanal} (for the ordinal model) for details on the
#' functional form and parameterization of the mdoel, which is a slight
#' deviation from the standard 2PL model.
#'
#' @section Variable coding:
#' As in a two-mode network in \link{dna_network}, two variables have to be
#' provided for the scaling. The first variable corresponds to the rows of a
#' two-mode network and usually entails actors (e.g., \code{"organizations"}),
#' while the second variable is equal to the columns of a two-mode network,
#' typically expressed by \code{"concepts"}. The \code{dna_scale} functions
#' use \code{"actors"} and \code{"concepts"} as synonyms for \code{variable1}
#' and \code{variable2}. However, the scaling is not restricted to
#' \code{"actors"} and \code{"concepts"} but depends on what you provide in
#' \code{variable1} or \code{variable2}.
#'
#' @section Binary models:
#' Binary models recode two-mode network matrices into zeroes and ones and then
#' estimate a logit model for binary data. The network cells are recoded using
#' the following rules:
#' \itemize{
#'   \item For a binary qualifier, \code{dna_scale1dbin} internally uses the
#'     \code{combine} qualifier aggregation and then recodes the values into
#'     \code{0} for disagreement, \code{1} for agreement, and \code{NA} for
#'     mixed positions and non-mentions of concepts. If
#'     \code{zero_as_na = FALSE} is set, the mixed positions and non-mentions
#'     become \code{0} instead of \code{NA} and are treated as informative.
#'   \item If no qualifier is used or the qualifier variable is categorical,
#'     non-mentions become \code{0} and any number of mentions become \code{1}.
#'     \code{zero_as_na} must be \code{FALSE} in this case.
#'   \item If a threshold is used (e.g., \code{0.4}), the fraction of positive
#'     mentions over the sum of both positive and negative mentions, which
#'     scales between \code{0} and \code{1}, is used to recode fractions smaller
#'     than or equal to the threshold as \code{0}, values larger than or equal
#'     to one minus the threshold as \code{1}, and values between the threshold
#'     and one minus the threshold as \code{NA}. For example, if an actor
#'     mentions a concept six times in a positive way and five times in a
#'     negative way, the fraction of positive mentions is \code{6 / 11 = 0.54}.
#'     If a threshold of \code{0.4} is used (or, equivalently, \code{0.6}), the
#'     value is recoded to \code{NA}. If no threshold is used, the value becomes
#'     \code{1}.
#'   \item Integer qualifiers are also recoded into \code{0} and \code{1} by
#'     rescaling the qualifier values between \code{0} and \code{1}. Thresholds
#'     larger than \code{0} and smaller than \code{1} are possible here as well.
#' }
#'
#' @section Ordinal models:
#' Ordinal models recode two-mode network matrices into values \code{1},
#' \code{2}, or \code{3} and then estimate an ordinal latent factor model for
#' ordinal data, which corresponds to ordinal item response theory. The network
#' cells are recoded using the following rules:
#' \itemize{
#'   \item For a binary qualifier, \code{dna_scale1dord} internally uses the
#'     \code{combine} qualifier aggregation and then recodes the values into
#'     \code{1} for disagreement, \code{2} for ambivalent positions with both
#'     positive and negative mentions, \code{3} for exclusively positive
#'     mentions, and \code{NA} for non-mentions. If \code{zero_as_na = FALSE} is
#'     set, non-mentions are recoded as \code{2} as well and thereby become
#'     informative as neutral positions.
#'   \item If no qualifier is used or the qualifier variable is categorical,
#'     non-mentions become \code{0} and any number of mentions become \code{1}.
#'     \code{zero_as_na} must be \code{FALSE} in this case.
#'   \item If a threshold is used, the same recoding procedure as in the binary
#'     model is used, but values below or equal to the threshold become
#'     \code{1}, values above the threshold and below one minus the threshold
#'     become \code{2}, values equal to or above the threshold become \code{3},
#'     and non-mentions are coded as \code{NA} (unless
#'     \code{zero_as_na = FALSE}).
#'   \item Integer qualifiers are also recoded into three positive integer
#'     values by rescaling the qualifier values between \code{0} and \code{1}.
#'     Thresholds larger than \code{0} and smaller than \code{1} are possible
#'     here as well.
#' }
#' In ordinal models, threshold parameters are estimated alongside the other
#' parameters, like in other ordinal logit models. They are treated as nuisance
#' parameters and not reported in the output. However, they are stored in the
#' object as part of the posterior samples and can be retrieved if necessary.
#'
#' @section Tweaking the estimation:
#' As these functions implement a Bayesian Item Response Theory approach,
#' \code{priors} and \code{starting values} can be set on the actor and concept
#' parameters. Changing the default \code{prior} values can often help you to
#' achieve better results. Constraints on the actor parameters can also be
#' specified to help identifying the model and to indicate in which direction
#' ideological positions of actors and concepts run. The returned MCMC output
#' can also be post-processed by normalizing the samples for each iteration with
#' \code{mcmc_normalize}. Normalization can be a sufficient way of identifying
#' one-dimensional ideal point models.
#'
#' Unlike \link{dna_scale1dbin}, \link{dna_scale2dbin} constrains the values
#' indicated in \code{variable2}. For these values, the scaling estimates an
#' item discrimination parameter for each dimension and an item difficulty
#' parameter for both dimensions. The item difficulty parameter should,
#' however, not be constrained (see \link[MCMCpack]{MCMCirtKd}). Therefore, you
#' should set constraints on the item discrimination parameters.
#'
#' Fitting two-dimensional scaling models requires a good choice of concept
#' constraints to specify the ideological dimensions of your data. A suitable
#' way of identifying your ideological dimensions is to constrain one item
#' discrimination parameter to load only on one dimension. This means that we
#' set one parameter to load either positive or negative on one dimension and
#' setting it to zero on the other. A second concept should also be constrained
#' to load either positive or negative on one dimension (see example).
#'
#' The argument \code{drop_min_actors} excludes actors with only a limited
#' number of concepts used. Limited participation of actors in a debate can
#' impact the scaling of the ideal points, as actors with only few mentions of
#' concepts convey limited information on their ideological position. The same
#' can also be done for concepts with the argument \code{drop_min_concepts}.
#' Concepts that have been rarely mentioned do not strongly discriminate the
#' ideological positions of actors and can, therefore, impact the accuracy of
#' the scaling. Reducing the number of actors of concepts to be scaled hence
#' improves the precision of the ideological positions for both variables and
#' the scaling itself. Another possibility to reduce the number of concepts is
#' to use \code{drop_constant_concepts}, which will reduce concepts not having
#' any variation in the agreement/disagreement structure of actors. This means
#' that all concepts will be dropped which have only agreeing or disagreeing
#' statements.
#'
#' @param statementType The statement type as a character object.
#' @param variable1 The first variable for the scaling construction (see
#'   \link{dna_network}). Defaults to \code{"organization"}.
#' @param variable2 The second variable for the scaling construction (see
#'   \link{dna_network}). Defaults to \code{"concept"}.
#' @param qualifier The qualifier variable for the scaling construction (see
#'   \link{dna_network}). Defaults to \code{"agreement"}. If you did not use
#'   a qualifier in the coding, you can set the qualifier to be the same value
#'   as variable2 and estimate an ordinal model with argument
#'   \code{zero_as_na = FALSE} (because non-mentions of concepts are interpreted
#'   as \code{NA} in the binary model).
#' @param zero_as_na Logical. Only ordinal models. If \code{TRUE}, all
#'   non-mentions of an actor towards a concept will be recoded as \code{NA}. If
#'   \code{FALSE} as \code{2}.
#' @param threshold Numeric value that specifies when a mixed position can be
#'   considered as agreement or disagreement. If, for example, one actor has 60
#'   percent of agreeing and 40 percent of disagreeing statements towards a
#'   concept, a \code{threshold} of 0.51 will recode the actor position on this
#'   concept as "agreement". The same accounts also for disagreeing statements.
#'   If one actor has 60 percent of disagreeing and 40 percent of agreeing
#'   statements, a \code{threshold} of 0.51 will recode the actor position on
#'   this concept as "disagreement". All values in between the \code{threshold}
#'   (e.g., 55 percent agreement and 45 percent of disagreement and a threshold
#'   of 0.6) will be recoded as \code{NA}. If is set to \code{NULL}, all "mixed"
#'   positions of actors will be recoded as \code{NA}. Must be strictly
#'   positive.
#' @param theta_constraints A list specifying the constraints on the actor
#'   parameter in a one-dimensional binary model. Three forms of constraints are
#'   possible: \code{actorname = value}, which will constrain an actor to be
#'   equal to the specified value (e.g. \code{0}), \code{actorname = "+"}, which
#'   will constrain the actor to be positively scaled and
#'   \code{actorname = "-"}, which will constrain the actor to be negatively
#'   scaled (see example).
#' @param lambda_constraints A list of lists specifying constraints on the
#'   concept parameters in an ordinal model. Note that value \code{1} in the
#'   brackets of the argument refers to the negative item difficulty parameters,
#'   which in general should not be constrained. Value \code{2} relates to the
#'   item discrimination parameter and should be used for constraints on
#'   concepts. Three forms of constraints are possible:
#'   \code{conceptname = list(2, value)} will constrain the item discrimination
#'   parameter to be equal to the specified value (e.g., 0).
#'   \code{conceptname = list(2,"+")} will constrain the item discrimination
#'   parameter to be positively scaled and \code{conceptname = list(2, "-")}
#'   will constrain the parameter to be negatively scaled (see example).
#' @param item_constraints A list of lists specifying constraints on the
#'   concept parameters in a two-dimensional binary model. Note that value
#'   \code{1} in the brackets of the argument refers to the item difficulty
#'   parameters, which in general should not be constrained. All values above
#'   \code{1} relate to the item discrimination parameters on the single
#'   dimensions. These should be used for constraints on concepts. Three forms
#'   of constraints are possible: \code{conceptname = list(2, value)} will
#'   constrain a concept to be equal to the specified value (e.g., 0) on the
#'   first dimension of the item discrimination parameter.
#'   \code{conceptname = list(2,"+")} will constrain the concept to be
#'   positively scaled on the first dimension and
#'   \code{conceptname = list(2, "-")} will constrain the concept to be
#'   negatively scaled on the first dimension (see example). If you
#'   wish to constrain a concept on the second dimension, please indicate this
#'   with a \code{3} in the first position in the bracket.
#' @param mcmc_iterations The number of iterations for the sampler (not
#'   including the burn-in iterations, which are discarded) before thinning.
#' @param mcmc_burnin The number of burn-in iterations for the sampler, which
#'   are discarded.
#' @param mcmc_thin The thinning interval for the sampler. Iterations must be
#'   divisible by the thinning interval. The final number of samples retained in
#'   the output equals \code{(mcmc_iterations - mcmc_burnin) / mcmc_thin}.
#' @param mcmc_tune Only ordinal models. The tuning parameter for the acceptance
#'   rates of the sampler. Acceptance rates should ideally range between
#'   \code{0.15} and \code{0.5}. Can be either a scalar or a k-vector. Must be
#'   strictly positive.
#' @param mcmc_normalize Logical. Should the MCMC output be normalized? If
#'   \code{TRUE}, samples are normalized to a mean of \code{0} and a standard
#'   deviation of \code{1}.
#' @param theta_start The starting values for the actor parameters in a
#'   one-dimensional binary model. Can either be a scalar or a column vector
#'   with as many elements as the number of actors included in the scaling. If
#'   set to the default \code{NA}, starting values will be set according to an
#'   eigenvalue-eigenvector decomposition of the actor agreement score.
#' @param alpha_start The starting values for the concept difficulty
#'   parameters in a one-dimensional binary model. Can either be a scalar or a
#'   column vector with as many elements as the number of items included in the
#'   scaling. If set to the default \code{NA}, starting values will be set
#'   according to a series of probit regressions that condition the starting
#'   values of the difficulty parameters.
#' @param beta_start The starting values for the concept discrimination
#'   parameters in a one-dimensional binary model. Can either be a scalar or a
#'   column vector with as many elements as the number of items included in the
#'   scaling. If set to the default \code{NA}, starting values will be set
#'   according to a series of probit regressions that condition the starting
#'   values of the discrimination parameters.
#' @param alpha_beta_start The starting values for the concept difficulty and
#'   discrimination parameters in a two-dimensional binary model. Can either be
#'   a scalar or a column vector with as many elements as the number of items
#'   included in the scaling. If set to the default \code{NA}, starting values
#'   will be set according to a series of probit regressions that condition the
#'   starting values of the difficulty and discrimination parameters.
#' @param lambda_start The starting values for the concept discrimination
#'   parameters in an ordinal model. Can be either a scalar or a matrix. If set
#'   to \code{NA} (default), the \code{starting values} for the unconstrained
#'   parameters in the first column are based on the observed response pattern.
#'   The remaining unconstrained elements are set to \code{starting values} of
#'   either \code{1.0} or \code{-1.0}, depending on the nature of the
#'   constraint.
#' @param theta_prior_mean A scalar value specifying the prior mean of the
#'   actor parameters in a one-dimensional binary model.
#' @param theta_prior_variance A scalar value specifying the prior inverse
#'   variances of the actor parameters in a one-dimensional binary model.
#' @param alpha_beta_prior_mean Mean of the difficulty and discrimination
#'   parameters in a one- or two-dimensional binary model. Can either be a
#'   scalar or a vector of length two. If a scalar, both means will be set
#'   according to the specified value.
#' @param alpha_beta_prior_variance Inverse variance of the difficulty and
#'   discrimination parameters in a one- or two-dimensional binary model. Can
#'   either be a scalar or a vector of length two. If a scalar, both means will
#'   be set according to the specified value.
#' @param lambda_prior_mean The prior mean of the concept discrimination
#'   parameters in an ordinal model. Can be either a scalar or a matrix.
#' @param lambda_prior_variance The prior inverse variances of the concept
#'   discrimination parameters in an ordinal model. Can be either a scalar or a
#'   matrix.
#' @param store_variables A character vector indicating which variables should
#'   be stored from the scaling. Can either take the value of the character
#'   vector indicated in \code{variable1} or \code{variable2} or \code{"both"}
#'   to store both variables. Note that saving both variables can impact the
#'   speed of the scaling. Defaults to \code{"both"}.
#' @param drop_constant_concepts Logical. Should concepts that have no
#'   variation be deleted before the scaling? Defaults to \code{FALSE}.
#' @param drop_min_actors A numeric value specifying the minimum number of
#'   concepts actors should have mentioned to be included in the scaling.
#'   Defaults to \code{1}.
#' @param drop_min_concepts A numeric value specifying the minimum number a
#'   concept should have been jointly mentioned by actors. Defaults to \code{2}.
#' @param verbose A boolean or numeric value indicating whether the iterations
#'   of the scaling should be printed to the R console. If set to a numeric
#'   value, every \code{verboseth} iteration will be printed. If set to
#'   \code{TRUE}, \code{verbose} will print the total of iterations and burn-in
#'   divided by \code{10}.
#' @param seed The random seed for the scaling.
#' @param ... Additional arguments passed to \link{dna_network}. Actors can, for
#'   example, be removed with the \code{excludeValues} arguments. The scaling
#'   can also be applied to a specific time slice by using \code{start.date} and
#'   \code{stop.date}. For the \code{autoplot} method, this argument is not in
#'   use.
#'
#' @examples
#' \dontrun{
#' library("rDNA")
#' library("ggplot2")
#' library("ggrepel")
#'
#' dna_init()
#' dna_openDatabase(dna_sample(overwrite = TRUE), coderPassword = "sample")
#'
#' # one-dimensional binary model
#' fit_1d_bin <- dna_scale1dbin(
#'   variable1 = "organization",
#'   variable2 = "concept",
#'   qualifier = "agreement",
#'   threshold = 0.49,
#'   theta_constraints = list(
#'     `National Petrochemical & Refiners Association` = "+",
#'     `Alliance to Save Energy` = "-"),
#'   mcmc_iterations = 20000,
#'   mcmc_burnin = 2000,
#'   mcmc_thin = 10,
#'   mcmc_normalize = TRUE,
#'   theta_prior_mean = 0,
#'   theta_prior_variance = 1,
#'   alpha_beta_prior_mean = 0,
#'   alpha_beta_prior_variance = 0.25,
#'   store_variables = "both",
#'   drop_constant_concepts = FALSE,
#'   drop_min_actors = 1,
#'   verbose = TRUE,
#'   seed = 12345
#' )
#' fit_1d_bin
#' autoplot(fit_1d_bin)
#'
#' # two-dimensional binary model
#' fit_2d_bin <- dna_scale2dbin(
#'   variable1 = "organization",
#'   variable2 = "concept",
#'   qualifier = "agreement",
#'   threshold = 0.4,
#'   item_constraints = list(
#'     `Climate change is caused by greenhouse gases (CO2).` = list(2, "-"),
#'     `Climate change is caused by greenhouse gases (CO2).` = c(3, 0),
#'     `CO2 legislation will not hurt the economy.` = list(3, "-")),
#'   mcmc_iterations = 20000,
#'   mcmc_burnin = 2000,
#'   mcmc_thin = 10,
#'   alpha_beta_prior_mean = 0,
#'   alpha_beta_prior_variance = 1,
#'   store_variables = "organization",
#'   drop_constant_concepts = TRUE,
#'   verbose = TRUE,
#'   seed = 12345
#' )
#' fit_2d_bin
#' autoplot(fit_2d_bin)
#'
#' # one-dimensional ordinal model
#' fit_1d_ord <- dna_scale1dord(
#'   variable1 = "organization",
#'   variable2 = "concept",
#'   qualifier = "agreement",
#'   zero_as_na = TRUE,
#'   threshold = 0.4,
#'   lambda_constraints = list(`CO2 legislation will not hurt the economy.` = list(2, "-")),
#'   mcmc_iterations = 20000,
#'   mcmc_burnin = 2000,
#'   mcmc_thin = 10,
#'   mcmc_tune = 1.5,
#'   mcmc_normalize = FALSE,
#'   lambda_prior_mean = 0,
#'   lambda_prior_variance = 0.1,
#'   store_variables = "organization",
#'   drop_constant_concepts = TRUE,
#'   verbose = TRUE,
#'   seed = 12345
#' )
#' fit_1d_ord
#' autoplot(fit_1d_ord)
#'
#' # two-dimensional ordinal model
#' fit_2d_ord <- dna_scale2dord(
#'   variable1 = "organization",
#'   variable2 = "concept",
#'   qualifier = "agreement",
#'   zero_as_na = TRUE,
#'   threshold = 0.4,
#'   lambda_constraints = list(
#'     `Climate change is caused by greenhouse gases (CO2).` = list(2, "-"),
#'     `Climate change is caused by greenhouse gases (CO2).` = list(3, 0),
#'     `CO2 legislation will not hurt the economy.` = list(3, "-")),
#'   mcmc_iterations = 20000,
#'   mcmc_burnin = 2000,
#'   mcmc_thin = 10,
#'   mcmc_tune = 1.5,
#'   lambda_prior_mean = 0,
#'   lambda_prior_variance = 0.1,
#'   store_variables = "both",
#'   drop_constant_concepts = TRUE,
#'   verbose = TRUE,
#'   seed = 12345
#' )
#' fit_2d_ord
#' autoplot(fit_2d_ord)
#' }
#'
#' @author Tim Henrichsen, Philip Leifeld, Johannes B. Gruber
#'
#' @rdname dna_scaling
#' @export
dna_scale1dbin <- function(statementType = "DNA Statement",
                           variable1 = "organization",
                           variable2 = "concept",
                           qualifier = "agreement",
                           zero_as_na = TRUE,
                           threshold = NULL,
                           theta_constraints = NULL,
                           mcmc_iterations = 20000,
                           mcmc_burnin = 1000,
                           mcmc_thin = 10,
                           mcmc_normalize = FALSE,
                           theta_start = NA,
                           alpha_start = NA,
                           beta_start = NA,
                           theta_prior_mean = 0,
                           theta_prior_variance = 1,
                           alpha_beta_prior_mean = 0,
                           alpha_beta_prior_variance = 0.25,
                           store_variables = "both",
                           drop_constant_concepts = FALSE,
                           drop_min_actors = 1,
                           drop_min_concepts = 2,
                           verbose = TRUE,
                           seed = 12345,
                           ...) {

  if (!requireNamespace("MCMCpack", quietly = TRUE)) {
    stop("The DNA scaling functions require the 'MCMCpack' package to be installed.\n",
         "To do this, enter 'install.packages(\"MCMCpack\")'.")
  }
  if (!requireNamespace("coda", quietly = TRUE)) {
    stop("The DNA scaling functions require the 'coda' package to be installed.\n",
         "To do this, enter 'install.packages(\"coda\")'.")
  }

  dots <- list(...)
  out <- bin_recode(statementType = statementType,
                    variable1 = variable1,
                    variable2 = variable2,
                    qualifier = qualifier,
                    zero_as_na = zero_as_na,
                    threshold = threshold,
                    drop_min_actors = drop_min_actors,
                    drop_min_concepts = drop_min_concepts,
                    store_variables = store_variables,
                    dots = dots)
  nw2 <- out$nw2
  dots <- out$dots
  dots_nw <- out$dots_nw
  invertValues <- out$invertValues
  excludeValues <- out$excludeValues
  # Scaling
  x <- do.call(eval(parse(text = "MCMCpack::MCMCirt1d")), c(list(
    nw2,
    theta.constraints = theta_constraints,
    burnin = mcmc_burnin,
    mcmc = mcmc_iterations,
    thin = mcmc_thin,
    verbose = ifelse(verbose == TRUE,
                     ((mcmc_iterations + mcmc_burnin) / 10),
                     verbose),
    seed = seed,
    theta.start = theta_start,
    alpha.start = alpha_start,
    beta.start = beta_start,
    t0 = theta_prior_mean,
    T0 = theta_prior_variance,
    ab0 = alpha_beta_prior_mean,
    AB0 = alpha_beta_prior_variance,
    store.item = (store_variables == variable2 | store_variables == "both"),
    store.ability = (store_variables == variable1 | store_variables == "both"),
    drop.constant.items = drop_constant_concepts),
    dots))
  if (mcmc_normalize) {
    names <- colnames(x)
    x <- coda::as.mcmc(t(apply(x, 1, scale)))
    colnames(x) <- names
  }
  dna_scale <- list()
  dna_scale$sample <- x
  # Store actor frequency for possible min argument in dna_plotScale
  nw_freq <- do.call("dna_network", c(list(networkType = "twomode",
                                           statementType = statementType,
                                           variable1 = variable1,
                                           variable2 = variable2,
                                           qualifier = qualifier,
                                           qualifierAggregation = "ignore",
                                           excludeValues = excludeValues,
                                           invertValues = invertValues),
                                      dots_nw))
  if (store_variables == variable1 | store_variables == "both") {
    actors <- x[, grepl("^theta.", colnames(x))]
    hpd <- as.data.frame(coda::HPDinterval(actors, prob = 0.95))
    actors <- as.data.frame(colMeans(actors))
    actors <- merge(actors, hpd, by = 0)
    colnames(actors)[colnames(actors) == "colMeans(actors)"] <- "mean"
    colnames(actors)[colnames(actors) == "lower"] <- "HPD2.5"
    colnames(actors)[colnames(actors) == "upper"] <- "HPD97.5"
    actors$Row.names <- gsub("^theta.", "", actors$Row.names)
    at <- dna_getAttributes(statementType = statementType,
                            variable = variable1)
    at <- at[at$value %in% actors$Row.names, ]
    at$frequency <- rowSums(nw_freq)[match(at$value, rownames(nw_freq))]
    actors <- merge(actors, at, by.x = "Row.names", by.y = "value")
    actors <- actors[, !(colnames(actors) == "ID")]
    dna_scale$ability <- actors
  }
  if (store_variables == variable2 | store_variables == "both") {
    at <- dna_getAttributes(statementType = statementType,
                            variable = variable2)
    concepts_beta <- x[, grepl("^beta.", colnames(x))] # beta = discrimination parameters
    hpd_beta <- as.data.frame(coda::HPDinterval(concepts_beta, prob = 0.95))
    concepts_beta <- as.data.frame(colMeans(concepts_beta))
    concepts_beta <- merge(concepts_beta, hpd_beta, by = 0)
    colnames(concepts_beta)[colnames(concepts_beta) == "colMeans(concepts_beta)"] <- "mean"
    colnames(concepts_beta)[colnames(concepts_beta) == "lower"] <- "HPD2.5"
    colnames(concepts_beta)[colnames(concepts_beta) == "upper"] <- "HPD97.5"
    concepts_beta$Row.names <- gsub("^beta.", "", concepts_beta$Row.names)
    at <- at[at$value %in% concepts_beta$Row.names, ]
    concepts_beta <- merge(concepts_beta, at, by.x = "Row.names", by.y = "value")
    concepts_beta <- concepts_beta[, !(colnames(concepts_beta) == "ID")]
    dna_scale$discrimination <- concepts_beta

    concepts_alpha <- x[, grepl("^alpha.", colnames(x))] # alpha = difficulty parameters
    hpd_alpha <- as.data.frame(coda::HPDinterval(concepts_alpha, prob = 0.95))
    concepts_alpha <- as.data.frame(colMeans(concepts_alpha))
    concepts_alpha <- merge(concepts_alpha, hpd_alpha, by = 0)
    colnames(concepts_alpha)[colnames(concepts_alpha) == "colMeans(concepts_alpha)"] <- "mean"
    colnames(concepts_alpha)[colnames(concepts_alpha) == "lower"] <- "HPD2.5"
    colnames(concepts_alpha)[colnames(concepts_alpha) == "upper"] <- "HPD97.5"
    concepts_alpha$Row.names <- gsub("^alpha.", "", concepts_alpha$Row.names)
    at <- at[at$value %in% concepts_alpha$Row.names, ]
    concepts_alpha <- merge(concepts_alpha, at, by.x = "Row.names", by.y = "value")
    concepts_alpha <- concepts_alpha[, !(colnames(concepts_alpha) == "ID")]
    dna_scale$difficulty <- concepts_alpha

    concepts <- rbind(concepts_beta, concepts_alpha)
  }
  dna_scale$call <- mget(names(formals()), sys.frame(sys.nframe()))
  dna_scale$call$connection <- NULL
  class(dna_scale) <- c("dna_scale1dbin", class(dna_scale))
  class(dna_scale) <- c("dna_scale", class(dna_scale))
  return(dna_scale)
}

#' @rdname dna_scaling
#' @export
dna_scale1dord <- function(statementType = "DNA Statement",
                           variable1 = "organization",
                           variable2 = "concept",
                           qualifier = "agreement",
                           zero_as_na = TRUE,
                           threshold = NULL,
                           lambda_constraints = NULL,
                           mcmc_iterations = 20000,
                           mcmc_burnin = 1000,
                           mcmc_thin = 10,
                           mcmc_tune = 1.5,
                           mcmc_normalize = FALSE,
                           lambda_start = NA,
                           lambda_prior_mean = 0,
                           lambda_prior_variance = 1,
                           store_variables = "both",
                           drop_constant_concepts = FALSE,
                           drop_min_actors = 1,
                           drop_min_concepts = 2,
                           verbose = TRUE,
                           seed = 12345,
                           ...) {

  if (!requireNamespace("MCMCpack", quietly = TRUE)) {
    stop("The DNA scaling functions require the 'MCMCpack' package to be installed.\n",
         "To do this, enter 'install.packages(\"MCMCpack\")'.")
  }
  if (!requireNamespace("coda", quietly = TRUE)) {
    stop("The DNA scaling functions require the 'coda' package to be installed.\n",
         "To do this, enter 'install.packages(\"coda\")'.")
  }

  dots <- list(...)
  out <- ord_recode(statementType = statementType,
                    variable1 = variable1,
                    variable2 = variable2,
                    qualifier = qualifier,
                    zero_as_na = zero_as_na,
                    threshold = threshold,
                    drop_min_actors = drop_min_actors,
                    drop_min_concepts = drop_min_concepts,
                    store_variables = store_variables,
                    dots = dots)
  nw2 <- out$nw2
  dots <- out$dots
  dots_nw <- out$dots_nw
  invertValues <- out$invertValues
  excludeValues <- out$excludeValues
  # Scaling
  x <- do.call(eval(parse(text = "MCMCpack::MCMCordfactanal")), c(list(
    nw2,
    factors = 1,
    lambda.constraints = lambda_constraints,
    burnin = mcmc_burnin,
    mcmc = mcmc_iterations,
    thin = mcmc_thin,
    tune = mcmc_tune,
    verbose = ifelse(verbose == TRUE,
                     ((mcmc_iterations + mcmc_burnin) / 10),
                     verbose),
    seed = seed,
    lambda.start = lambda_start,
    l0 = lambda_prior_mean,
    L0 = lambda_prior_variance,
    store.lambda = (store_variables == variable2 | store_variables == "both"),
    store.scores = (store_variables == variable1 | store_variables == "both"),
    drop.constantvars = drop_constant_concepts),
    dots))
  if (mcmc_normalize) {
    names <- colnames(x)
    x <- coda::as.mcmc(t(apply(x, 1, scale)))
    colnames(x) <- names
  }
  dna_scale <- list()
  dna_scale$sample <- x
  # Store actor frequency for possible min argument in dna_plotScale
  nw_freq <- do.call("dna_network", c(list(networkType = "twomode",
                                           statementType = statementType,
                                           variable1 = variable1,
                                           variable2 = variable2,
                                           qualifier = qualifier,
                                           qualifierAggregation = "ignore",
                                           excludeValues = excludeValues,
                                           invertValues = invertValues),
                                      dots_nw))
  if (store_variables == variable1 | store_variables == "both") {
    actors <- x[, grepl("^phi.", colnames(x))]
    hpd <- as.data.frame(coda::HPDinterval(actors, prob = 0.95))
    actors <- as.data.frame(colMeans(actors))
    actors <- merge(actors, hpd, by = 0)
    colnames(actors)[colnames(actors) == "colMeans(actors)"] <- "mean"
    colnames(actors)[colnames(actors) == "lower"] <- "HPD2.5"
    colnames(actors)[colnames(actors) == "upper"] <- "HPD97.5"
    actors$Row.names <- gsub("^phi.|.2$", "", actors$Row.names)
    at <- dna_getAttributes(statementType = statementType,
                            variable = variable1)
    at <- at[at$value %in% actors$Row.names, ]
    at$frequency <- rowSums(nw_freq)[match(at$value, rownames(nw_freq))]
    actors <- merge(actors, at, by.x = "Row.names", by.y = "value")
    actors <- actors[, !(colnames(actors) == "ID")]
    dna_scale$ability <- actors
  }
  if (store_variables == variable2 | store_variables == "both") {
    at <- dna_getAttributes(statementType = statementType, variable = variable2)
    concepts_lambda <- x[, grepl("^Lambda.", colnames(x))] # Lambda = difficulty and discrimination parameters
    hpd_lambda <- as.data.frame(coda::HPDinterval(concepts_lambda, prob = 0.95))
    concepts_lambda <- as.data.frame(colMeans(concepts_lambda))
    concepts_lambda <- merge(concepts_lambda, hpd_lambda, by = 0)
    colnames(concepts_lambda)[colnames(concepts_lambda) == "colMeans(concepts_lambda)"] <- "mean"
    colnames(concepts_lambda)[colnames(concepts_lambda) == "lower"] <- "HPD2.5"
    colnames(concepts_lambda)[colnames(concepts_lambda) == "upper"] <- "HPD97.5"

    concepts_difficulty <- concepts_lambda[grepl("\\.1$", concepts_lambda$Row.names), ] # lambda.1 = difficulty because multiplied by one (see first equation on MCMCordfactanal help page)
    concepts_difficulty$Row.names <- gsub("^Lambda|\\.1$", "", concepts_difficulty$Row.names)
    at_difficulty <- at[at$value %in% concepts_difficulty$Row.names, ]
    concepts_difficulty <- merge(concepts_difficulty, at_difficulty, by.x = "Row.names", by.y = "value")
    concepts_difficulty <- concepts_difficulty[, !(colnames(concepts_difficulty) == "ID")]
    dna_scale$difficulty <- concepts_difficulty

    concepts_discrimination <- concepts_lambda[grepl("\\.2$", concepts_lambda$Row.names), ] # lambda.2 = discrimination because multiplied by phi_i
    concepts_discrimination$Row.names <- gsub("^Lambda|\\.2$", "", concepts_discrimination$Row.names)
    at_discrimination <- at[at$value %in% concepts_discrimination$Row.names, ]
    concepts_discrimination <- merge(concepts_discrimination, at_discrimination, by.x = "Row.names", by.y = "value")
    concepts_discrimination <- concepts_discrimination[, !(colnames(concepts_discrimination) == "ID")]
    dna_scale$discrimination <- concepts_discrimination
  }
  dna_scale$call <- mget(names(formals()), sys.frame(sys.nframe()))
  dna_scale$call$connection <- NULL
  class(dna_scale) <- c("dna_scale1dord", class(dna_scale))
  class(dna_scale) <- c("dna_scale", class(dna_scale))
  return(dna_scale)
}

#' @rdname dna_scaling
#' @export
dna_scale2dbin <- function(statementType = "DNA Statement",
                           variable1 = "organization",
                           variable2 = "concept",
                           qualifier = "agreement",
                           zero_as_na = TRUE,
                           threshold = NULL,
                           item_constraints = NULL,
                           mcmc_iterations = 20000,
                           mcmc_burnin = 1000,
                           mcmc_thin = 10,
                           alpha_beta_start = NA,
                           alpha_beta_prior_mean = 0,
                           alpha_beta_prior_variance = 0.1,
                           store_variables = "both",
                           drop_constant_concepts = FALSE,
                           drop_min_actors = 1,
                           drop_min_concepts = 2,
                           verbose = TRUE,
                           seed = 12345,
                           ...) {

  if (!requireNamespace("MCMCpack", quietly = TRUE)) {
    stop("The DNA scaling functions require the 'MCMCpack' package to be installed.\n",
         "To do this, enter 'install.packages(\"MCMCpack\")'.")
  }
  if (!requireNamespace("coda", quietly = TRUE)) {
    stop("The DNA scaling functions require the 'coda' package to be installed.\n",
         "To do this, enter 'install.packages(\"coda\")'.")
  }

  dots <- list(...)
  out <- bin_recode(statementType = statementType,
                    dots = dots,
                    variable1 = variable1,
                    variable2 = variable2,
                    qualifier = qualifier,
                    zero_as_na = zero_as_na,
                    threshold = threshold,
                    drop_min_actors = drop_min_actors,
                    drop_min_concepts = drop_min_concepts,
                    store_variables = store_variables)
  nw2 <- out$nw2
  dots <- out$dots
  dots_nw <- out$dots_nw
  invertValues <- out$invertValues
  excludeValues <- out$excludeValues
  # Scaling
  x <- do.call(eval(parse(text = "MCMCpack::MCMCirtKd")), c(list(
    nw2,
    dimensions = 2,
    item.constraints = item_constraints,
    burnin = mcmc_burnin,
    mcmc = mcmc_iterations,
    thin = mcmc_thin,
    verbose = ifelse(verbose == TRUE,
                     ((mcmc_iterations + mcmc_burnin) / 10),
                     verbose),
    seed = seed,
    alphabeta.start = alpha_beta_start,
    b0 = alpha_beta_prior_mean,
    B0 = alpha_beta_prior_variance,
    store.item = (store_variables == variable2 | store_variables == "both"),
    store.ability = (store_variables == variable1 | store_variables == "both"),
    drop.constant.items = drop_constant_concepts),
    dots))
  dna_scale <- list()
  dna_scale$sample <- x
  # Store actor frequency for possible min argument in dna_plotScale
  nw_freq <- do.call("dna_network", c(list(networkType = "twomode",
                                           statementType = statementType,
                                           variable1 = variable1,
                                           variable2 = variable2,
                                           qualifier = qualifier,
                                           qualifierAggregation = "ignore",
                                           excludeValues = excludeValues,
                                           invertValues = invertValues),
                                      dots_nw))
  if (store_variables == variable1 | store_variables == "both") {
    actors <- x[, grepl("^theta.", colnames(x))]
    hpd <- as.data.frame(coda::HPDinterval(actors, prob = 0.95))
    actors <- as.data.frame(colMeans(actors))
    actors <- merge(actors, hpd, by = 0)
    colnames(actors)[colnames(actors) == "colMeans(actors)"] <- "mean"
    colnames(actors)[colnames(actors) == "lower"] <- "HPD2.5"
    colnames(actors)[colnames(actors) == "upper"] <- "HPD97.5"
    actors1 <- actors[grepl(".1$", actors$Row.names), drop = FALSE, ]
    actors1$Row.names <- gsub("^theta.|.1$", "", actors1$Row.names)
    actors2 <- actors[grepl(".2$", actors$Row.names), drop = FALSE, ]
    actors2$Row.names <- gsub("^theta.|.2$", "", actors2$Row.names)
    actors <- merge(actors1,
                    actors2,
                    by = "Row.names",
                    suffixes = c("_dim1", "_dim2"))
    at <- dna_getAttributes(statementType = statementType, variable = variable1)
    at <- at[at$value %in% actors$Row.names, ]
    at$frequency <- rowSums(nw_freq)[match(at$value, rownames(nw_freq))]
    actors <- merge(actors, at, by.x = "Row.names", by.y = "value")
    actors <- actors[, !(colnames(actors) == "ID")]
    dna_scale$ability <- actors
  }
  if (store_variables == variable2 | store_variables == "both") {
    at_alpha <- at_beta <- dna_getAttributes(statementType = statementType, variable = variable2)
    concepts_beta <- x[, grepl("^beta.", colnames(x))] # beta = discrimination parameters
    hpd_beta <- as.data.frame(coda::HPDinterval(concepts_beta, prob = 0.95))
    concepts_beta <- as.data.frame(colMeans(concepts_beta))
    concepts_beta <- merge(concepts_beta, hpd_beta, by = 0)
    colnames(concepts_beta)[colnames(concepts_beta) == "colMeans(concepts_beta)"] <- "mean"
    colnames(concepts_beta)[colnames(concepts_beta) == "lower"] <- "HPD2.5"
    colnames(concepts_beta)[colnames(concepts_beta) == "upper"] <- "HPD97.5"
    concepts_beta1 <- concepts_beta[grepl("\\.1$", concepts_beta$Row.names), drop = FALSE, ]
    concepts_beta1$Row.names <- gsub("^beta\\.|\\.1$", "", concepts_beta1$Row.names)
    concepts_beta2 <- concepts_beta[grepl("\\.2$", concepts_beta$Row.names), drop = FALSE, ]
    concepts_beta2$Row.names <- gsub("^beta\\.|\\.2$", "", concepts_beta2$Row.names)
    concepts_beta <- merge(concepts_beta1, concepts_beta2, by = "Row.names", suffixes = c("_dim1", "_dim2"))
    at_beta <- at_beta[at_beta$value %in% concepts_beta$Row.names, ]
    concepts_beta <- merge(concepts_beta, at_beta, by.x = "Row.names", by.y = "value")
    concepts_beta <- concepts_beta[, !(colnames(concepts_beta) == "ID")]
    dna_scale$discrimination <- concepts_beta

    concepts_alpha <- x[, grepl("^alpha.", colnames(x))] # alpha = difficulty parameters
    hpd_alpha <- as.data.frame(coda::HPDinterval(concepts_alpha, prob = 0.95))
    concepts_alpha <- as.data.frame(colMeans(concepts_alpha))
    concepts_alpha <- merge(concepts_alpha, hpd_alpha, by = 0)
    colnames(concepts_alpha)[colnames(concepts_alpha) == "colMeans(concepts_alpha)"] <- "mean"
    colnames(concepts_alpha)[colnames(concepts_alpha) == "lower"] <- "HPD2.5"
    colnames(concepts_alpha)[colnames(concepts_alpha) == "upper"] <- "HPD97.5"
    concepts_alpha$Row.names <- gsub("^alpha\\.", "", concepts_alpha$Row.names)
    at_alpha <- at_alpha[at_alpha$value %in% concepts_alpha$Row.names, ]
    concepts_alpha <- merge(concepts_alpha, at_alpha, by.x = "Row.names", by.y = "value")
    concepts_alpha <- concepts_alpha[, !(colnames(concepts_alpha) == "ID")]
    dna_scale$difficulty <- concepts_alpha
  }
  dna_scale$call <- mget(names(formals()), sys.frame(sys.nframe()))
  dna_scale$call$connection <- NULL
  class(dna_scale) <- c("dna_scale2dbin", class(dna_scale))
  class(dna_scale) <- c("dna_scale", class(dna_scale))
  return(dna_scale)
}

#' @rdname dna_scaling
#' @export
dna_scale2dord <- function(statementType = "DNA Statement",
                           variable1 = "organization",
                           variable2 = "concept",
                           qualifier = "agreement",
                           zero_as_na = TRUE,
                           threshold = NULL,
                           lambda_constraints = NULL,
                           mcmc_iterations = 20000,
                           mcmc_burnin = 1000,
                           mcmc_thin = 10,
                           mcmc_tune = 1.5,
                           lambda_start = NA,
                           lambda_prior_mean = 0,
                           lambda_prior_variance = 0.1,
                           store_variables = "both",
                           drop_constant_concepts = FALSE,
                           drop_min_actors = 1,
                           drop_min_concepts = 2,
                           verbose = TRUE,
                           seed = 12345,
                           ...) {

  if (!requireNamespace("MCMCpack", quietly = TRUE)) {
    stop("The DNA scaling functions require the 'MCMCpack' package to be installed.\n",
         "To do this, enter 'install.packages(\"MCMCpack\")'.")
  }
  if (!requireNamespace("coda", quietly = TRUE)) {
    stop("The DNA scaling functions require the 'coda' package to be installed.\n",
         "To do this, enter 'install.packages(\"coda\")'.")
  }

  dots <- list(...)
  out <- ord_recode(statementType = statementType,
                    variable1 = variable1,
                    variable2 = variable2,
                    qualifier = qualifier,
                    zero_as_na = zero_as_na,
                    threshold = threshold,
                    drop_min_actors = drop_min_actors,
                    drop_min_concepts = drop_min_concepts,
                    store_variables = store_variables,
                    dots = dots)
  nw2 <- out$nw2
  dots <- out$dots
  dots_nw <- out$dots_nw
  invertValues <- out$invertValues
  excludeValues <- out$excludeValues
  # Scaling
  x <- do.call(eval(parse(text = "MCMCpack::MCMCordfactanal")), c(list(
    nw2,
    factors = 2,
    lambda.constraints = lambda_constraints,
    burnin = mcmc_burnin,
    mcmc = mcmc_iterations,
    thin = mcmc_thin,
    tune = mcmc_tune,
    verbose = ifelse(verbose == TRUE,
                     ((mcmc_iterations + mcmc_burnin) / 10),
                     verbose),
    seed = seed,
    lambda.start = lambda_start,
    l0 = lambda_prior_mean,
    L0 = lambda_prior_variance,
    store.lambda = (store_variables == variable2 | store_variables == "both"),
    store.scores = (store_variables == variable1 | store_variables == "both"),
    drop.constantvars = drop_constant_concepts),
    dots))
  dna_scale <- list()
  dna_scale$sample <- x
  # Store actor frequency for possible min argument in dna_plotScale
  nw_freq <- do.call("dna_network", c(list(networkType = "twomode",
                                           statementType = statementType,
                                           variable1 = variable1,
                                           variable2 = variable2,
                                           qualifier = qualifier,
                                           qualifierAggregation = "ignore",
                                           excludeValues = excludeValues,
                                           invertValues = invertValues),
                                      dots_nw))
  if (store_variables == variable1 | store_variables == "both") {
    actors <- x[, grepl("^phi.", colnames(x))]
    hpd <- as.data.frame(coda::HPDinterval(actors, prob = 0.95))
    actors <- as.data.frame(colMeans(actors))
    actors <- merge(actors, hpd, by = 0)
    colnames(actors)[colnames(actors) == "colMeans(actors)"] <- "mean"
    colnames(actors)[colnames(actors) == "lower"] <- "HPD2.5"
    colnames(actors)[colnames(actors) == "upper"] <- "HPD97.5"
    actors1 <- actors[grepl(".2$", actors$Row.names), drop = FALSE, ]
    actors1$Row.names <- gsub("^phi.|.2$", "", actors1$Row.names)
    actors2 <- actors[grepl(".3$", actors$Row.names), drop = FALSE, ]
    actors2$Row.names <- gsub("^phi.|.3$", "", actors1$Row.names)
    actors <- merge(actors1,
                    actors2,
                    by = "Row.names",
                    suffixes = c("_dim1", "_dim2"))
    at <- dna_getAttributes(statementType = statementType,
                            variable = variable1)
    at <- at[at$value %in% actors$Row.names, ]
    at$frequency <- rowSums(nw_freq)[match(at$value, rownames(nw_freq))]
    actors <- merge(actors, at, by.x = "Row.names", by.y = "value")
    actors <- actors[, !(colnames(actors) == "ID")]
    dna_scale$ability <- actors
  }
  if (store_variables == variable2 | store_variables == "both") {
    at_lambda1 <- at_lambda23 <- dna_getAttributes(statementType = statementType, variable = variable2)
    concepts_lambda23 <- x[, grepl("^Lambda.+\\.(2|3)$", colnames(x))] # Lambda 2 = discrimination parameters, first dimension; Lambda 3 = second dimension
    hpd_lambda23 <- as.data.frame(coda::HPDinterval(concepts_lambda23, prob = 0.95))
    concepts_lambda23 <- as.data.frame(colMeans(concepts_lambda23))
    concepts_lambda23 <- merge(concepts_lambda23, hpd_lambda23, by = 0)
    colnames(concepts_lambda23)[colnames(concepts_lambda23) == "colMeans(concepts_lambda23)"] <- "mean"
    colnames(concepts_lambda23)[colnames(concepts_lambda23) == "lower"] <- "HPD2.5"
    colnames(concepts_lambda23)[colnames(concepts_lambda23) == "upper"] <- "HPD97.5"
    concepts_lambda2 <- concepts_lambda23[grepl("\\.2$", concepts_lambda23$Row.names), drop = FALSE, ]
    concepts_lambda2$Row.names <- gsub("^Lambda|\\.2$", "", concepts_lambda2$Row.names)
    concepts_lambda3 <- concepts_lambda23[grepl("\\.3$", concepts_lambda23$Row.names), drop = FALSE, ]
    concepts_lambda3$Row.names <- gsub("^Lambda|\\.3$", "", concepts_lambda3$Row.names)
    concepts_lambda23 <- merge(concepts_lambda2, concepts_lambda3, by = "Row.names", suffixes = c("_dim1", "_dim2"), all = TRUE)
    at_lambda23 <- at_lambda23[at_lambda23$value %in% concepts_lambda23$Row.names, ]
    concepts_lambda23 <- merge(concepts_lambda23, at_lambda23, by.x = "Row.names", by.y = "value")
    concepts_lambda23 <- concepts_lambda23[, !(colnames(concepts_lambda23) == "ID")]
    dna_scale$discrimination <- concepts_lambda23

    concepts_lambda1 <- x[, grepl("^Lambda.+\\.1$", colnames(x))] # Lambda 1 = difficulty parameters
    hpd_lambda1 <- as.data.frame(coda::HPDinterval(concepts_lambda1, prob = 0.95))
    concepts_lambda1 <- as.data.frame(colMeans(concepts_lambda1))
    concepts_lambda1 <- merge(concepts_lambda1, hpd_lambda1, by = 0)
    colnames(concepts_lambda1)[colnames(concepts_lambda1) == "colMeans(concepts_lambda1)"] <- "mean"
    colnames(concepts_lambda1)[colnames(concepts_lambda1) == "lower"] <- "HPD2.5"
    colnames(concepts_lambda1)[colnames(concepts_lambda1) == "upper"] <- "HPD97.5"
    concepts_lambda1$Row.names <- gsub("^Lambda|\\.1$", "", concepts_lambda1$Row.names)
    at_lambda1 <- at_lambda1[at_lambda1$value %in% concepts_lambda1$Row.names, ]
    concepts_lambda1 <- merge(concepts_lambda1, at_lambda1, by.x = "Row.names", by.y = "value")
    concepts_lambda1 <- concepts_lambda1[, !(colnames(concepts_lambda1) == "ID")]
    dna_scale$difficulty <- concepts_lambda1
  }
  dna_scale$call <- mget(names(formals()), sys.frame(sys.nframe()))
  dna_scale$call$connection <- NULL
  class(dna_scale) <- c("dna_scale2dord", class(dna_scale))
  class(dna_scale) <- c("dna_scale", class(dna_scale))
  return(dna_scale)
}

#' @noRd
bin_recode <- function(statementType,
                       variable1,
                       variable2,
                       qualifier,
                       zero_as_na,
                       threshold,
                       drop_min_actors,
                       drop_min_concepts,
                       store_variables,
                       dots) {
  if ("excludeValues" %in% names(dots)) {
    excludeValues <- dots["excludeValues"][[1]]
    dots["excludeValues"] <- NULL
  } else {
    excludeValues <- list()
  }
  if ("invertValues" %in% names(dots)) {
    invertValues <- dots["invertValues"][[1]]
    dots["invertValues"] <- NULL
  } else {
    invertValues <- FALSE
  }
  if ("normalization" %in% names(dots)) {
    dots["normalization"] <- NULL
    warning("'normalization' is not supported in dna_scale and will be ",
            "ignored.")
  }
  if ("qualifierAggregation" %in% names(dots)) {
    dots["qualifierAggregation"] <- NULL
    warning("'qualifierAggregation' is not supported in dna_scale and ",
            "will be ignored.")
  }
  if (any(names(formals(dna_network)) %in% names(dots))) {
    dots_nw <- dots[names(dots) %in% names(formals(dna_network))]
    dots[names(dots) %in% names(formals(dna_network))] <- NULL
  } else {
    dots_nw <- list()
  }
  if (!is.character(variable1) | !is.character(variable2)) {
    stop ("'variable1' and 'variable2' must be provided as character objects.")
  }
  if (!is.character(store_variables)) {
    stop ("'store_variables' must be provided as a character object.")
  }
  if (isTRUE(threshold > 1)) {
    threshold <- threshold / 100
  }
  if (!(store_variables == "both" |
        store_variables == variable1 |
        store_variables == variable2)) {
    stop ("'store_variables' does not match with 'variable1' or 'variable2'. ",
          "Please match 'store_variables' with variables in 'variable1' or ",
          "'variable2', or use \"both\" in case you want to store both ",
          "variables.")
  }
  # Check if non-binary structure in agreement
  nw <- do.call("dna_network", c(list(networkType = "eventlist",
                                      statementType = statementType,
                                      variable1 = variable1,
                                      variable2 = variable2,
                                      qualifier = qualifier,
                                      excludeValues = excludeValues,
                                      invertValues = invertValues),
                                 dots_nw))
  if ("character" %in% class(nw[, qualifier]) || !all(unique(nw[, qualifier])) %in% c(0, 1)) {
    nw <- do.call("dna_network", c(list(networkType = "twomode",
                                        statementType = statementType,
                                        variable1 = variable1,
                                        variable2 = variable2,
                                        qualifier = qualifier,
                                        qualifierAggregation = "ignore",
                                        excludeValues = excludeValues,
                                        invertValues = invertValues),
                                   dots_nw))
    nw2 <- as.matrix(nw)
    nw2[nw2 > 1] <- 1
    if (isTRUE(zero_as_na)) {
      zero_as_na <- FALSE
      warning("Setting 'zero_as_na' to FALSE because there are otherwise only 1s in the data matrix.")
    }
    if (!is.null(threshold)) {
      warning("'threshold' is not supported and will be ignored.")
    }
  } else {
    # retrieve data from network
    nw <- do.call("dna_network", c(list(networkType = "twomode",
                                        statementType = statementType,
                                        variable1 = variable1,
                                        variable2 = variable2,
                                        qualifier = qualifier,
                                        qualifierAggregation = "combine",
                                        excludeValues = excludeValues,
                                        invertValues = invertValues),
                                   dots_nw))
    if (is.null(threshold)) {
      # change structure of network according to scaling type
      nw2 <- nw
      if (isTRUE(zero_as_na)) {
        nw2[nw == 0 | nw == 3] <- NA
      } else {
        nw2[nw == 3] <- 0
      }
      nw2[nw == 2] <- 0
    } else {
      # Include threshold in export of network
      nw_pos <- do.call("dna_network", c(list(
        networkType = "twomode",
        statementType = statementType,
        variable1 = variable1,
        variable2 = variable2,
        qualifier = qualifier,
        qualifierAggregation = "ignore",
        isolates = TRUE,
        excludeValues = c(list("agreement" =
                                 ifelse(invertValues, 1, 0)),
                          excludeValues),
        invertValues = invertValues),
        dots_nw))
      nw_neg <- do.call("dna_network", c(list(
        networkType = "twomode",
        statementType = statementType,
        variable1 = variable1,
        variable2 = variable2,
        qualifier = qualifier,
        qualifierAggregation = "ignore",
        isolates = TRUE,
        excludeValues = c(list("agreement" =
                                 ifelse(invertValues, 0, 1)),
                          excludeValues),
        invertValues = invertValues),
        dots_nw))
      nw_com <- nw_pos / (nw_pos + nw_neg)
      nw2 <- nw_com
      if (threshold > 0.5) {
        threshold <- 1 - threshold
      }
      nw2[is.nan(nw_com)] <- NA
      nw2[nw_com > threshold & nw_com < 1 - threshold] <- NA
      if (isFALSE(zero_as_na)) {
        nw2[is.na(nw2)] <- 0
      }
      nw2[nw_com <= threshold] <- 0
      nw2[nw_com >= 1 - threshold] <- 1
      nw2 <- nw2[match(rownames(nw), rownames(nw2)),
                 match(colnames(nw), colnames(nw2))]
    }
  }
  if (isTRUE(drop_min_actors > 1) | isTRUE(drop_min_concepts > 1)) {
    nw_exclude <- nw2
    nw_exclude[nw_exclude == 0] <- 1
    nw_exclude[is.na(nw_exclude)] <- 0
    if (isTRUE(drop_min_actors > 1)) {
      if (drop_min_actors > max(rowSums(nw_exclude))) {
        stop(paste0("The specified number in 'drop_min_actors' is higher than ",
                    "the maximum number of concepts mentioned by an actor (",
                    max(rowSums(nw_exclude))), ").")
      }
      nw2 <- nw2[rowSums(nw_exclude) >= drop_min_actors, ]
    }
    if (isTRUE(drop_min_concepts > 1)) {
      if (drop_min_concepts > max(colSums(nw_exclude))) {
        stop(paste0("The specified number in 'drop_min_concepts' is higher ",
                    "than the maximum number of jointly mentioned concepts (",
                    max(colSums(nw_exclude))), ").")
      }
      nw2 <- nw2[, colSums(nw_exclude) >= drop_min_concepts]
    }
  }
  # Test if actor is without any statements
  filter_actor <- sapply(rownames(nw2), function(c) {
    !sum(is.na(nw2[c, ]) * 1) >= ncol(nw2)
  })
  # Test if only one concept used by actor
  filter_concept <- sapply(colnames(nw2), function(c) {
    !sum(is.na(nw2[, c]) * 1) >= nrow(nw2) - 1
  })
  nw2 <- nw2[filter_actor, filter_concept]
  if ("FALSE" %in% filter_concept) {
    if (drop_min_actors > 1 & drop_min_concepts >= 2) {
      warning("After deleting actors with 'drop_min_actors', some concepts ",
              "are now mentioned by less than the two required actors. The ",
              "follwing concepts have therefore not been included in the ",
              "scaling:\n",
              paste(names(filter_concept[filter_concept == FALSE]),
                    collapse = "\n"))
    } else {
      warning("dna_scale requires concepts mentioned by at least two actors. ",
              "The following concepts have therefore not been included in the ",
              "scaling:\n",
              paste(names(filter_concept[filter_concept == FALSE]),
                    collapse = "\n"))
    }
  }
  if ("FALSE" %in% filter_actor) {
    if (drop_min_concepts >= 1) {
      warning("After deleting concepts with 'drop_min_concepts', some actors ",
              "now have less than one statement. The following actors have ",
              "therefore not been included in the scaling:\n",
              paste(names(filter_actor[filter_actor == FALSE]),
                    collapse = "\n"))
    } else {
      warning("Some actors do not have any statements and were not included in",
              " the scaling. Setting or lowering the 'threshold' might include",
              " them:\n",
              paste(names(filter_actor[filter_actor == FALSE]),
                    collapse = "\n"))
    }
  }
  out <- list(nw2 = nw2,
              dots = dots,
              dots_nw = dots_nw,
              excludeValues = excludeValues,
              invertValues = invertValues)
  return(out)
}

#' @noRd
ord_recode <- function(statementType,
                       variable1,
                       variable2,
                       qualifier,
                       zero_as_na,
                       threshold,
                       drop_min_actors,
                       drop_min_concepts,
                       store_variables,
                       dots) {
  if ("excludeValues" %in% names(dots)) {
    excludeValues <- dots["excludeValues"][[1]]
    dots["excludeValues"] <- NULL
  } else {
    excludeValues <- list()
  }
  if ("invertValues" %in% names(dots)) {
    invertValues <- dots["invertValues"][[1]]
    dots["invertValues"] <- NULL
  } else {
    invertValues <- FALSE
  }
  if ("normalization" %in% names(dots)) {
    dots["normalization"] <- NULL
    warning("'normalization' is not supported in dna_scale and will be ",
            "ignored.")
  }
  if ("qualifierAggregation" %in% names(dots)) {
    dots["qualifierAggregation"] <- NULL
    warning("'qualifierAggregation' is not supported in dna_scale and will be ",
            "ignored.")
  }
  if (any(names(formals(dna_network)) %in% names(dots))) {
    dots_nw <- dots[names(dots) %in% names(formals(dna_network))]
    dots[names(dots) %in% names(formals(dna_network))] <- NULL
  } else {
    dots_nw <- list()
  }
  if (!is.character(variable1) | !is.character(variable2)) {
    stop ("'variable1' and 'variable2' must be provided as character objects.")
  }
  if (!is.character(store_variables)) {
    stop ("'store_variables' must be provided as a character object.")
  }
  if (isTRUE(threshold > 1)) {
    threshold <- threshold / 100
  }
  if (!(store_variables == "both" |
        store_variables == variable1 |
        store_variables == variable2)) {
    stop ("'store_variables' does not match with 'variable1' or 'variable2'. ",
          "Please match 'store_variables' with variables in 'variable1' or ",
          "'variable2', or use \"both\" in case you want to store both ",
          "variables.")
  }
  # Check if non-binary structure in agreement
  nw <- do.call("dna_network", c(list(networkType = "eventlist",
                                      statementType = statementType,
                                      variable1 = variable1,
                                      variable2 = variable2,
                                      qualifier = qualifier,
                                      excludeValues = excludeValues,
                                      invertValues = invertValues),
                                 dots_nw))
  if ("character" %in% class(nw[, qualifier]) || !!all(unique(nw[, qualifier])) %in% c(0, 1)) {
    nw <- do.call("dna_network", c(list(networkType = "twomode",
                                        statementType = statementType,
                                        variable1 = variable1,
                                        variable2 = variable2,
                                        qualifier = qualifier,
                                        qualifierAggregation = "ignore",
                                        excludeValues = excludeValues,
                                        invertValues = invertValues),
                                   dots_nw))
    nw2 <- nw
    nw2[nw > 1] <- 1
    if (zero_as_na == TRUE) {
      zero_as_na <- FALSE
      warning("Setting 'zero_as_na' to FALSE because there are otherwise only 1s in the data matrix.")
    }
    if (!is.null(threshold)) {
      threshold <- NULL
      warning("'threshold' is not supported and will be ignored.")
    }
  } else {
    # retrieve data from network
    nw <- do.call("dna_network", c(list(networkType = "twomode",
                                        statementType = statementType,
                                        variable1 = variable1,
                                        variable2 = variable2,
                                        qualifier = qualifier,
                                        qualifierAggregation = "combine",
                                        excludeValues = excludeValues,
                                        invertValues = invertValues),
                                   dots_nw))
    if (is.null(threshold)) {
      # change structure of network according to scaling type
      nw2 <- nw
      if (zero_as_na == TRUE) {
        nw2[nw == 0] <- NA
        nw2[nw == 1] <- 3
        nw2[nw == 2] <- 1
        nw2[nw == 3] <- 2
      } else if (zero_as_na == FALSE) {
        nw2[nw == 0] <- 2
        nw2[nw == 1] <- 3
        nw2[nw == 2] <- 1
        nw2[nw == 3] <- 2
      }
    } else {
      # Include threshold in export of network
      nw_pos <- do.call("dna_network", c(list(
        networkType = "twomode",
        statementType = statementType,
        variable1 = variable1,
        variable2 = variable2,
        qualifier = qualifier,
        qualifierAggregation = "ignore",
        isolates = TRUE,
        excludeValues = c(list("agreement" =
                                 ifelse(invertValues, 1, 0)),
                          excludeValues),
        invertValues = invertValues),
        dots_nw))
      nw_neg <- do.call("dna_network", c(list(
        networkType = "twomode",
        statementType = statementType,
        variable1 = variable1,
        variable2 = variable2,
        qualifier = qualifier,
        qualifierAggregation = "ignore",
        isolates = TRUE,
        excludeValues = c(list("agreement" =
                                 ifelse(invertValues, 0, 1)),
                          excludeValues),
        invertValues = invertValues),
        dots_nw))
      nw_com <- nw_pos / (nw_pos + nw_neg)
      nw2 <- nw_com
      if (threshold > 0.5) {
        threshold <- 1 - threshold
      }
      nw2[is.nan(nw_com)] <- NA
      nw2[nw_com < threshold & nw_com > 1 - threshold] <- 2
      nw2[nw_com <= -threshold] <- 1
      nw2[nw_com >= threshold] <- 3
      if (isFALSE(zero_as_na)) {
        nw2[is.na(nw2)] <- 2
      }
      nw2 <- nw2[match(rownames(nw), rownames(nw2)),
                 match(colnames(nw), colnames(nw2))]
    }
  }
  if (isTRUE(drop_min_actors > 1) | isTRUE(drop_min_concepts > 1)) {
    if (zero_as_na == FALSE) {
      if (is.null(threshold)) {
        nw_exclude <- nw
        nw_exclude[nw_exclude > 1] <- 1
      } else {
        nw_exclude <- nw_com
        nw_exclude[!is.nan(nw_exclude)] <- 1
        nw_exclude[is.nan(nw_exclude)] <- 0
      }
    } else {
      nw_exclude <- nw2
      nw_exclude[nw_exclude > 1] <- 1
      nw_exclude[is.na(nw_exclude)] <- 0
    }
    if (isTRUE(drop_min_actors > 1)) {
      if (drop_min_actors > max(rowSums(nw_exclude))) {
        stop(paste0("The specified number in 'drop_min_actors' is higher than ",
                    "the maximum number of concepts mentioned by an actor (",
                    max(rowSums(nw_exclude))), ").")
      }
      nw2 <- nw2[rowSums(nw_exclude) >= drop_min_actors, ]
    }
    if (isTRUE(drop_min_concepts > 1)) {
      if (drop_min_concepts > max(colSums(nw_exclude))) {
        stop(paste0("The specified number in 'drop_min_concepts' is higher ",
                    "than the maximum number of jointly mentioned concepts (",
                    max(colSums(nw_exclude))), ").")
      }
      nw2 <- nw2[, colSums(nw_exclude) >= drop_min_concepts]
    }
  }
  # Test if actor is without any statements
  filter_actor <- sapply(rownames(nw2), function(c) {
    !sum(is.na(nw2[c, ]) * 1) >= ncol(nw2)
  })
  # Test if only one concept used by actor
  filter_concept <- sapply(colnames(nw2), function(c) {
    !sum(is.na(nw2[, c]) * 1) >= nrow(nw2) - 1
  })
  nw2 <- nw2[filter_actor, filter_concept]
  if ("FALSE" %in% filter_concept) {
    if (drop_min_actors > 1 & drop_min_concepts >= 2) {
      warning("After deleting actors with 'drop_min_actors', some concepts ",
              "are now mentioned by less than the two required actors. The ",
              "follwing concepts have therefore not been included in the ",
              "scaling:\n",
              paste(names(filter_concept[filter_concept == FALSE]),
                    collapse = "\n"))
    } else {
      warning("dna_scale requires concepts mentioned by at least two actors. ",
              "The following concepts have therefore not been included in the ",
              "scaling:\n",
              paste(names(filter_concept[filter_concept == FALSE]),
                    collapse = "\n"))
    }
  }
  if ("FALSE" %in% filter_actor) {
    if (drop_min_concepts >= 1) {
      warning("After deleting concepts with 'drop_min_concepts', some actors ",
              "now have less than one statement. The following actors have ",
              "therefore not been included in the scaling:\n",
              paste(names(filter_actor[filter_actor == FALSE]),
                    collapse = "\n"))
    } else {
      warning("Some actors do not have any statements and were not included in",
              " the scaling. Setting or lowering the 'threshold' might include",
              " them:\n",
              paste(names(filter_actor[filter_actor == FALSE]),
                    collapse = "\n"))
    }
  }
  out <- list(nw2 = nw2,
              dots = dots,
              dots_nw = dots_nw,
              excludeValues = excludeValues,
              invertValues = invertValues)
  return(out)
}

#' @param x A \code{dna_scale} object.
#' @rdname dna_scaling
#' @export
print.dna_scale <- function(x, trim = 60, ...) {
  cat("Method:", class(x)[2], "\n")
  if (grepl("scale1d", class(x)[2])) {
    if ("ability" %in% names(x)) {
      cat(paste0("\nAbility parameters (variable '", x$call$variable1, "'):\n"))
      ability <- x$ability[, c("mean", "HPD2.5", "HPD97.5")]
      rownames(ability) <- sapply(x$ability$Row.names, function(r) if (nchar(r) > trim) paste0(substr(r, 1, trim - 1), "*") else r)
      ability <- ability[order(ability$mean, decreasing = TRUE), ]
      print(ability)
    }
    if ("discrimination" %in% names(x)) {
      cat(paste0("\nItem discrimination parameters (variable '", x$call$variable2, "'):\n"))
      discrimination <- x$discrimination[, c("mean", "HPD2.5", "HPD97.5")]
      rownames(discrimination) <- sapply(x$discrimination$Row.names, function(r) if (nchar(r) > trim) paste0(substr(r, 1, trim - 1), "*") else r)
      discrimination <- discrimination[order(discrimination$mean, decreasing = TRUE), ]
      print(discrimination)
    }
  } else {
    if ("ability" %in% names(x)) {
      cat(paste0("\nAbility parameters (variable '", x$call$variable1, "'):\n"))
      ability <- x$ability[, c("mean_dim1", "HPD2.5_dim1", "HPD97.5_dim1", "mean_dim2", "HPD2.5_dim2", "HPD97.5_dim2")]
      rownames(ability) <- sapply(x$ability$Row.names, function(r) if (nchar(r) > trim) paste0(substr(r, 1, trim - 1), "*") else r)
      ability <- ability[order(ability$mean_dim1, decreasing = TRUE), ]
      print(ability)
    }
    if ("discrimination" %in% names(x)) {
      cat(paste0("\nItem discrimination parameters (variable '", x$call$variable2, "'):\n"))
      discrimination <- x$discrimination[, c("mean_dim1", "HPD2.5_dim1", "HPD97.5_dim1", "mean_dim2", "HPD2.5_dim2", "HPD97.5_dim2")]
      rownames(discrimination) <- sapply(x$discrimination$Row.names, function(r) if (nchar(r) > trim) paste0(substr(r, 1, trim - 1), "*") else r)
      discrimination <- discrimination[order(discrimination$mean_dim1, decreasing = TRUE), ]
      print(discrimination)
    }
  }
  if ("difficulty" %in% names(x)) {
    cat(paste0("\nItem difficulty parameters (variable '", x$call$variable2, "'):\n"))
    difficulty <- x$difficulty[, c("mean", "HPD2.5", "HPD97.5")]
    rownames(difficulty) <- sapply(x$difficulty$Row.names, function(r) if (nchar(r) > trim) paste0(substr(r, 1, trim - 1), "*") else r)
    difficulty <- difficulty[order(difficulty$mean, decreasing = TRUE), ]
    print(difficulty)
  }
}

#' @param object A \code{dna_scale} object, created by one of the scaling functions.
#' @param type The type(s) of plot to generate. Must be one or more of the following:
#'   \itemize{
#'     \item \code{"trace"}: For creating MCMC trace plots.
#'     \item \code{"density"}: For creating MCMC density plots.
#'     \item \code{"scaling"}: For creating plots summarizing the estimated parameters.
#'   }
#' @param parameters The parameter type(s) to plot. Must be one or more of the following:
#'   \itemize{
#'     \item \code{"ability"}: The ability parameters for the actors, indicating their ideology.
#'     \item \code{"discrimination"}: The item discrimination parameters.
#'     \item \code{"difficulty"}: The item difficulty parameters.
#'   }
#' @param trim An integer defining the maximum length of the labels.
#' @param nrow An integer defining how many rows of parameters should be in a trace or density plot.
#' @param ncol An integer defining how many columns of parameters should be in a trace or density plot.
#' @rdname dna_scaling
#' @importFrom stats reorder reshape
#' @export
autoplot.dna_scale <- function(object,
                               ...,
                               type = c("trace", "density", "scaling"),
                               parameters = c("ability", "discrimination", "difficulty"),
                               trim = 40,
                               nrow = 5,
                               ncol = 3) {
  # ensure type and parameters are matched correctly
  type <- match.arg(type, several.ok = TRUE)
  parameters <- match.arg(parameters, c("ability", "discrimination", "difficulty"), several.ok = TRUE)

  # check for required packages
  if (any(grepl("^dna_scale2d", class(object))) && "scaling" %in% type && !requireNamespace("ggrepel", quietly = TRUE)) {
    stop("The 'ggrepel' package is required for plotting 2D scalings.\n",
         "To install it, enter 'install.packages(\"ggrepel\")'.")
  }

  # helper function to trim labels
  trim_label <- function(label, trim) {
    if (nchar(label) > trim) {
      if (grepl(".+\\.(1|2|3)$", label)) {
        return(paste0(substr(label, 1, trim - 3), "*", substr(label, nchar(label) - 1, nchar(label))))
      } else {
        return(paste0(substr(label, 1, trim - 1), "*"))
      }
    } else {
      return(label)
    }
  }

  # helper function to create trace or density plots
  create_diagnostic_plots <- function(samples, param_type, plot_type) {
    tp <- t(samples)
    tpdf <- as.data.frame(tp) # transposed parameters data frame := tpdf
    tpdf$Row.names <- tpdf$parameter <- rownames(tpdf)
    tpdf$Row.names <- gsub("((^theta\\.)|(^alpha\\.)|(^beta\\.)|(^phi\\.)|(^Lambda))|(\\.(1|2|3)$)", "", tpdf$Row.names)
    if (param_type == "ability") {
      at <- object$ability[, c("Row.names", "color")]
      tpdf <- tpdf[grepl("(^phi\\.)|(^theta\\.)", tpdf$parameter), ]
    } else {
      at <- object$discrimination[, c("Row.names", "color")]
      if (param_type == "discrimination") {
        tpdf <- tpdf[grepl("(^beta\\.)|(^Lambda.+\\.(2|3)$)", tpdf$parameter), ]
      } else if (param_type == "difficulty") {
        tpdf <- tpdf[grepl("(^alpha\\.)|(^Lambda.+\\.1$)", tpdf$parameter), ]
      }
    }
    tpdf <- merge(tpdf, at, all.x = TRUE, all.y = FALSE)
    if (any(grepl("1dord$", class(object))) || (any(grepl("2dord$", class(object))) && param_type == "difficulty")) {
      tpdf$parameter <- gsub("\\.(1|2)$", "", tpdf$parameter)
    } else if (any(grepl("2dord$", class(object))) && param_type %in% c("ability", "discrimination")) {
      tpdf$parameter <- gsub("\\.1$", ".0", tpdf$parameter)
      tpdf$parameter <- gsub("\\.2$", ".1", tpdf$parameter)
      tpdf$parameter <- gsub("\\.3$", ".2", tpdf$parameter)
    }
    tpdf$parameter <- gsub("(^theta\\.)|(^alpha\\.)|(^beta\\.)|(^phi\\.)|(^Lambda)", "", tpdf$parameter)
    tpdf$parameter <- sapply(tpdf$parameter, trim_label, trim = trim)
    tpdf_long <- stats::reshape(tpdf,
                                varying = list(colnames(tpdf)[2:(ncol(tpdf) - 2)]),
                                v.names = "value",
                                timevar = "variable",
                                times = colnames(tpdf)[2:(ncol(tpdf) - 2)],
                                direction = "long")
    rownames(tpdf_long) <- NULL
    tpdf_long$Iteration <- sort(rep(1:nrow(samples), nrow(tpdf)))
    tpdf_long <- tpdf_long[order(tpdf_long$parameter, tpdf_long$Iteration), -which(colnames(tpdf_long) %in% c("Row.names", "variable", "id"))]

    plots_needed <- ceiling(nrow(tpdf) / (nrow * ncol)) # maximum number of panels per plot in denominator
    plots <- list()
    if (plot_type == "trace") {
      for (i in 1:plots_needed) {
        subset_df <- tpdf_long[tpdf_long$parameter %in% unique(tpdf_long$parameter)[((i - 1) * (nrow * ncol) + 1):(i * (nrow * ncol))], ]
        trace_plot <- ggplot2::ggplot(subset_df, ggplot2::aes(x = .data[["Iteration"]], y = .data[["value"]], color = .data[["color"]])) +
          ggplot2::geom_line() +
          ggplot2::facet_wrap(~parameter, scales = "free_y", ncol = ncol, nrow = nrow) +
          ggplot2::scale_color_identity() +
          ggplot2::labs(title = paste("Trace plots for", param_type, "parameters")) +
          ggplot2::theme_minimal() +
          ggplot2::theme(legend.position = "none",
                         axis.title = ggplot2::element_blank())
        plots <- c(plots, list(trace_plot))
      }
    } else if (plot_type == "density") {
      for (i in 1:plots_needed) {
        subset_df <- tpdf_long[tpdf_long$parameter %in% unique(tpdf_long$parameter)[((i - 1) * (nrow * ncol) + 1):(i * (nrow * ncol))], ]
        density_plot <- ggplot2::ggplot(subset_df, ggplot2::aes(x = .data[["value"]], fill = .data[["color"]])) +
          ggplot2::geom_density(alpha = 0.5) +
          ggplot2::facet_wrap(~parameter, scales = "free", ncol = ncol, nrow = nrow) +
          ggplot2::scale_fill_identity() +
          ggplot2::labs(title = paste("Density plots for", param_type, "parameters")) +
          ggplot2::theme_minimal() +
          ggplot2::theme(legend.position = "none",
                         axis.title = ggplot2::element_blank())
        plots <- c(plots, list(density_plot))
      }
    }
    return(plots)
  }

  # helper function to create scaling plot
  create_scaling_plot_1d <- function(df, param_name) {
    df$Row.names <- sapply(df$Row.names, trim_label, trim = trim)
    scaling_plot <- ggplot2::ggplot(df, ggplot2::aes(x = stats::reorder(.data[["Row.names"]], .data[["mean"]]), y = mean)) +
      ggplot2::geom_hline(yintercept = 0, color = "gray", linewidth = 1.5) +
      ggplot2::geom_pointrange(ggplot2::aes(ymin = .data[["HPD2.5"]], ymax = .data[["HPD97.5"]], color = .data[["color"]]), linewidth = 1) +
      ggplot2::coord_flip() +
      ggplot2::scale_color_identity() +
      ggplot2::labs(title = paste(param_name, "parameters"), x = NULL, y = NULL) +
      ggplot2::theme_minimal()
    return(scaling_plot)
  }

  # helper function to create 2D scaling plot
  create_scaling_plot_2d <- function(df, param_name) {
    df$Row.names <- sapply(df$Row.names, trim_label, trim = trim)
    ggplot2::ggplot(df, ggplot2::aes(x = .data[["mean_dim1"]], y = .data[["mean_dim2"]], color = .data[["color"]])) +
      ggplot2::geom_point() +
      ggrepel::geom_text_repel(aes(label = .data[["Row.names"]]), max.overlaps = nrow(df)) +
      ggplot2::scale_color_identity() +
      ggplot2::labs(title = paste("Mean", param_name, "parameters"), x = NULL, y = NULL) +
      ggplot2::theme_minimal()
  }

  plots <- list()

  if ("trace" %in% type) {
    if ("ability" %in% parameters && "ability" %in% names(object)) {
      plots <- c(plots, create_diagnostic_plots(object$sample, "ability", "trace"))
    }
    if ("discrimination" %in% parameters && "discrimination" %in% names(object)) {
      plots <- c(plots, create_diagnostic_plots(object$sample, "discrimination", "trace"))
    }
    if ("difficulty" %in% parameters && "difficulty" %in% names(object)) {
      plots <- c(plots, create_diagnostic_plots(object$sample, "difficulty", "trace"))
    }
  }

  if ("density" %in% type) {
    if ("ability" %in% parameters && "ability" %in% names(object)) {
      plots <- c(plots, create_diagnostic_plots(object$sample, "ability", "density"))
    }
    if ("discrimination" %in% parameters && "discrimination" %in% names(object)) {
      plots <- c(plots, create_diagnostic_plots(object$sample, "discrimination", "density"))
    }
    if ("difficulty" %in% parameters && "difficulty" %in% names(object)) {
      plots <- c(plots, create_diagnostic_plots(object$sample, "difficulty", "density"))
    }
  }

  if ("scaling" %in% type) {
    if ("ability" %in% parameters && "ability" %in% names(object)) {
      if (any(grepl("^dna_scale2d", class(object)))) {
        scaling_plot <- create_scaling_plot_2d(object$ability, "ability")
      } else {
        scaling_plot <- create_scaling_plot_1d(object$ability, "Ability")
      }
      plots <- c(plots, list(scaling_plot))
    }
    if ("discrimination" %in% parameters && "discrimination" %in% names(object)) {
      if (any(grepl("^dna_scale2d", class(object)))) {
        scaling_plot <- create_scaling_plot_2d(object$discrimination, "discrimination")
      } else {
        scaling_plot <- create_scaling_plot_1d(object$discrimination, "Discrimination")
      }
      plots <- c(plots, list(scaling_plot))
    }
    if ("difficulty" %in% parameters && "difficulty" %in% names(object)) {
      scaling_plot <- create_scaling_plot_1d(object$difficulty, "Difficulty")
      plots <- c(plots, list(scaling_plot))
    }
  }

  return(plots)
}