package com.example.todoapp.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/import")
public class ImportController {

    /**
     * VULNERABLE (CWE-611, XML External Entity Injection): DocumentBuilderFactory is used
     * with its default settings, which resolve DOCTYPE declarations and external entities.
     * An uploaded file containing
     * {@code <!DOCTYPE x [<!ENTITY xxe SYSTEM "file:///etc/passwd">]>} lets the parser read
     * arbitrary local files (or reach internal network services) and embed the result in
     * the parsed document. The factory should call
     * {@code setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)} (or
     * disable external general/parameter entities) before parsing untrusted input.
     */
    @PostMapping("/xml")
    public ResponseEntity<List<String>> importXml(@RequestParam MultipartFile file)
            throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(file.getInputStream());

        NodeList titles = document.getElementsByTagName("title");
        List<String> imported = new ArrayList<>();
        for (int i = 0; i < titles.getLength(); i++) {
            imported.add(titles.item(i).getTextContent());
        }
        return ResponseEntity.ok(imported);
    }
}
