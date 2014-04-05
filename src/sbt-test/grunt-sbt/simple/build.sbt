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

(gruntResourcesClasspath in Compile) := file("META-INF/resources") / name.value / version.value