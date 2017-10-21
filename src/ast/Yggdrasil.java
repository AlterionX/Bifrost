package ast;

import config.PathHolder;
import logger.Log;
import symtable.Nidhogg;
import tagtable.TagTable;

import java.util.*;

public class Yggdrasil implements AST {
    private final ArrayList<Core> cores = new ArrayList<>();

    private TagTable tagTable;
    private Nidhogg symTable;

    public Yggdrasil() {
        tagTable = new TagTable();
        symTable = new Nidhogg();
    }
    //Core data
    /**
     * Adding a "Core", or the root of an AST, to the complete Tree.
     * @param newCore The new AST to add.
     */
    public void addCore(Branch newCore) {
        cores.add(new Core(newCore));
    }
    /**
     * Prints the cores listed under the static cases.
     */
    public void printCores() {
        System.out.println("Printing public core.");
        for (Core publicCore : cores) {
            Seedling.simplePrint(publicCore);
        }
        System.out.println("Print complete");
    }
    public int getCoreCount() {
        return cores.size();
    }
    public Core getCore(int idx) {
        return cores.get(idx);
    }

    //MLGenerator compiler symtable
    public TagTable getTagTable() {
        return tagTable;
    }

    //MLGenerator symtable
    public Nidhogg getSymTable() {
        return symTable;
    }

    public void printCore(int i) {
        Log.l("/*******************************AST********************************/");
        Seedling.simplePrint(cores.get(i));
        Log.l("/******************************************************************/");
    }

    public Branch getRoot(int idx) {
        return getCore(idx).getInternal();
    }
}
