package com.raihanorium.javaconcurrency.files;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

@Log4j2
@Service("fileGeneratorService")
public class CsvFileGeneratorService implements FileGeneratorService {

    private final String linePattern;

    public CsvFileGeneratorService() {
        this.linePattern = "First Name %s,Last Name %s,Organization %s,Tel %s,Tel %s,Mobile %s,Email %s,Address %s,Note %s";
    }

    @Override
    public boolean generate(int lines, String fileName) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, false))) {
            for (int i = 0; i < lines; i++) {
                int number = i + 1;
                writer.write(String.format(getLinePattern(), number, number, number, number, number, number, number, number, number));
                writer.newLine();
            }
            return true;
        } catch (IOException ex) {
            log.error(ex.getMessage(), ex);
            return false;
        }
    }

    @Override
    public String getLinePattern() {
        return this.linePattern;
    }
}
