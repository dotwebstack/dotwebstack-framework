package org.dotwebstack.framework;

import java.util.Objects;
import org.eclipse.rdf4j.model.IRI;

public class Representation {

  private IRI identifier;
  private InformationProduct informationProduct;
  private String urlPattern;

  // private Stage stage;

  public static class Builder {

    private IRI identifier;
    private InformationProduct informationProduct;
    private String urlPattern;

    // private Stage stage;

    public Builder(IRI identifier) {
      this.identifier = Objects.requireNonNull(identifier);
    }
  }
}
