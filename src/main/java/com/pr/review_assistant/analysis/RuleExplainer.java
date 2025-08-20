package com.pr.review_assistant.analysis;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class RuleExplainer {
    
    private final Map<String, RuleInfo> ruleExplanations;
    
    public RuleExplainer() {
        this.ruleExplanations = initializeRuleExplanations();
    }
    
    public Finding enhanceFinding(Finding finding) {
        RuleInfo ruleInfo = ruleExplanations.get(finding.getRule());
        
        if (ruleInfo != null) {
            finding.setExplanation(ruleInfo.explanation);
            finding.setSuggestion(ruleInfo.suggestion);
            finding.setCategory(ruleInfo.category);
            finding.setImpact(ruleInfo.impact);
        } else {
            // Default explanation for unknown rules
            finding.setExplanation("This code pattern violates coding standards.");
            finding.setSuggestion("Please review and fix according to the message: " + finding.getMessage());
            finding.setCategory("Code Style");
            finding.setImpact("Low");
        }
        
        return finding;
    }
    
    private Map<String, RuleInfo> initializeRuleExplanations() {
        Map<String, RuleInfo> rules = new HashMap<>();
        
        // Naming Convention Rules
        rules.put("ConstantName", new RuleInfo(
            "Code Style",
            "Medium",
            "Constants should follow UPPER_SNAKE_CASE naming convention for better readability and consistency.",
            "Rename the constant to use UPPER_SNAKE_CASE format (e.g., 'MY_CONSTANT' instead of 'myConstant')."
        ));
        
        rules.put("MethodName", new RuleInfo(
            "Code Style",
            "Medium",
            "Method names should follow camelCase convention starting with lowercase letter.",
            "Rename the method to use camelCase format (e.g., 'getUserName' instead of 'GetUserName')."
        ));
        
        rules.put("VariableName", new RuleInfo(
            "Code Style",
            "Medium",
            "Variable names should follow camelCase convention and be descriptive.",
            "Use camelCase naming and choose more descriptive variable names."
        ));
        
        // Import Rules
        rules.put("UnusedImports", new RuleInfo(
            "Code Cleanliness",
            "Low",
            "Unused imports clutter the code and can slow down compilation.",
            "Remove the unused import statement to keep your code clean."
        ));
        
        rules.put("RedundantImport", new RuleInfo(
            "Code Cleanliness",
            "Low",
            "Redundant imports are unnecessary and make the code harder to read.",
            "Remove the redundant import statement."
        ));
        
        rules.put("AvoidStarImport", new RuleInfo(
            "Code Quality",
            "Medium",
            "Star imports (import package.*) make it unclear which classes are being used and can cause naming conflicts.",
            "Replace star import with specific class imports (e.g., 'import java.util.List' instead of 'import java.util.*')."
        ));
        
        // Whitespace Rules
        rules.put("WhitespaceAfter", new RuleInfo(
            "Code Style",
            "Low",
            "Consistent whitespace after tokens improves code readability.",
            "Add a space after the specified token (e.g., 'if (' instead of 'if(')."
        ));
        
        rules.put("WhitespaceAround", new RuleInfo(
            "Code Style",
            "Low",
            "Consistent whitespace around operators and keywords improves readability.",
            "Add spaces around the operator or keyword as needed."
        ));
        
        rules.put("NoWhitespaceBefore", new RuleInfo(
            "Code Style",
            "Low",
            "Unnecessary whitespace before certain tokens reduces code readability.",
            "Remove the space before the specified token."
        ));
        
        // Block Rules
        rules.put("NeedBraces", new RuleInfo(
            "Code Safety",
            "High",
            "Missing braces around single statements can lead to bugs when code is modified later.",
            "Add braces around the statement block, even for single statements (e.g., 'if (condition) { statement; }')."
        ));
        
        rules.put("LeftCurly", new RuleInfo(
            "Code Style",
            "Low",
            "Consistent brace placement improves code readability and follows Java conventions.",
            "Place the opening brace at the end of the line, not on a new line."
        ));
        
        rules.put("RightCurly", new RuleInfo(
            "Code Style",
            "Low",
            "Consistent closing brace placement improves code readability.",
            "Follow the standard brace placement convention for closing braces."
        ));
        
        // Coding Rules
        rules.put("MagicNumber", new RuleInfo(
            "Code Quality",
            "High",
            "Magic numbers make code hard to understand and maintain. They should be replaced with named constants.",
            "Extract the number into a well-named constant (e.g., 'private static final int MAX_RETRIES = 3;')."
        ));
        
        rules.put("EmptyStatement", new RuleInfo(
            "Code Quality",
            "Medium",
            "Empty statements are usually mistakes and can indicate incomplete code or bugs.",
            "Remove the empty statement or add the intended code."
        ));
        
        rules.put("SimplifyBooleanExpression", new RuleInfo(
            "Code Quality",
            "Medium",
            "Complex boolean expressions can be simplified for better readability and maintainability.",
            "Simplify the boolean expression (e.g., 'return condition' instead of 'return condition == true')."
        ));
        
        rules.put("SimplifyBooleanReturn", new RuleInfo(
            "Code Quality",
            "Medium",
            "Boolean return statements can often be simplified by returning the condition directly.",
            "Return the boolean condition directly instead of using if-else (e.g., 'return x > 0' instead of 'if (x > 0) return true; else return false;')."
        ));
        
        rules.put("EqualsHashCode", new RuleInfo(
            "Code Safety",
            "High",
            "Classes that override equals() must also override hashCode() to maintain the contract between these methods.",
            "Override hashCode() method when you override equals(), or use @EqualsAndHashCode from Lombok."
        ));
        
        // Design Rules
        rules.put("HiddenField", new RuleInfo(
            "Code Quality",
            "Medium",
            "Local variables or parameters that hide instance fields can cause confusion and bugs.",
            "Rename the local variable/parameter or use 'this.' to reference the field explicitly."
        ));
        
        rules.put("VisibilityModifier", new RuleInfo(
            "Code Safety",
            "High",
            "Public fields break encapsulation and make the class harder to maintain and test.",
            "Make the field private and provide getter/setter methods, or use Lombok annotations like @Getter/@Setter."
        ));
        
        rules.put("FinalParameters", new RuleInfo(
            "Code Quality",
            "Low",
            "Making parameters final prevents accidental reassignment and makes code more robust.",
            "Add 'final' keyword to method parameters (e.g., 'public void method(final String param)')."
        ));
        
        // Size Rules
        rules.put("LineLength", new RuleInfo(
            "Code Style",
            "Low",
            "Long lines are harder to read and may not fit on smaller screens or in side-by-side diffs.",
            "Break the line into multiple lines, extract variables, or refactor the code to be more concise."
        ));
        
        rules.put("MethodLength", new RuleInfo(
            "Code Quality",
            "High",
            "Long methods are harder to understand, test, and maintain. They often violate the Single Responsibility Principle.",
            "Break the method into smaller, more focused methods with descriptive names."
        ));
        
        rules.put("ParameterNumber", new RuleInfo(
            "Code Quality",
            "High",
            "Methods with too many parameters are hard to use and understand. They often indicate design problems.",
            "Reduce the number of parameters by grouping related parameters into objects or using the Builder pattern."
        ));
        
        // Miscellaneous Rules
        rules.put("TodoComment", new RuleInfo(
            "Code Maintenance",
            "Low",
            "TODO comments indicate incomplete work that should be addressed before production.",
            "Complete the TODO task or create a proper issue/ticket to track the work."
        ));
        
        rules.put("ArrayTypeStyle", new RuleInfo(
            "Code Style",
            "Low",
            "Java style array declarations are preferred over C-style for consistency.",
            "Use Java-style array declaration (e.g., 'String[] args' instead of 'String args[]')."
        ));
        
        return rules;
    }
    
    private static class RuleInfo {
        final String category;
        final String impact;
        final String explanation;
        final String suggestion;
        
        RuleInfo(String category, String impact, String explanation, String suggestion) {
            this.category = category;
            this.impact = impact;
            this.explanation = explanation;
            this.suggestion = suggestion;
        }
    }
}