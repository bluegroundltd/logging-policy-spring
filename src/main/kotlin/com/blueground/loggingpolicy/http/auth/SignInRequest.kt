package com.blueground.loggingpolicy.http.auth

data class SignInRequest(
  val username: String,
  val password: String
)
