package com.blueground.loggingpolicy.config

import com.blueground.loggingpolicy.logging.rabbit.LoggingConsumerAdvice
import com.blueground.loggingpolicy.logging.rabbit.LoggingMessageProcessor
import com.blueground.loggingpolicy.rabbit.RabbitOrderConsumer
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.springframework.amqp.core.Binding
import org.springframework.amqp.core.BindingBuilder
import org.springframework.amqp.core.Queue
import org.springframework.amqp.core.TopicExchange
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter
import org.springframework.amqp.support.converter.MessageConverter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
class RabbitConfig {
  private val exchangeName = "order.request"
  private val queueName = "order.request"
  private val routingKey = "default"

  @Bean
  fun queue(): Queue = Queue(queueName)

  @Bean
  fun exchange(): TopicExchange = TopicExchange(exchangeName)

  @Bean
  fun binding(queue: Queue?, exchange: TopicExchange?): Binding =
    BindingBuilder.bind(queue).to(exchange).with(routingKey)

  @Bean
  fun messageConverter(): MessageConverter {
    val objectMapper = ObjectMapper().registerModule(KotlinModule.Builder().build())
    return Jackson2JsonMessageConverter(objectMapper)
  }

  @Bean
  fun rabbitTemplate(connectionFactory: ConnectionFactory, messageConverter: MessageConverter): RabbitTemplate {
    val rabbitTemplate = RabbitTemplate(connectionFactory)
    rabbitTemplate.messageConverter = messageConverter
    rabbitTemplate.addBeforePublishPostProcessors(LoggingMessageProcessor())
    return rabbitTemplate
  }

  @Bean
  fun container(
    connectionFactory: ConnectionFactory,
    listenerAdapter: MessageListenerAdapter,
    loggingAdvice: LoggingConsumerAdvice
  ): SimpleMessageListenerContainer {
    val container = SimpleMessageListenerContainer()
    container.connectionFactory = connectionFactory
    container.setQueueNames(queueName)
    container.setAdviceChain(loggingAdvice)
    container.setMessageListener(listenerAdapter)
    return container
  }

  @Bean
  fun listenerAdapter(consumer: RabbitOrderConsumer): MessageListenerAdapter {
    val adapter = MessageListenerAdapter(consumer, "receiveOrder")
    adapter.setMessageConverter(messageConverter())
    return adapter
  }
}
