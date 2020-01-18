library("testthat")
library("rDNA")

if (identical(Sys.getenv("NOT_CRAN"), "true")) {
  test_check("rDNA")
} else {
  jar <- list.files(system.file("extdata", package = "rDNA"),
                    pattern = ".jar$", full.names = TRUE)
  if (length(jar) == 0) {
    jar <- dna_downloadJar(path = ".")
  }
  dna_init(jar)
  test_check("rDNA")
}
