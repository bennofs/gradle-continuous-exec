package io.github.bennofs.gradle.continuous;

import org.gradle.api.Action;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.TaskAction;
import org.gradle.deployment.internal.DeploymentRegistry;
import org.gradle.work.FileChange;
import org.gradle.work.Incremental;
import org.gradle.work.InputChanges;

import javax.inject.Inject;
import java.util.ArrayList;

/**
 * A continuous exec task extends the standard Exec task from gradle with the ability to react to changes
 * in a continuous build.
 *
 * The task starts a process in the background. The process the communicates over a simple protocol via stdin and stdout.
 * After finishing start up, the process must write the line "ok" (without quotes) to stdout.
 *
 * Whenever a change is detected in the task dependencies or watched files,
 * a line consisting of the null-separated full paths of changed files is written to the process.
 * The process should then respond with a line "ok" (without quotes).
 *
 * Subclasses specify how to spawn the process. See for example {@link ContinuousJavaExec} for spawning a java task.
 */
public abstract class AbstractContinuousExec extends DefaultTask {
    private final DeploymentRegistry deploymentRegistry;

    private final ConfigurableFileCollection watch;

    @Inject
    public AbstractContinuousExec(DeploymentRegistry deploymentRegistry) {
        this.deploymentRegistry = deploymentRegistry;
        this.watch = this.getProject().files();

        // the task should execute at least once in a continuous run, even if all inputs are up-to-date
        // so we add a up-to-date check that returns false if no deployment exists yet
        this.getOutputs().upToDateWhen(task ->
                deploymentRegistry.get(task.getPath(), ContinuousDeploymentHandle.class) != null);
    }

    /**
     * An action that is executed on a background thread when the continuous exec task is started.
     *
     * @return The action to execute
     */
    @Internal
    abstract Action<? super ContinuousExecSpec> getExecAction();

    @TaskAction
    public void execute(InputChanges changes) throws InterruptedException {
        String deploymentId = getPath();
        ContinuousDeploymentHandle handle = deploymentRegistry.get(deploymentId, ContinuousDeploymentHandle.class);

        if (handle == null) {
            final ContinuousDeploymentHandle deployed = deploymentRegistry.start(
                    deploymentId, DeploymentRegistry.ChangeBehavior.NONE, ContinuousDeploymentHandle.class, getExecAction());
        } else {
            final ArrayList<FileChange> watchChanges = new ArrayList<>();
            changes.getFileChanges(watch).forEach(watchChanges::add);
            handle.reload(watchChanges);
        }
    }

    /**
     * Configure the paths that are watched for changes.
     * Only paths that are contained in the file collection will be passed as "changed files" to the process.
     * Directories in this collection are watched recursively.
     *
     * @return Collection of watched files and directories
     */
    @InputFiles
    @Incremental
    public ConfigurableFileCollection getWatch() {
        return watch;
    }
}
