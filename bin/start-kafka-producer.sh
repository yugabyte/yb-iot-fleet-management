#!/bin/bash

ROOT_DIR=$PWD/..
PROJECT_NAME=iot-kafka-producer
PROJECT_DIR=$ROOT_DIR/$PROJECT_NAME

mvn clean package -f $PROJECT_DIR

nohup mvn spring-boot:run -f  $PROJECT_DIR > $PROJECT_DIR/$PROJECT_NAME.log & echo $! > $PROJECT_DIR/run.pid

