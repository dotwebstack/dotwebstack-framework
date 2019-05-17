package org.dotwebstack.framework.backend.rdf4j.query;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.unsupportedOperationException;

import com.google.common.collect.ImmutableMap;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLTypeUtil;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.dotwebstack.framework.backend.rdf4j.ValueUtils;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShape;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShapeRegistry;
import org.dotwebstack.framework.backend.rdf4j.shacl.PropertyShape;
import org.dotwebstack.framework.core.DotWebStackRuntimeException;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;

@RequiredArgsConstructor
public final class ValueFetcher implements DataFetcher<Object> {

  private final PropertyShape propertyShape;
  private final NodeShapeRegistry nodeShapeRegistry;

  @Override
  public Object get(DataFetchingEnvironment environment) {
    GraphQLType fieldType = GraphQLTypeUtil.unwrapNonNull(environment.getFieldType());
    QuerySolution source = environment.getSource();

    return getValue(source.getModel(),source.getSubject(),fieldType,propertyShape);
  }

  private Object getValue(Model model, Resource subject, GraphQLType fieldType, PropertyShape propertyShape) {
    if (fieldType instanceof GraphQLFieldDefinition) {
      GraphQLType unwrappedType = GraphQLTypeUtil.unwrapNonNull(((GraphQLFieldDefinition) fieldType).getType());
      return getValue(model,subject,unwrappedType,propertyShape);
    }

    if (GraphQLTypeUtil.isScalar(fieldType)) {
      return getScalar(model,subject,propertyShape);
    }

    if (GraphQLTypeUtil.isList(fieldType)
        && GraphQLTypeUtil.isScalar(GraphQLTypeUtil.unwrapNonNull(((GraphQLList)fieldType).getWrappedType()))) {
      return getList(model,subject,propertyShape);
    }

    if (fieldType instanceof GraphQLObjectType) {
      NodeShape nodeShape = nodeShapeRegistry.get((IRI)propertyShape.getIdentifier());
      ImmutableMap.Builder<String,Object> builder = ImmutableMap.builder();

      propertyShape.getPath().resolvePath(model,subject,false).stream()
          .map(value -> (Resource) value)
          .forEach(resource -> {
            for (PropertyShape childShape : nodeShape.getPropertyShapes().values()) {
              builder.put(childShape.getName(),
                  getValue(model,resource, getChildType(fieldType, childShape), childShape));
            }
          });

      return builder.build();
    }

    throw unsupportedOperationException("Field type '{}' not supported.", fieldType);
  }

  private GraphQLType getChildType(GraphQLType fieldType, PropertyShape shape) {
    return fieldType.getChildren()
        .stream()
        .filter(childType -> StringUtils.equals(childType.getName(),shape.getName()))
        .findFirst()
        .orElseThrow(() -> new DotWebStackRuntimeException("No type found for propertyShape {}",shape.getName()));
  }

  private Object getScalar(Model model, Resource subject, PropertyShape propertyShape) {
    return propertyShape.getPath().resolvePath(model, subject, false)
        .stream()
        .findFirst()
        .map(ValueUtils::convertValue)
        .orElse(null);
  }

  private Object getList(Model model, Resource subject, PropertyShape propertyShape) {
    return propertyShape.getPath().resolvePath(model, subject, false)
        .stream()
        .map(ValueUtils::convertValue)
        .collect(Collectors.toList());
  }
}
