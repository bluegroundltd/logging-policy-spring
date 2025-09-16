# Spring Boot logging policy reference implementation

👀 [Logging Policy](https://theblueground.getoutline.com/doc/logging-policy-a74XHsph7v)

© Blueground 

## Features

* HTTP ✅
* GraphQL ✅
* Kafka ✅
* RabbitMQ ✅
* Jobrunr ✅
* RestTemplate ✅
* OkHttp ✅
* Async ✅

## Getting started

### Run the application

```sh
$ docker compose up -d
$ ./gradlew bootRun
```

### Issue HTTP requests

Use the provided [Postman Collection](https://github.com/bluegroundltd/logging-policy-spring/blob/main/postman.json)

Recommended sequence of requests:

1. SignIn: `POST /signin`
2. Home: `GET /`
3. Get Products via REST: `GET /products`
4. Get Products via GraphQL: `POST /graphql`
5. Create Order via Kafka: `POST /orders/kafka`
6. Create Order via RabbitMQ: `POST /orders/rabbit`
7. Get submitted orders: `GET /orders`
8. Test an error: `POST /kaboom`
9. Test an outgoing HTTP request: `GET /products/2`. Check out the request
   on [mockbin](https://mockbin.io/bins/cf6a7196c25648e3ac240530d1ce6120)

## Logback configuration

The logback configuration is
in [logback-spring.xml](https://github.com/bluegroundltd/logging-policy-spring/blob/main/src/main/resources/logback-spring.xml)

Pretty-printing in `dev` profile is implemented exclusively
inside [logback-spring.xml](https://github.com/bluegroundltd/logging-policy-spring/blob/main/src/main/resources/logback-spring.xml)
using
a [logstash encoder prefix](https://github.com/logfellow/logstash-logback-encoder/tree/logstash-logback-encoder-4.7?tab=readme-ov-file#prefix_suffix)

```xml

<prefix class="ch.qos.logback.core.encoder.LayoutWrappingEncoder">
  <layout class="ch.qos.logback.classic.PatternLayout">
    <!-- https://github.com/logfellow/logstash-logback-encoder?tab=readme-ov-file#using-with-patternlayout-->
    <pattern>%d{HH:mm:ss.SSS} %highlight_level(%level) %logger{0} %cyan(%msg) %n%red(%stack{30,full,full,true,true})</pattern>
  </layout>
</prefix>
```

## HTTP requests and GraphQL

Implemented
in [HttpLoggingFilter](https://github.com/bluegroundltd/logging-policy-spring/blob/main/src/main/kotlin/com/blueground/loggingpolicy/logging/http/HttpLoggingFilter.kt)

GraphQL attributes are captured
in [GraphQLInstrumentation](https://github.com/bluegroundltd/logging-policy-spring/blob/main/src/main/kotlin/com/blueground/loggingpolicy/logging/graphql/GraphQLInstrumentation.kt)
and injected into
the [ServletRequest](https://jakarta.ee/specifications/platform/9/apidocs/?jakarta/servlet/ServletRequest.html) so that
they can be logged in
the [HttpLoggingFilter](https://github.com/bluegroundltd/logging-policy-spring/blob/main/src/main/kotlin/com/blueground/loggingpolicy/logging/http/HttpLoggingFilter.kt)
in one event and avoid redundant logging.

Example of an HTTP request log:

```sh
18:56:51.737 INFO HttpLoggingFilter [req] POST /signin 
{
  "http" : {
    "url" : "http://localhost:3000/signin",
    "referer" : null,
    "method" : "POST",
    "status_code" : 200,
    "useragent" : "PostmanRuntime/7.42.0",
    "version" : "HTTP/1.1",
    "headers" : {
      "content-type" : "application/json",
      "user-agent" : "PostmanRuntime/7.42.0",
      "accept" : "*/*",
      "postman-token" : "47c17b35-0d95-410d-b4e6-e07751e2bf16",
      "host" : "localhost:3000",
      "accept-encoding" : "gzip, deflate, br",
      "connection" : "keep-alive",
      "content-length" : "61",
      "cookie" : "JSESSIONID=96E5FBB45C8C0311B2F5ACD5486D2DE1"
    },
    "url_details" : {
      "host" : "localhost",
      "port" : 3000,
      "path" : "/signin",
      "queryString" : null,
      "scheme" : "http"
    },
    "useragent_details" : {
      "os" : {
        "family" : "Other"
      },
      "browser" : {
        "family" : "PostmanRuntime"
      },
      "device" : {
        "family" : "Other"
      }
    }
  },
  "network" : {
    "bytes_read" : 61,
    "bytes_written" : 0,
    "client" : {
      "ip" : "0:0:0:0:0:0:0:1",
      "port" : 57342,
      "internal_ip" : "0:0:0:0:0:0:0:1",
      "geoip" : {
        "city_name" : null,
        "country" : {
          "iso_code" : null
        },
        "continent" : {
          "code" : null
        },
        "subdivision" : {
          "name" : null,
          "iso_code" : null
        }
      }
    },
    "destination" : {
      "ip" : "0:0:0:0:0:0:0:1",
      "port" : 3000
    }
  },
  "traceId" : "672ab0c37ba959229091c3d98e44d274",
  "spanId" : "20fe7fb69f8d6634",
  "correlationId" : "1-1730851011-2oS7EWepxVnsVNcT22Fu4S7yVnq",
  "entrypoint" : "http",
  "usr.name" : "anonymousUser",
  "logger" : "com.blueground.loggingpolicy.logging.http.HttpLoggingFilter",
  "thread_name" : "http-nio-3000-exec-2"
}
```

Example of a GraphQL request log:

```sh
19:34:52.760 INFO HttpLoggingFilter [res] POST /graphql 200 OK (15.386s) 
{
  "http" : {
    "url" : "http://localhost:3000/graphql",
    "referer" : null,
    "method" : "POST",
    "status_code" : 200,
    "useragent" : "PostmanRuntime/7.42.0",
    "version" : "HTTP/1.1",
    "headers" : {
      "accept-language" : "en,en-US;q=0.9,el;q=0.8,fr;q=0.7",
      "cache-control" : "no-cache",
      "origin" : "http://localhost:3000",
      "pragma" : "no-cache",
      "accept" : "application/json, multipart/mixed",
      "content-type" : "application/json",
      "user-agent" : "PostmanRuntime/7.42.0",
      "postman-token" : "0351a971-ba8d-4093-b104-091077409cc4",
      "host" : "localhost:3000",
      "accept-encoding" : "gzip, deflate, br",
      "connection" : "keep-alive",
      "content-length" : "181",
      "cookie" : "JSESSIONID=529EA42E9910790559AD26E8F8510C0B"
    },
    "url_details" : {
      "host" : "localhost",
      "port" : 3000,
      "path" : "/graphql",
      "queryString" : null,
      "scheme" : "http"
    },
    "useragent_details" : {
      "os" : {
        "family" : "Other"
      },
      "browser" : {
        "family" : "PostmanRuntime"
      },
      "device" : {
        "family" : "Other"
      }
    }
  },
  "network" : {
    "bytes_read" : 181,
    "bytes_written" : 0,
    "client" : {
      "ip" : "0:0:0:0:0:0:0:1",
      "port" : 59310,
      "internal_ip" : "0:0:0:0:0:0:0:1",
      "geoip" : {
        "city_name" : null,
        "country" : {
          "iso_code" : null
        },
        "continent" : {
          "code" : null
        },
        "subdivision" : {
          "name" : null,
          "iso_code" : null
        }
      }
    },
    "destination" : {
      "ip" : "0:0:0:0:0:0:0:1",
      "port" : 3000
    }
  },
  "graphql" : {
    "operationType" : "query",
    "operationName" : "Undefined",
    "operationBody" : "query GetProducts($offset: Int, $limit: Int) {\n    products(offset: $offset, limit: $limit) {\n        id,\n        name\n    }\n}",
    "variables" : {
      "offset" : 0,
      "limit" : 5
    },
    "responseTime" : 6163,
    "responseStatus" : "Success",
    "parsingTimeMs" : 8,
    "validationTimeMs" : 25,
    "executionTimeMs" : 34,
    "errors" : [ ]
  },
  "traceId" : "672ab99d4aa97bdb7180e8d3de63fdc1",
  "spanId" : "bee21f7d3f19085e",
  "correlationId" : "1-1730853277-2oSBpLOGFdkfmM4h3gJGnbGCruM",
  "entrypoint" : "http",
  "usr.name" : "jane@test.app",
  "logger" : "com.blueground.loggingpolicy.logging.http.HttpLoggingFilter",
  "thread_name" : "http-nio-3000-exec-3"
}
```

## Kafka

* [LoggingProducerInterceptor](https://github.com/bluegroundltd/logging-policy-spring/blob/main/src/main/kotlin/com/blueground/loggingpolicy/logging/kafka/LoggingProducerInterceptor.kt)
  propagates the `correlationId` to produced messages.
* [LoggingRecordInterceptor](https://github.com/bluegroundltd/logging-policy-spring/blob/main/src/main/kotlin/com/blueground/loggingpolicy/logging/kafka/LoggingRecordInterceptor.kt)
  updates the MDC with the `correlationId` of the consumed message.

Example of a Kafka message log:

```sh
19:24:02.796 INFO KafkaOrderConsumer Received OrderRequest message 
{
  "order" : {
    "userId" : 123,
    "productId" : 1,
    "quantity" : 2
  },
  "entrypoint" : "kafka/order.request",
  "correlationId" : "1-1730852642-2oSAXU0BwDuWguEypEpJYKj6dbg",
  "logger" : "com.blueground.loggingpolicy.kafka.KafkaOrderConsumer",
  "thread_name" : "org.springframework.kafka.KafkaListenerEndpointContainer#0-0-C-1"
}
```

## RabbitMQ

* [LoggingMessageProcessor](https://github.com/bluegroundltd/logging-policy-spring/blob/main/src/main/kotlin/com/blueground/loggingpolicy/logging/rabbit/LoggingMessageProcessor.kt)
  propagates the `correlationId` to produced messages.
* [LoggingConsumerAdvice](https://github.com/bluegroundltd/logging-policy-spring/blob/main/src/main/kotlin/com/blueground/loggingpolicy/logging/rabbit/LoggingConsumerAdvice.kt)
  updates the MDC with the `correlationId` of the consumed message.

```sh
19:25:17.967 INFO RabbitOrderConsumer Received OrderRequest message 
{
  "order" : {
    "userId" : 123,
    "productId" : 1,
    "quantity" : 2
  },
  "entrypoint" : "rabbit/order.request",
  "correlationId" : "1-1730852717-2oSAgxzArJAshyerU9SMmAXWsLM",
  "logger" : "com.blueground.loggingpolicy.rabbit.RabbitOrderConsumer",
  "thread_name" : "container-1"
}
```

## Jobrunr

Works out of the box
with [first class support](https://www.jobrunr.io/en/documentation/background-methods/logging-progress/) for MDC and
SLF4J.

## HTTP clients

See [RestTemplateInterceptor](https://github.com/bluegroundltd/logging-policy-spring/blob/main/src/main/kotlin/com/blueground/loggingpolicy/logging/rest/RestTemplateInterceptor.kt)
for RestTemplate

See [OkHttpInterceptor](https://github.com/bluegroundltd/logging-policy-spring/blob/main/src/main/kotlin/com/blueground/loggingpolicy/logging/okhttp/OkHttpInterceptor.kt)
for OkHttp

## Async tasks

See [MdcTaskDecorator](https://github.com/bluegroundltd/logging-policy-spring/blob/main/src/main/kotlin/com/blueground/loggingpolicy/logging/async/MdcTaskDecorator.kt)

## 💡 How to log domain and service attributes

```kotlin
logger
  .atInfo()
  .addKeyValue("domain.foo", foo)
  .addKeyValue("domain.bar", bar)
  .addKeyValue("service.qux", bar)
  .log("Message")
```

This would be parsed as:

```json
{
  "domain": {
    "foo": "foo",
    "bar": "bar"
  },
  "service": {
    "qux": "qux"
  },
  "message": "Message"
}
```

