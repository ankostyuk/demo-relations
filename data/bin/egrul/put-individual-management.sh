#!/bin/bash

DIR="$( cd "$( dirname "$0" )" && pwd )"

BODY=$DIR/../../egrul/egrul-management.zhukova.json


cat $BODY | curl -v -X PUT -H "Content-Type: application/json" --data @- localhost:8084/nkbrelation/internal/api/egrul/individuals/management/20160808

