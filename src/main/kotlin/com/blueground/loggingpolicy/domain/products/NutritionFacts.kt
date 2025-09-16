package com.blueground.loggingpolicy.domain.products

data class NutritionFacts(
  val servingSize: String,
  val servingsPerContainer: Double,
  val calories: Int,
  val totalFat: Double,
  val saturatedFat: Double,
  val transFat: Double,
  val cholesterol: Double,
  val sodium: Double,
  val totalCarbohydrate: Double,
  val dietaryFiber: Double,
  val totalSugars: Double,
  val addedSugars: Double,
  val protein: Double,
  val vitaminD: Double,
  val calcium: Double,
  val iron: Double,
  val potassium: Double
)
