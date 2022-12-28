#!/bin/bash
cd "$(dirname "$0")" # go into directory of script
cd ..
java -jar core/IMU_OSC.jar "$@"
