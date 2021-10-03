package org.dotwebstack.framework.backend.rdf4j.query;

import static org.dotwebstack.framework.backend.rdf4j.query.QueryHelper.applyCardinality;
import static org.dotwebstack.framework.backend.rdf4j.query.QueryHelper.createTypePatterns;
import static org.dotwebstack.framework.backend.rdf4j.query.QueryHelper.getObjectField;

import graphql.schema.SelectedField;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import lombok.Builder;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShape;
import org.dotwebstack.framework.core.backend.query.ObjectFieldMapper;
import org.dotwebstack.framework.core.query.model.KeyCriteria;
import org.dotwebstack.framework.core.query.model.ObjectRequest;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.util.Values;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.sparqlbuilder.core.SparqlBuilder;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPattern;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPatterns;

@Builder
public class WherePatternFactory {

  private final ObjectRequest objectRequest;

  private final NodeShape nodeShape;

  private final Variable subject;

  private final ObjectFieldMapper<BindingSet> fieldMapper;

  private final AliasManager aliasManager;

  private final Map<Variable, Set<? extends Value>> valuesMap = new HashMap<>();

  public GraphPattern create() {
    var typeVar = SparqlBuilder.var(aliasManager.newAlias());
    var typePatterns = createTypePatterns(subject, typeVar, nodeShape);
    var subPatterns = new ArrayList<>(typePatterns);

    objectRequest.getKeyCriteria()
        .stream()
        .flatMap(this::createFilterPattern)
        .forEach(subPatterns::add);

    objectRequest.getSelectedScalarFields()
        .stream()
        .flatMap(this::createWherePattern)
        .forEach(subPatterns::add);

    objectRequest.getSelectedObjectFields()
        .entrySet()
        .stream()
        .map(entry -> createNestedWherePattern(entry.getKey(), entry.getValue()))
        .forEach(subPatterns::add);

    var graphPattern = GraphPatterns.and(subPatterns.toArray(GraphPattern[]::new));

    if (!valuesMap.isEmpty()) {
      return new GraphPatternWithValues(graphPattern, valuesMap);
    }

    return graphPattern;
  }

  private Stream<GraphPattern> createFilterPattern(KeyCriteria keyCriteria) {
    return keyCriteria.getValues()
        .entrySet()
        .stream()
        .flatMap(entry -> createFilterPattern(entry.getKey(), entry.getValue()));
  }

  private Stream<GraphPattern> createFilterPattern(String name, Object value) {
    var objectField = getObjectField(objectRequest, name);

    if (objectField.isResource()) {
      valuesMap.put(subject, Set.of(Values.iri(value.toString())));
      return Stream.of();
    }

    var propertyShape = nodeShape.getPropertyShape(name);

    return Stream.of(subject.has(propertyShape.toPredicate(), Values.literal(value)));
  }

  private Stream<GraphPattern> createWherePattern(SelectedField selectedField) {
    var objectField = getObjectField(objectRequest, selectedField.getName());

    if (objectField.isResource()) {
      fieldMapper.register(selectedField.getName(), new BindingMapper(subject));
      return Stream.empty();
    }

    var objectAlias = aliasManager.newAlias();
    var propertyShape = nodeShape.getPropertyShape(selectedField.getName());

    fieldMapper.register(selectedField.getName(), new BindingMapper(objectAlias));

    return Stream
        .of(applyCardinality(propertyShape, subject.has(propertyShape.toPredicate(), SparqlBuilder.var(objectAlias))));
  }

  private GraphPattern createNestedWherePattern(SelectedField selectedField, ObjectRequest nestedObjectRequest) {
    var nestedResourceMapper = new BindingSetMapper(aliasManager.newAlias());
    var nestedResource = SparqlBuilder.var(nestedResourceMapper.getAlias());

    fieldMapper.register(selectedField.getName(), nestedResourceMapper);

    var propertyShape = nodeShape.getPropertyShape(selectedField.getName());

    var nestedPatternFactory = WherePatternFactory.builder()
        .objectRequest(nestedObjectRequest)
        .nodeShape(propertyShape.getNode())
        .subject(nestedResource)
        .fieldMapper(nestedResourceMapper)
        .aliasManager(aliasManager)
        .build();

    var nestedPattern = subject.has(propertyShape.toPredicate(), nestedResource)
        .and(nestedPatternFactory.create());

    return applyCardinality(propertyShape, nestedPattern);
  }
}
