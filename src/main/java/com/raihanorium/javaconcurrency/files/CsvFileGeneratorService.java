package com.raihanorium.javaconcurrency.files;

import com.raihanorium.javaconcurrency.constants.Constants;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

@Log4j2
@Service("fileGeneratorService")
public class CsvFileGeneratorService implements FileGeneratorService {

    @Override
    public boolean generate(int lines, String fileName) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, false))) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < lines; i++) {
                int number = i + 1;
                writer.write(String.format(Constants.CSV_LINE_PATTERN, number, number, number, number, number, number, number, number, number));
                writer.newLine();
            }
            log.info("File generated with {} lines in {} ms.", lines, System.currentTimeMillis() - start);
            return true;
        } catch (IOException ex) {
            log.error(ex.getMessage(), ex);
            return false;
        }
    }
}
