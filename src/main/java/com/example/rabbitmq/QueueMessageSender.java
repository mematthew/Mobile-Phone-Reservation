package com.example.rabbitmq;

import com.example.model.exception.MessageSendingException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@Profile("!test")
public class QueueMessageSender {

    private final RabbitTemplate rabbitTemplate;

    /**
     * Sends a message to the specified exchange.
     *
     * @param exchangeName The name of the exchange to send the message to.
     * @param routing      The routing key.
     * @param message      The message to send.
     * @throws MessageSendingException if sending the message fails.
     */
    @Transactional
    public void send(String exchangeName, String routing, Object message) {
        try {
            log.debug("Sending a message to exchange: [{}]", exchangeName);
            rabbitTemplate.convertAndSend(exchangeName, routing, message);
        } catch (AmqpException exception) {
            String exceptionMessage = MessageFormatter.format("Sending message to {} failed, {}", exchangeName,
                exception.getMessage()).getMessage();
            throw new MessageSendingException(exceptionMessage, exception);
        }
    }
}