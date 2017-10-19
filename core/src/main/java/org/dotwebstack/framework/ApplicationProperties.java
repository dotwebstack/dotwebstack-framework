package org.dotwebstack.framework;

import org.dotwebstack.framework.config.ConfigurationException;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties(prefix = "dotwebstack.config")
@Configuration
public class ApplicationProperties {

  private String resourcePath = "file:src/main/resources";

  private String systemGraph;

  public void setResourcePath(String resourcePath) {
    this.resourcePath = resourcePath;
  }

  public String getResourcePath() {
    return resourcePath;
  }

  public void setSystemGraph(String systemGraph) {
    this.systemGraph = systemGraph;
  }

  public IRI getSystemGraph() {
    if (systemGraph == null) {
      // TODO Move the ConfigurationException one package level higher?
      throw new ConfigurationException("No systemGraph defined in the application configuration. "
          + "Please add the following property: dotwebstack.config.systemGraph");
    }

    return SimpleValueFactory.getInstance().createIRI(systemGraph);
  }

}
