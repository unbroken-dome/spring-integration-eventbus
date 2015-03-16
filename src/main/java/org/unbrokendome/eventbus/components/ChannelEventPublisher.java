package org.unbrokendome.eventbus.components;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.unbrokendome.eventbus.EventPublisher;


public class ChannelEventPublisher implements EventPublisher {

    private final MessageChannel channel;


    public ChannelEventPublisher(MessageChannel channel) {
        this.channel = channel;
    }


    @Override
    public void publish(Object event) {

        Message<?> message = MessageBuilder.withPayload(event)
                .build();
        channel.send(message);
    }
}
