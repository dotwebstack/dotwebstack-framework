package org.dotwebstack.framework.backend.rdf4j.query;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalArgumentException;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.dotwebstack.framework.backend.rdf4j.model.Rdf4jObjectField;
import org.dotwebstack.framework.backend.rdf4j.model.Rdf4jObjectType;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShape;
import org.dotwebstack.framework.backend.rdf4j.shacl.PropertyShape;
import org.dotwebstack.framework.core.query.model.ObjectRequest;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.repository.sparql.query.QueryStringUtil;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPattern;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPatterns;
import org.eclipse.rdf4j.sparqlbuilder.rdf.RdfPredicate;

class QueryHelper {

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

    return new GraphPatternWithValues(GraphPatterns.tp(subject, typePredicate, type), Map.of(type, classes));
  }

  public static GraphPattern applyCardinality(PropertyShape propertyShape, GraphPattern graphPattern) {
    var minCount = Optional.ofNullable(propertyShape.getMinCount())
        .orElse(0);

    return minCount == 0 ? graphPattern.optional() : graphPattern;
  }

  public static Rdf4jObjectField getObjectField(ObjectRequest objectRequest, String name) {
    var objectType = objectRequest.getObjectType();

    if (!(objectType instanceof Rdf4jObjectType)) {
      throw illegalArgumentException("Object type has wrong type.");
    }

    return ((Rdf4jObjectType) objectType).getField(name)
        .orElseThrow(() -> illegalArgumentException("Object field {} not found", name));
  }
}
