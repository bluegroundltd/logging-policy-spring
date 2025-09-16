package com.blueground.loggingpolicy.http

import com.blueground.loggingpolicy.domain.orders.Order
import com.blueground.loggingpolicy.domain.orders.OrderRequest
import com.blueground.loggingpolicy.domain.orders.OrderService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/orders")
class OrderController(
  private val kafkaProducer: com.blueground.loggingpolicy.kafka.KafkaOrderProducer,
  private val rabbitProducer: com.blueground.loggingpolicy.rabbit.RabbitOrderProducer,
  private val orderService: OrderService
) {

  @PostMapping("/rabbit")
  fun submitOrderRabbit(@RequestBody order: OrderRequest): OrderSubmitResponse {
    rabbitProducer.sendOrder(order)
    return OrderSubmitResponse("Order submitted via RabbitMQ")
  }

  @PostMapping("/kafka")
  fun submitOrderKafka(
    @RequestBody order: OrderRequest
  ): OrderSubmitResponse {
    kafkaProducer.sendOrder(order)
    return OrderSubmitResponse("Order submitted via Kafka")
  }

  @GetMapping
  fun getOrders(): List<Order> {
    return orderService.getOrders()
  }

  data class OrderSubmitResponse(val status: String)
}
