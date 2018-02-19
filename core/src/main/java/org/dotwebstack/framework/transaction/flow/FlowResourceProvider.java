package org.dotwebstack.framework.transaction.flow;

import org.dotwebstack.framework.AbstractResourceProvider;
import org.dotwebstack.framework.ApplicationProperties;
import org.dotwebstack.framework.config.ConfigurationBackend;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.repository.RepositoryConnection;

public class FlowResourceProvider extends AbstractResourceProvider {

  /*
   * Responsibilty for loading flow
   * (no blank nodes supported yet)
   *
   * e.g.
   *
      elmo:sequentialFlow a elmo:Flow;
      elmo:step config:validationStep;
      elmo:step config:updateStep;
      elmo:step config:finalInsertOrUpdateStep;
   */

  public FlowResourceProvider (
      ConfigurationBackend configurationBackend,
      ApplicationProperties applicationProperties) {
    super(configurationBackend, applicationProperties);
  }

  @Override
  protected GraphQuery getQueryForResources(RepositoryConnection conn) {
    return null;
  }

  @Override
  protected Flow createResource(Model model, IRI identifier) {
    return null;
  }
}
