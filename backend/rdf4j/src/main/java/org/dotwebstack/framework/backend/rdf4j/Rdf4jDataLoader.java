package org.dotwebstack.framework.backend.rdf4j;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.NonNull;
import org.dotwebstack.framework.backend.rdf4j.config.Rdf4jTypeConfiguration;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShape;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShapeRegistry;
import org.dotwebstack.framework.backend.rdf4j.shacl.PropertyShape;
import org.dotwebstack.framework.core.config.FieldConfiguration;
import org.dotwebstack.framework.core.config.TypeConfiguration;
import org.dotwebstack.framework.core.datafetchers.BackendDataLoader;
import org.dotwebstack.framework.core.datafetchers.LoadEnvironment;
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
import org.eclipse.rdf4j.sparqlbuilder.core.query.SelectQuery;
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

  private final LocalRepositoryManager localRepositoryManager;

  private final NodeShapeRegistry nodeShapeRegistry;

  public Rdf4jDataLoader(@NonNull LocalRepositoryManager localRepositoryManager,
      @NonNull NodeShapeRegistry nodeShapeRegistry) {
    this.localRepositoryManager = localRepositoryManager;
    this.nodeShapeRegistry = nodeShapeRegistry;
  }

  @Override
  public boolean supports(TypeConfiguration<? extends FieldConfiguration> typeConfiguration) {
    return typeConfiguration instanceof Rdf4jTypeConfiguration;
  }

  @Override
  public Mono<Map<String, Object>> loadSingle(Object key, LoadEnvironment<?> environment) {
    TupleQueryResult queryResult = executeQuery(key, environment);

    BindingSet bindingSet = queryResult.next();

    return Mono.just(convertToMap(bindingSet));
  }

  private TupleQueryResult executeQuery(Object key, LoadEnvironment<?> environment) {
    String query = createQuery(key, environment);
    String repositoryId = "local";

    return localRepositoryManager.getRepository(repositoryId)
        .getConnection()
        .prepareTupleQuery(query)
        .evaluate();
  }

  private String createQuery(Object key, LoadEnvironment<?> environment) {
    NodeShape nodeShape = nodeShapeRegistry.get(environment.getObjectType());

    Collection<PropertyShape> propertyShapes = new ArrayList<>(nodeShape.getPropertyShapes()
        .values());

    SelectQuery query = Queries.SELECT();

    Variable subject = SparqlBuilder.var("subject");

    List<GraphPattern> graphPatterns = getWherePatterns(nodeShape, propertyShapes, subject);

    GraphPatternNotTriples wherePatterns =
        GraphPatterns.and(graphPatterns.toArray(new GraphPattern[graphPatterns.size()]));

    if (key != null) {
      Operand filterValue;
      if (key instanceof IRI) {
        filterValue = Rdf.iri((IRI) key);
      } else {
        filterValue = Rdf.literalOf(key.toString());
      }

      wherePatterns.filter(Expressions.equals(SparqlBuilder.var("identifier"), filterValue));
    }

    return query.select(getProjectables(propertyShapes).toArray(new Projectable[propertyShapes.size()]))
        .where(wherePatterns)
        .limit(10)
        .getQueryString();
  }

  private List<Projectable> getProjectables(Collection<PropertyShape> propertyShapes) {
    return propertyShapes.stream()
        .map(propertyShape -> SparqlBuilder.var(propertyShape.getName()))
        .collect(Collectors.toList());
  }

  private List<GraphPattern> getWherePatterns(NodeShape nodeShape, Collection<PropertyShape> propertyShapes,
      Variable subject) {
    List<GraphPattern> graphPatterns = new ArrayList<>();

    graphPatterns.addAll(nodeShape.getClasses()
        .stream()
        .flatMap(Collection::stream)
        .map(classIri -> GraphPatterns.tp(subject, RDF.TYPE, classIri))
        .collect(Collectors.toList()));

    graphPatterns.addAll(propertyShapes.stream()
        .map(propertyShape -> GraphPatterns.tp(subject, propertyShape.getPath()
            .toPredicate(), SparqlBuilder.var(propertyShape.getName())))
        .collect(Collectors.toList()));

    return graphPatterns;
  }

  @Override
  public Flux<Tuple2<Object, Map<String, Object>>> batchLoadSingle(Flux<Object> keys, LoadEnvironment<?> environment) {
    return keys.flatMap(key -> loadSingle(key, environment).map(item -> Tuples.of(key, item)));
  }

  @Override
  public Flux<Map<String, Object>> loadMany(Object key, LoadEnvironment environment) {
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
            objectValue = ((IRI) value).getLocalName();
          } else {
            objectValue = value.stringValue();
          }
          dataMap.put(bindingName, objectValue);
        });

    return dataMap;
  }

  @Override
  public Flux<Flux<Map<String, Object>>> batchLoadMany(List<Object> keys, LoadEnvironment<?> environment) {
    return Flux.fromIterable(keys)
        .map(key -> this.loadMany(key, environment));
  }
}
