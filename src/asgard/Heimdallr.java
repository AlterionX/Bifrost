package asgard;

import yggdrasil.Branch;
import yggdrasil.Yggdrasil;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Heimdallr {
    Yggdrasil parent;
    Hoenir irlGen;
    List<Stag> astWalkers;
    List<String> stagFiles;

    public Heimdallr(Yggdrasil parent) {
        this.parent = parent;
        irlGen = new Hoenir(parent);
        astWalkers = new ArrayList<>();
        stagFiles = new ArrayList<>();
        try {
            stagFiles = Files.readAllLines(Paths.get(parent.BASE_DIR +
                    parent.TARGET + parent.ANALYZER_DEC_EXTENSION));
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

    public void analyze(Branch root, String file) {
        URL base = null;
        try {
            if (stagFiles.get(0).startsWith("PATH=")) {
                base = Paths.get(parent.BASE_DIR + stagFiles.get(0).substring("PATH=".length()).trim()).toUri().toURL();
                stagFiles.remove(0);
            } else {
                base = Paths.get(parent.BASE_DIR).toUri().toURL();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
            throw new RuntimeException("Something bad happened with the configuration.");
        }
        URLClassLoader loader = URLClassLoader.newInstance(new URL[]{base});
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        System.out.println(base);
        //Setup walkers - compile and load
        for (String targetStag : stagFiles) {
            System.out.println("Will load class found in " + targetStag);
            int errcode = compiler.run(null, null, null, base.getPath() + targetStag + ".java");
            if (errcode != 0) {
                throw new RuntimeException("Dynamic class failed to compile.");
            }
            System.out.println("Compiler run with errcode: " + errcode);
            Stag walker;
            try {
                Object o = loader.loadClass(targetStag.replace('\\', '.'
                        ).replace('/', '.')).getConstructor(parent.getClass()).newInstance(parent);
                walker = (Stag) o;
            } catch (ClassNotFoundException | InstantiationException |
                    IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                e.printStackTrace();
                throw new RuntimeException("Something is wrong with the java file that has been passed int.");
            }
            astWalkers.add(walker);
        }
        for (Stag walker : astWalkers) {
            System.out.println("Walking " + walker.getWalkerName());
            Stag.startWalk(walker, root, true);
        }
        irlGen.prime(file);
        Stag.startWalk(irlGen, root, true);
    }

    public String getTargetPath() {
        return irlGen.getTargetPath();
    }
}