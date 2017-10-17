package ragnarok;

import yggdrasil.Cosmos;
import yggdrasil.Yggdrasil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Idavoll extends Cosmos{
    private String[] flags;
    private Map<String, ArrayList<String>> flagArgs;

    private String base_dir;

    public Idavoll(Yggdrasil context) {
        super(context.getPaths(), context.getTagTable());
    }
    protected void configure() {
        //throw new RuntimeException("Unimplemented.");
    }

    public void create(String... files) {
        System.out.println("Compilation beginning.");


        boolean implmented = false;
        if (!implmented) {
            System.out.println("Backend conversion not yet implemented. Compilation of assembly will not run.");
            System.exit(-1);
        }

        List<String> cmd = new ArrayList<>();
        String assembler = "gcc";
        cmd.add(assembler);
        for (String flag : flags) {
            cmd.add(flag);
            if (flagArgs.containsKey(flag)) {
                cmd.addAll(flagArgs.get(flag));
            }
        }
        ProcessBuilder pb = new ProcessBuilder(cmd).inheritIO()
                .directory(new java.io.File(base_dir == null ? getContext().BASE_DIR : base_dir));
        Collections.addAll(cmd, files);
        System.out.print("ProcessBuilder launching with the following process: ");
        System.out.println(pb.command());
        try {
            Process p = pb.start();
            p.waitFor();
            int exit = p.exitValue();
            if (exit != 0) {
                System.out.println("Error reported from " + assembler + ".");
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("IO error with the subprocess.");
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.out.print(assembler);
            System.out.println(" interrupted.");
        }
    }
}
