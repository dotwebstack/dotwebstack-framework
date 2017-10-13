package org.dotwebstack.framework;

import lombok.NonNull;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationProperties {

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
