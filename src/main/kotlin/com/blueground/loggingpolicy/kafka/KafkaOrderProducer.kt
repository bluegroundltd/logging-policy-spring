package com.blueground.loggingpolicy.kafka

import com.blueground.loggingpolicy.domain.orders.OrderRequest
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service

@Service
class KafkaOrderProducer(
  private val kafkaTemplate: KafkaTemplate<String, OrderRequest>
) {

  private val topic = "order.request"

  fun sendOrder(order: OrderRequest) {
    kafkaTemplate.send(topic, order.productId.toString(), order)
  }

}
