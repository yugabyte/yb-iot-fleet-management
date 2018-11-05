# IoT KSQL Processor

IoT KSQL Processor provides a sequence of steps to setup and process Kafka streams. Processed data is persisted in to YugaByte DB.

## Prerequisites
This project requires following tools.
- Confluent 5.0.0 or later, for KSQL. Assumed to be installed in `~/yb-kafka/confluent-os/confluent-5.0.0`.
- YugaByte DB, [installed](https://docs.yugabyte.com/quick-start/install/) and local cluster started up. `cqlsh` is present in this install.


*Note*: The steps 1 through 5 from `https://github.com/YugaByte/yb-kafka-connector` should be performed before running the following steps..


## Running the Kafka sinks
Please perform the following steps from the top level directory in this repo.

- Create a topic.
```
~/yb-kafka/confluent-os/confluent-5.0.0/bin/kafka-topics --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic iot-data-event
```
- Create the ksql streams/tables.
```
ksql <<EOF
RUN SCRIPT './iot-ksql-processor/setup_streams.ksql';
exit
EOF
```

- Setup the property files for Connect Sink
```
cp iot-ksql-processor/resources/kafka.connect.properties ~/yb-kafka/confluent-os/confluent-5.0.0/etc/kafka/
cp iot-ksql-processor/resources/*.sink.properties ~/yb-kafka/confluent-os/confluent-5.0.0/etc/kafka-connect-yugabyte
```

- Create the YugaByte DB tables
```
cqlsh -f resources/IoTData.cql
```

- Run the sink to save processed data to tables
```
cd ~/yb-kafka/confluent-os/confluent-5.0.0; ./bin/connect-standalone ./etc/kafka/kafka.connect.properties ./etc/kafka-connect-yugabyte/total_traffic.sink.properties ./etc/kafka-connect-yugabyte/window_traffic.sink.properties ./etc/kafka-connect-yugabyte/poi_traffic.sink.properties ./etc/kafka-connect-yugabyte/origin.sink.properties
```

- Check that the tables are getting populated using cqlsh
```
select count(*) from TrafficKeySpace.Origin_Table;
select count(*) from TrafficKeySpace.Total_Traffic;
select count(*) from TrafficKeySpace.Window_Traffic;
select count(*) from TrafficKeySpace.Poi_Traffic;
```

Once the SpringBoot app is running, one can visualize it at `http://localhost:8080` as well.
