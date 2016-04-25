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

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

/**
 * Functional tests for the Miniconda plugin.
 *
 * @author mnazario
 */
class MinicondaFunctionalTest extends Specification {
    @Rule
    final TemporaryFolder testProjectDir = new TemporaryFolder();
    File buildFile;
    File minicondaDir;

    def setup() {
        buildFile = testProjectDir.newFile('build.gradle');
        minicondaDir = testProjectDir.newFolder();

        // See https://docs.gradle.org/2.6/userguide/test_kit.html
        def pluginClasspathResource = getClass().classLoader.findResource('plugin-classpath.txt')
        if (pluginClasspathResource == null) {
            throw new IllegalStateException('Did not find plugin classpath resource, run `testClasses` build task.')
        }

        def pluginClasspath = pluginClasspathResource.readLines()
                .collect { it.replace('\\', '\\\\') } // escape backslashes in Windows paths
                .collect { "'$it'" }
                .join(', ')

        // Add the logic under test to the test build
        buildFile << """
            buildscript {
                dependencies {
                    classpath files($pluginClasspath)
                }
            }
        """
    }

    def cleanup() {
        testProjectDir.delete()
    }

    def 'only one run'() {
        buildFile << """
            apply plugin: 'com.palantir.python.miniconda'

            miniconda {
                bootstrapDirectoryPrefix = new File('$minicondaDir/bootstrap')
                buildEnvironmentDirectory = new File('$minicondaDir/env')
                minicondaVersion = '3.18.3'
                packages = ['ipython-notebook']
            }
        """

        when:
        def runner = GradleRunner.create()
                .withProjectDir(testProjectDir.getRoot())
                .withArguments(":setupPython")

        BuildResult result = runner.build();
        BuildResult secondResult = runner.build();

        then:
        result.task(":bootstrapPython").outcome == TaskOutcome.SUCCESS
        result.task(":setupPython").outcome == TaskOutcome.SUCCESS
        secondResult.task(":bootstrapPython").outcome == TaskOutcome.SKIPPED
        secondResult.task(":setupPython").outcome == TaskOutcome.UP_TO_DATE
    }
}
