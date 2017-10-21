package base;

import ast.AST;
import config.PathHolder;
import symtable.SymTable;
import tagtable.TagTable;

public abstract class Cosmos {
    private final PathHolder context;
    private final AST ast;

    /**
     * Initializes the module with a context AST, symtable, and data.
     * Call the abstract method configure during construction.
     *
     * @param context The AST, symtable, and contextual data class.
     */
    protected Cosmos(PathHolder context, AST ast) {
        this.ast = ast;
        this.context = context;
        configure();
    }

    /**
     * Is called by the constructor to configure the module.
     */
    protected abstract void configure();

    protected AST getAST() {
        return ast;
    }
    protected SymTable getSymTable() {
        return ast.getSymTable();
    }
    protected TagTable getTagTable() {
        return ast.getTagTable();
    }
    protected PathHolder getContext() {
        return context;
    }
}
