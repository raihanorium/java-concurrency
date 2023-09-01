package com.raihanorium.javaconcurrency.processor;

import com.raihanorium.javaconcurrency.constants.Constants;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Log4j2
public class PhysicalStorageFileProcessingStrategy implements FileProcessingStrategy {

    private static final String CONTACT_FILE_NAME_PATTERN = "contact%s.vcf";

    @Override
    public boolean process(String fileName) {
        try {
            long start = System.currentTimeMillis();
            File root = new File(Constants.PHYSICAL_STORAGE_DIRECTORY);
            FileUtils.forceMkdir(root);
            FileUtils.cleanDirectory(root);
            log.info("Output directory cleanup finished in {} ms.", System.currentTimeMillis() - start);

            start = System.currentTimeMillis();
            ExecutorService executorService = Executors.newFixedThreadPool(Constants.THREAD_POOL_SIZE);
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
            String line;
            long index = 0;
            while ((line = reader.readLine()) != null) {
                Callable<File> fileCallable = createFileFromLine(root, line, index);
                executorService.submit(fileCallable);
                index++;
            }
            executorService.shutdown();
            if (executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS)) {
                executorService.shutdownNow();
            }
            log.info("File processing finished in {} ms.", System.currentTimeMillis() - start);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    private Callable<File> createFileFromLine(final File root, final String line, final long index) {
        return () -> {
            File directory = new File(root, String.valueOf(index / 1000));
            FileUtils.forceMkdir(directory);
            File vcfFile = new File(directory, String.format(CONTACT_FILE_NAME_PATTERN, index));
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(vcfFile, false))) {
                writer.write(line);
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
            }
            return vcfFile;
        };
    }
}
