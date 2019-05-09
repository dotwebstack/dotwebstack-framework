package org.dotwebstack.framework.backend.rdf4j.shacl.propertypath;

import java.util.ArrayList;
import java.util.List;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.sail.memory.model.MemBNode;
import org.eclipse.rdf4j.sail.memory.model.MemStatement;
import org.eclipse.rdf4j.sail.memory.model.MemStatementList;
import org.eclipse.rdf4j.sail.memory.model.MemValue;

@Builder
@Getter
@Setter
public class SequencePath extends PropertyPath {

  private static final String RDF_SYNTAX = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";

  private static final String RDF_SYNTAX_FIRST = RDF_SYNTAX + "first";

  private static final String RDF_SYNTAX_REST = RDF_SYNTAX + "rest";

  private final MemBNode blankNode;

  private final IRI predicateIri;

  @Override
  public IRI getPredicateIri() {
    return this.predicateIri;
  }

  public List<IRI> getIris() {
    final List<IRI> result = new ArrayList<>();
    MemBNode nextNode = this.blankNode;

    while (nextNode != null) {
      final MemStatementList subjectStatements = nextNode.getSubjectStatementList();
      for (int i = 0; i < subjectStatements.size(); i++) {
        final MemStatement statement = subjectStatements.get(i);
        if (statement.getPredicate().stringValue().equals(RDF_SYNTAX_FIRST)) {
          if (statement.getObject() instanceof IRI) {
            result.add((IRI) statement.getObject());
          }
        }
        if (statement.getPredicate().stringValue().equals(RDF_SYNTAX_REST)) {
          final MemValue memValue = statement.getObject();
          if (memValue instanceof MemBNode) {
            nextNode = (MemBNode) memValue;
          } else {
            nextNode = null;
          }
          break;
        }
      }
    }
    return result;
  }

}
