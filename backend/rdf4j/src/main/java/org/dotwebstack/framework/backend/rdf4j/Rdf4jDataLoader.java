package org.dotwebstack.framework.backend.rdf4j;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.NonNull;
import org.dotwebstack.framework.backend.rdf4j.config.Rdf4jTypeConfiguration;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShape;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShapeRegistry;
import org.dotwebstack.framework.backend.rdf4j.shacl.PropertyShape;
import org.dotwebstack.framework.core.config.KeyConfiguration;
import org.dotwebstack.framework.core.config.TypeConfiguration;
import org.dotwebstack.framework.core.datafetchers.BackendDataLoader;
import org.dotwebstack.framework.core.datafetchers.LoadEnvironment;
import org.dotwebstack.framework.core.datafetchers.keys.FieldKey;
import org.dotwebstack.framework.core.datafetchers.keys.FilterKey;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.manager.LocalRepositoryManager;
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
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.TriplePattern;
import org.eclipse.rdf4j.sparqlbuilder.rdf.Rdf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

@Component
public class Rdf4jDataLoader implements BackendDataLoader {

  private static final Logger LOG = LoggerFactory.getLogger(Rdf4jDataLoader.class);

  private static final Variable SUBJECT = SparqlBuilder.var("subject");

  private final LocalRepositoryManager localRepositoryManager;

  private final NodeShapeRegistry nodeShapeRegistry;

  public Rdf4jDataLoader(@NonNull LocalRepositoryManager localRepositoryManager,
      @NonNull NodeShapeRegistry nodeShapeRegistry) {
    this.localRepositoryManager = localRepositoryManager;
    this.nodeShapeRegistry = nodeShapeRegistry;
  }

  @Override
  public boolean supports(TypeConfiguration<?> typeConfiguration) {
    return typeConfiguration instanceof Rdf4jTypeConfiguration;
  }

  @Override
  public Mono<Map<String, Object>> loadSingle(Object key, LoadEnvironment environment) {
    try (TupleQueryResult queryResult = executeQuery(List.of(key), environment)) {
      if (queryResult.hasNext()) {
        return Mono.just(convertToMap(queryResult.next()));
      }
      return Mono.empty();
    }
  }

  private TupleQueryResult executeQuery(Collection<Object> keys, LoadEnvironment environment) {
    String query = createQuery(keys, environment);

    LOG.debug("Sparql query: {}", query);

    return localRepositoryManager.getRepository("local")
        .getConnection()
        .prepareTupleQuery(query)
        .evaluate();
  }

  private String createQuery(Collection<Object> keys, LoadEnvironment environment) {
    NodeShape nodeShape = nodeShapeRegistry.get(environment.getObjectType());

    Set<String> requiredFields = getRequiredFields(environment);

    Collection<PropertyShape> propertyShapes = requiredFields.stream()
        .map(nodeShape::getPropertyShape)
        .collect(Collectors.toList());

    GraphPatternNotTriples wherePatterns = getWherePatterns(nodeShape, propertyShapes, keys, environment);

    return Queries.SELECT()
        .select(getProjectables(propertyShapes))
        .where(wherePatterns)
        .limit(10)
        .getQueryString();
  }

  private GraphPatternNotTriples getWherePatterns(NodeShape nodeShape, Collection<PropertyShape> propertyShapes,
      Collection<Object> keys, LoadEnvironment environment) {
    final List<GraphPattern> triplePatterns = getTriplePatterns(nodeShape, propertyShapes);

    List<Expression<?>> orOperands = new ArrayList<>();

    if (keys != null && !keys.isEmpty()) {
      keys.forEach(key -> {
        if (key instanceof FieldKey) {
          FieldKey fieldKey = (FieldKey) key;
          Variable variable = SparqlBuilder.var(fieldKey.getName());

          Operand value = Rdf.literalOf(fieldKey.getValue()
              .toString());
          orOperands.add(Expressions.equals(variable, value));
        }

        if (key instanceof String) {
          Variable variable = SparqlBuilder.var("identifier");

          Operand value = Rdf.literalOf(key.toString());
          orOperands.add(Expressions.equals(variable, value));
        }

        if (key instanceof FilterKey) {
          FilterKey filterKey = (FilterKey) key;
          PropertyShape propertyShape = nodeShape.getPropertyShape(filterKey.getPath()
              .get(0));

          TriplePattern triplePattern = GraphPatterns.tp(SUBJECT, propertyShape.getPath()
              .toPredicate(), SparqlBuilder.var(propertyShape.getName()));

          triplePatterns.add(triplePattern);

          PropertyShape propertyShape1 = propertyShape.getNode()
              .getPropertyShape(filterKey.getPath()
                  .get(1));

          Variable filterVariable = SparqlBuilder.var("filter");
          TriplePattern triplePattern1 =
              GraphPatterns.tp(SparqlBuilder.var(propertyShape.getName()), propertyShape1.getPath()
                  .toPredicate(), filterVariable);

          triplePatterns.add(triplePattern1);

          Operand value = Rdf.literalOf(((FilterKey) key).getValue()
              .toString());
          orOperands.add(Expressions.equals(filterVariable, value));
        }
      });

      GraphPatternNotTriples wherePatterns = GraphPatterns.and(triplePatterns.toArray(new GraphPattern[0]));

      if (orOperands.size() > 1) {
        // TODO: SparqlBuilder doesn't support VALUES block for now
        wherePatterns.filter(Expressions.or(orOperands.toArray(new Operand[0])));
      } else {
        wherePatterns.filter(orOperands.get(0));

      }

      return wherePatterns;
    }

    return GraphPatterns.and(triplePatterns.toArray(new GraphPattern[0]));

  }

