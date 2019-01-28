/*
 *  ViewOrientation.scala
 *  (TinkerForgeIMU2Test)
 *
 *  Copyright (c) 2018-2019 Hanns Holger Rutz. All rights reserved.
 *
 *  This software is published under the GNU Lesser General Public License v2.1+
 *
 *
 *  For further information, please contact Hanns Holger Rutz at
 *  contact@sciss.de
 */

package de.sciss.tinkerforge

import com.tinkerforge.BrickIMUV2.{OrientationListener, QuaternionListener}
import com.tinkerforge.{BrickIMUV2, IPConnection}

import scala.swing.{Alignment, GridPanel, Label, MainFrame, Swing, TextField}

/** Simple swing view showing the euler angles */
object ViewOrientation {
  case class Config(uid: String = Common.DefaultIMU_UID)

  def main(args: Array[String]): Unit = {
    val default = Config()

    val p = new scopt.OptionParser[Config]("TestEulerAngles") {
      opt[String]('u', "uid")
        .text (s"UID of the IMU brick you want to use (default: ${default.uid})")
        .action { (v, c) => c.copy(uid = v) }
    }
    p.parse(args, default).fold(sys.exit(1)) { config =>
      Swing.onEDT(run(config))
    }
  }

  def run(config: Config): Unit = {
    val c = new IPConnection
    // Create IP connection
    val imu = new BrickIMUV2(config.uid, c) // Create device object
    c.connect(Common.Host, Common.Port)     // Connect to brickd

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

    imu.addOrientationListener(new OrientationListener {
      private[this] val scaleAng = 1.0/16.0

      def orientation(heading: Short, roll: Short, pitch: Short): Unit = {
        Swing.onEDT {
          ggHead  .text = f"${heading * scaleAng}%1.1f°"
          ggRoll  .text = f"${roll    * scaleAng}%1.1f°"
          ggPitch .text = f"${pitch   * scaleAng}%1.1f°"
        }
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

    imu.addQuaternionListener(new QuaternionListener {
      private[this] val scaleQuat = 1.0/16383.0

      def quaternion(wi: Short, xi: Short, yi: Short, zi: Short): Unit = {
        Swing.onEDT {
          val w = wi *scaleQuat
          val x = xi *scaleQuat
          val y = yi *scaleQuat
          val z = zi *scaleQuat

          ggQW.text = f"$w%1.2f"
          ggQX.text = f"$x%1.2f"
          ggQY.text = f"$y%1.2f"
          ggQZ.text = f"$z%1.2f"

          val rot = rotatePoint(Pt3(1, 0, 0), Quat(w, x, y , z))
          val ll = rot.toLatLon

          ggAzi.text = f"${-ll.lon.toDegrees}%1.1f°"
          ggEle.text = f"${-ll.lat.toDegrees}%1.1f°"
        }
      }
    })

    imu.setQuaternionPeriod(20)

    new MainFrame {
      title = "IMU Orientation"
      contents = new GridPanel(0, 2) {
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
      pack().centerOnScreen()
      open()
    }
  }
}