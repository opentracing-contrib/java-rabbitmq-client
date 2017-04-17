[![Build Status][ci-img]][ci] [![Released Version][maven-img]][maven]

# OpenTracing RabbitMQ Client Instrumentation
OpenTracing instrumentation for RabbitMQ Client.

## Installation

pom.xml
```xml
<dependency>
    <groupId>io.opentracing.contrib</groupId>
    <artifactId>opentracing-rabbitmq-client</artifactId>
    <version>0.0.1</version>
</dependency>
```

## Usage

`DefaultSpanManager` is used to get active span

```java
// Instantiate tracer
Tracer tracer = ...

// Register tracer with GlobalTracer
GlobalTracer.register(tracer);


// Decorate RabbitMQ Channel with TracingChannel
TracingChannel tracingChannel = new TracingChannel(channel);

// Send
tracingChannel.basicPublish(...);

// Get
GetResponse response = tracingChannel.basicGet(queueName, false);

// Consume
tracingChannel.basicConsume(...);

```

[ci-img]: https://travis-ci.org/opentracing-contrib/java-rabbitmq-client.svg?branch=master
[ci]: https://travis-ci.org/opentracing-contrib/java-rabbitmq-client
[maven-img]: https://img.shields.io/maven-central/v/io.opentracing.contrib/opentracing-rabbitmq-client.svg?maxAge=2592000
[maven]: http://search.maven.org/#search%7Cga%7C1%7Copentracing-rabbitmq-client
