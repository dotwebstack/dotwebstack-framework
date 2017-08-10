package org.dotwebstack.framework.backend;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.dotwebstack.framework.Registry;
import org.dotwebstack.framework.config.ConfigurationBackend;
import org.dotwebstack.framework.config.ConfigurationException;
import org.dotwebstack.framework.vocabulary.ELMO;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.util.Models;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.query.QueryResults;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BackendLoader {

  private static final Logger LOG = LoggerFactory.getLogger(BackendLoader.class);

  private Registry registry;

  private ConfigurationBackend configurationBackend;

  private List<BackendFactory> backendFactories;

  @Autowired
  public BackendLoader(Registry registry, ConfigurationBackend configurationBackend,
      List<BackendFactory> backendFactories) {
    this.registry = Objects.requireNonNull(registry);
    this.configurationBackend = Objects.requireNonNull(configurationBackend);
    this.backendFactories = Objects.requireNonNull(backendFactories);
  }

  public void load() {
    Model backendModel;

    try (RepositoryConnection conn = configurationBackend.getRepository().getConnection()) {
      GraphQuery graphQuery = conn.prepareGraphQuery("CONSTRUCT { ?s ?p ?o }"
          + " WHERE { ?s ?p ?o . ?s a ?type . ?type rdfs:subClassOf ?backend }");
      graphQuery.setBinding("backend", ELMO.BACKEND);
      backendModel = QueryResults.asModel(graphQuery.evaluate());
    }

    backendModel.subjects().forEach(identifier -> {
      Backend backend =
          createBackend(backendModel.filter(identifier, null, null), (IRI) identifier);
      registry.registerBackend(backend);
      LOG.info("Registered backend: <{}>", backend.getIdentifier());
    });
  }

  @java.lang.SuppressWarnings("squid:S3655")
  private Backend createBackend(Model backendModel, IRI identifier) {
    Optional<IRI> backendType = Models.objectIRI(backendModel.filter(identifier, RDF.TYPE, null));

    for (BackendFactory backendFactory : backendFactories) {
      if (backendFactory.supports(backendType.get())) {
        return backendFactory.create(backendModel, identifier);
      }
    }

    throw new ConfigurationException(
        String.format("No backend factories available for type <%s>.", backendType.get()));
  }

}
