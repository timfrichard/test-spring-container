package com.sparkys.app.testcontainer.config;

import org.apache.tomcat.jdbc.pool.PoolConfiguration;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.sql.DataSource;

import static com.sparkys.app.testcontainer.config.PostgreSQLProperties.BEAN_NAME_EMBEDDED_POSTGRESQL;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("enabled")
@SpringBootTest(classes = {TestApplication.class,
        EmbeddedPostgreSQLBootstrapConfigurationTest.TestConfiguration.class})
public class EmbeddedPostgreSQLBootstrapConfigurationTest {

    @Autowired
    private ConfigurableListableBeanFactory beanFactory;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ConfigurableEnvironment environment;

    @Test
    void shouldConnectToPostgreSQL() {
        assertThat(jdbcTemplate.queryForObject("select version()", String.class)).contains("PostgreSQL");
    }

    @Test
    void shouldSaveAndGetUnicode() {
        jdbcTemplate.execute("CREATE TABLE employee(id INT, name VARCHAR(64));");
        jdbcTemplate.execute("insert into employee (id, name) values (1, 'some data \uD83D\uDE22');");

        assertThat(jdbcTemplate.queryForObject("select name from employee where id = 1", String.class)).isEqualTo("some data \uD83D\uDE22");
    }

    @Test
    void propertiesAreAvailable() {
        assertThat(environment.getProperty("embedded.postgresql.port")).isNotEmpty();
        assertThat(environment.getProperty("embedded.postgresql.host")).isNotEmpty();
        assertThat(environment.getProperty("embedded.postgresql.schema")).isNotEmpty();
        assertThat(environment.getProperty("embedded.postgresql.user")).isNotEmpty();
        assertThat(environment.getProperty("embedded.postgresql.password")).isNotEmpty();
    }

    @Test
    void shouldSetupDependsOnForAllDataSources() {
        String[] beanNamesForType = beanFactory.getBeanNamesForType(DataSource.class);
        assertThat(beanNamesForType)
                .as("Custom datasource should be present")
                .hasSize(1)
                .contains("customDatasource");
        asList(beanNamesForType).forEach(this::hasDependsOn);
    }

    private void hasDependsOn(String beanName) {
        assertThat(beanFactory.getBeanDefinition(beanName).getDependsOn())
                .isNotNull()
                .isNotEmpty()
                .contains(BEAN_NAME_EMBEDDED_POSTGRESQL);
    }

    @Configuration
    static class TestConfiguration {

        @Value("${spring.datasource.url}")
        private String jdbcUrl;
        @Value("${spring.datasource.username}")
        private String user;
        @Value("${spring.datasource.password}")
        private String password;

        @Bean(destroyMethod = "close")
        public DataSource customDatasource() {
            PoolConfiguration poolConfiguration = new PoolProperties();
            poolConfiguration.setUrl(jdbcUrl);
            poolConfiguration.setDriverClassName("org.postgresql.Driver");
            poolConfiguration.setUsername(user);
            poolConfiguration.setPassword(password);
            poolConfiguration.setTestOnBorrow(true);
            poolConfiguration.setTestOnReturn(true);
            return new org.apache.tomcat.jdbc.pool.DataSource(poolConfiguration);
        }
    }
}
