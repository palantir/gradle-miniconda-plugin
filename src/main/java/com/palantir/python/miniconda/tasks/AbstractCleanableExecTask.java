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

import org.gradle.api.tasks.AbstractExecTask;
import org.gradle.api.tasks.Delete;
import org.gradle.api.tasks.TaskContainer;

/**
 * An abstract {@link AbstractExecTask} that has support for adding a clean task.
 *
 * @author mnazbro
 */
public abstract class AbstractCleanableExecTask<T extends AbstractCleanableExecTask> extends AbstractExecTask<T> {

    public AbstractCleanableExecTask(Class<T> taskType) {
        super(taskType);
    }

    public Delete createCleanupTask(TaskContainer tasks) {
        Delete clean = tasks.create(makeCleanTaskName(), Delete.class);
        clean.setGroup(getGroup());
        clean.setDescription("Cleans for " + getName());
        clean.delete(getOutputs().getFiles());

        mustRunAfter(clean);
        return clean;
    }

    private String makeCleanTaskName() {
        String name = getName();
        if (name.isEmpty()) {
            throw new IllegalArgumentException("Cannot have an empty name");
        }
        String firstChar = name.substring(0, 1);
        return "clean" + firstChar.toUpperCase() + name.substring(1);
    }
}
