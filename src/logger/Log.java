package logger;

public class Log {
    public static final int VERBOSE = 3;
    public static final int DEBUG = 2;
    public static final int LOG = 1;
    public static final int ALWAYS = 0;
    public static final int LOG_LVL = LOG;

    public static void logln(int lvl, String data) {
        if (LOG_LVL >= lvl) System.out.println(data);
    }
    private static void logf(int lvl, String fmt, Object[] data) {
        if (LOG_LVL >= lvl) System.out.printf(fmt, data);
    }
    private static void log(int lvl, String data) {
        if (LOG_LVL >= lvl) System.out.print(data);
    }

    public static void vln(String data) {
        logln(VERBOSE, data);
    }
    public static void dln(String data) {
        logln(DEBUG, data);
    }
    public static void lln(String data) {
        logln(LOG, data);
    }
    public static void aln(String data) {
        logln(ALWAYS, data);
    }

    public static void v(String data) {
        log(VERBOSE, data);
    }
    public static void d(String data) {
        log(DEBUG, data);
    }
    public static void l(String data) {
        log(LOG, data);
    }
    public static void a(String data) {
        log(ALWAYS, data);
    }

    public static void df(String s, Object... i) {
        logf(DEBUG, s, i);
    }
}
