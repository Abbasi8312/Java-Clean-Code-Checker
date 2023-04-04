package ir.ac.kntu;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    private static int lineCounter;

    private static int indentation;

    private static int originalIndentation;

    private static String line;

    private static String originalLine;

    private static String newLine;

    private static List<Integer> multipleLinesIndex = new ArrayList<>();

    private static int offset;

    public static void main(String[] args) {
        System.out.println("Enter file name: ");
        Scanner scanner = new Scanner(System.in);
        String fileName = scanner.nextLine();
        try {
            BufferedReader reader = new BufferedReader(new FileReader("src/main/java/ir/ac/kntu/" + fileName));
            line = reader.readLine();
            lineCounter = 1;
            while (line != null) {
                originalLine = line;
                modifyLine();
                newLine = line;
                if (multipleLinesIndex.size() != 0) {
                    indentation = originalIndentation;
                    lineCounter += multipleLinesIndex.size();
                    multipleLinesIndex = new ArrayList<>();
                }
                indentationCheck(line);
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
        longLine();
        if (matcherMap.get("package").find()) {
            packageTest(matcherMap.get("package"));
        } else if (matcherMap.get("for").find()) {
            forTest(matcherMap.get("for"));
        } else if (matcherMap.get("import").find()) {
            importTest(matcherMap.get("import"));
        } else if (matcherMap.get("other").find()) {
            otherTest(matcherMap.get("other"));
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
        } else if (!line.matches("^\\s*$")) {
            multipleLines(reader);
        }
        if (offset != 0) {
            line = line.substring(offset);
            if (line.charAt(0) != '}') {
                System.out.printf("%3d| Multiple statements in one line\n", getLineCount());
                cleanCodeTest(reader);
            }
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
        matcherMap.put("other", getMatcher("case\\s+\\S+\\s*:|default\\s*:|break\\s*;|continue\\s*;"));
    }

    private static Matcher getMatcher(String regex) {
        return Pattern.compile("^\\s*(?:" + regex + ")\\s*(?<offset>.*?)$").matcher(line);
    }

    private static void packageTest(Matcher packageMatcher) {
        if (lineCounter != 1) {
            System.out.printf("%3d| The \"package\" statement should be the first line of code\n", getLineCount());
        }
        getOffset(packageMatcher);
    }

    private static void importTest(Matcher importMatcher) {
        getOffset(importMatcher);
    }

    private static void classTest(Matcher classMatcher) {
        indentation += 4;
        if (!classMatcher.group("name").matches("^[A-Z][a-zA-Z0-9]*$")) {
            System.out.printf("%3d| The class name should be in UpperCamelCase\n", getLineCount());
        }
        if (classMatcher.group("brace").matches("")) {
            System.out.printf("%3d| class statement must contain \"{\" in front of it\n", getLineCount());
        }
        getOffset(classMatcher);
    }

    private static void methodTest(Matcher methodMatcher) {
        indentation += 4;
        if (!methodMatcher.group("name").matches("^[a-z][a-zA-Z0-9]*$")) {
            System.out.printf("%3d| The method name should be in lowerCamelCase\n", getLineCount());
        }
        if (methodMatcher.group("name").length() <= 1) {
            System.out.printf("%3d| The method name should have at least 2 characters\n", getLineCount());
        }
        String parameters = methodMatcher.group("parameters");
        variableDeclarationCheck(parameters, 2);
        if (methodMatcher.group("brace").matches("")) {
            System.out.printf("%3d| method declaration must contain \"{\" in front of it\n", getLineCount());
        }
        getOffset(methodMatcher);
    }

    private static void variableTest(Matcher variableMatcher) {
        variableDeclarationCheck(variableMatcher.group(), 2);
        getOffset(variableMatcher);
    }

    private static void callTest(Matcher callMatcher) {
        variableDeclarationCheck(callMatcher.group(), 2);
        getOffset(callMatcher);
    }

    private static void conditionalTest(Matcher conditionalMatcher) {
        indentation += 4;
        if (conditionalMatcher.group("type").equals("else") || conditionalMatcher.group("type").equals("else if")) {
            System.out.printf("%3d| The \"%s\" statement shouldn't be on a new line\n", getLineCount(),
                    conditionalMatcher.group("type"));
        }
        if (conditionalMatcher.group("brace").matches("")) {
            System.out.printf("%3d| %s statement must contain \"{\" in front of it\n", getLineCount(),
                    conditionalMatcher.group("type"));
        }
        getOffset(conditionalMatcher);
    }

    private static void forTest(Matcher forMatcher) {
        indentation += 4;
        variableDeclarationCheck(forMatcher.group("initialize"), 1);
        if (forMatcher.group("brace").matches("")) {
            System.out.printf("%3d| for statement must contain \"{\" in front of it\n", getLineCount());
        }
        getOffset(forMatcher);
    }

    private static void switchTest(Matcher switchMatcher, BufferedReader reader) throws IOException {
        int startingIndentation = indentation;
        indentation += 4;
        StringBuilder switchString = new StringBuilder();
        getOffset(switchMatcher);
        if (offset == 0) {
            line = reader.readLine();
            originalLine = line;
            lineCounter++;
        }
        if (switchMatcher.group("brace").matches("")) {
            System.out.printf("%3d| switch statement must contain \"{\" in front of it\n", getLineCount());
        }
        while (indentation != startingIndentation && line != null) {
            modifyLine();
            newLine = line;
            if (multipleLinesIndex.size() != 0) {
                indentation = originalIndentation;
                lineCounter += multipleLinesIndex.size();
                multipleLinesIndex = new ArrayList<>();
            }
            switchString.append(line);
            if (!line.matches("^\\s*(case\\s+\\S+\\s*:|default\\s*:).*") &&
                    (!line.matches("^\\s*}.*") || indentation != startingIndentation + 4)) {
                indentation += 4;
                indentationCheck(line);
                indentation -= 4;
            } else {
                indentationCheck(line);
            }
            cleanCodeTest(reader);
            line = reader.readLine();
            originalLine = line;
            lineCounter++;
        }
        if (!switchString.toString().matches(".*default\\s*:.*")) {
            System.out.printf("%3d| Switch block must contain default case\n", getLineCount() - 1);
        }
        if (line == null) {
            line = "";
        }
        modifyLine();
        indentationCheck(line);
        cleanCodeTest(reader);
    }

    private static void bracesTest(Matcher bracesMatcher) {
        indentation -= 4;
        if (bracesMatcher.group("type").equals("{")) {
            System.out.printf("%3d| \"{\" shouldn't be on a new line\n", getLineCount());
            indentation += 4;
        } else if (bracesMatcher.group("type").equals("}") && offset != 0) {
            System.out.printf("%3d| \"}\" should be on a new line\n", getLineCount());
        } else if (bracesMatcher.group("conditional") != null) {
            indentation += 4;
        }
        getOffset(bracesMatcher);
    }

    private static void otherTest(Matcher otherMatcher) {
        getOffset(otherMatcher);
    }

    private static void variableDeclarationCheck(String string, int charCount) {
        Matcher parameterMatcher = Pattern.compile(
                "^\\s*(?<type>int|byte|long|char|boolean|String|double|float|Scanner)(?:\\s*\\[.*])*\\s+(?<name>[^" +
                        " ,;]+)").matcher(string);
        while (parameterMatcher.find()) {
            if (!parameterMatcher.group("name").matches("^[a-z][a-zA-Z0-9]*$")) {
                System.out.printf("%3d| The variable name should be in lowerCamelCase\n", getLineCount());
            }
            if (charCount == 2 && parameterMatcher.group("name").length() <= 1) {
                System.out.printf("%3d| The variable name should have at least 2 characters\n", getLineCount());
            }
        }
    }

    private static void indentationCheck(String line) {
        int spaces = 0;
        int newIndentation = indentation;
        for (int i = 0; i < line.length() && line.charAt(i) == ' '; i++) {
            spaces++;
        }
        if (spaces < line.length() && (line.charAt(spaces) == '}' || line.charAt(spaces) == '{')) {
            newIndentation -= 4;
        }
        if (newIndentation != spaces && spaces != line.length()) {
            System.out.printf("%3d| Wrong indentation (need %d spaces)\n", getLineCount(), newIndentation);
        }
    }

    private static void spaceCheck() {
        checkOperatorSpacing(newLine);
        checkWordSpacing(newLine);
    }

    private static void checkOperatorSpacing(String line) {
        Matcher spaceMatcher = Pattern.compile(
                        "(\\S+?|^)(\\s*)(\\|\\||\\+\\+|\\+=|\\*=|\\|=|&&|==|!=|<=|>=|--|-=|/=|%=|&=|\\^=|" +
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
        Matcher spaceMatcher = Pattern.compile("([A-Za-z0-9]+)(\\s+)([A-Za-z0-9]+)").matcher(line);
        int index = 0;
        while (spaceMatcher.find(index)) {
            if (!spaceMatcher.group(2).equals(" ")) {
                System.out.printf("%3d| There should be one space between \"%s\" and \"%s\" (near character %d)\n",
                        getLineCount(), spaceMatcher.group(1), spaceMatcher.group(3), spaceMatcher.start(2) + 1);
            }
            index = spaceMatcher.start(3);
        }
    }

    private static void edgeSpacing(Matcher spaceMatcher, int leftCount, int rightCount) {
        if (leftCount != -1 && !spaceMatcher.group(2).equals(leftCount == 0 ? "" : " ")) {
            if (!spaceMatcher.group(1).matches("")) {
                System.out.printf("%3d| There should be %s space before \"%s\" (near character %d)\n", getLineCount(),
                        leftCount == 0 ? "no" : "one", spaceMatcher.group(3), spaceMatcher.end(2) + 1);
            }
        }
        if (rightCount != -1 && !spaceMatcher.group(4).equals(rightCount == 0 ? "" : " ")) {
            if (!spaceMatcher.group(5).matches("")) {
                System.out.printf("%3d| There should be %s space after \"%s\" (near character %d)\n", getLineCount(),
                        rightCount == 0 ? "no" : "one", spaceMatcher.group(3), spaceMatcher.start(4) + 1);
            }
        }
    }

    private static int getLineCount() {
        return lineCounter + multipleLinesIndex.size();
    }

    private static void getOffset(Matcher matcher) {
        if (!matcher.group("offset").matches("")) {
            offset = matcher.start("offset");
        } else {
            offset = 0;
        }
    }

    private static void multipleLines(BufferedReader reader) throws IOException {
        String tmp = reader.readLine();
        originalLine = line + " " + tmp.trim();
        modifyLine();
        if (multipleLinesIndex.size() == 0) {
            originalIndentation = indentation;
            Matcher matcher1 = Pattern.compile("\\(.*$").matcher(line);
            Matcher matcher2 = Pattern.compile("=.*$").matcher(line);
            if (matcher1.find()) {
                indentation = matcher1.start() + 1;
            } else if (matcher2.find()) {
                indentation = matcher2.start() + 2;
            } else {
                indentation += 8;
            }
        }
        multipleLinesIndex.add(line.length());
        newLine = tmp;
        line = line + " " + tmp.trim();
        indentationCheck(tmp);
        cleanCodeTest(reader);
    }

    private static void longLine() {
        if (newLine.length() > 80) {
            System.out.printf("%3d| Line shouldn't have more than 80 characters\n", getLineCount());
            Matcher matcher = Pattern.compile("([=(]).*$").matcher(newLine);
            if (matcher.find()) {
                System.out.printf("%3d| This line can break like this:\n", getLineCount());
                String tmp = newLine.trim();
                String originalTmp = originalLine.trim();
                if (matcher.group(1).equals("(")) {
                    commaBreak(matcher, tmp, originalTmp);
                } else if (matcher.group(1).equals("=")) {
                    operatorBreak(matcher, tmp, originalTmp);
                } else {
                    System.out.println("Invalid line");
                }
            }
        }
    }

    private static void operatorBreak(Matcher matcher, String tmp, String originalTmp) {
        int indent;
        indent = matcher.start(1) + 2;
        for (int i = Math.min(79 - indent, tmp.length() - 1 - indent); i >= 0; i--) {
            if (tmp.substring(i, i + 1).matches("[+\\-*/%|&^]")) {
                System.out.printf("%s\n", " ".repeat(indentation) + originalTmp.substring(0, i));
                originalTmp = originalTmp.substring(i).trim();
                tmp = tmp.substring(i).trim();
                break;
            }
        }
        while (tmp.length() + indent > 80) {
            if (tmp.substring(1, 79 - indent + 1).matches("[^+\\-*/%|&^]*")) {
                break;
            }
            for (int i = 79 - indent; i >= 0; i--) {
                if (tmp.substring(i, i + 1).matches("[+\\-*/%|&^]")) {
                    System.out.printf("%s\n", " ".repeat(indent) + originalTmp.substring(0, i));
                    originalTmp = originalTmp.substring(i).trim();
                    tmp = tmp.substring(i).trim();
                    break;
                }
            }
        }
        System.out.printf("%s\n", " ".repeat(indent) + originalTmp);
        if ((" ".repeat(indent) + originalTmp).length() > 80) {
            System.out.println("     Couldn't break the line completely");
        }
    }

    private static void commaBreak(Matcher matcher, String tmp, String originalTmp) {
        int indent;
        indent = matcher.start(1) + 1;
        for (int i = Math.min(79 - indent, tmp.length() - 1 - indent); i >= 0; i--) {
            if (tmp.charAt(i) == ',') {
                System.out.printf("%s\n", " ".repeat(indentation) + originalTmp.substring(0, i + 1));
                originalTmp = originalTmp.substring(i + 1).trim();
                tmp = tmp.substring(i + 1).trim();
                break;
            }
        }
        while (tmp.length() + indent > 80) {
            if (tmp.substring(1, 79 - indent + 1).matches("[^,]*")) {
                break;
            }
            for (int i = 79 - indent; i >= 0; i--) {
                if (tmp.charAt(i) == ',') {
                    System.out.printf("%s\n", " ".repeat(indent) + originalTmp.substring(0, i + 1));
                    originalTmp = originalTmp.substring(i + 1).trim();
                    tmp = tmp.substring(i + 1).trim();
                    break;
                }
            }
        }
        System.out.printf("%s\n", " ".repeat(indent) + tmp);
    }
}