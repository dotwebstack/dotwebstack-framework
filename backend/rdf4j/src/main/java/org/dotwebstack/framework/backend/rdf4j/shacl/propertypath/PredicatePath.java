package org.dotwebstack.framework.backend.rdf4j.shacl.propertypath;

import java.util.Set;
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
public class PredicatePath extends BasePath {

  private final IRI iri;

  @Override
  public Set<Value> resolvePath(Model model, Resource subject) {
    return Models.getProperties(model, subject, this.iri);
  }

  @Override
  public RdfPredicate toConstructPredicate() {
    return toPredicate();
  }

  @Override
  public RdfPredicate toPredicate() {
    return () -> Rdf.iri(getIri().stringValue())
        .getQueryString();
  }
}
