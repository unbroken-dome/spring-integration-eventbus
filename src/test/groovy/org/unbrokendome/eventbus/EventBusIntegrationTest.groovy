package org.unbrokendome.eventbus

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.integration.config.EnableIntegration
import org.springframework.integration.dsl.core.Pollers
import org.springframework.integration.store.ChannelMessageStore
import org.springframework.integration.store.SimpleMessageStore
import org.springframework.test.context.ContextConfiguration
import org.unbrokendome.eventbus.components.EventBusBuilder
import org.unbrokendome.eventbus.config.EnableEventBus
import org.unbrokendome.eventbus.config.EventBusConfigurer
import spock.lang.Specification

import static org.mockito.Mockito.*

@ContextConfiguration(classes = TestConfig)
class EventBusIntegrationTest extends Specification {

    @Configuration
    @EnableEventBus
    static class TestConfig implements EventBusConfigurer {

        @Bean
        TestSubscriber testSubscriber() { mock(TestSubscriber) }

        @Bean
        AsyncTestSubscriber asyncTestSubscriber() { mock(AsyncTestSubscriber) }

        @Override
        void configureEventBus(EventBusBuilder eventBusBuilder) {
            eventBusBuilder.eventPoller = Pollers.fixedDelay(100)
        }
    }

    @Autowired
    EventPublisher eventPublisher;

    @Autowired
    TestSubscriber testSubscriber
    @Autowired
    AsyncTestSubscriber asyncTestSubscriber


    def setup() {
        reset testSubscriber
        reset asyncTestSubscriber
    }


    def "TestSubscriber receives published event"() {
        given:
            def event = new TestEvent(name: 'test')

        when:
            eventPublisher.publish event

        then:
            mockito {
                verify(testSubscriber).handleTestEvent(event)
            }
    }


    def "AsyncTestSubscriber receives published event"() {
        given:
            def event = new AsyncTestEvent(name: 'test')

        when:
            eventPublisher.publish event
            sleep 200 // wait for the queue poller to be triggered at least once

        then:
            mockito {
                verify(asyncTestSubscriber).handleTestEvent(event)
            }
    }


    boolean mockito(Closure closure) {
        closure.run()
        true
    }
}
