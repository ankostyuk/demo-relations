#!/bin/sh

TOMCAT=/srv/tomcat6
WEBAPP=nkbrelation

DIR="$( cd "$( dirname "$0" )" && pwd )"

cat $DIR/$WEBAPP.war > $TOMCAT/webapps/$WEBAPP.war

