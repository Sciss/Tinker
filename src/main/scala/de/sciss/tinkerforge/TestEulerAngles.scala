/*
 *  TestEulerAngles.scala
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

import com.tinkerforge.BrickIMUV2.OrientationListener
import com.tinkerforge.{BrickIMUV2, IPConnection}

import scala.swing.{Alignment, Frame, GridPanel, Label, MainFrame, SimpleSwingApplication, Swing, TextField}

/** Simple swing view showing the euler angles */
object TestEulerAngles extends SimpleSwingApplication {
  lazy val top: Frame = {

    val c = new IPConnection
    // Create IP connection
    val imu = new BrickIMUV2(Common.DefaultIMU_UID, c) // Create device object
    c.connect(Common.Host, Common.Port)     // Connect to brickd

    def mkField(): TextField =
      new TextField(8) {
        editable = false
      }

    val ggHead  = mkField()
    val ggRoll  = mkField()
    val ggPitch = mkField()

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

    new MainFrame {
      title = "IMU Orientation"
      contents = new GridPanel(3, 2) {
        hGap = 8
        vGap = 2
        contents ++= Seq(
          new Label("Heading:", null, Alignment.Trailing), ggHead,
          new Label("Roll:"   , null, Alignment.Trailing), ggRoll,
          new Label("Pitch:"  , null, Alignment.Trailing), ggPitch,
        )
      }
      pack().centerOnScreen()
      open()
    }
  }
}
