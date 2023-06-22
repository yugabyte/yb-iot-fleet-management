# Sample scripts for the IoT Fleet Management Demo

This directory contains sample scripts for building, starting, and stopping the IoT Fleet Management Demo. Modify these for your environment.

The following scripts are provided:
* `set-cassandra-variables.sh` - Sets the environment variables needed to connect to Cassandra / YugabyteDB
* `start-kafka-producer.sh` - Builds and starts the `iot-kafka-producer` application.
* `stop-kafka-producer.sh` - Stops the `iot-kafka-producer` application
* `start-spark-processor.sh` - Builds and starts the `iot-spark-processor` application.
* `stop-spark-processor.sh` - Stops the `iot-spark-processor` application
* `start-dashboard.sh` - Builds and starts the `iot-springboot-dashboard` application.
* `stop-dashboard.sh` - Stops the `iot-springboot-dashboard` application

To start the applications, first edit `set-cassandra-variables.sh` and set the variables to your YugabyteDB configuration. Then start each of the applications:

```bash
./start-kafka-producer.sh
./start-spark-producer.sh
./start-dashboard.sh
```
This will launch all of the applications in the background.

To stop the applications, run the stop scripts:

```bash
./stop-dashboard.sh
./stop-spark-producer.sh
./stop-kafka-producer.sh
```