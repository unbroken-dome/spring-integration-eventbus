package org.unbrokendome.eventbus.config;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;


@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(EventBusConfig.class)
public @interface EnableEventBus {
}
