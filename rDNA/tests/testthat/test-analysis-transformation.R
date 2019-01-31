context("Analysis/Transformation")

conn <- dna_connection("sample.dna")

test_that("cluster", {
  expect_equal({
    dna_cluster(conn)
  }, readRDS("../files/dna_cluster.RDS"))
})

# saveRDS(dna_cluster(conn), "../files/dna_cluster.RDS")

test_that("print dna_cluster", {
  expect_equal({
    capture.output(print(dna_cluster(conn)))
  },
  readLines("../files/print_dna_cluster"))
})

# writeLines(capture.output(print(dna_cluster(conn))), "../files/print_dna_cluster")
