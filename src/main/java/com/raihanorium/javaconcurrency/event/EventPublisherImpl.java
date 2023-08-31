package com.raihanorium.javaconcurrency.event;

import jakarta.annotation.Nonnull;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.stereotype.Component;

@Component
public class EventPublisherImpl implements EventPublisher, ApplicationEventPublisherAware {

    private ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void setApplicationEventPublisher(@Nonnull ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public void publish(Event event) {
        applicationEventPublisher.publishEvent(event);
    }
}
