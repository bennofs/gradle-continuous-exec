package io.github.bennofs.gradle.continuous;

import java.io.*;

/**
 * Arguments passed to the continuous exec worker process.
 *
 * The worker is passed an InputStream and an OutputStream.
 * Whenever a change is detected, the list of changed files separated by null bytes is made available on the input stream.
 * The worker should then respond with the line "ok" (without quotes) on the output stream if the changes have been processed.
 *
 * The worker should also send an initial ok after starting up.
 */
public class ContinuousExecSpec {
    private final InputStream in;
    private final OutputStream out;

    ContinuousExecSpec(InputStream in, OutputStream out) {
        this.in = in;
        this.out = out;
    }

    /**
     * @return The input stream receiving commands for the worker (see class description)
     */
    public InputStream getInputStream() {
        return in;
    }

    /**
     * @return The output stream where the worker should reply after commands (see class description)
     */
    public OutputStream getOutputStream() {
        return out;
    }
}
