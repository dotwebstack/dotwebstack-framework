package org.dotwebstack.framework;

import org.eclipse.rdf4j.model.IRI;

public class InformationProduct {

  private IRI identifier;

  private Source source;

  public InformationProduct(IRI identifier, Source source) {
    this.identifier = identifier;
    this.source = source;
  }

  public IRI getIdentifier() {
    return identifier;
  }

  public Source getSource() {
    return source;
  }

}
