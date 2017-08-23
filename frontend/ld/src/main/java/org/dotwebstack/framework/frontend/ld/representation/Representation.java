package org.dotwebstack.framework.frontend.ld.representation;

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
    informationProduct = builder.informationProduct;
    urlPattern = builder.urlPattern;
    stage = builder.stage;
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

    // private Stage stage;

    public Builder(IRI identifier, InformationProduct informationProduct, String urlPattern) {
      this.identifier = Objects.requireNonNull(identifier);
      this.informationProduct = Objects.requireNonNull(informationProduct);
      this.urlPattern = urlPattern;
    }

    public Builder informationProduct(
        InformationProduct informationProduct) {
      this.informationProduct = informationProduct;
      return this;
    }

    public Builder urlPattern(String urlPattern) {
      this.urlPattern = urlPattern;
      return this;
    }

     /*public Builder stage(Stage stage) {
      this.stage = stage;
      return this;
    }*/

    public Representation build() {
      return new Representation(this);
    }
  }

}
