# IoT Fleet Management

This is a sample application that demos how IoT applications can leverage YugaByte as the database part of the SMACK stack. YugaByte is an open source cloud-native database for mission-critical applications. It supports the Cassandra Query Language (CQL) in addition to Redis and SQL (coming soon). This example uses CQL to layout the tables and perform queries.


## Scenario

Here is a brief description of the scenario.

Assume that a fleet management company wants to track their fleet of vehicles, which are of different types (18 Wheelers, busses, large trucks, etc).

Below is a view of the dashboard of the running app.

![IoT Fleet Management Dashboard](https://github.com/YugaByte/yb-iot-fleet-management/blob/master/yb-iot-fleet-management-screenshot.png)

The above dashboard can be used to monitor the different vehicle types and the routes they have taken both over the lifetime of the app as well as over the last 30 second window. It also points out the trucks that are near road closures, which might cause a delay in the shipping schedule.


## Architecture

![IoT Fleet Management Architecture](https://github.com/YugaByte/yb-iot-fleet-management/blob/master/yb-iot-fleet-mgmt-arch.png)

The above is a high level architecture diagram of the IoT Fleet Management application. It contains the following three components:

- IoT Kafka Producer
This component emulates data being emitted from a connected vehicle, and generates data for the Kafka topic `iot-data-event`. The data emitted is of the format shown below.
```
{"vehicleId":"0bf45cac-d1b8-4364-a906-980e1c2bdbcb","vehicleType":"Taxi","routeId":"Route-37","longitude":"-95.255615","latitude":"33.49808","timestamp":"2017-10-16 12:31:03","speed":49.0,"fuelLevel":38.0}
```

- IoT Spark Processor
This component reads data from Kafka topic `iot-data-event` and computes the following:
-- Total traffic snapshot
-- Last 30 seconds traffic snapshot
-- Vehicles in a point of interest

- IoT Spring Boot Dashboard
The app uses the Java Spring Boot framework with its integration for Cassandra as the data layer, using the Cassandra Query Language (CQL) internally.


## Prerequisites

For building these projects it requires following tools. Please refer README.md files of individual projects for more details.
- JDK - 1.8 +
- Maven - 3.3 +
- Apache Kafka (we assume this is installed in the `/opt/kafka/` directory.

1. Build the required binaries.
```sh
mvn package
```

2. Start Zookeeper.
```sh
/opt/kafka/bin/zookeeper-server-start.sh /opt/kafka/config/zookeeper.properties
```

3. Start Kafka and create the Kafka topic if this is a new installation.
```sh
/opt/kafka/bin/kafka-server-start.sh /opt/kafka/config/server.properties
```

If this is a new installation, create the Kafka topic. Note that this needs to be done only once.
```sh
/opt/kafka/bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic iot-data-event
```

4. Install YugaByte and create the needed keyspaces.
- Download YugaByte [here](https://www.yugabyte.com/#download).
- Start the local cluster using the quick-start instructions [here](https://docs.yugabyte.com/community-edition/quick-start/).
- Create the necessary keyspaces. You can find `cqlsh` in the `bin` sub-directory located inside the YugaByte installation folder. See the details [here](https://docs.yugabyte.com/community-edition/quick-start/#test-cql-service).
```sh
cqlsh -f iot-springboot-dashboard/IoTData.cql
```

## Running the application

1. Start the data producer.
```sh
java -jar iot-kafka-producer/target/iot-kafka-producer-1.0.0.jar
```

It should start emitting data points to the Kafka topic. You should see something like the following as the output:
```
  2017-10-16 12:31:52 INFO  IoTDataEncoder:28 - {"vehicleId":"0bf45cac-d1b8-4364-a906-980e1c2bdbcb","vehicleType":"Taxi","routeId":"Route-37","longitude":"-95.255615","latitude":"33.49808","timestamp":"2017-10-16 12:31:03","speed":49.0,"fuelLevel":38.0}

  2017-10-16 12:31:53 INFO  IoTDataEncoder:28 - {"vehicleId":"600863bc-c918-4c8e-a90b-7d66db4958e0","vehicleType":"18 Wheeler","routeId":"Route-43","longitude":"-97.918175","latitude":"35.78791","timestamp":"2017-10-16 12:31:03","speed":59.0,"fuelLevel":12.0}
```

2. Start the spark application
```sh
java -jar iot-spark-processor/target/iot-spark-processor-1.0.0.jar
```

3. Start the UI application.
```sh
java -jar iot-springboot-dashboard/target/iot-springboot-dashboard-1.0.0.jar
```

4. Now open the UI in a web browser. The application will refresh itself periodically.
```
http://localhost:8080
```
