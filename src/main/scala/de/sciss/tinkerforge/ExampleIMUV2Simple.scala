/*
 *  ExampleIMUV2Simple.scala
 *  (TinkerForgeIMU2Test)
 *
 *  Copyright (c) 2018-2022 Hanns Holger Rutz. All rights reserved.
 *
 *  This software is published under the GNU Lesser General Public License v2.1+
 *
 *
 *  For further information, please contact Hanns Holger Rutz at
 *  contact@sciss.de
 */

package de.sciss.tinkerforge

import com.tinkerforge.{BrickIMUV2, IPConnection}

// quite straight translation from the original Java source
// published by TinkerForge under CC0 1.0 Universal (public domain)
//
// opens the IMU v2 brick, prints current quaternion, then quits
object ExampleIMUV2Simple {
  case class Config(uid: String = Common.DefaultIMU_Brick_UID, bricklet: Boolean = false)

  def main(args: Array[String]): Unit = {
    val default = Config()

    val p = new scopt.OptionParser[Config]("ExampleIMUV2Simple") {
      opt[String]('u', "uid")
        .text (s"UID of the IMU brick you want to use (default: ${default.uid})")
        .action { (v, c) => c.copy(uid = v) }

      opt[Unit]('b', "bricklet")
        .text(s"Use Bricklet v3 instead of Brick v2 (default: ${default.bricklet})")
        .action { (_, c) => c.copy(bricklet = true) }
    }
    p.parse(args, default).fold(sys.exit(1))(run)
  }

  def run(config: Config): Unit = {
    val c = new IPConnection
    // Create IP connection
    val imu = IMUBrickLike(config.uid, c, bricklet = config.bricklet) // Create device object
    c.connect(Common.Host, Common.Port)     // Connect to brickd

    // Don't use device before `c` is connected
    // Get current quaternion
    val q = imu.getQuaternion // Can throw com.tinkerforge.TimeoutException
    val scale = 1.0/16383.0
    println(s"Quaternion [W]: ${q.w * scale}")
    println(s"Quaternion [X]: ${q.x * scale}")
    println(s"Quaternion [Y]: ${q.y * scale}")
    println(s"Quaternion [Z]: ${q.z * scale}")
    c.disconnect()
  }
}