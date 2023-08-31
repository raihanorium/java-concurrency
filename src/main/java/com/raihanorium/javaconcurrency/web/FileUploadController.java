package com.raihanorium.javaconcurrency.web;

import com.raihanorium.javaconcurrency.constants.Path;
import com.raihanorium.javaconcurrency.files.FileGeneratorService;
import com.raihanorium.javaconcurrency.utils.StreamUtils;
import jakarta.annotation.Nonnull;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.fileupload2.core.DiskFileItem;
import org.apache.commons.fileupload2.core.DiskFileItemFactory;
import org.apache.commons.fileupload2.core.FileUploadException;
import org.apache.commons.fileupload2.jakarta.JakartaServletDiskFileUpload;
import org.apache.commons.fileupload2.jakarta.JakartaServletFileUpload;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.*;

@Log4j2
@Controller
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class FileUploadController {

    private static final String GENERATE_VIEW = "generate";
    private static final String MESSAGE = "message";
    private static final String TEMP_FILE_NAME = "contacts.csv";
    private static final String FILE_PRESENT = "filePresent";
    private static final String GENERATE_FILE_SUCCESS = "File generated. Time taken: %s s.";
    public static final String GENERATE_FILE_ERROR = "Error generating file.";

    @Nonnull
    private final FileGeneratorService fileGeneratorService;


    @GetMapping
    public String uploaderPage(Model model) {
        model.addAttribute("name", "hello");
        return "uploader";
    }

    @PostMapping(value = "/upload")
    public @ResponseBody ResponseEntity<String> upload(HttpServletRequest request) {
        long start = System.currentTimeMillis();
        try {
            boolean isMultipart = JakartaServletFileUpload.isMultipartContent(request);
            if (!isMultipart) {
                return ResponseEntity.ok("Not a multipart request.");
            }

            JakartaServletFileUpload<DiskFileItem, DiskFileItemFactory> upload = new JakartaServletDiskFileUpload();

            upload.getItemIterator(request).forEachRemaining(item -> {
                String name = item.getFieldName();
                InputStream stream = item.getInputStream();
                if (item.isFormField()) {
                    System.out.println("Form field " + name + " detected.");
                } else {
                    System.out.println("File field " + name + " with file name " + item.getName() + " detected.");

                    String filename = item.getName();
                    OutputStream out = new FileOutputStream(filename);
                    IOUtils.copyLarge(stream, out);
                    stream.close();
                    out.close();

                    System.out.println("File uploaded, now processing.");

                    File directory = new File("contacts");
                    if (!directory.exists()) {
                        directory.mkdir();
                    } else {
                        directory.delete();
                    }

                    BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filename)));
                    String strLine;
                    long count = 0;
                    while ((strLine = br.readLine()) != null) {
                        File currentDirectory = new File(directory, String.valueOf(count / 1000));
                        currentDirectory.mkdirs();
                        try (BufferedWriter writer = new BufferedWriter(new FileWriter(new File(currentDirectory, "contact" + count + ".vcf"), false))) {
                            writer.write(strLine);
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        } finally {
                            count++;
                        }
                    }

                }
            });
        } catch (FileUploadException e) {
            return ResponseEntity.ok("File upload error\n" + e);
        } catch (IOException e) {
            return ResponseEntity.ok("Internal server IO error\n" + e);
        }

        return ResponseEntity.ok("Success. Time taken: " + ((System.currentTimeMillis() - start) / 1000) + " s.");
    }

    @GetMapping(Path.GENERATE)
    public String showGenerate() {
        return GENERATE_VIEW;
    }

    @PostMapping(value = Path.GENERATE)
    public String generate(@RequestParam Integer lines, Model model) {
        long start = System.currentTimeMillis();
        boolean generated = fileGeneratorService.generate(lines, TEMP_FILE_NAME);
        String message = generated ? String.format(GENERATE_FILE_SUCCESS, ((System.currentTimeMillis() - start) / 1000)) : GENERATE_FILE_ERROR;
        model.addAttribute(FILE_PRESENT, generated);
        model.addAttribute(MESSAGE, message);
        return GENERATE_VIEW;
    }

    @GetMapping(Path.DOWNLOAD)
    public ResponseEntity<InputStreamResource> downloadGeneratedFile() {
        return StreamUtils.getDownloadStream(new File(TEMP_FILE_NAME))
                .orElseGet(() -> ResponseEntity.noContent().build());
    }
}
