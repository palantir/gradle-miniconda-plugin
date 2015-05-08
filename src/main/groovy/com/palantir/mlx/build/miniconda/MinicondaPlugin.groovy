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

package com.palantir.mlx.build.miniconda

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Exec

/**
 * Gradle plugin to download Miniconda and set up a Python build environment.
 *
 * @author pbiswal
 */
class MinicondaPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        def myExt = project.extensions.create("miniconda", MinicondaExtension.class)

        project.repositories {
            ivy {
                url "http://repo.continuum.io"
                layout "pattern", {
                    artifact "[organisation]/[module]-[revision]-[classifier].[ext]"
                }
            }
        }

        project.configurations {
            minicondaInstaller
        }

        project.afterEvaluate {
            def conf = project.configurations.minicondaInstaller
            conf.incoming.beforeResolve {
                if (conf.dependencies.empty) {
                    project.dependencies {
                        def os = System.getProperty('os.name').replaceAll(' ', '')
                        def arch = "x86_64"
                        minicondaInstaller(group: "miniconda", name: "Miniconda", version: myExt.minicondaVersion) {
                            artifact {
                                name = "Miniconda"
                                type = "sh"
                                classifier = "$os-$arch"
                                extension = "sh"
                            }
                        }
                    }
                }
            }

            project.task([type: Exec], "bootstrapPython") {
                outputs.dir(myExt.bootstrapDirectory)
                onlyIf {
                    !myExt.bootstrapDirectory.exists()
                }
                commandLine "bash", conf.singleFile, "-b", "-p", myExt.bootstrapDirectory
            }

            project.task("setupPython") {
                dependsOn "bootstrapPython"
                outputs.dir myExt.buildEnvironmentDirectory
                doFirst {
                    myExt.buildEnvironmentDirectory.deleteDir()
                }
                doLast {
                    project.exec {
                        executable new File(new File(myExt.bootstrapDirectory, "bin"), "conda")
                        args "create", "--yes", "--quiet", "-p", myExt.buildEnvironmentDirectory
                        args myExt.packages
                    }
                }
            }
        }
    }
}
