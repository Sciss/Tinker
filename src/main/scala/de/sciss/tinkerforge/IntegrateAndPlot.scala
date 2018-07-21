/*
 *  IntegrateAndPlot.scala
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

import de.sciss.file._
import de.sciss.kollflitz
import org.jzy3d.chart.{AWTChart, ChartLauncher}
import org.jzy3d.colors.Color
import org.jzy3d.maths.doubles.Coord3D
import org.jzy3d.maths.{Coord3d, Rectangle}
import org.jzy3d.plot3d.primitives.Point
import org.jzy3d.plot3d.rendering.canvas.Quality

import scala.collection.JavaConverters._

object IntegrateAndPlot {
  case class Config(
                     fIn    : File    = file("in"),
                     period : Int     = 10,
                     zeroVel: Boolean = false,
                     noZ    : Boolean = false
                   )

  def main(args: Array[String]): Unit = {
    val default = Config()

    val p = new scopt.OptionParser[Config]("RecordAccel") {
      opt[File]('f', "file")
        .required()
        .text ("Input file for binary linear acceleration data.")
        .action { (v, c) => c.copy(fIn = v) }

      opt[Int]('p', "period")
        .validate { v => if (v >= 1) success else failure("Must be >= 1") }
        .text (s"Data polling period in ms (default: ${default.period})")
        .action { (v, c) => c.copy(period = v) }

      opt[Unit]('z', "zero-velocity")
        .text ("Assume start and end velocity are zero.")
        .action { (_, c) => c.copy(zeroVel = true) }

      opt[Unit]("no-vertical")
        .text ("Assume all Z coordinates are zero.")
        .action { (_, c) => c.copy(noZ = true) }
    }
    p.parse(args, default).fold(sys.exit(1))(run)
  }

  def run(config: Config): Unit = {
    val scaleAccel  = 1.0/  100.0             // to get to metres per squared-seconds
    val dt          = config.period / 1000.0  // seconds

    val raw   = RecordAccel.readData(config.fIn)
    val accel = raw.grouped(3).map { case Array(x, y, z) =>
      new Coord3D(x * scaleAccel, y * scaleAccel, z * scaleAccel)
    } .toVector

    import kollflitz.Ops._

    implicit object Coord3DNumeric extends Numeric[Coord3D] {
      private def undefined(name: String): Nothing = throw new UnsupportedOperationException(name)

      def plus(a: Coord3D, b: Coord3D): Coord3D =
        new Coord3D(a.x + b.x, a.y + b.y, a.z + b.z)

      def minus(a: Coord3D, b: Coord3D): Coord3D =
        new Coord3D(a.x - b.x, a.y - b.y, a.z - b.z)

      def times(a: Coord3D, b: Coord3D): Coord3D =
        new Coord3D(a.x * b.x, a.y * b.y, a.z * b.z)

      def negate(a: Coord3D): Coord3D =
        new Coord3D(-a.x, -a.y, -a.z)

      def fromInt(a: Int): Coord3D =
        new Coord3D(a, a, a)

      def toInt   (a: Coord3D ): Int      = undefined("toInt")
      def toLong  (a: Coord3D ): Long     = undefined("toLong")
      def toFloat (a: Coord3D ): Float    = undefined("toFloat")
      def toDouble(a: Coord3D ): Double   = undefined("toDouble")

      def compare(a: Coord3D, b: Coord3D): Int = {
        if      (a.x < b.x) -1
        else if (a.x > b.x) +1
        else if (a.y < b.y) -1
        else if (a.y > b.y) +1
        else if (a.z < b.z) -1
        else if (a.z > b.z) +1
        else 0
      }
    }

//    val accel1      = (new Coord3D(0,0,0) +: accel).scanLeft(new Coord3D(0,0,0))((p, in) =>
//      new Coord3D(in.x * 0.02 + p.x * 0.98, in.y * 0.02 + p.y * 0.98, in.z * 0.02 + p.z * 0.98)
//    )

    val accel1 = accel

    val vel0        = (new Coord3D(0,0,0) +: accel1).integrate.map(_ mul dt)
    val diffVel     = vel0.last
    val weight1     = 1.0 / (vel0.size - 1)
    val ctlCoords1  = if (!config.zeroVel) vel0 else vel0.zipWithIndex.map { case (c, i) =>
      val w = i * weight1
      new Coord3D(c.x - diffVel.x * w, c.y - diffVel.y * w, c.z - diffVel.z * w)
    }
    val pos0        = (new Coord3D(0,0,0) +: ctlCoords1).integrate.map(_ mul dt)
    val diffPos     = pos0.last
    val weight2     = 1.0 / (pos0.size - 1)
    val pos1        = pos0.zipWithIndex.map { case (c, i) =>
      val w = i * weight2
      new Coord3D(c.x - diffPos.x * w, c.y - diffPos.y * w, c.z - diffPos.z * w)
    }
    val pos2        = if (config.noZ) pos1.map(_.mul(1.0, 1.0, 0.0)) else pos1
    val pos         = pos2

    println(s"x mean-var: ${accel.map(_.x).meanVariance}")
    println(s"y mean-var: ${accel.map(_.y).meanVariance}")
    println(s"z mean-var: ${accel.map(_.z).meanVariance}")
    println(accel     .take(20))
    println(pos .take(20))
    println(pos .takeRight(20))

//    val intp        = new BernsteinInterpolator
//    val intpCoords  = intp.interpolate(ctlCoords.asJava, 30)
//    val ctlPts      = ctlCoords         .map(new Point(_, Color.RED , 5.0f))
    val ctlPts      = pos.map(c => new Point(new Coord3d(c.x.toFloat, c.y.toFloat, c.z.toFloat), Color.BLUE, 1.0f))
//    val intpPts     = intpCoords.asScala.map(new Point(_, Color.BLUE, 3.0f))

//    val line = new LineStrip(intpCoords)
//    line.setWireframeColor(Color.BLACK)

    val chart = new AWTChart(Quality.Intermediate)
//    chart.add(line)
    chart.add(ctlPts .asJava)
//    chart.add(intpPts.asJava)

    ChartLauncher.instructions()
    ChartLauncher.openChart(chart, new Rectangle(0, 0, 600, 600), "Position")
  }
}
