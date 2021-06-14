package org.dotwebstack.framework.integrationtest.graphqlpostgres;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer;

@org.springframework.boot.test.context.TestConfiguration
class TestConfiguration {
  @Bean
  public ConnectionFactoryInitializer initializer(ConnectionFactory connectionFactory) {
    ConnectionFactoryInitializer initializer = new ConnectionFactoryInitializer();
    initializer.setConnectionFactory(connectionFactory);

    return initializer;
  }
}
