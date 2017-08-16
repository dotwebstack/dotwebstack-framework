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

    public Builder inforamationProduct(InformationProduct informationProduct) {
      this.informationProduct = informationProduct;
      return this;
    }

    public Builder urlPattern(String urlPattern) {
      this.urlPattern = urlPattern;
      return this;
    }
  }
}
