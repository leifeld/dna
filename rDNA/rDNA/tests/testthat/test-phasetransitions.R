context("Testing dna_phaseTransitions")

# Initialize DNA and sample database
suppressPackageStartupMessages(library("rDNA"))
dna_init()

# example 1: testing with default values
test_that("dna_phaseTransitions produces expected output with default settings", {
  samp <- dna_sample()
  dna_openDatabase(samp, coderId = 1, coderPassword = "sample")
  result <- dna_phaseTransitions()
  expect_s3_class(result, "dna_phaseTransitions")
  expect_true(!is.null(result$states))
  expect_true(!is.null(result$modularity))
  expect_true(!is.null(result$clusterMethod))
  expect_true(!is.null(result$distmat))
  dna_closeDatabase()
  unlink(samp)
})

# example 2: testing plotting function
context("Testing autoplot.dna_phaseTransitions")


set_up_test_data <- function() {
  samp <- dna_sample()
  dna_openDatabase(samp, coderId = 1, coderPassword = "sample")
  phase_trans_obj <- dna_phaseTransitions()
  return(list(samp = samp, pt_obj = phase_trans_obj))
}


clean_up_test_data <- function(samp) {
  # Close the database and perform any cleanup operations
  dna_closeDatabase()
  unlink(samp)
}

test_that("autoplot.dna_phaseTransitions produces expected plots", {
  test_data <- set_up_test_data()
  plots <- autoplot.dna_phaseTransitions(test_data$pt_obj)
  expect_is(plots, "list")
  expect_length(plots, 4)
  expect_is(plots[[1]], "ggplot")
  clean_up_test_data(test_data$samp)
})