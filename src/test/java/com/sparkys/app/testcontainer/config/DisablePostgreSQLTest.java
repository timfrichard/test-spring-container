package com.sparkys.app.testcontainer.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.GenericContainer;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("disabled")
@SpringBootTest(properties = "embedded.postgresql.enabled=false")
public class DisablePostgreSQLTest {

    @Autowired
    ConfigurableListableBeanFactory beanFactory;

    @Test
    public void contextLoads() {
        String[] containers = beanFactory.getBeanNamesForType(GenericContainer.class);
        String[] postProcessors = beanFactory.getBeanNamesForType(BeanFactoryPostProcessor.class);

        assertThat(containers).isEmpty();
        assertThat(postProcessors).doesNotContain("datasourceDependencyPostProcessor");
    }
}
