lazy val deps = new {
  val main = new {
    val fileUtil  = "1.1.3"
    val jzy3d     = "1.0.2"
    val kollFlitz = "0.2.2"
    val scopt     = "3.7.0"
    val tinker    = "2.1.17"
  }
}

lazy val root = project.in(file("."))
  .settings(
    name                 := "TinkerForgeIMU2Test",
    organization         := "de.sciss",
    version              := "0.1.0-SNAPSHOT",
    scalaVersion         := "2.12.5",
    description          := "Testing the TinkerForge IMU2 brick sensor board from Scala",
    homepage             := Some(url(s"https://github.com/Sciss/${name.value}")),
    licenses             := Seq("LGPL v2.1+" -> url("http://www.gnu.org/licenses/lgpl-2.1.txt")),
    scalacOptions       ++= Seq("-deprecation", "-unchecked", "-feature", "-Xfuture", "-encoding", "utf8"),
    libraryDependencies ++= Seq(
      "com.tinkerforge"   %   "tinkerforge" % deps.main.tinker,
      "com.github.scopt"  %%  "scopt"       % deps.main.scopt,
      "de.sciss"          %%  "fileutil"    % deps.main.fileUtil,
      "de.sciss"          %%  "kollflitz"   % deps.main.kollFlitz,
      "org.jzy3d"         %   "jzy3d-api"   % deps.main.jzy3d
    )
  )
