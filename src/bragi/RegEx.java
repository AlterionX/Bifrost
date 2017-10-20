package bragi;

public interface RegEx {
    /**
     * Simplify the regex (sub)expression to simpler terms.
     *
     * @return The simplified regex. This can be null.
     */
    RegEx reduce();

    //Should always have been reduced before any of these methods are called
    /**
     * Print the regex and any of its subcomponents.
     *
     * @param level The level of indentation to print.
     */
    void printStructure(int level);
    /**
     * Generate a regex string representation of the regex.
     *
     * @return A StringBuilder occupied by the String.
     */
    StringBuilder generateString();
    /**
     * Generate the NFA representation of the Regex. Note that this discards lookbehind.
     *
     * @return The NFA generated.
     */
    NFA generateNFA();

    RegEx reverse();
}
