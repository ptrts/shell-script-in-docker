package app;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.stream.Collectors;

public class Main {

    public static String runProcess(File directory, boolean inheritIO, String... command) throws IOException, InterruptedException {

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        if (directory != null) {
            processBuilder.directory(directory);
        }

        if (inheritIO) {
            // Делаем, чтобы поток выводил бы свое туда же, куда выводит System.out.println.
            // То же самое с stdin и stderr.
            processBuilder.inheritIO();
        }

        Process process = processBuilder.start();

        try (
                BufferedReader error = process.errorReader();
                BufferedReader input = process.inputReader();
        ) {
            int exitStatus = process.waitFor();

            String outputText = input.lines().collect(Collectors.joining("\n"));
            if (outputText.length() > 0) {
                System.out.println(outputText);
            }

            error
                    .lines()
                    .forEachOrdered(System.err::println);

            if (exitStatus == 0) {
                return outputText;
            } else {
                throw new RuntimeException(command[0] + " exited with status " + exitStatus);
            }
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        String output;

        System.out.println("====== Output (inheritIO = false)");

        System.out.println("--- Docker process");
        output = runProcess(null, false, "docker", "run", "--rm", "alpine", "sh", "-c", "echo hello");

        System.out.println("--- Java process");
        System.out.println(output);

        System.out.println("====== Output (inheritIO = true)");

        System.out.println("--- Docker process");
        output = runProcess(null, true, "docker", "run", "--rm", "alpine", "sh", "-c", "echo hello");

        System.out.println("--- Java process");
        System.out.println(output);

        System.out.println("======");
    }
}