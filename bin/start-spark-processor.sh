#!/bin/bash

ROOT_DIR=$PWD/..
PROJECT_NAME=iot-spark-processor
PROJECT_DIR=$ROOT_DIR/$PROJECT_NAME
APP_VERSION=1.0.0

mvn clean package -f $PROJECT_DIR

source set-cassandra-variables.sh

nohup java -jar $PROJECT_DIR/target/$PROJECT_NAME-$APP_VERSION.jar > $PROJECT_DIR/$PROJECT_NAME.log & echo $! > $PROJECT_DIR/run.pid

