package ir.ac.kntu;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

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
        String line = reader.readLine();
        int lineCounter = 1;
        int indentation = 0;
        int offset = 0;
        while (line != null) {
            Matcher packageMatcher = getMatcher("package( +)\\S+( *);", line);
            Matcher importMatcher = getMatcher("import( +)\\S+( *);", line);
            Matcher classMatcher = getMatcher("public( +)class( +)(\\S+)( *)\\{", line);
            Matcher bracesMatcher = getMatcher("}", line);
            if (offset != 0) {
                System.out.println(lineCounter + ": Multiple statements in one line!");
            }
            if (packageMatcher.find(offset)) {
                packageTest(lineCounter, indentation, packageMatcher);
                offset = getOffset(packageMatcher);
            } else if (importMatcher.find(offset)) {
                importTest(lineCounter, indentation, importMatcher);
                offset = getOffset(importMatcher);
            } else if (classMatcher.find(offset)) {
                indentation = classTest(lineCounter, indentation, classMatcher);
                offset = getOffset(classMatcher);
            } else if (bracesMatcher.find(offset)) {
                indentation = bracesTest(lineCounter, indentation, bracesMatcher);
                offset = getOffset(bracesMatcher);
            } else {
                offset = 0;
            }
            if (offset == 0) {
                lineCounter++;
                line = reader.readLine();
            }
        }
    }

    private static int bracesTest(int lineCounter, int indentation, Matcher braceMatcher) {
        indentation--;
        indentationCheck(lineCounter, indentation, braceMatcher);
        return indentation;
    }

    private static int classTest(int lineCounter, int indentation, Matcher classMatcher) {
        indentationCheck(lineCounter, indentation, classMatcher);
        indentation++;
        if (!classMatcher.group(2).equals(" ") || !classMatcher.group(3).equals(" ") ||
                !classMatcher.group(5).equals(" ")) {
            System.out.println(lineCounter + ": Wrong space count!");
        }
        if (!classMatcher.group(4).matches("^[A-Z][a-zA-Z]*$")) {
            System.out.println(lineCounter + ": Class name should be UpperCamelCase!");
        }
        return indentation;
    }

    private static void importTest(int lineCounter, int indentation, Matcher importMatcher) {
        indentationCheck(lineCounter, indentation, importMatcher);
        if (!importMatcher.group(2).equals(" ") || !importMatcher.group(3).equals("")) {
            System.out.println(lineCounter + ": Wrong space count!");
        }
    }

    private static void packageTest(int lineCounter, int indentation, Matcher packageMatcher) {
        indentationCheck(lineCounter, indentation, packageMatcher);
        if (lineCounter != 1) {
            System.out.println(lineCounter + ": The \"package\" statement should be the first line of code!");
        }
        if (!packageMatcher.group(2).equals(" ") || !packageMatcher.group(3).equals("")) {
            System.out.println(lineCounter + ": Wrong space count!");
        }
    }

    private static Matcher getMatcher(String regex, String line) {
        return Pattern.compile("(?<indentation> *)" + regex + " *(?<offset>.*?)(?://.*)?$").matcher(line);
    }

    private static void indentationCheck(int lineCounter, int indentation, Matcher matcher) {
        if (matcher.group("indentation").length() != indentation * 4) {
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