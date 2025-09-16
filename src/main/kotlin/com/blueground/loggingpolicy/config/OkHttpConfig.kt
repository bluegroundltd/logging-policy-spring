package com.blueground.loggingpolicy.config

import com.blueground.loggingpolicy.logging.okhttp.OkHttpInterceptor
import okhttp3.OkHttpClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Scope

@Configuration
class OkHttpConfig {

  @Bean
  @Scope("prototype")
  fun okHttpClient(): OkHttpClient {
    return OkHttpClient.Builder()
      .addInterceptor(OkHttpInterceptor())
      .build()
  }
}
