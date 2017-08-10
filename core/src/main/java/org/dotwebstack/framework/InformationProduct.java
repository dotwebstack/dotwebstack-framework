package org.dotwebstack.framework;

import org.eclipse.rdf4j.model.IRI;

class InformationProduct {

  private IRI identifier;

  private String label;

  public InformationProduct(IRI identifier) {
    this.identifier = identifier;
  }

  public IRI getIdentifier() {
    return identifier;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

}
