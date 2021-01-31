package org.dotwebstack.framework.backend.rdf4j.query;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalArgumentException;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalStateException;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.unsupportedOperationException;

import graphql.schema.DataFetchingFieldSelectionSet;
import graphql.schema.SelectedField;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.dotwebstack.framework.backend.rdf4j.config.Rdf4jTypeConfiguration;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShape;
import org.dotwebstack.framework.backend.rdf4j.shacl.PropertyShape;
import org.dotwebstack.framework.core.config.KeyConfiguration;
import org.dotwebstack.framework.core.datafetchers.FieldKeyCondition;
import org.dotwebstack.framework.core.datafetchers.KeyCondition;
import org.dotwebstack.framework.core.datafetchers.LoadEnvironment;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.sparqlbuilder.constraint.Expression;
import org.eclipse.rdf4j.sparqlbuilder.constraint.Operand;
import org.eclipse.rdf4j.sparqlbuilder.core.Projectable;
import org.eclipse.rdf4j.sparqlbuilder.core.SparqlBuilder;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;
import org.eclipse.rdf4j.sparqlbuilder.core.query.Queries;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPattern;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPatternNotTriples;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPatterns;
import org.eclipse.rdf4j.sparqlbuilder.rdf.RdfSubject;

public class Rdf4jQueryBuilder {

  private final AtomicInteger aliasCounter = new AtomicInteger();

  public Rdf4jQueryHolder build(Rdf4jTypeConfiguration typeConfiguration, NodeShape nodeShape,
      LoadEnvironment loadEnvironment, KeyCondition keyCondition) {
    return build(typeConfiguration, nodeShape, loadEnvironment,
        keyCondition != null ? List.of(keyCondition) : List.of());
  }

  public Rdf4jQueryHolder build(Rdf4jTypeConfiguration typeConfiguration, NodeShape nodeShape,
      LoadEnvironment loadEnvironment, Collection<KeyCondition> keyCondition) {
    Map<String, Object> fieldAliasMap = new HashMap<>();

    GraphPatternNotTriples wherePatterns =
        getWherePatterns(typeConfiguration, nodeShape, loadEnvironment, keyCondition, fieldAliasMap);

    String query = Queries.SELECT(createProjectables(fieldAliasMap).toArray(new Projectable[0]))
        .where(wherePatterns)
        .limit(10)
        .getQueryString();

    return Rdf4jQueryHolder.builder()
        .query(query)
        .fieldAliasMap(fieldAliasMap)
        .build();
  }

  @SuppressWarnings("unchecked")
  private List<Projectable> createProjectables(Map<String, Object> fieldAliasMap) {
    List<Projectable> projectables = fieldAliasMap.values()
        .stream()
        .filter(value -> value instanceof String)
        .map(value -> SparqlBuilder.var(value.toString()))
        .collect(Collectors.toList());

    fieldAliasMap.values()
        .stream()
        .filter(value -> value instanceof Map)
        .map(value -> (Map<String, Object>) value)
        .flatMap(map -> createProjectables(map).stream())
        .forEach(projectables::add);

    return projectables;
  }

  private GraphPatternNotTriples getWherePatterns(Rdf4jTypeConfiguration typeConfiguration, NodeShape nodeShape,
      LoadEnvironment loadEnvironment, Collection<KeyCondition> keyConditions, Map<String, Object> fieldAliasMap) {
    RdfSubject subject = SparqlBuilder.var(newAlias());

    GraphPattern[] graphPatterns =
        createGraphPatterns(typeConfiguration, nodeShape, loadEnvironment.getSelectionSet(), fieldAliasMap, subject);

    return createFilterPatterns(keyConditions, graphPatterns, fieldAliasMap);
  }

  private GraphPatternNotTriples createFilterPatterns(Collection<KeyCondition> keyConditions,
      GraphPattern[] graphPatterns, Map<String, Object> fieldAliasMap) {
    List<Expression<?>> operands = new ArrayList<>();

    if (keyConditions != null && keyConditions.size() > 0) {
      keyConditions.stream()
          .map(FieldKeyCondition.class::cast)
          .forEach(fieldKeyCondition -> {
            Variable variable = SparqlBuilder.var(fieldAliasMap.get(fieldKeyCondition.getFieldValues()
                .keySet()
                .iterator()
                .next())
                .toString());

            Operand value;
            // FIX
            // if (fieldKeyCondition.getFieldValues().values().iterator().next() instanceof List) {
            // List<String> values = (List<String>) fieldKeyCondition.getValue();
            // value = Rdf.literalOf(values.get(0));
            // } else {
            // value = Rdf.literalOf(fieldKeyCondition.getValue()
            // .toString());
            // }

            // operands.add(Expressions.equals(variable, value));
          });
    }

    GraphPatternNotTriples wherePatterns = GraphPatterns.and(graphPatterns);

    if (operands.size() > 1) {
      throw unsupportedOperationException("Multiple filter values not supported yet!");
    }

    if (operands.size() == 1) {
      wherePatterns.filter(operands.get(0));
    }

    return wherePatterns;
  }

