package ast;

import config.PathHolder;
import tagtable.TagTable;

public abstract class Cosmos {
    private final PathHolder context;
    private final TagTable tagTable;

    /**
     * Initializes the module with a context AST, symtable, and data.
     * Call the abstract method configure during construction.
     *
     * @param context The AST, symtable, and contextual data class.
     */
    protected Cosmos(PathHolder context, TagTable tagTable) {
        this.tagTable = tagTable;
        this.context = context;
        configure();
    }

    /**
     * Is called by the constructor to configure the module.
     */
    protected abstract void configure();

    protected TagTable getTagTable() {
        return tagTable;
    }
    protected PathHolder getContext() {
        return context;
    }
}
