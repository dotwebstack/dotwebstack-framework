package org.dotwebstack.framework;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import lombok.NonNull;
import org.dotwebstack.framework.config.ConfigurationBackend;
import org.dotwebstack.framework.config.ConfigurationException;
import org.dotwebstack.framework.vocabulary.ELMO;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.util.Models;
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.QueryResults;
import org.eclipse.rdf4j.query.impl.SimpleDataset;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractResourceProvider<R> implements ResourceProvider<R> {

  private static final Logger LOG = LoggerFactory.getLogger(AbstractResourceProvider.class);

  protected ConfigurationBackend configurationBackend;

  protected ApplicationProperties applicationProperties;

  private HashMap<Resource, R> resources = new HashMap<>();

  public AbstractResourceProvider(@NonNull ConfigurationBackend configurationBackend,
      @NonNull ApplicationProperties applicationProperties) {
    this.configurationBackend = configurationBackend;
    this.applicationProperties = applicationProperties;
  }

  @Override
  public R get(Resource identifier) {
    if (!resources.containsKey(identifier)) {
      String availableResources = resources.keySet().stream().map(i -> "<" + i + ">").collect(
          Collectors.toSet()).toString();

      throw new IllegalArgumentException(String.format(
          "Resource <%s> not found. Available resources: %s", identifier, availableResources));
    }

    return resources.get(identifier);
  }

  @Override
  public Map<Resource, R> getAll() {
    return resources;
  }

  @PostConstruct
  public void loadResources() {
    RepositoryConnection repositoryConnection;

    try {
      repositoryConnection = configurationBackend.getRepository().getConnection();
    } catch (RepositoryException e) {
      throw new ConfigurationException("Error while getting repository connection.", e);
    }
    SimpleDataset simpleDataset = new SimpleDataset();
    simpleDataset.addDefaultGraph(applicationProperties.getSystemGraph());
    simpleDataset.addDefaultGraph(ELMO.CONFIG_GRAPHNAME);
    simpleDataset.addNamedGraph(ELMO.SHACL_GRAPHNAME);

    GraphQuery query = getQueryForResources(repositoryConnection);
    query.setDataset(simpleDataset);

    Model model;

    try {
      model = QueryResults.asModel(query.evaluate());
      model.subjects().forEach(identifier -> {
        R resource = createResource(model, identifier);
        if (resource != null) {
          resources.put(identifier, resource);
          LOG.info("Registered resource: <{}>", identifier);
        }
      });
    } catch (QueryEvaluationException e) {
      throw new ConfigurationException("Error while evaluating SPARQL query.", e);
    } finally {
      repositoryConnection.close();
    }
    resources.forEach((key, resource) -> finalizeResource(model, resource));
  }

  protected abstract GraphQuery getQueryForResources(RepositoryConnection conn);

  protected abstract R createResource(Model model, Resource identifier);

  protected void finalizeResource(Model model, R resource) {}

  protected Optional<String> getObjectString(Model model, Resource subject, IRI predicate) {
    return Models.objectString(model.filter(subject, predicate, null));
  }

  protected Collection<String> getObjectStrings(Model model, Resource subject, IRI predicate) {
    return Models.objectStrings(model.filter(subject, predicate, null));
  }

  protected Collection<IRI> getPredicateIris(Model model, Resource subject) {
    return model.filter(subject, null, null).predicates();
  }

  protected Optional<IRI> getObjectIRI(Model model, Resource subject, IRI predicate) {
    return Models.objectIRI(model.filter(subject, predicate, null));
  }

  protected Optional<Resource> getObjectResource(Model model, Resource subject, IRI predicate) {
    return Models.objectResource(model.filter(subject, predicate, null));
  }

  protected Collection<IRI> getObjectIris(Model model, Resource subject, IRI predicate) {
    return Models.objectIRIs(model.filter(subject, predicate, null));
  }

  protected Collection<Resource> getObjectResources(Model model, Resource subject, IRI predicate) {
    return Models.objectResources(model.filter(subject, predicate, null));
  }

  protected Optional<Value> getObjectValue(Model model, Resource subject, IRI predicate) {
    return Models.getProperty(model, subject, predicate);
  }

}
