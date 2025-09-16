package com.blueground.loggingpolicy.logging.graphql

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.context.DgsContext
import com.netflix.graphql.dgs.internal.DgsWebMvcRequestData
import graphql.ExecutionResult
import graphql.GraphQLContext
import graphql.execution.instrumentation.Instrumentation
import graphql.execution.instrumentation.InstrumentationContext
import graphql.execution.instrumentation.InstrumentationState
import graphql.execution.instrumentation.SimpleInstrumentationContext.noOp
import graphql.execution.instrumentation.parameters.InstrumentationExecuteOperationParameters
import graphql.execution.instrumentation.parameters.InstrumentationExecutionParameters
import graphql.execution.instrumentation.parameters.InstrumentationValidationParameters
import graphql.language.Document
import graphql.language.OperationDefinition
import graphql.validation.ValidationError
import org.slf4j.LoggerFactory
import org.springframework.web.context.request.RequestAttributes
import org.springframework.web.context.request.WebRequest
import java.time.Duration
import java.time.Instant

@DgsComponent
class GraphQLInstrumentation : Instrumentation {

  private val logger = LoggerFactory.getLogger(GraphQLInstrumentation::class.java)

  override fun beginExecution(
    parameters: InstrumentationExecutionParameters,
    state: InstrumentationState?
  ): InstrumentationContext<ExecutionResult> {
    val startTime = Instant.now()

    // Access the HttpServletRequest
    val request = getWebRequest(parameters.graphQLContext)

    if (request == null) {
      logger.warn("Cannot set GraphQL logging attributes: WebRequest in DgsRequestData is null")
      return noOp()
    }

    // Proceed to collect data
    return object : InstrumentationContext<ExecutionResult> {
      override fun onCompleted(result: ExecutionResult?, t: Throwable?) {
        val endTime = Instant.now()
        val responseTime = Duration.between(startTime, endTime).toMillis()

        val errors = result?.errors ?: emptyList()
        val responseStatus = if (errors.isEmpty()) "Success" else "Error"

        val context = parameters.graphQLContext

        val operationType = getOperationType(parameters.query, parameters.operation)
        val operationName = parameters.operation ?: parameters.graphQLContext.get("operationName")
        val operationBody = parameters.query
        val variables = parameters.variables

        val graphQLAttributes = GraphQLAttributes(
          operationType = operationType,
          operationName = operationName,
          operationBody = operationBody,
          variables = variables,
          responseTime = responseTime,
          responseStatus = responseStatus,
          parsingTimeMs = context.get("parsingTimeMs"),
          validationTimeMs = context.get("validationTimeMs"),
          executionTimeMs = context.get("executionTimeMs"),
          errors = errors
        )

        // Store the collected attributes in the HttpServletRequest
        // for the HttpLoggingFilter to access and log
        setGraphQLAttributes(request, graphQLAttributes)
      }

      override fun onDispatched() {}
    }
  }

  override fun beginParse(
    parameters: InstrumentationExecutionParameters,
    state: InstrumentationState?
  ): InstrumentationContext<Document>? {
    val startTime = Instant.now()

    return object : InstrumentationContext<Document> {
      override fun onCompleted(result: Document?, t: Throwable?) {
        val endTime = Instant.now()
        val parsingTimeMs = Duration.between(startTime, endTime).toMillis()
        parameters.graphQLContext.put("parsingTimeMs", parsingTimeMs)
      }

      override fun onDispatched() {}
    }
  }

  override fun beginValidation(
    parameters: InstrumentationValidationParameters,
    state: InstrumentationState?
  ): InstrumentationContext<MutableList<ValidationError>>? {
    val startTime = Instant.now()

    return object : InstrumentationContext<MutableList<ValidationError>> {
      override fun onCompleted(result: MutableList<ValidationError>?, t: Throwable?) {
        val endTime = Instant.now()
        val validationTimeMs = Duration.between(startTime, endTime).toMillis()
        parameters.graphQLContext.put("validationTimeMs", validationTimeMs)
      }

      override fun onDispatched() {}
    }
  }

  override fun beginExecuteOperation(
    parameters: InstrumentationExecuteOperationParameters,
    state: InstrumentationState?
  ): InstrumentationContext<ExecutionResult>? {
    val startTime = Instant.now()

    return object : InstrumentationContext<ExecutionResult> {
      override fun onCompleted(result: ExecutionResult?, t: Throwable?) {
        val endTime = Instant.now()
        val executionTimeMs = Duration.between(startTime, endTime).toMillis()
        val operationName = parameters.executionContext.operationDefinition.name
        parameters.executionContext.graphQLContext.put("operationName", operationName)
        parameters.executionContext.graphQLContext.put("executionTimeMs", executionTimeMs)
      }

      override fun onDispatched() {}
    }
  }

  private fun getWebRequest(context: GraphQLContext): WebRequest? {
    val requestData = DgsContext.from(context).requestData as? DgsWebMvcRequestData
    return requestData?.webRequest
  }

  private fun setGraphQLAttributes(request: WebRequest, graphQLAttributes: GraphQLAttributes) {
    request.setAttribute("graphql", graphQLAttributes, RequestAttributes.SCOPE_REQUEST)
  }

  private fun getGraphQLAttributes(request: WebRequest): GraphQLAttributes? {
    return request.getAttribute("graphql", RequestAttributes.SCOPE_REQUEST) as? GraphQLAttributes
  }

  private fun getOperationType(query: String, operationName: String?): String {
    val parser = graphql.parser.Parser()
    val document: Document = parser.parseDocument(query)
    val operationDefinitions = document.getDefinitionsOfType(OperationDefinition::class.java)

    val operationDefinition = operationDefinitions.firstOrNull { it.name == operationName }
      ?: operationDefinitions.firstOrNull()

    return operationDefinition?.operation?.name?.lowercase() ?: "unknown"
  }
}
