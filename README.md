SBT grunt-sbt Plugin
====================

An sbt plugin for adding Grunt.js to your sbt build.

Adding the Plugin
=================

First, add the plugin to your plugins.sbt file (coming soon):

```scala
addSbtPlugin("org.rbayer" % "grunt-sbt" % "1.0")
```

If you want to use the latest version of the plugin directly from source
control, add the following to your plugins.sbt file:

```scala
lazy val root = project.in(file(".")).dependsOn(gruntSbtPlugin)

lazy val gruntSbtPlugin = uri("https://github.com/rossbayer/grunt-sbt.git")
```

Using the Plugin
================

By default, the plugin comes with a basic configuration that can be used for
typical projects involving Grunt.js.  These settings can be referenced in your
`build.sbt` file via the `gruntSettings` variable, like so:

```scala
name := "grunt-example"

version := "1.0"

organization := "org.rbayer"

scalaVersion := "2.10.3"

gruntSettings
```

Without modification, the settings defined in `gruntSettings` will add a
`compile:grunt` task that will be invoked as part of `compile` and a
`test:grunt` task that will be invoked as part of the `test` task. Additionally,
a `grunt` command will be introduced that allows you to call grunt from the
sbt console and execute an arbitrary command (for instance, `serve`).

Tasks
-----

The following tasks are available when `gruntSettings` is included in your
build:

- `compile:npmInstall` - Executes `npm install` in your current working
directory to install any NPM package dependencies defined in `package.json`.
- `compile:grunt` - Executes grunt tasks associated with the `Compile`
configuration.  By default, the `build` task will be executed in your
`Gruntfile`.
- `test:grunt` - Executes grunt tasks associated with the `Test`
configuration.  By default, the `test` task will be executed in your
`Gruntfile`.

Commands
--------

The following commands are provided by the plugin:

- `grunt` - Command that will execute an arbitrary set of tasks using Grunt.js
in your current sbt console session.  Example:

    > grunt --force jshint serve
    ...

Settings
--------

To override or modify plugin settings, import the `GruntKeys` object as a
package and modify keys as appropriate in your `build.sbt` file:

```scala
    import GruntKeys._

    name := "grunt-example"

    version := "1.0"

    organization := "org.rbayer"

    scalaVersion := "2.10.3"

    gruntSettings

    gruntPath := "/path/to/grunt-cli/bin/grunt"

    ...
```

The following settings are available for the plugin that can be overridden:

| Setting | Default Value | Description |
| ------- | ------------- | ----------- |
| gruntPath | grunt | The path to the grunt executable. This setting can be used
to override the location of Grunt.js when it is not on the `$PATH`, like in
a CI environment. |
| gruntNodePath | Nil | The path to the node executable. If set, this will
cause the plugin to execute the grunt executable specified via `gruntPath`
by passing it as an argument to the `node` interpreter process
(i.e. `node /path/to/grunt`) |
| gruntNpmPath | npm | The path to the npm executable.  This setting can be used
to override the location of `npm` when it is not on the `$PATH`. |
| gruntTasks | `"build" in Compile` or `"test" in Test` | Sequence of tasks
to execute via `grunt` in a particular configuration.  By default, during a
`compile`, `grunt build` will be executed, and `grunt test` will be executed
during a `test` task. |

