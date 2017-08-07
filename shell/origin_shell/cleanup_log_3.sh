#!/bin/bash
#
# Clenaup
# version 3
# root
LOG_DIR=/var/log
ROOT_UID=0
LINES=50
E_XCD=66
E_NOTROOT=67

# check userId
if [[ "$UID" -ne "$ROOT_UID" ]]
then
    echo "Must be root user"
    exit $E_NOTROOT
fi

if [[ -n "$1" ]]
# 测试是否有命令行参数(非空).
then
    lines=$1
else
    Line=$LINES
fi


# 测试是否有命令行参数(非空).
E_WRONGARGS=65

case "$1" in
"") lines=50;;
*[0-9]*)
    echo "Usage:  `basename $0` file-to-cleanup";
    exit $E_WRONGARGS;;
*)
    lines=$1;;
esca
#----

cd $LOG_DIR
if [[ $(pwd) != "$LOG_DIR" ]]
then
    echo "Can't not change to $LOG_DIR"
    exit $E_XCD
fi

cd $LOG_DIR ||{
    echo "Can't not change to $LOG_DIR" >&2
    exit $E_XCD
}

tail -$lines messages >messages.temp
mv messages{.temp,}
#  cat /dev/null > messages
#
cat /dev/null > wtmp
echo "Log cleand up"

exit 0
