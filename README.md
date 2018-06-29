[![Build Status][ci-img]][ci] [![Coverage Status][cov-img]][cov] [![Released Version][maven-img]][maven]

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

// Factory
ConnectionFactory factory = new TracingConnectionFactory(tracer);
Connection connection = factory.newConnection();

```

[ci-img]: https://travis-ci.org/opentracing-contrib/java-rabbitmq-client.svg?branch=master
[ci]: https://travis-ci.org/opentracing-contrib/java-rabbitmq-client
[cov-img]: https://coveralls.io/repos/github/opentracing-contrib/java-rabbitmq-client/badge.svg?branch=master
[cov]: https://coveralls.io/github/opentracing-contrib/java-rabbitmq-client?branch=master
[maven-img]: https://img.shields.io/maven-central/v/io.opentracing.contrib/opentracing-rabbitmq-client.svg
[maven]: http://search.maven.org/#search%7Cga%7C1%7Copentracing-rabbitmq-client
