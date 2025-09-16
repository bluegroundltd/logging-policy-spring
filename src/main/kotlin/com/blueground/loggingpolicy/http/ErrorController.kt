package com.blueground.loggingpolicy.http

import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class ErrorController {

  companion object {
    private val logger = LoggerFactory.getLogger(ErrorController::class.java)
  }

  @PostMapping("/kaboom")
  fun error(): ResponseEntity<String> {
    return try {
      val result = kaboom()
      ResponseEntity.ok(result)
    } catch (e: Exception) {
      logger.error("An error occurred: ${e.message}", e)
      ResponseEntity.status(500).body("An error occurred: ${e.message}")
    }
  }

  fun kaboom(): String {
    throw RuntimeException("Kaboom!")
  }
}
