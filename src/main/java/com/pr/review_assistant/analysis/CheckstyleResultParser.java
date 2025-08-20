package com.pr.review_assistant.analysis;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
public class CheckstyleResultParser {
    
    public List<Finding> parseCheckstyleResults(String xmlFilePath) {
        List<Finding> findings = new ArrayList<>();
        
        try {
            File xmlFile = new File(xmlFilePath);
            if (!xmlFile.exists()) {
                log.warn("Checkstyle results file not found: {}", xmlFilePath);
                return findings;
            }
            
            // Load the list of changed files to filter results
            Set<String> changedFiles = loadChangedFiles();
            
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(xmlFile);
            
            NodeList fileNodes = document.getElementsByTagName("file");
            
            for (int i = 0; i < fileNodes.getLength(); i++) {
                Element fileElement = (Element) fileNodes.item(i);
                String fileName = fileElement.getAttribute("name");
                
                // Skip files that are not in the changed files list
                if (!changedFiles.isEmpty() && !isFileInChangedList(fileName, changedFiles)) {
                    log.debug("Skipping file not in changed list: {}", fileName);
                    continue;
                }
                
                NodeList errorNodes = fileElement.getElementsByTagName("error");
                
                for (int j = 0; j < errorNodes.getLength(); j++) {
                    Element errorElement = (Element) errorNodes.item(j);
                    
                    Finding finding = Finding.builder()
                            .file(extractFileName(fileName))
                            .line(parseIntegerAttribute(errorElement.getAttribute("line"), 0))
                            .column(parseIntegerAttribute(errorElement.getAttribute("column"), 0))
                            .severity(errorElement.getAttribute("severity"))
                            .rule(extractRuleName(errorElement.getAttribute("source")))
                            .message(errorElement.getAttribute("message"))
                            .source(errorElement.getAttribute("source"))
                            .build();
                    
                    findings.add(finding);
                }
            }
            
            log.info("Parsed {} findings from checkstyle results", findings.size());
            
        } catch (Exception e) {
            log.error("Error parsing checkstyle results: {}", e.getMessage(), e);
        }
        
        return findings;
    }
    
    private String extractFileName(String fullPath) {
        if (fullPath == null) return "";
        int lastSlash = fullPath.lastIndexOf('/');
        return lastSlash >= 0 ? fullPath.substring(lastSlash + 1) : fullPath;
    }
    
    private String extractRuleName(String source) {
        if (source == null) return "";
        int lastDot = source.lastIndexOf('.');
        return lastDot >= 0 ? source.substring(lastDot + 1) : source;
    }
    
    private int parseIntegerAttribute(String value, int defaultValue) {
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            log.debug("Could not parse integer value '{}', using default: {}", value, defaultValue);
            return defaultValue;
        }
    }
    
    private Set<String> loadChangedFiles() {
        Set<String> changedFiles = new HashSet<>();
        Path changedFilesPath = Paths.get("artifacts/changed-files.txt");
        
        try {
            if (Files.exists(changedFilesPath)) {
                List<String> lines = Files.readAllLines(changedFilesPath);
                for (String line : lines) {
                    String trimmedLine = line.trim();
                    if (!trimmedLine.isEmpty()) {
                        changedFiles.add(trimmedLine);
                    }
                }
                log.debug("Loaded {} changed files from {}", changedFiles.size(), changedFilesPath);
            } else {
                log.debug("Changed files list not found at {}, will process all files", changedFilesPath);
            }
        } catch (IOException e) {
            log.warn("Could not read changed files list from {}: {}", changedFilesPath, e.getMessage());
        }
        
        return changedFiles;
    }
    
    private boolean isFileInChangedList(String filePath, Set<String> changedFiles) {
        if (filePath == null || changedFiles.isEmpty()) {
            return false;
        }
        
        // Extract just the file path relative to the project root
        // The filePath from checkstyle might be absolute, so we need to normalize it
        String normalizedPath = filePath;
        
        // Remove common prefixes that might be added by checkstyle
        if (normalizedPath.startsWith("/app/")) {
            normalizedPath = normalizedPath.substring(5); // Remove "/app/"
        }
        
        // Check if any of the changed files match this file
        for (String changedFile : changedFiles) {
            if (normalizedPath.endsWith(changedFile) || changedFile.endsWith(normalizedPath)) {
                log.debug("File {} matches changed file {}", filePath, changedFile);
                return true;
            }
        }
        
        log.debug("File {} not found in changed files list", filePath);
        return false;
    }
}