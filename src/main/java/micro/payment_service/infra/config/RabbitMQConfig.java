package micro.payment_service.infra.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.json.JsonMapper;

@Configuration
public class RabbitMQConfig {

    public static final String QUEUE_PAYMENT_PROCESS = "payment.process.queue";
    public static final String DLX_PAYMENT = "payment.dlx";
    public static final String DLQ_PAYMENT_PROCESS = "payment.process.dlq";

    @Bean
    public Queue paymentQueue(){
        return QueueBuilder.durable(QUEUE_PAYMENT_PROCESS)
                .deadLetterExchange(DLX_PAYMENT)
                .deadLetterRoutingKey(DLQ_PAYMENT_PROCESS)
                .build();
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
        return BindingBuilder.bind(paymentDlq).to(paymentDlx).with(DLQ_PAYMENT_PROCESS);
    }

    @Bean
    public JacksonJsonMessageConverter messageConverter(JsonMapper jsonMapper) {
        return new JacksonJsonMessageConverter(jsonMapper);
    }


}
