import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        List<String> builtins = builtins();

        while (true) {
            System.out.print("$ "); // Shell starts with dollar
            String input = scanner.nextLine(); // accept any input
            String[] str = input.split(" ");

            if (str.length == 0) continue;

            String command = str[0];
            String[] parameters = new String[str.length - 1];
            System.arraycopy(str, 1, parameters, 0, str.length - 1);

            switch (command) {
                case "exit" -> {
                    if (parameters.length == 1 && parameters[0].equals("0")) {
                        System.exit(0);
                    } else {
                        System.out.println(input + ": command not found");
                    }
                }
                case "echo" -> System.out.println(String.join(" " + parameters));
                case "type" -> {
                    if (builtins.contains(parameters[0])) {
                        System.out.println(parameters[0] + " is a shell builtin");
                    } else {
                        String path = getPath(parameters[0]);
                        if (path != null) {
                            System.out.println(parameters[0] + " is " + path);
                        } else {
                            System.out.println(parameters[0] + ": not found");
                        }
                    }
                }
                default -> runExternalCommand(command, parameters);
            }
        }
    }

    public static void runExternalCommand(String command, String[] args) {
        try {
            String path = getPath(command);
            if (path == null) {
                System.out.println(command + ": command not found");
                return;
            }

            List<String> cmdList = new ArrayList<>();
            cmdList.add(path);
            cmdList.addAll(List.of(args));

            ProcessBuilder pb = new ProcessBuilder(cmdList);
            Process process = pb.start();

            try(BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
            }
            process.waitFor();
        } catch (Exception e) {
            System.out.println(command + ": error running command");
        }
    }

    public static String getPath(String parameter) {
        for (String path : System.getenv("PATH").split(":")) {
            Path fullPath = Path.of(path, parameter);
            if (Files.isRegularFile(fullPath)) {
                return fullPath.toString();
            }
        }
        return null;
    }

    public static List<String> builtins() {
        List<String> builtins = new ArrayList<>();
        builtins.add("exit");
        builtins.add("echo");
        builtins.add("type");
        return builtins;
    }
}
