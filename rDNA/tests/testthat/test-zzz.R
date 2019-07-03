context("cleanup")

teardown({
  unlink("sample.dna")
  unlink(dir(path = "../../inst/extdata/",
             pattern = "^dna-.+\\.jar$",
             full.names = TRUE))
})
