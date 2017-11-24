package org.dotwebstack.framework.param;

import lombok.NonNull;
import org.dotwebstack.framework.AbstractResourceProvider;
import org.dotwebstack.framework.ApplicationProperties;
import org.dotwebstack.framework.config.ConfigurationBackend;
import org.dotwebstack.framework.config.ConfigurationException;
import org.dotwebstack.framework.vocabulary.ELMO;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ParameterResourceProvider extends AbstractResourceProvider<ParameterDefinition> {

  @Autowired
  public ParameterResourceProvider(ConfigurationBackend configurationBackend,
      ApplicationProperties applicationProperties) {
    super(configurationBackend, applicationProperties);
  }

  @Override
  protected GraphQuery getQueryForResources(@NonNull RepositoryConnection conn) {
    String query = "CONSTRUCT { ?s ?p ?o } WHERE { ?s ?p ?o . ?s a ?type . }";
    GraphQuery graphQuery = conn.prepareGraphQuery(query);

    graphQuery.setBinding("type", ELMO.TERM_FILTER);

    return graphQuery;
  }

  @Override
  protected ParameterDefinition createResource(@NonNull Model model, @NonNull IRI identifier) {
    String name = getObjectString(model, identifier, ELMO.NAME_PROP).orElseThrow(
        () -> new ConfigurationException(
            String.format("No <%s> property found for <%s> of type <%s>", ELMO.NAME_PROP,
                identifier, ELMO.TERM_FILTER)));

    return new ParameterDefinition(identifier, name);
  }

}
