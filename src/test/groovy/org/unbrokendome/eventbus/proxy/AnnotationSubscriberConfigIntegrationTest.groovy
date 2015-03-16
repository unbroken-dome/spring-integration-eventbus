package org.unbrokendome.eventbus.proxy

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.integration.support.MessageBuilder
import org.springframework.test.context.ContextConfiguration
import org.unbrokendome.eventbus.EventSubscriber
import org.unbrokendome.eventbus.TestEvent
import org.unbrokendome.eventbus.TestSubscriber
import spock.lang.Specification

import static org.mockito.Mockito.*


@ContextConfiguration(classes = TestConfig)
class AnnotationSubscriberConfigIntegrationTest extends Specification {

    @Configuration
    @Import(AnnotationSubscriberConfig)
    static class TestConfig {

        @Bean
        TestSubscriber testSubscriber() { mock(TestSubscriber) }
    }

    @Autowired
    EventSubscriber eventSubscriber

    @Autowired
    TestSubscriber testSubscriber


    def setup() {
        reset(testSubscriber)
    }


    def "EventSubscriber is registered for each @Subscribe method"() {
        expect:
            eventSubscriber != null
            !eventSubscriber.async
            eventSubscriber.eventType == TestEvent
    }


    def "EventSubscriber handleEvent() forwards to @Subscribe annotated method"() {
        given:
            def event = new TestEvent('asdf')
            def eventMessage = MessageBuilder.withPayload(event).build()

        when:
            eventSubscriber.handleMessage eventMessage

        then:
            verify(testSubscriber).handleTestEvent(event)
    }
}
