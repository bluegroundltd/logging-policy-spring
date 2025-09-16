package com.blueground.loggingpolicy.db

import com.blueground.loggingpolicy.domain.orders.Order
import org.springframework.stereotype.Service

@Service
class Database {

  private val data = mutableMapOf<String, Order>()

  fun saveOrder(order: Order) {
    data[order.orderId] = order
  }

  fun getOrder(orderId: String): Order? {
    return data[orderId]
  }

  fun listOrders(): List<Order> {
    return data.values.toList()
  }

}
