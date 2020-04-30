#!/bin/bash -x

#
#
# Simple Startup Script w/ Backups
#
# by A248 with help from alvid98
#
#
#
# Usage
# 1. Enter a unique name for this server instance.
# 2. Configure the RAM and server JAR.
# 3. Create a new screen session (this script is designed to work with screens)
# 4. Fill in the screen name from the screen you just made.
# 5. Run './run.sh start' from inside the screen sesssion.
# 6. When you need a backup, run './run.sh backup "<command>"'. If the server is
# running, this script will safely shutdown the server, conduct the backup
# by running the backup command, and restart the server.
# 7. If the server stops for another reason besides making a backup,
# this script will automatically restart it within 20 seconds.
#

#
# Name of this instance
# Must be unique
#
MC_NAME='test'

#
# Command to stop the server
# Most servers use /stop
#
MC_STOP_CMD='stop'

#
# Screen name
#
#
SCREEN_NAME='Test'

#
# Server RAM
#
JVM_RAM=1024M

#
# Server JAR file
# e.g. paperclip.jar
#
JARFILE="paperclip.jar"

#
#
# JVM flags
#
# The default is Aikar's recommended flags, which are optimised
# based on Minecraft's memory allocation rate to reduce GC pauses
# See https://mcflags.emc.gs
#
JVM_FLAGS="-XX:+UseG1GC -XX:+ParallelRefProcEnabled -XX:MaxGCPauseMillis=200 -XX:+UnlockExperimentalVMOptions -XX:+DisableExplicitGC -XX:+AlwaysPreTouch -XX:G1NewSizePercent=30 -XX:G1MaxNewSizePercent=40 -XX:G1HeapRegionSize=8M -XX:G1ReservePercent=20 -XX:G1HeapWastePercent=5 -XX:G1MixedGCCountTarget=8 -XX:InitiatingHeapOccupancyPercent=15 -XX:G1MixedGCLiveThresholdPercent=90 -XX:G1RSetUpdatingPauseTimePercent=5 -XX:SurvivorRatio=32 -XX:MaxTenuringThreshold=1 -Dusing.aikars.flags=”https://mcflags.emc.gs” -Daikars.new.flags=true"

#
#
#
#
#

DEBUG_ENABLE=true

debug() {
  if [ $DEBUG_ENABLE ] ; then
    echo "Debug: $1"
  fi
}

is_script_running() {
  PID=`cat run.sh-script_pid`
  RESULT=`ps -aux | grep $PID | grep -v grep | cut -f1 -d' ' | head -n 1`
  if [ "$RESULT" != "" ] ; then
    return 0
  else
    return 1
  fi
}

is_server_running() {
  RESULT=`ps -aux | grep Drun.sh.name=$MC_NAME | grep -v grep`
#  if [ echo "$(ps -aux | grep Drun.sh.name=$MC_NAME | grep -v grep)" != "" ] ; then
  if [ "$RESULT" != "" ] ; then
    return 0
  else
    return 1
  fi
}

backup_in_process() {
  RESULT=`find -iname run.sh-backup_in_process`
  if [ "$RESULT" != "" ] ; then
    return 0
  else
    return 1
  fi
}

mc_start() {
  if is_script_running ; then
    echo "[ERROR] $NAME is already running"
    exit 1
  fi
  # PID=`ps -ax | grep Drun.sh.name=test | cut -f1 -d' ' | head -n 1`
  # echo "pid = $PID"
  echo "$$" > run.sh-script_pid
  while true; do
    while backup_in_process ; do
      echo "Awaiting backup completion..."
      sleep 5
    done
    java -Xms$JVM_RAM -Xmx$JVM_RAM $JVM_FLAGS -Drun.sh.name=$MC_NAME -jar $JARFILE
    echo ""
    echo "Restarting in 10 seconds..."
    sleep 10
  done
}

mc_stop() {
  if is_server_running ; then
    screen -S $SCREEN_NAME -p 0 -X stuff 'stop\n'
  else
    echo "[ERROR] $NAME is offline"
  fi
}

mc_backup() {
  if backup_in_process ; then
    echo "[ERROR] $NAME already has a backup in process"
    exit 1
  fi
  echo "" > run.sh-backup_in_process
  if is_server_running ; then
    mc_stop
  fi
  while is_server_running ; do
    debug "Waiting for server to stop"
    sleep 5
  done
  $@
  rm run.sh-backup_in_process
  if [ $? ] ; then
    debug "Success"
  else
    debug "Failure?"
  fi
}

if is_script_running ; then
  debug "Script Running"
else
  debug "Script not running"
fi
if is_server_running ; then
  debug "Server Running"
else
  debug "Server not running"
fi
if backup_in_process ; then
  debug "Backing up"
else
  debug "Not backing up"
fi

if [ $1 == 'start' ] ; then
  mc_start
elif [ $1 == 'stop' ] ; then
  mc_stop
elif [ $1 == 'backup' ]; then
  shift
  mc_backup "$@"
else
  echo "Unknown argument"
fi
