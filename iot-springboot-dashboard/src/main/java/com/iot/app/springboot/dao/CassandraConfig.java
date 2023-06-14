package com.iot.app.springboot.dao;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.data.cassandra.config.CassandraClusterFactoryBean;
import org.springframework.data.cassandra.config.java.AbstractCassandraConfiguration;
import org.springframework.data.cassandra.mapping.BasicCassandraMappingContext;
import org.springframework.data.cassandra.mapping.CassandraMappingContext;
import org.springframework.data.cassandra.repository.config.EnableCassandraRepositories;

import com.datastax.driver.core.PlainTextAuthProvider;
import com.datastax.driver.core.SSLOptions;

/**
 * Spring bean configuration for Cassandra db.
 * 
 * @author abaghel
 *
 */
@Configuration
@PropertySource(value = { "classpath:iot-springboot.properties" })
@EnableCassandraRepositories(basePackages = { "com.iot.app.springboot.dao" })
public class CassandraConfig extends AbstractCassandraConfiguration {
    
    private static final Logger logger = Logger.getLogger(CassandraConfig.class);

    @Autowired
    private Environment environment;

    private SSLContext createSSLHandler() {
        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            FileInputStream fis = new FileInputStream(environment.getProperty("cassandra.certificate"));
            BufferedInputStream bis = new BufferedInputStream(fis);
            X509Certificate ca;
            try {
                ca = (X509Certificate) cf.generateCertificate(bis);
            } catch (Exception e) {
                System.err.println("Exception generating certificate from string: " + e);
                return null;
            }

            // Create a KeyStore containing our trusted CAs
            String keyStoreType = KeyStore.getDefaultType();
            KeyStore keyStore = KeyStore.getInstance(keyStoreType);
            keyStore.load(null, null);
            keyStore.setCertificateEntry("ca", ca);

            // Create a TrustManager that trusts the CAs in our KeyStore
            String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
            tmf.init(keyStore);

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, tmf.getTrustManagers(), null);
            return sslContext;
        } catch (Exception e) {
            System.err.println("Exception creating sslContext: " + e);
            return null;
        }
    }

    private SSLOptions sslOptions() {
        SSLOptions sslOptions = new SSLOptions(createSSLHandler(), SSLOptions.DEFAULT_SSL_CIPHER_SUITES);
        return sslOptions;
    }

    @Bean
    public CassandraClusterFactoryBean cluster() {
        CassandraClusterFactoryBean cluster = new CassandraClusterFactoryBean();
        String cassandraHost = environment.getProperty("cassandra.host");
        String cassandraPort = environment.getProperty("cassandra.port");
        logger.info("Using cassandra host=" + cassandraHost + " port=" + cassandraPort);
        cluster.setContactPoints(cassandraHost);
        cluster.setPort(Integer.parseInt(cassandraPort));
        PlainTextAuthProvider authProvider = new PlainTextAuthProvider(
                environment.getProperty("cassandra.username"),
                environment.getProperty("cassandra.password"));
        cluster.setAuthProvider(authProvider);
        cluster.setSslEnabled(Boolean.parseBoolean(environment.getProperty("cassandra.sslEnabled")));
        cluster.setSslOptions(sslOptions());
        return cluster;
    }

    @Bean
    public CassandraMappingContext cassandraMapping() {
        return new BasicCassandraMappingContext();
    }

    @Override
    @Bean
    protected String getKeyspaceName() {
        return environment.getProperty("cassandra.keyspace");
    }
}
