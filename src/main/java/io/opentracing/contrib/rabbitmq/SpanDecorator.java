package io.opentracing.contrib.rabbitmq;


import io.opentracing.ActiveSpan;
import io.opentracing.tag.Tags;

class SpanDecorator {

  static final String COMPONENT_NAME = "java-rabbitmq";

  static void onRequest(String exchange, ActiveSpan span) {
    Tags.COMPONENT.set(span, COMPONENT_NAME);
    Tags.MESSAGE_BUS_DESTINATION.set(span, exchange);
  }

  static void onResponse(ActiveSpan span) {
    Tags.COMPONENT.set(span, COMPONENT_NAME);
  }
}
