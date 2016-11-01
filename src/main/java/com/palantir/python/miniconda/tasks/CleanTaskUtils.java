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

import org.gradle.api.Task;
import org.gradle.api.tasks.Delete;
import org.gradle.api.tasks.TaskContainer;

/**
 * Utility class for creating clean tasks based off of a task.
 *
 * @author mnazbro
 */
public final class CleanTaskUtils {

    public static Delete createCleanupTask(TaskContainer tasks, Task task) {
        String cleanTaskName = getCleanTaskName(task);
        Delete clean = tasks.create(cleanTaskName, Delete.class);
        clean.setGroup(task.getGroup());
        clean.setDescription("Cleans for " + task.getName());
        clean.delete(task.getOutputs().getFiles());

        task.mustRunAfter(clean);
        return clean;
    }

    public static String getCleanTaskName(Task task) {
        String name = task.getName();
        if (name.isEmpty()) {
            throw new IllegalArgumentException("Cannot have an empty name");
        }
        String firstChar = name.substring(0, 1);
        return "clean" + firstChar.toUpperCase() + name.substring(1);
    }

    private CleanTaskUtils() {
        // Ignore
    }
}
