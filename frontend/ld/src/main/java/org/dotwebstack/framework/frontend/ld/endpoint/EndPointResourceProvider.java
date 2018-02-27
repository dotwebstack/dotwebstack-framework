package org.dotwebstack.framework.frontend.ld.endpoint;

import lombok.NonNull;
import org.dotwebstack.framework.AbstractResourceProvider;
import org.dotwebstack.framework.ApplicationProperties;
import org.dotwebstack.framework.config.ConfigurationBackend;
import org.dotwebstack.framework.config.ConfigurationException;
import org.dotwebstack.framework.frontend.http.stage.StageResourceProvider;
import org.dotwebstack.framework.frontend.ld.parameter.ParameterMapperResourceProvider;
import org.dotwebstack.framework.vocabulary.ELMO;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EndPointResourceProvider extends AbstractResourceProvider<EndPoint> {

  private final ParameterMapperResourceProvider parameterMapperResourceProvider;

  private final StageResourceProvider stageResourceProvider;

  @Autowired
  public EndPointResourceProvider(ConfigurationBackend configurationBackend,
      ApplicationProperties applicationProperties,
      @NonNull ParameterMapperResourceProvider parameterMapperResourceProvider,
      @NonNull StageResourceProvider stageResourceProvider) {
    super(configurationBackend, applicationProperties);
    this.parameterMapperResourceProvider = parameterMapperResourceProvider;
    this.stageResourceProvider = stageResourceProvider;
  }


  @Override
  protected GraphQuery getQueryForResources(RepositoryConnection conn) {
    String query = "CONSTRUCT { ?s ?p ?o } WHERE { ?s ?p ?o . ?s a ?type . }";
    GraphQuery graphQuery = conn.prepareGraphQuery(query);
    graphQuery.setBinding("type", ELMO.ENDPOINT);
    return graphQuery;
  }

  @Override
  protected EndPoint createResource(@NonNull Model model, @NonNull Resource identifier) {
    String pathPattern = getObjectString(model, identifier, ELMO.PATH_PATTERN).orElseThrow(
        () -> new ConfigurationException(
            String.format("No <%s> statement has been found for pathPattern <%s>.",
                ELMO.PATH_PATTERN, identifier)));

    final EndPoint.Builder builder = new EndPoint.Builder(identifier, pathPattern);
    getObjectString(model, identifier, RDFS.LABEL).ifPresent(builder::label);
    getObjectResource(model, identifier, ELMO.STAGE_PROP).ifPresent(
        iri -> builder.stage(stageResourceProvider.get(iri)));
    getObjectResource(model, identifier, ELMO.PARAMETER_MAPPER_PROP).ifPresent(
        iri -> builder.parameterMapper(parameterMapperResourceProvider.get(iri)));

    return builder.build();
  }
}
