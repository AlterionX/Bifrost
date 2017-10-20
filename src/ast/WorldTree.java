package ast;

import symtable.Nidhogg;
import tagtable.TagTable;

public interface WorldTree {
    /**
     * Calls the functions necessary for generating the AST, or itself.
     *
     * Note that this does not prepare the files to be read, only priming sub-components with
     * the input file paths to the children.
     */
    void launch();

    /**
     * Adding a "Core", or the root of an AST, to the complete Tree.
     * @param newCore The new AST to add.
     */
    void addCore(Branch newCore);

    int getCoreCount();

    Core getCore(int index);

    //Compiler symtable
    TagTable getTagTable();

    //Compiling program's symtable
    Nidhogg getSymTable();
}
