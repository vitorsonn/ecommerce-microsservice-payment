package micro.payment_service.application.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import micro.payment_service.infra.config.RabbitMQConfig;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class DLQReprocessorService {

    private final RabbitTemplate rabbitTemplate;

    public int reprocessDLQMessages(int maxMessages){

        int reprocessedCount = 0;

        for (int i = 0; i < maxMessages; i++){

            Message message = rabbitTemplate.receive(RabbitMQConfig.DLQ_PAYMENT_PROCESS);

            if (message == null) {
                log.info("Sem mensagens na DLQ para reprocessamento.");
                break;
            }

            log.info("Mensagem capturada da DLQ para reprocessamento. ID/Headers: {}", message.getMessageProperties().getHeaders());

            message.getMessageProperties().getHeaders().remove("x-death");
            message.getMessageProperties().getHeaders().remove("x-first-death-exchange");
            message.getMessageProperties().getHeaders().remove("x-first-death-queue");
            message.getMessageProperties().getHeaders().remove("x-first-death-reason");

            rabbitTemplate.send(RabbitMQConfig.QUEUE_PAYMENT_PROCESS, message);
            reprocessedCount++;

        }
        log.info("Total de mensagens reprocessadas: {}", reprocessedCount);

        return reprocessedCount;

    }
}
