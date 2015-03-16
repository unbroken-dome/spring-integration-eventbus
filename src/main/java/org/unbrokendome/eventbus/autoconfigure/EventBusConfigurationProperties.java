package org.unbrokendome.eventbus.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.Ordered;
import org.springframework.integration.dsl.core.PollerSpec;
import org.springframework.integration.dsl.core.Pollers;
import org.springframework.util.StringUtils;
import org.unbrokendome.eventbus.components.EventBusBuilder;
import org.unbrokendome.eventbus.config.EventBusConfigurer;


@ConfigurationProperties(prefix = "eventbus")
@SuppressWarnings("unused")
public class EventBusConfigurationProperties implements EventBusConfigurer, Ordered {

    private String name;
    private final Poller poller = new Poller();


    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }


    @Override
    public void configureEventBus(EventBusBuilder eventBusBuilder) {

        if (StringUtils.hasLength(name)) {
            eventBusBuilder.setName(name);
        }

        PollerSpec pollerSpec = poller.toPollerSpec();
        if (pollerSpec != null) {
            eventBusBuilder.setEventPoller(pollerSpec);
        }
    }


    public String getName() {
        return name;
    }


    public void setName(String name) {
        this.name = name;
    }


    public Poller getPoller() {
        return poller;
    }


    public static class Poller {

        private Integer fixedDelay;
        private Integer fixedRate;
        private String cron;

        public Integer getFixedDelay() {
            return fixedDelay;
        }

        public void setFixedDelay(Integer fixedDelay) {
            this.fixedDelay = fixedDelay;
        }

        public Integer getFixedRate() {
            return fixedRate;
        }

        public void setFixedRate(Integer fixedRate) {
            this.fixedRate = fixedRate;
        }

        public String getCron() {
            return cron;
        }

        public void setCron(String cron) {
            this.cron = cron;
        }

        public PollerSpec toPollerSpec() {
            if (fixedDelay != null) {
                return Pollers.fixedDelay(fixedDelay);

            } else if (fixedRate != null) {
                return Pollers.fixedRate(fixedRate);

            } else if (cron != null) {
                return Pollers.cron(cron);

            } else {
                return null;
            }
        }
    }
}
