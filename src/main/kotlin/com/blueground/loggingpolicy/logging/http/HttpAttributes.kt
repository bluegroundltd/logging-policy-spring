package com.blueground.loggingpolicy.logging.http

import com.fasterxml.jackson.annotation.JsonProperty


data class HttpAttributes(
  @JsonProperty("url")
  val url: String,
  @JsonProperty("referer")
  val referer: String?,
  @JsonProperty("method")
  val method: String,
  @JsonProperty("status_code")
  val statusCode: Int,
  @JsonProperty("useragent")
  val userAgent: String?,
  @JsonProperty("version")
  val version: String,
  @JsonProperty("headers")
  val headers: Map<String, String>,
  @JsonProperty("url_details")
  val urlDetails: UrlDetails,
  @JsonProperty("useragent_details")
  val userAgentDetails: UserAgentDetails?
) {

  data class UrlDetails(
    @JsonProperty("host")
    val host: String,
    @JsonProperty("port")
    val port: Int,
    @JsonProperty("path")
    val path: String,
    @JsonProperty("queryString")
    val queryString: String?,
    @JsonProperty("scheme")
    val scheme: String
  )

  data class UserAgentDetails(
    @JsonProperty("os") val os: OSDetails?,
    @JsonProperty("browser") val browser: BrowserDetails?,
    @JsonProperty("device") val device: DeviceDetails?
  )

  data class OSDetails(
    @JsonProperty("family") val family: String?
  )

  data class BrowserDetails(
    @JsonProperty("family") val family: String?
  )

  data class DeviceDetails(
    @JsonProperty("family") val family: String?
  )

}
