/*
 *  IMU_OSC.scala
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

object IMU_OSC {
  // "amveqlgtc"
  // 'a' acceleration (x,y,z)
  // 'm' magnetic field (x,y,z)
  // 'v' angular velocity (x,y,z)
  // 'e' euler angle (h,r,p)
  // 'q' quaternion (w,x,y,z)
  // 'l' linear acceleration (x,y,z)
  // 'g' gravity vector (x,y,z)
  // 't' temperature (t)
  // 'c' calibration status (c)

  final case class Config()

  def main(args: Array[String]): Unit = {

  }
}
