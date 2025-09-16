package com.blueground.loggingpolicy.logging.http

import com.fasterxml.jackson.annotation.JsonProperty

data class NetworkAttributes(
  @JsonProperty("bytes_read")
  val bytesRead: Long,
  @JsonProperty("bytes_written")
  var bytesWritten: Long,
  @JsonProperty("client")
  val client: ClientDetails,
  @JsonProperty("destination")
  val destination: DestinationDetails
) {

  data class ClientDetails(
    @JsonProperty("ip")
    val ip: String?,
    @JsonProperty("port")
    val port: Int?,
    @JsonProperty("internal_ip")
    val internalIp: String?,
    @JsonProperty("geoip")
    val geoIp: GeoIpDetails?
  )

  data class GeoIpDetails(
    @JsonProperty("city_name")
    val cityName: String?,
    @JsonProperty("country")
    val country: Country?,
    @JsonProperty("continent")
    val continent: Continent?,
    @JsonProperty("subdivision")
    val subdivision: Subdivision?
  )

  data class Country(
    @JsonProperty("iso_code")
    val isoCode: String?
  )

  data class Continent(
    @JsonProperty("code")
    val code: String?
  )

  data class Subdivision(
    @JsonProperty("name")
    val name: String?,
    @JsonProperty("iso_code")
    val isoCode: String?
  )

  data class DestinationDetails(
    @JsonProperty("ip")
    val ip: String?,
    @JsonProperty("port")
    val port: Int?
  )
}
