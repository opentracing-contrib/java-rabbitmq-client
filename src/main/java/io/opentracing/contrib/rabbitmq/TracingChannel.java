package io.opentracing.contrib.rabbitmq;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Command;
import com.rabbitmq.client.ConfirmListener;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.FlowListener;
import com.rabbitmq.client.GetResponse;
import com.rabbitmq.client.Method;
import com.rabbitmq.client.ReturnListener;
import com.rabbitmq.client.ShutdownListener;
import com.rabbitmq.client.ShutdownSignalException;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.contrib.spanmanager.DefaultSpanManager;
import io.opentracing.propagation.Format;
import io.opentracing.tag.Tags;
import io.opentracing.util.GlobalTracer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;


public class TracingChannel implements Channel {
    private final Channel channel;

    public TracingChannel(Channel channel) {
        this.channel = channel;
    }

    @Override
    public int getChannelNumber() {
        return channel.getChannelNumber();
    }

    @Override
    public Connection getConnection() {
        return channel.getConnection();
    }

    @Override
    public void close() throws IOException, TimeoutException {
        channel.close();
    }

    @Override
    public void close(int closeCode, String closeMessage) throws IOException, TimeoutException {
        channel.close(closeCode, closeMessage);
    }

    @Override
    @Deprecated
    public boolean flowBlocked() {
        return channel.flowBlocked();
    }

    @Override
    public void abort() throws IOException {
        channel.abort();
    }

    @Override
    public void abort(int closeCode, String closeMessage) throws IOException {
        channel.abort(closeCode, closeMessage);
    }

    @Override
    public void addReturnListener(ReturnListener listener) {
        channel.addReturnListener(listener);
    }

    @Override
    public boolean removeReturnListener(ReturnListener listener) {
        return channel.removeReturnListener(listener);
    }

    @Override
    public void clearReturnListeners() {
        channel.clearReturnListeners();
    }

    @Override
    @Deprecated
    public void addFlowListener(FlowListener listener) {
        channel.addFlowListener(listener);
    }

    @Override
    @Deprecated
    public boolean removeFlowListener(FlowListener listener) {
        return channel.removeFlowListener(listener);
    }

    @Override
    @Deprecated
    public void clearFlowListeners() {
        channel.clearFlowListeners();
    }

    @Override
    public void addConfirmListener(ConfirmListener listener) {
        channel.addConfirmListener(listener);
    }

    @Override
    public boolean removeConfirmListener(ConfirmListener listener) {
        return channel.removeConfirmListener(listener);
    }

    @Override
    public void clearConfirmListeners() {
        channel.clearConfirmListeners();
    }

    @Override
    public Consumer getDefaultConsumer() {
        return channel.getDefaultConsumer();
    }

    @Override
    public void setDefaultConsumer(Consumer consumer) {
        channel.setDefaultConsumer(consumer);
    }

    @Override
    public void basicQos(int prefetchSize, int prefetchCount, boolean global) throws IOException {
        channel.basicQos(prefetchSize, prefetchCount, global);
    }

    @Override
    public void basicQos(int prefetchCount, boolean global) throws IOException {
        channel.basicQos(prefetchCount, global);
    }

    @Override
    public void basicQos(int prefetchCount) throws IOException {
        channel.basicQos(prefetchCount);
    }

    @Override
    public void basicPublish(String exchange, String routingKey, AMQP.BasicProperties props, byte[] body) throws IOException {
        basicPublish(exchange, routingKey, false, false, props, body);
    }

    @Override
    public void basicPublish(String exchange, String routingKey, boolean mandatory, AMQP.BasicProperties props, byte[] body) throws IOException {
        basicPublish(exchange, routingKey, mandatory, false, props, body);
    }

    @Override
    public void basicPublish(String exchange, String routingKey, boolean mandatory, boolean immediate, AMQP.BasicProperties props, byte[] body) throws IOException {

        Span span = buildSpan(exchange, props);
        AMQP.BasicProperties properties = inject(props, span);

        try {
            channel.basicPublish(exchange, routingKey, mandatory, immediate, properties, body);
        } finally {
            span.finish();
        }
    }

    @Override
    public AMQP.Exchange.DeclareOk exchangeDeclare(String exchange, String type) throws IOException {
        return channel.exchangeDeclare(exchange, type);
    }

