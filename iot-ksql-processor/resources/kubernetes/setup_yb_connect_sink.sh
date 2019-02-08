#!/bin/bash

set -euo pipefail

print_help() {
  cat <<-EOT
This script runs all the setup steps needed for Kafka to YugaByte DB Connect Sink.
Usage: ${0##*/} <options>
Options:
    --kafka_helm_name <name>
      The name used during the helm install of cp-helm-charts. Required.
EOT
}

kafka_name=""

setup_yb_kafka_sink() {
    cd ~/code/yb-iot-fleet-management/iot-ksql-processor/resources/
    mkdir -p kafka-connect-yugabyte-deps
    cd kafka-connect-yugabyte-deps
    echo "Copying dependent jars."
    wget http://central.maven.org/maven2/io/netty/netty-all/4.1.25.Final/netty-all-4.1.25.Final.jar >& /dev/null
    wget http://central.maven.org/maven2/com/yugabyte/cassandra-driver-core/3.2.0-yb-18/cassandra-driver-core-3.2.0-yb-18.jar  >& /dev/null
    wget http://central.maven.org/maven2/com/codahale/metrics/metrics-core/3.0.1/metrics-core-3.0.1.jar  >& /dev/null
    kubectl cp netty-all-4.1.25.Final.jar $kafka_name-cp-kafka-0:/usr/share/java/kafka -c cp-kafka-broker
    kubectl cp cassandra-driver-core-3.2.0-yb-18.jar $kafka_name-cp-kafka-0:/usr/share/java/kafka -c cp-kafka-broker
    kubectl cp metrics-core-3.0.1.jar $kafka_name-cp-kafka-0:/usr/share/java/kafka -c cp-kafka-broker

    echo "Copying property files."
    cd ~/code/yb-kafka-connector
    mvn clean install -DskipTests >& /dev/null
    kubectl cp target/yb-kafka-connnector-1.0.0.jar $kafka_name-cp-kafka-0:/usr/share/java/kafka -c cp-kafka-broker

    cd ~/code/yb-iot-fleet-management/iot-ksql-processor/resources
    kubectl cp kubernetes/ $kafka_name-cp-kafka-0:/etc/kafka -c cp-kafka-broker

    # cleanup
    cd ~/code/yb-iot-fleet-management/iot-ksql-processor/resources/
    rm -rf kafka-connect-yugabyte-deps
}

while [ $# -gt 0 ]; do
  case "$1" in
    --kafka_helm_name)
      kafka_name="$2"
      shift
    ;;
    -h|--help)
      print_help
      exit
    ;;
    *)
      echo "Invalid command line arg : $1"
      print_help
      exit
  esac
  shift
done

if [ -z "$kafka_name" ]; then
  echo "'kafka_name' needs to be a valid string - name given to the the kafka helm install." >&2
  print_help >& 2
  exit 1
fi

setup_yb_kafka_sink

echo "Setup for Kafka YugaByte DB Connect sink done."
