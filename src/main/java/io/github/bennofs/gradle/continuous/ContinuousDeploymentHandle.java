package io.github.bennofs.gradle.continuous;

import org.gradle.api.Action;
import org.gradle.deployment.internal.Deployment;
import org.gradle.deployment.internal.DeploymentHandle;
import org.gradle.work.FileChange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Collection;
import java.util.concurrent.SynchronousQueue;
import java.util.stream.Collectors;

/**
 * The deployment handle is for gradle to manage the continuous execution task.
 * It is created once the first time and then stays alive for futher executions of a continuous task.
 */
public class ContinuousDeploymentHandle implements DeploymentHandle {
    private static final Logger LOGGER = LoggerFactory.getLogger(ContinuousDeploymentHandle.class);
    private final Action<? super ContinuousExecSpec> execAction;

    private Runner runner;

    @Inject
    public ContinuousDeploymentHandle(Action<? super ContinuousExecSpec> execAction) {
        this.execAction = execAction;

    }

    @Override
    public boolean isRunning() {
        return true;
    }

    @Override
    public void start(Deployment deployment) {
        System.err.println("START");
        this.runner = new Runner(this.execAction);
        this.runner.start();
        try {
            this.runner.waitForOk();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        LOGGER.info("continuous run task start");
    }

    @Override
    public void stop() {
        System.out.println("stop deployment");
        if (this.runner != null) {
            this.runner.interrupt();
            try {
                this.runner.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void reload(Collection<FileChange> changes) throws InterruptedException {
        LOGGER.info("continuous run task reload");
        runner.sendRequest(changes.stream().map(FileChange::getNormalizedPath).collect(Collectors.joining("\0")));
    }

    private static class Runner extends Thread {
        private final Action<? super ContinuousExecSpec> execAction;
        private final SynchronousQueue<String> requestQueue = new SynchronousQueue<>();
        private final SynchronousQueue<String> responseQueue = new SynchronousQueue<>();

        private Runner(Action<? super ContinuousExecSpec> execAction) {
            super("continuous run thread");
            this.execAction = execAction;
        }

        public void sendRequest(String request) throws InterruptedException {
            requestQueue.put(request);
            waitForOk();
        }

        public void waitForOk() throws InterruptedException {
            String r = responseQueue.take();
            if (!r.equals("ok")) {
                throw new RuntimeException("invalid response from continuous exec process, expected ok but got: " + r);
            }
        }

        @Override
        public void run() {
            final ContinuousExecSpec clientSpec = new ContinuousExecSpec(
                    new QueueLineInputStream(requestQueue),
                    new QueueLineOutputStream(responseQueue)
            );
            try {
                this.execAction.execute(clientSpec);
            } catch (RuntimeException e) {
                final Throwable cause = e.getCause();
                if (cause instanceof InterruptedException) return;

                throw e;
            }
        }
    }


}
