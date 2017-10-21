import analyzer.Heimdallr;
import analyzer.IRAnalyzer;
import assembler.Assembler;
import ast.AST;
import ast.Yggdrasil;
import compiler.MLGenerator;
import config.PathHolder;
import lexer.Lexer;
import parser.Jormungandr;
import compiler.Surtr;
import lexer.Helvegar;
import assembler.Idavoll;
import parser.Parser;

import java.util.ArrayList;
import java.util.Arrays;

public class Ymir implements Launcher {
    //Main tester, calls launch.
    public static void main(String[] args) {
        Launcher launcher = new Ymir("hello.tyr");
        launcher.launch();
    }

    //Instance specific modules
    private AST ast;
    private Lexer phaseOne;
    private Parser phaseTwo;
    private IRAnalyzer phaseThree;
    private MLGenerator phaseFour;
    private Assembler phaseFive;
    //private Idavoll phaseFive;
    //Per run info
    private final ArrayList<String> programFiles = new ArrayList<>();
    private PathHolder paths;

    /**
     * Initializes an instance of Yggdrasil with a list of private
     * program files.
     * @param files Program files to process individually.
     */
    public Ymir(String... files) {
        paths = new PathHolder();

        ast = new Yggdrasil();

        programFiles.addAll(Arrays.asList(files));
        phaseOne = new Helvegar(paths, this.ast);
        phaseTwo = new Jormungandr(paths, ast, phaseOne);
        phaseThree = new Heimdallr(paths, ast);
        phaseFour = new Surtr(paths, this.ast);
        phaseFive = new Idavoll(paths, ast);
    }

    //Let's begin!
    /**
     * Calls the functions necessary for generating the AST, or itself.
     *
     * Note that this does not prepare the files to be read, only priming sub-components with
     * the input file paths to the children.
     */
    public void launch() {
        for (String file : programFiles) {
            System.out.println("Processing file: " + file);
            System.out.println("Priming file.");
            phaseOne.loadStream(file);
            System.out.println("Parsing file.");
            if (phaseTwo.parse()) {
                ast.printCore(ast.getCoreCount() - 1);
                System.out.println("Analyzing file.");
                phaseThree.analyze(ast.getRoot(ast.getCoreCount() - 1), file);
                phaseFour.convert(phaseThree.getTargetPath());
            }
            System.out.println("File processed: " + file);
            System.out.println();
            System.out.println();
        }
        phaseFive.assemble(programFiles.toArray(new String[programFiles.size()]));
    }

    //Path configuration
    public PathHolder getPaths() {
        return paths;
    }
}
