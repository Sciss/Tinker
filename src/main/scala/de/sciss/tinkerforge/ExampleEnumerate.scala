package de.sciss.tinkerforge

import com.tinkerforge.{IPConnection, IPConnectionBase}

// quite straight translation from the original Java source
// published by TinkerForge under CC0 1.0 Universal (public domain)
//
// Enumerates the devices found and then quits.
object ExampleEnumerate {
  private val Host = "localhost"
  private val Port = 4223

  def main(args: Array[String]): Unit = run()

  def run(): Unit = {
    val c = new IPConnection
    c.connect(Host, Port)
    c.addEnumerateListener(new IPConnection.EnumerateListener {
      override def enumerate(uid: String, connectedUid: String, position: Char, hardwareVersion: Array[Short],
                             firmwareVersion: Array[Short], deviceIdentifier: Int, enumerationType: Short): Unit = {
        println(s"UID:               $uid")
        println(s"Enumeration Type:  $enumerationType")
        if (enumerationType == IPConnectionBase.ENUMERATION_TYPE_DISCONNECTED) {
          println()
          return
        }
        println(s"Connected UID:     $connectedUid")
        println(s"Position:          $position")
        println(s"Hardware Version:  ${hardwareVersion(0)}.${hardwareVersion(1)}.${hardwareVersion(2)}")
        println(s"Firmware Version:  ${firmwareVersion(0)}.${firmwareVersion(1)}.${firmwareVersion(2)}")
        println(s"Device Identifier: $deviceIdentifier")
        println()
      }
    })
    c.enumerate()
    c.disconnect()
  }
}