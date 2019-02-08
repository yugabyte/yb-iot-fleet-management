// Create keyspace
CREATE KEYSPACE IF NOT EXISTS TrafficKeySpace;

// Create tables
CREATE TABLE IF NOT EXISTS TrafficKeySpace.Origin_Table (vehicleId text, routeId text, vehicleType text, longitude text, latitude text, timeStamp timestamp, speed double, fuelLevel double, PRIMARY KEY ((vehicleId), timeStamp)) WITH default_time_to_live = 3600;
CREATE TABLE IF NOT EXISTS TrafficKeySpace.Total_Traffic (routeId text, vehicleType text, totalCount bigint, timeStamp timestamp, recordDate text, PRIMARY KEY (routeId, recordDate, vehicleType));
CREATE TABLE IF NOT EXISTS TrafficKeySpace.Window_Traffic (routeId text, vehicleType text, totalCount bigint, timeStamp timestamp, recordDate text, PRIMARY KEY (routeId, recordDate, vehicleType));
CREATE TABLE IF NOT EXISTS TrafficKeySpace.Poi_Traffic(vehicleid text, vehicletype text, distance bigint, timeStamp timestamp, PRIMARY KEY (vehicleid));

// Select from the tables
SELECT count(*) FROM TrafficKeySpace.Origin_Table;
SELECT count(*) FROM TrafficKeySpace.Total_Traffic;
SELECT count(*) FROM TrafficKeySpace.Window_Traffic;
SELECT count(*) FROM TrafficKeySpace.Poi_Traffic;

// Truncate the tables
TRUNCATE TABLE TrafficKeySpace.Origin_Table;
TRUNCATE TABLE TrafficKeySpace.Total_Traffic;
TRUNCATE TABLE TrafficKeySpace.Window_Traffic;
TRUNCATE TABLE TrafficKeySpace.Poi_Traffic;
