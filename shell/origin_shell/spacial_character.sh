#!/bin/bash

# spacial character
echo "this is a comment"

#
echo "The # heree does not begun a comment"

echo 'The # heree does not begun a comment'

echo The \# heree does not begun a comment

echo The # this is a comment

echo ${PATH#*:}  # 参数替换
echo $(( 2#101011 ))  #数制转换


touch_file(){
    filename="$1"
    if [[ -e "$filename" ]];then
        echo "File $filename exists."
        cp $filename $filename.bak
    else
        echo "File $filename not found. touch $filename"
        touch $filename
    fi
}

touch_file test.file

:${username=$(whoami)}

:${HOSTNAME?} ${USER} ${MAIL}

# 清空文件
: > test.file

set -x
echo *
set +x
