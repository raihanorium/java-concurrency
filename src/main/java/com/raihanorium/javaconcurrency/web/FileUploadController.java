package com.raihanorium.javaconcurrency.web;

import com.raihanorium.javaconcurrency.constants.Constants;
import com.raihanorium.javaconcurrency.constants.Paths;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.nio.file.Path;

@Log4j2
@Controller
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class FileUploadController {

    private static final String UPLOADER_VIEW = "uploader";
    private static final String GENERATE_VIEW = "generate";
    private static final String MESSAGE = "message";
    private static final String FILE_NAME = "fileName";
    private static final String UPLOAD_FILE_SUCCESS = "File processing completed.";
    private static final String UPLOAD_FILE_ERROR = "Error uploading file.";
    private static final String GENERATE_FILE_SUCCESS = "File generated.";
    private static final String GENERATE_FILE_ERROR = "Error generating file.";
    private static final String NOT_A_MULTIPART_REQUEST = "Not a multipart request.";

    @Nonnull
    private final FileGeneratorService fileGeneratorService;
    @Nonnull
    private final EventPublisher eventPublisher;


    @GetMapping
    public String uploaderPage() {
        return UPLOADER_VIEW;
    }

    @PostMapping(value = Paths.UPLOAD)
    public String upload(HttpServletRequest request, Model model) {
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

        model.addAttribute(MESSAGE, UPLOAD_FILE_SUCCESS);
        return UPLOADER_VIEW;
    }

    @GetMapping(Paths.GENERATE)
    public String showGenerate() {
        return GENERATE_VIEW;
    }

    @PostMapping(value = Paths.GENERATE)
    public String generate(@RequestParam Integer lines, Model model) {
        boolean generated = fileGeneratorService.generate(lines, Constants.TEMP_FILE_NAME);
        String message = generated ? GENERATE_FILE_SUCCESS : GENERATE_FILE_ERROR;
        model.addAttribute(FILE_NAME, Path.of(Constants.TEMP_FILE_NAME));
        model.addAttribute(MESSAGE, message);
        return GENERATE_VIEW;
    }

    @GetMapping(Paths.DOWNLOAD)
    public ResponseEntity<InputStreamResource> downloadGeneratedFile(@PathVariable String fileName) {
        return StreamUtils.getDownloadStream(Path.of(fileName).toFile())
                .orElseGet(() -> ResponseEntity.noContent().build());
    }
}
