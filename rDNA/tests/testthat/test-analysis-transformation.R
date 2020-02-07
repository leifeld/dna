context("Analysis/Transformation")

conn <- dna_connection(dna_sample(overwrite = TRUE, verbose = FALSE))

test_that("cluster", {
  expect_equal({
    clust <- dna_cluster(conn)
    c(length(clust),
      names(clust),
      clust$merge)
  }, c("14", "merge", "height", "order", "labels", "method", "call", 
       "dist.method", "activities", "attribute1", "attribute2", "group", 
       "mds", "fa", "network", "-3", "-6", "-1", "-2", "-4", "3", "-5", 
       "-7", "2", "1", "4", "5"))
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
    as.vector(nw)
  }, c(0, 22, 6, 8, 4, 2, 2, 22, 0, 12, 22, 2, 10, 1, 6, 12, 0, 7, 
       6, 4, 6, 8, 22, 7, 0, 6, 4, 3, 4, 2, 6, 6, 0, 4, 4, 2, 10, 4, 
       4, 4, 0, 3, 2, 1, 6, 3, 4, 3, 0))
})

# saveRDS(nw, file = "../files/dna_network_no_qualifier.RDS", version = 2)

# conversions
test_that("dna_toIgraph", {
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
  expect_equal({
    nw <- dna_network(conn,
                      networkType = "onemode",
                      variable1 = "organization",
                      variable2 = "concept",
                      qualifier = NULL,
                      verbose = FALSE)
    ig <- dna_toIgraph(nw, attributes = dna_getAttributes(conn, 
                                                          variable = "organization"))
    igraph::get.vertex.attribute(ig)$color
  }, c("#00CC00", "#FF9900", "#000000", "#FF9900", "#000000", "#00CC00", 
       "#00CC00"))
  expect_equal({
    nw <- dna_network(conn,
                      networkType = "twomode",
                      variable1 = "organization",
                      variable2 = "concept",
                      qualifier = NULL,
                      verbose = FALSE)
    ig <- dna_toIgraph(nw, attributes = rbind(
      dna_getAttributes(conn, variable = "organization"),
      dna_getAttributes(conn, variable = "concept")))
    igraph::get.vertex.attribute(ig)$color
  }, c("#00CC00", "#000000", "#000000", "#000000", "#000000", "#000000", 
       "#FF9900", "#000000", "#FF9900", "#000000", "#00CC00", "#000000", 
       "#00CC00"))
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

