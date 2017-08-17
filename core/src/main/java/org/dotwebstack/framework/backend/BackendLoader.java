package org.dotwebstack.framework.backend;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import javax.annotation.PostConstruct;
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

  private ConfigurationBackend configurationBackend;

  private HashMap<IRI, Backend> backends = new HashMap<>();

  private List<BackendFactory> backendFactories;

  @Autowired
  public BackendLoader(ConfigurationBackend configurationBackend,
      List<BackendFactory> backendFactories) {
    this.configurationBackend = Objects.requireNonNull(configurationBackend);
    this.backendFactories = Objects.requireNonNull(backendFactories);
  }

  @PostConstruct
  public void load() {
    Model backendModel;

    try (RepositoryConnection conn = configurationBackend.getRepository().getConnection()) {
      GraphQuery graphQuery = conn.prepareGraphQuery("CONSTRUCT { ?s ?p ?o }"
          + " WHERE { ?s ?p ?o . ?s a ?type . ?type rdfs:subClassOf ?backend }");
      graphQuery.setBinding("backend", ELMO.BACKEND);
      backendModel = QueryResults.asModel(graphQuery.evaluate());
    }

    backendModel.subjects().forEach(identifier -> {
      IRI backendIri = (IRI) identifier;
      Backend backend = createBackend(backendModel.filter(backendIri, null, null), (IRI) identifier);
      backends.put(backendIri, backend);
      LOG.info("Registered backend: <{}>", backend.getIdentifier());
    });
  }

  public Backend getBackend(IRI identifier) {
    if (!backends.containsKey(identifier)) {
      throw new IllegalArgumentException(String.format("Backend <%s> not found.", identifier));
    }

    return backends.get(identifier);
  }

  public int getNumberOfBackends() {
    return backends.size();
  }

  private Backend createBackend(Model backendModel, IRI identifier) {
    IRI backendType = Models.objectIRI(backendModel.filter(identifier, RDF.TYPE, null)).orElseThrow(
        () -> new ConfigurationException(String.format(
            "No <%s> statement has been found for backend <%s>.", RDF.TYPE, identifier)));

    for (BackendFactory backendFactory : backendFactories) {
      if (backendFactory.supports(backendType)) {
        return backendFactory.create(backendModel, identifier);
      }
    }

    throw new ConfigurationException(
        String.format("No backend factories available for type <%s>.", backendType));
  }

}
