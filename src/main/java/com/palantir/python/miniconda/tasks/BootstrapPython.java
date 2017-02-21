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
import java.io.File;
import java.util.Objects;
import org.gradle.api.Action;
import org.gradle.api.Task;
import org.gradle.api.specs.Spec;
import org.gradle.api.tasks.AbstractExecTask;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.internal.os.OperatingSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Task that installs Miniconda for the bootstrap step of creating Conda environments.
 *
 * @author mnazbro
 */
public class BootstrapPython extends AbstractExecTask<BootstrapPython> {
    private static final Logger LOG = LoggerFactory.getLogger(BootstrapPython.class);

    private static final String DEFAULT_GROUP = "build";
    private static final String DEFAULT_DESCRIPTION = "Installs a conda env with specified packages.";

    public static BootstrapPython createTask(TaskContainer tasks) {
        Objects.requireNonNull(tasks, "tasks must not be null");

        BootstrapPython task = tasks.create("bootstrapPython", BootstrapPython.class);
        task.setGroup(DEFAULT_GROUP);
        task.setDescription(DEFAULT_DESCRIPTION);
        CleanTaskUtils.createCleanupTask(tasks, task);
        return task;
    }

    public BootstrapPython() {
        super(BootstrapPython.class);
    }

    public void configureAfterEvaluate(final MinicondaExtension miniconda, File condaInstaller, OperatingSystem os) {
        Objects.requireNonNull(miniconda, "miniconda must not be null");
        Objects.requireNonNull(condaInstaller, "condaInstaller must not be null");
        Objects.requireNonNull(os, "os must not be null");

        getInputs().property("version", miniconda.getMinicondaVersion());
        getInputs().property("directory", miniconda.getBootstrapDirectoryPrefix());
        getOutputs().dir(miniconda.getBootstrapDirectory());

        if (os.isWindows()) {
            executable("cmd");
            args("/c");
            args(condaInstaller);
            args("/InstallationType=JustMe");
            args("/AddToPath=0");
            args("/RegisterPython=0");
            args("/S");
            args("/D=" + miniconda.getBootstrapDirectory().getAbsolutePath());
        } else {
            executable("bash");
            args(condaInstaller, "-b", "-p", miniconda.getBootstrapDirectory());
        }
        doFirst(new Action<Task>() {
            @Override
            public void execute(Task task) {
                if (!miniconda.getBootstrapDirectoryPrefix().mkdirs()) {
                    getProject().delete(miniconda.getBootstrapDirectory());
                    LOG.debug("Deleted BootstrapPython dir: {}", miniconda.getBootstrapDirectory());
                }
            }
        });
        onlyIf(new Spec<Task>() {
            @Override
            public boolean isSatisfiedBy(Task task) {
                boolean directoryDoesNotExist = !miniconda.getBootstrapDirectory().exists();
                LOG.debug("BootstrapPython directory exists: {}", !directoryDoesNotExist);
                return directoryDoesNotExist;
            }
        });
        LOG.info("{} configured to execute {}", getName(), getCommandLine());
    }
}
