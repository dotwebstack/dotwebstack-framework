package org.dotwebstack.framework.backend.rdf4j.query;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalStateException;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.unsupportedOperationException;

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
import org.dotwebstack.framework.core.datafetchers.keys.FieldKey;
import org.eclipse.rdf4j.model.vocabulary.RDF;
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

public class Rdf4jQueryBuilder {

  private final AtomicInteger aliasCounter = new AtomicInteger();

  public Rdf4jQueryHolder build(Rdf4jTypeConfiguration typeConfiguration, NodeShape nodeShape,
      List<SelectedField> selectedFields, Object key) {
    Map<String, Object> fieldAliasMap = new HashMap<>();

    GraphPatternNotTriples wherePatterns =
        getWherePatterns(typeConfiguration, nodeShape, selectedFields, key, fieldAliasMap);

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
      List<SelectedField> selectedFields, Object key, Map<String, Object> fieldAliasMap) {
    RdfSubject subject = SparqlBuilder.var(newAlias());

    GraphPattern[] graphPatterns =
        createGraphPatterns(typeConfiguration, nodeShape, selectedFields, fieldAliasMap, subject);

    return createFilterPatterns(typeConfiguration, key, graphPatterns, fieldAliasMap);
  }

  private GraphPatternNotTriples createFilterPatterns(Rdf4jTypeConfiguration typeConfiguration, Object key,
      GraphPattern[] graphPatterns, Map<String, Object> fieldAliasMap) {
    List<Expression<?>> operands = new ArrayList<>();

    if (key != null) {
      if (key instanceof FieldKey) {
        FieldKey fieldKey = (FieldKey) key;

        Variable variable = SparqlBuilder.var(fieldAliasMap.get(fieldKey.getName())
            .toString());

        Operand value;
        if (fieldKey.getValue() instanceof List) {
          List<String> values = (List<String>) fieldKey.getValue();
          value = Rdf.literalOf(values.get(0));
        } else {
          value = Rdf.literalOf(fieldKey.getValue()
              .toString());
        }

        operands.add(Expressions.equals(variable, value));
      }

      if (key instanceof String) {
        Variable variable = SparqlBuilder.var(typeConfiguration.getKeys()
            .get(0)
            .getField());

        Operand value = Rdf.literalOf(key.toString());
        operands.add(Expressions.equals(variable, value));
      }

      GraphPatternNotTriples wherePatterns = GraphPatterns.and(graphPatterns);

      if (operands.size() > 1) {
        throw unsupportedOperationException("Multiple filter values not supported yet!");
      }

      wherePatterns.filter(operands.get(0));

      return wherePatterns;
    }

    return GraphPatterns.and(graphPatterns);
  }

  private GraphPattern[] createGraphPatterns(Rdf4jTypeConfiguration typeConfiguration, NodeShape nodeShape,
      List<SelectedField> selectedFields, Map<String, Object> fieldAliasMap, RdfSubject subject) {
    // Create class patterns
    List<GraphPattern> classPatterns = createClassPatterns(subject, nodeShape);

    // Create patterns for selected fields
    List<GraphPattern> selectedPatterns = selectedFields.stream()
        .flatMap(selectedField -> createGraphPatterns(subject, selectedField, nodeShape, fieldAliasMap).stream())
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

  private List<GraphPattern> createGraphPatterns(RdfSubject subject, SelectedField selectedField, NodeShape nodeShape,
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
          .flatMap(nestedSelectedField -> {
            Map<String, Object> nestedFieldAliasMap = new HashMap<>();
            List<GraphPattern> nestedResult = createGraphPatterns(SparqlBuilder.var(alias), nestedSelectedField,
                propertyShape.getNode(), nestedFieldAliasMap);

            fieldAliasMap.put(selectedField.getResultKey(), nestedFieldAliasMap);

            return nestedResult.stream();
          })
          .collect(Collectors.toList());

      return Stream.concat(List.of(current)
          .stream(), nested.stream())
          .collect(Collectors.toList());
    }

    return List.of(current);
  }

  private GraphPattern createGraphPattern(RdfSubject subject, PropertyShape propertyShape, String alias,
      Map<String, Object> fieldAliasMap) {
    GraphPattern graphPattern = GraphPatterns.tp(subject, propertyShape.getPath()
        .toPredicate(), SparqlBuilder.var(alias));

    if (propertyShape.getMinCount() == null || propertyShape.getMinCount() == 0) {
      graphPattern = GraphPatterns.optional(graphPattern);
    }

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
