import config.PathHolder;

public interface Launcher {
    /**
     * Calls the functions necessary for generating the AST, or itself.
     *
     * Note that this does not prepare the files to be read, only priming sub-components with
     * the input file paths to the children.
     */
    void launch();

    //Path configuration
    PathHolder getPaths();
}
