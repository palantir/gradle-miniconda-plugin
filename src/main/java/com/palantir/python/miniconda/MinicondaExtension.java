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

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.gradle.api.Project;
import org.gradle.internal.os.OperatingSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Project extension to configureAfterEvaluate Python build environment.
 *
 * @author pbiswal
 */
public class MinicondaExtension {
    private static final Logger LOG = LoggerFactory.getLogger(MinicondaExtension.class);

    private static final String DEFAULT_CHANNEL = "https://repo.continuum.io/pkgs/free";
    private static final File DEFAULT_BOOTSTRAP_DIRECTORY_PREFIX =
            new File(System.getProperty("user.home"), ".miniconda-bootstrap");
    private static final String DEFAULT_BUILD_ENVIRONMENT_DIRECTORY = "build/miniconda";
    private static final String DEFAULT_META_YAML_DIR = "conda_recipe/";
    private static final int DEFAULT_PYTHON_VERSION = 2;

    private final Project project;

    private String minicondaVersion;
    private String condaBuildVersion;
    private int pythonVersion = DEFAULT_PYTHON_VERSION;
    private File bootstrapDirectoryPrefix = DEFAULT_BOOTSTRAP_DIRECTORY_PREFIX;
    private File buildEnvironmentDirectory = null;
    private File buildOutputDirectory = null;
    private Path metaYaml = null;
    private List<String> packages = new ArrayList<>();
    private List<String> channels = new ArrayList<>(Collections.singletonList(DEFAULT_CHANNEL));
    private OperatingSystem os = OperatingSystem.current();

    public MinicondaExtension(Project project) {
        this.project = project;
    }

    public final void validate() {
        Objects.requireNonNull(minicondaVersion, "miniconda.minicondaVersion must be set.");
        Objects.requireNonNull(bootstrapDirectoryPrefix, "miniconda.bootstrapDirectoryPrefix must not be null.");
        Objects.requireNonNull(packages, "miniconda.packages must not be null.");
        if (packages.isEmpty()) {
            throw new IllegalArgumentException("miniconda.packages must contain at least one requirement.");
        }
        Objects.requireNonNull(channels, "miniconda.channels must not be null.");
        if (channels.isEmpty()) {
            throw new IllegalArgumentException("miniconda.channels must contain at least one channel.");
        }
        if (pythonVersion <= 1) {
            throw new IllegalArgumentException("miniconda.pythonVersion must be 2 or greater.");
        } else if (pythonVersion > 3) {
            LOG.warn("The miniconda-gradle-plugin was designed when only Python 3 existed. Any version greater than 3"
                    + " is allowed, but not tested or supported.");
        }
    }

    public final File getBootstrapDirectory() {
        return bootstrapDirectoryPrefix.toPath()
                .resolve("python-" + pythonVersion)
                .resolve("miniconda-" + minicondaVersion)
                .toFile();
    }

    public final File getBootstrapDirectoryPrefix() {
        return bootstrapDirectoryPrefix;
    }

    public final void setBootstrapDirectoryPrefix(String bootstrapDirectoryPrefix) {
        setBootstrapDirectoryPrefix(new File(bootstrapDirectoryPrefix));
    }

    public final void setBootstrapDirectoryPrefix(Path bootstrapDirectoryPrefix) {
        setBootstrapDirectoryPrefix(bootstrapDirectoryPrefix.toFile());
    }

    public final void setBootstrapDirectoryPrefix(File bootstrapDirectoryPrefix) {
        this.bootstrapDirectoryPrefix = bootstrapDirectoryPrefix;
    }

    public final File getBuildEnvironmentDirectory() {
        if (buildEnvironmentDirectory == null) {
            return project.file(DEFAULT_BUILD_ENVIRONMENT_DIRECTORY);
        }
        return buildEnvironmentDirectory;
    }

    public final File getBuildOutputDirectory() {
        return buildOutputDirectory;
    }

    public final void setMetaYaml(Path metaYaml) {
        this.metaYaml = metaYaml;
    }

    public final void setMetaYaml(File metaYaml) {
        setMetaYaml(metaYaml.toPath());
    }

    public final void setMetaYaml(String metaYaml) {
        setMetaYaml(Paths.get(metaYaml));
    }

    public final Path getMetaYaml() {
        if (metaYaml == null) {
            return project.file(DEFAULT_META_YAML_DIR).toPath();
        }
        return metaYaml;
    }

    public final void setBuildEnvironmentDirectory(String buildEnvironmentDirectory) {
        setBuildEnvironmentDirectory(new File(buildEnvironmentDirectory));
    }

    public final void setBuildEnvironmentDirectory(Path buildEnvironmentDirectory) {
        setBuildEnvironmentDirectory(buildEnvironmentDirectory.toFile());
    }

    public final void setBuildEnvironmentDirectory(File buildEnvironmentDirectory) {
        this.buildEnvironmentDirectory = buildEnvironmentDirectory;
    }

    public final void setBuildOutputDirectory(String buildOutputDirectory) {
        setBuildOutputDirectory(new File(buildOutputDirectory));
    }

    public final void setBuildOutputDirectory(Path buildOutputDirectory) {
        setBuildOutputDirectory(buildOutputDirectory.toFile());
    }

    public final void setBuildOutputDirectory(File buildOutputDirectory) {
        this.buildOutputDirectory = buildOutputDirectory;
    }

    public final String getMinicondaVersion() {
        return minicondaVersion;
    }

    public final void setMinicondaVersion(String minicondaVersion) {
        this.minicondaVersion = minicondaVersion;
    }

    public final String getCondaBuildVersion() {
        return condaBuildVersion;
    }

    public final void setCondaBuildVersion(String condaBuildVersion) {
        this.condaBuildVersion = condaBuildVersion;
    }

    public final int getPythonVersion() {
        return pythonVersion;
    }

    public final void setPythonVersion(int pythonVersion) {
        this.pythonVersion = pythonVersion;
    }

    public final List<String> getPackages() {
        return packages;
    }

    public final void setPackages(List<String> packages) {
        this.packages = packages;
    }

    public final List<String> getChannels() {
        return channels;
    }

    public final void setChannels(List<String> channels) {
        this.channels = channels;
    }

    public Path getPythonRelativePath() {
        if (os.isWindows()) {
            return Paths.get("python");
        } else {
            return Paths.get("bin", "python");
        }
    }

    public Path getScriptsRelativeDir() {
        if (os.isWindows()) {
            return Paths.get("Scripts");
        } else {
            return Paths.get("bin");
        }
    }

    public OperatingSystem getOs() {
        return os;
    }
}
