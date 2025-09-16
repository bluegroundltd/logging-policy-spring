package com.blueground.loggingpolicy.domain.products

data class Product(
  val id: Long,
  val name: String,
  val calories: Int,
  val price: Int,
  var nutritionFacts: NutritionFacts? = null
)
