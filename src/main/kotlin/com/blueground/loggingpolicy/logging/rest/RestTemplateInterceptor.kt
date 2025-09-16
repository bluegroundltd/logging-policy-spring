package com.blueground.loggingpolicy.logging.rest

import com.blueground.loggingpolicy.logging.CORRELATION_ID_HEADER_HTTP
import com.blueground.loggingpolicy.logging.MdcKeys
import com.blueground.loggingpolicy.logging.generateCorrelationId
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse

/**
 * Interceptor that adds the CorrelationId
 * to the headers of the outgoing HTTP requests
 * performed by the [RestTemplate](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/web/client/RestTemplate.html).
 */
class RestTemplateInterceptor : ClientHttpRequestInterceptor {

  companion object {
    private val logger = LoggerFactory.getLogger(RestTemplateInterceptor::class.java)
  }

  override fun intercept(
    request: HttpRequest,
    body: ByteArray,
    execution: ClientHttpRequestExecution
  ): ClientHttpResponse {
    val correlationId = MDC.get(MdcKeys.CORRELATION_ID) ?: generateCorrelationId()
    request.headers.add(CORRELATION_ID_HEADER_HTTP, correlationId)

    val startTime = System.currentTimeMillis()

    logger.info("-> [req] ${request.method} ${request.uri.host} ${request.uri.path}")

    val response = execution.execute(request, body)

    val duration = (System.currentTimeMillis() - startTime).toDouble() / 1000

    logger.info("<- [res] ${request.method} ${request.uri.host} ${request.uri.path} ${response.statusCode} (${duration}s)")

    return response
  }
}
