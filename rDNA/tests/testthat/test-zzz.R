context("cleanup")

test_that("str_length is number of characters", {
  expect_that(nchar("a"), equals(1))
})

teardown({
  unlink("sample.dna")
  unlink(dir(path = "../../inst/extdata/", 
             pattern = "^dna-.+\\.jar$",
             full.names = TRUE))
})
