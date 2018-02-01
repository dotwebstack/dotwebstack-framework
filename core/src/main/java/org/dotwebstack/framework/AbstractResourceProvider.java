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

  private HashMap<IRI, R> resources = new HashMap<>();

  public AbstractResourceProvider(@NonNull ConfigurationBackend configurationBackend,
      @NonNull ApplicationProperties applicationProperties) {
    this.configurationBackend = configurationBackend;
    this.applicationProperties = applicationProperties;
  }

  @Override
  public R get(IRI identifier) {
    if (!resources.containsKey(identifier)) {
      String availableResources = resources.keySet().stream().map(i -> "<" + i + ">").collect(
          Collectors.toSet()).toString();

      throw new IllegalArgumentException(String.format(
          "Resource <%s> not found. Available resources: %s", identifier, availableResources));
    }

    return resources.get(identifier);
  }

  @Override
  public Map<IRI, R> getAll() {
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

    GraphQuery query = getQueryForResources(repositoryConnection);

    SimpleDataset simpleDataset = new SimpleDataset();
    simpleDataset.addDefaultGraph(applicationProperties.getSystemGraph());
    simpleDataset.addDefaultGraph(ELMO.CONFIG_GRAPHNAME);
    query.setDataset(simpleDataset);

    Model model;

    try {
      model = QueryResults.asModel(query.evaluate());
      model.subjects().forEach(identifier -> {
        if (identifier instanceof IRI) {
          R resource = createResource(model, (IRI) identifier);

          if (resource != null) {
            resources.put((IRI) identifier, resource);
            LOG.info("Registered resource: <{}>", identifier);
          }
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

  protected abstract R createResource(Model model, IRI identifier);

  protected void finalizeResource(Model model, R resource) {}

  protected Optional<String> getObjectString(Model model, IRI subject, IRI predicate) {
    return Models.objectString(model.filter(subject, predicate, null));
  }

  protected Collection<String> getObjectStrings(Model model, IRI subject, IRI predicate) {
    return Models.objectStrings(model.filter(subject, predicate, null));
  }

  protected Optional<IRI> getObjectIRI(Model model, IRI subject, IRI predicate) {
    return Models.objectIRI(model.filter(subject, predicate, null));
  }

  protected Collection<IRI> getObjectIris(Model model, IRI subject, IRI predicate) {
    return Models.objectIRIs(model.filter(subject, predicate, null));
  }

}
