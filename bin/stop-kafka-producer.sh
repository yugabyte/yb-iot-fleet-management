#!/bin/bash

ROOT_DIR=$PWD/..
PROJECT_NAME=iot-kafka-producer
PROJECT_DIR=$ROOT_DIR/$PROJECT_NAME

kill -9 `cat $PROJECT_DIR/run.pid`