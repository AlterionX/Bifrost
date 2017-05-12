package asgard;

import yggdrasil.Branch;
import yggdrasil.Cosmos;
import yggdrasil.Yggdrasil;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Heimdallr extends Cosmos {
    private Hoenir irlGen;
    private List<Stag> astWalkers;
    private List<String> stagFiles;

    /**
     * Construct Heimdallr.
     * @param context The context data, AST, and symtable
     */
    public Heimdallr(Yggdrasil context) {
        super(context);
        System.out.println("Heimdallr configured.");
    }
    /**
     * Configure Heimdallr and launch Hoenir's configuration
     */
    protected void configure() {
        irlGen = new Hoenir(context);
        System.out.println("Hoenir configured.");
        astWalkers = new ArrayList<>();
        stagFiles = new ArrayList<>();

        try {
            stagFiles = Files.readAllLines(Paths.get(context.BASE_DIR +
                    context.TARGET + context.ANALYZER_DEC_EXTENSION));
            for (int i = 0; i < stagFiles.size(); i++) {
                String line = stagFiles.get(i);
                if (line.startsWith("PATH=")) {
                    stagFiles.add(0, stagFiles.remove(i));
                }
            }
            stagFiles.removeAll(Collections.singletonList(""));
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Issues with the analyzer's declaration file.");
        }
    }

    /**
     * Repeatedly traverse the AST with different tree walkers.
     * @param root The root of the AST
     * @param file The name of the file being written to.
     */
    public void analyze(Branch root, String file) {
        URL base;
        try {
            if (stagFiles.get(0).startsWith("PATH=")) {
                base = Paths.get(context.BASE_DIR + stagFiles.get(0).substring("PATH=".length()).trim()).toUri().toURL();
                stagFiles.remove(0);
            } else {
                base = Paths.get(context.BASE_DIR).toUri().toURL();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
            throw new RuntimeException("Something bad happened with the configuration.");
        }
        URLClassLoader loader = null;
        try {
            loader = URLClassLoader.newInstance(new URL[]{
                    Paths.get(base.toURI()).toAbsolutePath().resolve("./out/").toAbsolutePath().toUri().toURL(),
                    Paths.get("./out/").toAbsolutePath().toUri().toURL()
            });
        } catch (MalformedURLException | URISyntaxException e) {
            e.printStackTrace();
            throw new RuntimeException("Unable to load classes with path.");
        }
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        System.out.println(base);
        //Setup walkers - compile and load
        for (String targetStag : stagFiles) {
            System.out.println("Loading walker " + targetStag + ".");
            System.out.println("Compiling " + targetStag + ".");
            try {
                if (!Files.exists(
                        Paths.get("./").toAbsolutePath()
                                .relativize(Paths.get(
                                        base.toURI()).toAbsolutePath()
                                        .resolve("./out")
                                )
                )) {
                    Files.createDirectories(
                            Paths.get("./").toAbsolutePath()
                                    .relativize(Paths.get(
                                            base.toURI()).toAbsolutePath()
                                            .resolve("./out")
                                    )
                    );
                }
            } catch (URISyntaxException | IOException e) {
                e.printStackTrace();
                throw new RuntimeException("Could not create output directory.");
            }
            int errcode = 0;
            try {
                errcode = compiler.run(null, null, null,
                        "-sourcepath",
                        Paths.get(base.toURI()).toAbsolutePath().toString() + ";./src/",
                        "-classpath",
                        Paths.get(base.toURI()).toAbsolutePath().resolve("./out/").toAbsolutePath() + ";" + "./out/",
                        "-d",
                        Paths.get(base.toURI()).toAbsolutePath().resolve("./out/").toAbsolutePath().toString(),
                        base.getPath() + targetStag + ".java");
            } catch (URISyntaxException e) {
                e.printStackTrace();
                throw new RuntimeException("Error opening folders for the classes.");
            }
            if (errcode != 0) {
                System.out.println("Compiler run with errcode: " + errcode);
                throw new RuntimeException("Dynamic class failed to compile.");
            }
            Stag walker;
            try {
                Object o = loader.loadClass(
                        targetStag.replace('\\', '.').replace('/', '.')
                ).getConstructor(context.getClass())
                        .newInstance(context);
                walker = (Stag) o;
            } catch (ClassNotFoundException | InstantiationException |
                    IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                e.printStackTrace();
                throw new RuntimeException("Something is wrong with the java file that has been passed int.");
            }
            System.out.println(targetStag + " loaded. Walking AST.");
            astWalkers.add(walker);
        }
        for (Stag walker : astWalkers) {
            System.out.println("Walking " + walker.getWalkerName());
            Stag.startWalk(walker, root, true);
        }
        context.mangleSyms();
        irlGen.prime(file);
        System.out.println("Walking " + irlGen.getWalkerName());
        System.out.println("Converting AST to IR code");
        Stag.startWalk(irlGen, root, true);
    }
    /**
     * Find the current path being written to.
     * @return The path being written to.
     */
    public String getTargetPath() {
        return irlGen.getTargetPath();
    }
}