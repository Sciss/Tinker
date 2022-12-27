lazy val baseName = "TinkerForgeIMU2Test"
lazy val gitHost  = "codeberg.org"
lazy val gitUser  = "sciss"

lazy val deps = new {
  val main = new {
    val fileUtil  = "1.1.5"
    val jzy3d     = "1.0.3"
    val kollFlitz = "0.2.4"
    val pi4j      = "1.4"
    val scalaOSC  = "1.3.1"
    val scallop   = "4.1.0"
    val scopt     = "4.1.0"   // TODO: move all over to scallop
    val swingPlus = "0.5.0"
    val tinker    = "2.1.32"
  }
}

lazy val root = project.in(file("."))
  .settings(
    name                 := baseName,
    organization         := "de.sciss",
    version              := "0.1.0-SNAPSHOT",
    scalaVersion         := "2.13.10",
    crossScalaVersions   := Seq("3.2.1", "2.13.10"),
    description          := "Using the TinkerForge IMU2 brick sensor board from Scala",
    homepage             := Some(url(s"https://$gitHost/$gitUser/$baseName")),
    licenses             := Seq("LGPL v2.1+" -> url("http://www.gnu.org/licenses/lgpl-2.1.txt")),
    scalacOptions       ++= Seq("-deprecation", "-unchecked", "-feature", "-encoding", "utf8"),
    libraryDependencies ++= Seq(
      "com.tinkerforge"   %   "tinkerforge" % deps.main.tinker,
      "de.sciss"          %%  "fileutil"    % deps.main.fileUtil,
      "de.sciss"          %%  "kollflitz"   % deps.main.kollFlitz,
      "de.sciss"          %%  "swingplus"   % deps.main.swingPlus,
      "de.sciss"          %%  "scalaosc"    % deps.main.scalaOSC,
      "org.jzy3d"         %   "jzy3d-api"   % deps.main.jzy3d,
      "org.rogach"        %%  "scallop"     % deps.main.scallop,     // command line option parsing
      "com.pi4j"          %   "pi4j-core"   % deps.main.pi4j
    ),
    resolvers ++= Seq(
      ("jzv3d releases" at "http://maven.jzy3d.org/releases").withAllowInsecureProtocol(true)  // 3D chart
    ),
    // assembly
    assembly / target          := baseDirectory.value,
//    assembly / mainClass := Some("de.sciss.tinkerforge.View"),
//    assembly / assemblyJarName := "WekiIMU.jar",
    assembly / mainClass       := Some("de.sciss.tinkerforge.IMU_OSC"),
    assembly / assemblyJarName := "IMU_OSC.jar",
  )
