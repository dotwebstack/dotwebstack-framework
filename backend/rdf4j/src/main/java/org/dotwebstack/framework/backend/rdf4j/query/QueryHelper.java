package org.dotwebstack.framework.backend.rdf4j.query;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShape;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.repository.sparql.query.QueryStringUtil;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPattern;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPatterns;
import org.eclipse.rdf4j.sparqlbuilder.rdf.RdfPredicate;

public class QueryHelper {

  private QueryHelper() {}

  public static List<GraphPattern> createTypePatterns(Variable subject, Variable type, NodeShape nodeShape) {
    return nodeShape.getClasses()
        .stream()
        .map(classes -> createTypePattern(subject, type, classes))
        .collect(Collectors.toList());
  }

  private static GraphPattern createTypePattern(Variable subject, Variable type, Set<IRI> classes) {
    RdfPredicate typePredicate = () -> String.format("%s/%s*", QueryStringUtil.valueToString(RDF.TYPE),
        QueryStringUtil.valueToString(RDFS.SUBCLASSOF));

    if (classes.size() == 1) {
      return GraphPatterns.tp(subject, typePredicate, classes.iterator()
          .next());
    }

    var graphPattern = GraphPatterns.tp(subject, typePredicate, type);

    return new GraphPatternWithValues(graphPattern, Map.of(type, classes));
  }

  public static FieldMapper<Object> createFieldMapper(String objectAlias) {
    return bindings -> {
      if (!bindings.hasBinding(objectAlias)) {
        return null;
      }

      return bindings.getBinding(objectAlias)
          .getValue()
          .stringValue();
    };
  }
}
