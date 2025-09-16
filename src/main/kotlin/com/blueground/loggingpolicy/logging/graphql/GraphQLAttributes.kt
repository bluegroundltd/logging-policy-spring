package com.blueground.loggingpolicy.logging.graphql

data class GraphQLAttributes(
  val operationType: String,
  var operationName: String?,
  val operationBody: String,
  val variables: Map<String, Any>,
  val responseTime: Long,
  val responseStatus: String,
  val parsingTimeMs: Long? = null,
  val validationTimeMs: Long? = null,
  val executionTimeMs: Long? = null,
  val errors: List<Any> = emptyList()
)
