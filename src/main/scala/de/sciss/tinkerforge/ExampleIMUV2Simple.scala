/*
 *  ExampleIMUV2Simple.scala
 *  (TinkerForgeIMU2Test)
 *
 *  Copyright (c) 2018-2023 Hanns Holger Rutz. All rights reserved.
 *
 *  This software is published under the GNU Lesser General Public License v2.1+
 *
 *
 *  For further information, please contact Hanns Holger Rutz at
 *  contact@sciss.de
 */

package de.sciss.tinkerforge

import com.tinkerforge.IPConnection
import org.rogach.scallop.{ScallopConf, ScallopOption => Opt}

// quite straight translation from the original Java source
// published by TinkerForge under CC0 1.0 Universal (public domain)
//
// opens the IMU v2 brick, prints current quaternion, then quits
object ExampleIMUV2Simple {
  case class Config(uid: String = Common.DefaultIMU_Brick_UID, bricklet: Boolean = false)

  def main(args: Array[String]): Unit = {
    object p extends ScallopConf(args) {

      import org.rogach.scallop._

      printedName = "IMU Simple"
      private val default = Config()

      val uid: Opt[String] = opt(short = 'u', name = "uid", default = Some(default.uid),
        descr = s"UID of the IMU brick you want to use (default: ${default.uid})"
      )
      val bricklet: Opt[Boolean] = toggle(short = 'b', name = "bricklet", default = Some(default.bricklet),
        descrYes = s"Use Bricklet v3 instead of Brick v2 (default: ${default.bricklet})"
      )
      verify()

      val config: Config = Config(
        uid = uid(),
        bricklet = bricklet(),
      )
    }
    implicit val c: Config = p.config
    run()
  }

  def run()(implicit config: Config): Unit = {
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