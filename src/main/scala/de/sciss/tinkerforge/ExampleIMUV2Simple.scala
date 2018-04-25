package de.sciss.tinkerforge

import com.tinkerforge.{BrickIMUV2, IPConnection}

// quite straight translation from the original Java source
// published by TinkerForge under CC0 1.0 Universal (public domain)
//
// opens the IMU v2 brick, prints current quaternion, then quits
object ExampleIMUV2Simple {
  val DefaultUID = "6jDAtS"   // the one I've got...

  case class Config(uid: String = DefaultUID)

  def main(args: Array[String]): Unit = {
    val default = Config()

    val p = new scopt.OptionParser[Config]("ExampleIMUV2Simple") {
      opt[String]('u', "uid")
        .text (s"UID of the IMU brick you want to use (default: ${default.uid})")
        .action { (v, c) => c.copy(uid = v) }
    }
    p.parse(args, default).fold(sys.exit(1))(run)
  }

  def run(config: Config): Unit = {
    val c = new IPConnection
    // Create IP connection
    val imu = new BrickIMUV2(config.uid, c) // Create device object
    c.connect(Common.Host, Common.Port)   // Connect to brickd

    // Don't use device before `c` is connected
    // Get current quaternion
    val q = imu.getQuaternion // Can throw com.tinkerforge.TimeoutException
    val scale = 1.0/16383.0
    println(s"Quaternion [W]: ${q.w * scale}")
    println(s"Quaternion [X]: ${q.x * scale}")
    println(s"Quaternion [Y]: ${q.y * scale}")
    println(s"Quaternion [Z]: ${q.z * scale}")
    c.disconnect()
  }
}