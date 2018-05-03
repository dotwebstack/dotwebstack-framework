package org.dotwebstack.framework.rml;

import com.taxonic.carml.vocab.Rdf.Carml;
import com.taxonic.carml.vocab.Rml;
import java.util.List;
import lombok.NonNull;
import org.dotwebstack.framework.AbstractResourceProvider;
import org.dotwebstack.framework.ApplicationProperties;
import org.dotwebstack.framework.config.ConfigurationBackend;
import org.dotwebstack.framework.config.ConfigurationException;
import org.dotwebstack.framework.vocabulary.ELMO;
import org.dotwebstack.framework.vocabulary.R2RML;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.query.QueryResults;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.impl.SimpleDataset;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RmlMappingResourceProvider extends AbstractResourceProvider<RmlMapping> {

  private static final String STREAMNAME = "streamName";

  private RepositoryConnection repositoryConnection;

  @Autowired
  public RmlMappingResourceProvider(@NonNull ConfigurationBackend configurationBackend,
      @NonNull ApplicationProperties applicationProperties) {
    super(configurationBackend, applicationProperties);
  }

  @Override
  protected GraphQuery getQueryForResources(RepositoryConnection conn) {
    String query = "CONSTRUCT { ?s a ?type } WHERE { ?s a ?type }";
    GraphQuery graphQuery = conn.prepareGraphQuery(query);
    graphQuery.setBinding("type", R2RML.TRIPLES_MAP);

    return graphQuery;
  }

  @Override
  protected RmlMapping createResource(Model model, Resource identifier) {
    try {
      repositoryConnection = configurationBackend.getRepository().getConnection();
    } catch (RepositoryException repositoryException) {
      throw new ConfigurationException("Error while getting repository connection.",
          repositoryException);
    }

    RmlMapping rmlMapping = new RmlMapping.Builder(identifier) //
        .model(getModel(identifier)).streamName(getStreamName(identifier)).build();

    repositoryConnection.close();

    return rmlMapping;
  }

  private Model getModel(Resource identifier) {
    String query = "CONSTRUCT { ?rmlmapping ?p ?o1 . ?o1 ?p2 ?o2 . ?o2 ?p3 ?o3 } "
        + "WHERE { ?rmlmapping ?p ?o1 . ?rmlmapping a ?type "
        + "OPTIONAL { ?o1 ?p2 ?o2 OPTIONAL { ?o2 ?p3 ?o3 } } } ";
    GraphQuery graphQuery = repositoryConnection.prepareGraphQuery(query);
    graphQuery.setBinding("rmlmapping", identifier);
    graphQuery.setBinding("type", R2RML.TRIPLES_MAP);

    graphQuery.setDataset(getSimpleDataset());

    return QueryResults.asModel(graphQuery.evaluate());
  }

  private String getStreamName(Resource identifier) {
    String query = String.format(
        "SELECT ?streamName " + "WHERE { ?rmlmapping ?p ?o . ?rmlmapping a ?type "
            + "OPTIONAL { ?o <%s> ?oo OPTIONAL { ?oo <%s> ?streamName } } } ",
        Rml.source, Carml.streamName);
    TupleQuery tupleQuery = repositoryConnection.prepareTupleQuery(query);
    tupleQuery.setBinding("rmlmapping", identifier);
    tupleQuery.setBinding("type", R2RML.TRIPLES_MAP);

    tupleQuery.setDataset(getSimpleDataset());

    List<BindingSet> queryResults = QueryResults.asList(tupleQuery.evaluate());

    if (queryResults.isEmpty() || queryResults.get(0).getValue(STREAMNAME) == null) {
      throw new ConfigurationException(
          String.format("%s not set for RmlMapping %s", STREAMNAME, identifier));
    }

    return queryResults.get(0).getValue(STREAMNAME).stringValue();
  }

  private SimpleDataset getSimpleDataset() {
    SimpleDataset simpleDataset = new SimpleDataset();
    simpleDataset.addDefaultGraph(applicationProperties.getSystemGraph());
    simpleDataset.addDefaultGraph(ELMO.CONFIG_GRAPHNAME);
    return simpleDataset;
  }

}
