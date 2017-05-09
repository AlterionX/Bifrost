package muspelheim;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Machine {
    private String machineName;
    private int regCount;
    private Map<Integer, List<String>> properties;
    private String[] names;

    private IRLConverter client;

    public Machine(int count, String name) {
        regCount = count;
        properties = new HashMap<>(count);
        names = new String[regCount];
        machineName = name;
    }

    public Machine(InputSeries series) {

    }

    public String convertLine(String line) {
        String initialSub = client.convert(line);
        //Process machine specific data, especially with the register allocation and deallocation
        return initialSub;
    }
}
