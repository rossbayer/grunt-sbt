//------------------------------------------------------------------------------
// Copyright (c) 2014 Ross Bayer
//------------------------------------------------------------------------------

package org.rbayer

import sbt._
import Keys._

/**
 * sbt plugin that provides integration with
 * <a href="http://gruntjs.com/">Grunt.js</a> that allows execution of Grunt.js
 * builds from within sbt.
 *
 * In addition to a basic <code>grunt</code> command that allows abritrary
 * execution of grunt commands from the sbt console, this plugin also provides
 * tasks for executing one or more grunt tasks as part of a the compile
 * and test phases (with the ability to extend other configurations as well).
 *
 * @author Ross Bayer (rossbayer@gmail.com)
 */
object GruntSbtPlugin extends Plugin {

  /**
   * Object defining setting and tasks keys for the plugin
   */
  object GruntKeys {

    /**
     * Setting for the path to grunt-cli script.  By default, this is
     * just <code>grunt</code>.
     */
    val gruntPath = settingKey[String]("The path to the grunt executable. By " +
      "default, the plugin will look for `grunt` in the $PATH.")

    /**
     * Setting for the path to the node executable.
     *
     * By default, this is not set.  This setting can be used to set an explicit
     * path to <code>node</code> for use in environments (like continuous
     * integration servers) where it is not on the PATH.  By setting this value,
     * the grunt and npm executables will be run as a script passed to the
     * <code>node</code> program.
     */
    val gruntNodePath = settingKey[String]("The path to the node.js " +
      "executable.  By default, the plugin will look for `node` in the $PATH.")

    /**
     * Setting for the path to the npm executable.
     *
     * By default, this is <code>npm</code>, assumed to by on your $PATH.
     */
    val gruntNpmPath = settingKey[String]("The path to the npm executable. " +
      "By default, the plugin will look for `npm` in the $PATH.")

    /**
     * Set of tasks for Grunt to run as part of a configuration.  For instance,
     * this can be set in the <code>Compile</code> configuration to run
     * the <code>build</code> and <code>jshint</code> tasks, for instance.
     */
    val gruntTasks = settingKey[Seq[String]]("Sequence of grunt tasks to " +
      "execute as part of the `grunt` task.")

    /**
     * Setting indicating if grunt should use the <code>--force</code> option
     * to ignore errors while executing tasks.
     */
    val gruntForce = settingKey[Boolean]("If true, the `--force` option " +
      "will be used when executing grunt, thus ignoring failures.")

    /**
     * The grunt task that will execute one or more grunt tasks as part of a
     * configuration, such as compile or test.
     */
    lazy val grunt = taskKey[Int]("Executes grunt tasks defined by the " +
      "`gruntTasks` key.")

    /**
     * Task that will execute <code>npm install</code> to install npm packages
     * in the current directory based on the values defined in
     * <code>package.json</code>.
     */
    lazy val npmInstall = taskKey[Int]("Executes `npm install ` in the " +
      "project's parent directory if a package.json file is present.")
  }

  import GruntKeys._

  /**
   * Base sequence of settings for sbt for an arbitrary configuration.
   */
  val basePerConfigGruntSettings: Seq[Setting[_]] = Seq(
    gruntTasks := Seq(""),
    gruntForce := false,
    grunt <<= gruntTask
  )

  /**
   * Core settings used by all configurations.
   */
  val baseGruntSettings: Seq[Setting[_]] = Seq(
    gruntPath := "grunt",
    gruntNpmPath := "npm",
    gruntNodePath := ""
  )

  /**
   * Sequence of settings to be included in a user's build.sbt file to
   * add grunt support to their build.  This includes defining grunt tasks
   * in the <code>Compile</code> and <code>Test</code> configurations.
   */
  val gruntSettings: Seq[Setting[_]] =
    baseGruntSettings ++
    gruntSettingsIn(Compile, Seq(
      gruntTasks := Seq("build"),
      npmInstall <<= npmInstallTask,
      grunt <<= gruntTask.dependsOn(npmInstall in Compile),
      (compile in Compile) <<= (compile in Compile).dependsOn(grunt in Compile)
    )) ++
    gruntSettingsIn(Test, Seq(
      gruntTasks := Seq("test"),
      (grunt in Test) <<= (grunt in Test) dependsOn (executeTests in Test),
      (test in Test) <<= (test in Test) dependsOn (grunt in Test)
    ))

  override lazy val settings = Seq(
    commands ++= Seq(
      gruntCmd
    )
  )

  /**
   * Implementation of grunt task that will execute one or more tasks defined
   * by the <code>gruntTasks</code> setting via the <code>grunt</code> command.
   */
  lazy val gruntTask: Def.Initialize[Task[Int]] = Def.task {
    val force = if (gruntForce.value) "--force" else ""
    exec(
      gruntNodePath.value,
      gruntPath.value,
      Seq(force) ++ gruntTasks.value,
      Some(streams.value))
  }

  /**
   * Task that will execute <code>npm install</code> in the current working
   * directory.
   */
  lazy val npmInstallTask: Def.Initialize[Task[Int]] = Def.task {
    exec(
      gruntNodePath.value,
      gruntNpmPath.value,
      Seq("install"),
      Some(streams.value))
  }

  /**
   * Command for executing the <code>grunt</code> command interactively from
   * the sbt console.
   */
  lazy val gruntCmd = Command.single("grunt") { (state, task) =>
    val extracted = Project.extract(state)
    val nodePath = extracted.getOpt(gruntNodePath).get
    val cmd = extracted.getOpt(gruntPath).get

    exec(nodePath, cmd, Seq(task), None)

    state
  }

  /**
   * Convenience method for including grunt-sbt plugin configuration values
   * with an arbitrary sbt configuration.
   *
   * This method will add the default settings to the configuration, and then
   * allow the user to update the values as they wish.
   *
   * @param c The configuration to add grunt values to.
   * @param overrides Sequence of settings that can be used to override or
   *                  extend the default values provided by the plugin.
   *
   * @return The final sequence of settings in the specified configuration.
   */
  def gruntSettingsIn(c: Configuration, overrides: Seq[Setting[_]] = Seq.empty[Setting[_]]): Seq[Setting[_]] =
    inConfig(c)(basePerConfigGruntSettings ++ overrides)

  /**
   * Executes a node command in an external process.
   *
   * @param nodePath Path to the <code>node</code> executable.
   * @param cmd The command to execute.  This can be either a fully-qualified
   *            filesystem path to the command, or a basename (assumed to be
   *            on the user's $PATH).
   * @param args Sequence of arguments to pass to the command.
   * @param s Optional TaskStreams object used for debug logging.
   *
   * @return The return code from the node process.
   */
  private def exec(nodePath: String, cmd: String, args: Seq[String] = Seq(), s: Option[TaskStreams] = None):Int = {
    val fullCmd = (Seq[String](nodePath, cmd) ++ args) filter { _.length > 0 } mkString " "

    s map { _.log.debug(s"Executing grunt-sbt command: ${fullCmd}") }

    val rc = fullCmd.!

    if (rc == 0) rc else sys.error(s"Grunt: ${cmd} generated non-zero return code: ${rc}")
  }
}
