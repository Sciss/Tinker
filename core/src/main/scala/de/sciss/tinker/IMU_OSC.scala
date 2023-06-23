/*
 *  IMU_OSC.scala
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

import com.tinkerforge.IPConnection
import de.sciss.osc
import de.sciss.tinker.IMUBrickLike.{AllData, isBricklet}
import org.rogach.scallop.{ScallopConf, ScallopOption => Opt}

import java.net.{InetAddress, InetSocketAddress}
import scala.collection.immutable.ArraySeq
import scala.util.control.NonFatal

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
  
  final case class Config(
                           sensor     : List[String]  = List("el", "el"),
                           uid        : List[String]  = List(Common.DefaultIMU_Brick_UID, Common.DefaultIMU_Bricklet_UID),
                           cmd        : List[String]  = List("/sensor1", "/sensor2"),
                           rate       : List[Int]     = List(10, 10),
                           mul        : List[Float]   = List(1f, 1f),
                           add        : List[Float]   = List(0f, 0f),
                           targetHost : String  = "127.0.0.1",
                           targetPort : Int     = 7771,
                           sourceHost : String  = "127.0.0.1",
                           sourcePort : Int     = 0,
                           tcp        : Boolean = false,
                           verbose    : Boolean = false,
                         )

  object DataType {
    private final val scaleAccel    = 1.0f / 100.0f
    private final val scaleMagnetic = 1.0f / 16.0f
    private final val scaleAng      = 1.0f / 16.0f
    private final val scaleQuat     = 1.0f / 16383.0f
    private final val scaleGrav     = 1.0f / 100.0f

    object Acc extends DataType('a') {
      override val encodeSize: Int = 3

      override def encode(a: AllData, out: Array[Float], off: Int): Unit = {
        val arr   = a.acceleration
        val scale = scaleAccel
        out(off + 0) = arr(0) * scale: Float
        out(off + 1) = arr(1) * scale: Float
        out(off + 2) = arr(2) * scale: Float
      }
    }
    object MagField extends DataType('m') {
      override val encodeSize: Int = 3

      override def encode(a: AllData, out: Array[Float], off: Int): Unit = {
        val arr = a.magneticField
        val scale = scaleMagnetic
        out(off + 0) = arr(0) * scale
        out(off + 1) = arr(1) * scale
        out(off + 2) = arr(2) * scale
      }
    }
    object AngVel extends DataType('v') {
      override val encodeSize: Int = 3

      override def encode(a: AllData, out: Array[Float], off: Int): Unit = {
        val arr = a.angularVelocity
        val scale = scaleAng
        out(off + 0) = arr(0) * scale
        out(off + 1) = arr(1) * scale
        out(off + 2) = arr(2) * scale
      }
    }
    object Euler extends DataType('e') {
      override val encodeSize: Int = 3

      override def encode(a: AllData, out: Array[Float], off: Int): Unit = {
        val arr = a.eulerAngle
        val scale = scaleAng
        out(off + 0) = arr(0) * scale: Float
        out(off + 1) = arr(1) * scale: Float
        out(off + 2) = arr(2) * scale: Float
      }
    }
    object Quat extends DataType('q') {
      override val encodeSize: Int = 4

      override def encode(a: AllData, out: Array[Float], off: Int): Unit = {
        val arr = a.quaternion
        val scale = scaleQuat
        out(off + 0) = arr(0) * scale: Float
        out(off + 1) = arr(1) * scale: Float
        out(off + 2) = arr(2) * scale: Float
        out(off + 3) = arr(3) * scale: Float
      }
    }
    object LinAcc extends DataType('l') {
      override val encodeSize: Int = 3

      override def encode(a: AllData, out: Array[Float], off: Int): Unit = {
        val arr = a.linearAcceleration
        val scale = scaleAccel
        out(off + 0) = arr(0) * scale
        out(off + 1) = arr(1) * scale
        out(off + 2) = arr(2) * scale
      }
    }
    object Gravity extends DataType('g') {
      override val encodeSize: Int = 3

      override def encode(a: AllData, out: Array[Float], off: Int): Unit = {
        val arr = a.gravityVector
        val scale = scaleGrav
        out(off + 0) = arr(0) * scale
        out(off + 1) = arr(1) * scale
        out(off + 2) = arr(2) * scale
      }
    }
    object Temp extends DataType('t') {
      override val encodeSize: Int = 1

      override def encode(a: AllData, out: Array[Float], off: Int): Unit =
        out(off) = a.temperature.toFloat
    }
    object Status extends DataType('c') {
      override val encodeSize: Int = 1

      override def encode(a: AllData, out: Array[Float], off: Int): Unit =
        out(off) = a.calibrationStatus.toFloat
    }

    val all: Seq[DataType] = Seq(
      Acc, MagField, AngVel, Euler, Quat, LinAcc, Gravity, Temp, Status
    )

    val map: Map[Char, DataType] = all.map(dt => dt.ch -> dt).toMap
  }
  abstract case class DataType(ch: Char) {
    def encodeSize: Int

    def encode(a: AllData, out: Array[Float], off: Int): Unit
  }

  def main(args: Array[String]): Unit = {
    object p extends ScallopConf(args) {
      import org.rogach.scallop._

      printedName = "IMU OSC"
      private val default = Config()

      val sensor: Opt[List[String]] = opt(default = Some(default.sensor),
        descr = s"""Sensor data to transmit, list of strings (default: ${default.sensor.mkString(" ")}).
            |"amveqlgtc"
            |'a' acceleration (x,y,z)
            |'m' magnetic field (x,y,z)
            |'v' angular velocity (x,y,z)
            |'e' euler angle (h,r,p)
            |'q' quaternion (w,x,y,z)
            |'l' linear acceleration (x,y,z)
            |'g' gravity vector (x,y,z)
            |'t' temperature (t)
            |'c' calibration status (c)
            |""".stripMargin,
        validate = x => x.forall(s => s.forall(DataType.map.contains)),
      )
      val uid: Opt[List[String]] = opt(default = Some(default.uid),
        descr = s"UID of IMU sensors (three chars indicate bricklet, more chars indicate brick) (default: ${default.uid.mkString(" ")}).",
      )
      val cmd: Opt[List[String]] = opt(default = Some(default.cmd),
        descr = s"OSC commands to use (default: ${default.cmd.mkString(" ")}).",
        validate = x => x.forall(_.startsWith("/")),
      )
      val rate: Opt[List[Int]] = opt(default = Some(default.rate),
        descr = s"Sensor data period in ms (default: ${default.rate.mkString(" ")}).",
        validate = x => x.forall(_ >= 0)
      )
      val mul: Opt[List[Float]] = opt(default = Some(default.mul),
        descr = s"Sensor data scaling factor (default: ${default.mul.mkString(" ")}).",
      )
      val add: Opt[List[Float]] = opt(default = Some(default.add),
        descr = s"Sensor data scaling offset (default: ${default.add.mkString(" ")}).",
      )
      val targetHost: Opt[String] = opt(default = Some(default.targetHost),
        descr = s"Target OSC host (default: ${default.targetHost}).",
      )
      val targetPort: Opt[Int] = opt(default = Some(default.targetPort),
        descr = s"Target OSC port (default: ${default.targetPort}).",
      )
      val sourceHost: Opt[String] = opt(default = Some(default.sourceHost),
        descr = s"Source OSC host (default: ${default.sourceHost}).",
      )
      val sourcePort: Opt[Int] = opt(default = Some(default.sourcePort),
        descr = s"Source OSC port, or zero to pick any (default: ${default.sourcePort}).",
      )
      val tcp: Opt[Boolean] = toggle(default = Some(default.tcp),
        descrYes = "Use TCP instead of UDP",
      )
      val verbose: Opt[Boolean] = toggle(default = Some(default.verbose),
        descrYes = "Turn verbose on",
      )

      verify()

      require (sensor().size == uid() .size, s"Number of sensors and unique ids must match")
      require (sensor().size == cmd() .size, s"Number of sensors and commands must match")
      require (sensor().size == rate().size, s"Number of sensors and rates must match")

      val config: Config = Config(
        sensor      = sensor(),
        uid         = uid(),
        cmd         = cmd(),
        rate        = rate(),
        mul         = mul(),
        add         = add(),
        targetHost  = targetHost(),
        targetPort  = targetPort(),
        sourceHost  = sourceHost(),
        sourcePort  = sourcePort(),
        tcp         = tcp(),
        verbose     = verbose(),
      )
    }

    implicit val c: Config = p.config
    run()
  }

  def run()(implicit config: Config): Unit = {
    val sock = new InetSocketAddress(config.targetHost, config.targetPort)
    val send: osc.Message => Unit = if (config.tcp) {
      val cfg = osc.TCP.Config()
//      cfg.localIsLoopback = config.targetHost == "127.0.0.1"
      cfg.localAddress  = InetAddress.getByName(config.sourceHost)
      cfg.localPort     = config.sourcePort
      val t = osc.TCP.Transmitter(sock, cfg)
      t.connect()
      if (config.verbose) t.dump()
      t ! _
    } else {
      val cfg = osc.UDP.Config()
//      cfg.localIsLoopback = config.targetHost == "127.0.0.1"
      cfg.localAddress  = InetAddress.getByName(config.sourceHost)
      cfg.localPort = config.sourcePort
      val t = osc.UDP.Transmitter(cfg)
      t.connect()
      if (config.verbose) t.dump()
      t.send(_, sock)
    }

    val c   = new IPConnection // Create IP connection
    val num = config.sensor.size
    val imuSeq = Seq.tabulate(num) { i =>
      val uid       = config.uid(i)
      val bricklet  = isBricklet(uid)
      val imu       = IMUBrickLike(uid, c, bricklet = bricklet)
      imu
    }
    c.connect(Common.Host, Common.Port) // Connect to brickd

    for (i <- 0 until num) {
      val imu     = imuSeq(i)
      val dataSeq = config.sensor(i).toArray.map { ch =>
        DataType.map(ch)
      }
      val dataSize  = dataSeq.map(_.encodeSize).sum
      val cmd       = config.cmd(i)
      val mul       = config.mul(i)
      val add       = config.add(i)
      imu.setAllDataPeriod(config.rate(i))
      imu.addAllDataListener { d =>
        val args = new Array[Float](dataSize)
        var j = 0
        var off = 0
        while (j < dataSeq.length) {
          val dt = dataSeq(j)
          dt.encode(d, args, off)
          val stop = off + dt.encodeSize
          while (off < stop) {
            args(off) = args(off) * mul + add
            off += 1
          }
          j += 1
        }
        val b = osc.Message(cmd, ArraySeq.unsafeWrapArray(args): _*)
        try {
          send(b)
        } catch {
          case NonFatal(ex) =>
            println(s"OSC send failed: ${ex.getClass.getName}")
//            ex.printStackTrace()
        }
      }
    }

    println("Press key to exit")
    Console.in.read()
  }
}
