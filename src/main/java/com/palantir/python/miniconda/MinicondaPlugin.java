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

package com.palantir.python.miniconda;

import com.palantir.python.miniconda.tasks.BootstrapPython;
import com.palantir.python.miniconda.tasks.SetupPython;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.repositories.IvyArtifactRepository;
import org.gradle.api.artifacts.repositories.IvyPatternRepositoryLayout;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.internal.os.OperatingSystem;

/**
 * Gradle plugin to download Miniconda and set up a Python build environment.
 *
 * @author pbiswal
 */
public class MinicondaPlugin implements Plugin<Project> {

    private static final OperatingSystem OS = OperatingSystem.current();
    private static final String EXTENSION_NAME = "miniconda";
    private static final String CONFIGURATION_NAME = "minicondaInstaller";
    private static final String IVY_REPO_URL = "https://repo.continuum.io";

    @Override
    public void apply(Project project) {
        createIvyRepository(project);

        TaskContainer tasks = project.getTasks();
        BootstrapPython bootstrapPython = BootstrapPython.createTask(tasks);
        SetupPython setupPython = SetupPython.createTask(tasks, bootstrapPython);

        MinicondaExtension miniconda = project.getExtensions().create(EXTENSION_NAME, MinicondaExtension.class);
        Configuration configuration = project.getConfigurations().create(CONFIGURATION_NAME);
        project.afterEvaluate(new AfterEvaluateAction(OS, miniconda, configuration, bootstrapPython, setupPython));
    }

    private static void createIvyRepository(Project project) {
        project.getRepositories().ivy(new Action<IvyArtifactRepository>() {
            @Override
            public void execute(IvyArtifactRepository ivy) {
                ivy.setUrl(IVY_REPO_URL);
                ivy.layout("pattern", new Action<IvyPatternRepositoryLayout>() {
                    @Override
                    public void execute(IvyPatternRepositoryLayout layout) {
                        layout.artifact("[organisation]/[module]-[revision]-[classifier].[ext]");
                    }
                });
            }
        });
    }
}
