package io.opentracing.contrib.rabbitmq;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.ShutdownSignalException;
import io.opentracing.ActiveSpan;
import io.opentracing.Tracer;
import java.io.IOException;


public class TracingConsumer implements Consumer {

  private final Consumer consumer;
  private final Tracer tracer;

  public TracingConsumer(Consumer consumer, Tracer tracer) {
    this.consumer = consumer;
    this.tracer = tracer;
  }

  @Override
  public void handleConsumeOk(String consumerTag) {
    consumer.handleConsumeOk(consumerTag);
  }

  @Override
  public void handleCancelOk(String consumerTag) {
    consumer.handleCancelOk(consumerTag);

  }

  @Override
  public void handleCancel(String consumerTag) throws IOException {
    consumer.handleCancel(consumerTag);

  }

  @Override
  public void handleShutdownSignal(String consumerTag, ShutdownSignalException sig) {
    consumer.handleShutdownSignal(consumerTag, sig);
  }

  @Override
  public void handleRecoverOk(String consumerTag) {
    consumer.handleRecoverOk(consumerTag);
  }

  @Override
  public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
      byte[] body) throws IOException {
    ActiveSpan child = TracingUtils.buildChildSpan(properties, tracer);

    try {
      consumer.handleDelivery(consumerTag, envelope, properties, body);
    } finally {
      if (child != null) {
        child.close();
      }
    }
  }
}
