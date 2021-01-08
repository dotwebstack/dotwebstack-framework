package org.dotwebstack.framework.backend.rdf4j;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.invalidConfigurationException;

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
import org.dotwebstack.framework.core.datafetchers.keys.Key;
import org.dotwebstack.framework.core.datafetchers.keys.ReferencedKey;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.manager.LocalRepositoryManager;
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
  public Mono<Map<String, Object>> loadSingle(Key key, LoadEnvironment environment) {
    try (TupleQueryResult queryResult = executeQuery(key, environment)) {
      if (queryResult.hasNext()) {
        return Mono.just(convertToMap(queryResult.next()));
      }
      return Mono.empty();
    }
  }

  private TupleQueryResult executeQuery(Key key, LoadEnvironment environment) {
    String query = createQuery(key, environment);

    LOG.debug("Sparql query: {}", query);

    return localRepositoryManager.getRepository("local")
        .getConnection()
        .prepareTupleQuery(query)
        .evaluate();
  }

  private String createQuery(Key key, LoadEnvironment environment) {
    NodeShape nodeShape = nodeShapeRegistry.get(environment.getObjectType());

    Set<String> requiredFields = getRequiredFields(environment);

    Collection<PropertyShape> propertyShapes = requiredFields.stream()
        .map(nodeShape::getPropertyShape)
        .collect(Collectors.toList());

    GraphPatternNotTriples wherePatterns = getWherePatterns(nodeShape, propertyShapes, key, environment);

    return Queries.SELECT()
        .select(getProjectables(propertyShapes))
        .where(wherePatterns)
        .limit(10)
        .getQueryString();
  }

  private GraphPatternNotTriples getWherePatterns(NodeShape nodeShape, Collection<PropertyShape> propertyShapes,
      Key key, LoadEnvironment environment) {
    GraphPattern[] triplePatterns = getTriplePatterns(nodeShape, propertyShapes);

    GraphPatternNotTriples wherePatterns = GraphPatterns.and(triplePatterns);

    if (key != null) {
      Rdf4jTypeConfiguration typeConfiguration = (Rdf4jTypeConfiguration) environment.getTypeConfiguration();

      if (key instanceof ReferencedKey) {
        Variable variable = typeConfiguration.getKeys()
            .stream()
            .findFirst()
            .map(KeyConfiguration::getField)
            .map(SparqlBuilder::var)
            .orElseThrow(() -> invalidConfigurationException("No key field configured for nodeShape '{}'",
                typeConfiguration.getNodeShape()));

        Operand value = Rdf.literalOf(((ReferencedKey) key).getValue()
            .toString());

        wherePatterns.filter(Expressions.equals(variable, value));
      }
    }
    return wherePatterns;
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

  private GraphPattern[] getTriplePatterns(NodeShape nodeShape, Collection<PropertyShape> propertyShapes) {
    return Stream.concat(createClassPatterns(nodeShape).stream(), createPropertyPatterns(propertyShapes).stream())
        .toArray(GraphPattern[]::new);
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
  public Flux<Tuple2<Key, Map<String, Object>>> batchLoadSingle(Flux<Key> keys, LoadEnvironment environment) {
    return keys.flatMap(key -> loadSingle(key, environment).map(item -> Tuples.of(key, item)));
  }

  @Override
  public Flux<Map<String, Object>> loadMany(Key key, LoadEnvironment environment) {
    TupleQueryResult queryResult = executeQuery(key, environment);

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
            objectValue = ReferencedKey.builder()
                .value(((IRI) value).getLocalName())
                .build();
          } else {
            objectValue = value.stringValue();
          }
          dataMap.put(bindingName, objectValue);
        });

    return dataMap;
  }

  @Override
  public Flux<Flux<Map<String, Object>>> batchLoadMany(List<Key> keys, LoadEnvironment environment) {
    return Flux.fromIterable(keys)
        .map(key -> this.loadMany(key, environment));
  }
}
