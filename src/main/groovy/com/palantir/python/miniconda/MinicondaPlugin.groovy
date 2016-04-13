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

package com.palantir.python.miniconda

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Delete
import org.gradle.api.tasks.Exec
import org.gradle.util.VersionNumber

import java.nio.file.Paths

/**
 * Gradle plugin to download Miniconda and set up a Python build environment.
 *
 * @author pbiswal
 */
class MinicondaPlugin implements Plugin<Project> {
    def os = System.getProperty('os.name').replaceAll(' ', '')

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
                    def myExtension = "sh"
                    def arch = "x86_64"
                    if (os.contains("Windows")) {
                        os = "Windows"
                        myExtension = "exe"
                        if (System.getenv("ProgramFiles(x86)") == null) {
                            arch = "x86"
                        }
                    }
                    def myName = "Miniconda2"
                    // versions <= 3.16 were named "Miniconda-${version}"
                    if (VersionNumber.parse(myExt.minicondaVersion) <= VersionNumber.parse("3.16")) {
                        myName = "Miniconda"
                    }
                    project.dependencies {
                        minicondaInstaller(group: 'miniconda', name: myName, version: myExt.minicondaVersion) {
                            artifact {
                                name = myName
                                type = myExtension
                                classifier = "$os-$arch"
                                extension = myExtension
                            }
                        }
                    }
                }
            }

            def bootstrapPython = project.task([type: Exec], "bootstrapPython") {
                inputs.property "version", myExt.minicondaVersion
                inputs.property "directory", myExt.bootstrapDirectoryPrefix
                outputs.dir myExt.bootstrapDirectory

                if (os.contains("Windows")) {
                    commandLine conf.singleFile, "-b", "-p", myExt.bootstrapDirectory
                } else {
                    commandLine "bash", conf.singleFile, "-b", "-p", myExt.bootstrapDirectory
                }
                doFirst {
                    if (!myExt.bootstrapDirectoryPrefix.exists()) {
                        myExt.bootstrapDirectoryPrefix.mkdirs()
                    }
                }
            }

            project.task([type: Delete], 'cleanBootstrapPython') {
                delete bootstrapPython.outputs.files
            }

            def setupPython = project.task([type: Exec], 'setupPython') {
                dependsOn bootstrapPython
                inputs.property "packages", myExt.packages
                outputs.dir myExt.buildEnvironmentDirectory

                executable Paths.get("$myExt.bootstrapDirectory", 'bin', 'conda')
                args "create", "--yes", "--quiet", "-p", myExt.buildEnvironmentDirectory
                args myExt.packages
            }

            project.task([type: Delete], 'cleanSetupPython') {
                delete setupPython.outputs.files
            }
        }
    }
}
