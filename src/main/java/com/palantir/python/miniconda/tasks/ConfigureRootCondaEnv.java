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
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Objects;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.TaskContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigureRootCondaEnv extends DefaultTask {

    private static final Logger LOG = LoggerFactory.getLogger(ConfigureRootCondaEnv.class);

    private static final String DEFAULT_GROUP = "build";
    private static final String DEFAULT_DESCRIPTION = "Configures the root conda env (writes a .condarc).";

    private MinicondaExtension miniconda;

    public static ConfigureRootCondaEnv createTask(TaskContainer tasks, BootstrapPython bootstrapPython) {
        Objects.requireNonNull(tasks, "tasks must not be null");
        Objects.requireNonNull(bootstrapPython, "bootstrapPython must not be null");

        ConfigureRootCondaEnv task = tasks.create("configureRootCondaEnv", ConfigureRootCondaEnv.class);
        task.setGroup(DEFAULT_GROUP);
        task.setDescription(DEFAULT_DESCRIPTION);
        task.dependsOn(bootstrapPython);

        CleanTaskUtils.createCleanupTask(tasks, task);
        return task;
    }

    @OutputFile
    public File getOutputFile() {
        return new File(miniconda.getBootstrapDirectoryPrefix(), ".condarc");
    }

    @TaskAction
    public void createCondaRcFile() {
        LOG.info("writing a condarc file to {}", getOutputFile().getAbsolutePath());

        String condaRc = "channels: []\ndefault_channels: []\n";

        try {
            Files.write(getOutputFile().toPath(), condaRc.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void configureAfterEvaluate(final MinicondaExtension minicondaExtension) {
        this.miniconda = minicondaExtension;
    }
}
