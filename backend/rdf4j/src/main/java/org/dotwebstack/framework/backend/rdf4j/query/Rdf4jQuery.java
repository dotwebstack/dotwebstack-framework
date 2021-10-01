package org.dotwebstack.framework.backend.rdf4j.query;

import graphql.schema.SelectedField;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.validation.constraints.NotNull;
import lombok.Builder;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShape;
import org.dotwebstack.framework.core.query.model.CollectionRequest;
import org.dotwebstack.framework.core.query.model.ObjectRequest;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.sparql.query.QueryStringUtil;
import org.eclipse.rdf4j.sparqlbuilder.core.SparqlBuilder;
import org.eclipse.rdf4j.sparqlbuilder.core.query.Queries;
import org.eclipse.rdf4j.sparqlbuilder.core.query.SelectQuery;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPattern;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPatterns;
import org.eclipse.rdf4j.sparqlbuilder.rdf.RdfPredicate;
import org.eclipse.rdf4j.sparqlbuilder.rdf.RdfSubject;
import reactor.core.publisher.Flux;

@Builder
public class Rdf4jQuery {

  @NotNull
  private final NodeShape nodeShape;

  private final AtomicInteger aliasCounter = new AtomicInteger();

  private final ResultMapper resultMapper = new ResultMapper();

  private final SelectQuery selectQuery;

  private Rdf4jQuery(NodeShape nodeShape, ObjectRequest objectRequest) {
    this.nodeShape = nodeShape;
    selectQuery = createSelect(objectRequest);
  }

  private Rdf4jQuery(NodeShape nodeShape, CollectionRequest collectionRequest) {
    this.nodeShape = nodeShape;
    selectQuery = createSelect(collectionRequest);
  }

  public Flux<Map<String, Object>> execute(Repository repository) {
    var connection = repository.getConnection();
    var queryResult = connection.prepareTupleQuery(selectQuery.getQueryString())
        .evaluate();

    return resultMapper.map(queryResult)
        .doOnComplete(connection::close);
  }

  private SelectQuery createSelect(CollectionRequest collectionRequest) {
    return createSelect(collectionRequest.getObjectRequest()).limit(10);
  }

  private SelectQuery createSelect(ObjectRequest objectRequest) {
    var subject = SparqlBuilder.var(newAlias());
    var wherePattern = createWherePattern(objectRequest, subject);

    return Queries.SELECT()
        .where(wherePattern);
  }

  private GraphPattern createWherePattern(ObjectRequest objectRequest, RdfSubject subject) {
    var patterns = new ArrayList<>(createClassPatterns(subject));

    objectRequest.getSelectedScalarFields()
        .stream()
        .flatMap(field -> createWherePatterns(field, subject))
        .forEach(patterns::add);

    return GraphPatterns.and(patterns.toArray(GraphPattern[]::new));
  }

  private Stream<GraphPattern> createWherePatterns(SelectedField selectedField, RdfSubject subject) {
    var propertyShape = nodeShape.getPropertyShape(selectedField.getName());
    var objectAlias = newAlias();
    var pattern = subject.has(propertyShape.toPredicate(), SparqlBuilder.var(objectAlias));

    resultMapper.registerFieldMapper(selectedField.getName(), bindings -> bindings.getBinding(objectAlias)
        .getValue()
        .stringValue());

    return Stream.of(pattern);
  }

  private List<GraphPattern> createClassPatterns(RdfSubject subject) {
    return nodeShape.getClasses()
        .stream()
        .map(classes -> createClassPattern(subject, classes))
        .collect(Collectors.toList());
  }

  private GraphPattern createClassPattern(RdfSubject subject, Set<IRI> classes) {
    RdfPredicate typePredicate = () -> String.format("%s/%s*", QueryStringUtil.valueToString(RDF.TYPE),
        QueryStringUtil.valueToString(RDFS.SUBCLASSOF));

    if (classes.size() == 1) {
      return GraphPatterns.tp(subject, typePredicate, classes.iterator()
          .next());
    }

    var typeVar = SparqlBuilder.var(newAlias());
    var graphPattern = GraphPatterns.tp(subject, typePredicate, typeVar);

    return new GraphPatternWithValues(graphPattern, Map.of(typeVar, classes));
  }

  private String newAlias() {
    return "x".concat(String.valueOf(aliasCounter.incrementAndGet()));
  }

  public static class Rdf4jQueryBuilder {

    public Rdf4jQuery build(ObjectRequest objectRequest) {
      return new Rdf4jQuery(nodeShape, objectRequest);
    }

    public Rdf4jQuery build(CollectionRequest collectionRequest) {
      return new Rdf4jQuery(nodeShape, collectionRequest);
    }
  }
}
