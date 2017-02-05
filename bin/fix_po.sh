#!/bin/bash

#
# Скрипт разбивает многострочные reference комментарии в .po файлах
# по одному на строку и заменяет номер строки на 1
#

DIR="$( cd "$( dirname "$0" )/.." && pwd )"

# .po файлы с сортировкой и удаление дубликатов
for pofile in `find $DIR/webapp/i18* -name "*.po"`
do
    msgcat -s --no-wrap "$pofile" |\
    sed 's/\(#:.*\):[0-9]\+/\1:1/' |\
    msgcat -s --no-wrap - > "$pofile.new" &&
    mv -f "$pofile.new" "$pofile"
done

# .pot файлы без сортировки и с дубликатами, т.к. keyextract формирует дубликаты
for pofile in `find $DIR/webapp/i18* -name "*.pot"`
do
    msgcat --no-wrap "$pofile" |\
    sed 's/\(#:.*\):[0-9]\+/\1:1/' > "$pofile.new" &&
    mv -f "$pofile.new" "$pofile"
done
