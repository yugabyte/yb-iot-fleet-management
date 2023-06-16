# Edit these environment variables to connect to your Yugabyte database
export CQLSH_HOST=localhost # The hostname(s) or IP address(es) of Yugabyte, comma delimited
export CQLSH_PORT=9042 # The port of the YCQL interface of Yugabyte
export CASSANDRA_HOST=localhost # The hostname(s) or IP address(es) of Yugabyte, comma delimited
export CASSANDRA_PORT=9042 # The port of the YCQL interface of Yugabyte
export CASSANDRA_USERNAME=cassandra # The username to connect with
export CASSANDRA_PASSWORD=password # The password of the user
export CASSANDRA_SSL_ENABLED=false # Whether or not to use SSL when connecting
export CASSANDRA_KEYSTORE=mykeystore.jks # The keystore containing the SSL certificate for Yugabyte. Not needed if CASSANDRA_SSL_ENABLED=false.
export CASSANDRA_KEYSTORE_PASSWORD=password # The password of the keystore containing the SSL certificate.  Not needed if CASSANDRA_SSL_ENABLED=false.
export CASSANDRA_SSL_CERTIFICATE=$HOME/root.crt # The path to the SSL certificate for Yugabyte. Not needed if CASSANDRA_SSL_ENABLED=false.
