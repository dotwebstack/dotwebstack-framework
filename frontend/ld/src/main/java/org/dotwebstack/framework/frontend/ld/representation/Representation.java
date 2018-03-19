package org.dotwebstack.framework.frontend.ld.representation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import lombok.NonNull;
import org.dotwebstack.framework.frontend.http.stage.Stage;
import org.dotwebstack.framework.frontend.ld.appearance.Appearance;
import org.dotwebstack.framework.frontend.ld.parameter.ParameterMapper;
import org.dotwebstack.framework.informationproduct.InformationProduct;
import org.dotwebstack.framework.transaction.Transaction;
import org.eclipse.rdf4j.model.Resource;

public class Representation {

  private Resource identifier;

  private InformationProduct informationProduct;

  private Transaction transaction;

  private Appearance appearance;

  private List<String> appliesTo;

  private List<ParameterMapper> parameterMappers;

  private Stage stage;

  private List<Representation> subRepresentations;

  private Representation(Builder builder) {
    identifier = builder.identifier;
    appliesTo = builder.appliesTo;
    parameterMappers = builder.parameterMappers;
    stage = builder.stage;
    informationProduct = builder.informationProduct;
    this.transaction = builder.transaction;
    appearance = builder.appearance;
    subRepresentations = builder.subRepresentations;
  }

  public Resource getIdentifier() {
    return identifier;
  }

  public InformationProduct getInformationProduct() {
    return informationProduct;
  }

  public Transaction getTransaction() {
    return transaction;
  }

  public Appearance getAppearance() {
    return appearance;
  }

  public Collection<String> getAppliesTo() {
    return appliesTo;
  }

  public List<ParameterMapper> getParameterMappers() {
    return parameterMappers;
  }

  public Stage getStage() {
    return stage;
  }

  public List<Representation> getSubRepresentations() {
    return subRepresentations;
  }

  public void addSubRepresentation(@NonNull Representation subRepresentation) {
    this.subRepresentations.add(subRepresentation);
  }

  public static class Builder {

    private Resource identifier;

    private InformationProduct informationProduct;

    private Transaction transaction;

    private Appearance appearance;

    private List<String> appliesTo = new ArrayList<>();

    private List<ParameterMapper> parameterMappers = new ArrayList<>();

    private Stage stage;

    private List<Representation> subRepresentations = new ArrayList<>();

    public Builder(@NonNull Resource identifier) {
      this.identifier = identifier;
    }

    public Builder(@NonNull Representation representation) {
      this.identifier = representation.identifier;
      this.informationProduct = representation.informationProduct;
      this.transaction = representation.transaction;
      this.appearance = representation.appearance;
      this.appliesTo = representation.appliesTo;
      this.parameterMappers = representation.parameterMappers;
      this.stage = representation.stage;
      this.subRepresentations = representation.subRepresentations;
    }

    public Builder informationProduct(InformationProduct informationProduct) {
      this.informationProduct = informationProduct;
      return this;
    }

    public Builder transaction(Transaction transaction) {
      this.transaction = transaction;
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

    public Builder appliesTo(String appliesTo) {
      this.appliesTo.add(appliesTo);
      return this;
    }

    public Builder parameterMapper(ParameterMapper parameterMapper) {
      this.parameterMappers.add(parameterMapper);
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
