#!/bin/bash
unset PS1
if [[ -n $PS1 ]]
then
    echo  ${PS1?}
    echo $PS1
    echo "interactive"
else
    echo "auto"
fi