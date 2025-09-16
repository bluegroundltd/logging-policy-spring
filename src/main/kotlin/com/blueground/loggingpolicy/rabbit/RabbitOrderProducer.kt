package com.blueground.loggingpolicy.rabbit

import com.blueground.loggingpolicy.domain.orders.OrderRequest
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Service

@Service
class RabbitOrderProducer(
  private val rabbitTemplate: RabbitTemplate
) {

  companion object {
    private const val EXCHANGE = "order.request"
    private const val ROUTING_KEY = "default"
  }

  fun sendOrder(order: OrderRequest) {
    rabbitTemplate.convertAndSend(EXCHANGE, ROUTING_KEY, order)
  }
}
