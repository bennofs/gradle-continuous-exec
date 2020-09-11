package io.github.bennofs.gradle.continuous;

import org.gradle.api.Action;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.JavaExec;
import org.gradle.api.tasks.Nested;
import org.gradle.deployment.internal.DeploymentRegistry;

import javax.inject.Inject;

/**
 * Implementation of {@link AbstractContinuousExec} that launches a java process to handle changes.
 * See base class for more information.
 */
public class ContinuousJavaExec extends AbstractContinuousExec {
    private final JavaExec javaExec;

    @Inject
    public ContinuousJavaExec(ObjectFactory objectFactory, DeploymentRegistry deploymentRegistry) {
        super(deploymentRegistry);
        javaExec = objectFactory.newInstance(JavaExec.class);
    }

    @Override
    @Internal
    Action<? super ContinuousExecSpec> getExecAction() {
        return continuousExecSpec -> {
            javaExec.setStandardInput(continuousExecSpec.getInputStream());
            javaExec.setStandardOutput(continuousExecSpec.getOutputStream());
            javaExec.exec();
        };
    }

    /**
     * @return The exec task to start the daemon.
     */
    @Nested
    public JavaExec getJavaExec() {
        return javaExec;
    }

    /**
     * Configures options for the forked process.
     *
     * @param configure Action to configure the process
     */
    public void javaExec(Action<? super JavaExec> configure) {
        configure.execute(this.javaExec);
    }
}
