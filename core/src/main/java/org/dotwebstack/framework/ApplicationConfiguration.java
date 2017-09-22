package org.dotwebstack.framework;

import org.dotwebstack.framework.config.ConfigurationBackend;
import org.dotwebstack.framework.config.FileConfigurationBackend;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;

@Configuration
public class ApplicationConfiguration {

  @Bean
  public ConfigurationBackend configurationBackend(Environment environment,
      @Value("classpath:/model/elmo.trig") Resource elmoConfiguration,
      @Value("${dotwebstack.config.resourcePath: classpath:.}") String resourcePath) {
    return new FileConfigurationBackend(elmoConfiguration, new SailRepository(new MemoryStore()),
        resourcePath, environment);
  }

}
