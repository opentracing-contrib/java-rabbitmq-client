[![Build Status][ci-img]][ci] [![Released Version][maven-img]][maven]

# OpenTracing RabbitMQ Client Instrumentation
OpenTracing instrumentation for RabbitMQ Client.

## Installation

pom.xml
```xml
<dependency>
    <groupId>io.opentracing.contrib</groupId>
    <artifactId>opentracing-rabbitmq-client</artifactId>
    <version>VERSION</version>
</dependency>
```

## Usage


```java
// Instantiate tracer
Tracer tracer = ...

// Decorate RabbitMQ Channel with TracingChannel
TracingChannel tracingChannel = new TracingChannel(channel, tracer);

// Send
tracingChannel.basicPublish(...);

// Get
GetResponse response = tracingChannel.basicGet(queueName, false);

// Consume
tracingChannel.basicConsume(...);

```

[ci-img]: https://travis-ci.org/opentracing-contrib/java-rabbitmq-client.svg?branch=master
[ci]: https://travis-ci.org/opentracing-contrib/java-rabbitmq-client
[maven-img]: https://img.shields.io/maven-central/v/io.opentracing.contrib/opentracing-rabbitmq-client.svg
[maven]: http://search.maven.org/#search%7Cga%7C1%7Copentracing-rabbitmq-client
