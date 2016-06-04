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

import nebula.test.IntegrationSpec
import nebula.test.functional.ExecutionResult

import java.nio.file.Files

/**
 * Integration tests for the Miniconda plugin.
 *
 * @author pbiswal
 */
class MinicondaIntegrationSpec extends IntegrationSpec {
    // use a temp dir with a short name, because miniconda complains
    // when PREFIX is longer than 128 chars
    private File tempDirectory = Files.createTempDirectory("miniconda").toFile()

    def setup() {
        tempDirectory.mkdirs()
    }

    def cleanup() {
        tempDirectory.deleteDir()
    }

    def 'setup and run build'() {
        buildFile << """
            apply plugin: 'com.palantir.python.miniconda'

            miniconda {
                bootstrapDirectoryPrefix = new File('$tempDirectory/bootstrap')
                buildEnvironmentDirectory = new File('$tempDirectory/env')
                minicondaVersion = '3.18.3'
                packages = ['ipython-notebook']
            }
        """

        when:
        ExecutionResult result = runTasksSuccessfully('setupPython')

        then:
        new File("$tempDirectory/env/bin/ipython").exists()
    }

    def 'support legacy versions'() {
        buildFile << """
            apply plugin: 'com.palantir.python.miniconda'

            miniconda {
                bootstrapDirectoryPrefix = new File('$tempDirectory/bootstrap')
                buildEnvironmentDirectory = new File('$tempDirectory/env')
                minicondaVersion = '3.10.1'
                packages = ['ipython-notebook']
            }
        """

        when:
        ExecutionResult result = runTasksSuccessfully('setupPython')

        then:
        new File("$tempDirectory/env/bin/ipython").exists()
    }

    def 'support multiple versions'() {
        buildFile << """
            apply plugin: 'com.palantir.python.miniconda'

            miniconda {
                bootstrapDirectoryPrefix = new File('$tempDirectory/bootstrap')
                buildEnvironmentDirectory = new File('$tempDirectory/env1')
                minicondaVersion = '3.10.1'
                packages = ['python']
            }
        """
        addSubproject("foo", """
            apply plugin: 'com.palantir.python.miniconda'

            miniconda {
                bootstrapDirectoryPrefix = new File('$tempDirectory/bootstrap')
                buildEnvironmentDirectory = new File('$tempDirectory/env2')
                minicondaVersion = '3.16.0'
                packages = ['python']
            }
        """)

        when:
        ExecutionResult result = runTasksSuccessfully(':setupPython', ':foo:setupPython')

        then:
        new File("$tempDirectory/bootstrap/3.10.1").exists()
        new File("$tempDirectory/bootstrap/3.16.0").exists()
        result.wasExecuted(':bootstrapPython')
        result.wasExecuted(':foo:bootstrapPython')
    }
}
