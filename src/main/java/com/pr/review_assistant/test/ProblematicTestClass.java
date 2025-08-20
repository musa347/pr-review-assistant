package com.pr.review_assistant.test;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap; // Unused import - will trigger UnusedImports
import java.util.*;        // Star import - will trigger AvoidStarImport

public class ProblematicTestClass {
    
    // Public field - will trigger VisibilityModifier
    public String publicField = "bad practice";
    
    // Constant not following naming convention - will trigger ConstantName
    private static final String badConstantName = "should be UPPER_CASE";
    
    // Method name not following convention - will trigger MethodName
    public void BadMethodName() {  // Should be camelCase
        // Magic numbers - will trigger MagicNumber
        int result = calculateSomething(42, 100, 3.14);
        
        // Variable name issues - will trigger LocalVariableName
        String bad_variable_name = "should be camelCase";
        String x = "not descriptive";
        
        // Missing braces - will trigger NeedBraces
        if (result > 50)
            System.out.println("No braces here");
        
        // Empty statement - will trigger EmptyStatement
        ;
        
        // Whitespace issues - will trigger WhitespaceAfter, WhitespaceAround
        if(result>0){
            for(int i=0;i<10;i++){
                System.out.println("Bad spacing");
            }
        }
        
        // Hidden field - will trigger HiddenField
        String publicField = "hiding the instance field";
        
        // Multiple variable declarations - will trigger MultipleVariableDeclarations
        int a, b, c;
        
        // Complex boolean expression - will trigger SimplifyBooleanExpression
        boolean complexCondition = (result > 0) == true;
        
        // Complex boolean return - will trigger SimplifyBooleanReturn
        if (result > 100) {
            return;
        } else {
            return;
        }
    }
    
    // Method with too many parameters - will trigger ParameterNumber
    public void methodWithTooManyParameters(String param1, String param2, String param3, 
                                          String param4, String param5, String param6, 
                                          String param7, String param8) {
        // Method body
    }
    
    // Very long method - will trigger MethodLength
    public void veryLongMethod() {
        System.out.println("Line 1");
        System.out.println("Line 2");
        System.out.println("Line 3");
        System.out.println("Line 4");
        System.out.println("Line 5");
        System.out.println("Line 6");
        System.out.println("Line 7");
        System.out.println("Line 8");
        System.out.println("Line 9");
        System.out.println("Line 10");
        System.out.println("Line 11");
        System.out.println("Line 12");
        System.out.println("Line 13");
        System.out.println("Line 14");
        System.out.println("Line 15");
        System.out.println("Line 16");
        System.out.println("Line 17");
        System.out.println("Line 18");
        System.out.println("Line 19");
        System.out.println("Line 20");
        System.out.println("Line 21");
        System.out.println("Line 22");
        System.out.println("Line 23");
        System.out.println("Line 24");
        System.out.println("Line 25");
        System.out.println("Line 26");
        System.out.println("Line 27");
        System.out.println("Line 28");
        System.out.println("Line 29");
        System.out.println("Line 30");
        System.out.println("Line 31");
        System.out.println("Line 32");
        System.out.println("Line 33");
        System.out.println("Line 34");
        System.out.println("Line 35");
        System.out.println("Line 36");
        System.out.println("Line 37");
        System.out.println("Line 38");
        System.out.println("Line 39");
        System.out.println("Line 40");
        System.out.println("Line 41");
        System.out.println("Line 42");
        System.out.println("Line 43");
        System.out.println("Line 44");
        System.out.println("Line 45");
        System.out.println("Line 46");
        System.out.println("Line 47");
        System.out.println("Line 48");
        System.out.println("Line 49");
        System.out.println("Line 50");
        System.out.println("Line 51");
        System.out.println("Line 52");
        System.out.println("Line 53");
        System.out.println("Line 54");
        System.out.println("Line 55");
        System.out.println("Line 56");
        System.out.println("Line 57");
        System.out.println("Line 58");
        System.out.println("Line 59");
        System.out.println("Line 60");
    }
    
    // Line too long - will trigger LineLength
    public void methodWithVeryLongLinesThatExceedTheMaximumLineLengthConfigurationAndShouldBeReportedByCheckstyleAsAnIssue() {
        String veryLongStringThatExceedsTheMaximumLineLengthAndShouldBeBrokenIntoMultipleLinesForBetterReadability = "This is a very long string";
    }
    
    // Missing switch default - will trigger MissingSwitchDefault
    public void switchWithoutDefault(int value) {
        switch (value) {
            case 1:
                System.out.println("One");
                break;
            case 2:
                System.out.println("Two");
                break;
            // Missing default case
        }
    }
    
    // TODO comment - will trigger TodoComment
    public void methodWithTodo() {
        // TODO: Implement this method properly
        System.out.println("Not implemented yet");
    }
    
    // Array style issue - will trigger ArrayTypeStyle
    public void arrayStyleIssue() {
        String args[] = {"bad", "style"}; // Should be String[] args
    }
    
    // Equals without hashCode - will trigger EqualsHashCode
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ProblematicTestClass that = (ProblematicTestClass) obj;
        return publicField.equals(that.publicField);
        // Missing hashCode() method
    }
    
    // Utility class without private constructor - will trigger HideUtilityClassConstructor
    public static class UtilityClass {
        public static void utilityMethod() {
            System.out.println("Utility method");
        }
        // Should have private constructor
    }
    
    private int calculateSomething(int a, double b, double c) {
        return (int) (a + b + c);
    }
}