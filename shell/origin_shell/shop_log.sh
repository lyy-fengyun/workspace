#!/bin/bash

############################
#
# 商城应急系统上线
# version:1.0
#
#############################

crm_IPS=(
172.16.59.161
172.16.59.162
172.16.59.164
172.16.59.165
172.16.59.182
172.16.59.183
)
for ip in "${crm_IPS[@]}"
do
echo $ip

done


# 备份文件
# 日期变量
today=$(date +%Y%m%d)
# tomcat6 bin目录
crm_tomcat_bin='/opt/mcb/emergency/tomcat6/bin'
# 备份根目录
crm_backup_today='/opt/mcb/backup'
# 文件上传目录
crm_upload_today='/opt/mcb/upload'


crm_backup(){
for ip in $crm_IPS
do
ssh $ip <<EEE
cd $crm_backup_today
if ! [[ -e $today ]]
then
 mkdir %today
fi

cd $crm_upload_today
if ! [[ -e $today ]]
then
 mkdir %today
fi





alias cp='cp -vi'
cd /opt/mcb/emergency/tomcat6/bin
pwd
sh shutdown.sh
echo "关闭tomcat应用"
echo ""
echo "全量备份开始"
cd  /opt/mcb/emergency/tomcat6/webapps/
cp -r CMUPayCrmFront ../CMUPayCrmFront_bak.20170221
echo "全量备份结束"
echo 

echo "增量备件文件"
cd /opt/mcb/emergency/tomcat6/webapps/CMUPayCrmFront/WEB-INF/classes/com/huateng/core/remoting/impl
cp CrmRemotingImpl.class CrmRemotingImpl.class.20170221
cp CrmRemotingImpl\$1.class CrmRemotingImpl\$1.class.20170221

cd /opt/mcb/emergency/tomcat6/webapps/CMUPayCrmFront/WEB-INF/classes
cp logback.xml logback.xml.20170221

cd /opt/mcb/upayconf
cp jms_emergency.properties jms_emergency.properties.20170221
echo "增量备件文件结束"
EEE

scp CrmRemotingImpl.class $ip:/opt/mcb/emergency/tomcat6/webapps/CMUPayCrmFront/WEB-INF/classes/com/huateng/core/remoting/impl
scp CrmRemotingImpl\$1.class $ip:/opt/mcb/emergency/tomcat6/webapps/CMUPayCrmFront/WEB-INF/classes/com/huateng/core/remoting/impl

done
}


notify_backup(){
ssh 172.16.59.4 <<EEE
alias cp='cp -vi'
echo "关闭应用"
cd   /opt/upay/upay_shop_yingji/tomcat6/bin
sh shutdown.sh 

echo "全量备份开始"
cd  /opt/upay/upay_shop_yingji/tomcat6/webapps/
cp -r CMUPayCoreNotify ../CMUPayCoreNotify_bak.20170221
echo "全量备份结束"
echo ""


echo "增量备份开始"
cd /opt/upay/upay_shop_yingji/tomcat6/webapps/CMUPayCoreNotify/WEB-INF/classes/
cp server.properties server.properties.20170221
cp applicationContext.xml applicationContext.xml.20170221
cp applicationContext-jms.xml applicationContext-jms.xml.20170221 
cp applicationContext-jms-send.xml applicationContext-jms-send.xml.20170221

cd /opt/upay/upay_shop_yingji/tomcat6/webapps/CMUPayCoreNotify/WEB-INF/classes/com/cmsz/cmupay/service/task
cp NotifyJob.class NotifyJob.class.20170221
cp NotifyJob\$1.class NotifyJob\$1.class.20170221
echo "增量备份结束"

echo ""
EEE

scp NotifyJob.class 172.16.59.4:/opt/upay/upay_shop_yingji/tomcat6/webapps/CMUPayCoreNotify/WEB-INF/classes/com/cmsz/cmupay/service/task
scp NotifyJob\$1.class 172.16.59.4:/opt/upay/upay_shop_yingji/tomcat6/webapps/CMUPayCoreNotify/WEB-INF/classes/com/cmsz/cmupay/service/task
}

startapp(){

for ip in $crm_IPS
do 
ssh $ip <<EEE
echo "启动 $ip crm应用"
cd /opt/mcb/emergency/tomcat6/bin
sh startup.sh
ps –ef|grep tomcat6 
cd /opt/mcb/emergency/tomcat6/logs
tail -f catalina.out
EEE
done

ssh 172.16.59.4 <<EEE
echo "启动notify应用"
cd /opt/upay/upay_shop_yingji/tomcat6/bin
sh startup.sh 
ps –ef|grep tomcat6 
cd /opt/upay/upay_shop_yingji/tomcat6/logs
tail -f catalina.out
EEE
}

crm_backup
notify_backup
startapp