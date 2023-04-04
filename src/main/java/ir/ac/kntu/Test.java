package ir.ac.kntu;

import java.util.Scanner;

public class Test {
    public static boolean isPalindrome(String s) {
        s = s.toLowerCase();
        int i = 0;
        int j = s.length() - 1;
        while (j > i) {
            while (!Character.isLetter(s.charAt(i))) {
                i++;
            }
            while (!Character.isLetter(s.charAt(j))) {
                j--;
            }
            if (s.charAt(i) != s.charAt(j)) {
                return false;
            }
            i++;
            j--;
        }
        return true;
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        if (isPalindrome(in.nextLine())) {
            System.out.println("true");
        } else {
            System.out.println("false");
        }
    }
}