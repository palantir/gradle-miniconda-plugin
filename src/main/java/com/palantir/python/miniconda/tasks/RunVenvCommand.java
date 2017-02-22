package com.palantir.python.miniconda.tasks;

import com.palantir.python.miniconda.MinicondaExtension;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Exec;

/**
 * Created by ivanatanasov on 22/02/2017.
 */
public class RunVenvCommand extends Exec {

    private MinicondaExtension miniconda = getProject().getExtensions().findByType(MinicondaExtension.class);

    public RunVenvCommand() {
        if (miniconda.getOs().isWindows()) {
            executable("cmd");
            args("/c");
        }
    }

    public void setVenvCommand(String command){
        if (miniconda.getOs().isWindows()){
            args(miniconda.getBuildEnvironmentDirectory().toPath().resolve("Scripts/" + command));
        } else {
            executable(miniconda.getBuildEnvironmentDirectory().toPath().resolve("bin/" + command));
        }
    }




}
