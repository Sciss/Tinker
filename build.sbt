lazy val root = project.in(file("."))
  .settings(
    name                := "TinkerForgeIMU2Test",
    organization        := "de.sciss",
    version             := "0.1.0-SNAPSHOT",
    scalaVersion        := "2.12.5",
    description         := "Testing the TinkerForge IMU2 brick sensor board from Scala",
    homepage            := Some(url(s"https://github.com/Sciss/${name.value}")),
    licenses            := Seq("LGPL v2.1+" -> url("http://www.gnu.org/licenses/lgpl-2.1.txt")),
    scalacOptions      ++= Seq("-deprecation", "-unchecked", "-feature", "-Xfuture", "-encoding", "utf8"),
    libraryDependencies += "com.tinkerforge" % "tinkerforge" % "2.1.17"
  )
