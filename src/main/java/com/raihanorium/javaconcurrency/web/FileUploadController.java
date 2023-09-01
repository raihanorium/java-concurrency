package com.raihanorium.javaconcurrency.web;

import com.raihanorium.javaconcurrency.constants.Constants;
import com.raihanorium.javaconcurrency.constants.Path;
import com.raihanorium.javaconcurrency.event.EventPublisher;
import com.raihanorium.javaconcurrency.event.FileUploadedEvent;
import com.raihanorium.javaconcurrency.files.FileGeneratorService;
import com.raihanorium.javaconcurrency.utils.StreamUtils;
import jakarta.annotation.Nonnull;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.fileupload2.jakarta.JakartaServletDiskFileUpload;
import org.apache.commons.fileupload2.jakarta.JakartaServletFileUpload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.File;
import java.io.IOException;

@Log4j2
@Controller
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class FileUploadController {

    public static final String UPLOADER_VIEW = "uploader";
    private static final String GENERATE_VIEW = "generate";
    private static final String MESSAGE = "message";
    private static final String FILE_PRESENT = "filePresent";
    private static final String UPLOAD_FILE_SUCCESS = "File uploaded. Processing going on in the background. Time taken: %s ms. Memory usage: %s mb.";
    public static final String UPLOAD_FILE_ERROR = "Error uploading file.";
    private static final String GENERATE_FILE_SUCCESS = "File generated. Time taken: %s s.";
    public static final String GENERATE_FILE_ERROR = "Error generating file.";
    public static final String NOT_A_MULTIPART_REQUEST = "Not a multipart request.";

    @Nonnull
    private final FileGeneratorService fileGeneratorService;
    @Nonnull
    private final EventPublisher eventPublisher;


    @GetMapping
    public String uploaderPage() {
        return UPLOADER_VIEW;
    }

    @PostMapping(value = Path.UPLOAD)
    public String upload(HttpServletRequest request, Model model) {
        long start = System.currentTimeMillis();

        if (!JakartaServletFileUpload.isMultipartContent(request)) {
            model.addAttribute(MESSAGE, NOT_A_MULTIPART_REQUEST);
            return UPLOADER_VIEW;
        }

        try {
            new JakartaServletDiskFileUpload().getItemIterator(request).forEachRemaining(item -> {
                String filename = item.getName();
                if (!item.isFormField()) {
                    if (StreamUtils.uploadInputStream(item.getInputStream(), item.getName())) {
                        eventPublisher.publish(new FileUploadedEvent(filename));
                    }
                }
            });
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            model.addAttribute(MESSAGE, UPLOAD_FILE_ERROR);
            return UPLOADER_VIEW;
        }

        long memoryUsage = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024 * 1024);
        model.addAttribute(MESSAGE, String.format(UPLOAD_FILE_SUCCESS, (System.currentTimeMillis() - start), memoryUsage));
        return UPLOADER_VIEW;
    }

    @GetMapping(Path.GENERATE)
    public String showGenerate() {
        return GENERATE_VIEW;
    }

    @PostMapping(value = Path.GENERATE)
    public String generate(@RequestParam Integer lines, Model model) {
        long start = System.currentTimeMillis();
        boolean generated = fileGeneratorService.generate(lines, Constants.TEMP_FILE_NAME);
        String message = generated ? String.format(GENERATE_FILE_SUCCESS, ((System.currentTimeMillis() - start) / 1000)) : GENERATE_FILE_ERROR;
        model.addAttribute(FILE_PRESENT, generated);
        model.addAttribute(MESSAGE, message);
        return GENERATE_VIEW;
    }

    @GetMapping(Path.DOWNLOAD)
    public ResponseEntity<InputStreamResource> downloadGeneratedFile() {
        return StreamUtils.getDownloadStream(new File(Constants.TEMP_FILE_NAME))
                .orElseGet(() -> ResponseEntity.noContent().build());
    }
}
