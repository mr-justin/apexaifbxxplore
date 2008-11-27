#!/bin/sh
split -C 200MB $1 $1.split
for f in $1.split*
do
    sort -n -T . < $f | uniq > $f.sort
    rm -f $f
done
sort -m $1.split*.sort | uniq
rm -f $1.split*.sort
