#!/bin/bash
cd "$(dirname "$0")" # go into directory of script
java -jar core/IMU_OSC.jar "$@"
