package org.unbrokendome.eventbus.components

import org.springframework.messaging.MessageChannel
import org.unbrokendome.eventbus.TestEvent
import org.unbrokendome.eventbus.components.ChannelEventPublisher
import spock.lang.Specification
import spock.lang.Subject

class ChannelEventPublisherTest extends Specification {

    def channel = Mock(MessageChannel)

    @Subject
    def eventPublisher = new ChannelEventPublisher(channel)


    def "Published event should be sent to channel as message payload"() {

        given:
            def event = new TestEvent('foo')

        when:
            eventPublisher.publish(event)

        then:
            1 * channel.send({ it.payload == event }) >> true
    }
}
