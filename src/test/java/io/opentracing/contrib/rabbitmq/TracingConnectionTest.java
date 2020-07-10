/*
 * Copyright 2017-2020 The OpenTracing Authors
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

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import io.opentracing.mock.MockTracer;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TracingConnectionTest {

  @Test
  public void shouldNotWrapNullChannel() throws Exception {
    Connection connection = mock(Connection.class);
    when(connection.createChannel()).thenReturn(null);
    when(connection.createChannel(1)).thenReturn(null);

    TracingConnection tracingConnection = new TracingConnection(connection, new MockTracer());
    assertNull(tracingConnection.createChannel());
    assertNull(tracingConnection.createChannel(1));
  }

  @Test
  public void shouldWrapNotNullChannel() throws Exception {
    Connection connection = mock(Connection.class);
    Channel channel = mock(Channel.class);
    when(connection.createChannel()).thenReturn(channel);
    when(connection.createChannel(1)).thenReturn(channel);

    TracingConnection tracingConnection = new TracingConnection(connection, new MockTracer());
    assertNotNull(tracingConnection.createChannel());
    assertNotNull(tracingConnection.createChannel(1));
  }

}
