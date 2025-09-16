package com.blueground.loggingpolicy.logging.rabbit

import com.blueground.loggingpolicy.logging.CORRELATION_ID_HEADER
import com.blueground.loggingpolicy.logging.MdcKeys
import com.blueground.loggingpolicy.logging.generateCorrelationId
import org.slf4j.MDC
import org.springframework.amqp.core.Message
import org.springframework.amqp.core.MessagePostProcessor


class LoggingMessageProcessor : MessagePostProcessor {

  override fun postProcessMessage(message: Message): Message {
    val correlationId = MDC.get(MdcKeys.CORRELATION_ID) ?: generateCorrelationId()
    message.messageProperties.correlationId = correlationId
    message.messageProperties.headers[CORRELATION_ID_HEADER] = correlationId
    return message
  }
}
