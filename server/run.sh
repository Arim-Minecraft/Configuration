#!/bin/bash

#
# Server JAR file
# e.g. paperclip.jar
#
JARFILE="idkspigot.jar"

#
# Server RAM
#
JVM_RAM=1024M

#
# JVM flags
#
JVM_FLAGS="-XX:+UseG1GC -XX:+ParallelRefProcEnabled -XX:MaxGCPauseMillis=200 -XX:+UnlockExperimentalVMOptions -XX:+DisableExplicitGC -XX:+AlwaysPreTouch  -XX:G1NewSizePercent=30 -XX:G1MaxNewSizePercent=40 -XX:G1HeapRegionSize=8M -XX:G1ReservePercent=20 -XX:G1HeapWastePercent=5 -XX:G1MixedGCCountTarget=8 -XX:InitiatingHeapOccupancyPercent=15 -XX:G1MixedGCLiveThresholdPercent=90 -XX:G1RSetUpdatingPauseTimePercent=5 -XX:SurvivorRatio=32 -XX:MaxTenuringThreshold=1 -Dusing.aikars.flags=true -Daikars.new.flags=true"

MC_NAME='test'

DEBUG_ENABLE=true

debug() {
  if [ DEBUG_ENABLE ] ; then
    echo "Debug: $1"
  fi
}

is_running() {
  debug "Checking"
  debug "Drun.sh.name=$MC_NAME"
  if [ ps -aux | grep Drun.sh.name=$MC_NAME | grep -v grep ] ; then
    return 1
  else
    return 0
  fi
}

backup_in_process() {
  if [ -f "backup_in_process.txt" ] ; then
    return 1
  else
    return 0
  fi
}

mc_start() {

  if is_running ; then
    echo "[ERROR] $NAME is already running"
    exit 1
  fi

  # If we ever need PID
  # pidfile=$PATH/$NAME_pid.pid
  #
  # run_as_user "screen -ls | grep $get_screen_name | cut -f1 -d'.' | head -n 1 | tr -d -c 0-9 > $pidfile"
  while true; do
    while [ backup_in_process ]; do
      "Awaiting backup completion..."
      sleep 50
    done
    java -Xms$JVM_RAM -Xmx$JVM_RAM $JVM_FLAGS -Drun.sh.name=$MC_NAME -jar $JARFILE
    echo "20 seconds until restart."
    sleep 20
  done
}

mc_backup() {
  if backup_in_process ; then
    echo "[ERROR] $NAME already has a backup in process"
    exit 1
  fi
  echo "" > backup_in_process.txt
  bash -c "$1"
  rm backup_in_process.txt
}

debug "--- DEBUG START ---"
debug "NAME=$NAME"
debug "ME=$ME"
debug "JARFILE=$JARFILE"
if is_running ; then
  debug "Running"
else
  debug "Not running"
fi
debug "startupcmd=java -Xms$JVM_RAM -Xmx$JVM_RAM $JVM_FLAGS -Drun.sh.name=$MC_NAME -jar $JARFILE"
debug "--- DEBUG END ---"

if [ $1 == 'start' ] ; then
  mc_start
elif [ $1 == 'backup' ]; then
  mc_backup $2
else
  echo "Unknown argument"
fi
