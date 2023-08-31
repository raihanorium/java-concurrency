package com.raihanorium.javaconcurrency.event;

import lombok.Getter;

@Getter
public class FileUploadedEvent extends Event {

    private final String fileName;

    public FileUploadedEvent(String fileName) {
        super();
        this.fileName = fileName;
    }

}
