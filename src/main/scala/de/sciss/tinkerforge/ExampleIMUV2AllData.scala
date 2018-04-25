package de.sciss.tinkerforge

import com.tinkerforge.{BrickIMUV2, IPConnection}

object ExampleIMUV2AllData {
  case class Config(uid: String = Common.DefaultIMU_UID)

  def main(args: Array[String]): Unit = {
    val default = Config()

    val p = new scopt.OptionParser[Config]("ExampleIMUV2AllData") {
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
    c.connect(Common.Host, Common.Port)     // Connect to brickd

    val scaleAccel    = 1.0/  100.0
    val scaleMagnetic = 1.0/   16.0
    val scaleAng      = 1.0/   16.0
    val scaleQuat     = 1.0/16383.0
    val scaleGrav     = 1.0/  100.0

    imu.addAllDataListener { (accel: Array[Short], magneticField: Array[Short], angVel: Array[Short],
                              eulerAng: Array[Short], quat: Array[Short], linAccel: Array[Short],
                              gravityVec: Array[Short], temp: Byte, calibStatus: Short) =>
      println(f"Acceleration        (X): ${accel(0)*scaleAccel}%1.2f m/s²")
      println(f"Acceleration        (Y): ${accel(1)*scaleAccel}%1.2f m/s²")
      println(f"Acceleration        (Z): ${accel(2)*scaleAccel}%1.2f m/s²")
      println(s"Magnetic Field      (X): ${magneticField(0)*scaleMagnetic} µT")
      println(s"Magnetic Field      (Y): ${magneticField(1)*scaleMagnetic} µT")
      println(s"Magnetic Field      (Z): ${magneticField(2)*scaleMagnetic} µT")
      println(s"Angular Velocity    (X): ${angVel(0)*scaleAng} °/s")
      println(s"Angular Velocity    (Y): ${angVel(1)*scaleAng} °/s")
      println(s"Angular Velocity    (Z): ${angVel(2)*scaleAng} °/s")
      println(s"Euler Angle         (X): ${eulerAng(0)*scaleAng} °")
      println(s"Euler Angle         (Y): ${eulerAng(1)*scaleAng} °")
      println(s"Euler Angle         (Z): ${eulerAng(2)*scaleAng} °")
      println(f"Quaternion          (W): ${quat(0)*scaleQuat}%1.3f")
      println(f"Quaternion          (X): ${quat(1)*scaleQuat}%1.3f")
      println(f"Quaternion          (Y): ${quat(2)*scaleQuat}%1.3f")
      println(f"Quaternion          (Z): ${quat(3)*scaleQuat}%1.3f")
      println(s"Linear Acceleration (X): ${linAccel(0)*scaleAccel} m/s²")
      println(s"Linear Acceleration (Y): ${linAccel(1)*scaleAccel} m/s²")
      println(s"Linear Acceleration (Z): ${linAccel(2)*scaleAccel} m/s²")
      println(f"Gravity Vector      (X): ${gravityVec(0)*scaleGrav}%1.2f m/s²")
      println(f"Gravity Vector      (Y): ${gravityVec(1)*scaleGrav}%1.2f m/s²")
      println(f"Gravity Vector      (Z): ${gravityVec(2)*scaleGrav}%1.2f m/s²")
      println(s"Temperature            : $temp °C")
      println(s"Calibration Status     : ${calibStatus.toBinaryString}")
      println()
    }
    
    // Set period for all data callback to 0.1s (100ms)
    imu.setAllDataPeriod(100)

    println("Press key to exit") 
    Console.in.read()
  }
}
