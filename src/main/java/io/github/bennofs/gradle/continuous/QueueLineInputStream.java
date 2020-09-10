package io.github.bennofs.gradle.continuous;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.BlockingQueue;

/**
 * An InputStream that returns lines from a queue.
 */
public class QueueLineInputStream extends InputStream implements Closeable {
    private boolean closed = false;
    private final BlockingQueue<String> commandQueue;
    private ByteBuffer currentLine;

    public QueueLineInputStream(BlockingQueue<String> commandQueue) {
        this.commandQueue = commandQueue;
    }

    @Override
    synchronized public void close() throws IOException {
        this.closed = true;
    }

    @Override
    public int read() throws IOException {
        if (this.closed) return -1;
        ensureLine();

        return currentLine.get();
    }

    // we need to override read, so that read returns all data that is available without blocking
    // if we didn't do this, the worker can't know when to send data to the process
    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (this.closed) return -1;
        ensureLine();

        int amount = Math.min(len, currentLine.remaining());
        currentLine.get(b, off, amount);
        return amount;
    }

    private void ensureLine() throws IOException {
        if (currentLine != null && currentLine.hasRemaining()) return;
        try {
            currentLine = StandardCharsets.UTF_8.encode(commandQueue.take() + "\n");
        } catch (InterruptedException e) {
            throw new IOException("queue take interrupted", e);
        }
    }
}
