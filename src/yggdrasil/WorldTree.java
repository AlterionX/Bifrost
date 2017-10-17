package yggdrasil;

import tagtable.TagTable;

import java.util.Map;

public interface WorldTree {
    //Let's begin!
    /**
     * Calls the functions necessary for generating the AST, or itself.
     *
     * Note that this does not prepare the files to be read, only priming sub-components with
     * the input file paths to the children.
     */
    void launch();
    //Core data
    /**
     * Adding a "Core", or the root of an AST, to the complete Tree.
     * @param newCore The new AST to add.
     */
    void addCore(Branch newCore);
    /**
     * Prints the cores listed under the static cases.
     */
    void printCores();
    int getCoreCount();
    Core getCore(int index);

    //Compiler compiler symtable
    TagTable getTagTable();

    //Compiler symtable
    Nidhogg getSymTable();
}
