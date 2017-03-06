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
import java.util.Objects;
import org.gradle.api.tasks.AbstractExecTask;
import org.gradle.api.tasks.TaskContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CondaBuild extends AbstractExecTask<CondaBuild> {
    private static final Logger LOG = LoggerFactory.getLogger(CondaBuild.class);

    private static final String DEFAULT_GROUP = "build";
    private static final String DEFAULT_DESCRIPTION = "Builds conda package using conda-build.";

    public static CondaBuild createTask(TaskContainer tasks, SetupPython setupPython) {
        Objects.requireNonNull(tasks, "tasks must not be null");
        Objects.requireNonNull(setupPython, "setupPython must not be null");

        CondaBuild task = tasks.create("condaBuild", CondaBuild.class);
        task.setGroup(DEFAULT_GROUP);
        task.setDescription(DEFAULT_DESCRIPTION);
        task.dependsOn(setupPython);

        CleanTaskUtils.createCleanupTask(tasks, task);
        return task;
    }

    public void configureAfterEvaluate(final MinicondaExtension miniconda) {
        Objects.requireNonNull(miniconda, "miniconda must not be null");

        executable(miniconda.getBootstrapDirectory().toPath().resolve("bin/conda"));
        args(
                "build", "--quiet", "-p", getProject().getProjectDir().toPath().resolve("conda_recipe/meta.yaml"),
                "--override-channels", "--output-folder", miniconda.getCondaBuildOutputDirectory());
        args(MinicondaUtils.convertChannelsToArgs(miniconda.getChannels()));

        LOG.info("{} configured to execute {}", getName(), getCommandLine());
    }

    public CondaBuild() {
        super(CondaBuild.class);
    }

}
