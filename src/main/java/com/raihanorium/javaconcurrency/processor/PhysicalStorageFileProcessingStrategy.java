package com.raihanorium.javaconcurrency.processor;

import com.raihanorium.javaconcurrency.constants.Constants;
import lombok.extern.log4j.Log4j2;

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
            if (root.exists()) {
                if (!root.delete()) {
                    throw new RuntimeException("Could not clear existing physical storage directory.");
                }
            }

            if (!root.mkdir()) {
                throw new RuntimeException("Could not create physical storage directory.");
            }

            ExecutorService executorService = Executors.newFixedThreadPool(Constants.THREAD_POOL_SIZE);
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
            String line;
            long count = 0;
            while ((line = reader.readLine()) != null) {
                final String content = line;
                final long index = count;
                executorService.submit(() -> {
                    File vcfFile = new File(getDirectory(root, index), String.valueOf(index));
                    if (!vcfFile.createNewFile()) {
                        log.error("Could not create vcf file {}", index);
                    }
                    try (BufferedWriter writer = new BufferedWriter(new FileWriter(new File(getDirectory(root, index), "contact" + index + ".vcf"), false))) {
                        writer.write(content);
                    }
                    return vcfFile;
                });
                executorService.shutdown();
                if (executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS)) {
                    throw new RuntimeException("Executor service terminated.");
                }
                count++;
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    private File getDirectory(File rootDirectory, long currentFileNumber) throws IOException {
        long firstLayerIndex = currentFileNumber / 1000;

        if (currentFileNumber < 1000) {
            File file = new File(rootDirectory, String.valueOf(currentFileNumber / 1000));
            if (!file.exists()) {
                if (!file.createNewFile()) {
                    throw new RuntimeException("Could not create directory");
                }
            }
            return file;
        } else if (currentFileNumber < 1000000) {
            File layer1Directory = new File(rootDirectory, String.valueOf(currentFileNumber / 1000));
            if (!layer1Directory.exists()) {
                if (!layer1Directory.createNewFile()) {
                    throw new RuntimeException("Could not create directory");
                }
            }

            File file = new File(layer1Directory, String.valueOf(currentFileNumber / 100000));
            if (!file.exists()) {
                if (!file.createNewFile()) {
                    throw new RuntimeException("Could not create directory");
                }
            }
            return file;
        }
        return null;
    }
}
