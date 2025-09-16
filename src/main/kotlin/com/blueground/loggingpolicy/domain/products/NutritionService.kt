package com.blueground.loggingpolicy.domain.products

import com.fasterxml.jackson.databind.ObjectMapper
import okhttp3.OkHttpClient
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class NutritionService(
  private val restTemplate: RestTemplate,
  private val okHttpClient: OkHttpClient,
  private val objectMapper: ObjectMapper
) {

  companion object {
    // Go to https://mockbin.io/bins/cf6a7196c25648e3ac240530d1ce6120
    // to see the outgoing HTTP requests
    private const val API_URL = "https://cf6a7196c25648e3ac240530d1ce6120.api.mockbin.io/?productId={productId}"
    private val logger = LoggerFactory.getLogger(NutritionService::class.java)
  }

  fun fetchNutritionFactsWithRestTemplate(product: Product): NutritionFacts? {
    val scope = "[restTemplate]"

    return try {
      logger
        .atInfo()
        .addKeyValue("productId", product.id)
        .addKeyValue("url", API_URL)
        .log("$scope Fetching nutrition facts (productId=${product.id})")

      restTemplate.getForObject(API_URL, NutritionFacts::class.java, product.id)
    } catch (e: Exception) {
      logger
        .atError()
        .addKeyValue("productId", product.id)
        .addKeyValue("API_URL", API_URL)
        .log("$scope Failed to fetch nutrition facts (productId=${product.id})", e)
      null
    }
  }

  fun fetchNutritionFactsWithOkHttp(product: Product): NutritionFacts? {
    val scope = "[okHttp]"

    return try {
      logger
        .atInfo()
        .addKeyValue("productId", product.id)
        .addKeyValue("url", API_URL)
        .log("$scope Fetching nutrition facts (productId=${product.id})")

      val request = okhttp3.Request.Builder()
        .url(API_URL.replace("{productId}", product.id.toString()))
        .build()

      val response = okHttpClient.newCall(request).execute()

      if (response.isSuccessful) {
        objectMapper.readValue(response.body?.string(), NutritionFacts::class.java)
      } else {
        throw Exception("HTTP error ${response.code}")
      }
    } catch (e: Exception) {
      logger
        .atError()
        .addKeyValue("productId", product.id)
        .addKeyValue("API_URL", API_URL)
        .log("$scope Failed to fetch nutrition facts (productId=${product.id})", e)
      null
    }
  }
}
