#!/bin/sh

DIR="$( cd "$( dirname "$0" )" && pwd )"

SOLR_CORES="
bsn
individual
address
phone
purchase
"

for core in $SOLR_CORES
do

echo "===== Удаляем данные $core ====="
curl "http://localhost:8983/solr/$core/update/?commit=true&optimize=true" -H "Content-Type: text/xml" --data-binary "<delete><query>*:*</query></delete>"

done

