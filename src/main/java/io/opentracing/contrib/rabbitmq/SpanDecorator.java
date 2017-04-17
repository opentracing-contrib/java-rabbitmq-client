package io.opentracing.contrib.rabbitmq;


import io.opentracing.Span;
import io.opentracing.tag.Tags;

public class SpanDecorator {
    public static final String COMPONENT_NAME = "java-rabbitmq";

    public static void onRequest(String exchange, Span span) {
        Tags.COMPONENT.set(span, COMPONENT_NAME);
        Tags.MESSAGE_BUS_DESTINATION.set(span, exchange);
    }

    public static void onResponse(Span span) {
        Tags.COMPONENT.set(span, COMPONENT_NAME);
    }
}
