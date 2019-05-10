package org.dotwebstack.framework.backend.rdf4j.shacl.propertypath;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.sail.memory.model.MemBNode;
import org.eclipse.rdf4j.sail.memory.model.MemIRI;
import org.eclipse.rdf4j.sail.memory.model.MemStatementList;

public class PropertyPathFactory  {

  private PropertyPathFactory() {
    throw new IllegalStateException(
            String.format("%s is not meant to be instantiated.", PropertyPathFactory.class));
  }

  public static PropertyPath create(Model model, Resource subject, IRI predicate) {
    Value v = PropertyPathHelper.findRequiredProperty(model, subject, predicate);
    if (v instanceof MemBNode) {
      MemStatementList subjectStatements = ((MemBNode) v).getSubjectStatementList();

      if (subjectStatements.size() >= 1) {
        MemIRI predicateIri = subjectStatements.get(0).getPredicate();
        switch (predicateIri.stringValue()) {
          case PropertyPathHelper.SEQUENCE_PATH:
            return SequencePath.builder()
                    .first(create(model, subjectStatements.get(0).getSubject(),
                            subjectStatements.get(0).getPredicate()))
                    .rest(create(model, subjectStatements.get(1).getSubject(),
                            subjectStatements.get(1).getPredicate()))
                    .build();
          case PropertyPathHelper.INVERSE_PATH:
            return InversePath.builder()
                    .object((PredicatePath) create(model, subjectStatements.get(0).getSubject(),
                            subjectStatements.get(0).getPredicate()))
                    .build();
          case PropertyPathHelper.ALTERNATIVE_PATH:
            return AlternativePath.builder()
                    .object(create(model, subjectStatements.get(0).getSubject(),
                            subjectStatements.get(0).getPredicate()))
                    .build();
          case PropertyPathHelper.ZERO_OR_MORE_PATH:
            return ZeroOrMorePath.builder()
                    .object(create(model, subjectStatements.get(0).getSubject(),
                            subjectStatements.get(0).getPredicate()))
                    .build();
          case PropertyPathHelper.ONE_OR_MORE_PATH:
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
}
