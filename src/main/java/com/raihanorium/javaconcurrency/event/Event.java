package com.raihanorium.javaconcurrency.event;

import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
public abstract class Event implements Serializable {

    @Serial
    private static final long serialVersionUID = -7079366385789391543L;

    private final LocalDateTime timestamp;

    public Event() {
        this.timestamp = LocalDateTime.now();
    }

}
