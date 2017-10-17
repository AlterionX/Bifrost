package tagtable;

/**
 * Works in conjunction with TagTable to filter tokens.
 */
public enum TagPriority {
    //Lexical tags, regular expression sub-components of the language, should only be terminals
    LEX,
    //Parsing tags, context-free grammar tags, both terminal and non-terminal
    PAR,
    //Tags used for substituting parsing tags
    SUB
}
