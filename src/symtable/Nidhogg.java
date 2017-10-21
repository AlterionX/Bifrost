package symtable;

import java.util.Map;
import java.util.Optional;

/**
 * Symbol table.
 */
public class Nidhogg implements SymTable {
    private final Nastrond root;
    private Nastrond currentScope;
    public Nidhogg() {
        root = new Nastrond(null);
        currentScope = root;
    }
    //What goes in a symbol table?
    public void addSym(String symbol, String qualifier, int offset) {
        if (offset != -1) {
            Optional<Nastrond> scope = Optional.of(currentScope);
            for (int i = 0; i < offset; i++) {
                scope = scope.flatMap(Nastrond::up);
            }
            if (!scope.isPresent()) {
                throw new RuntimeException("No scope!");
            }
            scope.get().addSym(symbol, qualifier);
        } else {
            root.addSym(symbol, qualifier);
        }
    }
    public String hasSym(String symbol, String qualifier, int offset) {
        if (offset != -1) {
            Optional<Nastrond> scope = Optional.of(currentScope);
            for (int i = 0; i < offset; i++) {
                scope = scope.flatMap(Nastrond::up);
            }
            if (!scope.isPresent()) {
                throw new RuntimeException("No scope!");
            }
            return scope.get().getSym(symbol, qualifier).isPresent() ? scope.get().getPrefix() + symbol : null;
        }
        return root.getSym(symbol, qualifier).isPresent() ? root.getPrefix() + symbol : null;
    }
    public void addSymProperty(String symbol, String qualifier, String property, String data) {
        currentScope.getSymScope(symbol, qualifier).ifPresent(scope -> scope.modSymAttribute(symbol, qualifier, property, data));
    }
    public Optional<String> getSymProperty(String symbol, String qualifier, String property) {
        return currentScope.getSymScope(symbol, qualifier).flatMap(scope -> scope.checkSymAttribute(symbol, qualifier, property));
    }
    public Optional<Map<String, String>> getSym(String symbol, String qualifier, int offser) {
        return currentScope.getSym(symbol, qualifier);
    }

    public void pushScope() {
        currentScope = currentScope.down(true);
    }
    public void popScope() {
        Optional<Nastrond> scope = currentScope.up();
        if (!scope.isPresent()) {
            throw new RuntimeException("Parent doesn't exist.");
        }
        currentScope = scope.get();
    }
    public void travDownScope() {
        currentScope = currentScope.down(false);
    }
    public void travUpScope() {
        popScope();
    }

    public void reset() {
        root.reset();
        currentScope = root;
    }

    public int getOffset(String symbol, String qualifier) {
        Optional<Nastrond> targ = currentScope.getSymScope(symbol, qualifier);
        if (!targ.isPresent()) {
            throw new RuntimeException("Symbol does not exist.");
        }
        int level = 0;
        Optional<Nastrond> temp = Optional.ofNullable(currentScope);
        while (temp.isPresent() && !temp.get().equals(targ.get())) {
            level++;
            temp = temp.get().up();
        }
        if (!temp.isPresent()) {
            return -1;
        }
        return level;
    }

    /**
     * Mangles names so symbols only fit only within their specific scope.
     */
    public void mangle() {
        root.mangle("0");
    }
}
