import org.rbayer.GruntSbtPlugin._
import GruntKeys._

name := "grunt-simple"

version := "1.0"

organization := "org.rbayer"

scalaVersion := "2.10.3"

libraryDependencies ++= Seq("org.specs2" %% "specs2" % "2.3.10" % "test")

scalacOptions in Test ++= Seq("-Yrangepos")

resolvers ++= Seq("snapshots", "releases").map(Resolver.sonatypeRepo)

gruntSettings

// Configure grunt to run from local copy using the `node` interpreter directly.

gruntPath := "node_modules/grunt-cli/bin/grunt"

//gruntNodePath := "node"

(gruntResourcesClasspath in Compile) := file("META-INF/resources") / name.value / version.value

gruntSettingsIn(config("testfailure"), Seq(
  (gruntTasks in config("testfailure")) := Seq("nope")
))

gruntSettingsIn(config("testforce"), Seq(
  gruntForce := true,
  (gruntTasks in config("testforce")) := Seq("nope")
))