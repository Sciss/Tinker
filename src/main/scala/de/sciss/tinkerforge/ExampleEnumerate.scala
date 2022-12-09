/*
 *  ExampleEnumerate.scala
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

import com.tinkerforge.{IPConnection, IPConnectionBase}

// quite straight translation from the original Java source
// published by TinkerForge under CC0 1.0 Universal (public domain)
//
// Enumerates the devices found and then quits.
object ExampleEnumerate {
  def main(args: Array[String]): Unit = run()

  def run(): Unit = {
    val c = new IPConnection
    c.connect(Common.Host, Common.Port)
    c.addEnumerateListener { (uid: String, connectedUid: String, position: Char, hardwareVersion: Array[Short],
                            firmwareVersion: Array[Short], deviceIdentifier: Int, enumerationType: Short) =>
      println(s"UID:               $uid")
      println(s"Enumeration Type:  $enumerationType")

      if (enumerationType != IPConnectionBase.ENUMERATION_TYPE_DISCONNECTED) {
        println(s"Connected UID:     $connectedUid")
        println(s"Position:          $position")
        println(s"Hardware Version:  ${hardwareVersion(0)}.${hardwareVersion(1)}.${hardwareVersion(2)}")
        println(s"Firmware Version:  ${firmwareVersion(0)}.${firmwareVersion(1)}.${firmwareVersion(2)}")
        println(s"Device Identifier: $deviceIdentifier")
        println()
      }
    }
    c.enumerate()

    Thread.sleep(2000L)
    c.disconnect()
    sys.exit()
  }
}