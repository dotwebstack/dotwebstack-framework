package org.dotwebstack.framework.informationproduct;

import java.util.HashMap;
import java.util.Optional;
import javax.annotation.PostConstruct;
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

  private static final Logger LOG = LoggerFactory.getLogger(InformationProductLoader.class);

  private final BackendLoader backendLoader;

  private final ConfigurationBackend configurationBackend;

  private HashMap<IRI, InformationProduct> informationProducts = new HashMap<>();

  @Autowired
  public InformationProductLoader(BackendLoader backendLoader,
      ConfigurationBackend configurationBackend) {
    this.backendLoader = backendLoader;
    this.configurationBackend = configurationBackend;
  }

  @PostConstruct
  public void load() {
    Model informationProductModels = getModelFromConfiguration();

    informationProductModels.subjects().forEach(identifier -> {
      Model informationProductTriples = informationProductModels.filter(identifier, null, null);
      if (identifier instanceof IRI) {
        IRI iri = (IRI) identifier;
        InformationProduct informationProduct =
            createInformationProduct(iri, informationProductTriples);

        informationProducts.put(iri, informationProduct);

        LOG.info("Registered informationProduct: <{}>", informationProduct.getIdentifier());
      }
    });
  }

  public InformationProduct getInformationProduct(IRI identifier) {
    if (!informationProducts.containsKey(identifier)) {
      throw new IllegalArgumentException(
          String.format("Information product <%s> not found.", identifier));
    }

    return informationProducts.get(identifier);
  }

  public int getNumberOfInformationProducts() {
    return informationProducts.size();
  }

  private InformationProduct createInformationProduct(IRI identifier, Model statements) {
    IRI backendIRI =
        getIRI(statements, ELMO.BACKEND_PROP).orElseThrow(() -> new ConfigurationException(
            String.format("No <%s> backend has been found for information product <%s>.",
                ELMO.BACKEND_PROP, identifier)));

    return new InformationProduct.Builder(identifier,
        createBackendSource(backendIRI, statements)).label(
            getObjectString(statements, RDFS.LABEL).orElse(null)).build();
  }

  private Optional<String> getObjectString(Model informationProductTriples, IRI predicate) {
    return Models.objectString(informationProductTriples.filter(null, predicate, null));
  }

  private Optional<IRI> getIRI(Model informationProductTriples, IRI predicate) {
    return Models.objectIRI(informationProductTriples.filter(null, predicate, null));
  }

  private BackendSource createBackendSource(IRI backendIdentifier, Model statements) {
    return backendLoader.getBackend(backendIdentifier).createSource(statements);
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
