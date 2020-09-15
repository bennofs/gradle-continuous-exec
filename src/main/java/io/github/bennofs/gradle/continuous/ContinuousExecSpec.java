package io.github.bennofs.gradle.continuous;

import java.io.*;

/**
 * Arguments passed to the continuous exec worker process.
 * <p>
 * The worker is passed an InputStream and an OutputStream.
 * These streams are used to communicate with the worker.
 * <p>
 * <b>Worker requests:</b> The protocol consists of simple JSON-encoded messages, where each message is terminated
 * by a newline. The worker receives a {@code {"command": "changed","paths": [...list of changed watched paths....]}}
 * message whenever a change is detected. After the build is complete, a {@code {"command":"buildFinished"}} message
 * is sent. When the build has failed, this message contains an additional "failure" key with the error message.
 * <p>
 * <b>Worker responses:</b> After startup, the worker must send a message with an empty object {@code {}}. The same
 * message must be sent again after processing a request.
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
