package com.palantir.python.miniconda;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Project extension to configure Python build environment.
 *
 * @author pbiswal
 */
public class MinicondaExtension {

    private static final String DEFAULT_CHANNEL = "https://repo.continuum.io/pkgs/free";

    private File bootstrapDirectoryPrefix;
    private File buildEnvironmentDirectory;
    private String minicondaVersion;
    private List<String> packages;
    private List<String> channels = new ArrayList<>(Collections.singletonList(DEFAULT_CHANNEL));

    public File getBootstrapDirectory() {
        return new File(bootstrapDirectoryPrefix, minicondaVersion);
    }

    public File getBootstrapDirectoryPrefix() {
        return bootstrapDirectoryPrefix;
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
