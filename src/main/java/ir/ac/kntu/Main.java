package ir.ac.kntu;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
//import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

    public static void main(String[] args) {
        //Scanner scanner = new Scanner(System.in);
        //String fileName = scanner.nextLine();
        BufferedReader reader;
        String fileName = "Test";
        int lineCounter = 1;
        int indentation = 0;
        int offset = 0;
        try {
            reader = new BufferedReader(new FileReader("src/main/java/ir/ac/kntu/" + fileName));
            String line = reader.readLine();
            int multipleLines = 0;
            while (line != null) {
                Matcher matcher = Pattern.compile("( *)([^; (]+)").matcher(line);
                if (matcher.find(offset)) {
                    if (matcher.group(1).length() != indentation && offset == 0) {
                        System.out.println(lineCounter + ": Indentation error!");
                    } else if (offset != 0 && multipleLines == 0) {
                        System.out.println(
                                lineCounter + ": The \"" + matcher.group(2) + "\" statement should be on a new line!");
                    }
                    switch (matcher.group(2)) {
                        case "package":
                            Matcher subMatcher =
                                    Pattern.compile(" *package( +)[^ ;]+( *);([ ;]*)(.*?)(?://.*)?$").matcher(line);
                            if (subMatcher.find(offset)) {
                                if (multipleLines != 0) {
                                    System.out.println(lineCounter + ": Merge " + (multipleLines + 1) + " lines" + "!");
                                } else {
                                    if (subMatcher.group(1).length() != 1) {
                                        System.out.println(lineCounter + ": " + subMatcher.group(1).length() +
                                                " spaces after \"package\"! (Need 1)");
                                    }
                                    if (subMatcher.group(2).length() != 0) {
                                        System.out.println(lineCounter + ": " + subMatcher.group(2).length() +
                                                " spaces before semicolon! (Need 0)");
                                    }
                                }
                                semicolonWarning(lineCounter, subMatcher.group(3));
                                lineCounter += multipleLines;
                                multipleLines = 0;
                                if (subMatcher.group(4).length() != 0) {
                                    offset = subMatcher.start(4);
                                } else {
                                    offset = 0;
                                }
                            } else {
                                line = line + " " + reader.readLine();
                                multipleLines++;
                                continue;
                            }
                            break;
                        case "import":
                            break;
                        default:
                            offset = 0;
                            break;
                    }
                } else {
                    System.out.println(lineCounter + ": Empty line!");
                }
                if (offset == 0) {
                    line = reader.readLine();
                    lineCounter++;
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void semicolonWarning(int lineCounter, String string) {
        int semicolonCounter = 0;
        for (int i = 0; i < string.length(); i++) {
            if (string.charAt(i) == ';') {
                semicolonCounter++;
            }
        }
        if (semicolonCounter != 0) {
            System.out.println(lineCounter + ": " + (semicolonCounter + 1) + " semicolons for one statement! (Need 1)");
        }
    }
}
