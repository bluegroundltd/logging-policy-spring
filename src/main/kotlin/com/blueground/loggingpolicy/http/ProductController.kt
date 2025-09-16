package com.blueground.loggingpolicy.http

import com.blueground.loggingpolicy.domain.products.Product
import com.blueground.loggingpolicy.domain.products.ProductService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/products")
class ProductController(
  private val productService: ProductService
) {

  @GetMapping
  fun getProducts(
    @RequestParam(value = "offset", defaultValue = "0") offset: Int,
    @RequestParam(value = "limit", defaultValue = "10") limit: Int
  ): List<Product> {
    return productService.getProducts(offset, limit)
  }

  @GetMapping("/{id}")
  fun getProductById(
    @PathVariable(value = "id") id: Long
  ): Product? {
    return productService.getProductById(id)
  }

}
