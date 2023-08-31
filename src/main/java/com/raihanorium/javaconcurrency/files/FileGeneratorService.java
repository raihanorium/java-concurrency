package com.raihanorium.javaconcurrency.files;

public interface FileGeneratorService {

    boolean generate(int lines, String fileName);

    String getLinePattern();
}
