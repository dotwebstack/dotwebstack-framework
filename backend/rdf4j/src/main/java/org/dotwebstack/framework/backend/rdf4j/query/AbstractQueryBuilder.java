package org.dotwebstack.framework.backend.rdf4j.query;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import org.dotwebstack.framework.backend.rdf4j.shacl.propertypath.InversePath;
import org.dotwebstack.framework.backend.rdf4j.shacl.propertypath.PredicatePath;
import org.dotwebstack.framework.backend.rdf4j.shacl.propertypath.PropertyPath;
import org.dotwebstack.framework.backend.rdf4j.shacl.propertypath.SequencePath;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.sail.memory.model.MemIRI;
import org.eclipse.rdf4j.sparqlbuilder.core.Prefix;
import org.eclipse.rdf4j.sparqlbuilder.core.SparqlBuilder;
import org.eclipse.rdf4j.sparqlbuilder.core.query.OuterQuery;
import org.eclipse.rdf4j.sparqlbuilder.rdf.Iri;
import org.eclipse.rdf4j.sparqlbuilder.rdf.Rdf;
import org.eclipse.rdf4j.sparqlbuilder.rdf.RdfPredicate;

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

  RdfPredicate toPredicate(final PropertyPath path) {
    return () -> {
      StringBuilder builder = new StringBuilder();
      add(builder, path);
      return builder.toString();
    };
  }

  private void add(StringBuilder builder, PropertyPath path) {
    if (path instanceof PredicatePath) {
      add(builder, (PredicatePath) path);
    } else if (path instanceof SequencePath) {
      add(builder, (SequencePath) path);
    } else if (path instanceof InversePath) {
      add(builder, (InversePath) path);
    } else {
      throw new IllegalArgumentException("not implemented yet");
    }
  }

  private void add(StringBuilder builder, InversePath path) {
    builder.append("^");
    add(builder, path.getObject());
  }

  private void add(StringBuilder builder, SequencePath path) {
    add(builder, path.getFirst());
    if ((path.getRest() instanceof PredicatePath)) {
      PredicatePath predicatePath = (PredicatePath) path.getRest();
      if (predicatePath.getIri() instanceof MemIRI && predicatePath.getIri().toString().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#nil")) {
        return;
      }
    }
    builder.append("/");
    add(builder, path.getRest());
  }

  private void add(StringBuilder builder, PredicatePath path) {
    builder.append(ns(path.getIri()).getQueryString());
  }



}
