package com.raihanorium.javaconcurrency.utils;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Log4j2
public class StreamUtils {

    private static final String CONTENT_DISPOSITION_PATTERN = "attachment; filename=\"%s\"";

    public static Optional<ResponseEntity<InputStreamResource>> getDownloadStream(File file) {
        if (file.exists()) {
            try {
                String fileName = URLEncoder.encode(file.getName(), StandardCharsets.UTF_8);
                final HttpHeaders httpHeaders = new HttpHeaders();
                httpHeaders.set(HttpHeaders.LAST_MODIFIED, String.valueOf(file.lastModified()));
                httpHeaders.set(HttpHeaders.CONTENT_DISPOSITION, String.format(CONTENT_DISPOSITION_PATTERN, fileName));
                httpHeaders.set(HttpHeaders.CONTENT_LENGTH, String.valueOf(file.length()));
                return Optional.of(ResponseEntity.ok()
                        .headers(httpHeaders)
                        .contentLength(file.length())
                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
                        .body(new InputStreamResource(new FileInputStream(file))));
            } catch (IOException ex) {
                log.error(ex.getMessage(), ex);
            }
        }
        return Optional.empty();
    }

    public static boolean uploadInputStream(InputStream inputStream, String filename) {
        try (OutputStream outputStream = new FileOutputStream(filename)) {
            IOUtils.copyLarge(inputStream, outputStream);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            return false;
        }
        return true;
    }
}
