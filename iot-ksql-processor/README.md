# IoT KSQL Processor

IoT KSQL Processor provides a sequence of steps to setup and process Kafka streams. Processed data is persisted in to YugaByte DB.

## Prerequisites
This project requires following tools.
- Confluent 5.0.0 or later, for KSQL. Assumed to be installed in `~/yb-kafka/confluent-os/confluent-5.0.0`.
- YugaByte DB, [installed](https://docs.yugabyte.com/quick-start/install/) and local cluster started up. `cqlsh` is present in this install.
- The setup steps from the top-level [README](https://github.com/YugaByte/yb-iot-fleet-management/blob/master/README.md) should have been performed.

## Running real-time IoT KSQL processor
Please perform the following steps from the *top level directory* of this repo.

- Create the KSQL streams/tables.
```
ksql <<EOF
RUN SCRIPT './iot-ksql-processor/setup_streams.ksql';
exit
EOF
```

- Run the connect sink to save KSQL processed data to tables.
```
cd ~/yb-kafka/confluent-os/confluent-5.0.0
./bin/connect-standalone ./etc/kafka/kafka.connect.properties ./etc/kafka-connect-yugabyte/total_traffic.sink.properties ./etc/kafka-connect-yugabyte/window_traffic.sink.properties ./etc/kafka-connect-yugabyte/poi_traffic.sink.properties
```

- Check that the tables are getting populated using `cqlsh`.
```
$> cqlsh
select count(*) from TrafficKeySpace.Origin_Table;
select count(*) from TrafficKeySpace.Total_Traffic;
select count(*) from TrafficKeySpace.Window_Traffic;
select count(*) from TrafficKeySpace.Poi_Traffic;
```

Once the SpringBoot app is running, one can visualize it at `http://localhost:8080` as well.
