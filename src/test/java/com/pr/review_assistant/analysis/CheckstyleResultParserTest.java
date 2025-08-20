package com.pr.review_assistant.analysis;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CheckstyleResultParserTest {

    @Test
    void testParseCheckstyleResults(@TempDir Path tempDir) throws IOException {
        // Create a sample checkstyle XML result
        String sampleXml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <checkstyle version="10.3.4">
                <file name="/app/src/main/java/com/example/TestClass.java">
                    <error line="10" column="5" severity="warning" message="Missing a Javadoc comment." source="com.puppycrawl.tools.checkstyle.checks.javadoc.MissingJavadocMethodCheck"/>
                    <error line="15" column="20" severity="error" message="Name 'myVariable' must match pattern '^[a-z][a-zA-Z0-9]*$'." source="com.puppycrawl.tools.checkstyle.checks.naming.LocalVariableNameCheck"/>
                </file>
            </checkstyle>
            """;
        
        Path xmlFile = tempDir.resolve("checkstyle-result.xml");
        Files.writeString(xmlFile, sampleXml);
        
        CheckstyleResultParser parser = new CheckstyleResultParser();
        List<Finding> findings = parser.parseCheckstyleResults(xmlFile.toString());
        
        assertEquals(2, findings.size());
        
        Finding first = findings.get(0);
        assertEquals("TestClass.java", first.getFile());
        assertEquals(10, first.getLine());
        assertEquals(5, first.getColumn());
        assertEquals("warning", first.getSeverity());
        assertEquals("MissingJavadocMethodCheck", first.getRule());
        assertEquals("Missing a Javadoc comment.", first.getMessage());
        
        Finding second = findings.get(1);
        assertEquals("TestClass.java", second.getFile());
        assertEquals(15, second.getLine());
        assertEquals(20, second.getColumn());
        assertEquals("error", second.getSeverity());
        assertEquals("LocalVariableNameCheck", second.getRule());
    }
    
    @Test
    void testParseEmptyResults(@TempDir Path tempDir) throws IOException {
        String emptyXml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <checkstyle version="10.3.4">
            </checkstyle>
            """;
        
        Path xmlFile = tempDir.resolve("empty-result.xml");
        Files.writeString(xmlFile, emptyXml);
        
        CheckstyleResultParser parser = new CheckstyleResultParser();
        List<Finding> findings = parser.parseCheckstyleResults(xmlFile.toString());
        
        assertTrue(findings.isEmpty());
    }
}