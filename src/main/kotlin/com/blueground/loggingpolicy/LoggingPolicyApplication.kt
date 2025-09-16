package com.blueground.loggingpolicy

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class LoggingPolicyApplication

fun main(args: Array<String>) {
	runApplication<LoggingPolicyApplication>(*args)
}
