package com.blueground.loggingpolicy.config

import com.blueground.loggingpolicy.logging.rest.RestTemplateInterceptor
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.boot.web.client.RestTemplateCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Scope
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.web.client.RestTemplate

@Configuration
class RestTemplateConfig {

  @Bean
  fun restTemplateCustomizer(objectMapper: ObjectMapper): RestTemplateCustomizer {
    return RestTemplateCustomizer { restTemplate ->
      val interceptors = restTemplate.interceptors.toMutableList()
      interceptors.add(RestTemplateInterceptor())
      restTemplate.messageConverters.add(0, MappingJackson2HttpMessageConverter(objectMapper))
      restTemplate.interceptors = interceptors
    }
  }

  @Bean
  @Scope("prototype")
  fun restTemplate(restTemplateCustomizer: RestTemplateCustomizer): RestTemplate {
    val restTemplate = RestTemplate()
    restTemplateCustomizer.customize(restTemplate)
    return restTemplate
  }
}
