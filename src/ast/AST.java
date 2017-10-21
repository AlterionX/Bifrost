package ast;

import symtable.Nidhogg;
import tagtable.TagTable;

public interface AST {

    /**
     * Adding a "Core", or the root of an AST, to the complete Tree.
     * @param newCore The new AST to add.
     */
    void addCore(Branch newCore);

    int getCoreCount();

    Core getCore(int idx);
    Branch getRoot(int idx);
    void printCore(int idx);

    //MLGenerator symtable
    TagTable getTagTable();

    //Compiling program's symtable
    Nidhogg getSymTable();
}
