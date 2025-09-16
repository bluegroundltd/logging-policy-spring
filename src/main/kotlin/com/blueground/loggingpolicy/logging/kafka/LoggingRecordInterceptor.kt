package com.blueground.loggingpolicy.logging.kafka

import com.blueground.loggingpolicy.logging.CORRELATION_ID_HEADER
import com.blueground.loggingpolicy.logging.MdcKeys
import com.blueground.loggingpolicy.logging.generateCorrelationId
import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.header.Headers
import org.slf4j.MDC
import org.springframework.kafka.listener.RecordInterceptor


class LoggingRecordInterceptor<K : Any, V : Any> : RecordInterceptor<K, V> {

  override fun intercept(record: ConsumerRecord<K, V>, consumer: Consumer<K, V>): ConsumerRecord<K, V> {
    val headers = record.headers() ?: return record

    val correlationId = headers.stringValue(CORRELATION_ID_HEADER) ?: generateCorrelationId()

    MDC.put(MdcKeys.CORRELATION_ID, correlationId)
    MDC.put(MdcKeys.ENTRYPOINT, "kafka/${record.topic()}")

    return record
  }

  private fun Headers.stringValue(key: String): String? =
    this.lastHeader(key)?.value()?.toString(Charsets.UTF_8)

}
