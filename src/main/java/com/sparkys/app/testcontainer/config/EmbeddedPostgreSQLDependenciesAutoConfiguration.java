package com.sparkys.app.testcontainer.config;

import com.sparkys.app.testcontainer.common.spring.DependsOnPostProcessor;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

import static com.sparkys.app.testcontainer.config.PostgreSQLProperties.BEAN_NAME_EMBEDDED_POSTGRESQL;

@Configuration
@AutoConfigureOrder
@ConditionalOnClass(DataSource.class)
@ConditionalOnProperty(name = "embedded.postgresql.enabled", matchIfMissing = true)
@AutoConfigureAfter(name = "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration")
public class EmbeddedPostgreSQLDependenciesAutoConfiguration {

    @Bean
    public BeanFactoryPostProcessor datasourceDependencyPostProcessor() {
        return new DependsOnPostProcessor(DataSource.class, new String[]{BEAN_NAME_EMBEDDED_POSTGRESQL});
    }
}
