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

import com.palantir.python.miniconda.MinicondaExtension;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.gradle.api.Action;
import org.gradle.api.internal.AbstractTask;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.TaskAction;
import org.gradle.process.ExecSpec;

/**
 * An abstract task used as a base for tasks that run commands from within the miniconda virtual environment.
 *
 * @author Ivan Atanasov
 */
public abstract class AbstractRunVenvCommand extends AbstractTask {

    private final MinicondaExtension miniconda = getProject().getExtensions().findByType(MinicondaExtension.class);
    private Path workingDir;
    private List<Object> commandPrefix = new ArrayList<>();
    private List<Object> args = new ArrayList<>();

    @TaskAction
    protected void exec() {
        getProject().exec(new Action<ExecSpec>() {
            @Override
            public void execute(ExecSpec execSpec) {
                if (workingDir != null) {
                    execSpec.workingDir(workingDir);
                }
                ArrayList<Object> commandLineList = new ArrayList<>();
                commandLineList.addAll(commandPrefix);
                commandLineList.add(getExecutable());
                commandLineList.addAll(args);
                execSpec.commandLine(commandLineList);
            }
        });
    }

    AbstractRunVenvCommand() {
        dependsOn(getProject().getTasks().findByName("setupPython"));
        if (miniconda.getOs().isWindows()) {
            commandPrefix.add("cmd");
            commandPrefix.add("/c");
        }
    }

    protected abstract Path getExecutable();

    public void setWorkingDir(Path path) {
        workingDir = path;
    }

    public void addArgs(Object... argSet) {
        this.args.addAll(Arrays.asList(argSet));
    }

    @Internal
    protected MinicondaExtension getMiniconda() {
        return miniconda;
    }

}