  private GraphPattern[] createGraphPatterns(Rdf4jTypeConfiguration typeConfiguration, NodeShape nodeShape,
      DataFetchingFieldSelectionSet selectionSet, Map<String, Object> fieldAliasMap, RdfSubject subject) {
    // Create class patterns
    List<GraphPattern> classPatterns = createClassPatterns(subject, nodeShape);

    // Create patterns for selected fields
    List<GraphPattern> selectedPatterns = selectionSet.getImmediateFields()
        .stream()
        .map(selectedField -> createGraphPatterns(subject, selectedField, nodeShape, fieldAliasMap))
        .collect(Collectors.toList());

    // Create patterns for keys
    List<GraphPattern> keyPatterns = typeConfiguration.getKeys()
        .stream()
        .map(KeyConfiguration::getField)
        .filter(field -> !fieldAliasMap.containsKey(field))
        .map(nodeShape::getPropertyShape)
        .map(propertyShape -> createGraphPattern(subject, propertyShape, newAlias(), fieldAliasMap))
        .collect(Collectors.toList());

    return Stream.concat(Stream.concat(classPatterns.stream(), selectedPatterns.stream()), keyPatterns.stream())
        .toArray(GraphPattern[]::new);
  }

  private GraphPattern createGraphPatterns(RdfSubject subject, SelectedField selectedField, NodeShape nodeShape,
      Map<String, Object> fieldAliasMap) {
    String alias = newAlias();

    PropertyShape propertyShape = nodeShape.getPropertyShape(selectedField.getName());

    GraphPattern current = createGraphPattern(subject, propertyShape, alias, fieldAliasMap);

    if (selectedField.getSelectionSet()
        .getImmediateFields()
        .size() > 0) {
      if (propertyShape.getNode() == null) {
        throw illegalStateException("Nodeshape expected!");
      }

      List<GraphPattern> nested = selectedField.getSelectionSet()
          .getImmediateFields()
          .stream()
          .map(nestedSelectedField -> {
            Map<String, Object> nestedFieldAliasMap =
                (Map<String, Object>) fieldAliasMap.get(selectedField.getResultKey());
            if (nestedFieldAliasMap == null) {
              nestedFieldAliasMap = new HashMap<>();
            }

            GraphPattern nestedResult = createGraphPatterns(SparqlBuilder.var(alias), nestedSelectedField,
                propertyShape.getNode(), nestedFieldAliasMap);

            fieldAliasMap.put(selectedField.getResultKey(), nestedFieldAliasMap);

            return nestedResult;
          })
          .collect(Collectors.toList());

      GraphPattern[] patternBlock = Stream.concat(List.of(current)
          .stream(), nested.stream())
          .toArray(GraphPattern[]::new);
      return makeOptionalIfNeeded(propertyShape, patternBlock);
    }

    return makeOptionalIfNeeded(propertyShape, current);
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

  private GraphPattern createGraphPattern(RdfSubject subject, PropertyShape propertyShape, String alias,
      Map<String, Object> fieldAliasMap) {
    GraphPattern graphPattern = GraphPatterns.tp(subject, propertyShape.getPath()
        .toPredicate(), SparqlBuilder.var(alias));

    if (propertyShape.getNode() == null) {
      fieldAliasMap.put(propertyShape.getName(), alias);
    }

    return graphPattern;
  }

  private List<GraphPattern> createClassPatterns(RdfSubject rdfSubject, NodeShape nodeShape) {
    return nodeShape.getClasses()
        .stream()
        .flatMap(Collection::stream)
        .map(classIri -> GraphPatterns.tp(rdfSubject, RDF.TYPE, classIri))
        .collect(Collectors.toList());
  }

  private String newAlias() {
    return "x".concat(String.valueOf(aliasCounter.incrementAndGet()));
  }
}
