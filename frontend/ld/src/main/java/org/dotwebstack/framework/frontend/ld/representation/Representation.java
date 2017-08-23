package org.dotwebstack.framework.frontend.ld.representation;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import org.dotwebstack.framework.frontend.http.stage.Stage;
import org.dotwebstack.framework.informationproduct.InformationProduct;
import org.eclipse.rdf4j.model.IRI;

public class Representation {

  private IRI identifier;

  private InformationProduct informationProduct;

  private String urlPattern;

  private Stage stage;

  private Representation(Builder builder) {
    identifier = builder.identifier;
    urlPattern = builder.urlPattern;
  }

  public IRI getIdentifier() {
    return identifier;
  }

  public InformationProduct getInformationProduct() {
    return informationProduct;
  }

  public String getUrlPattern() {
    return urlPattern;
  }

  public Stage getStage() {
    return stage;
  }

  public static class Builder {

    private IRI identifier;

    private InformationProduct informationProduct;

    private String urlPattern;

    private Stage stage;

    public Builder(IRI identifier, String urlPattern) {
      this.identifier = Objects.requireNonNull(identifier);
      this.urlPattern = Objects.requireNonNull(urlPattern);
    }

    public Builder informationProduct(
        InformationProduct informationProduct) {
      this.informationProduct = informationProduct;
      return this;
    }

    public Builder stage(Stage stage) {
      this.stage = stage;
      return this;
    }

    public Representation build() {
      return new Representation(this);
    }
  }

}
