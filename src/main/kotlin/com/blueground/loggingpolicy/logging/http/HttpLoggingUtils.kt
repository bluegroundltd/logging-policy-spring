package com.blueground.loggingpolicy.logging.http

import com.blueground.loggingpolicy.logging.http.HttpAttributes.*
import com.blueground.loggingpolicy.logging.http.NetworkAttributes.ClientDetails
import com.blueground.loggingpolicy.logging.http.NetworkAttributes.DestinationDetails
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import ua_parser.Parser


private val userAgentParser = Parser()

fun extractHttpLoggingAttributes(request: HttpServletRequest, response: HttpServletResponse): HttpAttributes {
  val url = request.requestURL.toString()
  val referer = request.getHeader(HttpHeaders.REFERER)
  val method = request.method
  val userAgentHeader = request.getHeader(HttpHeaders.USER_AGENT)
  val version = request.protocol

  // Extract URL details
  val host = request.serverName
  val port = request.serverPort
  val path = request.requestURI
  val queryString = request.queryString
  val scheme = request.scheme

  // Extract HTTP status code
  val statusCode = response.status

  // Parse user-agent details (using ua_parser)
  val uaClient = if (userAgentHeader != null) userAgentParser.parse(userAgentHeader) else null
  val osFamily = uaClient?.os?.family
  val browserFamily = uaClient?.userAgent?.family
  val deviceFamily = uaClient?.device?.family

  val headers = request.headerNames.toList().map { headerName ->
    headerName to request.getHeader(headerName)
  }.toMap()


  return HttpAttributes(
    url = url,
    referer = referer,
    method = method,
    statusCode = statusCode,
    userAgent = userAgentHeader,
    version = version,
    headers = headers,
    urlDetails = UrlDetails(
      host = host,
      port = port,
      path = path,
      queryString = queryString,
      scheme = scheme
    ),
    userAgentDetails = UserAgentDetails(
      os = OSDetails(osFamily),
      browser = BrowserDetails(browserFamily),
      device = DeviceDetails(deviceFamily)
    )
  )
}

fun extractNetworkAttributes(request: HttpServletRequest, response: HttpServletResponse): NetworkAttributes {
  // Bytes read and written might not be directly available,
  // you can estimate them using request size and response size.
  val bytesRead = request.contentLengthLong.takeIf { it > 0 } ?: 0L
  val bytesWritten = response.getHeader("content-length")?.toLongOrNull() ?: 0L

  // Extract external IP from the X-Forwarded-For or X-Real-IP headers (if present)
  val clientIp = request.getHeader("true-client-ip")
    ?: request.getHeader("cf-connecting-ip")
    ?: request.getHeader("x-forwarded-for")
    ?: request.remoteAddr

  // External port (you might not get this from HTTP headers,
  // but could infer it if load balancing headers are used)
  val clientPort = request.getHeader("x-forwarded-port")?.toIntOrNull()
    ?: request.remotePort

  // Client internal IP
  val clientInternalIp = request.remoteAddr

  // Destination IP (typically the server IP)
  val destinationIp = request.localAddr
  val destinationPort = request.localPort


  // GeoIP
  // See https://developers.cloudflare.com/rules/transform/managed-transforms/reference/#add-visitor-location-headers
  val cityName = request.getHeader("cf-ipcity")
  val countryIsoCode = request.getHeader("cf-ipcountry")
  val continentCode = request.getHeader("cf-ipcontinent")
  val regionName = request.getHeader("cf-region")
  val regionCode = request.getHeader("cf-region-code")

  val geoIpDetails = NetworkAttributes.GeoIpDetails(
    cityName = cityName,
    country = NetworkAttributes.Country(
      isoCode = countryIsoCode
    ),
    continent = NetworkAttributes.Continent(
      code = continentCode,
    ),
    subdivision = NetworkAttributes.Subdivision(
      name = regionName,
      isoCode = regionCode
    )
  )

  return NetworkAttributes(
    bytesRead = bytesRead,
    bytesWritten = bytesWritten,
    client = ClientDetails(
      ip = clientIp,
      port = clientPort,
      internalIp = clientInternalIp,
      geoIp = geoIpDetails
    ),
    destination = DestinationDetails(
      ip = destinationIp,
      port = destinationPort
    )
  )
}
