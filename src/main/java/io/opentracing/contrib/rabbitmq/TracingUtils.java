/*
 * Copyright 2017-2019 The OpenTracing Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package io.opentracing.contrib.rabbitmq;

import com.rabbitmq.client.AMQP;
import io.opentracing.References;
import io.opentracing.Scope;
import io.opentracing.Span;
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

    Span span = tracer.activeSpan();
    if (span != null) {
      return span.context();
    }
    return null;
  }

  static void buildAndFinishChildSpan(AMQP.BasicProperties props, Tracer tracer) {
    Scope child = buildChildSpan(props, tracer);
    if (child != null) {
      child.close();
    }
  }

  static Scope buildChildSpan(AMQP.BasicProperties props, Tracer tracer) {
    SpanContext context = TracingUtils.extract(props, tracer);
    if (context != null) {
      Tracer.SpanBuilder spanBuilder = tracer.buildSpan("receive")
          .ignoreActiveSpan()
          .withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CONSUMER);

      spanBuilder.addReference(References.FOLLOWS_FROM, context);

      Scope scope = spanBuilder.startActive(true);
      SpanDecorator.onResponse(scope.span());
      return scope;
    }

    return null;
  }
}
