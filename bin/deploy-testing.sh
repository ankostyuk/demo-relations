#!/bin/sh

#if [ -z "$1" ];then
#  echo "Не указан сервер"
#  exit
#fi
#SERVER=$1

SERVER=deploy@relations.nkb

ssh $SERVER true
if [ $? -ne 0 ];then
  echo "Сервер $SERVER недоступен. Отмена."
  exit
fi

echo "Деплоймент на $SERVER"

DIR="$( cd "$( dirname "$0" )" && pwd )"

cd $DIR/../webapp/

mvn -Ptesting clean verify

if [ $? -ne 0 ];then
  echo "Сборка прошла неудачно. Отмена."
  exit
fi

REMOTE_DIR=nkbrelation

rsync -avz $DIR/redeploy.sh $SERVER:~/$REMOTE_DIR/  &&
rsync -avz -b --backup-dir=backup --suffix=`date +-%Y%m%d-%H%M` target/*.war $SERVER:~/$REMOTE_DIR/nkbrelation.war &&
ssh $SERVER "sh ~/$REMOTE_DIR/redeploy.sh"