    @Override
    public AMQP.Exchange.DeclareOk exchangeDeclare(String exchange, BuiltinExchangeType type) throws IOException {
        return channel.exchangeDeclare(exchange, type);
    }

    @Override
    public AMQP.Exchange.DeclareOk exchangeDeclare(String exchange, String type, boolean durable) throws IOException {
        return channel.exchangeDeclare(exchange, type, durable);
    }

    @Override
    public AMQP.Exchange.DeclareOk exchangeDeclare(String exchange, BuiltinExchangeType type, boolean durable) throws IOException {
        return channel.exchangeDeclare(exchange, type, durable);
    }

    @Override
    public AMQP.Exchange.DeclareOk exchangeDeclare(String exchange, String type, boolean durable, boolean autoDelete, Map<String, Object> arguments) throws IOException {
        return channel.exchangeDeclare(exchange, type, durable, autoDelete, arguments);
    }

    @Override
    public AMQP.Exchange.DeclareOk exchangeDeclare(String exchange, BuiltinExchangeType type, boolean durable, boolean autoDelete, Map<String, Object> arguments) throws IOException {
        return channel.exchangeDeclare(exchange, type, durable, autoDelete, arguments);
    }

    @Override
    public AMQP.Exchange.DeclareOk exchangeDeclare(String exchange, String type, boolean durable, boolean autoDelete, boolean internal, Map<String, Object> arguments) throws IOException {
        return channel.exchangeDeclare(exchange, type, durable, autoDelete, internal, arguments);
    }

    @Override
    public AMQP.Exchange.DeclareOk exchangeDeclare(String exchange, BuiltinExchangeType type, boolean durable, boolean autoDelete, boolean internal, Map<String, Object> arguments) throws IOException {
        return channel.exchangeDeclare(exchange, type, durable, autoDelete, internal, arguments);
    }

    @Override
    public void exchangeDeclareNoWait(String exchange, String type, boolean durable, boolean autoDelete, boolean internal, Map<String, Object> arguments) throws IOException {
        channel.exchangeDeclareNoWait(exchange, type, durable, autoDelete, internal, arguments);
    }

    @Override
    public void exchangeDeclareNoWait(String exchange, BuiltinExchangeType type, boolean durable, boolean autoDelete, boolean internal, Map<String, Object> arguments) throws IOException {
        channel.exchangeDeclareNoWait(exchange, type, durable, autoDelete, internal, arguments);
    }

    @Override
    public AMQP.Exchange.DeclareOk exchangeDeclarePassive(String name) throws IOException {
        return channel.exchangeDeclarePassive(name);
    }

    @Override
    public AMQP.Exchange.DeleteOk exchangeDelete(String exchange, boolean ifUnused) throws IOException {
        return channel.exchangeDelete(exchange, ifUnused);
    }

    @Override
    public void exchangeDeleteNoWait(String exchange, boolean ifUnused) throws IOException {
        channel.exchangeDeleteNoWait(exchange, ifUnused);
    }

    @Override
    public AMQP.Exchange.DeleteOk exchangeDelete(String exchange) throws IOException {
        return channel.exchangeDelete(exchange);
    }

    @Override
    public AMQP.Exchange.BindOk exchangeBind(String destination, String source, String routingKey) throws IOException {
        return channel.exchangeBind(destination, source, routingKey);
    }

    @Override
    public AMQP.Exchange.BindOk exchangeBind(String destination, String source, String routingKey, Map<String, Object> arguments) throws IOException {
        return channel.exchangeBind(destination, source, routingKey, arguments);
    }

    @Override
    public void exchangeBindNoWait(String destination, String source, String routingKey, Map<String, Object> arguments) throws IOException {
        channel.exchangeBindNoWait(destination, source, routingKey, arguments);
    }

    @Override
    public AMQP.Exchange.UnbindOk exchangeUnbind(String destination, String source, String routingKey) throws IOException {
        return channel.exchangeUnbind(destination, source, routingKey);
    }

    @Override
    public AMQP.Exchange.UnbindOk exchangeUnbind(String destination, String source, String routingKey, Map<String, Object> arguments) throws IOException {
        return channel.exchangeUnbind(destination, source, routingKey, arguments);
    }

    @Override
    public void exchangeUnbindNoWait(String destination, String source, String routingKey, Map<String, Object> arguments) throws IOException {
        channel.exchangeUnbindNoWait(destination, source, routingKey, arguments);
    }

