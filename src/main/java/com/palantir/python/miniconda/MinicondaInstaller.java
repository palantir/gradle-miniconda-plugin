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

package com.palantir.python.miniconda;

import java.util.HashMap;
import java.util.Map;
import org.gradle.api.Action;
import org.gradle.api.artifacts.DependencyArtifact;
import org.gradle.api.artifacts.ModuleDependency;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.internal.os.OperatingSystem;
import org.gradle.util.VersionNumber;

/**
 * Represents a miniconda installer and how to reference a specific one from ivy.
 *
 * @author mnazbro
 */
public class MinicondaInstaller {
    private static final VersionNumber MINIMUM_NON_LEGACY_VERSION = VersionNumber.parse("3.16.0");
    private static final String CONFIGURATION_NAME = "minicondaInstaller";

    private final OperatingSystem os;
    private final MinicondaExtension miniconda;

    public MinicondaInstaller(OperatingSystem os, MinicondaExtension miniconda) {
        this.os = os;
        this.miniconda = miniconda;
    }

    public void addToDependencyHandler(DependencyHandler handler) {
        final Map<String, String> map = new HashMap<>();
        map.put("group", "miniconda");
        map.put("name", getName());
        map.put("version", getVersion());
        ModuleDependency minicondaInstaller = (ModuleDependency) handler.add(CONFIGURATION_NAME, map);
        minicondaInstaller.artifact(new Action<DependencyArtifact>() {
            @Override
            public void execute(DependencyArtifact artifact) {
                artifact.setName(getName());
                artifact.setType(getExtension());
                artifact.setClassifier(getClassifier());
                artifact.setExtension(getExtension());
            }
        });
    }

    private String getName() {
        // Versions <= 3.16 were named "Miniconda-${version}"
        if (isLegacyMiniconda()) {
            return "Miniconda";
        }
        return "Miniconda2";
    }

    private String getExtension() {
        if (os.isWindows()) {
            return "exe";
        }
        return "sh";
    }

    private String getClassifier() {
        return getEffectiveOs() + "-" + getArch();
    }

    private String getVersion() {
        return miniconda.getMinicondaVersion();
    }

    private String getEffectiveOs() {
        if (os.isWindows()) {
            return "Windows";
        } else if (os.isMacOsX()) {
            return "MacOSX";
        } else if (os.isLinux()) {
            return "Linux";
        }
        throw new IllegalArgumentException("Miniconda only supports: [Windows, MacOSX, Linux]");
    }

    private String getArch() {
        if (os.isWindows() && System.getenv("ProgramFiles(x86)") == null) {
            return "x86";
        }
        return "x86_64";
    }

    private boolean isLegacyMiniconda() {
        return VersionNumber.parse(miniconda.getMinicondaVersion()).compareTo(MINIMUM_NON_LEGACY_VERSION) <= 0;
    }
}
