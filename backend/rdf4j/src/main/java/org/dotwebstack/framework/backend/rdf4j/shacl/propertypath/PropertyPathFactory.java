package org.dotwebstack.framework.backend.rdf4j.shacl.propertypath;

import lombok.RequiredArgsConstructor;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.util.Models;
import org.eclipse.rdf4j.sail.memory.model.MemBNode;
import org.eclipse.rdf4j.sail.memory.model.MemIRI;
import org.eclipse.rdf4j.sail.memory.model.MemStatementList;

public class PropertyPathFactory  {

  private static final String SHACL_NS = "http://www.w3.org/ns/shacl#";

  private static final String RDF_NS = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";

  private static final String SEQUENCE_PATH = RDF_NS + "first";

  private static final String INVERSE_PATH = SHACL_NS + "inversePath";

  private static final String ALTERNATIVE_PATH = SHACL_NS + "alternativePath";

  private static final String ZERO_OR_MORE_PATH = SHACL_NS + "zeroOrMorePath";

  private static final String ONE_OR_MORE_PATH = SHACL_NS + "oneOrMorePath";

  private PropertyPathFactory() {
    throw new IllegalStateException(
            String.format("%s is not meant to be instantiated.", PropertyPathFactory.class));
  }

  public static PropertyPath create(Model model, Resource subject, IRI predicate) {
    Value v = findRequiredProperty(model, subject, predicate);
    if (v instanceof MemBNode) {
      MemStatementList subjectStatements = ((MemBNode) v).getSubjectStatementList();

      if (subjectStatements.size() >= 1) {
        MemIRI predicateIri = subjectStatements.get(0).getPredicate();
        switch (predicateIri.stringValue()) {
          case SEQUENCE_PATH:
            return SequencePath.builder()
                    .first(create(model, subjectStatements.get(0).getSubject(),
                            subjectStatements.get(0).getPredicate()))
                    .rest(create(model, subjectStatements.get(1).getSubject(),
                            subjectStatements.get(1).getPredicate()))
                    .build();
          case INVERSE_PATH:
            return InversePath.builder()
                    .object(create(model, subjectStatements.get(0).getSubject(),
                            subjectStatements.get(0).getPredicate()))
                    .build();
          case ALTERNATIVE_PATH:
            return AlternativePath.builder()
                    .object(create(model, subjectStatements.get(0).getSubject(),
                            subjectStatements.get(0).getPredicate()))
                    .build();
          case ZERO_OR_MORE_PATH:
            return ZeroOrMorePath.builder()
                    .object(create(model, subjectStatements.get(0).getSubject(),
                            subjectStatements.get(0).getPredicate()))
                    .build();
          case ONE_OR_MORE_PATH:
            return OneOrMorePath.builder()
                    .object(create(model, subjectStatements.get(0).getSubject(),
                            subjectStatements.get(0).getPredicate()))
                    .build();
          default:
            throw new IllegalArgumentException("Not yet implemented");
        }
      }
    }
    return PredicatePath.builder().iri((IRI) v).build();
  }

  public static Value findRequiredProperty(Model model, Resource subject, IRI predicate) {
    return Models.getProperty(model, subject, predicate)
            .orElseThrow(() -> new InvalidConfigurationException(String
                    .format("Resource '%s' requires a '%s' IRI property.", subject, predicate)));
  }
}
