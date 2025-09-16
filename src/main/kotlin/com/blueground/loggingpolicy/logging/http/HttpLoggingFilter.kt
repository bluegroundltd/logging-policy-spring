package com.blueground.loggingpolicy.logging.http

import CountingHttpServletResponseWrapper
import com.blueground.loggingpolicy.logging.CORRELATION_ID_HEADER
import com.blueground.loggingpolicy.logging.CORRELATION_ID_HEADER_HTTP
import com.blueground.loggingpolicy.logging.MdcKeys
import com.blueground.loggingpolicy.logging.generateCorrelationId
import com.blueground.loggingpolicy.logging.graphql.GraphQLAttributes
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter


/**
 * An HTTP request/response logger implemented as a [jakarta.servlet.Filter].
 *
 * - incoming HTTP requests (can be toggled on/off via a feature flag)
 * - outgoing HTTP responses
 *
 * The log format is:
 * ```
 * [req] METHOD PATH
 * [res] METHOD PATH STATUS_CODE STATUS_TEXT (duration)
 * ```
 *
 * Log levels:
 * - 5xx are logged at ERROR level
 * - 4xx at WARN level
 * - rest at INFO level
 *
 * Attributes included as [key value pairs](https://www.slf4j.org/manual.html#fluent):
 * - HTTP attributes
 * - Network attributes
 * - GeoIP attributes
 * - GraphQL attributes
 *
 * @see [Datadog standard attributes](https://docs.datadoghq.com/standard-attributes/?product=log)
 * @see [HTTP in LoggingPolicy](https://theblueground.getoutline.com/doc/logging-policy-4Aju7m0Iwq#h-http)
 */
@Order(1)
@Component()
class HttpLoggingFilter(
  @Value("\${logging.http.incoming.requests.enabled:false}") private val logIncomingRequests: Boolean
) : OncePerRequestFilter() {

  companion object {
    private val httpLogger = LoggerFactory.getLogger(HttpLoggingFilter::class.java)
  }

  override fun doFilterInternal(
    request: HttpServletRequest,
    response: HttpServletResponse,
    filterChain: FilterChain
  ) {
    // Capture the request details
    val method = request.method
    val path = request.requestURI

    // Get the correlation ID from the request headers or generate a new one
    val correlationId = request.getHeader(CORRELATION_ID_HEADER)
      ?: request.getHeader(CORRELATION_ID_HEADER_HTTP)
      ?: generateCorrelationId()

    // Get the user from the authentication context
    val authentication = SecurityContextHolder.getContext().authentication
    val username = if (authentication != null && authentication.isAuthenticated) {
      when (val principal = authentication.principal) {
        is UserDetails -> principal.username
        is String -> principal
        else -> null
      }
    } else {
      null
    }

    MDC.put(MdcKeys.CORRELATION_ID, correlationId)
    MDC.put(MdcKeys.ENTRYPOINT, "http")
    MDC.put(MdcKeys.USERNAME, username ?: "anonymous")

    val httpAttributes = extractHttpLoggingAttributes(request, response)
    val networkAttributes = extractNetworkAttributes(request, response)

    // Log incoming request: [req] METHOD PATH
    if (logIncomingRequests) {
      httpLogger.atInfo()
        .addKeyValue("http", httpAttributes)
        .addKeyValue("network", networkAttributes)
        .log("[req] $method $path")
    }

    // Track the start time for calculating the duration
    val startTime = System.currentTimeMillis()

    val responseWrapper = CountingHttpServletResponseWrapper(response)

    // Proceed with the request
    filterChain.doFilter(request, responseWrapper)

    // After the request is processed, capture the response details
    val statusCode = response.status
    val statusText = HttpStatus.valueOf(statusCode).reasonPhrase ?: "N/A"
    val duration = (System.currentTimeMillis() - startTime).toDouble() / 1000
    val graphQLAttributes = request.getAttribute("graphql") as? GraphQLAttributes

    networkAttributes.bytesWritten = responseWrapper.getByteCount().toLong()

    // Log outgoing response: [res] METHOD PATH STATUS_CODE STATUS_TEXT (duration)
    when {
      statusCode >= 500 -> httpLogger.atError()
      statusCode >= 400 -> httpLogger.atWarn()
      else -> httpLogger.atInfo()
    }
      .addKeyValue("http", httpAttributes)
      .addKeyValue("network", networkAttributes)
      .addKeyValue("graphql", graphQLAttributes)
      .log("[res] $method $path $statusCode $statusText (${String.format("%.3f", duration)}s)")
  }
}
