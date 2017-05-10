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

package com.palantir.python.miniconda

import java.nio.file.Files
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import spock.lang.Specification

/**
 * Functional tests for the Miniconda plugin.
 *
 * @author mnazbro
 */
class MinicondaFunctionalTest extends Specification {
    // use a temp dir with a short name, because miniconda complains
    // when PREFIX is longer than 128 chars
    private File tempDirectory = Files.createTempDirectory("miniconda").toFile()

    File buildFile;
    File minicondaDir;

    def setup() {
        tempDirectory.mkdirs()
        buildFile = new File(tempDirectory, 'build.gradle')
        minicondaDir = new File(tempDirectory, 'miniconda')
    }

    def cleanup() {
        tempDirectory.deleteDir()
    }

    def 'only one run'() {
        buildFile << """
            plugins {
                id 'com.palantir.python.miniconda'
            }

            miniconda {
                bootstrapDirectoryPrefix = new File('$minicondaDir/bootstrap')
                buildEnvironmentDirectory = new File('$minicondaDir/env')
                minicondaVersion = '3.18.3'
                condaBuildVersion = '2.1.9'
                packages = ['ipython-notebook']
                channels = ["${TestConstants.CHANNEL}"]
            }
        """

        when:
        def runner = GradleRunner.create()
                .forwardOutput()
                .withProjectDir(tempDirectory)
                .withArguments("--info", "--stacktrace", ":setupPython")
                .withPluginClasspath()

        BuildResult result = runner.build();
        BuildResult secondResult = runner.build();

        then:
        result.task(":bootstrapPython").outcome == TaskOutcome.SUCCESS
        result.task(":setupPython").outcome == TaskOutcome.SUCCESS
        secondResult.task(":bootstrapPython").outcome == TaskOutcome.SKIPPED
        secondResult.task(":setupPython").outcome == TaskOutcome.UP_TO_DATE
    }
}
