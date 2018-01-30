package org.dotwebstack.framework.transaction;

import org.dotwebstack.framework.AbstractResourceProvider;
import org.dotwebstack.framework.ApplicationProperties;
import org.dotwebstack.framework.config.ConfigurationBackend;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.repository.RepositoryConnection;

public class TransactionResourceProvider extends AbstractResourceProvider{

  /*
   * Responsibilty for loading transactions with flow
   * (no blank nodes supported yet)
   *
   * example:
   *
   config:InsertOrReplaceConcept a elmo:Transaction;
   elmo:sequentialFlow config:InsertOrReplaceFlow;
   */

  public TransactionResourceProvider(
      ConfigurationBackend configurationBackend,
      ApplicationProperties applicationProperties) {
    super(configurationBackend, applicationProperties);
  }

  @Override
  protected GraphQuery getQueryForResources(RepositoryConnection conn) {
    return null;
  }

  @Override
  protected Transaction createResource(Model model, IRI identifier) {
    return null;
  }
}
