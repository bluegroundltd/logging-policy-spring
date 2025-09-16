package com.blueground.loggingpolicy.logging.rabbit

import com.blueground.loggingpolicy.logging.CORRELATION_ID_HEADER
import com.blueground.loggingpolicy.logging.MdcKeys
import com.blueground.loggingpolicy.logging.generateCorrelationId
import org.aopalliance.intercept.MethodInterceptor
import org.aopalliance.intercept.MethodInvocation
import org.slf4j.MDC
import org.springframework.amqp.core.MessageProperties
import org.springframework.messaging.MessageHeaders
import org.springframework.stereotype.Component

@Component
class LoggingConsumerAdvice : MethodInterceptor {

  @Throws(Throwable::class)
  override fun invoke(invocation: MethodInvocation): Any? {
    val args = invocation.arguments
    var amqpMessageProperties: MessageProperties? = null
    var messagingHeaders: MessageHeaders? = null

    for (arg in args) {
      when (arg) {
        is org.springframework.amqp.core.Message -> {
          // AMQP Message
          amqpMessageProperties = arg.messageProperties
          break
        }

        is org.springframework.messaging.Message<*> -> {
          // Spring Messaging Message
          messagingHeaders = arg.headers
          break
        }
      }
    }

    // Access headers before message handling
    amqpMessageProperties?.let {
      val correlationId = it.headers[CORRELATION_ID_HEADER]?.toString()
        ?: it.correlationId
        ?: generateCorrelationId()

      MDC.put(MdcKeys.CORRELATION_ID, correlationId)
      MDC.put(MdcKeys.ENTRYPOINT, "rabbitmq/${it.consumerQueue}")
    }

    messagingHeaders?.let {
      val correlationId = it[CORRELATION_ID_HEADER]?.toString() ?: generateCorrelationId()
      MDC.put(MdcKeys.CORRELATION_ID, correlationId)
      MDC.put(MdcKeys.ENTRYPOINT, "rabbitmq")
    }

    // Proceed with the original method invocation
    val result = invocation.proceed()

    return result
  }
}
