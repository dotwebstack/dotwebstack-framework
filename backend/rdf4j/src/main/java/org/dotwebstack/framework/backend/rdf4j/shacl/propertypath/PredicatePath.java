package org.dotwebstack.framework.backend.rdf4j.shacl.propertypath;

import java.util.Optional;
import lombok.Builder;
import lombok.Getter;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.util.Models;
import org.eclipse.rdf4j.sparqlbuilder.rdf.Rdf;
import org.eclipse.rdf4j.sparqlbuilder.rdf.RdfPredicate;

@Builder
@Getter
public class PredicatePath implements PropertyPath {

  private final IRI iri;

  @Override
  public Optional<Value> resolvePath(Model model, Resource subject) {
    return Models.getProperty(model, subject, this.iri);
  }

  public RdfPredicate toPredicate() {
    return () -> Rdf.iri(getIri()).getQueryString();
  }
}
