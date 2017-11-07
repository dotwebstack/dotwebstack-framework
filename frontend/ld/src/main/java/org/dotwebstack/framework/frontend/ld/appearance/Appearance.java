package org.dotwebstack.framework.frontend.ld.appearance;

import java.io.OutputStream;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import lombok.NonNull;
import org.dotwebstack.framework.frontend.http.stage.Stage;
import org.dotwebstack.framework.informationproduct.InformationProduct;
import org.dotwebstack.framework.vocabulary.ELMO;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;

public class Appearance {

  private IRI identifier;

  private IRI type;
  
  private Model model;
  
  private Appearance(Builder builder) {
    identifier = builder.identifier;
    type = builder.appearanceType;
    model = builder.appearanceModel;
  }

  public Appearance() {
    identifier = ELMO.APPEARANCE;
    type = ELMO.APPEARANCE;
    model = new LinkedHashModel();
  }
  
  public IRI getIdentifier() {
    return identifier;
  }

  public IRI getAppearanceType() {
    return type;
  }
  
  public Model getModel() {
    return model;
  }
  
  public static class Builder {

    private IRI identifier;

    private IRI appearanceType;

    private Model appearanceModel;

    public Builder(@NonNull IRI identifier) {
      this.identifier = identifier;
    }

    public Builder appearanceType(
        IRI appearanceType) {
      this.appearanceType = appearanceType;
      return this;
    }

    public Builder appearanceModel(
        Model appearanceModel) {
      this.appearanceModel = appearanceModel;
      return this;
    }

    public Appearance build() {
      return new Appearance(this);
    }
  }

}
