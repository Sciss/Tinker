/*
 *  IMUBrickLike.scala
 *  (Tinker)
 *
 *  Copyright (c) 2018-2023 Hanns Holger Rutz. All rights reserved.
 *
 *  This software is published under the GNU Lesser General Public License v2.1+
 *
 *
 *  For further information, please contact Hanns Holger Rutz at
 *  contact@sciss.de
 */

package de.sciss.tinker

import com.tinkerforge.{BrickIMUV2, BrickletIMUV3, IPConnection}

/** Abstracts over IMU Brick v2 and IMU Bricklet v3 */
object IMUBrickLike {
  def apply(uid: String, c: IPConnection, bricklet: Boolean): IMUBrickLike =
    if (bricklet) brickletV3(uid, c) else brickV2(uid, c)

  /** If the uid is four characters or less, returns `true`, if it is longer returns `false`.
    * This seems to be correct in my configuration.
    */
  def isBricklet(uid: String): Boolean = uid.length <= 4

  def brickV2(uid: String, c: IPConnection): IMUBrickLike =
    new IMUBrickLike {
      private val peer = new BrickIMUV2(uid, c)

      override def setOrientationPeriod(p: Int): Unit = peer.setOrientationPeriod(p)

      override def addOrientationListener(f: (Int, Int, Int) => Unit): Unit = peer.addOrientationListener(
        (heading: Short, roll: Short, pitch: Short) => f(heading, roll, pitch)
      )

      override def setQuaternionPeriod(p: Int): Unit = peer.setQuaternionPeriod(p)

      override def addQuaternionListener(f: (Int, Int, Int, Int) => Unit): Unit = peer.addQuaternionListener(
        (w: Short, x: Short, y: Short, z: Short) => f.apply(w, x, y, z)
      )

      override def getQuaternion: Quaternion = {
        val q = peer.getQuaternion
        Quaternion(w = q.w, x = q.x, y = q.y, z = q.z)
      }

      override def setAllDataPeriod(p: Int): Unit = peer.setAllDataPeriod(p)

      override def addAllDataListener(f: AllData => Unit): Unit = peer.addAllDataListener(
        new BrickIMUV2.AllDataListener {
          private def convert(xs: Array[Short]): Array[Int] = {
            val sz = xs.length
            val ys = new Array[Int](sz)
            var i = 0
            while (i < sz) {
              ys(i) = xs(i)
              i += 1
            }
            ys
          }

          override def allData(acceleration     : Array[Short], magneticField     : Array[Short],
                               angularVelocity  : Array[Short], eulerAngle        : Array[Short],
                               quaternion       : Array[Short], linearAcceleration: Array[Short],
                               gravityVector    : Array[Short],
                               temperature      : Byte,
                               calibrationStatus: Short,
                              ): Unit = {
            val d = new AllData(
              acceleration        = convert(acceleration),
              magneticField       = convert(magneticField),
              angularVelocity     = convert(angularVelocity),
              eulerAngle          = convert(eulerAngle),
              quaternion          = convert(quaternion),
              linearAcceleration  = convert(linearAcceleration),
              gravityVector       = convert(gravityVector),
              temperature         = temperature,
              calibrationStatus   = calibrationStatus,
            )
            f.apply(d)
          }
        }
      )
    }

  def brickletV3(uid: String, c: IPConnection): IMUBrickLike =
    new IMUBrickLike {
      private val peer = new BrickletIMUV3(uid, c)
      override def setOrientationPeriod(p: Int): Unit = peer.setOrientationCallbackConfiguration(p, false)

      override def addOrientationListener(f: (Int, Int, Int) => Unit): Unit =
        peer.addOrientationListener((heading: Int, roll: Int, pitch: Int) => f.apply(heading, roll, pitch))

      override def setQuaternionPeriod(p: Int): Unit = peer.setQuaternionCallbackConfiguration(p, false)

      override def addQuaternionListener(f: (Int, Int, Int, Int) => Unit): Unit = peer.addQuaternionListener(
        (w: Int, x: Int, y: Int, z: Int) => f.apply(w, x, y, z)
      )

      override def getQuaternion: Quaternion = {
        val q = peer.getQuaternion
        Quaternion(w = q.w, x = q.x, y = q.y, z = q.z)
      }

      override def setAllDataPeriod(p: Int): Unit = peer.setAllDataCallbackConfiguration(p, false)

      override def addAllDataListener(f: AllData => Unit): Unit = peer.addAllDataListener(
        new BrickletIMUV3.AllDataListener {
          override def allData(acceleration     : Array[Int], magneticField     : Array[Int],
                               angularVelocity  : Array[Int], eulerAngle        : Array[Int],
                               quaternion       : Array[Int], linearAcceleration: Array[Int],
                               gravityVector    : Array[Int],
                               temperature      : Int,
                               calibrationStatus: Int
                              ): Unit = {
            val d = new AllData(
              acceleration        = acceleration,
              magneticField       = magneticField,
              angularVelocity     = angularVelocity,
              eulerAngle          = eulerAngle,
              quaternion          = quaternion,
              linearAcceleration  = linearAcceleration,
              gravityVector       = gravityVector,
              temperature         = temperature,
              calibrationStatus   = calibrationStatus,
            )
            f.apply(d)
          }
        }
      )
    }

  final case class Quaternion(w: Int, x: Int, y: Int, z: Int) {
    override def toString: String =
      "[" + "w = " + w + ", " + "x = " + x + ", " + "y = " + y + ", " + "z = " + z + "]";
  }

  final class AllData(
                       val acceleration       : Array[Int],
                       val magneticField      : Array[Int],
                       val angularVelocity    : Array[Int],
                       val eulerAngle         : Array[Int],
                       val quaternion         : Array[Int],
                       val linearAcceleration : Array[Int],
                       val gravityVector      : Array[Int],
                       val temperature        : Int,
                       val calibrationStatus  : Int,
                     )
}
trait IMUBrickLike {
  def setOrientationPeriod(p: Int): Unit

  def addOrientationListener(f: (Int, Int, Int) => Unit): Unit

  def setQuaternionPeriod(p: Int): Unit

  def addQuaternionListener(f: (Int, Int, Int, Int) => Unit): Unit

  def getQuaternion: IMUBrickLike.Quaternion

  def setAllDataPeriod(p: Int): Unit

  def addAllDataListener(f: IMUBrickLike.AllData => Unit): Unit
}
