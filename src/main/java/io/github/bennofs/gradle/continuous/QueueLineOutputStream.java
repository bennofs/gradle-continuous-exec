package io.github.bennofs.gradle.continuous;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.BlockingQueue;

/**
 * An OutputStream that streams complete lines to a queue.
 */
public class QueueLineOutputStream extends OutputStream {
    private final BlockingQueue<String> commandQueue;
    private ByteBuffer currentLine;

    public QueueLineOutputStream(BlockingQueue<String> commandQueue) {
        this.commandQueue = commandQueue;
    }

    @Override
    public void write(int i) throws IOException {
        final byte b = (byte)(i & 0xff);
        if (b == '\n') {
            currentLine.flip();
            commandQueue.add(StandardCharsets.UTF_8.decode(currentLine).toString());
            currentLine.position(0);
            currentLine.limit(currentLine.capacity());
            return;
        }

        ensureSpace();
        this.currentLine.put(b);
    }

    private void ensureSpace() {
        if (currentLine == null) {
            currentLine = ByteBuffer.allocate(64);
            return;
        }
        final ByteBuffer newAlloc = ByteBuffer.allocate((int)(currentLine.capacity() * 1.5));
        this.currentLine.flip();
        newAlloc.put(this.currentLine);
        this.currentLine = newAlloc;
    }

}
