package com.blueground.loggingpolicy.domain.orders

import kotlinx.serialization.Serializable

@Serializable
data class OrderRequest(
  val userId: Long,
  val productId: Long,
  val quantity: Int
)


