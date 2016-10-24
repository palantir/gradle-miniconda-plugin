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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Project extension to configureAfterEvaluate Python build environment.
 *
 * @author pbiswal
 */
public class MinicondaExtension {

    private static final String DEFAULT_CHANNEL = "https://repo.continuum.io/pkgs/free";
    private static final File DEFAULT_BOOTSTRAP_DIRECTORY_PREFIX =
            new File(System.getProperty("user.home"), ".miniconda-bootstrap");
    private static final File DEFAULT_BUILD_ENVIRONMENT_DIRECTORY = new File("build/miniconda");

    private String minicondaVersion;
    private File bootstrapDirectoryPrefix = DEFAULT_BOOTSTRAP_DIRECTORY_PREFIX;
    private File buildEnvironmentDirectory = DEFAULT_BUILD_ENVIRONMENT_DIRECTORY;
    private List<String> packages = new ArrayList<>();
    private List<String> channels = new ArrayList<>(Collections.singletonList(DEFAULT_CHANNEL));

    public void validate() {
        Objects.requireNonNull(minicondaVersion, "miniconda.minicondaVersion must be set.");
        Objects.requireNonNull(bootstrapDirectoryPrefix, "miniconda.bootstrapDirectoryPrefix must not be null.");
        Objects.requireNonNull(buildEnvironmentDirectory, "miniconda.buildEnvironmentDirectory must not be null.");
        Objects.requireNonNull(packages, "miniconda.packages must not be null.");
        if (packages.isEmpty()) {
            throw new IllegalArgumentException("miniconda.packages must contain at least one requirement.");
        }
        Objects.requireNonNull(channels, "miniconda.channels must not be null.");
        if (channels.isEmpty()) {
            throw new IllegalArgumentException("miniconda.channels must contain at least one channel.");
        }
    }

    public File getBootstrapDirectory() {
        return new File(bootstrapDirectoryPrefix, minicondaVersion);
    }

    public File getBootstrapDirectoryPrefix() {
        return bootstrapDirectoryPrefix;
    }

    public void setBootstrapDirectoryPrefix(String bootstrapDirectoryPrefix) {
        setBootstrapDirectoryPrefix(new File(bootstrapDirectoryPrefix));
    }

    public void setBootstrapDirectoryPrefix(Path bootstrapDirectoryPrefix) {
        setBootstrapDirectoryPrefix(bootstrapDirectoryPrefix.toFile());
    }

    public void setBootstrapDirectoryPrefix(File bootstrapDirectoryPrefix) {
        this.bootstrapDirectoryPrefix = bootstrapDirectoryPrefix;
    }

    public File getBuildEnvironmentDirectory() {
        return buildEnvironmentDirectory;
    }

    public void setBuildEnvironmentDirectory(File buildEnvironmentDirectory) {
        this.buildEnvironmentDirectory = buildEnvironmentDirectory;
    }

    public String getMinicondaVersion() {
        return minicondaVersion;
    }

    public void setMinicondaVersion(String minicondaVersion) {
        this.minicondaVersion = minicondaVersion;
    }

    public List<String> getPackages() {
        return packages;
    }

    public void setPackages(List<String> packages) {
        this.packages = packages;
    }

    public List<String> getChannels() {
        return channels;
    }

    public void setChannels(List<String> channels) {
        this.channels = channels;
    }
}
