context("Testing phase transitions")

# Create a function to set up the database for tests
setup_dna_database <- function() {
  dna_init()
  samp <- dna_sample()
  dna_openDatabase(samp, coderId = 1, coderPassword = "sample")
  return(samp)
}

# Create a function to clean up after tests
cleanup_dna_database <- function(samp) {
  dna_closeDatabase()
  unlink(samp)
}

test_that("dna_phaseTransitions produces expected output with default settings", {
  testthat::skip_on_cran()
  testthat::skip_on_ci()
  samp <- setup_dna_database()
  result <- dna_phaseTransitions()

  expect_type(result, "dna_phaseTransitions")
  expect_true(!is.null(result$states))
  expect_true(!is.null(result$modularity))
  expect_true(!is.null(result$clusterMethod))
  expect_true(!is.null(result$distmat))

  cleanup_dna_database(samp)
})

test_that("autoplot.dna_phaseTransitions produces expected plots", {
  testthat::skip_on_cran()
  testthat::skip_on_ci()
  samp <- setup_dna_database()
  phase_trans_obj <- dna_phaseTransitions()

  plots <- autoplot.dna_phaseTransitions(phase_trans_obj)
  expect_type(plots, "list")
  expect_length(plots, 4)
  expect_type(plots[[1]], "ggplot")

  cleanup_dna_database(samp)
})
