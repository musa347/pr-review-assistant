package com.pr.review_assistant.test;

import java.util.*;
import java.io.*;

/**
 * Test class with intentional code issues to trigger analysis
 */
public class TestCodeWithIssues {
    
    // Issue 1: Unused field
    private String unusedField = "not used";
    
    // Issue 2: Non-final static field
    public static String publicStaticField = "bad practice";
    
    // Issue 3: Method with too many parameters and poor naming
    public void badMethod(String a, String b, String c, String d, String e, String f) {
        // Issue 4: Unused variable
        String unused = "this variable is never used";
        
        // Issue 5: Empty catch block
        try {
            Integer.parseInt("not a number");
        } catch (NumberFormatException e) {
            // Empty catch - bad practice
        }
        
        // Issue 6: Magic numbers
        for (int i = 0; i < 100; i++) {
            System.out.println("Magic number used: " + 42);
        }
        
        // Issue 7: String concatenation in loop
        String result = "";
        for (int i = 0; i < 10; i++) {
            result += "item" + i;
        }
    }
    
    // Issue 8: Method name doesn't follow camelCase
    public void BAD_METHOD_NAME() {
        // Issue 9: Hardcoded string that should be constant
        System.out.println("This should be a constant");
    }
    
    // Issue 10: Missing javadoc for public method
    public String getMissingJavadoc() {
        return "no javadoc";
    }
    
    // Issue 11: Overly complex method (high cyclomatic complexity)
    public void complexMethod(int value) {
        if (value > 0) {
            if (value < 10) {
                if (value % 2 == 0) {
                    if (value > 5) {
                        if (value < 8) {
                            System.out.println("Complex logic");
                        } else {
                            System.out.println("More complex logic");
                        }
                    } else {
                        System.out.println("Even more complex");
                    }
                } else {
                    System.out.println("Odd number logic");
                }
            } else {
                System.out.println("Large number");
            }
        } else {
            System.out.println("Negative or zero");
        }
    }
    
    // Issue 12: Potential null pointer dereference
    public void nullPointerRisk(String input) {
        System.out.println(input.length()); // No null check
    }
    
    // Issue 13: Resource leak - not using try-with-resources
    public void resourceLeak() throws IOException {
        FileInputStream fis = new FileInputStream("test.txt");
        // File not closed properly
    }
}