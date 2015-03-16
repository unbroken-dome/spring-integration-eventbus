package org.unbrokendome.eventbus.autoconfigure;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.unbrokendome.eventbus.config.EnableEventBus;


@Configuration
@EnableEventBus
@EnableConfigurationProperties(EventBusConfigurationProperties.class)
public class EventBusAutoConfiguration {

}
