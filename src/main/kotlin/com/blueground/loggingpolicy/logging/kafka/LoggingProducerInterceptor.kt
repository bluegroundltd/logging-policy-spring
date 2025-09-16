package com.blueground.loggingpolicy.logging.kafka

import com.blueground.loggingpolicy.logging.CORRELATION_ID_HEADER
import com.blueground.loggingpolicy.logging.MdcKeys
import com.blueground.loggingpolicy.logging.generateCorrelationId
import org.apache.kafka.clients.producer.ProducerInterceptor
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.RecordMetadata
import org.apache.kafka.common.protocol.Message
import org.slf4j.MDC

class LoggingProducerInterceptor : ProducerInterceptor<String, Message> {

  override fun onSend(record: ProducerRecord<String, Message>): ProducerRecord<String, Message> {
    if (record.headers().lastHeader(CORRELATION_ID_HEADER) == null) {
      val correlationId = MDC.get(MdcKeys.CORRELATION_ID) ?: generateCorrelationId()
      record.headers().add(CORRELATION_ID_HEADER, correlationId.toByteArray(Charsets.UTF_8))
    }
    return record
  }

  override fun onAcknowledgement(metadata: RecordMetadata, exception: Exception?) {}

  override fun close() {}

  override fun configure(configs: Map<String, *>) {}
}
