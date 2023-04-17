package org.dotwebstack.framework.backend.rdf4j.query;

import static org.dotwebstack.framework.backend.rdf4j.query.QueryHelper.applyCardinality;
import static org.dotwebstack.framework.backend.rdf4j.query.QueryHelper.createTypePatterns;
import static org.dotwebstack.framework.backend.rdf4j.query.QueryHelper.getObjectField;
import static org.dotwebstack.framework.core.backend.BackendConstants.JOIN_KEY_PREFIX;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShape;
import org.dotwebstack.framework.core.backend.query.AliasManager;
import org.dotwebstack.framework.core.backend.query.ObjectFieldMapper;
import org.dotwebstack.framework.core.query.model.FieldRequest;
import org.dotwebstack.framework.core.query.model.KeyCriteria;
import org.dotwebstack.framework.core.query.model.SingleObjectRequest;
import org.dotwebstack.framework.ext.spatial.SpatialConstants;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.util.Values;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.sparqlbuilder.core.SparqlBuilder;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPattern;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPatterns;

@Setter(onMethod = @__({@NonNull}))
@Accessors(fluent = true)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
class GraphPatternBuilder {

  private SingleObjectRequest objectRequest;

  private NodeShape nodeShape;

  private Variable subject;

  private ObjectFieldMapper<BindingSet> fieldMapper;

  private AliasManager aliasManager;

  private final Map<Variable, Set<? extends Value>> valuesMap = new HashMap<>();

  public static GraphPatternBuilder newGraphPattern() {
    return new GraphPatternBuilder();
  }

  public GraphPattern build() {
    var typeVar = SparqlBuilder.var(aliasManager.newAlias());
    var typePatterns = createTypePatterns(subject, typeVar, nodeShape);
    var subPatterns = new ArrayList<>(typePatterns);

    objectRequest.getKeyCriterias()
        .stream()
        .flatMap(this::createPattern)
        .forEach(subPatterns::add);

    objectRequest.getScalarFields()
        .stream()
        .flatMap(this::createPattern)
        .forEach(subPatterns::add);

    objectRequest.getObjectFields()
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

  private Stream<GraphPattern> createPattern(KeyCriteria keyCriteria) {
    var fieldPath = keyCriteria.getFieldPath();
    return createPattern(fieldPath.get(fieldPath.size() - 1)
        .getName(), keyCriteria.getValue());
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

  private Stream<GraphPattern> createPattern(FieldRequest fieldRequest) {
    var objectField = getObjectField(objectRequest, fieldRequest.getName());

    if (objectField.isResource()) {
      fieldMapper.register(fieldRequest.getName(), new BindingMapper(subject));
      return Stream.empty();
    }

    var objectAlias = aliasManager.newAlias();
    var propertyShape = nodeShape.getPropertyShape(fieldRequest.getName());

    if (SpatialConstants.GEOMETRY.equals(objectField.getType())) {
      fieldMapper.register(fieldRequest.getName(), new GeometryBindingMapper(objectAlias));
    } else {
      fieldMapper.register(fieldRequest.getName(), new BindingMapper(objectAlias));
    }

    return Stream
        .of(applyCardinality(propertyShape, subject.has(propertyShape.toPredicate(), SparqlBuilder.var(objectAlias))));
  }

  private Stream<GraphPattern> createNestedPattern(FieldRequest fieldRequest, SingleObjectRequest nestedSingleObjectRequest) {
    var propertyShape = nodeShape.getPropertyShape(fieldRequest.getName());

    if (fieldRequest.isList()) {
      // Provide join info for child data fetcher
      fieldMapper.register(JOIN_KEY_PREFIX.concat(fieldRequest.getName()),
          new JoinMapper(subject, propertyShape.toPredicate()));

      // Nested lists are never eager-loaded
      return Stream.empty();
    }

    var nestedResourceMapper = new BindingSetMapper(aliasManager.newAlias());
    var nestedResource = SparqlBuilder.var(nestedResourceMapper.getAlias());

    fieldMapper.register(fieldRequest.getName(), nestedResourceMapper);

    var nestedPattern = GraphPatternBuilder.newGraphPattern()
        .objectRequest(nestedSingleObjectRequest)
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
