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

/**
 * Project extension to configure Python build environment.
 *
 * @author pbiswal
 */
class MinicondaExtension {
    static String DEFAULT_CHANNEL = "https://repo.continuum.io"

    File bootstrapDirectoryPrefix
    File buildEnvironmentDirectory
    String minicondaVersion
    List<String> packages

    List<String> channels = [DEFAULT_CHANNEL]

    File getBootstrapDirectory() {
        return new File(bootstrapDirectoryPrefix, minicondaVersion)
    }
}
