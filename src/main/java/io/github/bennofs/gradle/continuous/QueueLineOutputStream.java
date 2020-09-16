package io.github.bennofs.gradle.continuous;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.BlockingQueue;

/**
 * An OutputStream that streams complete lines to a queue.
 */
public class QueueLineOutputStream extends OutputStream {
    private static final Logger LOGGER = LoggerFactory.getLogger(QueueLineOutputStream.class);

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
            final String line = StandardCharsets.UTF_8.decode(currentLine).toString();
            LOGGER.debug("received line from continuous worker: {}", line);
            try {
                commandQueue.put(line);
            } catch (InterruptedException e) {
                throw new IOException("queue put interrupted", e);
            }
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
        if (currentLine.hasRemaining()) return;

        final ByteBuffer newAlloc = ByteBuffer.allocate((int)(currentLine.capacity() * 1.5));
        this.currentLine.flip();
        newAlloc.put(this.currentLine);
        this.currentLine = newAlloc;
    }

}
