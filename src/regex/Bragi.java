package regex;

import lexer.Hel;

import java.util.*;

class Bragi {
    private static final Scanner s = new Scanner(System.in);
    private static Skald parser = null;
    public static void main(String[] args) {
        System.out.println("NOTE: backtracking is NOT available.");
        //Testing the regex
        boolean testRegex = true;
        boolean resetRegex = true;
        while (testRegex) {
            if (resetRegex) {
                resetRegex = false;
                generateRegExDFA();
            }
            System.out.println("Enter 1 to check a string's match.\n"
                    + "Enter 2 for a new regex.\n"
                    + "Enter 3 to search a string for the regex.\n"
                    + "Enter 4 to check the pattern string.\n"
                    + "Enter 5 to check the reduced, formal pattern string.\n"
                    + "Enter 6 to utilize NFA search instead.\n"
                    + "Enter q to quit.");
            String line = fetchUserOptionString(new String[]{"1", "2", "3", "4", "5", "6", "q"});
            switch (line) {
                case "1":
                    System.out.println("Enter a line to match with the regex.");
                    line = s.nextLine();
                    System.out.println("Using NFA.");
                    List<Integer> matches = parser.match(line);
                    if (matches.isEmpty()) {
                        System.out.println("Failed to match.");
                    } else {
                        System.out.println("String matched! Match from " + 0 + " to " + matches + ".");
                    }
                    break;
                case "2":
                    resetRegex = true;
                    break;
                case "3":
                    System.out.println("Search is not implemented.");
                    /*String[] parsed = parser.search();
                    System.out.println("Searched for regex " + parser.getPattern());
                    System.out.println("Found following matches: ");
                    for (String x : parsed) {
                        System.out.println("\t" + x);
                    }*/
                    break;
                case "4":
                    System.out.println("Current entered pattern: " + parser.getPattern());
                    break;
                case "5":
                    System.out.println("Regenerated pattern: " + parser.generateString());
                    break;
                case "6":
                    System.out.println("Enter a line to match with the regex.");
                    line = s.nextLine();
                    System.out.println("Using DFA.");
                    matches = parser.getDFA().process(line, 0);
                    if (matches.isEmpty()) {
                        System.out.println("Failed to match.");
                    } else {
                        System.out.println("String matched! Match from " + 0 + " to " + matches + ".");
                    }
                    break;
                default:
                    System.out.println("HAXORS DIEEEEEE");
                case "q":
                    testRegex = false;
            }
        }
    }

    private static void generateRegExDFA() {
        parser = resetParser();
        parser.setNFA(parser.generateNFA().tablify());
        parser.setDFA(parser.getNFA().generateDFA().minimize());

        parser.printStructure(0);
        parser.getNFA().printTable();
        parser.getDFA().printTable();
    }
    private static String fetchUserOptionString(String[] valid) {
        String line = s.nextLine();
        while (!Arrays.asList(valid).contains(line)) {
            System.out.println("Invalid input. Please reenter a value.");
            line = s.nextLine();
        }
        return line;
    }
    private static Skald resetParser() {
        System.out.println("Please enter a regex expression.");
        String line = s.nextLine();
        while (line == null) {
            line = s.nextLine();
        }
        return new Skald(line, Hel.DEFAULT_ALPH);
    }
}
