package com.blueground.loggingpolicy.domain.notify

import com.blueground.loggingpolicy.domain.orders.Order
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import java.util.concurrent.CompletableFuture


@Service
class Push {

  companion object {
    private val logger = LoggerFactory.getLogger(Push::class.java)
  }

  @Async
  @Throws(InterruptedException::class)
  fun notify(order: Order): CompletableFuture<Void> {
    Thread.sleep(1000L)
    logger.info("Order update: ${order.status}")
    return CompletableFuture.completedFuture(null)
  }

}
