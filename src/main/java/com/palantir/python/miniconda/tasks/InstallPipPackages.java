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
import java.util.List;
import org.gradle.api.Project;
import org.gradle.api.tasks.AbstractExecTask;

/**
 * Installs pip packages into the Python environment.
 *
 * @author mnazbro
 */
public class InstallPipPackages extends AbstractExecTask<InstallPipPackages> {

    public static InstallPipPackages createTask(
            Project project,
            MinicondaExtension miniconda,
            SetupPython setupPython,
            List<String> requirements) {
        InstallPipPackages task = project.getTasks().create("installPipPackages", InstallPipPackages.class);
        task.dependsOn(setupPython);
        task.executable(miniconda.getBuildEnvironmentDirectory().toPath().resolve("bin/pip"));
        task.args("install");
        task.args(requirements);

        CleanTaskUtils.createCleanupTask(project.getTasks(), task);
        return task;
    }

    public InstallPipPackages() {
        super(InstallPipPackages.class);
    }
}
