package io.opentracing.contrib.rabbitmq;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.GetResponse;
import io.opentracing.mock.MockSpan;
import io.opentracing.mock.MockTracer;
import io.opentracing.tag.Tags;
import io.opentracing.util.ThreadLocalActiveSpanSource;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class TracingTest {

  private static EmbeddedAMQPBroker embeddedAMQPBroker;
  private static final MockTracer mockTracer = new MockTracer(new ThreadLocalActiveSpanSource(),
      MockTracer.Propagator.TEXT_MAP);
  private Channel channel;
  private Connection connection;

  @BeforeClass
  public static void beforeClass() throws Exception {
    embeddedAMQPBroker = new EmbeddedAMQPBroker();
  }

  @AfterClass
  public static void afterClass() throws Exception {
    if (embeddedAMQPBroker != null) {
      embeddedAMQPBroker.shutdown();
    }
  }

  @Before
  public void before() throws IOException, TimeoutException {
    mockTracer.reset();

    ConnectionFactory factory = new ConnectionFactory();
    factory.setUsername("guest");
    factory.setPassword("guest");
    factory.setVirtualHost("default");
    factory.setHost("localhost");
    factory.setPort(embeddedAMQPBroker.getBrokerPort());
    connection = factory.newConnection();

    channel = new TracingChannel(connection.createChannel(), mockTracer);
  }

  @After
  public void after() throws IOException, TimeoutException {
    channel.close();
    connection.close();
  }

  @Test
  public void basicGet() throws Exception {
    String exchangeName = "basicGetExchange";
    String queueName = "basicGetQueue";
    String routingKey = "#";

    channel.exchangeDeclare(exchangeName, "direct", true);
    channel.queueDeclare(queueName, true, false, false, null);
    channel.queueBind(queueName, exchangeName, routingKey);

    byte[] messageBodyBytes = "Hello, world!".getBytes();

    channel.basicPublish(exchangeName, routingKey, null, messageBodyBytes);

    GetResponse response = channel.basicGet(queueName, false);
    assertNotNull(response.getBody());

    List<MockSpan> finishedSpans = mockTracer.finishedSpans();
    assertEquals(2, finishedSpans.size());
    checkSpans(finishedSpans);

    assertNull(mockTracer.activeSpan());
  }

  @Test
  public void basicConsume() throws Exception {
    String exchangeName = "basicConsumeExchange";
    String queueName = "basicConsumeQueue";
    String routingKey = "#";

    channel.exchangeDeclare(exchangeName, "direct", true);
    channel.queueDeclare(queueName, true, false, false, null);
    channel.queueBind(queueName, exchangeName, routingKey);

    byte[] messageBodyBytes = "Hello, world!".getBytes();

    channel.basicPublish(exchangeName, routingKey, null, messageBodyBytes);

    final CountDownLatch latch = new CountDownLatch(1);
    channel.basicConsume(queueName, false, new DefaultConsumer(channel) {
      @Override
      public void handleDelivery(String consumerTag,
          Envelope envelope,
          AMQP.BasicProperties properties,
          byte[] body)
          throws IOException {
        long deliveryTag = envelope.getDeliveryTag();
        channel.basicAck(deliveryTag, false);
        latch.countDown();
      }
    });

    latch.await(30, TimeUnit.SECONDS);

    List<MockSpan> finishedSpans = mockTracer.finishedSpans();
    int tries = 10;
    while (tries > 0 && finishedSpans.size() < 2) {
      TimeUnit.SECONDS.sleep(1L);
      finishedSpans = mockTracer.finishedSpans();
      tries--;
    }

    assertEquals(2, finishedSpans.size());
    checkSpans(finishedSpans);
    assertNull(mockTracer.activeSpan());
  }

  private void checkSpans(List<MockSpan> mockSpans) {
    for (MockSpan mockSpan : mockSpans) {
      assertTrue(mockSpan.tags().get(Tags.SPAN_KIND.getKey()).equals(Tags.SPAN_KIND_CONSUMER)
          || mockSpan.tags().get(Tags.SPAN_KIND.getKey()).equals(Tags.SPAN_KIND_PRODUCER));
      assertEquals(SpanDecorator.COMPONENT_NAME, mockSpan.tags().get(Tags.COMPONENT.getKey()));
      assertEquals(0, mockSpan.generatedErrors().size());
      String operationName = mockSpan.operationName();
      assertTrue(operationName.equals("send")
          || operationName.equals("receive"));
    }
  }

}
