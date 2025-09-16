package com.blueground.loggingpolicy.logging.async

import org.slf4j.MDC
import org.springframework.core.task.TaskDecorator

class MdcTaskDecorator : TaskDecorator {

  override fun decorate(runnable: Runnable): Runnable {
    val contextMap = MDC.getCopyOfContextMap().orEmpty()
    return Runnable {
      try {
        contextMap.entries.forEach { entry ->
          MDC.put(entry.key, entry.value)
        }
        runnable.run()
      } finally {
        MDC.clear()
      }
    }
  }
}
