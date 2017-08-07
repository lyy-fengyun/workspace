#!/bin/bash

file_core_ips=(
192.168.86.1 
)
file_core_ip=192.168.86.2  

clearing_core_ips=(
192.168.86.3
192.168.86.4
192.168.86.5     
)

dispatcher_ips=(
192.168.86.13
)
dispatcher_ip=192.168.86.14

guankong_ips=(
192.168.86.11
)

guankong_ip=192.168.86.12

replace_jdbc_url(){
ip=$1
app=$2
ssh $ip <<EEE
cd /opt/upay
[[ -e $app ]] && cd $app
[[ -e apps ]] && cd apps
[[ -e $app ]] && cd $app
file=config.properties
echo "\$(pwd) in $ip"
find . -name \$file | xargs grep -E '86.39' 
find . -name \$file | xargs perl -pi -e 's/86.39/86.40/g' 
find . -name \$file | xargs grep -E '86.39' 
find . -name \$file | xargs grep -E '86.40' 
EEE
}

file_core_app='filecoreTomcat'
clearing_app='mgzxjfClearingTomcat'
dispatcher_app='dispatcherTomcat'
display='displayPlatformTomcat'

kill_ps(){
ip=$1
app=$2
ssh $ip <<EEE
cd /opt/upay
[[ -e $app ]] && cd $app
[[ -e apps ]] && cd apps
[[ -e $app ]] && cd $app
echo "\$(pwd) in $ip"
echo ""
ps -ef | grep -v grep| grep  $app 
ps -ef | grep -v grep| grep  $app |wc -l
ps -ef | grep -v grep| grep  $app |awk '{print \$2}'|xargs kill -9 
EEE
}

start_app(){
ip=$1
app=$2
ssh $ip <<EEE
cd /opt/upay
[[ -e $app ]] && cd $app
[[ -e apps ]] && cd apps
[[ -e $app ]] && cd $app
cd bin 
echo "\$(pwd) in $ip"
bash startup.sh
sleep  5 
ps -ef | grep -v grep| grep  $app
EEE
do_ssh(){
    for ip in $(eval "echo $2")
    do
        #replace_jdbc_url $ip $2
        #echo_ip  $ip $2
        eval "$1 $ip $3"
    done
}


main(){
do_ssh kill_ps '${file_core_ips[@]}' $file_core_app
do_ssh kill_ps '${clearing_core_ips[@]}' $clearing_app
do_ssh kill_ps '${dispatcher_ips[@]}' $dispatcher_app
do_ssh kill_ps '${guankong_ips[@]}' $display

do_ssh replace_jdbc_url '${file_core_ips[@]}' $file_core_app
do_ssh replace_jdbc_url '${clearing_core_ips[@]}' $clearing_app
do_ssh replace_jdbc_url '${dispatcher_ips[@]}' $dispatcher_app
do_ssh replace_jdbc_url '${guankong_ips[@]}' $display

do_ssh start_app '${file_core_ips[@]}' $file_core_app
do_ssh start_app '${clearing_core_ips[@]}' $clearing_app
do_ssh start_app '${dispatcher_ips[@]}' $dispatcher_app
do_ssh start_app '${guankong_ips[@]}' $display

}


# main(){
# do_ssh replace_jdbc_url '${file_core_ip}' $file_core_app
# do_ssh replace_jdbc_url '${guankong_ip}' $display
# do_ssh replace_jdbc_url '${dispatcher_ip}' $dispatcher_app

# }


test(){

}

main