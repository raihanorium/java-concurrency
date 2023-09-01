package com.raihanorium.javaconcurrency.event;

import com.raihanorium.javaconcurrency.processor.FileProcessor;
import com.raihanorium.javaconcurrency.processor.PhysicalStorageFileProcessingStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Component
@Log4j2
public class FileUploadEventHandler {

    @EventListener
    public void onFileUpload(FileUploadedEvent event) {
        FileProcessor fileProcessor = new FileProcessor(new PhysicalStorageFileProcessingStrategy(), event.getFileName());
        if (!fileProcessor.process()) {
            log.error("Failed to process file.");
        }
    }
}
