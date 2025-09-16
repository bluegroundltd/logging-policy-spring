package com.blueground.loggingpolicy.config

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.provisioning.InMemoryUserDetailsManager
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.session.HttpSessionEventPublisher


@Configuration
@EnableWebSecurity
class SecurityConfig {

  @Bean
  // Make sure that the Spring Security session registry is notified
  // when the session is destroyed
  fun httpSessionEventPublisher(): HttpSessionEventPublisher {
    return HttpSessionEventPublisher()
  }

  @Bean
  @Throws(Exception::class)
  fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
    return http
      .sessionManagement { session ->
        session
          .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
          .sessionFixation().newSession()
      }
      .csrf { it.disable() }
      .authorizeHttpRequests {
        it
          .requestMatchers("/", "/signin", "/signout", "/kaboom", "/graphiql")
          .permitAll()
          .anyRequest()
          .authenticated()
      }
      .exceptionHandling {
        it.authenticationEntryPoint(UnauthorizedEntryPoint())
      }
      .build()
  }

  @Bean
  @Throws(Exception::class)
  fun authenticationManager(http: HttpSecurity): AuthenticationManager {
    return http.getSharedObject(AuthenticationManagerBuilder::class.java).also {
      it.userDetailsService(userDetailsService()).passwordEncoder(passwordEncoder())
    }.build()
  }

  @Bean
  fun userDetailsService(): UserDetailsService {
    val user: UserDetails = User
      .builder()
      .username("jane@test.app")
      .password(passwordEncoder().encode("foobar"))
      .roles("USER")
      .authorities(SimpleGrantedAuthority("USER"))
      .build()

    return InMemoryUserDetailsManager(user)
  }

  @Bean
  fun passwordEncoder(): PasswordEncoder {
    return BCryptPasswordEncoder()
  }

  class UnauthorizedEntryPoint : AuthenticationEntryPoint {
    override fun commence(
      request: HttpServletRequest,
      response: HttpServletResponse,
      authException: AuthenticationException
    ) {
      // Send a 401 Unauthorized response
      response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized: Authentication is required")
    }
  }

}
