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
import com.palantir.python.miniconda.tasks.CleanTaskUtils;
import com.palantir.python.miniconda.tasks.CondaBuild;
import com.palantir.python.miniconda.tasks.SetupCondaBuild;
import com.palantir.python.miniconda.tasks.SetupPython;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.repositories.IvyArtifactRepository;
import org.gradle.api.artifacts.repositories.IvyPatternRepositoryLayout;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.internal.os.OperatingSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Gradle plugin to download Miniconda and set up a Python build environment.
 *
 * @author pbiswal
 */
public class MinicondaPlugin implements Plugin<Project> {
    private static final Logger LOG = LoggerFactory.getLogger(MinicondaPlugin.class);

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
        SetupCondaBuild setupCondaBuild = SetupCondaBuild.createTask(tasks, setupPython);
        CondaBuild condaBuild = CondaBuild.createTask(tasks, setupCondaBuild);

        Task cleanBootstrapPython = project.getTasks().getByName(CleanTaskUtils.getCleanTaskName(bootstrapPython));
        Task cleanSetupPython = project.getTasks().getByName(CleanTaskUtils.getCleanTaskName(setupPython));
        cleanBootstrapPython.dependsOn(cleanSetupPython);

        project.getExtensions().create(EXTENSION_NAME, MinicondaExtension.class, project);

        LOG.debug("MinicondaPlugin tasks created.");
        Configuration configuration = project.getConfigurations().create(CONFIGURATION_NAME);
        project.afterEvaluate(new AfterEvaluateAction(OS, configuration, bootstrapPython, setupPython, setupCondaBuild, condaBuild));
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
        LOG.debug("Added Ivy repository url: {}", IVY_REPO_URL);
    }
}
