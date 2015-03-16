package org.unbrokendome.eventbus.components;

import org.springframework.integration.core.MessageSelector;
import org.springframework.messaging.Message;
import org.springframework.util.Assert;


class EventTypeMessageSelector implements MessageSelector {

    private final Class<?> eventType;


    public EventTypeMessageSelector(Class<?> eventType) {
        this.eventType = eventType;
    }


    @Override
    public boolean accept(Message<?> message) {
        Assert.notNull(message, "'message' must not be null");
        Object payload = message.getPayload();
        Assert.notNull(payload, "'payload' must not be null");

        return eventType.isAssignableFrom(payload.getClass());
    }
}
