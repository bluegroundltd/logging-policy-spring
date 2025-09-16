package com.blueground.loggingpolicy.domain.orders

import com.blueground.loggingpolicy.db.Database
import com.blueground.loggingpolicy.domain.notify.Push
import org.jobrunr.scheduling.BackgroundJob
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Instant


@Service
class OrderService(
  private val database: Database,
  private val push: Push
) {

  companion object {
    private val logger = LoggerFactory.getLogger(OrderService::class.java)
  }

  fun createOrder(orderRequest: OrderRequest): Order {
    val order = Order(
      userId = orderRequest.userId,
      productId = orderRequest.productId,
      quantity = orderRequest.quantity
    )
    database.saveOrder(order)

    logger
      .atInfo()
      .addKeyValue("domain.orderId", order.orderId)
      .addKeyValue("domain.productId", order.productId)
      .log("Created order (orderId=${order.orderId})")
    BackgroundJob.enqueue { acceptOrder(order.orderId) }
    return order
  }

  fun acceptOrder(orderId: String) {
    val order = database.getOrder(orderId) ?: throw OrderNotFoundException(orderId)
    order.status = OrderStatus.ACCEPTED
    database.saveOrder(order)
    logger
      .atInfo()
      .addKeyValue("domain.orderId", order.orderId)
      .addKeyValue("domain.productId", order.productId)
      .log("Accepted order (orderId=${orderId})")
    push.notify(order)
    BackgroundJob.schedule(Instant.now().plusSeconds(2)) { prepareOrder(orderId) }
  }

  fun prepareOrder(orderId: String) {
    val order = database.getOrder(orderId) ?: throw OrderNotFoundException(orderId)
    order.status = OrderStatus.PREPARING
    database.saveOrder(order)
    logger
      .atInfo()
      .addKeyValue("domain.orderId", order.orderId)
      .addKeyValue("domain.productId", order.productId)
      .log("Preparing order (orderId=${orderId})")
    push.notify(order)
    BackgroundJob.enqueue { deliverOrder(orderId) }
  }

  fun deliverOrder(orderId: String) {
    val order = database.getOrder(orderId) ?: throw OrderNotFoundException(orderId)
    order.status = OrderStatus.ON_DELIVERY
    database.saveOrder(order)
    logger
      .atInfo()
      .addKeyValue("domain.orderId", order.orderId)
      .addKeyValue("domain.productId", order.productId)
      .log("Delivering order (orderId=${orderId})")
    push.notify(order)
    BackgroundJob.schedule(Instant.now().plusSeconds(2)) { completeOrder(orderId) }
  }

  fun completeOrder(orderId: String) {
    val order = database.getOrder(orderId) ?: throw OrderNotFoundException(orderId)
    order.status = OrderStatus.DELIVERED
    database.saveOrder(order)
    push.notify(order)
    logger
      .atInfo()
      .addKeyValue("domain.orderId", order.orderId)
      .addKeyValue("domain.productId", order.productId)
      .log("Completed order (orderId=${orderId})")
  }

  fun getOrders(): List<Order> {
    return database.listOrders()
  }

}


class OrderNotFoundException(orderId: String) : Throwable() {
  override val message: String = "Order not found (orderId=$orderId)"
}
