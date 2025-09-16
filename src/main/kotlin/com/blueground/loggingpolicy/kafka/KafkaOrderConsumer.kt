package com.blueground.loggingpolicy.kafka

import com.blueground.loggingpolicy.domain.orders.OrderRequest
import com.blueground.loggingpolicy.domain.orders.OrderService
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component

@Component
class KafkaOrderConsumer(
  private val orderService: OrderService
) {

  companion object {
    private val logger = LoggerFactory.getLogger(KafkaOrderConsumer::class.java)
  }

  @KafkaListener(topics = ["order.request"], groupId = "logging_policy")
  fun receiveOrder(@Payload order: OrderRequest) {
    logger.atInfo()
      .addKeyValue("order", order)
      .log("[kafka] Received OrderRequest message")
    orderService.createOrder(order)
  }
}
