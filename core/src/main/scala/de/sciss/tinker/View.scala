/*
 *  View.scala
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
import de.sciss.tinker.IMUBrickLike.isBricklet
import org.rogach.scallop.{ScallopConf, ScallopOption => Opt}

import java.net.InetSocketAddress
import scala.swing.event.ButtonClicked
import scala.swing.{Alignment, BorderPanel, GridPanel, Label, MainFrame, Swing, TextField, ToggleButton}

/** Simple swing view showing the euler angles,
  * and option to forward data to Wekinator via OSC.
  */
object View {
  case class Config(uid: String = Common.DefaultIMU_Brick_UID,
                    osc: Boolean = false, oscHost: String = "127.0.0.1", oscPort: Int = 6448,
                    bricklet: Boolean = false)

  val default: Config = Config()

  def main(args: Array[String]): Unit = {
    object p extends ScallopConf(args) {

      import org.rogach.scallop._

      printedName = "View Euler Angles"
      private val default = Config()
      val uid: Opt[String] = opt(short = 'u', name = "uid", default = Some(default.uid),
        descr = s"UID of the IMU brick you want to use (default: ${default.uid})"
      )
      val osc: Opt[Boolean] = toggle(short = 'o', name = "osc", default = Some(default.osc),
        descrYes = s"Enable OSC output to Wekinator (default: ${default.osc})"
      )
      val host: Opt[String] = opt(short = 'h', name = "host", default = Some(default.oscHost),
        descr = s"OSC output host name to Wekinator (default: ${default.oscHost})"
      )
      val port: Opt[Int] = opt(short = 'p', name = "port", default = Some(default.oscPort),
        descr = s"OSC output port to Wekinator (default: ${default.oscPort})"
      )
      val bricklet: Opt[Boolean] = toggle(short = 'b', name = "bricklet",
        descrYes = "Use Bricklet v3 instead of Brick v2"
      )
      verify()

      val config: Config = Config(
        uid       = uid(),
        osc       = osc(),
        oscHost   = host(),
        oscPort   = port(),
        bricklet  = bricklet.getOrElse(isBricklet(uid())),
      )
    }
    implicit val c: Config = p.config
    Swing.onEDT(run())

//    // otherwise VM quits immediately
//    val sync = new AnyRef
//    val t = new Thread(() => sync.synchronized(sync.wait()))
//    t.setDaemon(false)
//    t.start()
  }

