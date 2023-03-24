package ir.ac.kntu;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    private static int lineCounter;

    private static int indentation;

    private static String line;

    public static void main(String[] args) {
        BufferedReader reader;
        String fileName = "Test";
        try {
            reader = new BufferedReader(new FileReader("src/main/java/ir/ac/kntu/" + fileName));
            cleanCodeTest(reader);
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void cleanCodeTest(BufferedReader reader) throws IOException {
        line = reader.readLine();
        lineCounter = 1;
        int offset = 0;
        while (line != null) {
            Map<String, Matcher> matcherMap = new HashMap<>();
            fillMatcherMap(matcherMap);
            if (offset != 0) {
                System.out.println(lineCounter + ": Multiple statements in one line!");
            }
            if (matcherMap.get("package").find(offset)) {
                packageTest(matcherMap.get("package"));
                offset = getOffset(matcherMap.get("package"));
            } else if (matcherMap.get("import").find(offset)) {
                importTest(matcherMap.get("import"));
                offset = getOffset(matcherMap.get("import"));
            } else if (matcherMap.get("class").find(offset)) {
                classTest(matcherMap.get("class"));
                offset = getOffset(matcherMap.get("class"));
            } else if (matcherMap.get("method").find(offset)) {
                methodTest(matcherMap.get("method"));
                offset = getOffset(matcherMap.get("method"));
            } else if (matcherMap.get("braces").find(offset)) {
                bracesTest(matcherMap.get("braces"));
                offset = getOffset(matcherMap.get("braces"));
            } else {
                offset = 0;
            }
            if (offset == 0) {
                lineCounter++;
                line = reader.readLine();
            }
        }
    }

    private static void fillMatcherMap(Map<String, Matcher> matcherMap) {
        matcherMap.put("package", getMatcher("package +\\S+ *;"));
        matcherMap.put("import", getMatcher("import +\\S+ *;"));
        matcherMap.put("class", getMatcher("public +class +(?<name>\\S+) *\\{"));
        matcherMap.put("braces", getMatcher("}"));
        matcherMap.put("method",
                getMatcher("public +static +(?<type>\\S+) +(?<name>\\S+) *\\((?<parameters>.*?)\\) *\\{"));
    }

    private static Matcher getMatcher(String regex) {
        return Pattern.compile("(?<indentation> *)" + regex + " *(?<offset>.*?)(?://.*)?$").matcher(line);
    }

    private static void methodTest(Matcher methodMatcher) {
        indentationCheck(methodMatcher);
        indentation += 4;
        if (!methodMatcher.group("name").matches("^[a-z][a-zA-Z0-9]+$")) {
            System.out.println(
                    lineCounter + ": The method name should be in lowerCamelCase and must have at least 2 characters.");
        }
        String parameters = methodMatcher.group("parameters");
        Matcher parameterMatcher = Pattern.compile("\\s*(?<type>\\S+)\\s+(?<name>[^ ,]+)").matcher(parameters);
        while (parameterMatcher.find()) {
            if (!parameterMatcher.group("name").matches("^[a-z][a-zA-Z0-9]+$")) {
                System.out.println(lineCounter +
                        ": The variable name should be in lowerCamelCase and must have at least 2 characters.");
            }
        }
    }

    private static void bracesTest(Matcher bracesMatcher) {
        indentation -= 4;
        indentationCheck(bracesMatcher);
    }

    private static void classTest(Matcher classMatcher) {
        indentationCheck(classMatcher);
        indentation += 4;
        if (!classMatcher.group("name").matches("^[A-Z][a-zA-Z0-9]*$")) {
            System.out.println(lineCounter + ": The class name should be in UpperCamelCase.");
        }
    }

    private static void importTest(Matcher importMatcher) {
        indentationCheck(importMatcher);
    }

    private static void packageTest(Matcher packageMatcher) {
        indentationCheck(packageMatcher);
        if (lineCounter != 1) {
            System.out.println(lineCounter + ": The \"package\" statement should be the first line of code!");
        }
    }

    private static void indentationCheck(Matcher matcher) {
        if (matcher.group("indentation").length() != indentation) {
            System.out.println(lineCounter + ": Wrong indentation!");
        }
    }

    private static int getOffset(Matcher matcher) {
        if (!matcher.group("offset").matches("")) {
            return matcher.start("offset");
        }
        return 0;
    }
}