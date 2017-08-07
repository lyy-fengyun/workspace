#!/bin/bash
#
# Clenaup
# version 2
# root
LOG_DIR=/var/log

cd $LOG_DIR
cat /dev/null > messages
cat /dev/null > wtmp
echo "Log cleand up"

exit 0
