package analyzer;

import config.PathHolder;
import symtable.SymTable;
import tagtable.Tag;
import tagtable.TagPriority;
import tagtable.TagTable;
import ast.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

class ScopeChanger {
    private TagTable tagTable;
    private final SymTable symTable;
    private final PathHolder holder;
    private final boolean force;
    private final Set<Tag> changeOnEntry = new HashSet<>();
    private final Map<Tag, HashSet<Object>> changeOnEntryFromTo = new HashMap<>();

    /**
     * Presents a scope changer for the tree walk.
     * @param parent The context data, AST and symtable.
     * @param symTable
     * @param force Force deepening the tree.
     */
    public ScopeChanger(TagTable tagTable, PathHolder holder, SymTable symTable, boolean force) {
        this.tagTable = tagTable;
        this.holder = holder;
        this.symTable = symTable;
        this.force = force;
        try {
            String config = new String(Files.readAllBytes(Paths.get(holder.BASE_DIR + holder.TARGET +
                    holder.SCOPE_CHANGER_DEC_EXTENSION)), StandardCharsets.UTF_8);
            parseConfig(config);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("No scope configuration file detected.");
        }
    }
    /**
     * Parse the provided configuration input.
     * @param input The input.
     */
    private void parseConfig(String input) {
        String[] config = input.trim().split("\\s+");
        int i = 0;
        Tag last = null;
        while (i < config.length) {
            if (config[i + 1].startsWith("%")) {
                throw new RuntimeException("Incorrect scope configuration file.");
            }
            Tag one = tagTable.addElseFindTag(TagPriority.PAR, config[i + 1]);
            boolean swap = true;
            switch (config[i]) {
                case "%CASE":
                    changeOnEntry.add(one);
                    i += 1;
                    break;
                case "%CHILD":
                    if (i < config.length - 2 && !config[i + 2].startsWith("%")) {
                        Tag two = tagTable.addElseFindTag(TagPriority.SUB, config[i + 2]);
                        changeOnEntryFromTo.putIfAbsent(one, new HashSet<>());
                        changeOnEntryFromTo.get(one).add(two);
                        i += 2;
                        break;
                    } else if (i < config.length - 1 && !changeOnEntry.isEmpty()) {
                        changeOnEntryFromTo.putIfAbsent(last, new HashSet<>());
                        changeOnEntryFromTo.get(last).add(one);
                        i += 1;
                        swap = false;
                        break;
                    }
                default:
                    throw new RuntimeException("Incorrect scope configuration file.");
            }
            i += 1;
            if (swap) last = one;
        }
    }

    public void onLaunch(boolean reset) {
        if (reset) symTable.reset();
    }
    public void onUpEnter(Branch branch) {
        if (changeOnEntry.contains(branch.getTag())) {
            //System.out.println("Headed into deeper scope: branch" + branch + ".");
            if (force) {
                symTable.pushScope();
            } else {
                symTable.travDownScope();
            }
        }
    }
    public void onDownEnter(Branch branch, Branch child) {
        if (changeOnEntryFromTo.containsKey(branch.getTag()) &&
                changeOnEntryFromTo.get(branch.getTag()).contains(child.getTag())) {
            //System.out.println("Headed into higher scope: branch and child" + branch + ", " + child + ".");
            symTable.travUpScope();
        }
    }
    void onUpExit(Branch branch) {
        if (changeOnEntry.contains(branch.getTag())) {
            //System.out.println("Headed into higher scope: branch" + branch + ".");
            symTable.travUpScope();
        }
    }
    void onDownExit(Branch branch, Branch child) {
        if ((changeOnEntryFromTo.containsKey(branch.getTag()) &&
                changeOnEntryFromTo.get(branch.getTag()).contains(child.getTag()))
        ){
            //System.out.println("Headed into deeper scope: branch and child: " + branch + ", " + child + ".");
            if (force) {
                symTable.pushScope();
            } else {
                symTable.travDownScope();
            }
        }
    }
    public void onComplete(boolean reset) {
        if (reset) symTable.reset();
    }
}
