#!/bin/bash

DIR="$( cd "$( dirname "$0" )" && pwd )"

SAMPLES=$DIR/../../sample

for sample in `ls $SAMPLES --ignore=demo`
do

BSN=$SAMPLES/$sample/bsn.xml

xsltproc -o $BSN $DIR/out2add.xsl $SAMPLES/$sample/in.xml
$DIR/bsnextract.py $BSN

done
