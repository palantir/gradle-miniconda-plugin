/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.palantir.python.miniconda.tasks;

import com.palantir.python.miniconda.MinicondaExtension;
import com.palantir.python.miniconda.MinicondaUtils;
import java.util.Objects;
import org.gradle.api.Action;
import org.gradle.api.Task;
import org.gradle.api.tasks.AbstractExecTask;
import org.gradle.api.tasks.TaskContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Task which creates a Conda Python environment.
 *
 * @author mnazbro
 */
public class SetupPython extends AbstractExecTask<SetupPython> {
    private static final Logger LOG = LoggerFactory.getLogger(SetupPython.class);

    private static final String DEFAULT_GROUP = "build";
    private static final String DEFAULT_DESCRIPTION = "Installs a conda env with specified packages.";

    public static SetupPython createTask(TaskContainer tasks, ConfigureRootCondaEnv configureRootCondaEnv) {
        Objects.requireNonNull(tasks, "tasks must not be null");
        Objects.requireNonNull(configureRootCondaEnv, "bootstrapPython must not be null");

        SetupPython task = tasks.create("setupPython", SetupPython.class);
        task.setGroup(DEFAULT_GROUP);
        task.setDescription(DEFAULT_DESCRIPTION);
        task.dependsOn(configureRootCondaEnv);

        CleanTaskUtils.createCleanupTask(tasks, task);
        return task;
    }

    public SetupPython() {
        super(SetupPython.class);
    }

    public void configureAfterEvaluate(final MinicondaExtension miniconda) {
        Objects.requireNonNull(miniconda, "miniconda must not be null");

        getInputs().property("packages", miniconda.getPackages());
        getOutputs().dir(miniconda.getBuildEnvironmentDirectory());

        executable(miniconda.getBootstrapDirectory().toPath().resolve("bin/conda"));
        args("create", "--yes", "--quiet", "-p", miniconda.getBuildEnvironmentDirectory());
        args("--override-channels");
        args(MinicondaUtils.convertChannelsToArgs(miniconda.getChannels()));
        args(miniconda.getPackages());

        doFirst(new Action<Task>() {
            @Override
            public void execute(Task task) {
                if (miniconda.getBuildEnvironmentDirectory().exists()) {
                    getProject().delete(miniconda.getBuildEnvironmentDirectory());
                    LOG.debug("Deleted BuildEnvironmentDir dir: {}", miniconda.getBuildEnvironmentDirectory());
                }
            }
        });
        LOG.info("{} configured to execute {}", getName(), getCommandLine());
    }
}
