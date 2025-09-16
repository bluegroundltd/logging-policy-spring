package com.blueground.loggingpolicy.config

import com.blueground.loggingpolicy.logging.kafka.LoggingProducerInterceptor
import com.blueground.loggingpolicy.logging.kafka.LoggingRecordInterceptor
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.support.serializer.JsonDeserializer


/**
 * Kafka configuration
 *
 * Note:
 * While a ProducerInterceptor is being used to inject the correlationId
 * to the message headers, a RecordInterceptor is being used instead for
 * consumers. The reason for this inconsistency is that the ConsumerInterceptor
 * gets executed for batches of records (max.poll.records), while
 * the RecordInterceptor is executed for each individual record.
 */
@EnableKafka
@Configuration
class KafkaConfig {

  @Bean
  fun producerFactory(kafkaProperties: KafkaProperties): DefaultKafkaProducerFactory<Any, Any> {
    val props = kafkaProperties.buildProducerProperties(null) + mapOf(
      ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to "org.apache.kafka.common.serialization.StringSerializer",
      ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to "org.springframework.kafka.support.serializer.JsonSerializer",
      ProducerConfig.INTERCEPTOR_CLASSES_CONFIG to LoggingProducerInterceptor::class.java.name
    )
    return DefaultKafkaProducerFactory(props)
  }

  @Bean
  fun consumerFactory(kafkaProperties: KafkaProperties): DefaultKafkaConsumerFactory<Any, Any> {
    val props = kafkaProperties.buildConsumerProperties(null) + mapOf(
      ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to "org.apache.kafka.common.serialization.StringDeserializer",
      ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to "org.springframework.kafka.support.serializer.JsonDeserializer",
      JsonDeserializer.TRUSTED_PACKAGES to "com.blueground.*, java.util, java.lang"
    )
    return DefaultKafkaConsumerFactory(props)
  }

  @Bean
  fun kafkaListenerContainerFactory(
    consumerFactory: ConsumerFactory<Any, Any>
  ): ConcurrentKafkaListenerContainerFactory<Any, Any> {
    val factory = ConcurrentKafkaListenerContainerFactory<Any, Any>()
    factory.consumerFactory = consumerFactory
    factory.setRecordInterceptor(LoggingRecordInterceptor())
    return factory
  }
}
