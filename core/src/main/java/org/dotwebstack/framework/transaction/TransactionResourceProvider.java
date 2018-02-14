package org.dotwebstack.framework.transaction;

import java.util.List;
import org.dotwebstack.framework.AbstractResourceProvider;
import org.dotwebstack.framework.ApplicationProperties;
import org.dotwebstack.framework.config.ConfigurationBackend;
import org.dotwebstack.framework.config.ConfigurationException;
import org.dotwebstack.framework.transaction.flow.FlowFactory;
import org.dotwebstack.framework.vocabulary.ELMO;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TransactionResourceProvider extends AbstractResourceProvider<Transaction> {

  private List<FlowFactory> flowFactories;

  @Autowired
  public TransactionResourceProvider(ConfigurationBackend configurationBackend,
      ApplicationProperties applicationProperties, List<FlowFactory> flowFactories) {
    super(configurationBackend, applicationProperties);
    this.flowFactories = flowFactories;
  }

  @Override
  protected GraphQuery getQueryForResources(RepositoryConnection conn) {
    final String query = "CONSTRUCT { ?s ?p ?o } WHERE { ?s ?p ?o . ?s a ?type . }";

    final GraphQuery graphQuery = conn.prepareGraphQuery(query);
    graphQuery.setBinding("type", ELMO.TRANSACTION);

    return graphQuery;
  }

  @Override
  protected Transaction createResource(Model model, IRI identifier) {
    final Transaction.Builder transactionBuilder = new Transaction.Builder(identifier);

    for (IRI predicate : getPredicateIris(model, identifier)) {
      for (FlowFactory flowFactory : flowFactories) {
        if (flowFactory.supports(predicate)) {
          transactionBuilder.flow(flowFactory.create(model, identifier));
          return transactionBuilder.build();
        }
      }
    }

    throw new ConfigurationException(
        String.format("No flow statements has been found for transaction <%s>.", identifier));
  }

}
