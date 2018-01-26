package org.dotwebstack.framework.param.term;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;

public class IriTermParameter extends TermParameter<IRI> {

  IriTermParameter(IRI identifier, String name, boolean required) {
    this(identifier, name, required, null);
  }

  IriTermParameter(IRI identifier, String name, boolean required, IRI defaultValue) {
    super(identifier, name, required, defaultValue);
  }

  @Override
  public Literal getValue(IRI value) {
    return FACTORY.createLiteral(value.stringValue());
  }

  @Override
  protected IRI handleInner(String value) {
    return FACTORY.createIRI(value);
  }
}
