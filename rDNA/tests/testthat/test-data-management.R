context("Data management")

conn <- dna_connection(dna_sample(overwrite = TRUE, verbose = FALSE))

test_that("add Attribute message", {
  expect_message({
     dna_addAttribute(conn)
  }, "A new attribute with ID 2 was added to the database.")
})

test_that("add Document message",{
  expect_message({
     dna_addDocument(conn, date = as.POSIXct("2018-12-02 19:32:19 GMT", tz = "GMT"))
  }, "A new document with ID 8 was added to the database")
})

test_that("get Attributes", {
  expect_that({
    dna_getAttributes(conn)
  }, equals(readRDS("../files/dna_getAttributes.RDS")))
})

# saveRDS(dna_getAttributes(conn), "../files/dna_getAttributes.RDS")

test_that("get Documents", {
  expect_that({
    dna_getDocuments(conn)
  }, equals(readRDS("../files/dna_getDocuments.RDS")))
})

# saveRDS(dna_getDocuments(conn), "../files/dna_getDocuments.RDS")

test_that("remove Attribute", {
  expect_output({
   dna_removeAttribute(conn, attribute = 25)
  }, paste0("Simulation mode: no actual changes are made to the database!\n",
            "Statements removed: 0"))
})

test_that("remove Document",{
  expect_output({
    dna_removeDocument(conn, id = 8)
  }, paste0("Simulation mode: no actual changes are made to the database!\n",
            "Statements removed in Document 8: 0\n",                         
            "Removal of Document 8: successful."))
})

test_that("set Attributes", {
  expect_that({
    att <- dna_getAttributes(conn)
    att[nrow(att) + 1, ] <- c(NA, "test", "#00CC00", "NGO", "", "", 1)
    att$id <- as.integer(att$id)
    att$frequency <- as.integer(att$frequency)
    dna_setAttributes(conn, attributes = att, simulate = FALSE)
    dna_getAttributes(conn)
  }, equals(readRDS("../files/dna_setAttributes.RDS")))
})

# saveRDS(dna_getAttributes(conn), "../files/dna_setAttributes.RDS")

test_that("set Documents", {
  expect_that({
    docs <- dna_getDocuments(conn)
    docs <- rbind(docs, docs)
    docs$id <- seq_along(docs$id)
    dna_setDocuments(
      conn, 
      documents = docs,
      simulate = FALSE, verbose = TRUE
    )
    dna_getDocuments(conn)
  }, equals(readRDS("../files/dna_setDocuments.RDS")))
})

# saveRDS(dna_getDocuments(conn), "../files/dna_setDocuments.RDS")

test_that("get coders", {
  expect_that({
    dna_getCoders(conn)
  }, equals(readRDS("../files/dna_getCoders.RDS")))
})

# saveRDS(dna_getCoders(conn), "../files/dna_getCoders.RDS")

test_that("add coder", {
  expect_output({
    dna_addCoder(conn, "new coder", "#FFFF00")
  }, "A new coder with ID 5 was added to the database.")
})

test_that("remove coder", {
  expect_output({
    dna_removeCoder(conn, 5)
  }, "Coder with ID 5 was removed from the database.")
})

test_that("remove coder abort", {
  expect_output({
    dna_removeCoder(conn, 6)
  }, "Coder with ID 6 not found. Aborting.")
})

test_that("update coder", {
  expect_output({
    skip_on_cran()
    dna_updateCoder(conn, 3, color = "#CCCCCC", deleteDocuments = TRUE, editRegex = FALSE)
  }, paste0("Updated color of coder 3: '#FF6633' -> '#CCCCCC'\n",
            "Updated 'editRegex' permission of coder 3: true -> false"))
  expect_error({
    dna_updateCoder(conn, 3, color = c("#CCCCCC", "#CCCCCC"), deleteDocuments = TRUE, editRegex = FALSE)
  }, "'color' must be NULL (for no changes) or an object of length 1.", fixed = TRUE)
})

test_that("get regex", {
  expect_that({
    dna_getRegex(conn)
  }, equals(readRDS("../files/dna_getRegex.RDS")))
})

# saveRDS(dna_getRegex(conn), "../files/dna_getRegex.RDS")

test_that("add regex", {
  expect_that({
    dna_addRegex(conn, "energy", "#FF0000")
    dna_getRegex(conn)
  }, equals(readRDS("../files/dna_addRegex.RDS")))
  expect_error({
    dna_addRegex(conn, "energy", c("#CCCCCC", "#CCCCCC"))
  }, "'color' must be an object of length 1.", fixed = TRUE)
})

# saveRDS(dna_getRegex(conn), "../files/dna_addRegex.RDS")

test_that("remove regex", {
  expect_that({
    dna_removeRegex(conn, "energy")
    dna_getRegex(conn)
  }, equals(readRDS("../files/dna_removeRegex.RDS")))
})

# saveRDS(dna_getRegex(conn), "../files/dna_removeRegex.RDS")

test_that("get settings", {
  expect_that({
    s <- dna_getSettings(conn)
    s[s[, 1] == "statementColor", 2]
  }, equals("statementType"))
})

test_that("update setting", {
  expect_that({
    dna_updateSetting(conn, "popupWidth", "400")
    s <- dna_getSettings(conn)
    s[s[, 1] == "popupWidth", 2]
  }, equals("400"))
})
