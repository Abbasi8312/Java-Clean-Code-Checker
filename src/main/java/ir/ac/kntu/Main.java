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
                System.out.printf("%3d| Multiple statements in one line!\n", lineCounter);
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
                spaceCheck();
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
            System.out.printf("%3d| The method name should be in lowerCamelCase and must have at least 2 characters.\n",
                    lineCounter);
        }
        String parameters = methodMatcher.group("parameters");
        Matcher parameterMatcher = Pattern.compile("\\s*(?<type>\\S+)\\s+(?<name>[^ ,]+)").matcher(parameters);
        while (parameterMatcher.find()) {
            if (!parameterMatcher.group("name").matches("^[a-z][a-zA-Z0-9]+$")) {
                System.out.printf(
                        "%3d| The variable name should be in lowerCamelCase and must have at least 2 characters.\n",
                        lineCounter);
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
            System.out.printf("%3d| The class name should be in UpperCamelCase.\n", lineCounter);
        }
    }

    private static void importTest(Matcher importMatcher) {
        indentationCheck(importMatcher);
    }

    private static void packageTest(Matcher packageMatcher) {
        indentationCheck(packageMatcher);
        if (lineCounter != 1) {
            System.out.printf("%3d| The \"package\" statement should be the first line of code!\n", lineCounter);
        }
    }

    private static void indentationCheck(Matcher matcher) {
        if (matcher.group("indentation").length() != indentation) {
            System.out.printf("%3d| Wrong indentation!\n", lineCounter);
        }
    }

    private static String removeComments(String line) {
        return line.replaceAll("//.*", "").trim();
    }

    private static String removeStringLiterals(String line) {
        return line.replaceAll("\"(?:\\\\.|[^\"])*[^\\\\]\"", "\"\"").trim();
    }

    private static void checkOperatorSpacing(String line) {
        Matcher spaceMatcher = Pattern.compile("(\\S*?)(\\s*)" +
                "(\\|\\||\\+\\+|\\+=|\\*=|\\|=|&&|==|!=|<=|>=|--|-=|/=|%=|&=|^=|[\\[\\]\\-;:(){,=+*/<>^%&|])" +
                "(\\s*)([^ \\[\\]\\-:(){,=+*/<>^%&|]*)").matcher(line);
        int index = 0;
        while (spaceMatcher.find(index)) {
            checkOperatorSpacing(line, spaceMatcher);
            index = spaceMatcher.end(3);
        }
    }

    private static void checkOperatorSpacing(String line, Matcher spaceMatcher) {
        switch (spaceMatcher.group(3)) {
            case "++", "--" -> {
                if (spaceMatcher.group(5).matches("[a-zA-Z0-9]+")) {
                    edgeSpace(spaceMatcher, -1, 0);
                } else {
                    edgeSpace(spaceMatcher, 0, -1);
                }
            }
            case ":", ";" -> edgeSpace(spaceMatcher, 0, -1);
            case "[" -> edgeSpace(spaceMatcher, 0, 0);
            case "]" -> {
                if (spaceMatcher.group(5).matches("[a-zA-Z0-9]+")) {
                    edgeSpace(spaceMatcher, 0, 1);
                } else {
                    edgeSpace(spaceMatcher, 0, 0);
                }
            }
            case "(" -> {
                if (spaceMatcher.group(1).matches("(if)|(else)|(for)|(while)|(switch)")) {
                    edgeSpace(spaceMatcher, 1, 0);
                } else if (spaceMatcher.group(1).matches("[a-zA-Z0-9.]+")) {
                    edgeSpace(spaceMatcher, 0, 0);
                } else {
                    edgeSpace(spaceMatcher, 1, 0);
                }
            }
            case ")" -> {
                if (spaceMatcher.group(5).matches("[A-Za-z0-9]+")) {
                    edgeSpace(spaceMatcher, 0, 1);
                } else {
                    edgeSpace(spaceMatcher, 0, -1);
                }
            }
            case "{" -> {
                if (line.charAt(spaceMatcher.start(2) - 1) != ']') {
                    edgeSpace(spaceMatcher, 1, -1);
                }
            }
            case "," -> edgeSpace(spaceMatcher, 0, 1);
            default -> edgeSpace(spaceMatcher, 1, 1);
        }
    }

    private static void checkWordSpacing(String line) {
        Matcher spaceMatcher = Pattern.compile("([A-Za-z0-9]+)( +)([A-Za-z0-9]+)").matcher(line);
        int index = 0;
        while (spaceMatcher.find(index)) {
            if (!spaceMatcher.group(2).equals(" ")) {
                System.out.printf("%3d| There should be one space between \"%s\" and \"%s\"", lineCounter,
                        spaceMatcher.group(1), spaceMatcher.group(3));
            }
            index = spaceMatcher.start(3);
        }
    }

    public static void spaceCheck() {
        String newLine = removeComments(line);
        newLine = removeStringLiterals(newLine);
        checkOperatorSpacing(newLine);
        checkWordSpacing(newLine);
    }

    private static void edgeSpace(Matcher spaceMatcher, int leftCount, int rightCount) {
        if (leftCount != -1 && !spaceMatcher.group(2).equals(leftCount == 0 ? "" : " ")) {
            System.out.printf("%3d| There should be %s space before \"%s\"\n", lineCounter,
                    leftCount == 0 ? "no" : "one", spaceMatcher.group(3));
        }
        if (rightCount != -1 && !spaceMatcher.group(4).equals(rightCount == 0 ? "" : " ")) {
            System.out.printf("%3d| There should be %s space after \"%s\"\n", lineCounter,
                    rightCount == 0 ? "no" : "one", spaceMatcher.group(3));
        }
    }


    private static int getOffset(Matcher matcher) {
        if (!matcher.group("offset").matches("")) {
            return matcher.start("offset");
        }
        return 0;
    }
}