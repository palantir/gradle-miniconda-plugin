// Copyright 2015 Palantir Technologies
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.palantir.python.miniconda;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.DependencyArtifact;
import org.gradle.api.artifacts.ModuleDependency;
import org.gradle.api.artifacts.ResolvableDependencies;
import org.gradle.api.artifacts.repositories.IvyArtifactRepository;
import org.gradle.api.artifacts.repositories.IvyPatternRepositoryLayout;
import org.gradle.api.specs.Spec;
import org.gradle.api.tasks.Delete;
import org.gradle.api.tasks.Exec;
import org.gradle.util.VersionNumber;

/**
 * Gradle plugin to download Miniconda and set up a Python build environment.
 *
 * @author pbiswal
 */
public class MinicondaPlugin implements Plugin<Project> {
    private final String OS = System.getProperty("os.name").replaceAll(" ", "");

    @Override
    public void apply(Project project) {
        final MinicondaExtension miniconda = project.getExtensions().create("miniconda", MinicondaExtension.class);

        project.getRepositories().ivy(new Action<IvyArtifactRepository>() {
            @Override
            public void execute(IvyArtifactRepository ivy) {
                ivy.setUrl("https://repo.continuum.io");
                ivy.layout("pattern", new Action<IvyPatternRepositoryLayout>() {
                    @Override
                    public void execute(IvyPatternRepositoryLayout layout) {
                        layout.artifact("[organisation]/[module]-[revision]-[classifier].[ext]");
                    }
                });
            }
        });
        project.getConfigurations().create("minicondaInstaller");

        final Exec bootstrapPython = (Exec) project.task(Collections.singletonMap("type", Exec.class), "bootstrapPython");
        bootstrapPython.setGroup("build");
        bootstrapPython.setDescription("Installs a vanilla miniconda root environment.");
        bootstrapPython.mustRunAfter("cleanBootstrapPython");

        final Exec setupPython = (Exec) project.task(Collections.singletonMap("type", Exec.class), "setupPython");
        setupPython.setGroup("build");
        setupPython.setDescription("Installs a conda env with specified packages.");
        setupPython.mustRunAfter("cleanSetupPython");

        Delete cleanSetupPython = (Delete) project.task(Collections.singletonMap("type", Delete.class), "cleanSetupPython");
        cleanSetupPython.setGroup("build");
        cleanSetupPython.setDescription("Removes the conda env with specified packages.");
        cleanSetupPython.delete(setupPython.getOutputs().getFiles());

        Delete cleanBootstrapPython = (Delete) project.task(Collections.singletonMap("type", Delete.class), "cleanBootstrapPython");
        cleanBootstrapPython.setGroup("build");
        cleanBootstrapPython.setDescription("Removes the root miniconda environment.");
        cleanBootstrapPython.delete(bootstrapPython.getOutputs().getFiles());

        project.afterEvaluate(new Action<Project>() {
            @Override
            public void execute(final Project project) {
                final Configuration conf = project.getConfigurations().getByName("minicondaInstaller");
                conf.getIncoming().beforeResolve(new Action<ResolvableDependencies>() {
                    @Override
                    public void execute(ResolvableDependencies resolvableDependencies) {
                        if (conf.getDependencies().isEmpty()) {
                            final String myExtension;
                            final String arch;
                            final String effectiveOs;
                            if (OS.contains("Windows")) {
                                effectiveOs = "Windows";
                                myExtension = "exe";
                            } else {
                                effectiveOs = OS;
                                myExtension = "sh";
                            }
                            if (OS.contains("Windows") && System.getenv("ProgramFiles(x86)") == null) {
                                arch = "x86";
                            } else {
                                arch = "x86_64";
                            }
                            final String myName;
                            // versions <= 3.16 were named "Miniconda-${version}"
                            if (VersionNumber.parse(miniconda.getMinicondaVersion()).compareTo(VersionNumber.parse("3.16")) <= 0) {
                                myName = "Miniconda";
                            } else {
                                myName = "Miniconda2";
                            }
                            Map<String, String> map = new HashMap<>();
                            map.put("group", "miniconda");
                            map.put("name", myName);
                            map.put("version", miniconda.getMinicondaVersion());
                            Dependency minicondaInstaller = project.getDependencies().add("minicondaInstaller", map);
                            ModuleDependency dep = (ModuleDependency) minicondaInstaller;
                            dep.artifact(new Action<DependencyArtifact>() {
                                @Override
                                public void execute(DependencyArtifact dependencyArtifact) {
                                    dependencyArtifact.setName(myName);
                                    dependencyArtifact.setType(myExtension);
                                    dependencyArtifact.setClassifier(effectiveOs + "-" + arch);
                                    dependencyArtifact.setExtension(myExtension);
                                }
                            });
                        }
                    }
                });
                bootstrapPython.setProperty("version", miniconda.getMinicondaVersion());
                bootstrapPython.setProperty("directory", miniconda.getBootstrapDirectoryPrefix());
                bootstrapPython.setWorkingDir(miniconda.getBootstrapDirectory());

                if (OS.contains("Windows")) {
                    bootstrapPython.executable(conf.getSingleFile());
                } else {
                    bootstrapPython.executable("bash");
                    bootstrapPython.args(conf.getSingleFile());
                }
                bootstrapPython.args("-b", "-p", miniconda.getBootstrapDirectory());
                bootstrapPython.doFirst(new Action<Task>() {
                    @Override
                    public void execute(Task task) {
                        miniconda.getBootstrapDirectoryPrefix().mkdirs();
                        if (miniconda.getBootstrapDirectory().exists()) {
                            project.delete(miniconda.getBootstrapDirectory());
                        }
                    }
                });
                bootstrapPython.onlyIf(new Spec<Task>() {
                    @Override
                    public boolean isSatisfiedBy(Task task) {
                        return !miniconda.getBootstrapDirectory().exists();
                    }
                });
                setupPython.getInputs().property("packages", miniconda.getPackages());
                setupPython.getOutputs().dir(miniconda.getBuildEnvironmentDirectory());
                setupPython.executable(miniconda.getBootstrapDirectory().toPath().resolve("bin/conda"));
                setupPython.args("create", "--yes", "--quiet", "-p", miniconda.getBuildEnvironmentDirectory());
                setupPython.args("--override-channels");
                setupPython.args(convertChannelsToArgs(miniconda.getChannels()));
                setupPython.args(miniconda.getPackages());

                setupPython.doFirst(new Action<Task>() {
                    @Override
                    public void execute(Task task) {
                        if (miniconda.getBuildEnvironmentDirectory().exists()) {
                            project.delete(miniconda.getBuildEnvironmentDirectory());
                        }
                    }
                });
            }
        });
    }

    private static List<String> convertChannelsToArgs(List<String> channels) {
        List<String> args = new ArrayList<>();
        for (String channel : channels) {
            args.add("--channel");
            args.add(channel);
        }
        return args;
    }
}
