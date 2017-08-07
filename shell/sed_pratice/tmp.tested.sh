#!/bin/bash
sedscr=$1
for x
do 
    sed -f $sedscr $x > tmp.$x
done 
