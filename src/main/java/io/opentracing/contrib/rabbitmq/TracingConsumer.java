package io.opentracing.contrib.rabbitmq;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.ShutdownSignalException;
import io.opentracing.Span;
import io.opentracing.contrib.spanmanager.DefaultSpanManager;
import io.opentracing.contrib.spanmanager.SpanManager;

import java.io.IOException;


public class TracingConsumer implements Consumer {
    private final Consumer consumer;

    public TracingConsumer(Consumer consumer) {
        this.consumer = consumer;
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
    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
        Span child = TracingUtils.buildChildSpan(properties);
        SpanManager.ManagedSpan managedSpan = DefaultSpanManager.getInstance().activate(child);

        try {
            consumer.handleDelivery(consumerTag, envelope, properties, body);
        } finally {
            if (child != null) {
                child.finish();
                managedSpan.deactivate();
            }
        }
    }
}
