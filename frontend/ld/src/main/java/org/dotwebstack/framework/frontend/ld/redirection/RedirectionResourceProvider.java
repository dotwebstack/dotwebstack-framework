package org.dotwebstack.framework.frontend.ld.redirection;

import lombok.NonNull;
import org.dotwebstack.framework.AbstractResourceProvider;
import org.dotwebstack.framework.ApplicationProperties;
import org.dotwebstack.framework.config.ConfigurationBackend;
import org.dotwebstack.framework.config.ConfigurationException;
import org.dotwebstack.framework.frontend.http.stage.StageResourceProvider;
import org.dotwebstack.framework.vocabulary.ELMO;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RedirectionResourceProvider extends AbstractResourceProvider<Redirection> {

  private static final String NO_STATEMENT_FOUND_FOR_REDIRECTION_EXCEPTION =
      "No <%s> statement has been found for redirection <%s>.";

  private StageResourceProvider stageResourceProvider;

  @Autowired
  public RedirectionResourceProvider(ConfigurationBackend configurationBackend,
      @NonNull StageResourceProvider stageResourceProvider,
      ApplicationProperties applicationProperties) {
    super(configurationBackend, applicationProperties);
    this.stageResourceProvider = stageResourceProvider;
  }

  @Override
  protected GraphQuery getQueryForResources(RepositoryConnection conn) {
    String query = "CONSTRUCT { ?s ?p ?o } WHERE { ?s ?p ?o . ?s a ?type . }";
    GraphQuery graphQuery = conn.prepareGraphQuery(query);
    graphQuery.setBinding("type", ELMO.REDIRECTION);
    return graphQuery;
  }

  @Override
  protected Redirection createResource(Model model, Resource identifier) {
    Resource stageIri = getObjectResource(model, identifier, ELMO.STAGE_PROP).orElseThrow(
        () -> new ConfigurationException(String.format(NO_STATEMENT_FOUND_FOR_REDIRECTION_EXCEPTION,
            ELMO.STAGE_PROP, identifier)));

    String pathPattern = getObjectString(model, identifier, ELMO.PATH_PATTERN).orElseThrow(
        () -> new ConfigurationException(String.format(NO_STATEMENT_FOUND_FOR_REDIRECTION_EXCEPTION,
            ELMO.PATH_PATTERN, identifier)));

    String redirectTemplate =
        getObjectString(model, identifier, ELMO.REDIRECT_TEMPLATE).orElseThrow(
            () -> new ConfigurationException(String.format(
                NO_STATEMENT_FOUND_FOR_REDIRECTION_EXCEPTION, ELMO.REDIRECT_TEMPLATE, identifier)));

    Redirection.Builder builder = new Redirection.Builder(identifier,
        stageResourceProvider.get(stageIri), pathPattern, redirectTemplate);

    return builder.build();
  }

}