    @Override
    public AMQP.Queue.DeclareOk queueDeclare() throws IOException {
        return channel.queueDeclare();
    }

    @Override
    public AMQP.Queue.DeclareOk queueDeclare(String queue, boolean durable, boolean exclusive, boolean autoDelete, Map<String, Object> arguments) throws IOException {
        return channel.queueDeclare(queue, durable, exclusive, autoDelete, arguments);
    }

    @Override
    public void queueDeclareNoWait(String queue, boolean durable, boolean exclusive, boolean autoDelete, Map<String, Object> arguments) throws IOException {
        channel.queueDeclareNoWait(queue, durable, exclusive, autoDelete, arguments);
    }

    @Override
    public AMQP.Queue.DeclareOk queueDeclarePassive(String queue) throws IOException {
        return channel.queueDeclarePassive(queue);
    }

    @Override
    public AMQP.Queue.DeleteOk queueDelete(String queue) throws IOException {
        return channel.queueDelete(queue);
    }

    @Override
    public AMQP.Queue.DeleteOk queueDelete(String queue, boolean ifUnused, boolean ifEmpty) throws IOException {
        return channel.queueDelete(queue, ifUnused, ifEmpty);
    }

    @Override
    public void queueDeleteNoWait(String queue, boolean ifUnused, boolean ifEmpty) throws IOException {
        channel.queueDeleteNoWait(queue, ifUnused, ifEmpty);
    }

    @Override
    public AMQP.Queue.BindOk queueBind(String queue, String exchange, String routingKey) throws IOException {
        return channel.queueBind(queue, exchange, routingKey);
    }

    @Override
    public AMQP.Queue.BindOk queueBind(String queue, String exchange, String routingKey, Map<String, Object> arguments) throws IOException {
        return channel.queueBind(queue, exchange, routingKey, arguments);
    }

    @Override
    public void queueBindNoWait(String queue, String exchange, String routingKey, Map<String, Object> arguments) throws IOException {
        channel.queueBindNoWait(queue, exchange, routingKey, arguments);
    }

    @Override
    public AMQP.Queue.UnbindOk queueUnbind(String queue, String exchange, String routingKey) throws IOException {
        return channel.queueUnbind(queue, exchange, routingKey);
    }

    @Override
    public AMQP.Queue.UnbindOk queueUnbind(String queue, String exchange, String routingKey, Map<String, Object> arguments) throws IOException {
        return channel.queueUnbind(queue, exchange, routingKey, arguments);
    }

    @Override
    public AMQP.Queue.PurgeOk queuePurge(String queue) throws IOException {
        return channel.queuePurge(queue);
    }

    @Override
    public GetResponse basicGet(String queue, boolean autoAck) throws IOException {
        GetResponse response = channel.basicGet(queue, autoAck);
        TracingUtils.buildAndFinishChildSpan(response.getProps());
        return response;
    }

    @Override
    public void basicAck(long deliveryTag, boolean multiple) throws IOException {
        channel.basicAck(deliveryTag, multiple);
    }

    @Override
    public void basicNack(long deliveryTag, boolean multiple, boolean requeue) throws IOException {
        channel.basicNack(deliveryTag, multiple, requeue);
    }

    @Override
    public void basicReject(long deliveryTag, boolean requeue) throws IOException {
        channel.basicReject(deliveryTag, requeue);
    }

    @Override
    public String basicConsume(String queue, Consumer callback) throws IOException {
        return basicConsume(queue, false, "", false, false, null, callback);
    }

    @Override
    public String basicConsume(String queue, boolean autoAck, Consumer callback) throws IOException {
        return basicConsume(queue, autoAck, "", false, false, null, callback);
    }

    @Override
    public String basicConsume(String queue, boolean autoAck, Map<String, Object> arguments, Consumer callback) throws IOException {
        return basicConsume(queue, autoAck, "", false, false, arguments, callback);
    }

    @Override
    public String basicConsume(String queue, boolean autoAck, String consumerTag, Consumer callback) throws IOException {
        return basicConsume(queue, autoAck, consumerTag, false, false, null, callback);
    }

