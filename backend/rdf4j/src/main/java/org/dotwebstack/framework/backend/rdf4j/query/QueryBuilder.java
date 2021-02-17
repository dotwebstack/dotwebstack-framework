package org.dotwebstack.framework.backend.rdf4j.query;

import static org.dotwebstack.framework.backend.rdf4j.query.QueryUtil.addBinding;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalArgumentException;

import graphql.schema.DataFetchingFieldSelectionSet;
import graphql.schema.GraphQLTypeUtil;
import graphql.schema.SelectedField;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Builder;
import lombok.Getter;
import org.dotwebstack.framework.backend.rdf4j.config.Rdf4jTypeConfiguration;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShape;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShapeRegistry;
import org.dotwebstack.framework.backend.rdf4j.shacl.PropertyShape;
import org.dotwebstack.framework.core.config.KeyConfiguration;
import org.dotwebstack.framework.core.datafetchers.FieldKeyCondition;
import org.dotwebstack.framework.core.datafetchers.KeyCondition;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.sparqlbuilder.constraint.Expression;
import org.eclipse.rdf4j.sparqlbuilder.constraint.Expressions;
import org.eclipse.rdf4j.sparqlbuilder.constraint.Operand;
import org.eclipse.rdf4j.sparqlbuilder.core.Projectable;
import org.eclipse.rdf4j.sparqlbuilder.core.SparqlBuilder;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;
import org.eclipse.rdf4j.sparqlbuilder.core.query.Queries;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPattern;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPatternNotTriples;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPatterns;
import org.eclipse.rdf4j.sparqlbuilder.rdf.Rdf;
import org.eclipse.rdf4j.sparqlbuilder.rdf.RdfSubject;
import org.springframework.stereotype.Component;

@Component
public class QueryBuilder {

  private static final int HARD_LIMIT = 10;

  private final NodeShapeRegistry nodeShapeRegistry;

  public QueryBuilder(NodeShapeRegistry nodeShapeRegistry) {
    this.nodeShapeRegistry = nodeShapeRegistry;
  }

  public QueryHolder build(Rdf4jTypeConfiguration typeConfiguration, DataFetchingFieldSelectionSet selectionSet,
      KeyCondition keyCondition) {
    return build(new QueryContext(), typeConfiguration, selectionSet,
        keyCondition != null ? List.of(keyCondition) : List.of());
  }

  public QueryHolder build(Rdf4jTypeConfiguration typeConfiguration, DataFetchingFieldSelectionSet selectionSet,
      Collection<KeyCondition> keyConditions) {
    return build(new QueryContext(), typeConfiguration, selectionSet, keyConditions);
  }

  public QueryHolder build(QueryContext queryContext, Rdf4jTypeConfiguration typeConfiguration,
      DataFetchingFieldSelectionSet selectionSet, Collection<KeyCondition> keyConditions) {
    RdfSubject subject = SparqlBuilder.var(queryContext.newAlias());

    NodeShape nodeShape = nodeShapeRegistry.get(typeConfiguration.getName());

    if (keyConditions.isEmpty()) {
      GraphWrapper graphWrapper =
          createGraphWrapper(queryContext, typeConfiguration, nodeShape, selectionSet, "", subject, Map.of());

      String query = Queries.SELECT(graphWrapper.getProjectables()
          .get()
          .toArray(new Projectable[0]))
          .where(graphWrapper.getGraphPattern())
          .limit(HARD_LIMIT)
          .getQueryString();

      return QueryHolder.builder()
          .query(query)
          .mapAssembler(bindingSet -> graphWrapper.getMapAssembler()
              .apply(bindingSet))
          .build();
    }

    Map<String, String> keyFieldNames = keyConditions.stream()
        .findAny()
        .map(FieldKeyCondition.class::cast)
        .orElseThrow()
        .getFieldValues()
        .keySet()
        .stream()
        .collect(Collectors.toMap(Function.identity(), keyColumnName -> queryContext.newAlias()));

    GraphWrapper graphWrapper =
        createGraphWrapper(queryContext, typeConfiguration, nodeShape, selectionSet, "", subject, keyFieldNames);

    Projectable[] projectables = graphWrapper.getProjectables()
        .get()
        .toArray(new Projectable[0]);

    GraphPatternNotTriples graphPatternNotTriples = GraphPatterns.and(graphWrapper.getGraphPattern());

    createFilterPatterns(keyConditions, keyFieldNames).forEach(graphPatternNotTriples::filter);

    String query = Queries.SELECT(projectables)
        .where(graphPatternNotTriples)
        .limit(HARD_LIMIT)
        .getQueryString();

    return QueryHolder.builder()
        .query(query)
        .mapAssembler(bindingSet -> graphWrapper.getMapAssembler()
            .apply(bindingSet))
        .build();
  }

