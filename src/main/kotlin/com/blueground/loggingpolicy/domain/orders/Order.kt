package com.blueground.loggingpolicy.domain.orders

import java.util.*

data class Order(
  val orderId: String = UUID.randomUUID().toString(),
  val userId: Long,
  val productId: Long,
  val quantity: Int,
  var status: OrderStatus = OrderStatus.PENDING,
  val createAt: Date = Date()
)
