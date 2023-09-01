package com.raihanorium.javaconcurrency.processor;

import com.raihanorium.javaconcurrency.constants.Constants;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Log4j2
public class PhysicalStorageFileProcessingStrategy implements FileProcessingStrategy {
    @Override
    public boolean process(String fileName) {
        try {
            File root = new File(Constants.PHYSICAL_STORAGE_DIRECTORY);
            FileUtils.forceMkdir(root);
            FileUtils.cleanDirectory(root);

            ExecutorService executorService = Executors.newFixedThreadPool(Constants.THREAD_POOL_SIZE);
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
            String line;
            long count = 0;
            while ((line = reader.readLine()) != null) {
                final String content = line;
                final long index = count;
                executorService.submit(() -> {
                    File directory = new File(root, String.valueOf(index / 1000));
                    FileUtils.forceMkdir(directory);
                    File vcfFile = new File(directory, "contact" + index + ".vcf");
                    try (BufferedWriter writer = new BufferedWriter(new FileWriter(vcfFile, false))) {
                        log.info("Writing file {} in thread {}", vcfFile.getName(), Thread.currentThread().getName());
                        writer.write(content);
                    } catch (Exception ex) {
                        log.error(ex.getMessage(), ex);
                    }
                    return vcfFile;
                });
                count++;
            }
            executorService.shutdown();
            if (executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS)) {
                executorService.shutdownNow();
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        return true;
    }
}