  private List<Expression<?>> createFilterPatterns(Collection<KeyCondition> keyConditions,
      Map<String, String> keyFieldNames) {
    if (keyConditions != null && keyConditions.size() > 0) {
      return keyConditions.stream()
          .map(FieldKeyCondition.class::cast)
          .map(FieldKeyCondition::getFieldValues)
          .flatMap(map -> map.entrySet()
              .stream())
          .map(valueEntry -> {
            Variable variable = SparqlBuilder.var(keyFieldNames.get(valueEntry.getKey()));
            Operand value;
            if (valueEntry.getValue() instanceof List) {
              value = Rdf.literalOf(((List<?>) valueEntry.getValue()).get(0)
                  .toString());
            } else {
              value = Rdf.literalOf(valueEntry.getValue()
                  .toString());
            }
            return Expressions.in(variable, value);
          })
          .collect(Collectors.toList());
    }

    return Collections.emptyList();
  }

  private Set<String> getFieldNames(Rdf4jTypeConfiguration typeConfiguration, Collection<SelectedField> selectedFields,
      Map<String, String> keyFieldNames) {
    return Stream.concat(Stream.concat(typeConfiguration.getKeys()
        .stream()
        .map(KeyConfiguration::getField),
        selectedFields.stream()
            .filter(selectedField -> !GraphQLTypeUtil.isList(selectedField.getFieldDefinition()
                .getType()))
            .map(SelectedField::getName)),
        keyFieldNames.keySet()
            .stream())
        .collect(Collectors.toSet());
  }

  private GraphWrapper createGraphWrapper(QueryContext queryContext, Rdf4jTypeConfiguration typeConfiguration,
      NodeShape nodeShape, DataFetchingFieldSelectionSet selectionSet, String fieldPathPrefix, RdfSubject subject,
      Map<String, String> keyFieldNames) {

    // Create class patterns
    List<GraphPattern> classPatterns = createClassPatterns(subject, nodeShape);

    Map<String, Function<BindingSet, Object>> assembleFns = new HashMap<>();

    List<Projectable> projectables = new ArrayList<>();

    Map<String, SelectedField> selectedFields = selectionSet.getFields(fieldPathPrefix.concat("*.*"))
        .stream()
        .collect(Collectors.toMap(SelectedField::getName, Function.identity()));

    // Create patterns for selected fields
    List<GraphPattern> selectedPatterns =
        getFieldNames(typeConfiguration, selectedFields.values(), keyFieldNames).stream()
            .map(fieldName -> {
              String alias = keyFieldNames.getOrDefault(fieldName, queryContext.newAlias());
              PropertyShape propertyShape = nodeShape.getPropertyShape(fieldName);

              GraphPattern current = GraphPatterns.tp(subject, propertyShape.getPath()
                  .toPredicate(), SparqlBuilder.var(alias));

              if (propertyShape.getNode() == null || selectedFields.get(fieldName) == null) {
                projectables.add(SparqlBuilder.var(alias));

                addBinding(assembleFns, alias, propertyShape, fieldName);

                return makeOptionalIfNeeded(propertyShape, current);
              } else {
                RdfSubject rdfSubject = SparqlBuilder.var(alias);

                SelectedField selectedField = selectedFields.get(fieldName);

                GraphWrapper graphWrapper = createGraphWrapper(queryContext, typeConfiguration, propertyShape.getNode(),
                    selectionSet, selectedField.getFullyQualifiedName()
                        .concat("/"),
                    rdfSubject, Map.of());

                assembleFns.put(fieldName, bindingSet -> graphWrapper.getMapAssembler()
                    .apply(bindingSet));
                projectables.addAll(graphWrapper.getProjectables()
                    .get());

                return GraphPatterns.and(makeOptionalIfNeeded(propertyShape, current), graphWrapper.getGraphPattern());
              }
            })
            .collect(Collectors.toList());

    GraphPatternNotTriples pattern = GraphPatterns.and(Stream.concat(classPatterns.stream(), selectedPatterns.stream())
        .toArray(GraphPattern[]::new));

    return GraphWrapper.builder()
        .graphPattern(pattern)
        .mapAssembler(bindingSet -> assembleFns.entrySet()
            .stream()
            .collect(HashMap::new, (acc, entry) -> acc.put(entry.getKey(), entry.getValue()
                .apply(bindingSet)), HashMap::putAll))
        .projectables(() -> projectables)
        .build();
  }

  private GraphPattern makeOptionalIfNeeded(PropertyShape propertyShape, GraphPattern... graphPatterns) {
    if (graphPatterns.length == 0) {
      throw illegalArgumentException("Graph pattern array size must be at least 1.");
    }

    GraphPattern graphPattern = graphPatterns[0];
    if (graphPatterns.length > 1) {
      graphPattern = GraphPatterns.and(graphPatterns);
    }

    return isPropertyOptional(propertyShape) ? GraphPatterns.optional(graphPattern) : graphPattern;
  }

  private boolean isPropertyOptional(PropertyShape propertyShape) {
    return propertyShape.getMinCount() == null || propertyShape.getMinCount() == 0;
  }

  private List<GraphPattern> createClassPatterns(RdfSubject rdfSubject, NodeShape nodeShape) {
    return nodeShape.getClasses()
        .stream()
        .flatMap(Collection::stream)
        .map(classIri -> GraphPatterns.tp(rdfSubject, RDF.TYPE, classIri))
        .collect(Collectors.toList());
  }

  @Builder
  @Getter
  static class GraphWrapper {
    private final GraphPattern graphPattern;

    private final Function<BindingSet, Map<String, Object>> mapAssembler;

    private final Supplier<List<Projectable>> projectables;
  }
}
