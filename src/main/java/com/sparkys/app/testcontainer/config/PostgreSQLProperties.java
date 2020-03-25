package com.sparkys.app.testcontainer.config;

import com.sparkys.app.testcontainer.common.utils.CommonContainerProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@EqualsAndHashCode(callSuper = true)
@ConfigurationProperties("embedded.postgresql")
public class PostgreSQLProperties extends CommonContainerProperties {

    static final String BEAN_NAME_EMBEDDED_POSTGRESQL = "embeddedPostgreSql";
    String dockerImage = "postgres:10-alpine";

    String user = "postgresql";
    String password = "letmein";
    String database = "test_db";
}
