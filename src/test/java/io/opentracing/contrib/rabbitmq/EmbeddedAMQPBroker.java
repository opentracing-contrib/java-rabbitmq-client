package io.opentracing.contrib.rabbitmq;


import com.google.common.io.Files;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import javax.net.ServerSocketFactory;
import org.apache.qpid.server.Broker;
import org.apache.qpid.server.BrokerOptions;


public class EmbeddedAMQPBroker {

  private int brokerPort;
  private final Broker broker = new Broker();

  public EmbeddedAMQPBroker() throws Exception {
    this.brokerPort = findAvailableTcpPort();
    final String configFileName = "qpid-config.json";
    final String passwordFileName = "passwd.properties";
    final BrokerOptions brokerOptions = new BrokerOptions();
    brokerOptions.setConfigProperty("qpid.amqp_port", String.valueOf(brokerPort));
    brokerOptions.setConfigProperty("qpid.pass_file", findResourcePath(passwordFileName));
    brokerOptions.setConfigProperty("qpid.work_dir", Files.createTempDir().getAbsolutePath());
    brokerOptions.setInitialConfigurationLocation(findResourcePath(configFileName));
    // start broker
    broker.startup(brokerOptions);
  }

  public void shutdown() {
    broker.shutdown();
  }

  private String findResourcePath(final String file) throws IOException {
    return "src/test/resources/" + file;
  }


  private static int findAvailableTcpPort() {
    for (int i = 1024; i < 65535; i++) {
      if (isPortAvailable(i)) {
        return i;
      }
    }
    throw new IllegalStateException("No port available");
  }

  private static boolean isPortAvailable(int port) {
    try {
      ServerSocket serverSocket = ServerSocketFactory.getDefault().createServerSocket(
          port, 1, InetAddress.getByName("localhost"));
      serverSocket.close();
      return true;
    } catch (Exception ex) {
      return false;
    }
  }

  public int getBrokerPort() {
    return brokerPort;
  }
}
