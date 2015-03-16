# Spring Integration Event Bus
An event bus library for Java/Spring projects, based on the Spring Integration framework.

Event-driven architectures promote loosely coupled interactions between event publishers and event subscribers.
Events are essentially messages with added semantics, which is why many event infrastructures are built on some kind of
messaging framework (like Spring Integration).

This library aims to combine the ease of use of the [Guava EventBus](https://code.google.com/p/guava-libraries/wiki/EventBusExplained)
with the power of the Spring and Spring Integration frameworks.

Notable features include:

- Declarative subscriber registration using method annotations
- Async events use a configurable [message store](http://docs.spring.io/spring-integration/reference/htmlsingle/#message-store)
- Transactional subscriber support
- Sync or async behavior on a per-subscriber basis
- Optional auto-configuration when using [Spring Boot](http://projects.spring.io/spring-boot/)


## Prerequisites

To use this library, your project needs to use:
- Java 8
- Spring Integration 4.1.x or higher
- Spring Framework 4.x or higher


## Setup

If your project uses Spring Boot and auto-configuration, then all you have to do is include the spring-integration-eventbus
library JAR on your classpath.

To enable the event bus manually, add the `@EnableEventBus` annotation to one of your `@Configuration` classes:

```java
@Configuration
@EnableEventBus
public class MyConfig {
}
```


## Publishing events

The event bus configuration provides a bean of type `EventPublisher` that can be injected to any bean that wishes
to publish events.

An event may be any object, but since it is going to forwarded to potentially many subscribers, it is preferred to
use only immutable objects as events.

```java
@Service
public class MyService {

    @Autowired
    private EventPublisher eventPublisher;

    public void doSomething() {
        // ...
        eventPublisher.publish(new SomethingHappenedEvent());
    }
}
```

## Subscribing to events

Simply place the `@Subscribe` annotation on any method that should be subscribed to a particular event type.

The method must have a return type of `void` and take exactly one parameter, which will be the event.
The parameter type may be an interface or abstract class.

The method will then be called by the event bus whenever an event of the given type (or a derived type) is published.

```java
@Service
public class MySubscriber {

    @Subscribe
    public void somethingHappened(SomethingHappenedEvent event) {
        System.out.println("Something happened...");
    }
}
```

## Subscription types

The Spring Integration Event Bus supports both synchronous and asynchronous subscribers in the same event bus.

### Synchronous subscribers

Subscribers will be default be called in a synchronous fashion; that means that they run in the same thread
(and hence in the same transaction) as the call to `EventPublisher.publish`.


### Asynchronous subscribers

To mark a subscriber as asynchronous, specify the argument `async = true` on the `@Subscribe` annotation. Events sent
to asynchronous subscribers will be queued using a `ChannelMessageStore`, and run on a different thread than the
publisher.

```java
@Service
public class MySubscriber {

    @Subscribe(async = true)
    public void somethingHappened(SomethingHappenedEvent event) {
        System.out.println("Something happened...");
    }
}
```

## Customizing event bus configuration

To further customize the event bus, implement the `EventBusConfigurer` interface on one of your configuration classes.
It defines a single method, `configureEventBus`, which takes an `EventBusBuilder` that can be used to fine-tune
event bus configuration.

```java
@Configuration
public class MyEventBusConfig implements EventBusConfigurer {

    @Override
    public void configureEventBus(EventBusBuilder eventBusBuilder) {
        eventBusBuilder.setMessageStore(eventBusMessageStore());
    }
}
```

If there are multiple `EventBusConfigurer`s in the application context, the ordering according to Spring's `Ordered`
interface or `@Order` annotation is taken into account. The configurer with the highest precedence is executed _last_
(so it may overwrite the changes made by other configurers). Two configurers with the same order value will be called
in unspecified order.


### Configuring the event message store

By default, an implementation of `SimpleMessageStore` will be used for the event message queue. When asynchronous
subscribers are used, it is advisable to provide a persistent `ChannelMessageStore` instead, so that events will not
get lost between publication and consumption.

You can do so by calling `setMessageStore` on the builder passed to the `configureEventBus` method:

```java
@Configuration
public class EventMessageStoreConfig implements EventBusConfigurer {

    @Autowired
    private DataSource dataSource;


    @Bean
    public ChannelMessageStore eventBusMessageStore() {
        return new JdbcChannelMessageStore(dataSource);
    }

    @Override
    public void configureEventBus(EventBusBuilder eventBusBuilder) {
        eventBusBuilder.setMessageStore(eventBusMessageStore());
    }
}
```

### Configuring the event queue poller

The message queue for asynchronous subscribers requires a polling on the subscribers' end. If no custom poller is
specified, the event bus uses a poller with a short _fixed rate_ (100 milliseconds), a very long _receive timeout_
(30 seconds) and the default `TaskExecutor` (see
[Polling Consumer](http://docs.spring.io/spring-integration/reference/html/messaging-endpoints-chapter.html#endpoint-pollingconsumer)
in the Spring Integration docs).

In addition, if a `PlatformTransactionManager` bean is present in the application context, it will automatically be used
to make the poller transactional.

To customize the polling behavior, call `setEventPoller` on the builder passed to the `configureEventBus` method:

```java
@Configuration
public class EventMessageStoreConfig implements EventBusConfigurer {

    @Bean
    public TaskExecutor eventPollingTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // ... configure the task executor ...
        return executor;
    }

    @Override
    public void configureEventBus(EventBusBuilder eventBusBuilder) {
        eventBusBuilder.setEventPoller(
                Pollers.fixedRate(100)
                       .receiveTimeout(30000)
                       .taskExecutor(eventPollingTaskExecutor()));
    }
}
```

### Configuring the event queue poller using Spring Boot application properties

When using Spring Boot auto-configuration, the following properties may be added to your `application.properties`
to configure the event queue poller:

- `eventbus.poller.fixed-delay`: use a _fixed delay_ poller with the specified value as the delay in milliseconds;
- `eventbus.poller.fixed-rate`: use a _fixed rate_ poller with the specified value as the rate in milliseconds;
- `eventbus.poller.cron`: use a _cron_ poller with the specified value as the cron expression.

These properties are all mutually exclusive, and only one of them should be used.
