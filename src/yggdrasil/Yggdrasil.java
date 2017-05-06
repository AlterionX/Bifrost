package yggdrasil;

import asgard.Heimdallr;
import midgard.Jormungandr;
import muspelheim.Surtr;
import niflheim.Helvegar;
import ragnarok.Idavoll;

import java.util.*;

public class Yggdrasil {
    //Main tester, calls launch.
    public static void main(String[] args) {
        Yggdrasil yggdrasil = new Yggdrasil("hello.tyr");
        yggdrasil.launch();
    }
    //Static, public variables
    public final boolean DEBUG = false;
    public final String TARGET = "tyrion";
    public final String BASE_DIR = "./" + TARGET + "/";
    public final String IRL_BASE_DIR = "irl/";
    public final String MTL_BASE_DIR = "mtl/";
    public final String MTL_EXTENSION = ".mtl";
    public final String SAMPLE_BASE_DIR = "samples/";
    public final String IRL_CODE_EXTENSION = ".irl";
    public final String LEXER_DEC_EXTENSION = ".lexdec";
    public final String PARSER_DEC_EXTENSION = ".pardec";
    public final String ANALYZER_DEC_EXTENSION = ".anadec";
    public final String ASSEMBLER_CALL_EXTENSION = ".asmrcall";
    public final String SCOPE_CHANGER_DEC_EXTENSION = ".scrdec";
    public final String MACHINE_LANG_TRANSLATION_DEC_EXTENSION = ".mltdec";
    public final String INTERMEDIATE_REPRESENTATION_LANG_DEC_EXTENSION = ".irldec";
    //Instance specific modules
    private TagRecord tagRecord;
    private Nidhogg symTable;
    private Helvegar phaseOne;
    private Jormungandr phaseTwo;
    private Heimdallr phaseThree;
    private Surtr phaseFour;
    private Idavoll phaseFive;
    //Per run info
    private ArrayList<String> programFiles = new ArrayList<>();
    private ArrayList<Core> cores = new ArrayList<>();
    /**
     * Initializes an instance of Yggdrasil with a list of private
     * program files.
     * @param files Program files to process individually.
     */
    public Yggdrasil(String... files) {
        this.programFiles.addAll(Arrays.asList(files));
        tagRecord = new TagRecord();
        symTable = new Nidhogg();
        phaseOne = new Helvegar(this);
        phaseTwo = new Jormungandr(this);
        phaseThree = new Heimdallr(this);
        phaseFour = new Surtr(this);
    }

    /**
     * Calls the functions necessary for generating the AST, or itself.
     *
     * Note that this does not prepare the files to be read, only priming sub-components with
     * the input file paths to the children.
     */
    private void launch() {
        for (String file : programFiles) {
            System.out.println("Processing file: " + file);
            System.out.println("Priming file.");
            phaseOne.prime(file);
            System.out.println("Parsing file.");
            if (phaseTwo.parse()) {
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

    public Integer addTagIfAbsent(String label, TagPriority priority) {
        switch (priority) {
            case LEX:
                return tagRecord.addLexTagIfAbsent(label);
            case PAR:
                return tagRecord.addParTagIfAbsent(label);
            case SUB:
            default:
                return tagEncode(tagRecord.generateSubTag(label), priority);
        }
    }
    public Integer tagEncode(String label, TagPriority priority) {
        switch (priority) {
            case LEX:
                return tagRecord.lexEncode(label);
            case PAR:
                return tagRecord.parEncode(label);
            case SUB:
            default:
                return tagRecord.subEncode(label);
        }
    }
    public String tagDecode(Integer tag, TagPriority priority) {
        switch (priority) {
            case LEX:
                return tagRecord.lexDecode(tag);
            case PAR:
                return tagRecord.parDecode(tag);
            case SUB:
            default:
                return tagRecord.subDecode(tag);
        }
    }
    public boolean hasTag(String label, TagPriority priority) {
        switch (priority) {
            case LEX:
                return tagRecord.hasLexTag(label);
            case PAR:
                return tagRecord.hasParTag(label);
            case SUB:
            default:
                return tagRecord.hasSubTag(label);
        }
    }
    public boolean hasTag(Integer tag, TagPriority priority) {
        switch (priority) {
            case LEX:
                return tagRecord.hasLexTag(tag);
            case PAR:
                return tagRecord.hasParTag(tag);
            case SUB:
            default:
                return tagRecord.hasSubTag(tag);
        }
    }
    public int terminalCount() {
        return tagRecord.terminalCount();
    }
    public int tagCount() {
        return tagRecord.tagCount();
    }
    public boolean isTerminal(String label) {
        return tagRecord.isTerminal(label);
    }
    public boolean isTerminal(Integer tag) {
        return tagRecord.isTerminal(tag);
    }

    public Leaf nextToken() {
        return phaseOne.next();
    }

    public void addSym(String symbol, String qualifier) {
        symTable.addSym(symbol, qualifier, 0);
    }
    public boolean hasSym(String symbol, String qualifier) {
        return symTable.hasSym(symbol, qualifier, 0);
    }
    public void addSymProperty(String symbol, String qualifier, String property, String value) {
        symTable.addSymProperty(symbol, qualifier, property, value);
    }
    public String getSymProperty(String symbol, String qualifier, String property) {
        return symTable.getSymProperty(symbol, qualifier, property).orElse("");
    }
    public void deepenScope() {
        symTable.pushScope();
    }
    public void popScope() {
        symTable.popScope();
    }
    public void scopeTravDown() {
        symTable.travDownScope();
    }
    public void scopeTravUp() {
        symTable.travUpScope();
    }
    public void resetSymTable() {
        symTable.reset();
    }
    public Map<String, String> getSym(String symbol, String qualifier) {
        return symTable.getSym(symbol, qualifier, 0).orElse(new HashMap<>());
    }
    public int getSymOffset(String symbol, String qualifier) {
        return symTable.getOffset(symbol, qualifier);
    }
}
