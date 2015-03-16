package org.unbrokendome.eventbus.proxy

import org.springframework.integration.support.MessageBuilder
import org.unbrokendome.eventbus.EventSubscriber
import org.unbrokendome.eventbus.TestEvent
import org.unbrokendome.eventbus.TestSubscriber
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Subject

class CglibSubscriberProxyClassGeneratorTest extends Specification {

    @Shared
    def subscriberInfo = new SimpleSubscriberInfo(
            beanClass     : TestSubscriber,
            subscriberMethodName: 'handleTestEvent',
            eventType     : TestEvent,
            async         : false)

    @Subject
    def proxyClassGenerator = new CglibSubscriberProxyClassGenerator()

    def classLoader = new IsolatedClassLoader(Thread.currentThread().contextClassLoader)


    def "Generate returns proxy class"() {
        when:
            def proxyClass = proxyClassGenerator.generate(subscriberInfo, classLoader)
        then:
            proxyClass != null
            EventSubscriber.isAssignableFrom proxyClass
    }


    def "handleMessage on proxy forwards to subscriber method"() {
        given:
            def testSubscriber = Mock(TestSubscriber)
            def event = new TestEvent('1234')
            def eventMessage = MessageBuilder.withPayload(event).build()

        when: 'proxy is generated'
            def proxy = newProxyInstance(testSubscriber)
        and: 'handleMessage is called'
            proxy.handleMessage(eventMessage)

        then:
            1 * testSubscriber.handleTestEvent(event)
    }


    def "getEventType returns the configured event type"() {
        when: 'proxy is generated'
            def proxy = newProxyInstance(new TestSubscriber())

        then:
            proxy.getEventType() == TestEvent
    }


    def "isAsync returns the configured async flag"() {
        given:
            subscriberInfo.async = async

        when: 'proxy is generated'
            def proxy = newProxyInstance(new TestSubscriber())

        then:
            proxy.isAsync() == async

        where:
            async << [ true, false ]
    }



    private EventSubscriber newProxyInstance(TestSubscriber testSubscriber) {
        def proxyClass = proxyClassGenerator.generate(subscriberInfo, classLoader)
        newProxyInstance(proxyClass, testSubscriber)
    }


    private EventSubscriber newProxyInstance(Class<?> proxyClass, TestSubscriber testSubscriber) {
        proxyClass.getConstructor(TestSubscriber).newInstance(testSubscriber)
    }


    static class IsolatedClassLoader extends ClassLoader {
        public IsolatedClassLoader(ClassLoader parent) {
            super(parent)
        }
    }
}
