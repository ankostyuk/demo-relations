#!/bin/bash

DIR="$( cd "$( dirname "$0" )" && pwd )"

BODY=$DIR/../../egrul/egrul-founder.zhukova.json

cat $BODY | curl -v -X PUT -H "Content-Type: application/json" --data @- localhost:8084/nkbrelation/internal/api/egrul/individuals/founder/20160808

