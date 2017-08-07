#
#   检查函数参数或脚本参数的函数
#
#   函数用途：检测参数是否达到要求，没有达到要求，给出提示退出执行脚本
#
E_PARAMETER=50
E_NO_FILE=51
E_NO_DIR=52


args_num_equal(){
    args_num=$1
    num=$2
    if [[ ! "$args_num" -eq "$num" ]]
    then
        echo "参数个数需要达到 $num 个"
        exiit $E_PARAMETER
    fi
}

args_is_null(){
    args=$1
    if [[ ! x"$args" -eq "x" ]]
    then
        echo "参数个数不为空"
        exiit $E_PARAMETER
    fi
}
