package org.dotwebstack.framework.informationproduct;

import org.eclipse.rdf4j.model.IRI;

public abstract class AbstractInformationProduct implements InformationProduct {

  private IRI identifier;

  private String label;

  protected AbstractInformationProduct(IRI identifier, String label) {
    this.identifier = identifier;
    this.label = label;
  }

  public IRI getIdentifier() {
    return identifier;
  }

  public String getLabel() {
    return label;
  }


}
