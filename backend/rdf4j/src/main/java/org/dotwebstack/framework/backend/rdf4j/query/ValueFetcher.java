package org.dotwebstack.framework.backend.rdf4j.query;

import static org.dotwebstack.framework.backend.rdf4j.helper.MemStatementListHelper.listOf;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.unsupportedOperationException;

import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLTypeUtil;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.NonNull;
import org.dotwebstack.framework.backend.rdf4j.converters.Rdf4jConverterRouter;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShapeRegistry;
import org.dotwebstack.framework.backend.rdf4j.shacl.PropertyShape;
import org.dotwebstack.framework.core.datafetchers.SourceDataFetcher;
import org.dotwebstack.framework.core.helpers.ExceptionHelper;
import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.SimpleIRI;
import org.eclipse.rdf4j.model.util.Models;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.sail.memory.model.MemResource;
import org.springframework.stereotype.Component;

@Component
public final class ValueFetcher extends SourceDataFetcher {

  private final NodeShapeRegistry nodeShapeRegistry;

  public ValueFetcher(final NodeShapeRegistry nodeShapeRegistry, Rdf4jConverterRouter router) {
    super(router);
    this.nodeShapeRegistry = nodeShapeRegistry;
  }

  @Override
  public Object get(DataFetchingEnvironment environment) {
    GraphQLType fieldType = GraphQLTypeUtil.unwrapNonNull(environment.getFieldType());
    QuerySolution source = environment.getSource();

    PropertyShape propertyShape = getPropertyShape(environment);

    if (GraphQLTypeUtil.isList(fieldType)) {
      return resolve(propertyShape, source).map(value -> convert(source.getModel(), propertyShape, value))
          .collect(Collectors.toList());
    }

    if (GraphQLTypeUtil.isScalar(fieldType) || fieldType instanceof GraphQLObjectType) {
      return resolve(propertyShape, source).map(value -> convert(source.getModel(), propertyShape, value))
          .findFirst()
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

  private Stream<Value> resolve(PropertyShape propertyShape, QuerySolution source) {
    return propertyShape.getPath()
        .resolvePath(source.getModel(), source.getSubject())
        .stream()
        .filter(result -> {
          if (propertyShape.getNode() != null) {
            // in case we have strong typing (sh:node), remove the types from the result that do not conform
            // typing
            if (result instanceof SimpleIRI) {
              return Models.getProperties(source.getModel(), (SimpleIRI) result, RDF.TYPE)
                  .stream()
                  .anyMatch(property -> property.equals(propertyShape.getNode()
                      .getTargetClass()));
            }

            return resultIsOfType(result, propertyShape.getNode()
                .getTargetClass());
          }
          return true;
        });
  }

  private boolean resultIsOfType(Value value, IRI type) {
    return listOf(((MemResource) value).getSubjectStatementList()).stream()
        .anyMatch(statement -> statement.getPredicate()
            .equals(RDF.TYPE)
            && statement.getObject()
                .equals(type));
  }

  private Object convert(@NonNull Model model, @NonNull PropertyShape propertyShape, @NonNull Value value) {
    if (propertyShape.getNode() != null || BNode.class.isAssignableFrom(value.getClass())) {
      return new QuerySolution(model, (Resource) value);
    }

    return this.converterRouter.convert(value);
  }

  @Override
  public boolean supports(DataFetchingEnvironment environment) {
    return (environment.getSource() instanceof QuerySolution);
  }
}
