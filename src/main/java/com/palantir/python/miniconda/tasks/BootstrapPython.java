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
import org.gradle.api.Action;
import org.gradle.api.Task;
import org.gradle.api.specs.Spec;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.internal.os.OperatingSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Task that installs Miniconda for the bootstrap step of creating Conda environments.
 *
 * @author mnazbro
 */
public class BootstrapPython extends AbstractCleanableExecTask<BootstrapPython> {
    private static final Logger LOG = LoggerFactory.getLogger(BootstrapPython.class);

    private static final String DEFAULT_GROUP = "build";
    private static final String DEFAULT_DESCRIPTION = "Installs a conda env with specified packages.";

    public static BootstrapPython createTask(TaskContainer tasks) {
        BootstrapPython task = tasks.create("bootstrapPython", BootstrapPython.class);
        task.setGroup(DEFAULT_GROUP);
        task.setDescription(DEFAULT_DESCRIPTION);
        task.createCleanupTask(tasks);
        return task;
    }

    public BootstrapPython() {
        super(BootstrapPython.class);
    }

    public void configureAfterEvaluate(final MinicondaExtension miniconda, File condaInstaller, OperatingSystem os) {
        getInputs().property("version", miniconda.getMinicondaVersion());
        getInputs().property("directory", miniconda.getBootstrapDirectoryPrefix());
        getOutputs().dir(miniconda.getBootstrapDirectory());

        if (os.isWindows()) {
            executable(condaInstaller);
        } else {
            executable("bash");
            args(condaInstaller);
        }
        args("-b", "-p", miniconda.getBootstrapDirectory());
        doFirst(new Action<Task>() {
            @Override
            public void execute(Task task) {
                if (!miniconda.getBootstrapDirectoryPrefix().mkdirs()) {
                    getProject().delete(miniconda.getBootstrapDirectory());
                }
            }
        });
        onlyIf(new Spec<Task>() {
            @Override
            public boolean isSatisfiedBy(Task task) {
                return !miniconda.getBootstrapDirectory().exists();
            }
        });
        LOG.info("{} configured to execute {}", getName(), getCommandLine());
    }
}
