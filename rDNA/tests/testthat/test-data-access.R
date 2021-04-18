context("data access")

test_that("dna_sample works", {
  unlink("sample.dna")
  output <- dna_sample()
  expect_match(output, paste0(getwd(), "/sample.dna$"))
  expect_true(file.exists("sample.dna"))
  expect_warning(dna_sample(), "Sample file already exists")
  unlink("sample.dna")
})

test_that("connecting to sample database works", {
  s <- dna_sample(overwrite = TRUE, verbose = FALSE)
  tryCatch({
    .jinit()
    print("Works fine.")
  },
  error = function(e) print("Uh-oh, Java failed."))
  dna_init()
  expect_that(
    dna_connection(s, verbose = FALSE)$dna_connection@jclass,
    equals("dna/export/ExporterR")
  )
  unlink(s)
})

test_that("loading sample database works", {
  unlink("sample.dna")
  expect_equal(dna_sample(), paste0(getwd(), "/sample.dna"))
  expect_true(file.exists(paste0(getwd(), "/sample.dna")))
  expect_gt(file.size(paste0(getwd(), "/sample.dna")), 200000)
  unlink("sample.dna")
})
