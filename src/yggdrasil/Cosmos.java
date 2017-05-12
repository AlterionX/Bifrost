package yggdrasil;

public abstract class Cosmos {
    protected Yggdrasil context;

    /**
     * Initializes the module with a context AST, symtable, and data.
     * Call the abstract method configure during construction.
     *
     * @param context The AST, symtable, and contextual data class.
     */
    public Cosmos(Yggdrasil context) {
        this.context = context;
        configure();
    }

    /**
     * Is called by the constructor to configure the module.
     */
    protected abstract void configure();
}
