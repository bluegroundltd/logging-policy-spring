package com.blueground.loggingpolicy.rabbit

import com.blueground.loggingpolicy.domain.orders.OrderRequest
import com.blueground.loggingpolicy.domain.orders.OrderService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class RabbitOrderConsumer(
  private val orderService: OrderService
) {

  companion object {
    private val logger = LoggerFactory.getLogger(RabbitOrderConsumer::class.java)
  }

  fun receiveOrder(order: OrderRequest) {
    logger.atInfo().addKeyValue("order", order).log("[rabbit] Received OrderRequest message")
    orderService.createOrder(order)
  }

}
