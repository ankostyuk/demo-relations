#!/bin/sh

DIR="$( cd "$( dirname "$0" )" && pwd )"

DB=nkbrelation
DBHOST=localhost
WEBAPP=localhost:8080
RENDER=localhost:3000

export OUTPUT=$DIR/data
export RENDERURL="http://$RENDER/render.png?check=_REPORT_READY_&selector=[app-graph-layout]&zoom=2&url=http://$WEBAPP/nkbrelation/internal/report/"

mkdir $OUTPUT

mongo --quiet --eval "db.report.find({}).forEach(function(doc) {print(doc._id + ' ' + doc.userId);})" --host $DBHOST nkbrelation | \
xargs -L 1 sh -c 'wget -v --retry-connrefused "$RENDERURL$0" -O $OUTPUT/$1_$0.png'

