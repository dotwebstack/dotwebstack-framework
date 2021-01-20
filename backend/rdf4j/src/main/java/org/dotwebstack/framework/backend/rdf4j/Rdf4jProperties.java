package org.dotwebstack.framework.backend.rdf4j;

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

  @Getter
  @Setter
  public static class ShapeProperties {

    @NotNull
    private IRI graph;

    @NotNull
    private String prefix;

  }
}
