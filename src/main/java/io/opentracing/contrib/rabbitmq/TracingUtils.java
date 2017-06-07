package io.opentracing.contrib.rabbitmq;

import com.rabbitmq.client.AMQP;
import io.opentracing.ActiveSpan;
import io.opentracing.References;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import io.opentracing.tag.Tags;

public class TracingUtils {

  public static SpanContext extract(AMQP.BasicProperties props, Tracer tracer) {
    SpanContext spanContext = tracer
        .extract(Format.Builtin.TEXT_MAP, new HeadersMapExtractAdapter(props.getHeaders()));
    if (spanContext != null) {
      return spanContext;
    }

    ActiveSpan span = tracer.activeSpan();
    if (span != null) {
      return span.context();
    }
    return null;
  }

  static void buildAndFinishChildSpan(AMQP.BasicProperties props, Tracer tracer) {
    ActiveSpan child = buildChildSpan(props, tracer);
    if (child != null) {
      child.close();
    }
  }

  static ActiveSpan buildChildSpan(AMQP.BasicProperties props, Tracer tracer) {
    SpanContext context = TracingUtils.extract(props, tracer);
    if (context != null) {
      Tracer.SpanBuilder spanBuilder = tracer.buildSpan("receive")
          .ignoreActiveSpan()
          .withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CONSUMER);

      spanBuilder.addReference(References.FOLLOWS_FROM, context);

      ActiveSpan span = spanBuilder.startActive();
      SpanDecorator.onResponse(span);
      return span;
    }

    return null;
  }
}
