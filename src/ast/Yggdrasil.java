package ast;

import asgard.Heimdallr;
import config.PathHolder;
import logger.Log;
import midgard.Jormungandr;
import muspelheim.Surtr;
import niflheim.Helvegar;
import ragnarok.Idavoll;
import symtable.Nidhogg;
import tagtable.TagTable;

import java.util.*;

public class Yggdrasil implements WorldTree {
    //Main tester, calls launch.
    public static void main(String[] args) {
        Yggdrasil yggdrasil = new Yggdrasil("hello.tyr");
        yggdrasil.launch();
    }

    //Instance specific modules
    private PathHolder paths;
    private TagTable tagTable;
    private Nidhogg symTable;
    private Helvegar phaseOne;
    private Jormungandr phaseTwo;
    private Heimdallr phaseThree;
    private Surtr phaseFour;
    private Idavoll phaseFive;
    //private Idavoll phaseFive;
    //Per run info
    private final ArrayList<String> programFiles = new ArrayList<>();
    private final ArrayList<Core> cores = new ArrayList<>();
    /**
     * Initializes an instance of Yggdrasil with a list of private
     * program files.
     * @param files Program files to process individually.
     */
    public Yggdrasil(String... files) {
        paths = new PathHolder();

        this.programFiles.addAll(Arrays.asList(files));
        tagTable = new TagTable();
        symTable = new Nidhogg();
        phaseOne = new Helvegar(this.getPaths(), this.getTagTable());
        phaseTwo = new Jormungandr(this.getPaths(), this.getTagTable(), this.phaseOne, this);
        phaseThree = new Heimdallr(this);
        phaseFour = new Surtr(this.getPaths(), this.getTagTable());
        phaseFive = new Idavoll(this);
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
                Log.l("/*******************************AST********************************/");
                Seedling.simplePrint(cores.get(cores.size() - 1));
                Log.l("/******************************************************************/");
                System.out.println("Analyzing file.");
                phaseThree.analyze(cores.get(cores.size() - 1).getInternal(), file);
                phaseFour.convert(phaseThree.getTargetPath());
            }
            System.out.println("File processed: " + file);
            System.out.println();
            System.out.println();
        }
        phaseFive.create(programFiles.toArray(new String[0]));
    }

    public PathHolder getPaths() {
        return paths;
    }
    //Core data
    /**
     * Adding a "Core", or the root of an AST, to the complete Tree.
     * @param newCore The new AST to add.
     */
    public void addCore(Branch newCore) {
        cores.add(new Core(newCore));
    }
    /**
     * Prints the cores listed under the static cases.
     */
    public void printCores() {
        System.out.println("Printing public core.");
        for (Core publicCore : cores) {
            Seedling.simplePrint(publicCore);
        }
        System.out.println("Print complete");
    }
    public int getCoreCount() {
        return cores.size();
    }
    public Core getCore(int index) {
        return cores.get(index);
    }

    //Compiler compiler symtable
    public TagTable getTagTable() {
        return tagTable;
    }

    //Compiler symtable
    public Nidhogg getSymTable() {
        return symTable;
    }
}
