plugins {
  id("org.springframework.boot") version "3.3.2"
  id("io.spring.dependency-management") version "1.1.6"
  kotlin("jvm") version "1.9.24"
  kotlin("plugin.spring") version "1.9.24"
  kotlin("plugin.serialization") version "1.9.24"
}

group = "com.blueground"
version = "0.0.1-SNAPSHOT"

java {
  toolchain {
    languageVersion = JavaLanguageVersion.of(17)
  }
}

repositories {
  mavenCentral()
}

dependencyManagement {
  imports {
    mavenBom("com.netflix.graphql.dgs:graphql-dgs-platform-dependencies:latest.release")
  }
}

dependencies {
  implementation("ch.qos.logback:logback-classic")
  implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
  implementation("com.github.ksuid:ksuid:1.1.2")
  implementation("com.github.ua-parser:uap-java:1.6.1")
  implementation("com.netflix.graphql.dgs:graphql-dgs-spring-graphql-starter")
  implementation("com.squareup.okhttp3:okhttp:4.12.0")
  implementation("io.micrometer:micrometer-tracing-bridge-brave")
  implementation("net.logstash.logback:logstash-logback-encoder:7.4")
  implementation("org.codehaus.janino:janino:3.1.0")
  implementation("org.jetbrains.kotlin:kotlin-reflect")
  implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.1")
  implementation("org.jobrunr:jobrunr-spring-boot-starter:5.3.3")
  implementation("org.postgresql:postgresql:42.7.2")
  implementation("org.springframework.boot:spring-boot-starter-actuator")
  implementation("org.springframework.boot:spring-boot-starter-amqp")
  implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
  implementation("org.springframework.boot:spring-boot-starter-security")
  implementation("org.springframework.boot:spring-boot-starter-web")
  implementation("org.springframework.kafka:spring-kafka")
}
