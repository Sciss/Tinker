# TinkerForgeIMU2Test

[![Build Status](https://github.com/Sciss/TinkerForgeIMU2Test/workflows/Scala%20CI/badge.svg?branch=main)](https://github.com/Sciss/TinkerForgeIMU2Test/actions?query=workflow%3A%22Scala+CI%22)

## statement

This is a project for testing the Java bindings to [TinkerForge](https://www.tinkerforge.com) API.
Specifically, to use the inertial measurement unit (IMU) sensors, IMU Brick v2 or IMU Bricklet v3.
It is (C)opyright 2018–2023 by Hanns Holger Rutz. All rights reserved. The project is released under 
the [GNU Lesser General Public License](https://codeberg.org/sciss/TinkerForgeIMU2Test/raw/branch/main/LICENSE) v2.1+
and comes with absolutely no warranties. To contact the author, send an e-mail to `contact at sciss.de`.

## requirements / installation

This project builds against Scala 3.x, 2.13, using [sbt](http://www.scala-sbt.org/). To run: `sbt 'runMain <class>'`:

- `sbt 'core/runMain de.sciss.tinkerforge.Enumerate'`
- `sbt 'core/runMain de.sciss.tinkerforge.IMU_SimpleData --uid your-imu-brick-id'`
- `sbt 'core/runMain de.sciss.tinkerforge.IMU_AllData --uid your-imu-brick-id'`
- `sbt 'core/runMain de.sciss.tinkerforge.View --uid your-imu-brick-id'`

To run the `IMU_OSC` target that can send the sensor data to a target node via OSC,
use `sbt core/assembly` and the `run.sh` script. `java -jar IMU_OSC.jar --help` for command line args.

The former recorder experiment is in sbt module `recorder`.