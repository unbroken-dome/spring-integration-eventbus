package org.unbrokendome.eventbus.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.unbrokendome.eventbus.components.EventBusBuilder;


@Configuration
public class EventBusTransactionConfig implements EventBusConfigurer {

    @Autowired(required = false)
    private PlatformTransactionManager transactionManager;

    @Override
    public void configureEventBus(EventBusBuilder eventBusBuilder) {
        if (transactionManager != null) {
            eventBusBuilder.setTransactionManager(transactionManager);
        }
    }
}