  private Set<String> getRequiredFields(LoadEnvironment environment) {
    Rdf4jTypeConfiguration typeConfiguration = (Rdf4jTypeConfiguration) environment.getTypeConfiguration();

    return Stream.concat(typeConfiguration.getKeys()
        .stream()
        .map(KeyConfiguration::getField),
        environment.getSelectedFields()
            .stream())
        .collect(Collectors.toSet());
  }

  private Projectable[] getProjectables(Collection<PropertyShape> propertyShapes) {
    return propertyShapes.stream()
        .map(propertyShape -> SparqlBuilder.var(propertyShape.getName()))
        .toArray(Projectable[]::new);
  }

  private List<GraphPattern> getTriplePatterns(NodeShape nodeShape, Collection<PropertyShape> propertyShapes) {
    return Stream.concat(createClassPatterns(nodeShape).stream(), createPropertyPatterns(propertyShapes).stream())
        .collect(Collectors.toList());
  }

  private List<GraphPattern> createPropertyPatterns(Collection<PropertyShape> propertyShapes) {
    return propertyShapes.stream()
        .map(this::createGraphPattern)
        .collect(Collectors.toList());
  }

  private GraphPattern createGraphPattern(PropertyShape propertyShape) {
    GraphPattern graphPattern = GraphPatterns.tp(SUBJECT, propertyShape.getPath()
        .toPredicate(), SparqlBuilder.var(propertyShape.getName()));

    if (propertyShape.getMinCount() == null || propertyShape.getMinCount() == 0) {
      graphPattern = GraphPatterns.optional(graphPattern);
    }

    return graphPattern;
  }

  private List<GraphPattern> createClassPatterns(NodeShape nodeShape) {
    return nodeShape.getClasses()
        .stream()
        .flatMap(Collection::stream)
        .map(classIri -> GraphPatterns.tp(SUBJECT, RDF.TYPE, classIri))
        .collect(Collectors.toList());
  }

  @Override
  public Flux<Tuple2<Object, Map<String, Object>>> batchLoadSingle(Set<Object> keys, LoadEnvironment environment) {
    try (TupleQueryResult queryResult = executeQuery(keys, environment)) {
      List<Tuple2<Object, Map<String, Object>>> result = queryResult.stream()
          .map(bindingSet -> Tuples.of((Object) bindingSet.getValue(environment.getTypeConfiguration()
              .getKeys()
              .get(0)
              .getField())
              .stringValue(), convertToMap(bindingSet)))
          .collect(Collectors.toList());

      return Flux.fromIterable(result);
    }
  }

  @Override
  public Flux<Map<String, Object>> loadMany(Object key, LoadEnvironment environment) {
    TupleQueryResult queryResult = executeQuery(key != null ? List.of(key) : null, environment);

    Stream<Map<String, Object>> stream = queryResult.stream()
        .map(this::convertToMap);

    return Flux.fromStream(stream);
  }

  public Map<String, Object> convertToMap(BindingSet bindingSet) {
    Map<String, Object> dataMap = new HashMap<>();
    bindingSet.getBindingNames()
        .forEach(bindingName -> {
          Value value = bindingSet.getValue(bindingName);

          Object objectValue;

          if (value instanceof IRI) {
            objectValue = ((IRI) value).getLocalName();
          } else {
            objectValue = value.stringValue();
          }
          dataMap.put(bindingName, objectValue);
        });

    return dataMap;
  }

  @Override
  public Flux<Flux<Map<String, Object>>> batchLoadMany(List<Object> keys, LoadEnvironment environment) {
    return Flux.fromIterable(new HashSet<>(keys))
        .map(key -> this.loadMany(key, environment));
  }
}
