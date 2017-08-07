#!/bin/bash

E_WEONG_ARGS=65

script_parameter="-a -h -m -z"

if [[ $# -ne $Number_of_except_args ]]
then
    echo "Usage: `basename $0` $script_parameter"
    exit $E_WEONG_ARGS
fi
