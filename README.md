# IoT Fleet Management

YugaByte DB is world's 1st open source database that is both NoSQL (Cassandra & Redis compatible) and SQL (PostgreSQL compatible) at the same time. It is purpose-built to power fast-growing online services on public, private and hybrid clouds with transactional data integrity, low latency, high throughput and multi-region scalability while also using popular NoSQL and SQL APIs.

This is a sample application that shows how real-time streaming applications (such as those in the IoT vertical) can leverage YugaByte DB as a highly reliable, elastic operational database. It uses YugaByte DB's Cassandra-compatible YCQL API.

## Scenario

Here is a brief description of the scenario.

Assume that a fleet management company wants to track their fleet of vehicles, which are of different types (18 Wheelers, busses, large trucks, etc).

Below is a view of the dashboard of the running app.

![IoT Fleet Management Dashboard](https://github.com/YugaByte/yb-iot-fleet-management/blob/master/yb-iot-fleet-management-screenshot.png)

The above dashboard can be used to monitor the different vehicle types and the routes they have taken both over the lifetime of the app as well as over the last 30 second window. It also points out the trucks that are near road closures, which might cause a delay in the shipping schedule.


## Architecture

The IoT Fleet Management application contains the following four components:

- IoT Kafka Producer
  This component emulates data being emitted from a connected vehicle, and generates data for the Kafka topic `iot-data-event`. The data emitted is of the format shown below.
  ```
  {"vehicleId":"0bf45cac-d1b8-4364-a906-980e1c2bdbcb","vehicleType":"Taxi","routeId":"Route-37","longitude":"-95.255615","latitude":"33.49808","timestamp":"2017-10-16 12:31:03","speed":49.0,"fuelLevel":38.0}
  ```

- IoT Real-Time Data Processor
  This component reads data from Kafka topic `iot-data-event` and computes the following:
  - Total traffic snapshot
  - Last 30 seconds traffic snapshot
  - Vehicles near a point of interest

  There are two ways the app can perform this analysis. First is through KSQL, Confluent's SQL-like streaming query language for Kafka, and second is through Apache Spark as an external stream processing engine.

- IoT Database
  This component is based on YugaByte DB. YugaByte DB's Cassandra-compatible YCQL API is used to integrate with other components of the app.

- IoT Spring Boot Dashboard
  This app uses the Java Spring Boot framework with its integration for Cassandra as the data layer, using the Cassandra Query Language (CQL) internally.

### Architecture with KSQL
![Architecture with KSQL](https://github.com/YugaByte/yb-iot-fleet-management/blob/master/yb-iot-fleet-mgmt-ksql-arch.png)

### Architecture with Apache Spark Streaming
![Architecture with Apache Spark Streaming](https://github.com/YugaByte/yb-iot-fleet-management/blob/master/yb-iot-fleet-mgmt-spark-arch.png)

## Prerequisites

For building these projects it requires following tools. Please refer README.md files of individual projects for more details.
- JDK - 1.11 +
- Maven - 3.3 +
- Confluent Open Source - 7.4.0 (we assume this is installed in the `$HOME/yb-kafka/confluent-os/confluent-7.4.0` directory).

## Run using kubernetes
Refer [here](https://github.com/YugaByte/yb-iot-fleet-management/tree/master/kubernetes/helm) for the setup and run steps using a helm based kubernetes environment.

## Steps to setup local environment
1. Clone this repository.
   ```sh
   git clone https://github.com/YugaByte/yb-iot-fleet-management.git ~
   ```

2. Build the required binaries.
   ```sh
   cd ~/yb-iot-fleet-management
   mvn package
   ```

3. Download Confluent Open Source from https://www.confluent.io/download/. This is a manual step, since an email id is needed to register (as of Nov 2018).
   Unbundle the content of the tar.gz to location `~/yb-kafka/confluent-os/confluent-7.4.0` using these steps.
   ```
   mkdir -p ~/yb-kafka/confluent-os
   cd ~/yb-kafka/confluent-os
   tar -xvf confluent-7.4.0.tar
   ```

4. Do the following to run Kafka and related components:
   ```
   export CONFLUENT_HOME=$HOME/yb-kafka/confluent-os/confluent-7.4.0
   export PATH=$PATH:$HOME/yb-kafka/confluent-os/confluent-7.4.0/bin
   confluent local services start
   ```

   The output for the `confluent status` should look like
   ```
    ZooKeeper is [UP]
    Kafka is [UP]
    Starting Schema Registry
    Schema Registry is [UP]
    Starting Kafka REST
    Kafka REST is [UP]
    Starting Connect
    Connect is [UP]
    Starting ksqlDB Server
    ksqlDB Server is [UP]
    Starting Control Center
    Control Center is [UP]
   ```
5. Create the origin Kafka topic
   ```
    kafka-topics --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 1 --topic iot-data-event
    ```
   *Note*: This is needed to be done only the first time.

6. Install YugaByte DB.
   - [Install YugaByte DB and start a local cluster](https://docs.yugabyte.com/quick-start/install/).

7. Create the YugaByte DB tables
   - Create the keyspaces and tables by running the following command. You can find `cqlsh` in the `bin` sub-directory located inside the YugaByte installation folder.
     ```
     $> cqlsh -f resources/IoTData.cql
     ```

8. Run the origin topic YugaByte DB Connect Sink

   ***Currently this isn't needed, so this step can be skipped***

   ```
   cd ~/yb-kafka/confluent-os/confluent-7.4.0
   nohup ./bin/connect-standalone ./etc/kafka/kafka.connect.properties ./etc/kafka-connect-yugabyte/origin.sink.properties >& origin_sink.txt &
   ```
   This will insert the origin topic data into the YugaByte DB CQL table `TrafficKeySpace.Origin_Table`.

## Running the application
From the top level directory of this repo, run the following

1. Start the data producer.
   ```sh
   cd ~/yb-iot-fleet-management
   java -jar iot-kafka-producer/target/iot-kafka-producer-1.0.0.jar
   ```

   It should start emitting data points to the Kafka topic. You should see something like the following as the output on the console:
   ```
   2017-10-16 12:31:52 INFO  IoTDataEncoder:28 - {"vehicleId":"0bf45cac-d1b8-4364-a906-980e1c2bdbcb","vehicleType":"Taxi","routeId":"Route-37","longitude":"-95.255615","latitude":"33.49808","timestamp":"2017-10-16 12:31:03","speed":49.0,"fuelLevel":38.0}

   2017-10-16 12:31:53 INFO  IoTDataEncoder:28 - {"vehicleId":"600863bc-c918-4c8e-a90b-7d66db4958e0","vehicleType":"18 Wheeler","routeId":"Route-43","longitude":"-97.918175","latitude":"35.78791","timestamp":"2017-10-16 12:31:03","speed":59.0,"fuelLevel":12.0}
   ```

2. Start the data processing application
  Use either of these options:
  - Spark
    - Set the following environment variables (note that you will need to create a Java keystore with the certificate if using SSL)
      - CASSANDRA_HOST
      - CASSANDRA_PORT
      - CASSANDRA_USERNAME
      - CASSANDRA_PASSWORD
      - CASSANDRA_SSL_ENABLED
      - CASSANDRA_KEYSTORE (only if sslEnabled=true)
      - CASSANDRA_KEYSTORE_PASSWORD (only if sslEnabled=true)
    - Run the spark app using this
      ```sh
      java -jar iot-spark-processor/target/iot-spark-processor-1.0.0.jar
      ```

  ***KSQL hasn't been revived and tested; Spark should used for data processing***

  - KSQL
    - Setup the KSQL tables/streams
      ```
      ksql <<EOF
      RUN SCRIPT './iot-ksql-processor/setup_streams.ksql';
      exit
      EOF
      ```
    - Run the connect sink from KSQL processed data
      ```
      cd ~/yb-kafka/confluent-os/confluent-7.4.0
      nohup ./bin/connect-standalone ./etc/kafka/kafka.ksql.connect.properties ./etc/kafka-connect-yugabyte/total_traffic.sink.properties ./etc/kafka-connect-yugabyte/window_traffic.sink.properties ./etc/kafka-connect-yugabyte/poi_traffic.sink.properties >& ksql_sink.txt &
      ```

3. Start the UI application.
   - Set the following environment variables:
     - CASSANDRA_HOST
     - CASSANDRA_PORT
     - CASSANDRA_USERNAME
     - CASSANDRA_PASSWORD
     - CASSANDRA_SSL_ENABLED
     - CASSANDRA_SSL_CERTIFICATE (only if sslEnabled=true) _<- Use the certicate inline, enclosed by quotes_
   - Start the application
     ```sh
     java -jar ~/yb-iot-fleet-management/iot-springboot-dashboard/target/iot-springboot-dashboard-1.0.0.jar
     ```

4. Now open the dashboard UI in a web browser. The application will refresh itself periodically.
   ```
   http://localhost:8080
   ```
