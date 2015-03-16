package org.unbrokendome.eventbus.config;

import org.unbrokendome.eventbus.components.EventBusBuilder;


public interface EventBusConfigurer {

    void configureEventBus(EventBusBuilder eventBusBuilder);
}
