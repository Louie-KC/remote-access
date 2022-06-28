public class Main {
    private static String targetIP = "127.0.0.1";
    private static int targetPort = 7777;
    private static boolean isRemoteMachine;

    private static Base program;

    public static void main(String[] args) {
        if (!handleArgs(args)) { return; }
        if (isRemoteMachine) {
            program = new Remote(targetPort);
        } else {
            program = new Client(targetIP, targetPort);
        }
        program.run();
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
                case "-client":
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