context("cleanup")

teardown({
  unlink("sample.dna")
  unlink("profile.dnc")
  unlink("test.dna")
  unlink("inst/java/*.jar")
})