package asgard;

import yggdrasil.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class ScopeChanger {
    private Yggdrasil parent;
    private boolean force;
    private Set<Integer> changeOnEntry = new HashSet<>();
    private Map<Integer, Set<Integer>> changeOnEntryFromTo = new HashMap<>();
    private Map<Integer, Set<Integer>> changeOnEntryToNumbered = new HashMap<>();
    public ScopeChanger(Yggdrasil parent, boolean force) {
        this.parent = parent;
        this.force = force;
        try {
            String config = new String (Files.readAllBytes(Paths.get(parent.BASE_DIR + parent.TARGET +
                    parent.SCOPE_CHANGER_DEC_EXTENSION)), StandardCharsets.UTF_8);
            parseConfig(config);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("No scope configuration file detected.");
        }
        System.out.println(changeOnEntry);
        System.out.println(changeOnEntryFromTo);
    }
    private void parseConfig(String input) {
        String[] config = input.trim().split("\\s+");
        int i = 0;
        Integer last = 0;
        while (i < config.length) {
            if (config[i + 1].startsWith("%")) {
                throw new RuntimeException("Incorrect scope configuration file.");
            }
            Integer one = parent.tagEncode(config[i + 1], TagPriority.SUB);
            boolean swap = true;
            switch (config[i]) {
                case "%CASE":
                    changeOnEntry.add(one);
                    i += 1;
                    break;
                case "%CHILD":
                    if (i < config.length - 2 && !config[i + 2].startsWith("%")) {
                        Integer two = parent.tagEncode(config[i + 2], TagPriority.SUB);
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
        if (reset) parent.resetSymTable();
    }
    public void onUpEnter(Branch branch) {
        if (changeOnEntry.contains(branch.getTag())) {
            System.out.println("Headed into deeper scope: branch" + branch + ".");
            Seedling.simplePrint(branch);
            if (force) {
                parent.deepenScope();
            } else {
                parent.scopeTravDown();
            }
        }
    }
    public void onDownEnter(Branch branch, Branch child) {
        if (changeOnEntryFromTo.containsKey(branch.getTag()) &&
                changeOnEntryFromTo.get(branch.getTag()).contains(child.getTag())) {
            System.out.println("Headed into higher scope: branch and child" + branch + ", " + child + ".");
            Seedling.simplePrint(branch);
            parent.scopeTravUp();
        }
    }
    protected void onUpExit(Branch branch) {
        if (changeOnEntry.contains(branch.getTag())) {
            System.out.println("Headed into higher scope: branch" + branch + ".");
            Seedling.simplePrint(branch);
            parent.scopeTravUp();
        }
    }
    protected void onDownExit(Branch branch, Branch child) {
        if ((changeOnEntryFromTo.containsKey(branch.getTag()) &&
                changeOnEntryFromTo.get(branch.getTag()).contains(child.getTag()))
        ){
            System.out.println("Headed into deeper scope: branch and child: " + branch + ", " + child + ".");
            Seedling.simplePrint(branch);
            if (force) {
                parent.deepenScope();
            } else {
                parent.scopeTravDown();
            }
        }
    }
    public void onComplete(boolean reset) {
        if (reset) parent.resetSymTable();
    }
}
