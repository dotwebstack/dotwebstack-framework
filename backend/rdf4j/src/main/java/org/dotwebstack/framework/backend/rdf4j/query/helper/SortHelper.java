package org.dotwebstack.framework.backend.rdf4j.query.helper;

import static org.dotwebstack.framework.backend.rdf4j.query.helper.PathHelper.resolvePath;
import static org.dotwebstack.framework.backend.rdf4j.query.helper.SubjectHelper.getSubjectForField;

import graphql.Scalars;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLTypeUtil;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.NonNull;
import org.dotwebstack.framework.backend.rdf4j.directives.Rdf4jDirectives;
import org.dotwebstack.framework.backend.rdf4j.query.FieldPath;
import org.dotwebstack.framework.backend.rdf4j.query.model.OrderBy;
import org.dotwebstack.framework.backend.rdf4j.query.model.PathType;
import org.dotwebstack.framework.backend.rdf4j.query.model.Vertice;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShape;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;
import org.eclipse.rdf4j.sparqlbuilder.core.query.OuterQuery;
import org.eclipse.rdf4j.sparqlbuilder.rdf.Rdf;
import org.eclipse.rdf4j.sparqlbuilder.rdf.RdfValue;

public class SortHelper {

  public static List<GraphQLScalarType> NUMERIC_TYPES = Arrays.asList(Scalars.GraphQLInt, Scalars.GraphQLFloat,
      Scalars.GraphQLBigDecimal, Scalars.GraphQLBigDecimal, Scalars.GraphQLLong, Scalars.GraphQLBigInteger);

  private SortHelper() {}

  public static Optional<Variable> findOrderVariable(@NonNull Vertice vertice, @NonNull NodeShape nodeShape,
      @NonNull OuterQuery<?> query, @NonNull OrderBy orderBy) {
    FieldPath fieldPath = orderBy.getFieldPath()
        .last()
        .filter(leaf -> Objects.nonNull(leaf.getDirective(Rdf4jDirectives.RESOURCE_NAME)))
        .map(fp -> orderBy.getFieldPath()
            .rest()
            .orElse(null))
        .orElse(orderBy.getFieldPath());

    return resolvePath(vertice, nodeShape, orderBy.getFieldPath(), query, PathType.SORT).map(edge -> {
      Optional<FieldPath> restOptional = fieldPath.rest();
      if (restOptional.isPresent()) {
        NodeShape childShape = nodeShape.getChildNodeShape(fieldPath.getFieldDefinitions())
            .orElse(nodeShape);
        return getSubjectForField(edge, childShape, restOptional.get());
      }
      return getSubjectForField(edge, nodeShape, fieldPath);
    })
        .or(() -> Optional.of(vertice.getSubject()));
  }

  public static RdfValue getDefaultOrderByValue(@NonNull GraphQLFieldDefinition fieldDefinition) {
    GraphQLType type = GraphQLTypeUtil.unwrapOne(fieldDefinition.getType());

    if (NUMERIC_TYPES.stream()
        .anyMatch(numericType -> numericType.equals(type))) {
      return Rdf.literalOf(0);
    }

    return Rdf.literalOf("");
  }


}
