context("Data access")
setup(
  unlink("sample.dna"),
  unlink(dir(pattern = "^dna-.+\\.jar$"))
)

# set Sys.setenv(MAKE_DNA="TRUE") to run this part
if (Sys.getenv("MAKE_DNA") == "TRUE") {
  test_that("download Jar", {
    expect_equal({
      file <- dna_downloadJar(path = ".", returnString = TRUE)
      file.exists(file)
    }, TRUE) 
  })
  test_that("make DNA",{
    expect_equal({
      unlink(dir(pattern = "dna-.+\\.jar$"))
      system("cd ../../../ && make dna")
      jar <- dir(path = "../../../output", pattern = "^dna-.+\\.jar$",
                 full.names = TRUE)
      file.copy(
        from = jar,
        to = paste0("../../inst/extdata/", basename(jar)),
        overwrite = TRUE
      )
    }, TRUE)
  })
} else if (tolower(Sys.getenv("NOT_CRAN")) %in% c("1", "yes", "true") &
           !nchar(Sys.getenv("TRAVIS_R_VERSION")) > 0){
  test_that("download Jar", {
    expect_equal({
      file <- dna_downloadJar(returnString = TRUE)
      file.exists(file)
    }, TRUE) 
  })
}

test_that("initialise DNA",{
  expect_equal({
    jar <- dir("../../inst/extdata", "^dna-.+\\.jar$", full.names = TRUE)
    if (!length(jar) > 0) 
      jar <- dir(paste0(system.file(package = "rDNA"), "/extdata"), 
                 "^dna-.+\\.jar$", 
                 full.names = TRUE)
    dna_init(jar)
    rJava::.jarray(1:5)@jsig
  }, "[I")
})

test_that("load sample", {
  expect_equal({
    file.size(dna_sample())
  }, file.size(system.file("extdata", "sample.dna", package = "rDNA")))
})

test_that("connect to db", {
  expect_equal(
    dna_connection(dna_sample(overwrite = TRUE, verbose = FALSE))$dna_connection@jclass,
    "dna/export/ExporterR"
  )
})
