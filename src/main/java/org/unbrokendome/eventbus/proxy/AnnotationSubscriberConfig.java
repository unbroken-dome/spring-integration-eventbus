package org.unbrokendome.eventbus.proxy;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.unbrokendome.eventbus.EventSubscriber;
import org.unbrokendome.eventbus.components.EventBusBuilder;
import org.unbrokendome.eventbus.config.EventBusConfigurer;

import java.util.Collection;


@Configuration
public class AnnotationSubscriberConfig implements EventBusConfigurer, Ordered, BeanFactoryAware {

    private BeanFactory beanFactory;


    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }


    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }


    @Bean
    public EventSubscriberBeanPostProcessor eventSubscriberBeanFactoryPostProcessor() {
        return new EventSubscriberBeanPostProcessor();
    }


    @Override
    public void configureEventBus(EventBusBuilder eventBusBuilder) {
        Collection<? extends EventSubscriber> eventSubscribers =
                ((ListableBeanFactory) beanFactory).getBeansOfType(EventSubscriberProxy.class).values();

        eventBusBuilder.addSubscribers(eventSubscribers);
    }
}
