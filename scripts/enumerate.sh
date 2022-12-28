#!/bin/bash
cd "$(dirname "$0")" # go into directory of script
cd ..
sbt 'core/runMain de.sciss.tinker.Enumerate'
