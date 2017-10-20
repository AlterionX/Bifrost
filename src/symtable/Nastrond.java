package symtable;

import java.util.*;

/**
 * Table scope
 */
class Nastrond {
    private final Nastrond parent;
    private final Map<String, Map<String, Map<String, String>>> functionSeparatedMap = new HashMap<>();
    private final List<Nastrond> children;
    private Integer nextTraverse;

    private String prefix = "";

    Nastrond(Nastrond parent) {
        children = new ArrayList<>();
        this.parent = parent;
        nextTraverse = 0;
    }

    void reset() {
        nextTraverse = 0;
        for (Nastrond scope : children) {
            scope.reset();
        }
    }

    Nastrond down(boolean generate) {
        if (nextTraverse >= children.size()) {
            if (!generate) {
                throw new RuntimeException("Non-generating downwards traversal has been requested with no available path.");
            }
            children.add(new Nastrond(this));
        }
        return children.get(nextTraverse++);
    }
    Optional<Nastrond> up() {
        return Optional.ofNullable(parent);
    }

    void addSym(String symbol, String qualifier) {
        functionSeparatedMap.putIfAbsent(qualifier, new HashMap<>());
        if (functionSeparatedMap.get(qualifier).containsKey(symbol)) {
            //Clash
            throw new RuntimeException("Error when attempting to insert symbol " + symbol + " with qualifier " + qualifier + ".");
        }
        functionSeparatedMap.get(qualifier).put(symbol, new HashMap<>());
    }
    Optional<Map<String, String>> getSym(String symbol, String qualifier) {
        if (functionSeparatedMap.containsKey(qualifier) &&
                functionSeparatedMap.get(qualifier).containsKey(symbol)) {
            return Optional.ofNullable(functionSeparatedMap.get(qualifier).get(symbol));
        }
        return Optional.ofNullable(parent).flatMap(nastrond -> nastrond.getSym(symbol, qualifier));
    }
    Optional<String> modSymAttribute(String symbol, String qualifier, String attribute, String attributeVal) {
        if (functionSeparatedMap.containsKey(qualifier) &&
                functionSeparatedMap.get(qualifier).containsKey(symbol)) {
            return Optional.ofNullable(functionSeparatedMap.get(qualifier).get(symbol).put(attribute, attributeVal));
        }
        return Optional.empty();
    }
    Optional<String> checkSymAttribute(String symbol, String qualifier, String attribute) {
        if (!functionSeparatedMap.containsKey(qualifier) ||
                !functionSeparatedMap.get(qualifier).containsKey(symbol)) {
            return Optional.empty();
        }
        return Optional.ofNullable(functionSeparatedMap.get(qualifier).get(symbol).get(attribute));
    }

    Optional<Nastrond> getSymScope(String symbol, String qualifier) {
        if (functionSeparatedMap.containsKey(qualifier) &&
                functionSeparatedMap.get(qualifier).containsKey(symbol)) {
            return Optional.of(this);
        }
        return Optional.ofNullable(parent).flatMap(nastrond -> nastrond.getSymScope(symbol, qualifier));
    }

    void mangle(String prefix) {
        this.prefix = prefix;
        for (int i = 0; i < children.size(); i++) {
            children.get(i).mangle(prefix + "_" + i);
        }
    }

    String getPrefix() {
        return prefix;
    }
}
