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


import static org.awaitility.Awaitility.await;
import static org.hamcrest.core.IsEqual.equalTo;
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
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;
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
  private static final MockTracer mockTracer = new MockTracer();
  private Channel channel;
  private Connection connection;

  @BeforeClass
  public static void beforeClass() throws Exception {
    embeddedAMQPBroker = new EmbeddedAMQPBroker();
  }

  @AfterClass
  public static void afterClass() {
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
        assertNotNull(mockTracer.activeSpan());
        latch.countDown();
      }
    });

    latch.await(30, TimeUnit.SECONDS);

    await().atMost(15, TimeUnit.SECONDS).until(reportedSpansSize(), equalTo(2));
    List<MockSpan> finishedSpans = mockTracer.finishedSpans();

    assertEquals(2, finishedSpans.size());
    checkSpans(finishedSpans);
    assertNull(mockTracer.activeSpan());
  }

  @Test
  public void basicConsumeWithCallback() throws Exception {
    String exchangeName = "basicConsumeExchange";
    String queueName = "basicConsumeQueue";
    String routingKey = "#";

    channel.exchangeDeclare(exchangeName, "direct", true);
    channel.queueDeclare(queueName, true, false, false, null);
    channel.queueBind(queueName, exchangeName, routingKey);

    byte[] messageBodyBytes = "Hello, world!".getBytes();

    channel.basicPublish(exchangeName, routingKey, null, messageBodyBytes);

    final CountDownLatch latch = new CountDownLatch(1);
    channel.basicConsume(queueName, false,
        (consumerTag, message) -> {
          assertNotNull(mockTracer.activeSpan());
          latch.countDown();
        },
        consumerTag -> {
        });

    latch.await(30, TimeUnit.SECONDS);

    await().atMost(15, TimeUnit.SECONDS).until(reportedSpansSize(), equalTo(2));
    List<MockSpan> finishedSpans = mockTracer.finishedSpans();

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

  private Callable<Integer> reportedSpansSize() {
    return () -> mockTracer.finishedSpans().size();
  }

}
