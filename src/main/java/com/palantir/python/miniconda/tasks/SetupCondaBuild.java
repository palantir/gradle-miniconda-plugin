/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
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
import java.nio.file.Path;
import java.util.Objects;
import org.gradle.api.Task;
import org.gradle.api.specs.Spec;
import org.gradle.api.tasks.AbstractExecTask;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.process.internal.ExecAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Installs conda-build.
 *
 * This can not be installed in an environment so we need a separate task for it.
 *
 * Created by jakobjuelich on 3/7/17.
 */
public class SetupCondaBuild extends AbstractExecTask<SetupCondaBuild> {

    private static final Logger LOG = LoggerFactory.getLogger(SetupCondaBuild.class);

    private static final String DEFAULT_GROUP = "build";
    private static final String DEFAULT_DESCRIPTION = "Installs conda-build.";

    public static SetupCondaBuild createTask(TaskContainer tasks, BootstrapPython bootstrapPython) {
        Objects.requireNonNull(tasks, "tasks must not be null");
        Objects.requireNonNull(bootstrapPython, "bootstrapPython must not be null");

        SetupCondaBuild task = tasks.create("setupCondaBuild", SetupCondaBuild.class);
        task.setGroup(DEFAULT_GROUP);
        task.setDescription(DEFAULT_DESCRIPTION);
        task.dependsOn(bootstrapPython);

        CleanTaskUtils.createCleanupTask(tasks, task);
        return task;
    }

    public SetupCondaBuild() {
        super(SetupCondaBuild.class);
    }

    public final void configureAfterEvaluate(final MinicondaExtension miniconda) {
        Objects.requireNonNull(miniconda, "miniconda must not be null");

        final Path condaExec = miniconda.getBootstrapDirectory().toPath().resolve("bin/conda");
        executable(condaExec);
        args("install", "--quiet", "--yes", "conda-build");
        args("--override-channels");
        args(MinicondaUtils.convertChannelsToArgs(miniconda.getChannels()));

        LOG.info("{} configured to execute {}", getName(), getCommandLine());

        this.getOutputs().upToDateWhen(new Spec<Task>() {
            @Override
            public boolean isSatisfiedBy(Task task) {
                ExecAction execAction = SetupCondaBuild.this.getExecActionFactory().newExecAction();
                execAction.executable(condaExec);
                execAction.args("build", "-V"); // will error if build is not installed, otherwise just print stuff
                execAction.setIgnoreExitValue(true);

                return 0 == execAction.execute().getExitValue();
            }
        });
    }
}
