#!/bin/bash

DIR="$( cd "$( dirname "$0" )" && pwd )"

BODY=$DIR/../../egrul/egrul.7703564300.json


cat $BODY | curl -v -X PUT -H "Content-Type: application/json" --data @- localhost:8084/nkbrelation/internal/api/egrul/info/

