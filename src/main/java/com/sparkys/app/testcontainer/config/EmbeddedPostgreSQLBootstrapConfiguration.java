package com.sparkys.app.testcontainer.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.LinkedHashMap;

import static com.sparkys.app.testcontainer.common.utils.ContainerUtils.containerLogsConsumer;
import static com.sparkys.app.testcontainer.config.PostgreSQLProperties.BEAN_NAME_EMBEDDED_POSTGRESQL;
import static org.springframework.core.Ordered.HIGHEST_PRECEDENCE;

@Slf4j
@Configuration
@Order(HIGHEST_PRECEDENCE)
@ConditionalOnProperty(name = "embedded.postgresql.enabled", matchIfMissing = true)
@EnableConfigurationProperties(PostgreSQLProperties.class)
public class EmbeddedPostgreSQLBootstrapConfiguration {

    private static class ConcretePostgreSQLContainer
            extends PostgreSQLContainer<ConcretePostgreSQLContainer> {
	public ConcretePostgreSQLContainer(final String dockerImageName) {
	    super(dockerImageName);
	}
    }

    @Bean(name = BEAN_NAME_EMBEDDED_POSTGRESQL, destroyMethod = "stop")
    public ConcretePostgreSQLContainer postgresql(
            final ConfigurableEnvironment environment,
            final PostgreSQLProperties properties) {
	log.info("Starting postgresql server. Docker image: {}",
	        properties.dockerImage);

	final ConcretePostgreSQLContainer postgresql = new ConcretePostgreSQLContainer(
	        properties.dockerImage).withUsername(properties.getUser())
	                .withPassword(properties.getPassword())
	                .withDatabaseName(properties.getDatabase())
	                .withLogConsumer(containerLogsConsumer(log))
	                .withStartupTimeout(properties.getTimeoutDuration());
	postgresql.start();
	registerPostgresqlEnvironment(postgresql, environment, properties);
	return postgresql;
    }

    private void registerPostgresqlEnvironment(
            final ConcretePostgreSQLContainer postgresql,
            final ConfigurableEnvironment environment,
            final PostgreSQLProperties properties) {
	final Integer mappedPort = postgresql
	        .getMappedPort(PostgreSQLContainer.POSTGRESQL_PORT);
	final String host = postgresql.getContainerIpAddress();

	final LinkedHashMap<String, Object> map = new LinkedHashMap<>();
	map.put("embedded.postgresql.port", mappedPort);
	map.put("embedded.postgresql.host", host);
	map.put("embedded.postgresql.schema", properties.getDatabase());
	map.put("embedded.postgresql.user", properties.getUser());
	map.put("embedded.postgresql.password", properties.getPassword());

	final String jdbcURL = "jdbc:postgresql://{}:{}/{}";
	log.info(
	        "Started postgresql server. Connection details: {}, "
	                + "JDBC connection url: " + jdbcURL,
	        map, host, mappedPort, properties.getDatabase());

	final MapPropertySource propertySource = new MapPropertySource(
	        "embeddedPostgreInfo", map);
	environment.getPropertySources().addFirst(propertySource);
    }
}
