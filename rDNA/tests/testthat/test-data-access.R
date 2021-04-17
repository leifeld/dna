context("data access")

test_that("dna_sample works", {
  unlink("sample.dna")
  output <- dna_sample()
  expect_match(output, paste0(getwd(), "/sample.dna$"))
  expect_true(file.exists("sample.dna"))
  expect_warning(dna_sample(), "Sample file already exists")
  unlink("sample.dna")
})

test_that("dna_init, dna_connection, and print.dna_connection work", {
  f <- dna_sample(overwrite = TRUE)
  suppressMessages(s <- dna_init(returnString = TRUE))
  expect_match(s, "\\.jar$")
  expect_output(conn <- dna_connection(f, verbose = TRUE), "Data loaded: 41 statements and 7 documents")
  expect_silent(conn <- dna_connection(f, verbose = FALSE))
  expect_s3_class(conn, "dna_connection")
  expect_output(print(conn), paste0("^DNA database: ", getwd(), "/sample.dna"))
  expect_output(print(conn), "41 statements in 7 documents")
  expect_output(print(conn), "Statement types: ")
  unlink("sample.dna")
  expect_error(conn <- dna_connection("temp.dna"), "could not be located")
  unlink("temp.dna")
  expect_output(conn <- dna_connection("temp.dna", create = TRUE),
                "Table structure created.\nData loaded: 0.+Statement types: DNA Statement, Annotation$")
  unlink("temp.dna")
})

test_that("dna_downloadJar works", {
  skip_on_cran()
  d <- dna_downloadJar(returnString = TRUE)
  expect_warning(dna_downloadJar(), "Latest DNA jar file already exists")
  d2 <- dna_downloadJar(force = TRUE, returnString = TRUE)
  expect_equal(d, d2)
  unlink(d)
})

test_that("dna_installJar works", {
  skip_on_cran()
  expect_error(suppressMessages(dna_installJar()), "Current working directory is not rDNA/")
  expect_message(tryCatch(dna_installJar(), error = function(e) {}),
                 "Attempting to install DNA JAR file in /inst/java/")
  dir.create("temp")
  dir.create("temp/rDNA")
  wd <- getwd()
  setwd(paste0(wd, "/temp/rDNA"))
  expect_error(suppressMessages(dna_installJar()), "DESCRIPTION file not found")
  output <- file.copy(from = system.file("DESCRIPTION", package = "rDNA"), to = ".")
  expect_error(suppressMessages(dna_installJar()), "inst/ directory not found")
  dir.create("inst")
  output <- dna_installJar()
  expect_equal(output, 0)
  expect_match(list.files("inst/java"), "dna-.+\\.jar")
  expect_equal(dna_installJar(), 0)
  setwd(wd)
  unlink("temp", force = TRUE, recursive = TRUE)
})

test_that("print.dna_dataframe works", {
  s <- dna_sample()
  conn <- dna_connection(s, verbose = FALSE)
  at <- dna_getAttributes(connection = conn)
  expect_equal(nchar(at$value[1]), 23)
  expect_output(expect_equal(nchar(print(at, truncate = 12)$value[1]), 12), "id")
  unlink(s)
})

test_that("loading sample database works", {
  expect_that({
    file.size(s <- dna_sample())
  }, equals(file.size(system.file("extdata", "sample.dna", package = "rDNA"))))
  unlink(s)
})

test_that("connecting to sample database works", {
  expect_that(
    dna_connection(s <- dna_sample(overwrite = TRUE, verbose = FALSE), verbose = FALSE)$dna_connection@jclass,
    equals("dna/export/ExporterR")
  )
  unlink(s)
})
