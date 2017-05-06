package yggdrasil;

import java.util.*;

public class Nastrond {
    private Nastrond parent;
    private Map<String, Map<String, Map<String, String>>> functionSeparatedMap = new HashMap<>();
    private List<Nastrond> children;
    private Integer nextTraverse;
    public Nastrond(Nastrond parent) {
        children = new ArrayList<>();
        this.parent = parent;
        nextTraverse = 0;
    }

    public void reset() {
        nextTraverse = 0;
        for (Nastrond scope : children) {
            scope.reset();
        }
    }

    public Nastrond down(boolean generate) {
        if (nextTraverse >= children.size()) {
            if (!generate) {
                throw new RuntimeException("Non-generating downwards traversal has been requested with no available path.");
            }
            children.add(new Nastrond(this));
        }
        return children.get(nextTraverse++);
    }
    public Optional<Nastrond> up() {
        return Optional.ofNullable(parent);
    }

    public void addSym(String symbol, String qualifier) {
        functionSeparatedMap.putIfAbsent(qualifier, new HashMap<>());
        if (functionSeparatedMap.get(qualifier).containsKey(symbol)) {
            //Clash
            throw new RuntimeException("Error when attempting to insert symbol " + symbol + " with qualifier " + qualifier + ".");
        }
        functionSeparatedMap.get(qualifier).put(symbol, new HashMap<>());
    }
    public Optional<Map<String, String>> getSym(String symbol, String qualifier) {
        if (functionSeparatedMap.containsKey(qualifier) &&
                functionSeparatedMap.get(qualifier).containsKey(symbol)) {
            return Optional.ofNullable(functionSeparatedMap.get(qualifier).get(symbol));
        }
        return Optional.ofNullable(parent).map(nastrond -> nastrond.getSym(symbol, qualifier)).orElse(Optional.empty());
    }
    public Optional<String> modSymAttribute(String symbol, String qualifier, String attribute, String attributeVal) {
        if (functionSeparatedMap.containsKey(qualifier) &&
                functionSeparatedMap.get(qualifier).containsKey(symbol)) {
            return Optional.ofNullable(functionSeparatedMap.get(qualifier).get(symbol).put(attribute, attributeVal));
        }
        return Optional.empty();
    }
    public Optional<String> checkSymAttribute(String symbol, String qualifier, String attribute) {
        if (!functionSeparatedMap.containsKey(qualifier) ||
                !functionSeparatedMap.get(qualifier).containsKey(symbol)) {
            return Optional.empty();
        }
        return Optional.ofNullable(functionSeparatedMap.get(qualifier).get(symbol).get(attribute));
    }

    public Optional<Nastrond> getSymScope(String symbol, String qualifier) {
        if (functionSeparatedMap.containsKey(qualifier) &&
                functionSeparatedMap.get(qualifier).containsKey(symbol)) {
            return Optional.of(this);
        }
        return Optional.ofNullable(parent).map((nastrond -> nastrond.getSymScope(symbol, qualifier))).orElse(Optional.empty());
    }
}
