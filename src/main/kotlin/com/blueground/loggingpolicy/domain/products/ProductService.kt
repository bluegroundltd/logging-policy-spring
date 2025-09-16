package com.blueground.loggingpolicy.domain.products

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class ProductService(
  private val nutritionService: NutritionService
) {

  companion object {
    private val logger = LoggerFactory.getLogger(ProductService::class.java)
  }

  private val products = listOf(
    Product(1, "Chips", 500, 199),
    Product(2, "Apple", 95, 50),
    Product(3, "Banana", 105, 30),
    Product(4, "Orange", 62, 60),
    Product(5, "Milk", 150, 150),
    Product(6, "Bread", 80, 200),
    Product(7, "Eggs", 78, 250),
    Product(8, "Cheese", 113, 300),
    Product(9, "Yogurt", 59, 100),
    Product(10, "Chicken Breast", 165, 500),
    Product(11, "Salmon Fillet", 208, 800),
    Product(12, "Almonds", 164, 350),
    Product(13, "Oatmeal", 150, 180),
    Product(14, "Spaghetti", 200, 120),
    Product(15, "White Rice", 206, 110),
    Product(16, "Broccoli", 55, 75),
    Product(17, "Carrots", 41, 50),
    Product(18, "Potatoes", 161, 90),
    Product(19, "Tomatoes", 22, 70),
    Product(20, "Lettuce", 15, 65),
    Product(21, "Peanut Butter", 188, 250)
  )

  fun getProducts(offset: Int = 0, limit: Int = 10): List<Product> {
    logger.info("Fetching products (offset=$offset) (limit=$limit)")
    return products.subList(offset, offset + limit)
  }

  fun getProductById(id: Long): Product? {
    logger.info("Fetching product by id (id=$id)")

    val product = products.firstOrNull { it.id == id }
    if (product == null) return null

    if (Instant.now().epochSecond % 2 == 0L) {
      product.nutritionFacts = nutritionService.fetchNutritionFactsWithRestTemplate(product)
    } else {
      product.nutritionFacts = nutritionService.fetchNutritionFactsWithOkHttp(product)
    }

    return product
  }

}
