# TinkerForgeIMU2Test

[![Build Status](https://travis-ci.org/Sciss/TinkerForgeIMU2Test.svg?branch=master)](https://travis-ci.org/Sciss/TinkerForgeIMU2Test)

## statement

This is a project for testing the Java bindings to [TinkerForge](https://www.tinkerforge.com) API.
It is (C)opyright 2018–2019 by Hanns Holger Rutz. All rights reserved. The project is released under 
the [GNU Lesser General Public License](https://raw.github.com/Sciss/TinkerForgeIMU2Test/master/LICENSE) v2.1+ and
comes with absolutely no warranties. To contact the author, send an email to `contact at sciss.de`

## requirements / installation

This project builds against Scala 2.12, using [sbt](http://www.scala-sbt.org/). To run: `sbt 'runMain <class>'`:

- `sbt 'runMain de.sciss.tinkerforge.ExampleEnumerate'`
- `sbt 'runMain de.sciss.tinkerforge.ExampleIMUV2Simple --uid your-imu-brick-id'`
- `sbt 'runMain de.sciss.tinkerforge.ExampleIMUV2AllData --uid your-imu-brick-id'`
