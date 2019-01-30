# Docker image for https://github.com/YugaByte/yb-iot-fleet-management
FROM openjdk:8-jre-alpine
MAINTAINER YugaByte
ENV container=yugabyte-iot

# App install directory
ENV IOT_HOME=/home/yugabyte-iot
WORKDIR $IOT_HOME

# Copy all the application jars.
COPY ./iot-kafka-producer/target/iot-kafka-producer-1.0.0.jar $IOT_HOME/iot-kafka-producer.jar
COPY ./iot-springboot-dashboard/target/iot-springboot-dashboard-1.0.0.jar $IOT_HOME/iot-springboot-dashboard.jar

# Expose necessary ports.
EXPOSE 8080

#
# To build:
#   cd yb-iot-fleet-management
#   docker build . -t yb-iot
#   docker tag <id> yugabytedb/yb-iot:<latest>
#   docker push yugabytedb/yb-iot:latest
#
# To run:
#   docker run -p 8080:8080 --name yb-iot yb-iot
#
# Stop:
#   docker stop yb-iot
#   docker rm yb-iot
#
# Notes:
#   Needs to be able to talk to port 9042(YugaByte DB) and port 9092(Confluent Kafka)
