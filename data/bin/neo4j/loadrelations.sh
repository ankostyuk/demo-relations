#!/bin/bash

DIR="$( cd "$( dirname "$0" )" && pwd )"
GRAPH_DB=$DIR/../../../neo4j/data/

LIB=$DIR/../../../lib

cd $LIB
./fetch.sh

CP=$LIB/target/dependency/creditnet-relations-data*.jar

rm -rf $GRAPH_DB
mkdir -p $GRAPH_DB

SAMPLES=$DIR/../../sample

for sample in `ls $SAMPLES --ignore=demo`
do

echo "===== Загружаем набор $sample ====="

java -cp $CP ru.nullpointer.nkbrelation.data.ImportBsnProperties $SAMPLES/$sample/properties.json $GRAPH_DB
java -cp $CP ru.nullpointer.nkbrelation.data.ImportBsnRelations $SAMPLES/$sample/relations.json $GRAPH_DB

if [ -f $SAMPLES/$sample/custom-relations.json ]
then
    java -cp $CP ru.nullpointer.nkbrelation.data.ImportRelations $SAMPLES/$sample/custom-relations.json $GRAPH_DB
fi

echo ""

done

echo "===== Загружаем демо набор ====="

java -cp $CP ru.nullpointer.nkbrelation.data.ImportBsnProperties $SAMPLES/demo/properties.json $GRAPH_DB
java -cp $CP ru.nullpointer.nkbrelation.data.ImportBsnRelations $SAMPLES/demo/relations.json $GRAPH_DB

java -cp $CP ru.nullpointer.nkbrelation.data.ImportEgrulProperties $SAMPLES/demo/egrul_properties.json $GRAPH_DB
java -cp $CP ru.nullpointer.nkbrelation.data.ImportEgrulRelations $SAMPLES/demo/egrul_relations.json $GRAPH_DB

echo ""
echo "===== Загружаем закупки ====="
# Закупки
java -cp $CP ru.nullpointer.nkbrelation.data.ImportBsnProperties $SAMPLES/demo/purchase/bsn-properties.json $GRAPH_DB

java -cp $CP ru.nullpointer.nkbrelation.data.ImportProperties $SAMPLES/demo/purchase/purchase-properties.json $GRAPH_DB
java -cp $CP ru.nullpointer.nkbrelation.data.ImportRelations $SAMPLES/demo/purchase/purchase-relations.json $GRAPH_DB

