context("cleanup")

teardown({
  unlink("sample.dna")
  unlink("../../java", recursive = TRUE)
  unlink("../../inst/java", recursive = TRUE)
})
