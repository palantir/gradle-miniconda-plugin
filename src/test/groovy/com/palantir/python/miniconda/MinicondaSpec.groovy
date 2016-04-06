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

/**
 * Integration tests for the Miniconda plugin.
 *
 * @author pbiswal
 */
class MinicondaSpec extends IntegrationSpec {
    // use a temp dir with a short name, because miniconda complains
    // when PREFIX is longer than 128 chars
    private String tempDirName = "/tmp/miniconda-${new Random().nextInt(Integer.MAX_VALUE)}"
    private File tempDirectory = new File(tempDirName)

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
                bootstrapDirectoryPrefix = new File('$tempDirName/bootstrap')
                buildEnvironmentDirectory = new File('$tempDirName/env')
                minicondaVersion = '3.18.3'
                packages = ['ipython-notebook']
            }
        """

        when:
        runTasksSuccessfully('setupPython')

        then:
        new File("$tempDirName/env/bin/ipython").exists()
    }

    def 'support legacy versions'() {
        buildFile << """
            apply plugin: 'com.palantir.python.miniconda'

            miniconda {
                bootstrapDirectoryPrefix = new File('$tempDirName/bootstrap')
                buildEnvironmentDirectory = new File('$tempDirName/env')
                minicondaVersion = '3.10.1'
                packages = ['ipython-notebook']
            }
        """

        when:
        runTasksSuccessfully('setupPython')

        then:
        new File("$tempDirName/env/bin/ipython").exists()
    }

    def 'support multiple versions'() {
        buildFile << """
            apply plugin: 'com.palantir.python.miniconda'

            miniconda {
                bootstrapDirectoryPrefix = new File('$tempDirName/bootstrap')
                buildEnvironmentDirectory = new File('$tempDirName/env1')
                minicondaVersion = '3.10.1'
                packages = ['python']
            }
        """
        addSubproject("foo", """
            apply plugin: 'com.palantir.python.miniconda'

            miniconda {
                bootstrapDirectoryPrefix = new File('$tempDirName/bootstrap')
                buildEnvironmentDirectory = new File('$tempDirName/env2')
                minicondaVersion = '3.16.0'
                packages = ['python']
            }
        """)

        when:
        ExecutionResult result = runTasksSuccessfully(':setupPython', ':foo:setupPython')

        then:
        new File("$tempDirName/bootstrap/3.10.1").exists()
        new File("$tempDirName/bootstrap/3.16.0").exists()
        result.wasExecuted(':bootstrapPython')
        result.wasExecuted(':foo:bootstrapPython')
    }
}
