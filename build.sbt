lazy val baseName = "Tinker"
lazy val gitHost  = "codeberg.org"
lazy val gitUser  = "sciss"

lazy val deps = new {
  val core = new {
    val scalaOSC  = "1.3.1"
    val scallop   = "4.1.0"
    val swingPlus = "0.5.0"
    val tinker    = "2.1.32"
  }

  val recorder = new {
    val fileUtil  = "1.1.5"
    val jzy3d     = "1.0.3"
    val kollFlitz = "0.2.4"
    val pi4j      = "1.4"
  }
}

lazy val commmonSettings = Seq(
  organization        := "de.sciss",
  version             := "0.1.0-SNAPSHOT",
  scalaVersion        := "2.13.10",
  crossScalaVersions  := Seq("3.2.1", "2.13.10"),
  homepage            := Some(url(s"https://$gitHost/$gitUser/$baseName")),
  licenses            := Seq("LGPL v2.1+" -> url("http://www.gnu.org/licenses/lgpl-2.1.txt")),
  scalacOptions      ++= Seq("-deprecation", "-unchecked", "-feature", "-encoding", "utf8"),
  run / fork          := true
)

lazy val root = project.in(file("."))
  .aggregate(core, recorder)
  .settings(commmonSettings)
  .settings(
    name                := baseName,
    description         := "Using the TinkerForge IMU2 brick sensor board from Scala"
  )

lazy val core = project.in(file("core"))
  .settings(commmonSettings)
  .settings(
    name                 := baseName,
    libraryDependencies ++= Seq(
      "com.tinkerforge"   %   "tinkerforge" % deps.core.tinker,
      "de.sciss"          %%  "swingplus"   % deps.core.swingPlus,
      "de.sciss"          %%  "scalaosc"    % deps.core.scalaOSC,
      "org.rogach"        %%  "scallop"     % deps.core.scallop      // command line option parsing
    ),
    // assembly
    assembly / target          := baseDirectory.value,
    //    assembly / mainClass := Some("de.sciss.tinkerforge.View"),
    //    assembly / assemblyJarName := "WekiIMU.jar",
    assembly / mainClass       := Some("de.sciss.tinker.IMU_OSC"),
    assembly / assemblyJarName := "IMU_OSC.jar"
  )

lazy val recorder = project.in(file("recorder"))
  .dependsOn(core)
  .settings(commmonSettings)
  .settings(
    name                 := baseName,
    libraryDependencies ++= Seq(
      "com.tinkerforge"   %   "tinkerforge" % deps.core.tinker,
      "de.sciss"          %%  "fileutil"    % deps.recorder.fileUtil,
      "de.sciss"          %%  "kollflitz"   % deps.recorder.kollFlitz,
      "org.jzy3d"         %   "jzy3d-api"   % deps.recorder.jzy3d,
      "org.rogach"        %%  "scallop"     % deps.core.scallop,     // command line option parsing
      "com.pi4j"          %   "pi4j-core"   % deps.recorder.pi4j
    ),
    resolvers ++= Seq(
      ("jzv3d releases" at "http://maven.jzy3d.org/releases").withAllowInsecureProtocol(true)  // 3D chart
    ),
    // assembly
    assembly / target          := baseDirectory.value,
    assembly / mainClass       := Some("de.sciss.tinker.RecordAccel"),
    assembly / assemblyJarName := "IMU_OSC.jar"
  )
