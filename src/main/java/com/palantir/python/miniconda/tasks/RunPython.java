package com.palantir.python.miniconda.tasks;

import com.palantir.python.miniconda.MinicondaExtension;
import org.gradle.api.tasks.Exec;

/**
 * Created by ivanatanasov on 22/02/2017.
 */
public class RunPython extends Exec {

    public RunPython() {
        MinicondaExtension miniconda = getProject().getExtensions().findByType(MinicondaExtension.class);
        if (miniconda.getOs().isWindows()) {
            executable("cmd");
            args("/c", miniconda.getBuildEnvironmentDirectory().toPath().resolve("python"));
        } else {
            executable(miniconda.getBuildEnvironmentDirectory().toPath().resolve("bin/python"));
        }
    }
}
