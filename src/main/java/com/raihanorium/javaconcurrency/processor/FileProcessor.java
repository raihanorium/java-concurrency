package com.raihanorium.javaconcurrency.processor;

import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FileProcessor {

    @Nonnull
    private final FileProcessingStrategy strategy;
    @Nonnull
    private final String fileName;


    public boolean process() {
        return strategy.process(fileName);
    }

}
