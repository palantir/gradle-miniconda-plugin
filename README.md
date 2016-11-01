Gradle Miniconda Plugin [![Circle CI](https://circleci.com/gh/palantir/gradle-miniconda-plugin.svg?style=svg)](https://circleci.com/gh/palantir/gradle-miniconda-plugin)
=======================

Plugin that sets up a Python environment for building and running tests using
[Miniconda](http://conda.pydata.org/miniconda.html).

Usage
-----

Apply the plugin to your project following
[`https://plugins.gradle.org/plugin/com.palantir.python.miniconda`](https://plugins.gradle.org/plugin/com.palantir.python.miniconda),
and configure the associated extension:

Minimal configuration:
```gradle
miniconda {
    minicondaVersion = '3.19.0'
    packages = ['python']
}
```
Then invoke the `setupPython` task and use the resulting installation directory from `Exec` tasks:

```gradle
task launchNotebook(type: Exec) {
    dependsOn 'setupPython'
    executable "${miniconda.buildEnvironmentDirectory}/bin/python"
    args '-c', 'print("Hello world!)'
}
```

Options
-------

| Name | Default Value | Description | Optional |
| ---- | ------------- | ----------- | -------- |
| minicondaVersion | N/A | The miniconda version which you want to use. See [the miniconda repo](https://repo.continuum.io/miniconda/) | false
| packages | N/A | The conda packages you want installed into your conda environment. This list must contain at least one argument. | false
| bootstrapDirectoryPrefix | `new File(System.getProperty('user.home'), '.miniconda')` | The root directory to put the root install of miniconda. This helps performance by caching the root environment by `pythonVersion` and `minicondaVersion`. | true
| buildEnvironmentDirectory | new File(buildDir, 'miniconda') | The directory to place your specific miniconda environment. | true
| pythonVersion | 2 | The python version you want for your miniconda. If you want Miniconda3, this value is 3. | true
| channels | ['https://repo.continuum.io/pkgs/free'] | The list of conda channels you want to use for downloading conda packages. Must not be empty. | true

Here is an example of the plugin with all the bells and whistles.
```gradle
miniconda {
    bootstrapDirectoryPrefix = new File(System.getProperty('user.home'), '.miniconda')
    buildEnvironmentDirectory = new File(buildDir, 'python')
    minicondaVersion = '3.19.0'
    packages = ['ipython-notebook']
    pythonVersion = 2
    channels = ['https://repo.continuum.io/pkgs/free', 'conda-forge']
}
```

If you need to customize where the Miniconda installer script is downloaded from, you can add your artifact to the `minicondaInstaller`
configuration. The default location where it is downloaded from is: [`https://repo.continuum.io`](https://repo.continuum.io).

License
-------

Gradle Miniconda Plugin is released by Palantir Technologies, Inc. under the Apache 2.0 License. See the included
[LICENSE](LICENSE) file for details.

Backwards Compatibility Breaks
------------------------------

### 0.5.0
The bootstrap Python is now placed in `bootstrapDirectoryPrefix/python-$pythonVersion/miniconda-$minicondaVersion`.

### 0.4.0
The bootstrap Python is now placed in `bootstrapDirectoryPrefix/minicondaVersion`. Users now must set
`bootstrapDirectoryPrefix` instead of `bootstrapDirectory`. You can still get the new directory of the bootstrap Python
by referring to the `bootstrapDirectory` property.

