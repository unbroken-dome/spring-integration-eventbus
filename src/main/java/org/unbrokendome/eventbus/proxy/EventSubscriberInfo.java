package org.unbrokendome.eventbus.proxy;


interface EventSubscriberInfo {

    String getBeanName();

    Class<?> getBeanClass();

    String getSubscriberMethodName();

    Class<?> getEventType();

    boolean isAsync();
}
