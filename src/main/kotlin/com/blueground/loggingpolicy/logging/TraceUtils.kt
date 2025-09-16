package com.blueground.loggingpolicy.logging

import com.github.ksuid.Ksuid
import java.time.Clock


const val version: String = "1"

const val CORRELATION_ID_HEADER = "correlationId"
const val CORRELATION_ID_HEADER_HTTP = "x-correlation-id"

/**
 * Generates a correlation id using the KSUID library.
 *
 * Format: "version-timestamp-uniqueId" where:
 *
 * - version: monotonically increasing integer. E.g. 1, 2, 3, ...
 * - timestamp: Epoch in seconds
 * - uniqueId: KSUID/UUID
 */
fun generateCorrelationId(clock: Clock = Clock.systemUTC()): String {
  val instant = clock.instant()
  val timestamp = instant.epochSecond
  val ksuid = Ksuid.fromInstant(instant).toString()
  return "$version-$timestamp-$ksuid"
}
