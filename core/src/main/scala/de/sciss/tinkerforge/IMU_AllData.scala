/*
 *  IMU_AllData.scala
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
import de.sciss.tinkerforge.IMUBrickLike.isBricklet
import org.rogach.scallop.{ScallopConf, ScallopOption => Opt}

object IMU_AllData {
  case class Config(uid: String = Common.DefaultIMU_Brick_UID, bricklet: Boolean = false)

  def main(args: Array[String]): Unit = {
    object p extends ScallopConf(args) {

      import org.rogach.scallop._

      printedName = "IMU All Data"
      private val default = Config()

      val uid: Opt[String] = opt(short = 'u', name = "uid", default = Some(default.uid),
        descr = s"UID of the IMU brick you want to use (default: ${default.uid})"
      )
      val bricklet: Opt[Boolean] = toggle(short = 'b', name = "bricklet",
        descrYes = "Use Bricklet v3 instead of Brick v2"
      )
      verify()

      val config: Config = Config(
        uid       = uid(),
        bricklet  = bricklet.getOrElse(isBricklet(uid())),
      )
    }
    implicit val c: Config = p.config
    run()
  }

  def run()(implicit config: Config): Unit = {
    val c = new IPConnection // Create IP connection
    val imu = IMUBrickLike(config.uid, c, bricklet = config.bricklet) // Create device object
    c.connect(Common.Host, Common.Port)     // Connect to brickd

    val scaleAccel    = 1.0/  100.0
    val scaleMagnetic = 1.0/   16.0
    val scaleAng      = 1.0/   16.0
    val scaleQuat     = 1.0/16383.0
    val scaleGrav     = 1.0/  100.0

    imu.addAllDataListener { d =>
      import d.{acceleration => accel, angularVelocity => angVel, calibrationStatus => calibStatus, eulerAngle => eulerAng, gravityVector => gravityVec, linearAcceleration => linAccel, quaternion => quat, temperature => temp, _}
      println(f"Acceleration        (X): ${accel(0)*scaleAccel}%1.2f m/s²")
      println(f"Acceleration        (Y): ${accel(1)*scaleAccel}%1.2f m/s²")
      println(f"Acceleration        (Z): ${accel(2)*scaleAccel}%1.2f m/s²")
      println(s"Magnetic Field      (X): ${magneticField(0)*scaleMagnetic} µT")
      println(s"Magnetic Field      (Y): ${magneticField(1)*scaleMagnetic} µT")
      println(s"Magnetic Field      (Z): ${magneticField(2)*scaleMagnetic} µT")
      println(s"Angular Velocity    (X): ${angVel(0)*scaleAng} °/s")
      println(s"Angular Velocity    (Y): ${angVel(1)*scaleAng} °/s")
      println(s"Angular Velocity    (Z): ${angVel(2)*scaleAng} °/s")
      println(s"Euler Angle         (H): ${eulerAng(0)*scaleAng} °")
      println(s"Euler Angle         (R): ${eulerAng(1)*scaleAng} °")
      println(s"Euler Angle         (P): ${eulerAng(2)*scaleAng} °")
      println(f"Quaternion          (W): ${quat(0)*scaleQuat}%1.3f")
      println(f"Quaternion          (X): ${quat(1)*scaleQuat}%1.3f")
      println(f"Quaternion          (Y): ${quat(2)*scaleQuat}%1.3f")
      println(f"Quaternion          (Z): ${quat(3)*scaleQuat}%1.3f")
      println(s"Linear Acceleration (X): ${linAccel(0)*scaleAccel} m/s²")
      println(s"Linear Acceleration (Y): ${linAccel(1)*scaleAccel} m/s²")
      println(s"Linear Acceleration (Z): ${linAccel(2)*scaleAccel} m/s²")
      println(f"Gravity Vector      (X): ${gravityVec(0)*scaleGrav}%1.2f m/s²")
      println(f"Gravity Vector      (Y): ${gravityVec(1)*scaleGrav}%1.2f m/s²")
      println(f"Gravity Vector      (Z): ${gravityVec(2)*scaleGrav}%1.2f m/s²")
      println(s"Temperature            : $temp °C")
      println(s"Calibration Status     : ${calibStatus.toBinaryString}")
      println()
    }
    
    // Set period for all data callback to 0.1s (100ms)
    imu.setAllDataPeriod(100)

    println("Press key to exit") 
    Console.in.read()
  }
}
