package org.dotwebstack.framework.frontend.ld.parameter;

import lombok.NonNull;
import org.dotwebstack.framework.AbstractResourceProvider;
import org.dotwebstack.framework.ApplicationProperties;
import org.dotwebstack.framework.config.ConfigurationBackend;
import org.dotwebstack.framework.vocabulary.ELMO;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UriParameterMapperResourceProvider
    extends AbstractResourceProvider<UriParameterMapper> {

  public UriParameterMapperResourceProvider(ConfigurationBackend configurationBackend,
      ApplicationProperties applicationProperties) {
    super(configurationBackend, applicationProperties);
  }

  private static final Logger LOG =
      LoggerFactory.getLogger(UriParameterMapperResourceProvider.class);

  @Override
  protected GraphQuery getQueryForResources(@NonNull RepositoryConnection conn) {
    String query = "CONSTRUCT { ?s ?p ?o } WHERE { ?s ?p ?o. ?rep ?parametermapper ?s. }";
    GraphQuery graphQuery = conn.prepareGraphQuery(query);
    graphQuery.setBinding("parametermapper", ELMO.PARAMETER_MAPPER_PROP);

    return graphQuery;
  }

  @Override
  protected UriParameterMapper createResource(Model model, IRI identifier) {
    final UriParameterMapper.Builder builder = UriParameterMapper.Builder.anUriParameterMapper();

    builder.identifier(identifier);

    return builder.build();
  }

}
