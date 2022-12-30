#!/bin/bash
cd "$(dirname "$0")" # go into directory of script
cd ..
java -jar core/IMU_OSC.jar -u 2146 Zpw "$@"
