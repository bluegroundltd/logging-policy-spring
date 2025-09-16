package com.blueground.loggingpolicy.http.auth

import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.web.context.HttpSessionSecurityContextRepository
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class AuthController(
  private val authenticationManager: AuthenticationManager
) {

  companion object {
    private val logger = LoggerFactory.getLogger(AuthController::class.java)
  }

  @PostMapping("/signin")
  fun signIn(@RequestBody signInRequest: SignInRequest, request: HttpServletRequest): ResponseEntity<String> {
    val authenticationToken = UsernamePasswordAuthenticationToken(
      signInRequest.username, signInRequest.password
    )

    return try {
      val authentication = authenticationManager.authenticate(authenticationToken)
      SecurityContextHolder.getContext().authentication = authentication

      val session = request.getSession(true) // Create a session if it doesn't exist
      session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, SecurityContextHolder.getContext())

      // Retrieve user details from the Authentication object
      val userDetails = authentication.principal as UserDetails

      logger.info("User signed in (username=${userDetails.username})")
      // In a real-world app, generate a JWT or return session info here
      ResponseEntity.ok("Signed in as ${userDetails.username}")
    } catch (e: AuthenticationException) {
      logger.warn("Sign-in failed", e)
      ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials")
    }
  }

  @PostMapping("/signout")
  fun signOut(request: HttpServletRequest, response: HttpServletResponse): ResponseEntity<String> {
    return try {
      val userDetails = SecurityContextHolder.getContext()?.authentication?.principal

      // Invalidate the session
      request.getSession(false)?.invalidate()

      // Clear the SecurityContext
      SecurityContextHolder.clearContext()

      // Clear any cookies related to the session
      val cookie = Cookie("JSESSIONID", null).apply {
        maxAge = 0
        path = "/"
      }
      response.addCookie(cookie)

      if (userDetails is UserDetails) {
        logger.info("User signed out (username=${userDetails.username})")
      } else {
        logger.info("User signed out (username=${userDetails})")
      }

      ResponseEntity.ok("Signed out successfully")
    } catch (e: Exception) {
      logger.warn("An error occurred during sign-out", e)
      ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred during sign-out")
    }
  }

}
