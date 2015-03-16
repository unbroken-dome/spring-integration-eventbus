package org.unbrokendome.eventbus.proxy

import org.unbrokendome.eventbus.proxy.EventSubscriberInfo


class SimpleSubscriberInfo implements EventSubscriberInfo {

    String beanName;
    Class<?> beanClass;
    String subscriberMethodName;
    Class<?> eventType;
    boolean async;
}