  def run()(implicit config: Config): Unit = {
    val c = new IPConnection  // Create IP connection
    // Create device object
    val imu = IMUBrickLike(config.uid, c, bricklet = config.bricklet)
    c.connect(Common.Host, Common.Port)     // Connect to brickd

    var oscEnabled = false

    val ggOSC = if (config.osc) {
      val b = new ToggleButton("Send OSC") {
        reactions += {
          case ButtonClicked(_) =>
            oscEnabled = selected
        }
      }
      Some(b)
    } else {
      None
    }

    val tgt = new InetSocketAddress(config.oscHost, config.oscPort)

    val tOpt = if (config.osc) {
      val cfg = osc.UDP.Config()
      cfg.localIsLoopback = config.oscHost == default.oscHost
      val t = osc.UDP.Transmitter(cfg)
      t.connect()
      Some(t)
    } else {
      None
    }

    def mkField(): TextField =
      new TextField(8) {
        editable = false
      }

    val ggHead  = mkField()
    val ggRoll  = mkField()
    val ggPitch = mkField()

    val ggQW    = mkField()
    val ggQX    = mkField()
    val ggQY    = mkField()
    val ggQZ    = mkField()

    val ggAzi   = mkField()
    val ggEle   = mkField()

    val scaleAng = 1.0/16.0

    imu.addOrientationListener({ (heading: Int, roll: Int, pitch: Int) =>
      val headAng   = heading * scaleAng
      val rollAng   = roll    * scaleAng
      val pitchAng  = pitch   * scaleAng

      if (oscEnabled) tOpt.foreach { t =>
        try {
          t.send(osc.Message("/wek/inputs", headAng.toFloat, rollAng.toFloat, pitchAng.toFloat), tgt)
        } catch {
          case _: Exception =>
            oscEnabled = false
            Console.err.println("Could not send OSC")
            Swing.onEDT {
              ggOSC.foreach(_.selected = false)
            }
        }
      }
      Swing.onEDT {
        ggHead  .text = f"$headAng%1.1f°"
        ggRoll  .text = f"$rollAng%1.1f°"
        ggPitch .text = f"$pitchAng%1.1f°"
      }
    })

    imu.setOrientationPeriod(20)
    
    case class Pt3 (x: Double, y: Double, z: Double) {
      def toLatLon: LatLon = {
        val theta = math.acos(z)
        val phi   = math.atan2(y, x)
        val lat   = math.Pi/2 - theta
        val lon   = phi
        LatLon(lat, lon)
      }
    }

    case class Quat(w: Double, x: Double, y: Double, z: Double)

    case class LatLon(lat: Double, lon: Double)

    def quatMul(q: Array[Double], r: Array[Double], res: Array[Double]): Unit = {
      res(0) = r(0) * q(0) - r(1) * q(1) - r(2) * q(2) - r(3) * q(3)
      res(1) = r(0) * q(1) + r(1) * q(0) - r(2) * q(3) + r(3) * q(2)
      res(2) = r(0) * q(2) + r(1) * q(3) + r(2) * q(0) - r(3) * q(1)
      res(3) = r(0) * q(3) - r(1) * q(2) + r(2) * q(1) + r(3) * q(0)
    }

    def rotatePoint(p: Pt3, quat: Quat): Pt3 = {
      val r     = Array(0.0, p.x, p.y, p.z)
      val q     = Array(quat.w, quat.x, quat.y, quat.z)
      val tmp   = new Array[Double](4)
      quatMul(q, r, tmp)
      q(1) = -q(1)
      q(2) = -q(2)
      q(3) = -q(3)
      quatMul(tmp, q, r)
      Pt3(r(1), r(2), r(3))
    }

    val scaleQuat = 1.0/16383.0

    imu.addQuaternionListener({ (wi: Int, xi: Int, yi: Int, zi: Int) =>
        Swing.onEDT {
          val w = wi * scaleQuat
          val x = xi * scaleQuat
          val y = yi * scaleQuat
          val z = zi * scaleQuat

          ggQW.text = f"$w%1.2f"
          ggQX.text = f"$x%1.2f"
          ggQY.text = f"$y%1.2f"
          ggQZ.text = f"$z%1.2f"

          val rot = rotatePoint(Pt3(1, 0, 0), Quat(w, x, y , z))
          val ll = rot.toLatLon

          ggAzi.text = f"${-ll.lon.toDegrees}%1.1f°"
          ggEle.text = f"${-ll.lat.toDegrees}%1.1f°"
        }
    })

//    imu.addAccelerationListener()

    imu.setQuaternionPeriod(20)

    val pPar: GridPanel = new GridPanel(0, 2) {
      hGap = 8
      vGap = 2
      contents ++= Seq(
        new Label("Heading:", null, Alignment.Trailing), ggHead,
        new Label("Roll:"   , null, Alignment.Trailing), ggRoll,
        new Label("Pitch:"  , null, Alignment.Trailing), ggPitch,
        Swing.HGlue, Swing.HGlue,
        new Label("W:", null, Alignment.Trailing), ggQW,
        new Label("X:", null, Alignment.Trailing), ggQX,
        new Label("Y:", null, Alignment.Trailing), ggQY,
        new Label("Z:", null, Alignment.Trailing), ggQZ,
        Swing.HGlue, Swing.HGlue,
        new Label("Azimuth:"  , null, Alignment.Trailing), ggAzi,
        new Label("Elevation:", null, Alignment.Trailing), ggEle,
      )
    }

    new MainFrame {
      title = "IMU Orientation"
      contents = new BorderPanel {
        add(pPar, BorderPanel.Position.Center)
        layoutManager.setVgap(8)
        ggOSC.foreach { b =>
          add(b, BorderPanel.Position.South)
        }
      }
      pack().centerOnScreen()
      open()
    }
  }
}
