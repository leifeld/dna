context("Analysis/Transformation")

conn <- dna_connection(dna_sample(overwrite = TRUE, verbose = FALSE))

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

test_that("dna_network without qualifier", {
  expect_equal({
    nw <- dna_network(conn,
                      networkType = "onemode",
                      variable1 = "person",
                      variable2 = "concept",
                      qualifier = NULL)
  }, readRDS("../files/dna_network_no_qualifier.RDS"))
})

# saveRDS(nw, file = "../files/dna_network_no_qualifier.RDS")
