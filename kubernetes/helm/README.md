# IoT Fleet Management App using Helm Charts

This page has details on deploying [Fleet Management IoT App](https://github.com/YugaByte/yb-iot-fleet-management) on [Kubernetes](https://kubernetes.io) using the `Helm Charts` feature. [Helm Charts](https://github.com/kubernetes/charts) can be used to deploy the app and its necessary dependencies on any configuration that customer prefers.

## Requirements
### Install Helm: 2.8.0 or later
One can install helm by following [these instructions](https://github.com/kubernetes/helm#install).
Check the version of helm installed using the following command:
```
helm version
Client: &version.Version{SemVer:"v2.12.3", GitCommit:"eecf22f77df5f65c823aacd2dbd30ae6c65f186e", GitTreeState:"clean"}
Server: &version.Version{SemVer:"v2.12.3", GitCommit:"eecf22f77df5f65c823aacd2dbd30ae6c65f186e", GitTreeState:"clean"}
```

## Configuration
Any one of the following container/kubernetes runtime environments will work.
- [Google Kubernetes Engine (GKE)](https://cloud.google.com/kubernetes-engine/)
- [Pivotal Container Service (PKS)](https://pivotal.io/platform/pivotal-container-service)
- [Minikube](https://kubernetes.io/docs/setup/minikube/) version 0.33+

## Clone the repositories
First, we setup the required repositories:
```
mkdir -p ~/code
cd ~/code
git clone https://github.com/confluentinc/cp-helm-charts.git
git clone https://github.com/YugaByte/yugabyte-db.git
git clone https://github.com/YugaByte/yb-iot-fleet-management.git
git clone https://github.com/YugaByte/yb-kafka-connector.git
```

These contain the helm charts need for the three main components: Kafka producer, YugaByte DB sink and the current IoT App.

## Install YugaByte DB
Install a YugaByte DB cluster using [these instructions](https://docs.yugabyte.com/latest/deploy/kubernetes/helm-chart/).

Sample command to setup single yb-master and single yb-tserver [YugaByte DB cluster](https://docs.yugabyte.com/latest/architecture/concepts/universe/).
```
cd ~/code/yugabyte-db/cloud/kubernetes/helm/yugabyte
helm install --name yb-iot --set resource.master.requests.cpu=0.8,resource.master.requests.memory=1Gi,resource.tserver.requests.cpu=0.8,resource.tserver.requests.memory=1Gi,replicas.master=1,replicas.tserver=1 .
```

These YugaByte pods should look like:
```
$ kubectl get pods
NAME                                             READY     STATUS             RESTARTS   AGE
yb-master-0                                      1/1       Running            0          26m
yb-tserver-0                                     1/1       Running            0          25m
```

### Create YCQL tables
The app uses Cassandra Query Language compatible [YCQL](https://docs.yugabyte.com/latest/api/cassandra/) to store the data in YugaByte DB. Run the script to create the tables using:

```
kubectl cp ~/code/yb-iot-fleet-management/resources/IoTData.cql yb-tserver-0:/home/yugabyte
kubectl exec -it yb-tserver-0 /home/yugabyte/bin/cqlsh -- -f /home/yugabyte/IoTData.cql
```

## Install Confluent Platform
Based on [CP-Kafka](https://github.com/confluentinc/cp-helm-charts/tree/master/charts/cp-kafka), the following brings up only the components needed for the IoT App.
```
cd ~/code/cp-helm-charts
helm install --name kafka-demo --set cp-kafka-rest.enabled=false,cp-kafka-connect.enabled=false,cp-kafka.brokers=1,cp-zookeeper.servers=1,cp-kafka.configurationOverrides.offsets.topic.replication.factor=1 .
```

*Note*: We bring up one broker for proof of concept purposes.

These Kafka related pods should look as follows:
```
$ kubectl get pods
NAME                                             READY     STATUS             RESTARTS   AGE
kafka-demo-cp-kafka-0                            2/2       Running            0          1m
kafka-demo-cp-ksql-server-64ff7f5579-hhvqq       2/2       Running            2          1m
kafka-demo-cp-schema-registry-774589f75f-2pbtg   2/2       Running            1          1m
kafka-demo-cp-zookeeper-0                        2/2       Running            0          1m
...
```

### Create origin topic
Run the following to create the root topic that simulates a fleet of different types of vehicles on the move and sending their stream of information.
```
$ kubectl exec -it kafka-demo-cp-kafka-0 -c cp-kafka-broker /usr/bin/kafka-topics -- --create --zookeeper kafka-demo-cp-zookeeper:2181 --replication-factor 1 --partitions 1 --topic iot-data-event
Created topic "iot-data-event".
```
This needs to be done only once per Kafka cluster.

## Setup YB Connect Sink dependencies
The [YugaByte Connect Sink](https://github.com/YugaByte/yb-kafka-connector) related depedencies and properties files can be copied into the Kafka cluster using the following steps.

First download the dependent jars
```
mkdir -p ~/kafka-connect-yugabyte/
cd ~/kafka-connect-yugabyte/
wget http://central.maven.org/maven2/io/netty/netty-all/4.1.25.Final/netty-all-4.1.25.Final.jar
wget http://central.maven.org/maven2/com/yugabyte/cassandra-driver-core/3.2.0-yb-18/cassandra-driver-core-3.2.0-yb-18.jar
wget http://central.maven.org/maven2/com/codahale/metrics/metrics-core/3.0.1/metrics-core-3.0.1.jar
```

Copy them into the kafka broker container
```
cd ~/kafka-connect-yugabyte/
kubectl cp netty-all-4.1.25.Final.jar kafka-demo-cp-kafka-0:/usr/share/java/kafka -c cp-kafka-broker
kubectl cp cassandra-driver-core-3.2.0-yb-18.jar kafka-demo-cp-kafka-0:/usr/share/java/kafka -c cp-kafka-broker
kubectl cp metrics-core-3.0.1.jar kafka-demo-cp-kafka-0:/usr/share/java/kafka -c cp-kafka-broker
```

Build and copy the YugaByte Kafka sink connector jar to the kafka server.
```
cd ~/code/yb-kafka-connector
mvn clean install -DskipTests
kubectl cp target/yb-kafka-connnector-1.0.0.jar kafka-demo-cp-kafka-0:/usr/share/java/kafka -c cp-kafka-broker
```

Copy the property files needed to run the sink connector.
*Note*: If a name different than `kafka-demo` was used, need to set the headless service info of the kafka brokers in these properties file.

```
cd ~/code/yb-iot-fleet-management/iot-ksql-processor/resources
kubectl cp kubernetes/ kafka-demo-cp-kafka-0:/etc/kafka -c cp-kafka-broker
```

## Setup the KSQL streams/tables

First create the ksql cli client pod using the example in the `cp-helm-charts` repo
# NOTE: First, edit this file to have the current kafka container name: *bootstrap-server=kafka-demo-cp-kafka:9092*
```
cd ~/code/cp-helm-charts/examples
kubectl apply -f ksql-demo.yaml
```

Wait for that `ksql-demo` container to be in Running state, and then Copy the streams setup commands files to the container and create the KSQL streams/tables needed by the IoT App.
```
kubectl cp ~/code/yb-iot-fleet-management/iot-ksql-processor/setup_streams.ksql ksql-demo:/opt -c ksql

kubectl exec -it ksql-demo -c ksql ksql  http://kafka-demo-cp-ksql-server:8088  <<EOF
RUN SCRIPT '/opt/setup_streams.ksql';
exit
EOF
```

## Start the YugaByte Kafka Connect sink

Run the connect script for the origin table to be saved. The TTL set on that table DDL will purge older data automatically.
```
kubectl exec -it kafka-demo-cp-kafka-0  -c cp-kafka-broker /usr/bin/connect-standalone -- /etc/kafka/kubernetes/kafka.connect.properties /etc/kafka/kubernetes/origin.sink.properties
```

Similarly for the other aggregated tables, setup the sink connectors:
```
kubectl exec -it kafka-demo-cp-kafka-0  -c cp-kafka-broker /usr/bin/connect-standalone -- /etc/kafka/kubernetes/kafka.ksql.connect.properties /etc/kafka/kubernetes/total_traffic.sink.properties /etc/kafka/kubernetes/window_traffic.sink.properties /etc/kafka/kubernetes/poi_traffic.sink.properties
```

## Start the Iot App

Grab the Kafka, Zookeeper and YB-tserver headless service names:
```
$ kubectl get services
NAME                               TYPE           CLUSTER-IP    EXTERNAL-IP       PORT(S)                               AGE
kafka-demo-cp-kafka-headless       ClusterIP      None           <none>           9092/TCP                              1h
kafka-demo-cp-zookeeper-headless   ClusterIP      None           <none>           2888/TCP,3888/TCP                     1h
yb-tservers                        ClusterIP      None           <none>           7100/TCP,9000/TCP,6379/TCP,9042/TCP   2h
```

Using these as the endpoints for services to start the IoT app
```
cd ~/code/yb-iot-fleet-management/kubernetes/helm
helm install yb-iot-helm --name iot-demo --set kafkaHostPort=kafka-demo-cp-kafka-headless:9092,zookeeperHostPort=kafka-demo-cp-zookeeper-headless:2181,yugabyteDBHost=yb-tservers
```

The pods for this app looks like
```
NAME                                             READY     STATUS             RESTARTS   AGE
iot-demo-yb-iot-helm-fb6b7db6f-rwskv             2/2       Running            0          46s
```

This pods has two containers:
- one ingests data into the origin topic of `iot-data-event`, which also get transformed into other streams/tables via KSQL.
- other reads the tables from YugaByte DB and reports in the springboard based UI.

## Check the IoT App UI
Using the `EXTERNAL-IP`:8080 from app's load balancer service, one can see the visual output of the fleet movement analytics.
```
$ kubectl get services
NAME                               TYPE           CLUSTER-IP     EXTERNAL-IP      PORT(S)                               AGE
iot-demo-yb-iot-helm               LoadBalancer   10.7.254.87    104.198.9.175    8080:31557/TCP                        1m
```

*Hint* : when using minikube, run the following to expose the load balancer endpoints for YugaByte DB UI and the app UI respectively.
```
minikube service  yb-master-ui --url
minikube service  iot-demo-yb-iot-helm  --url
```

## Next Steps:
- Package yb-kafka-sink jars more cleanly to reduce steps.
