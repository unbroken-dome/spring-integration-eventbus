package org.unbrokendome.eventbus.components;

import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import org.springframework.integration.channel.NullChannel;
import org.springframework.integration.core.MessageSelector;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.RecipientListRouterSpec;
import org.springframework.integration.dsl.core.PollerSpec;
import org.springframework.integration.dsl.core.Pollers;
import org.springframework.integration.selector.MessageSelectorChain;
import org.springframework.integration.store.ChannelMessageStore;
import org.springframework.integration.store.SimpleMessageStore;
import org.springframework.messaging.MessageChannel;
import org.springframework.transaction.PlatformTransactionManager;
import org.unbrokendome.eventbus.EventSubscriber;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


public class EventBusBuilderImpl implements EventBusBuilder {

    private static final String DEFAULT_NAME = "eventBus";
    private static final PollerSpec DEFAULT_EVENT_POLLER = Pollers.fixedRate(10).receiveTimeout(30000);

    private final MessageChannel inputChannel;
    private String name;
    private ChannelMessageStore messageStore;
    private PlatformTransactionManager transactionManager;
    private final List<EventSubscriber> subscribers = new ArrayList<>();
    private PollerSpec eventPoller = Pollers.fixedDelay(1000);


    public EventBusBuilderImpl(MessageChannel inputChannel) {
        this.inputChannel = inputChannel;
    }


    @Override
    public EventBusBuilder setName(String name) {
        this.name = name;
        return this;
    }


    @Override
    public EventBusBuilder setMessageStore(ChannelMessageStore messageStore) {
        this.messageStore = messageStore;
        return this;
    }


    @Override
    public EventBusBuilder setTransactionManager(PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
        return this;
    }


    @Override
    public EventBusBuilder addSubscribers(Iterable<? extends EventSubscriber> subscribers) {
        for (EventSubscriber subscriber : subscribers) {
            this.subscribers.add(subscriber);
        }
        return this;
    }


    @Override
    public EventBusBuilder setEventPoller(PollerSpec eventPoller) {
        this.eventPoller = eventPoller;
        return this;
    }


    private void applyDefaultsToMissingProperties() {
        if (name == null) {
            name = DEFAULT_NAME;
        }
        if (messageStore == null) {
            messageStore = new SimpleMessageStore();
        }
        if (eventPoller == null) {
            eventPoller = DEFAULT_EVENT_POLLER;
        }
    }


    @Override
    public IntegrationFlow build() {
        applyDefaultsToMissingProperties();

        if (subscribers.isEmpty()) {
            return f -> f
                    .channel(new NullChannel());
        }

        if (transactionManager != null) {
            eventPoller.transactional(transactionManager);
        }

        SyncSubscriberSet syncSubscribers = getSyncSubscribers();
        AsyncSubscriberSet asyncSubscribers = getAsyncSubscribers();

        return IntegrationFlows.from(inputChannel)
                .routeToRecipients(
                        router -> {
                            asyncSubscribers.configureFlow(router);
                            syncSubscribers.configureFlow(router);
                        },
                        spec -> spec.id(name + "_syncAsyncRouter"))
                .get();
    }


    private SyncSubscriberSet getSyncSubscribers() {
        return new SyncSubscriberSet(
                subscribers.stream()
                        .filter(s -> !s.isAsync())
                        .collect(Collectors.toList()));
    }


    private AsyncSubscriberSet getAsyncSubscribers() {
        return new AsyncSubscriberSet(
                subscribers.stream()
                        .filter(EventSubscriber::isAsync)
                        .collect(Collectors.toList()));
    }


    private abstract class SubscriberSet {
        private final Multimap<Class<?>, EventSubscriber> subscribersByEventType;


        public SubscriberSet(Collection<EventSubscriber> subscribers) {
            this.subscribersByEventType = Multimaps.index(subscribers, EventSubscriber::getEventType);
        }


        protected abstract String getMode();


        public Set<Class<?>> getEventTypes() {
            return subscribersByEventType.keySet();
        }


        public boolean isEmpty() {
            return subscribersByEventType.isEmpty();
        }


        public MessageSelector createMessageSelector() {
            MessageSelectorChain chain = new MessageSelectorChain();
            chain.setSelectors(
                    getEventTypes().stream()
                            .map(EventTypeMessageSelector::new)
                            .collect(Collectors.toList()));
            chain.setVotingStrategy(MessageSelectorChain.VotingStrategy.ANY);
            return chain;
        }


        protected final IntegrationFlow createFlowForEventType(Class<?> eventType) {
            return f -> f
                    .filter(p -> eventType.isAssignableFrom(p.getClass()),
                            spec -> spec.id(name + "_" + getMode() + "EventTypeFilter_" + eventType.getName()))
                    .publishSubscribeChannel(null, pubsub -> {
                        pubsub.id(name + "_pubsub_" + getMode() + "Event_" + eventType.getName());
                        subscribersByEventType.get(eventType).stream()
                                .map(this::createSubscriptionFlow)
                                .forEach(pubsub::subscribe);
                    });
        }


        private IntegrationFlow createSubscriptionFlow(EventSubscriber subscriber) {
            return f -> f
                    .handle(subscriber,
                            spec -> spec.id(name + "_" + getMode() + "SubscriptionHandler_" + subscriber.hashCode()));
        }
    }


    private class SyncSubscriberSet extends SubscriberSet {

        public SyncSubscriberSet(Collection<EventSubscriber> subscribers) {
            super(subscribers);
        }


        @Override
        protected String getMode() {
            return "sync";
        }

        public IntegrationFlow createEventsFlow() {
            return f -> f
                    .publishSubscribeChannel(pubsub -> {
                        pubsub.id(name + "_pubsub_allSyncEvents");
                        getEventTypes().stream()
                                .map(this::createFlowForEventType)
                                .forEach(pubsub::subscribe);
                    });
        }


        public void configureFlow(RecipientListRouterSpec router) {
            if (!isEmpty()) {
                router.recipientFlow(
                        createMessageSelector(),
                        createEventsFlow());
            }
        }
    }


    private class AsyncSubscriberSet extends SubscriberSet {

        public AsyncSubscriberSet(Collection<EventSubscriber> subscribers) {
            super(subscribers);
        }


        @Override
        protected String getMode() {
            return "async";
        }


        public IntegrationFlow createEventsFlow() {
            return f -> f
                    .channel(ch -> ch.queue(
                            name + "_asyncEventQueue",
                            messageStore,
                            name + ":AsyncEventQueue"))
                    .bridge(spec -> spec.poller(eventPoller)
                            .id(name + "_asyncPollingBridge"))
                    .publishSubscribeChannel(pubsub -> {
                        pubsub.id(name + "_pubsub_allAsyncEvents");
                        getEventTypes().stream()
                                .map(this::createFlowForEventType)
                                .forEach(pubsub::subscribe);
                    });
        }


        public void configureFlow(RecipientListRouterSpec router) {
            if (!isEmpty()) {
                router.recipientFlow(
                        createMessageSelector(),
                        createEventsFlow());
            }
        }
    }
}
