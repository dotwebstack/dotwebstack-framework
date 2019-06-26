package org.dotwebstack.framework.backend.rdf4j.query;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.unsupportedOperationException;

import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLTypeUtil;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.NonNull;
import org.dotwebstack.framework.backend.rdf4j.converters.DefaultConverter;
import org.dotwebstack.framework.backend.rdf4j.helper.QuerySolutionHelper;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShapeRegistry;
import org.dotwebstack.framework.backend.rdf4j.shacl.PropertyShape;
import org.dotwebstack.framework.core.converters.CoreConverter;
import org.dotwebstack.framework.core.datafetchers.SourceDataFetcher;
import org.dotwebstack.framework.core.helpers.ExceptionHelper;
import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.springframework.stereotype.Component;

@Component
public final class ValueFetcher extends SourceDataFetcher {

  private final NodeShapeRegistry nodeShapeRegistry;

  private final List<CoreConverter<?>> converters;

  public ValueFetcher(final NodeShapeRegistry nodeShapeRegistry, List<CoreConverter<?>> converters) {
    this.nodeShapeRegistry = nodeShapeRegistry;
    this.converters = converters;
  }

  @Override
  public Object get(DataFetchingEnvironment environment) {
    GraphQLType fieldType = GraphQLTypeUtil.unwrapNonNull(environment.getFieldType());
    QuerySolution source = environment.getSource();

    PropertyShape propertyShape = getPropertyShape(environment);

    if (GraphQLTypeUtil.isList(fieldType)) {
      return resolve(propertyShape, source).collect(Collectors.toList());
    }

    if (GraphQLTypeUtil.isScalar(fieldType) || fieldType instanceof GraphQLObjectType) {
      return resolve(propertyShape, source).findFirst()
          .orElse(null);
    }

    throw unsupportedOperationException("Field type '{}' not supported.", fieldType);
  }

  private PropertyShape getPropertyShape(DataFetchingEnvironment environment) {
    if (environment.getParentType() instanceof GraphQLObjectType) {
      return nodeShapeRegistry.get((GraphQLObjectType) environment.getParentType())
          .getPropertyShape(environment.getField()
              .getName());
    }
    throw ExceptionHelper.unsupportedOperationException("Cannot determine property shape for parent type '{}'.",
        environment.getParentType()
            .getClass()
            .getSimpleName());
  }

  private Stream<Object> resolve(PropertyShape propertyShape, QuerySolution source) {
    return propertyShape.getPath()
        .resolvePath(source.getModel(), source.getSubject(), false)
        .stream()
        .map(value -> convert(source.getModel(), propertyShape, value))
        .filter(result -> {
          if (propertyShape.getNode() != null) {
            // in case we have strong typing (sh:node), remove the types from the result that do not conform
            // typing
            resultIsOfType(source.getSubject(), (QuerySolution) result, propertyShape.getNode()
                .getTargetClass());
          }
          return true;
        });
  }

  private boolean resultIsOfType(Resource subject, QuerySolution result, IRI type) {
    return QuerySolutionHelper.getSubjectStatements(result)
        .stream()
        .anyMatch(statement -> statement.getPredicate()
            .equals(RDF.TYPE)
            && statement.getObject()
                .equals(type));
  }

  public Object convert(@NonNull Model model, @NonNull PropertyShape propertyShape, @NonNull Value value) {
    if (propertyShape.getNode() != null || BNode.class.isAssignableFrom(value.getClass())) {
      return new QuerySolution(model, (Resource) value);
    }

    Optional<CoreConverter<?>> compatibleConverter = converters.stream()
        .filter(converter -> converter.supports(value))
        .findFirst();

    if (compatibleConverter.isPresent()) {
      return compatibleConverter.get()
          .convert(value);
    }

    return DefaultConverter.convert(value);
  }

  @Override
  public boolean supports(DataFetchingEnvironment environment) {
    return (environment.getSource() instanceof QuerySolution);
  }
}
