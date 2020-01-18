context("Analysis/Transformation")

conn <- dna_connection(dna_sample(overwrite = TRUE, verbose = FALSE))

test_that("cluster", {
  expect_equal({
    dna_cluster(conn)
  }, readRDS("../files/dna_cluster.RDS"))
})

# saveRDS(dna_cluster(conn), "../files/dna_cluster.RDS", version = 2)

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
test_that("dna_network without qualifier", {
  expect_equal({
    nw <- dna_network(conn,
                      networkType = "onemode",
                      variable1 = "person",
                      variable2 = "concept",
                      qualifier = NULL,
                      verbose = FALSE)
    ig <- dna_toIgraph(nw)
    c(class(ig),
      igraph::vcount(ig),
      igraph::ecount(ig))
  }, c("igraph", "7", "21"))
  expect_equal({
    nw <- dna_network(conn,
                      networkType = "twomode",
                      variable1 = "person",
                      variable2 = "concept",
                      qualifier = NULL,
                      verbose = FALSE)
    ig <- dna_toIgraph(nw)
    c(class(ig),
      igraph::vcount(ig),
      igraph::ecount(ig))
  }, c("igraph", "13", "22"))
  expect_error({
    nw <- dna_network(conn,
                      networkType = "eventlist",
                      variable1 = "person",
                      variable2 = "concept",
                      qualifier = NULL,
                      verbose = FALSE)
    dna_toIgraph(nw)
  }, "Only takes objects of class 'dna_network_onemode' or 'dna_network_twomode'.")
})

