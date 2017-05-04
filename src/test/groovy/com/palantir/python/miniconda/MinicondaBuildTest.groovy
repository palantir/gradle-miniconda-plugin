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

import static groovy.test.GroovyAssert.shouldFail

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings
import java.nio.file.Files
import java.nio.file.Paths
import org.apache.commons.io.FileUtils
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.gradle.testkit.runner.UnexpectedBuildFailure
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import spock.lang.Specification

/**
 * Functional tests for the Miniconda build command.
 *
 * @author jakobjuelich
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
                .forwardOutput()
                .withProjectDir(tempDirectory)
                .withArguments("--info", "--stacktrace", ":condaBuild")
                .withPluginClasspath()

        BuildResult result = runner.build()
        LOG.info(result.getOutput())

        then:
        result.task(":condaBuild").outcome == TaskOutcome.SUCCESS

        FileUtils.listFiles(
                tempDirectory.toPath().resolve("build/output").toFile(), ["tar.bz2"] as String[], true).size() == 1
    }

    @SuppressFBWarnings
    def 'build with broken recipe should fail'() {
        when:
        // `conda build` exits with 0, even if meta.yaml doesn't exist. CondaBuildCheck should catch this.
        tempDirectory.toPath().resolve("conda_recipe/meta.yaml").toFile().delete();

        def runner = GradleRunner.create()
                .forwardOutput()
                .withProjectDir(tempDirectory)
                .withArguments("--info", "--stacktrace", ":condaBuild")
                .withPluginClasspath()

        shouldFail(UnexpectedBuildFailure) {
            runner.build()
        }

        then:
        1 == 1
    }

    def 'second setupCondaBuild up to date'() {
        when:
        def runner = GradleRunner.create()
                .forwardOutput()
                .withProjectDir(tempDirectory)
                .withArguments("--info", "--stacktrace", ":setupCondaBuild")
                .withPluginClasspath()
        BuildResult firstResult = runner.build()
        LOG.info(firstResult.getOutput())

        BuildResult secondResult = runner.build()
        LOG.info(secondResult.getOutput())

        then:
        firstResult.task(":setupCondaBuild").outcome == TaskOutcome.SUCCESS
        secondResult.task(":setupCondaBuild").outcome == TaskOutcome.UP_TO_DATE
    }
}
