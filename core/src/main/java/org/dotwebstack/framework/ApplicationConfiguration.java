package org.dotwebstack.framework;

import org.dotwebstack.framework.config.ConfigurationBackend;
import org.dotwebstack.framework.config.FileConfigurationBackend;
import org.dotwebstack.framework.validation.ShaclValidator;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

@Configuration
public class ApplicationConfiguration {

  @Bean
  public ConfigurationBackend configurationBackend(
      @Value("classpath:/model/elmo.trig") Resource elmoConfiguration,
      @Value("#{applicationProperties.resourcePath}") String resourcePath,
      @Value("classpath:/model/elmo-shapes.trig") Resource elmoShapes,
      ShaclValidator shaclValidator) {
    return new FileConfigurationBackend(elmoConfiguration, new SailRepository(new MemoryStore()),
        resourcePath, elmoShapes, shaclValidator);
  }

}
