public class Main {
    public static String targetIP;
    public static int targetPort;
    private static boolean isRemoteMachine;

    public static void main(String[] args) {
        if (!handleArgs(args)) { return; }
    }

    private static boolean handleArgs(String[] args) {
        if (args == null) { return false; }
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-ip":
                    targetIP = args[++i];
                    continue;
                case "-p":
                    targetPort = Integer.valueOf(args[++i]);
                    continue;
                case "-host":
                    isRemoteMachine = false;
                    continue;
                case "-remote":
                    isRemoteMachine = true;
                    continue;
                default:
                    System.err.println("Invalid argument: " + args[i]);
                    return false;
            }
        }
        return true;
    }
}