    @Override
    public String basicConsume(String queue, boolean autoAck, String consumerTag, boolean noLocal, boolean exclusive, Map<String, Object> arguments, Consumer callback) throws IOException {
        return channel.basicConsume(queue, autoAck, consumerTag, noLocal, exclusive, arguments, new TracingConsumer(callback));
    }

    @Override
    public void basicCancel(String consumerTag) throws IOException {
        channel.basicCancel(consumerTag);
    }

    @Override
    public AMQP.Basic.RecoverOk basicRecover() throws IOException {
        return channel.basicRecover();
    }

    @Override
    public AMQP.Basic.RecoverOk basicRecover(boolean requeue) throws IOException {
        return channel.basicRecover(requeue);
    }

    @Override
    public AMQP.Tx.SelectOk txSelect() throws IOException {
        return channel.txSelect();
    }

    @Override
    public AMQP.Tx.CommitOk txCommit() throws IOException {
        return channel.txCommit();
    }

    @Override
    public AMQP.Tx.RollbackOk txRollback() throws IOException {
        return channel.txRollback();
    }

    @Override
    public AMQP.Confirm.SelectOk confirmSelect() throws IOException {
        return channel.confirmSelect();
    }

    @Override
    public long getNextPublishSeqNo() {
        return channel.getNextPublishSeqNo();
    }

    @Override
    public boolean waitForConfirms() throws InterruptedException {
        return channel.waitForConfirms();
    }

    @Override
    public boolean waitForConfirms(long timeout) throws InterruptedException, TimeoutException {
        return channel.waitForConfirms(timeout);
    }

    @Override
    public void waitForConfirmsOrDie() throws IOException, InterruptedException {
        channel.waitForConfirmsOrDie();
    }

    @Override
    public void waitForConfirmsOrDie(long timeout) throws IOException, InterruptedException, TimeoutException {
        channel.waitForConfirmsOrDie(timeout);
    }

    @Override
    public void asyncRpc(Method method) throws IOException {
        channel.asyncRpc(method);
    }

    @Override
    public Command rpc(Method method) throws IOException {
        return channel.rpc(method);
    }

    @Override
    public long messageCount(String queue) throws IOException {
        return channel.messageCount(queue);
    }

    @Override
    public long consumerCount(String queue) throws IOException {
        return channel.consumerCount(queue);
    }

    @Override
    public void addShutdownListener(ShutdownListener listener) {
        channel.addShutdownListener(listener);
    }

    @Override
    public void removeShutdownListener(ShutdownListener listener) {
        channel.removeShutdownListener(listener);
    }

    @Override
    public ShutdownSignalException getCloseReason() {
        return channel.getCloseReason();
    }

    @Override
    public void notifyListeners() {
        channel.notifyListeners();
    }

    @Override
    public boolean isOpen() {
        return channel.isOpen();
    }

    private Span buildSpan(String exchange, AMQP.BasicProperties props) {
        Tracer.SpanBuilder spanBuilder = GlobalTracer.get().buildSpan("send")
                .withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_PRODUCER);

        SpanContext spanContext = null;

        if (props != null && props.getHeaders() != null) {
            // just in case if span context was injected manually to props in basicPublish
            spanContext = GlobalTracer.get().extract(Format.Builtin.TEXT_MAP,
                    new HeadersMapExtractAdapter(props.getHeaders()));
        }

        if (spanContext == null) {
            Span parentSpan = DefaultSpanManager.getInstance().current().getSpan();
            if (parentSpan != null) {
                spanContext = parentSpan.context();
            }
        }

        if (spanContext != null) {
            spanBuilder.asChildOf(spanContext);
        }

        Span span = spanBuilder.start();
        SpanDecorator.onRequest(exchange, span);

        return span;
    }

    private AMQP.BasicProperties inject(AMQP.BasicProperties properties, Span span) {

        // Headers of AMQP.BasicProperties is unmodifiableMap therefore we build new AMQP.BasicProperties
        // with injected span context into headers
        Map<String, Object> headers = new HashMap<>();

        GlobalTracer.get().inject(span.context(), Format.Builtin.TEXT_MAP, new HeadersMapInjectAdapter(headers));

        if (properties == null) {
            return new AMQP.BasicProperties().builder().headers(headers).build();
        }

        if (properties.getHeaders() != null) {
            headers.putAll(properties.getHeaders());
        }

        return properties.builder()
                .headers(headers)
                .build();
    }
}
