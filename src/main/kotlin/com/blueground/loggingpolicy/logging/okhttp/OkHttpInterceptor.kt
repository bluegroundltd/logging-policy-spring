package com.blueground.loggingpolicy.logging.okhttp

import com.blueground.loggingpolicy.logging.CORRELATION_ID_HEADER_HTTP
import com.blueground.loggingpolicy.logging.MdcKeys
import com.blueground.loggingpolicy.logging.generateCorrelationId
import okhttp3.Interceptor
import okhttp3.Response
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.http.HttpStatus

class OkHttpInterceptor : Interceptor {

  companion object {
    private val logger = LoggerFactory.getLogger(OkHttpInterceptor::class.java)
  }

  override fun intercept(chain: Interceptor.Chain): Response {
    val request = chain.request()
    val requestBuilder = request.newBuilder()
    val correlationId = MDC.get(MdcKeys.CORRELATION_ID) ?: generateCorrelationId()

    requestBuilder.addHeader(CORRELATION_ID_HEADER_HTTP, correlationId)

    val startTime = System.currentTimeMillis()

    val uri = request.url.toUri()
    logger.info("-> [req] ${request.method} ${uri.host} ${uri.path}")

    val newRequest = requestBuilder.build()

    // Proceed with the request
    val response = chain.proceed(newRequest)

    val duration = (System.currentTimeMillis() - startTime).toDouble() / 1000
    val status = HttpStatus.resolve(response.code)
    logger.info("<- [res] ${request.method} ${uri.host} ${uri.path} $status (${duration}s)")

    return response
  }
}
