context("Data access")
setup(
  unlink("sample.dna"),
  unlink(dir(pattern = "^dna-.+\\.jar$"))
)

# set Sys.setenv(MAKE_DNA = "TRUE") to run this part
if (Sys.getenv("MAKE_DNA") == "TRUE" & 
    tolower(Sys.getenv("NOT_CRAN")) %in% c("1", "yes", "true")) {
  test_that("download Jar", {
    expect_that({
      file <- dna_downloadJar(path = ".", returnString = TRUE)
      file.exists(file)
    }, equals(TRUE))
  })
  test_that("make DNA", {
    expect_that({
      unlink(dir(pattern = "dna-.+\\.jar$"))
      system("cd ../../../ && make dna", ignore.stdout = TRUE)
      jar <- dir(path = "../../../output", pattern = "^dna-.+\\.jar$",
                 full.names = TRUE)
      file.copy(
        from = jar,
        to = paste0("../../inst/extdata/", basename(jar)),
        overwrite = TRUE
      )
    },  equals(TRUE))
  })
} else if (tolower(Sys.getenv("NOT_CRAN")) %in% c("1", "yes", "true")) {
  test_that("download Jar", {
    expect_that({
      file <- dna_downloadJar("../../inst/extdata/", returnString = TRUE)
      file.exists(file)
    },  equals(TRUE))
  })
}

test_that("initialise DNA",{
  expect_that({
    jar <- dir("../../inst/extdata", "^dna-.+\\.jar$", full.names = TRUE)
    if (!length(jar) > 0) {
      jar <- dir(paste0(system.file(package = "rDNA"), "/extdata"),
                 "^dna-.+\\.jar$",
                 full.names = TRUE)
    }
    if (length(jar) > 0) {
      dna_init(jar)
    } else {
      dna_init()
    }
    rJava::.jarray(1:5)@jsig
  },  equals("[I"))
})

test_that("load sample", {
  expect_that({
    file.size(dna_sample())
  }, equals(file.size(system.file("extdata", "sample.dna", package = "rDNA"))))
})

test_that("connect to db", {
  expect_that(
    dna_connection(dna_sample(overwrite = TRUE, verbose = FALSE))$dna_connection@jclass,
    equals("dna/export/ExporterR")
  )
})
