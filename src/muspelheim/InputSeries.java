package muspelheim;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InputSeries {
    private List<String> series = new ArrayList<>();
    private String seriesName;
    private InputSeries() {}
    public static List<InputSeries> fetchSeries(List<String> input) {
        List<InputSeries> list = new ArrayList<>();
        int start = 0;
        String name = null;
        boolean inBlock = false;
        for (int i = 0; i < input.size(); i++) {
            if (input.get(i).startsWith("::")) {
                if (!inBlock) {
                    inBlock = true;
                    start = i;
                    name = input.get(i).split("::")[1];
                    list.add(new InputSeries());
                } else {
                    if (!name.equals(input.get(i).split("::")[1])) {
                        throw new RuntimeException("Multiline MTL input not bounded");
                    }
                    inBlock = false;
                    list.get(list.size() - 1).seriesName = name;
                }
            }
            if (inBlock) {
                if (start == i) {
                    String[] splitFirst = input.get(i).split("::");
                    if (splitFirst.length > 2 && splitFirst[2].length() > 0) {
                        System.out.println("Is a single line.");
                        inBlock = false;
                        list.get(list.size() - 1).series.add(input.get(i).substring(
                                input.get(i).indexOf("::", 2) + 2));
                        list.get(list.size() - 1).seriesName = name;
                    }
                } else {
                    list.get(list.size() - 1).series.add(input.get(i));
                }
            }
        }
        return list;
    }

    public List<String> getSeries() {
        return series;
    }
    public String getSeriesName() {
        return seriesName;
    }

    public String toString() {
        return "Name:" + seriesName + "=" + series;
    }
}
