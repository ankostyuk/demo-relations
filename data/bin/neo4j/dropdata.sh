#!/bin/sh

DIR="$( cd "$( dirname "$0" )" && pwd )"
GRAPH_DB=$DIR/../../../neo4j/data/

rm -rf $GRAPH_DB
mkdir -p $GRAPH_DB

