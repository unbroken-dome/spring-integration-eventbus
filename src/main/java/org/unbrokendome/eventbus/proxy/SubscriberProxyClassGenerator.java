package org.unbrokendome.eventbus.proxy;


import org.unbrokendome.eventbus.EventSubscriber;

interface SubscriberProxyClassGenerator {

    Class<? extends EventSubscriber> generate(EventSubscriberInfo subscriberInfo, ClassLoader classLoader);
}
