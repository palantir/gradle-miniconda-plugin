Gradle Miniconda Plugin [![Circle CI](https://circleci.com/gh/palantir/gradle-miniconda-plugin.svg?style=svg)](https://circleci.com/gh/palantir/gradle-miniconda-plugin)
=======================

Plugin that sets up a Python environment for building and running tests using
[Miniconda](http://conda.pydata.org/miniconda.html).

Usage
-----

Apply the plugin to your project following
[`https://plugins.gradle.org/plugin/com.palantir.python.miniconda`](https://plugins.gradle.org/plugin/com.palantir.python.miniconda),
and configure the associated extension:

```gradle
miniconda {
    bootstrapDirectoryPrefix = new File(System.getProperty('user.home'), '.miniconda')
    buildEnvironmentDirectory = new File(buildDir, 'python')
    minicondaVersion = '3.10.1'
    packages = ['ipython-notebook']
}
```

Then invoke the `setupPython` task and use the resulting installation directory from `Exec` tasks:

```gradle
task launchNotebook(type: Exec) {
    dependsOn 'setupPython'
    executable "${miniconda.buildEnvironmentDirectory}/bin/ipython"
    args 'notebook'
}
```

Options
-------

You can customize where the Miniconda installer script is downloaded from by adding a dependency to the
`minicondaInstaller` configuration. By default, it's downloaded from
[`http://repo.continuum.io`](http://repo.continuum.io).

License
-------

Gradle Miniconda Plugin is released by Palantir Technologies, Inc. under the Apache 2.0 License. See the included
[LICENSE](LICENSE) file for details.

Backwards Compatibility Breaks
------------------------------

### 0.4.0
The bootstrap Python is now placed in `bootstrapDirectoryPrefix/minicondaVersion`. Users now must set
`bootstrapDirectoryPrefix` instead of `bootstrapDirectory`. You can still get the new directory of the bootstrap Python
by referring to the `bootstrapDirectory` property.

