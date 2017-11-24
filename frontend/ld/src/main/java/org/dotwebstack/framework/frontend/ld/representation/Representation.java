package org.dotwebstack.framework.frontend.ld.representation;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import lombok.NonNull;
import org.dotwebstack.framework.frontend.http.stage.Stage;
import org.dotwebstack.framework.frontend.ld.appearance.Appearance;
import org.dotwebstack.framework.informationproduct.InformationProduct;
import org.eclipse.rdf4j.model.IRI;

public class Representation {

  private IRI identifier;

  private InformationProduct informationProduct;

  private Appearance appearance;

  private List<String> urlPatterns;

  private Stage stage;

  private List<Representation> subRepresentations;

  private Representation(Builder builder) {
    identifier = builder.identifier;
    urlPatterns = builder.urlPatterns;
    stage = builder.stage;
    informationProduct = builder.informationProduct;
    appearance = builder.appearance;
    subRepresentations = builder.subRepresentations;
  }

  public IRI getIdentifier() {
    return identifier;
  }

  public InformationProduct getInformationProduct() {
    return informationProduct;
  }

  public Appearance getAppearance() {
    return appearance;
  }

  public Collection<String> getUrlPatterns() {
    return urlPatterns;
  }

  public Stage getStage() {
    return stage;
  }

  public List<Representation> getSubRepresentations() {
    return subRepresentations;
  }

  public static class Builder {

    private IRI identifier;

    private InformationProduct informationProduct;

    private Appearance appearance;

    private List<String> urlPatterns = ImmutableList.of();

    private Stage stage;

    private List<Representation> subRepresentations = new ArrayList<>();

    public Builder(@NonNull IRI identifier) {
      this.identifier = identifier;
    }

    public Builder(@NonNull Representation representation) {
      this.identifier = representation.identifier;
      this.informationProduct = representation.informationProduct;
      this.appearance = representation.appearance;
      this.urlPatterns = representation.urlPatterns;
      this.stage = representation.stage;
      this.subRepresentations = representation.subRepresentations;
    }

    public Builder informationProduct(InformationProduct informationProduct) {
      this.informationProduct = informationProduct;
      return this;
    }

    public Builder appearance(Appearance appearance) {
      this.appearance = appearance;
      return this;
    }

    public Builder stage(Stage stage) {
      this.stage = stage;
      return this;
    }

    public Builder urlPatterns(String... urlPatterns) {
      this.urlPatterns = Arrays.asList(urlPatterns);
      return this;
    }

    public Builder subRepresentation(Representation subRespresentation) {
      this.subRepresentations.add(subRespresentation);
      return this;
    }

    public Representation build() {
      return new Representation(this);
    }
  }

}
