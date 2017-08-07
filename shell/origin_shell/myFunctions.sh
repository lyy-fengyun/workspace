#!/bin/bash

####################################
#
# fielName: myFunctions.sh
# 存放了常用的shell函数
#
#
####################################

function green_echo(){
	echo -e '\E[32m' $@ '\E[0m' 
}

function blue_echo(){
	echo -e '\E[34m' $@ '\E[0m' 
}

function red_echo(){
	echo -e '\E[31m' $@ '\E[0m' 
}

# 检查端口
ck_port()
{
	netstat -an|grep -E $1 | grep LISTEN
}

# 检查进程
ck_process()
{
	ps -ef | grep $2 | grep -v grep >>/dev/null && echo $2 exists || echo $2 not exists
}

# 检查cpu和mem的使用率
ck_cpumem()
{
	PID=$(ps -ef|grep -v grep | grep $2|awk '{print \$2}')
	
    ps aux|head -1 | awk '{for(i=3;i<5;i++)printf("%s ",$i)}END{print "\n"}';
	ps aux|grep $PID | grep -v grep |awk '{for(i=3;i<5;i++)printf("%s ",$i)}END{print "\n"}'
}

# 远程检查端口
function ssh_ckport
{
	blue_echo "check port $@"
	ssh $1 "netstat -an|grep -E $2 | grep LISTEN"
}

# 远程检查进程
function ssh_ckprogress
{
	blue_echo "check $1 progress"
	ssh $1 "ps -ef | grep $2 | grep -v grep >>/dev/null && echo $2 exists || echo $2 not exists"
}

# 远程检查cpu和mem的使用率
ssh_ckcpumem(){
	blue_echo $2
	cmd="ps aux|head -1 | awk '{for(i=3;i<5;i++)printf(\"\%s \",\$i)}END{print \"\\n\"}';\
	ps aux|grep \$(ps -ef|grep -v grep | grep $2|awk '{print \$2}') | grep -v grep \
	|awk '{for(i=3;i<5;i++)printf(\"\%s \",\$i)}END{print \"\\n\"}'"
	#blue_echo $cmd
	ssh $1 $cmd
}


MESS_LV_ERR="SERIOUS"
MESS_LV_WAR="WARNING"
MESS_LV_INFO="INFO"
HOSTNAME=`hostname`
APP="UPAY"
APPNAME="upay-db-manage"
MODNAME="hisdata_sybase"
DATE=`date +%Y%m%d`
LOG_BASE="/opt/upay/upay2logs"
mkdir -p ${LOG_BASE}/${APPNAME}/alarm
mkdir -p ${LOG_BASE}/${APPNAME}/${DATE}
LOGFILE_ALARM="${LOG_BASE}/${APPNAME}/alarm/${MODNAME}.log.${DATE}"
LOGFILE_INFO="${LOG_BASE}/${APPNAME}/${DATE}/${MODNAME}_info.log"
CFGFILE=${WORKDIR}/hisdata_sybase.conf
DEF_DAYS=-30


function writelog()
{
  TIME=`date +%Y%m%d%H%M%S`
  MESSAGE=$2
  case "$1" in
    e)echo "BL##${MESS_LV_ERR}#${TIME}#${HOSTNAME}#${APP}#${APPNAME}#${MODNAME}###${FPATH}[`caller|awk '{print $1}'`]####${MESSAGE}##LB"|tee -a ${LOGFILE_INFO} ${LOGFILE_ALARM};;
    m)echo "BL##${MESS_LV_WAR}#${TIME}#${HOSTNAME}#${APP}#${APPNAME}#${MODNAME}###${FPATH}[`caller|awk '{print $1}'`]####${MESSAGE}##LB"|tee -a ${LOGFILE_INFO};;
    i)echo "BL##${MESS_LV_INFO}#${TIME}#${HOSTNAME}#${APP}#${APPNAME}#${MODNAME}###${FPATH}[`caller|awk '{print $1}'`]####${MESSAGE}##LB"|tee -a ${LOGFILE_INFO};;
  esac
}