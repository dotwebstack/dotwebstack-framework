package org.dotwebstack.framework.informationproduct;

import java.util.Optional;
import javax.annotation.PostConstruct;
import org.dotwebstack.framework.InformationProduct;
import org.dotwebstack.framework.Registry;
import org.dotwebstack.framework.backend.BackendLoader;
import org.dotwebstack.framework.backend.BackendSource;
import org.dotwebstack.framework.config.ConfigurationBackend;
import org.dotwebstack.framework.config.ConfigurationException;
import org.dotwebstack.framework.vocabulary.ELMO;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.util.Models;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.query.QueryResults;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class InformationProductLoader {
  private static final Logger LOG = LoggerFactory.getLogger(BackendLoader.class);

  private final Registry registry;
  private final ConfigurationBackend configurationBackend;

  @Autowired
  public InformationProductLoader(
      Registry registry, ConfigurationBackend configurationBackend) {
    this.registry = registry;
    this.configurationBackend = configurationBackend;
  }

  @PostConstruct
  public void load() {
    Model informationProducts = getModelFromConfiguration();

    informationProducts.subjects().forEach(identifier -> {
      Model informationProductTriples = informationProducts.filter(identifier, null, null);
      if(identifier instanceof IRI) {
        InformationProduct informationProduct =
            createInformationProduct((IRI) identifier, informationProductTriples);
        registry.registerInformationProduct(informationProduct);
        LOG.info("Registered informationProduct: <{}>", informationProduct.getIdentifier());
      }
    });

  }

  private InformationProduct createInformationProduct(IRI identifier,
      Model statements) {
    IRI backendIRI = getIRI(statements, ELMO.BACKEND).orElseThrow(() -> new ConfigurationException(String.format(
        "No <%s> backend has been found for information product <%s>.", ELMO.BACKEND, identifier)));

    InformationProduct.Builder builder = new InformationProduct.Builder(identifier, createBackendSource(backendIRI, statements));
    getObjectString(statements, RDFS.LABEL).ifPresent(label -> builder.label(label));
    return builder.build();
  }

  private Optional<String> getObjectString(Model informationProductTriples, IRI predicate) {
    return Models.objectString(informationProductTriples.filter(null, predicate, null));
  }
  private Optional<IRI> getIRI(Model informationProductTriples, IRI predicate) {
    return Models.objectIRI(informationProductTriples.filter(null, predicate, null));
  }

  private BackendSource createBackendSource(IRI backend, Model statements) {
    return registry.getBackend(backend).createSource(statements);
  }

  private Model getModelFromConfiguration() {
    try (RepositoryConnection conn = configurationBackend.getRepository().getConnection()) {
      String query = "CONSTRUCT { ?s ?p ?o } WHERE { ?s ?p ?o . ?s a ?type . }";
      GraphQuery graphQuery = conn.prepareGraphQuery(query);
      graphQuery.setBinding("type", ELMO.INFORMATION_PRODUCT);
      return QueryResults.asModel(graphQuery.evaluate());
    }
  }
}
