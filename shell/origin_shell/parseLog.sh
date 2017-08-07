#!/bin/bash

file=$1

awk 'BEGIN{FS="#"}{print $4 "#" $12}' ${file} > tmp.file
cat tmp.file