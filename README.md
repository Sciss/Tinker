# TinkerForgeIMU2Test

[![Build Status](https://github.com/Sciss/TinkerForgeIMU2Test/workflows/Scala%20CI/badge.svg?branch=main)](https://github.com/Sciss/TinkerForgeIMU2Test/actions?query=workflow%3A%22Scala+CI%22)

## statement

This is a project for testing the Java bindings to [TinkerForge](https://www.tinkerforge.com) API.
It is (C)opyright 2018–2022 by Hanns Holger Rutz. All rights reserved. The project is released under 
the [GNU Lesser General Public License](https://raw.github.com/Sciss/TinkerForgeIMU2Test/main/LICENSE) v2.1+ and
comes with absolutely no warranties. To contact the author, send an e-mail to `contact at sciss.de`.

## requirements / installation

This project builds against Scala 2.13, using [sbt](http://www.scala-sbt.org/). To run: `sbt 'runMain <class>'`:

- `sbt 'runMain de.sciss.tinkerforge.ExampleEnumerate'`
- `sbt 'runMain de.sciss.tinkerforge.ExampleIMUV2Simple --uid your-imu-brick-id'`
- `sbt 'runMain de.sciss.tinkerforge.ExampleIMUV2AllData --uid your-imu-brick-id'`

To run the `View` target that can send OSC over to Wekinator, use `sbt assembly` and the `run.sh` script.
