package ragnarok;

import yggdrasil.Yggdrasil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Idavoll {
    private String assembler = "gcc";
    private String[] flags;
    private Map<String, ArrayList<String>> flagArgs;

    private String base_dir;

    private Yggdrasil parent;

    public Idavoll(Yggdrasil parent) {
        this.parent = parent;
    }

    public void create(String... files) {
        List<String> cmd = new ArrayList<>();
        cmd.add(assembler);
        for (String flag : flags) {
            cmd.add(flag);
            if (flagArgs.containsKey(flag)) {
                cmd.addAll(flagArgs.get(flag));
            }
        }
        ProcessBuilder pb = new ProcessBuilder(cmd).inheritIO()
                .directory(new java.io.File(base_dir == null ? parent.BASE_DIR : base_dir));
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
