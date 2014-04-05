sbtPlugin := true

name := "grunt-sbt"

organization := "org.rbayer"

version := "1.0-SNAPSHOT"

scalaVersion := "2.10.3"

scriptedSettings

scriptedLaunchOpts += "-Dplugin.version=" + version.value