package com.raihanorium.javaconcurrency.web;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.fileupload2.core.DiskFileItem;
import org.apache.commons.fileupload2.core.DiskFileItemFactory;
import org.apache.commons.fileupload2.core.FileUploadException;
import org.apache.commons.fileupload2.jakarta.JakartaServletDiskFileUpload;
import org.apache.commons.fileupload2.jakarta.JakartaServletFileUpload;
import org.apache.commons.io.IOUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.*;

@Controller
public class FileUploadController {
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
                    File directory = new File("contacts");

                    boolean dirFound = true;
                    if (!directory.exists()) {
                        dirFound = directory.mkdir();
                    }

                    if (dirFound) {
                        String filename = item.getName();
                        OutputStream out = new FileOutputStream(filename);
                        IOUtils.copyLarge(stream, out);
                        stream.close();
                        out.close();

                        System.out.println("File uploaded, now processing.");

                        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filename)));
                        String strLine;
                        long count = 0;
                        while ((strLine = br.readLine()) != null) {
                            long number = count + 1;
                            try (BufferedWriter writer = new BufferedWriter(new FileWriter("contacts/contact" + number + ".vcf", false))) {
                                writer.write(strLine);
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            } finally {
                                count++;
                            }
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

    @GetMapping(value = "/uploader")
    public String uploaderPage(Model model) {
        model.addAttribute("name", "hello");
        return "uploader";
    }

    @GetMapping(value = "/generate/{lineCount}")
    public ResponseEntity<String> generate(@PathVariable long lineCount) {
        long start = System.currentTimeMillis();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("contacts.csv", false))) {
            for (int i = 0; i < lineCount; i++) {
                int number = i + 1;
                writer.write(String.format("First Name %s,Last Name %s,Organization %s,Tel %s,Tel %s,Mobile %s,Email %s,Address %s,Note %s", number, number, number, number, number, number, number, number, number));
                writer.newLine();
            }
        } catch (IOException ex) {
            return ResponseEntity.ok("Error generating file.\n" + ex);
        }
        return ResponseEntity.ok("Success. Time taken: " + ((System.currentTimeMillis() - start) / 1000) + " s.");
    }
}
