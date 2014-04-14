sbtPlugin := true

name := "grunt-sbt"

organization := "org.rbayer"

version := "1.0"

scalaVersion := "2.10.3"

//----------------------------------------
// Publishing
//----------------------------------------

isSnapshot := false

licenses := Seq("MIT" -> url("http://www.opensource.org/licenses/mit-license.php"))

homepage := Some(url("https://github.com/rossbayer/grunt-sbt"))

publishMavenStyle := true

publishArtifact in Test := false

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

// POM settings
pomIncludeRepository := { _ => false }

pomExtra := (
  <scm>
    <url>git@github.com:rossbayer/grunt-sbt.git</url>
    <connection>scm:git:git@github.com:rossbayer/grunt-sbt.git</connection>
  </scm>
  <developers>
    <developer>
      <id>rossbayer</id>
      <name>Ross Bayer</name>
    </developer>
  </developers>)

//----------------------------------------
// Testing
//----------------------------------------

scriptedSettings

scriptedLaunchOpts += "-Dplugin.version=" + version.value