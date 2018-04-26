/*
 *  RecordAccel.scala
 *  (TinkerForgeIMU2Test)
 *
 *  Copyright (c) 2018 Hanns Holger Rutz. All rights reserved.
 *
 *  This software is published under the GNU Lesser General Public License v2.1+
 *
 *
 *  For further information, please contact Hanns Holger Rutz at
 *  contact@sciss.de
 */

package de.sciss.tinkerforge

import java.io.{DataInputStream, DataOutputStream, FileInputStream, FileOutputStream}

import de.sciss.file._
import com.tinkerforge.{BrickIMUV2, IPConnection}

object RecordAccel {
  case class Config(
                     uid      : String  = Common.DefaultIMU_UID,
                     fOut     : File    = file("out"),
                     maxRecord: Double  = 60.0,
                     skip     : Double  =  4.0,
                     period   : Int     = 10
                   )

  def main(args: Array[String]): Unit = {
    val default = Config()

    val p = new scopt.OptionParser[Config]("RecordAccel") {
      opt[File]('f', "file")
        .required()
        .text ("Output file for binary linear acceleration data.")
        .action { (v, c) => c.copy(fOut = v) }
      opt[String]('u', "uid")
        .text (s"UID of the IMU brick you want to use (default: ${default.uid})")
        .action { (v, c) => c.copy(uid = v) }
      opt[Double]('t', "duration")
        .text (s"Maximum record duration in seconds (default: ${default.maxRecord})")
        .validate { v => if (v >= 1.0) success else failure("Must be >= 1.0") }
        .action { (v, c) => c.copy(maxRecord = v) }
      opt[Double]('s', "skip")
        .validate { v => if (v >= 0.0) success else failure("Must be >= 0.0") }
        .text (s"Initial skip time before recording in seconds (default: ${default.skip})")
        .action { (v, c) => c.copy(skip = v) }
      opt[Int]('p', "period")
        .validate { v => if (v >= 1) success else failure("Must be >= 1") }
        .text (s"Data polling period in ms (default: ${default.period})")
        .action { (v, c) => c.copy(period = v) }
    }
    p.parse(args, default).fold(sys.exit(1))(run)
  }

  final val COOKIE = 0x4C6E4163 // "LnAc"

  def writeData(acc: Array[Short], off: Int, len: Int, fOut: File): Unit = {
    val fos = new FileOutputStream(fOut)
    try {
      val dos = new DataOutputStream(fos)
      dos.writeInt(COOKIE)
      dos.writeInt(len)
      var i = off * 3
      val j = (off + len) * 3
      while (i < j) {
        dos.writeShort(acc(i)); i += 1
        dos.writeShort(acc(i)); i += 1
        dos.writeShort(acc(i)); i += 1
      }
      dos.flush()
    } finally {
      fos.close()
    }
  }

  def readData(fIn: File): Array[Short] = {
    val fis = new FileInputStream(fIn)
    try {
      val dis = new DataInputStream(fis)
      val cookie = dis.readInt()
      require (cookie == COOKIE, s"Unexpected cookie ${cookie.toHexString} != ${COOKIE.toHexString}")
      val len = dis.readInt()
      val arr = new Array[Short](len * 3)
      var i = 0
      val j = len * 3
      while (i < j) {
        arr(i) = dis.readShort(); i += 1
        arr(i) = dis.readShort(); i += 1
        arr(i) = dis.readShort(); i += 1
      }
      arr
    } finally {
      fis.close()
    }
  }

  def run(config: Config): Unit = {
    config.fOut.createNewFile()
    require (config.fOut.canWrite, s"File ${config.fOut} is not writable.")

    val c   = new IPConnection
    val imu = new BrickIMUV2(config.uid, c)
    c.connect(Common.Host, Common.Port)

    val skip      = System.currentTimeMillis() + (config.skip * 1000).toLong
    val stop      = skip + (config.maxRecord * 1000).toLong
    val maxSmp    = (config.maxRecord * 1000).toInt / config.period + 1

    val timeSize  = maxSmp
    val arrSize   = timeSize * 3
    val arr       = new Array[Short](arrSize)
    val time      = new Array[Long ](timeSize)
    var cnt       = 0
    var cntT      = 0

    imu.addLinearAccelerationListener { (x: Short, y: Short, z: Short) =>
      val t = System.currentTimeMillis()
      if (t >= skip) {
        time(cntT) = t
        val _arr = arr
        var i = cnt
        _arr(i) = x; i += 1
        _arr(i) = y; i += 1
        _arr(i) = z; i += 1
        cnt = i
        cntT += 1
        if (i == arrSize || t >= stop) {
          try {
            imu.setLinearAccelerationPeriod(0)
            c.disconnect()
          } finally {
            writeData(arr, 0, i/3, config.fOut)
            //          i = 0
            ////          val scale = 1.0/  100.0
            //          val j     = timeSize/2
            val dt    = cntT - 1
            println(f"Effective rate: ${dt / ((time(cntT - 1) - time(0)) * 0.001)}%1.1f Hz")
            ////          while (i < arrSize) {
            ////            println(f"${_arr(i + 0) * scale}%1.2f, ${_arr(i + 1) * scale}%1.2f, ${_arr(i + 2) * scale}%1.2f")
            ////            i += 3
            ////          }
            sys.exit()
          }
        }
      }
    }

    imu.ledsOff()
    imu.disableStatusLED()
    println(s"SensorFusionMode   : ${imu.getSensorFusionMode}")
    // default: [magnetometerRate = 5, gyroscopeRange = 0, gyroscopeBandwidth = 7, accelerometerRange = 1, accelerometerBandwidth = 3]
    imu.setSensorConfiguration(5, 0, 7, 1, 3)
//    imu.setSensorConfiguration(5, 0, 3, 1, 7)
    println(s"SensorConfiguration: ${imu.getSensorConfiguration}")
    imu.setSensorFusionMode(1)
    imu.setAccelerationPeriod       (0L)
    imu.setOrientationPeriod        (0L)
    imu.setQuaternionPeriod         (0L)
    imu.setTemperaturePeriod        (0L)
    imu.setAllDataPeriod            (0L)
    imu.setAngularVelocityPeriod    (0L)
    imu.setGravityVectorPeriod      (0L)
    imu.setMagneticFieldPeriod      (0L)
    imu.setLinearAccelerationPeriod (config.period)
    Thread.sleep((config.skip * 1000).toLong)
    println("Recording...")
    Thread.sleep(Long.MaxValue)
  }
}
