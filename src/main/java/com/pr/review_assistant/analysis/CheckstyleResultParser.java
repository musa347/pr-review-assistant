package com.pr.review_assistant.analysis;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

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
            
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(xmlFile);
            
            NodeList fileNodes = document.getElementsByTagName("file");
            
            for (int i = 0; i < fileNodes.getLength(); i++) {
                Element fileElement = (Element) fileNodes.item(i);
                String fileName = fileElement.getAttribute("name");
                
                NodeList errorNodes = fileElement.getElementsByTagName("error");
                
                for (int j = 0; j < errorNodes.getLength(); j++) {
                    Element errorElement = (Element) errorNodes.item(j);
                    
                    Finding finding = Finding.builder()
                            .file(extractFileName(fileName))
                            .line(Integer.parseInt(errorElement.getAttribute("line")))
                            .column(Integer.parseInt(errorElement.getAttribute("column")))
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
}