package org.dotwebstack.framework;

import org.eclipse.rdf4j.model.IRI;

public class Product {

  private IRI identifier;

  private Source source;

  public Product(IRI identifier, Source source) {
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
