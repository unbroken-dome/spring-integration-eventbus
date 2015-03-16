package org.unbrokendome.eventbus.proxy

import org.unbrokendome.eventbus.Subscribe
import org.unbrokendome.eventbus.TestEvent
import spock.lang.Specification
import spock.lang.Subject


class ReflectiveSubscriberScannerTest extends Specification {

    @Subject
    def scanner = new ReflectiveSubscriberScanner()


    def "Scan returns EventSubscriberInfo for the sync subscribe method"() {
        when:
            def subscriberInfos = scanner.scanForSubscriberMethods('testSubscriber', SyncTestSubscriber).findAll()

        then:
            subscriberInfos.size() == 1
            subscriberInfos[0].with {
                beanName == 'testSubscriber'
                beanClass == SyncTestSubscriber
                subscriberMethodName == 'handleTestEvent'
                eventType == TestEvent
                async == false
            }
    }


    static class SyncTestSubscriber {
        @Subscribe(async = false)
        void handleTestEvent(TestEvent event) { }
    }


    def "Scan returns EventSubscriberInfo for the async subscribe method"() {
        when:
            def subscriberInfos = scanner.scanForSubscriberMethods('testSubscriber', AsyncTestSubscriber).findAll()

        then:
            subscriberInfos.size() == 1
            subscriberInfos[0].with {
                beanName == 'testSubscriber'
                beanClass == AsyncTestSubscriber
                subscriberMethodName == 'handleTestEvent'
                eventType == TestEvent
                async == true
            }
    }


    static class AsyncTestSubscriber {
        @Subscribe(async = true)
        void handleTestEvent(TestEvent event) { }
    }


    def "Scan omits subscriber method with more than one parameter"() {
        when:
            def subscriberInfos = scanner.scanForSubscriberMethods('testSubscriber', TestSubscriber_MoreThanOneParam)

        then: 'Scan should not return any subscribers'
            !subscriberInfos.any()
    }


    static class TestSubscriber_MoreThanOneParam {
        @Subscribe
        void handleTestEvent(TestEvent event, int param) { }
    }


    def "Scan omits subscriber method with non-void return type"() {
        when:
            def subscriberInfos = scanner.scanForSubscriberMethods('testSubscriber', TestSubscriber_NonVoidReturnType)

        then: 'Scan should not return any subscribers'
            !subscriberInfos.any()
    }


    static class TestSubscriber_NonVoidReturnType {
        @Subscribe
        boolean handleTestEvent(TestEvent event) { false }
    }


    def "Scan finds @Subscribe annotation on interface"() {
        when:
            def subscriberInfos = scanner.scanForSubscriberMethods(
                    'testSubscriber', TestSubscriber_NotAnnotated_ImplementingAnnotated)
                .findAll()
        then:
            subscriberInfos.size() == 1
            subscriberInfos[0].with {
                beanName == 'testSubscriber'
                beanClass == TestSubscriberIF_Annotated
                subscriberMethodName == 'handleTestEvent'
                eventType == TestEvent
            }
    }


    def "Scan uses overridden method on implemented interface"() {
        when:
            def subscriberInfos = scanner.scanForSubscriberMethods(
                    'testSubscriber', TestSubscriber_Annotated_ImplementingNotAnnotated)
                    .findAll()
        then:
            subscriberInfos.size() == 1
            subscriberInfos[0].with {
                beanName == 'testSubscriber'
                beanClass == TestSubscriberIF_NotAnnotated
                subscriberMethodName == 'handleTestEvent'
                eventType == TestEvent
                async == true
            }
    }


    def "Annotation on class overrides annotation on interface"() {
        when:
            def subscriberInfos = scanner.scanForSubscriberMethods(
                    'testSubscriber', TestSubscriber_Annotated_ImplementingAnnotated)
                    .findAll()
        then:
            subscriberInfos.size() == 1
            subscriberInfos[0].with {
                beanName == 'testSubscriber'
                beanClass == TestSubscriberIF_Annotated
                subscriberMethodName == 'handleTestEvent'
                eventType == TestEvent
                async == true
            }
    }


    static interface TestSubscriberIF_Annotated {
        @Subscribe
        void handleTestEvent(TestEvent event)
    }


    static interface TestSubscriberIF_NotAnnotated {
        void handleTestEvent(TestEvent event)
    }


    static class TestSubscriber_NotAnnotated_ImplementingAnnotated implements TestSubscriberIF_Annotated {
        @Override
        void handleTestEvent(TestEvent event) { }
    }


    static class TestSubscriber_Annotated_ImplementingAnnotated implements TestSubscriberIF_Annotated {
        @Override
        @Subscribe(async = true)
        void handleTestEvent(TestEvent event) { }
    }


    static class TestSubscriber_Annotated_ImplementingNotAnnotated implements TestSubscriberIF_NotAnnotated {
        @Override
        @Subscribe(async = true)
        void handleTestEvent(TestEvent event) { }
    }
}
