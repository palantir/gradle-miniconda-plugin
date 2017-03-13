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

package com.palantir.python.miniconda.tasks;
import java.nio.file.Path;

import org.gradle.api.tasks.Input;


/**
 * A configurable task that makes it easy to run a command in the miniconda virtual environment.
 *
 * @author Ivan Atanasov
 */
public class RunVenvCommand extends AbstractRunVenvCommand {

    @Input
    private String executable;

    protected Path getExecutable() {
        return getMiniconda().getBuildEnvironmentDirectory().toPath()
                .resolve(getMiniconda().getScriptsRelativeDir()).resolve(executable);
    }

    public void setExecutable(String executable) {
        this.executable = executable;
    }
}
