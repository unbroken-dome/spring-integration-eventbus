package org.unbrokendome.eventbus.autoconfigure

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.MapPropertySource
import org.springframework.test.context.ContextConfiguration
import org.unbrokendome.eventbus.EventPublisher
import spock.lang.Specification

@ContextConfiguration(classes = TestConfig, initializers = TestContextInitializer)
class EventBusAutoConfigurationIntegrationTest extends Specification {

    @Configuration
    @EnableAutoConfiguration
    static class TestConfig {
    }


    static class TestContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        void initialize(ConfigurableApplicationContext applicationContext) {
            applicationContext.environment.propertySources.addFirst(
                    new MapPropertySource('testProperties', ['eventbus.poller.fixed-delay': 2000]))
        }
    }


    @Autowired
    EventPublisher eventPublisher


    def "Event bus components are registered automatically"() {
        expect:
            eventPublisher != null
    }
}
