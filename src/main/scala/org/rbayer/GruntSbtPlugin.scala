package org.rbayer

import sbt._
import Keys._

object GruntSbtPlugin extends Plugin {
  override lazy val settings = Seq(
    commands ++= Seq(
      gruntCmd
    )
  )

  object GruntKeys {
    val gruntPath = settingKey[String]("The path to the grunt executable. By " +
      "default, the plugin will look for `grunt` in the $PATH.")
    val gruntNodePath = settingKey[String]("The path to the node.js " +
      "executable.  By default, the plugin will look for `node` in the $PATH.")
    val gruntNpmPath = settingKey[String]("The path to the npm executable. " +
      "By default, the plugin will look for `npm` in the $PATH.")
    val gruntTasks = settingKey[Seq[String]]("Sequence of grunt tasks to " +
      "execute as part of the `grunt` task.")
    val gruntForce = settingKey[Boolean]("If true, the `--force` option " +
      "will be used when executing grunt, thus ignoring failures.")

    lazy val grunt = taskKey[Int]("Executes grunt tasks defined by the " +
      "`gruntTasks` key.")

    lazy val npmInstall = taskKey[Int]("Executes `npm install ` in the " +
      "project's parent directory if a package.json file is present.")
  }

  import GruntKeys._

  val basePerConfigGruntSettings: Seq[Setting[_]] = Seq(
    gruntTasks := Seq(""),
    gruntForce := false,
    grunt <<= gruntTask
  )

  val baseGruntSettings: Seq[Setting[_]] = Seq(
    gruntPath := "grunt",
    gruntNpmPath := "",
    gruntNodePath := ""
  )

  val gruntSettings: Seq[Setting[_]] =
    baseGruntSettings ++
    gruntSettingsIn(Compile, Seq(
      gruntTasks := Seq("build"),
      (compile in Compile) <<= (compile in Compile).dependsOn(grunt in Compile)
    )) ++
    gruntSettingsIn(Test, Seq(
      gruntTasks := Seq("test"),
      (grunt in Test) <<= (grunt in Test) dependsOn (executeTests in Test),
      (test in Test) <<= (test in Test) dependsOn (grunt in Test)
    ))

  def gruntSettingsIn(c: Configuration, overrides: Seq[Setting[_]]): Seq[Setting[_]] =
    inConfig(c)(basePerConfigGruntSettings ++ overrides)

  lazy val gruntTask: Def.Initialize[Task[Int]] = Def.task {
    val s: TaskStreams = streams.value

    val grunt = gruntPath.value
    val nodeExe = gruntNodePath.value
    val force = if (gruntForce.value) "--force" else ""
    val taskArgs = gruntTasks.value

    val cmd = (Seq[String](nodeExe, grunt, force) ++ taskArgs) filter { _.length > 0 } mkString " "

    s.log.debug(s"Executing grunt using command: ${cmd}")

    val rc = cmd.!

    if (rc == 0) rc else sys.error(s"Grunt process generated non-zero return code: ${rc}")
  }

  lazy val npmInstallTask: Def.Initialize[Task[Int]] = Def.task {
    val s:TaskStreams = streams.value

    0
  }

  lazy val gruntCmd = Command.single("grunt") { (state, task) =>
    //gruntTask(task)
    state
  }
}

