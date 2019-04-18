package org.dotwebstack.framework.backend.rdf4j;

import java.util.Map;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.eclipse.rdf4j.model.IRI;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "dotwebstack.rdf4j")
public class Rdf4jProperties {

  @NotNull
  private ShapeProperties shape;

  private Map<String, String> prefixes;

  private Map<String, RepositoryProperties> repositories;

  @Getter
  @Setter
  public static class ShapeProperties {

    @NotNull
    private IRI graph;

    @NotNull
    private String prefix;

  }

  @Getter
  @Setter
  public static class RepositoryProperties {

    @NotNull
    private String type;

    private Map<String, Object> args;

  }

}
