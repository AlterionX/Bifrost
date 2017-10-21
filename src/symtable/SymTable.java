package symtable;

import java.util.Optional;

public interface SymTable {
    void mangle();
    void addSym(String substring, String function, int i);

    void reset();

    void pushScope();

    void travDownScope();

    void travUpScope();

    String hasSym(String symaccess, String symaccess1, int i);

    Optional<String> getSymProperty(String symaccess, String symaccess1, String symaccess2);

    void addSymProperty(String funcName, String function, String s, String substring);

    int getOffset(String substring, String var);
}
