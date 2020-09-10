package io.github.bennofs.gradle.continuous;

import groovy.lang.Closure;
import org.gradle.api.Action;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Nested;
import org.gradle.deployment.internal.DeploymentRegistry;
import org.gradle.process.JavaExecSpec;

import javax.inject.Inject;

/**
 * Implementation of {@link AbstractContinuousExec} that launches a java process to handle changes.
 * See base class for more information.
 */
public class ContinuousJavaExec extends AbstractContinuousExec {
    private Action<? super JavaExecSpec> javaExec;

    @Inject
    public ContinuousJavaExec(DeploymentRegistry deploymentRegistry) {
        super(deploymentRegistry);
    }

    @Override
    @Internal
    Action<? super ContinuousExecSpec> getExecAction() {
        return continuousExecSpec -> getProject().javaexec(spec -> {
            spec.setStandardInput(continuousExecSpec.getInputStream());
            spec.setStandardOutput(continuousExecSpec.getOutputStream());
            javaExec.execute(spec);
        });
    }

    @Input
    public Action<? super JavaExecSpec> getJavaExec() {
        return javaExec;
    }

    /**
     * Configure options for launching the java process.
     *
     * @param javaExec action to configure the java exec spec
     */
    public void javaExec(Action<? super JavaExecSpec> javaExec) {
        this.javaExec = javaExec;
    }

    public void javaExec(Closure<JavaExecSpec> javaExec) {
        this.javaExec = javaExec::call;
    }
}
