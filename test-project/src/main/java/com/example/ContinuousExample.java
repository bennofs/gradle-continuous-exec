package com.example;

import org.json.JSONObject;

import java.io.*;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.stream.Collectors;

public class ContinuousExample {
    public static void doIt(Collection<String> changed) throws IOException {
        try (Writer out = new FileWriter(("build/continuous-main"))) {
            out.write(new Date().toString() + "\n" + String.join("\n", changed) + "\n");
        }
    }

    public static void main(String[] args) throws IOException {
        final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        System.out.println("{}");
        doIt(Collections.emptyList());
        reader.lines().forEach(line -> {
            System.err.println(line);
            final JSONObject msg = new JSONObject(line);
            final String command = msg.getString("command");
            if (command.equals("changed")) {
                try {
                    doIt(msg.getJSONArray("paths").toList().stream().map(Object::toString).collect(Collectors.toList()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("{}");
        });
    }
}
