#!/bin/bash

ROOT_DIR=$PWD/..
PROJECT_NAME=iot-springboot-dashboard
PROJECT_DIR=$ROOT_DIR/$PROJECT_NAME

mvn clean package -f $PROJECT_DIR

source set-cassandra-variables.sh

nohup mvn spring-boot:run -f  $PROJECT_DIR > $PROJECT_DIR/$PROJECT_NAME.log & echo $! > $PROJECT_DIR/run.pid

