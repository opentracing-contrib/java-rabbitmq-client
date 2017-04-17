package io.opentracing.contrib.rabbitmq;

import com.rabbitmq.client.AMQP;
import io.opentracing.References;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.contrib.spanmanager.DefaultSpanManager;
import io.opentracing.propagation.Format;
import io.opentracing.tag.Tags;
import io.opentracing.util.GlobalTracer;


public class TracingUtils {

    public static SpanContext extract(AMQP.BasicProperties props) {
        SpanContext spanContext = GlobalTracer.get().extract(Format.Builtin.TEXT_MAP, new HeadersMapExtractAdapter(props.getHeaders()));
        if (spanContext != null) {
            return spanContext;
        }

        Span span = DefaultSpanManager.getInstance().current().getSpan();
        if (span != null) {
            return span.context();
        }
        return null;
    }

    public static void buildAndFinishChildSpan(AMQP.BasicProperties props) {
        Span child = buildChildSpan(props);
        if (child != null) {
            child.finish();
        }
    }

    public static Span buildChildSpan(AMQP.BasicProperties props) {
        SpanContext context = TracingUtils.extract(props);
        if (context != null) {

            Tracer.SpanBuilder spanBuilder = GlobalTracer.get().buildSpan("receive")
                    .withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CONSUMER);

            spanBuilder.addReference(References.FOLLOWS_FROM, context);

            Span span = spanBuilder.start();
            SpanDecorator.onResponse(span);
            return span;
        }

        return null;
    }
}
