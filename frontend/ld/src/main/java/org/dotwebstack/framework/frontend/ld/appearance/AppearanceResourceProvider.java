package org.dotwebstack.framework.frontend.ld.appearance;

import lombok.NonNull;
import org.dotwebstack.framework.AbstractResourceProvider;
import org.dotwebstack.framework.ApplicationProperties;
import org.dotwebstack.framework.config.ConfigurationBackend;
import org.dotwebstack.framework.config.ConfigurationException;
import org.dotwebstack.framework.vocabulary.ELMO;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.QueryResults;
import org.eclipse.rdf4j.query.impl.SimpleDataset;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AppearanceResourceProvider extends AbstractResourceProvider<Appearance> {

  @Autowired
  public AppearanceResourceProvider(ConfigurationBackend configurationBackend,
      ApplicationProperties applicationProperties) {
    super(configurationBackend, applicationProperties);
  }

  @Override
  protected GraphQuery getQueryForResources(@NonNull RepositoryConnection conn) {
    String query = "CONSTRUCT { ?s ?p ?o } WHERE { ?s ?p ?o. ?rep ?appearance ?s. }";
    GraphQuery graphQuery = conn.prepareGraphQuery(query);
    graphQuery.setBinding("appearance", ELMO.APPEARANCE_PROP);

    return graphQuery;
  }

  @Override
  protected Appearance createResource(@NonNull Model model, @NonNull Resource identifier) {
    IRI type = getObjectIRI(model, identifier, RDF.TYPE).orElseThrow(
        () -> new ConfigurationException(String.format(
            "No <%s> statement has been found for appearance <%s>.", RDF.TYPE, identifier)));
    return new Appearance.Builder(identifier, type, getModel(identifier)).build();
  }

  private Model getModel(Resource identifier) {
    final RepositoryConnection repositoryConnection;
    final Model model;

    try {
      repositoryConnection = configurationBackend.getRepository().getConnection();
    } catch (RepositoryException e) {
      throw new ConfigurationException("Error while getting repository connection.", e);
    }

    String query =
        "CONSTRUCT { ?app ?p ?o. ?o ?op ?oo } " + "WHERE { ?app ?p ?o. OPTIONAL { ?o ?op ?oo } }";
    GraphQuery graphQuery = repositoryConnection.prepareGraphQuery(query);
    graphQuery.setBinding("app", identifier);

    SimpleDataset simpleDataset = new SimpleDataset();
    simpleDataset.addDefaultGraph(applicationProperties.getSystemGraph());
    simpleDataset.addDefaultGraph(ELMO.CONFIG_GRAPHNAME);
    graphQuery.setDataset(simpleDataset);

    try {
      model = QueryResults.asModel(graphQuery.evaluate());
    } catch (QueryEvaluationException e) {
      throw new ConfigurationException("Error while evaluating SPARQL query.", e);
    } finally {
      repositoryConnection.close();
    }

    return model;
  }

}
