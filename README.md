Gradle Miniconda Plugin [![Build Status](https://magnum.travis-ci.com/palantir/gradle-miniconda-plugin.svg?token=7onD2L3nEXMByQUNdxv1&branch=develop)](https://magnum.travis-ci.com/palantir/gradle-miniconda-plugin)
=======================

Plugin that sets up a Python environment for building and running tests using
[Miniconda](http://conda.pydata.org/miniconda.html).

Usage
-----

Apply the plugin to your project and configure the associated extension:

    plugins {
        id 'com.palantir.mlx.build.miniconda' version '0.1.0-SNAPSHOT'
    }

    miniconda {
        bootstrapDirectory = new File(System.getProperty('user.home'), '.miniconda')
        buildEnvironmentDirectory = new File(buildDir, 'python')
        minicondaVersion = '3.10.1'
        packages = ['ipython-notebook']
    }

Then invoke the `setupPython` task and use the resulting installation directory from `Exec` tasks:

    task launchNotebook(type: Exec) {
        dependsOn setupPython
        commandLine "${miniconda.buildEnvironmentDirectory}/bin/ipython", 'notebook'
    }

Options
-------

You can customize where the Miniconda installer script is downloaded from by adding a dependency to the
`minicondaInstaller` configuration. By default, it's downloaded from
[`http://repo.continuum.io`](http://repo.continuum.io).

License
-------

Gradle Miniconda Plugin is released by Palantir Technologies, Inc. under the Apache 2.0 License. See the included
LICENSE file for details.
