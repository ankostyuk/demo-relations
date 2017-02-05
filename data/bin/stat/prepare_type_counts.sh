#!/bin/bash

jq -r '._type + " " + ._srcId + " " + ._dstId' $@ | awk '{print $2, "s"$1, $3; print $3, "d"$1, $2}' | sort -k2 | uniq -c -f1 | awk '{print $1, $3}'
