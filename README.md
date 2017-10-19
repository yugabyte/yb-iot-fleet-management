# IoT Fleet Management

This is a sample application that demos how Iot applications can leverage YugaByte as the database part of the SMACK stack. YugaByte is an open source cloud-native database for mission-critical applications. It supports the Cassandra Query Language (CQL) in addition to Redis and SQL (coming soon). This example uses CQL to layout the tables and perform queries.

![IoT Fleet Management Architecture](https://github.com/YugaByte/yb-iot-fleet-management/blob/master/yb-iot-fleet-mgmt-architecture.png)

The fleet management application uses following:

- JDK - 1.8
- Maven - 3.3.9
- ZooKeeper - 3.4.8
- Kafka - 2.10-0.10.0.0
- Cassandra - 2.2.6
- Spark - 1.6.2 Pre-built for Hadoop 2.6
- Spring Boot - 1.3.5
- jQuery.js
- Bootstrap.js
- Sockjs.js
- Stomp.js
- Chart.js

IoT Fleet Management includes following three projects:

- IoT Kafka Producer
- IoT Spark Processor
- IoT Spring Boot Dashboard

For building these projects it requires following tools. Please refer README.md files of individual projects for more details.

- JDK - 1.8
- Maven - 3.3.9

Use below command to build all projects.

```sh
mvn package
```
