package org.dotwebstack.framework.backend.rdf4j.query;

import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.sparqlbuilder.core.Prefix;
import org.eclipse.rdf4j.sparqlbuilder.core.SparqlBuilder;
import org.eclipse.rdf4j.sparqlbuilder.core.query.OuterQuery;
import org.eclipse.rdf4j.sparqlbuilder.rdf.Iri;
import org.eclipse.rdf4j.sparqlbuilder.rdf.Rdf;

@RequiredArgsConstructor
abstract class AbstractQueryBuilder<Q extends OuterQuery<?>> {

  protected final QueryEnvironment environment;

  private final Map<String, Prefix> usedPrefixes = new HashMap<>();

  protected final Q query;

  Iri ns(IRI iri) {
    Prefix prefix = usedPrefixes.get(iri.getNamespace());

    if (prefix == null) {
      String alias = environment.getPrefixMap().get(iri.getNamespace());

      if (alias != null) {
        prefix = SparqlBuilder.prefix(alias, Rdf.iri(iri.getNamespace()));
        usedPrefixes.put(iri.getNamespace(), prefix);
        query.prefix(prefix);
      }
    }

    return prefix != null ? prefix.iri(iri.getLocalName()) : Rdf.iri(iri);
  }

}
