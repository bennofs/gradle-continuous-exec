package com.example;

import java.io.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;

public class ContinuousExample {
    public static void doIt(Collection<String> changed) throws IOException {
        try (Writer out = new FileWriter(("build/continuous-main"))) {
            out.write(new Date().toString() + "\n" + String.join("\n", changed) + "\n");
        }
    }

    public static void main(String[] args) throws IOException {
        final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        System.out.println("ok");
        doIt(Collections.emptyList());
        reader.lines().forEach(line -> {
            System.err.println(line);
            try {
                doIt(Arrays.asList(line.split("\0")));
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("ok");
        });
    }
}
