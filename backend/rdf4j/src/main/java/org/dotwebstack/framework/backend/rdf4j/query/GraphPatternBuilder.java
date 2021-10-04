package org.dotwebstack.framework.backend.rdf4j.query;

import static org.dotwebstack.framework.backend.rdf4j.constants.Rdf4jConstants.JOIN_KEY_PREFIX;
import static org.dotwebstack.framework.backend.rdf4j.query.QueryHelper.applyCardinality;
import static org.dotwebstack.framework.backend.rdf4j.query.QueryHelper.createTypePatterns;
import static org.dotwebstack.framework.backend.rdf4j.query.QueryHelper.getObjectField;

import graphql.schema.GraphQLTypeUtil;
import graphql.schema.SelectedField;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShape;
import org.dotwebstack.framework.core.backend.query.AliasManager;
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

@Setter
@Accessors(fluent = true)
class GraphPatternBuilder {

  private ObjectRequest objectRequest;

  private NodeShape nodeShape;

  private Variable subject;

  private ObjectFieldMapper<BindingSet> fieldMapper;

  private AliasManager aliasManager;

  private final Map<Variable, Set<? extends Value>> valuesMap = new HashMap<>();

  private GraphPatternBuilder() {}

  public static GraphPatternBuilder newGraphPattern() {
    return new GraphPatternBuilder();
  }

  public GraphPattern build() {
    var typeVar = SparqlBuilder.var(aliasManager.newAlias());
    var typePatterns = createTypePatterns(subject, typeVar, nodeShape);
    var subPatterns = new ArrayList<>(typePatterns);

    createJoinPattern().ifPresent(subPatterns::add);

    objectRequest.getKeyCriteria()
        .stream()
        .flatMap(this::createPattern)
        .forEach(subPatterns::add);

    objectRequest.getSelectedScalarFields()
        .stream()
        .flatMap(this::createPattern)
        .forEach(subPatterns::add);

    objectRequest.getSelectedObjectFields()
        .entrySet()
        .stream()
        .flatMap(entry -> createNestedPattern(entry.getKey(), entry.getValue()))
        .forEach(subPatterns::add);

    var graphPattern = GraphPatterns.and(subPatterns.toArray(GraphPattern[]::new));

    if (!valuesMap.isEmpty()) {
      return new GraphPatternWithValues(graphPattern, valuesMap);
    }

    return graphPattern;
  }

  private Optional<GraphPattern> createJoinPattern() {
    var source = objectRequest.getSource();

    if (source == null) {
      return Optional.empty();
    }

    var parentField = objectRequest.getParentField();
    var joinCondition = (JoinCondition) source.get(JOIN_KEY_PREFIX.concat(parentField.getName()));

    return Optional.of(GraphPatterns.tp(joinCondition.getResource(), joinCondition.getPredicate(), subject));
  }

  private Stream<GraphPattern> createPattern(KeyCriteria keyCriteria) {
    return keyCriteria.getValues()
        .entrySet()
        .stream()
        .flatMap(entry -> createPattern(entry.getKey(), entry.getValue()));
  }

  private Stream<GraphPattern> createPattern(String name, Object value) {
    var objectField = getObjectField(objectRequest, name);

    if (objectField.isResource()) {
      valuesMap.put(subject, Set.of(Values.iri(value.toString())));
      return Stream.of();
    }

    var propertyShape = nodeShape.getPropertyShape(name);

    return Stream.of(subject.has(propertyShape.toPredicate(), Values.literal(value)));
  }

  private Stream<GraphPattern> createPattern(SelectedField selectedField) {
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

  private Stream<GraphPattern> createNestedPattern(SelectedField selectedField, ObjectRequest nestedObjectRequest) {
    var fieldType = GraphQLTypeUtil.unwrapNonNull(selectedField.getType());
    var propertyShape = nodeShape.getPropertyShape(selectedField.getName());

    if (GraphQLTypeUtil.isList(fieldType)) {
      // Provide join info for child data fetcher
      fieldMapper.register(JOIN_KEY_PREFIX.concat(selectedField.getName()),
          new JoinMapper(subject, propertyShape.toPredicate()));

      // Nested lists are never eager-loaded
      return Stream.empty();
    }

    var nestedResourceMapper = new BindingSetMapper(aliasManager.newAlias());
    var nestedResource = SparqlBuilder.var(nestedResourceMapper.getAlias());

    fieldMapper.register(selectedField.getName(), nestedResourceMapper);

    var nestedPattern = GraphPatternBuilder.newGraphPattern()
        .objectRequest(nestedObjectRequest)
        .nodeShape(propertyShape.getNode())
        .subject(nestedResource)
        .fieldMapper(nestedResourceMapper)
        .aliasManager(aliasManager)
        .build();

    nestedPattern = subject.has(propertyShape.toPredicate(), nestedResource)
        .and(nestedPattern);

    return Stream.of(applyCardinality(propertyShape, nestedPattern));
  }
}
