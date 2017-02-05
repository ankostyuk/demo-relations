#!/bin/bash

DIR="$( cd "$( dirname "$0" )" && pwd )"

SOLR_CORES="
bsn
individual
address
phone
"

SAMPLES=$DIR/../../sample

for sample in `ls $SAMPLES --ignore=demo`
do
    for core in $SOLR_CORES
    do

    echo "===== Загружаем набор $sample/$core ====="
    curl "http://localhost:8983/solr/$core/update?stream.file=$SAMPLES/$sample/$core.xml&stream.contentType=text/xml;charset=utf-8&commit=true"

    done
done

echo "===== Загружаем демо набор BSN ====="
curl "http://localhost:8983/solr/bsn/update?stream.file=$SAMPLES/demo/bsn.xml&stream.contentType=text/xml;charset=utf-8&commit=true"

echo "===== Загружаем демо CSV набор individual ====="
curl "http://localhost:8983/solr/individual/update/csv?stream.file=$SAMPLES/demo/individual.csv&stream.contentType=text/plain;charset=utf-8&fieldnames=id,name,subtype&commit=true"

echo "===== Загружаем демо CSV набор address ====="
curl "http://localhost:8983/solr/address/update/csv?stream.file=$SAMPLES/demo/address.csv&stream.contentType=text/plain;charset=utf-8&fieldnames=id,value,index&commit=true"

echo "===== Загружаем демо CSV набор phone ====="
curl "http://localhost:8983/solr/phone/update/csv?stream.file=$SAMPLES/demo/phone.csv&stream.contentType=text/plain;charset=utf-8&fieldnames=id,value,phoneType&commit=true"


echo "===== Загружаем закупки ====="
PURCHASES=$SAMPLES/demo/purchase
curl "http://localhost:8983/solr/purchase/update?stream.file=$PURCHASES/purchase.xml&stream.contentType=text/xml;charset=utf-8&commit=true"
curl "http://localhost:8983/solr/bsn/update?stream.file=$PURCHASES/bsn.xml&stream.contentType=text/xml;charset=utf-8&commit=true"
curl "http://localhost:8983/solr/individual/update/csv?stream.file=$PURCHASES/individual.csv&stream.contentType=text/plain;charset=utf-8&fieldnames=id,name,subtype&commit=true"
