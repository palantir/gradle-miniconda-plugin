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
import com.palantir.python.miniconda.tasks.CondaBuild;
import com.palantir.python.miniconda.tasks.CondaBuildCheck;
import com.palantir.python.miniconda.tasks.SetupCondaBuild;
import com.palantir.python.miniconda.tasks.SetupPython;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ResolvableDependencies;
import org.gradle.internal.os.OperatingSystem;

/**
 * Handles work for {@link Project#afterEvaluate(Action)} to act upon the {@link MinicondaExtension} configuration.
 *
 * @author mnazbro
 */
public class AfterEvaluateAction implements Action<Project> {

    private final OperatingSystem os;
    private final Configuration configuration;
    private final BootstrapPython bootstrapPython;
    private final SetupPython setupPython;
    private final SetupCondaBuild setupCondaBuild;
    private final CondaBuildCheck condaBuildCheck;
    private final CondaBuild condaBuild;

    public AfterEvaluateAction(
            OperatingSystem os,
            Configuration configuration,
            BootstrapPython bootstrapPython,
            SetupPython setupPython,
            SetupCondaBuild setupCondaBuild,
            CondaBuildCheck condaBuildCheck,
            CondaBuild condaBuild) {
        this.os = os;
        this.configuration = configuration;
        this.bootstrapPython = bootstrapPython;
        this.setupPython = setupPython;
        this.setupCondaBuild = setupCondaBuild;
        this.condaBuildCheck = condaBuildCheck;
        this.condaBuild = condaBuild;
    }

    @Override
    public final void execute(Project project) {
        MinicondaExtension miniconda = project.getExtensions().getByType(MinicondaExtension.class);
        miniconda.validate();

        addMinicondaInstallerDependency(project, miniconda);
        bootstrapPython.configureAfterEvaluate(miniconda, configuration.getSingleFile(), os);
        setupPython.configureAfterEvaluate(miniconda);
        setupCondaBuild.configureAfterEvaluate(miniconda);
        condaBuildCheck.configureAfterEvaluate(miniconda);
        condaBuild.configureAfterEvaluate(miniconda);
    }

    private void addMinicondaInstallerDependency(final Project project, final MinicondaExtension miniconda) {
        configuration.getIncoming().beforeResolve(new Action<ResolvableDependencies>() {
            @Override
            public void execute(ResolvableDependencies resolvableDependencies) {
                // Allows custom minicondaInstaller ivy dependency override
                if (configuration.getDependencies().isEmpty()) {
                    MinicondaInstaller installer = new MinicondaInstaller(os, miniconda);
                    installer.addToDependencyHandler(project.getDependencies());
                }
            }
        });
    }
}
