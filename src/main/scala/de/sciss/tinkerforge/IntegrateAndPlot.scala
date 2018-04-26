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
                     fIn: File = file("in")
                   )

  def main(args: Array[String]): Unit = {
    val default = Config()

    val p = new scopt.OptionParser[Config]("RecordAccel") {
      opt[File]('f', "file")
        .required()
        .text ("Input file for binary linear acceleration data.")
        .action { (v, c) => c.copy(fIn = v) }
    }
    p.parse(args, default).fold(sys.exit(1))(run)
  }

  def run(config: Config): Unit = {
    val raw   = RecordAccel.readData(config.fIn)
    val accel = raw.grouped(3).map { case Array(x, y, z) =>
      new Coord3D(x * 0.01, y * 0.01, z * 0.01)
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

    val ctlCoords0  = (new Coord3D(0,0,0) +: accel1).integrate
    val diffVel     = ctlCoords0.last
    val weight1     = 1.0 / (ctlCoords0.size - 1)
    val ctlCoords1  = ctlCoords0.zipWithIndex.map { case (c, i) =>
      val w = i * weight1
      new Coord3D(c.x - diffVel.x * w, c.y - diffVel.y * w, c.z - diffVel.z * w)
    }
    val ctlCoords2  = (new Coord3D(0,0,0) +: ctlCoords1).integrate
    val diffPos     = ctlCoords2.last
    val weight2     = 1.0 / (ctlCoords2.size - 1)
    val ctlCoords   = ctlCoords2.zipWithIndex.map { case (c, i) =>
      val w = i * weight2
      new Coord3D(c.x - diffPos.x * w, c.y - diffPos.y * w, c.z - diffPos.z * w)
    }

    println(s"x mean-var: ${accel.map(_.x).meanVariance}")
    println(s"y mean-var: ${accel.map(_.y).meanVariance}")
    println(s"z mean-var: ${accel.map(_.z).meanVariance}")
    println(accel     .take(20))
    println(ctlCoords .take(20))
    println(ctlCoords .takeRight(20))

//    val intp        = new BernsteinInterpolator
//    val intpCoords  = intp.interpolate(ctlCoords.asJava, 30)
//    val ctlPts      = ctlCoords         .map(new Point(_, Color.RED , 5.0f))
    val ctlPts      = ctlCoords         .map(c => new Point(new Coord3d(c.x.toFloat, c.y.toFloat, c.z.toFloat), Color.BLUE, 1.0f))
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
