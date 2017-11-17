package org.dotwebstack.framework.frontend.openapi.entity;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import io.swagger.models.Model;
import java.util.Map;
import org.eclipse.rdf4j.model.Resource;

public class GraphEntityContext {

  private final Map<String, Model> swaggerDefinitions;
  private final Map<String, String> ldPathNamespaces;
  private final org.eclipse.rdf4j.model.Model model;
  private final LdPathExecutor ldPathExecutor;
  private ImmutableList<Resource> subjects;

  public GraphEntityContext(Map<String, String> ldPathNamespaces,
      Map<String, Model> swaggerDefinitions, org.eclipse.rdf4j.model.Model model) {
    this.ldPathNamespaces = ldPathNamespaces;
    this.swaggerDefinitions = Maps.newHashMap(swaggerDefinitions);
    this.model = model;
    this.ldPathExecutor = new LdPathExecutor(this);
  }

  public LdPathExecutor getLdPathExecutor() {
    return this.ldPathExecutor;
  }

  public org.eclipse.rdf4j.model.Model getModel() {
    return model;
  }

  public ImmutableList<Resource> getSubjects() {
    return subjects;
  }

  public Map<String, String> getLdPathNamespaces() {
    return ldPathNamespaces;
  }

  public Map<String, Model> getSwaggerDefinitions() {
    return swaggerDefinitions;
  }

  //
  // public static class Builder {
  //
  // private final Map<String, Model> swaggerDefinitions = Maps.newHashMap();
  // private org.eclipse.rdf4j.model.Model model;
  // private Map<String, String> ldPathNamespaces;
  //
  // public Builder() {
  //
  // }
  //
  // public Builder model(org.eclipse.rdf4j.model.Model model) {
  // this.model = model;
  // return this;
  // }
  //
  // public GraphEntityContext build() {
  // return new GraphEntityContext(ldPathNamespaces, swaggerDefinitions,model);
  // }
  // }

}
