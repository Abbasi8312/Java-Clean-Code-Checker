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

    private static String originalLine;

    private static int offset;

    public static void main(String[] args) {
        BufferedReader reader;
        String fileName = "Test";
        try {
            reader = new BufferedReader(new FileReader("src/main/java/ir/ac/kntu/" + fileName));
            line = reader.readLine();
            lineCounter = 1;
            while (line != null) {
                originalLine = line;
                modifyLine();
                indentationCheck();
                cleanCodeTest(reader);
                line = reader.readLine();
                lineCounter++;
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void cleanCodeTest(BufferedReader reader) throws IOException {
        Map<String, Matcher> matcherMap = new HashMap<>();
        fillMatcherMap(matcherMap);
        if (offset == 0) {
            spaceCheck();
        }
        if (matcherMap.get("package").find()) {
            packageTest(matcherMap.get("package"));
        } else if (matcherMap.get("for").find()) {
            forTest(matcherMap.get("for"));
        } else if (matcherMap.get("import").find()) {
            importTest(matcherMap.get("import"));
        } else if (matcherMap.get("call").find()) {
            callTest(matcherMap.get("call"));
        } else if (matcherMap.get("variable").find()) {
            variableTest(matcherMap.get("variable"));
        } else if (matcherMap.get("class").find()) {
            classTest(matcherMap.get("class"));
        } else if (matcherMap.get("method").find()) {
            methodTest(matcherMap.get("method"));
        } else if (matcherMap.get("conditional").find()) {
            conditionalTest(matcherMap.get("conditional"));
        } else if (matcherMap.get("braces").find()) {
            bracesTest(matcherMap.get("braces"));
        } else if (matcherMap.get("switch").find()) {
            switchTest(matcherMap.get("switch"), reader);
        } else {
            offset = 0;
        }
        if (offset != 0) {
            line = line.substring(offset);
            if (line.charAt(0) != '}') {
                System.out.printf("%3d| Multiple statements in one line!\n", lineCounter);
                cleanCodeTest(reader);
            }
        }
    }

    private static void fillMatcherMap(Map<String, Matcher> matcherMap) {
        matcherMap.put("package", getMatcher("package\\s+\\S+\\s*;"));
        matcherMap.put("import", getMatcher("import\\s+\\S+\\s*;"));
        matcherMap.put("class", getMatcher("public\\s+class\\s+(?<name>\\S+)\\s*(?<brace>\\{?)"));
        matcherMap.put("braces",
                getMatcher("(?<type>}\\s*(?<conditional>(?:else\\s+if\\s*\\(.*\\)|else)\\s*\\{?)?|\\{)"));
        matcherMap.put("method", getMatcher(
                "(?:public\\s|private\\s)\\s*static\\s+(?<type>\\S+)\\s+(?<name>\\S+)\\s*\\((?<parameters>.*?)\\)" +
                        "\\s*(?<brace>\\{?)"));
        matcherMap.put("conditional", getMatcher(
                "(?<type>if|else|else\\s+if|while)\\s*(?:\\((?<conditions>[^{]*)\\)\\s*)?\\s*(?<brace>\\{?)"));
        matcherMap.put("for", getMatcher(
                "for\\s*\\(\\s*(?<initialize>.*\\s*;)\\s*(?<test>.*\\s*;)\\s*(?<update>[^{]*\\s*)\\)\\s*" +
                        "(?<brace>\\{?)"));
        matcherMap.put("variable",
                getMatcher("(?:\\S+(?:\\[.*]\\s*)*\\s+)?[^\\s(]+\\s*(?:=\\s*(?<equal>[^;]+)?\\s*)?;"));
        matcherMap.put("call", getMatcher("(?<name>\\S+)\\s*\\((?<arguments>.*?)\\)\\s*;"));
        matcherMap.put("switch", getMatcher("switch\\s*(?:\\((?<conditions>.*)\\)\\s*)?\\s*(?<brace>\\{?)"));
    }

    private static Matcher getMatcher(String regex) {
        return Pattern.compile("^\\s*" + regex + "\\s*(?<offset>.*?)$").matcher(line);
    }

    private static void methodTest(Matcher methodMatcher) {
        indentation += 4;
        if (!methodMatcher.group("name").matches("^[a-z][a-zA-Z0-9]+$")) {
            System.out.printf("%3d| The method name should be in lowerCamelCase and must have at least 2 characters.\n",
                    lineCounter);
        }
        String parameters = methodMatcher.group("parameters");
        variableDeclarationCheck(parameters, 2);
        if (methodMatcher.group("brace").matches("")) {
            System.out.printf("%3d| method declaration must contain \"{\" in front of it\n", lineCounter);
        }
        getOffset(methodMatcher);
    }

    private static void variableDeclarationCheck(String string, int charCount) {
        Matcher parameterMatcher = Pattern.compile(
                "^\\s*(?<type>int|byte|long|char|boolean|String|double|float|Scanner)(?:\\s*\\[.*])*\\s+(?<name>[^" +
                        " ,;]+)").matcher(string);
        while (parameterMatcher.find()) {
            if (!parameterMatcher.group("name").matches("^[a-z][a-zA-Z0-9]*$")) {
                System.out.printf("%3d| The variable name should be in lowerCamelCase\n", lineCounter);
            }
            if (charCount == 2 && parameterMatcher.group("name").length() <= 1) {
                System.out.printf("%3d| The variable name should have at least 2 characters\n", lineCounter);
            }
        }
    }

    private static void conditionalTest(Matcher conditionalMatcher) {
        indentation += 4;
        if (conditionalMatcher.group("type").equals("else") || conditionalMatcher.group("type").equals("else if")) {
            System.out.printf("%3d| The \"%s\" statement shouldn't be on a new line\n", lineCounter,
                    conditionalMatcher.group("type"));
        }
        if (conditionalMatcher.group("brace").matches("")) {
            System.out.printf("%3d| %s statement must contain \"{\" in front of it\n", lineCounter,
                    conditionalMatcher.group("type"));
        }
        getOffset(conditionalMatcher);
    }

    private static void forTest(Matcher forMatcher) {
        indentation += 4;
        variableDeclarationCheck(forMatcher.group("initialize"), 1);
        if (forMatcher.group("brace").matches("")) {
            System.out.printf("%3d| for statement must contain \"{\" in front of it\n", lineCounter);
        }
        getOffset(forMatcher);
    }

    private static void switchTest(Matcher switchMatcher, BufferedReader reader) throws IOException {
        int startingIndentation = indentation;
        indentation += 4;
        boolean hasDefault = false;
        getOffset(switchMatcher);
        if (offset == 0) {
            line = reader.readLine();
            lineCounter++;
        }
        if (switchMatcher.group("brace").matches("")) {
            System.out.printf("%3d| switch statement must contain \"{\" in front of it\n", lineCounter);
        }
        while (indentation != startingIndentation && line != null) {
            modifyLine();
            if (line.matches("^\\s*default\\s*:.*")) {
                hasDefault = true;
            }
            if (!line.matches("^\\s*(case\\s+\\S+\\s*:|}|default\\s*:).*")) {
                indentation += 4;
                indentationCheck();
                indentation -= 4;
            } else {
                indentationCheck();
                line = line.replaceAll("case//s+\\S+\\s*:", "");
            }
            cleanCodeTest(reader);
            line = reader.readLine();
            lineCounter++;
        }
        if (!hasDefault) {
            System.out.printf("%3d| Switch block must contain default case\n", lineCounter);
        }
        if (line == null) {
            line = "";
        }
        modifyLine();
        indentationCheck();
        cleanCodeTest(reader);
    }

    private static void bracesTest(Matcher bracesMatcher) {
        indentation -= 4;
        if (bracesMatcher.group("type").equals("{")) {
            System.out.printf("%3d| \"{\" shouldn't be on a new line\n", lineCounter);
            indentation += 4;
        } else if (bracesMatcher.group("type").equals("}") && offset != 0) {
            System.out.printf("%3d| \"}\" should be on a new line\n", lineCounter);
        } else if (bracesMatcher.group("conditional") != null) {
            indentation += 4;
        }
        getOffset(bracesMatcher);
    }

    private static void classTest(Matcher classMatcher) {
        indentation += 4;
        if (!classMatcher.group("name").matches("^[A-Z][a-zA-Z0-9]*$")) {
            System.out.printf("%3d| The class name should be in UpperCamelCase.\n", lineCounter);
        }
        if (classMatcher.group("brace").matches("")) {
            System.out.printf("%3d| class statement must contain \"{\" in front of it\n", lineCounter);
        }
        getOffset(classMatcher);
    }

    private static void importTest(Matcher importMatcher) {
        getOffset(importMatcher);
    }

    private static void variableTest(Matcher variableMatcher) {
        variableDeclarationCheck(variableMatcher.group(), 2);
        getOffset(variableMatcher);
    }

    private static void callTest(Matcher callMatcher) {
        variableDeclarationCheck(callMatcher.group(), 2);
        getOffset(callMatcher);
    }

    private static void packageTest(Matcher packageMatcher) {
        if (lineCounter != 1) {
            System.out.printf("%3d| The \"package\" statement should be the first line of code!\n", lineCounter);
        }
        getOffset(packageMatcher);
    }

    private static void indentationCheck() {
        int spaces = 0;
        for (int i = 0; i < line.length() && line.charAt(i) == ' '; i++) {
            spaces++;
        }
        if (spaces < line.length() && (line.charAt(spaces) == '}' || line.charAt(spaces) == '{')) {
            spaces += 4;
        }
        if (indentation != spaces && spaces != line.length()) {
            System.out.printf("%3d| Wrong indentation!\n", lineCounter);
        }
    }

    private static void modifyLine() {
        line = line.replaceAll("^[^\"]*//.*", "");
        Matcher matcher = Pattern.compile("\"(?:\\\\.|[^\"])*[^\\\\]\"|'(?:\\\\.|[^'])*[^\\\\]'").matcher(line);
        StringBuilder newLine = new StringBuilder(line);
        int start = 0;
        while (matcher.find(start)) {
            newLine.replace(matcher.start() + 1, matcher.end() - 1, ".".repeat(matcher.group().length() - 2));
            start = matcher.end();
        }
        line = String.valueOf(newLine);
        Matcher commentMatcher = Pattern.compile("//.*").matcher(line);
        if (commentMatcher.find()) {
            line = line.substring(0, commentMatcher.start());
        }
    }

    private static void checkOperatorSpacing(String line) {
        Matcher spaceMatcher = Pattern.compile(
                        "(\\S+?)(\\s*)(\\|\\||\\+\\+|\\+=|\\*=|\\|=|&&|==|!=|<=|>=|--|-=|/=|%=|&=|\\^=|" +
                                "(?<![&^|!<>=+*/%\\-])[\\[\\]\\-;:(){,=+*/<>^%&|])(\\s*)([^ \\[\\]\\-:(){," +
                                "=+*/<>^%&|]*)")
                .matcher(line);
        int index = 0;
        while (spaceMatcher.find(index)) {
            checkOperatorSpacing(spaceMatcher);
            index = spaceMatcher.end(2);
        }
    }

    private static void checkOperatorSpacing(Matcher spaceMatcher) {
        switch (spaceMatcher.group(3)) {
            case "++", "--" -> {
                if (spaceMatcher.group(5).matches("[a-zA-Z0-9]+")) {
                    edgeSpacing(spaceMatcher, -1, 0);
                } else {
                    edgeSpacing(spaceMatcher, 0, -1);
                }
            }
            case ":", ";" -> edgeSpacing(spaceMatcher, 0, -1);
            case "[" -> edgeSpacing(spaceMatcher, 0, 0);
            case "]" -> {
                if (spaceMatcher.group(5).matches("[a-zA-Z0-9]+")) {
                    edgeSpacing(spaceMatcher, 0, 1);
                } else {
                    edgeSpacing(spaceMatcher, 0, 0);
                }
            }
            case "(" -> {
                if (spaceMatcher.group(1).matches("(if)|(else)|(for)|(while)|(switch)")) {
                    edgeSpacing(spaceMatcher, 1, 0);
                } else if (spaceMatcher.group(1).matches(".*[a-zA-Z0-9]")) {
                    edgeSpacing(spaceMatcher, 0, 0);
                } else if (!spaceMatcher.group(1).matches(".*[+=\\-*/%^&|!<>),]")) {
                    edgeSpacing(spaceMatcher, 1, 0);
                } else {
                    edgeSpacing(spaceMatcher, -1, 0);
                }
            }
            case ")" -> {
                if (spaceMatcher.group(5).matches("[A-Za-z0-9].*")) {
                    edgeSpacing(spaceMatcher, 0, 1);
                } else if (!spaceMatcher.group(1).matches(".*\\(")) {
                    edgeSpacing(spaceMatcher, 0, -1);
                }
            }
            case "{" -> {
                if (!spaceMatcher.group(1).equals("")) {
                    edgeSpacing(spaceMatcher, 1, -1);
                }
            }
            case "," -> edgeSpacing(spaceMatcher, 0, 1);
            default -> edgeSpacing(spaceMatcher, 1, 1);
        }
    }

    private static void checkWordSpacing(String line) {
        Matcher spaceMatcher = Pattern.compile("([A-Za-z0-9]+)( +)([A-Za-z0-9]+)").matcher(line);
        int index = 0;
        while (spaceMatcher.find(index)) {
            if (!spaceMatcher.group(2).equals(" ")) {
                System.out.printf("%3d| There should be one space between \"%s\" and \"%s\"\n", lineCounter,
                        spaceMatcher.group(1), spaceMatcher.group(3));
            }
            index = spaceMatcher.start(3);
        }
    }

    private static void spaceCheck() {
        checkOperatorSpacing(line);
        checkWordSpacing(line);
    }

    private static void edgeSpacing(Matcher spaceMatcher, int leftCount, int rightCount) {
        if (leftCount != -1 && !spaceMatcher.group(2).equals(leftCount == 0 ? "" : " ")) {
            System.out.printf("%3d| There should be %s space before \"%s\"\n", lineCounter,
                    leftCount == 0 ? "no" : "one", spaceMatcher.group(3));
        }
        if (rightCount != -1 && !spaceMatcher.group(4).equals(rightCount == 0 ? "" : " ")) {
            System.out.printf("%3d| There should be %s space after \"%s\"\n", lineCounter,
                    rightCount == 0 ? "no" : "one", spaceMatcher.group(3));
        }
    }

    private static void getOffset(Matcher matcher) {
        if (!matcher.group("offset").matches("")) {
            offset = matcher.start("offset");
        } else {
            offset = 0;
        }
    }
}
//TODO multiple lines + other statements
//Todo exact location of error