package com.blueground.loggingpolicy.graphql

import com.blueground.loggingpolicy.domain.products.Product
import com.blueground.loggingpolicy.domain.products.ProductService
import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsData

@DgsComponent
class ProductDataFetcher(
  private val productService: ProductService
) {

  @DgsData(parentType = "Query", field = "products")
  fun products(offset: Int = 0, limit: Int = 10): List<Product> {
    return productService.getProducts(offset, limit)
  }
}
