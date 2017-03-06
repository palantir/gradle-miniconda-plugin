/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.palantir.python.miniconda

import java.nio.file.Files
import java.nio.file.Paths
import org.apache.commons.io.FileUtils
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import spock.lang.Specification

/**
 * Functional tests for the Miniconda build command.
 *
 * @author jjuelich
 */
class MinicondaBuildTest extends Specification {
    private static final Logger LOG = LoggerFactory.getLogger(MinicondaBuildTest.class);

    // use a temp dir with a short name, because miniconda complains
    // when PREFIX is longer than 128 chars
    private File tempDirectory = Files.createTempDirectory("miniconda").toFile()

    def setup() {
        tempDirectory.mkdirs()
        FileUtils.copyDirectory(Paths.get("src/test/resources/test-project").toFile(), tempDirectory)
    }

    def cleanup() {
        tempDirectory.deleteDir()
    }

    def 'build package'() {
        when:
        def runner = GradleRunner.create()
                .withProjectDir(tempDirectory)
                .withArguments("--debug", ":condaBuild")
                .withPluginClasspath()

        BuildResult result = runner.build()
        LOG.info(result.getOutput())

        then:
        result.task(":setupPython").outcome == TaskOutcome.SUCCESS
        result.task(":condaBuild").outcome == TaskOutcome.SUCCESS
        new File("$tempDirectory/build/conda-output/osx-64/example-project-1.0.0-0.tar.bz2").exists()
    }
}
