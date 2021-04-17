context("dna_multiclust")

dna_init()
conn <- dna_connection(dna_sample(overwrite = TRUE, verbose = FALSE))

test_that("dna_multiclust works cross-sectionally with k = 2", {
  mc <- dna_multiclust(conn, k = 2, verbose = FALSE)
  expect_equal(mc$k, 2)
  expect_equal(nrow(mc$max_mod), 1)
  expect_equal(ncol(mc$max_mod), 6)
  expect_equal(nrow(mc$modularity), 11)
  expect_equal(ncol(mc$modularity), 4)
  expect_equal(nrow(mc$memberships), 77)
  expect_equal(ncol(mc$memberships), 4)
  expect_length(unique(mc$modularity$method), 11)
})

test_that("dna_multiclust works cross-sectionally with k = 0", {
  mc <- dna_multiclust(conn, k = 0, verbose = FALSE)
  expect_equal(mc$k, 0)
  expect_equal(nrow(mc$max_mod), 1)
  expect_equal(ncol(mc$max_mod), 6)
  expect_equal(nrow(mc$modularity), 15)
  expect_equal(ncol(mc$modularity), 4)
  expect_equal(nrow(mc$memberships), 105)
  expect_equal(ncol(mc$memberships), 4)
  expect_length(unique(mc$modularity$method), 15)
  p <- dna_plotModularity(mc)
  expect_equal(class(p), c("gg", "ggplot"))
})

test_that("dna_multiclust works longitudinally with k = 0", {
  set.seed(12345)
  mc <- dna_multiclust(conn, k = 0, timewindow = "events", windowsize = 28, saveObjects = TRUE, verbose = FALSE)
  expect_true(class(mc$cl[[1]])[1] %in% c("hclust", "numeric", "communities", "pam"))
  expect_true(length(mc$cl) > 8)
  expect_gt(nrow(mc$max_mod), 8)
  expect_equal(ncol(mc$max_mod), 7)
  expect_gt(nrow(mc$modularity), 100)
  expect_equal(ncol(mc$modularity), 4)
  expect_gt(nrow(mc$memberships), 500)
  expect_equal(ncol(mc$memberships), 4)
  expect_length(unique(mc$modularity$method), 15)
  p <- dna_plotModularity(mc)
  expect_equal(class(p), c("gg", "ggplot"))
})

test_that("print.dna_multiclust works", {
  mc <- dna_multiclust(conn, k = 2, verbose = FALSE)
  expect_match(capture_output(mc, print = TRUE), "\\n\\$modularity")
})

test_that("dna_multiclust works without qualifier", {
  mc <- dna_multiclust(conn, qualifier = NULL, verbose = FALSE)
  expect_s3_class(mc, "dna_multiclust")
  expect_named(mc, c("k", "max_mod", "memberships", "modularity"))
})

test_that("dna_multiclust works with durations = TRUE", {
  mc <- dna_multiclust(conn, timewindow = "events", windowsize = 28, verbose = FALSE)
  p <- dna_plotModularity(mc, durations = TRUE)
  expect_s3_class(p, "gg")
  expect_s3_class(p, "ggplot")
  p2 <- dna_plotModularity(mc, only.max = FALSE, durations = TRUE)
  expect_s3_class(p2, "gg")
  expect_s3_class(p2, "ggplot")
})

test_that("dna_dendrogram works", {
  skip_on_cran()
  d <- dna_dendrogram(conn, method = "best", k = 0)
  expect_s3_class(d, "ggplot")

  d <- dna_dendrogram(conn, method = "walktrap", k = 3, rectangle.colors = "purple")
  expect_s3_class(d, "ggplot")

  d <- dna_dendrogram(conn,
                      label.colors = "color",
                      leaf.colors = "cluster",
                      rectangle.colors = c("steelblue", "orange"),
                      symbol.shapes = 17:18,
                      symbol.colors = 3:4)
  expect_s3_class(d, "ggplot")

  d <- dna_dendrogram(conn, circular = TRUE, label.truncate = 12)
  expect_s3_class(d, "ggplot")

  d <- dna_dendrogram(conn, excludeValues = list(concept = "There should be legislation to regulate emissions."))
  expect_s3_class(d, "ggplot")

  d <- dna_dendrogram(conn, k = 0, method = "best", return.multiclust = TRUE)
  expect_s3_class(d, "dna_multiclust")
})

unlink("sample.dna")