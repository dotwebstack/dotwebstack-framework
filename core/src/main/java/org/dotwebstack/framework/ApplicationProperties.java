package org.dotwebstack.framework;

import lombok.NonNull;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties(prefix = "dotwebstack.config")
@Configuration
public class ApplicationProperties {

  // TODO configurationproperties is not working, why not?
  @Value("${dotwebstack.config.resourcePath}")
  private String resourcePath = "file:src/main/resources";

  @Value("${dotwebstack.config.systemGraph}")
  @NonNull
  private String systemGraph;

  public String getResourcePath() {
    return resourcePath;
  }

  public IRI getSystemGraph() {
    return SimpleValueFactory.getInstance().createIRI(systemGraph);
  }

}
