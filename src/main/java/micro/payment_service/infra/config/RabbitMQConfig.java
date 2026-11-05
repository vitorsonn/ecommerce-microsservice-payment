package micro.payment_service.infra.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.json.JsonMapper;

@Configuration
public class RabbitMQConfig {


    public static final String QUEUE_PAYMENT_PROCESS = "payment.process.queue";
    public static final String EXCHANGE_ORDERS = "orders.exchange";
    public static final String ROUTING_KEY_ORDER_CREATED = "order.created";


    public static final String DLX_PAYMENT = "payment.dlx";
    public static final String DLQ_PAYMENT_PROCESS = "payment.process.dlq";


    public static final String EXCHANGE_PAYMENTS = "payments.exchange";
    public static final String ROUTING_KEY_PAYMENT_PROCESSED = "payment.processed";


    @Bean
    public Queue paymentQueue() {
        return QueueBuilder.durable(QUEUE_PAYMENT_PROCESS)
                .deadLetterExchange(DLX_PAYMENT)
                .deadLetterRoutingKey(DLQ_PAYMENT_PROCESS)
                .build();
    }


    @Bean
    public TopicExchange ordersExchange() {
        return new TopicExchange(EXCHANGE_ORDERS);
    }


    @Bean
    public Binding bindingPaymentQueue(Queue paymentQueue, TopicExchange ordersExchange) {
        return BindingBuilder.bind(paymentQueue)
                .to(ordersExchange)
                .with(ROUTING_KEY_ORDER_CREATED);
    }



    @Bean
    public DirectExchange paymentDlx() {
        return new DirectExchange(DLX_PAYMENT);
    }

    @Bean
    public Queue paymentDlq() {
        return QueueBuilder.durable(DLQ_PAYMENT_PROCESS).build();
    }

    @Bean
    public Binding bindingPaymentDlq(Queue paymentDlq, DirectExchange paymentDlx) {
        return BindingBuilder.bind(paymentDlq)
                .to(paymentDlx)
                .with(DLQ_PAYMENT_PROCESS);
    }


    @Bean
    public TopicExchange paymentsExchange() {
        return new TopicExchange(EXCHANGE_PAYMENTS);
    }

    @Bean
    public JacksonJsonMessageConverter messageConverter(JsonMapper jsonMapper) {
        return new JacksonJsonMessageConverter(jsonMapper);
    }

    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        RabbitAdmin rabbitAdmin = new RabbitAdmin(connectionFactory);
        rabbitAdmin.setAutoStartup(true);
        return rabbitAdmin;
    }


}
