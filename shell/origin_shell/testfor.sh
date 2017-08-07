#!/bin/bash

########################
#
#	test for arg in *
#
########################

for file in *
do
    echo $file
done


for file in [fi]*
do
    rm -f $file
    echo "Reomve file $file"
done
