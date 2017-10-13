package org.dotwebstack.framework;

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
    return SimpleValueFactory.getInstance().createIRI(systemGraph);
  }

}
