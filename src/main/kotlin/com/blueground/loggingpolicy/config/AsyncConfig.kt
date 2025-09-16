package com.blueground.loggingpolicy.config

import com.blueground.loggingpolicy.logging.async.MdcTaskDecorator
import org.springframework.boot.task.ThreadPoolTaskExecutorBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableAsync
import java.util.concurrent.Executor


@EnableAsync
@Configuration
class AsyncConfig {

  @Bean("asyncTaskExecutor")
  fun taskExecutor(): Executor {
    val coreCount = Runtime.getRuntime().availableProcessors()
    return ThreadPoolTaskExecutorBuilder()
      .corePoolSize(coreCount)
      .maxPoolSize(coreCount)
      .threadNamePrefix("task-")
      .taskDecorator(MdcTaskDecorator())
      .build()
  }
}
