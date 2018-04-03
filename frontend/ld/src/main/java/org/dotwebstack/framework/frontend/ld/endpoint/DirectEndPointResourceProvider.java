package org.dotwebstack.framework.frontend.ld.endpoint;

import lombok.NonNull;
import org.dotwebstack.framework.AbstractResourceProvider;
import org.dotwebstack.framework.ApplicationProperties;
import org.dotwebstack.framework.config.ConfigurationBackend;
import org.dotwebstack.framework.config.ConfigurationException;
import org.dotwebstack.framework.frontend.http.stage.StageResourceProvider;
import org.dotwebstack.framework.frontend.ld.endpoint.DirectEndPoint.Builder;
import org.dotwebstack.framework.frontend.ld.representation.RepresentationResourceProvider;
import org.dotwebstack.framework.frontend.ld.service.ServiceResourceProvider;
import org.dotwebstack.framework.vocabulary.ELMO;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DirectEndPointResourceProvider extends AbstractResourceProvider<DirectEndPoint> {

  private final StageResourceProvider stageResourceProvider;

  private final RepresentationResourceProvider representationResourceProvider;

  private final ServiceResourceProvider serviceResourceProvider;

  @Autowired
  public DirectEndPointResourceProvider(ConfigurationBackend configurationBackend,
      ApplicationProperties applicationProperties,
      @NonNull StageResourceProvider stageResourceProvider,
      @NonNull RepresentationResourceProvider representationResourceProvider,
      @NonNull ServiceResourceProvider serviceResourceProvider) {
    super(configurationBackend, applicationProperties);
    this.stageResourceProvider = stageResourceProvider;
    this.representationResourceProvider = representationResourceProvider;
    this.serviceResourceProvider = serviceResourceProvider;
  }

  @Override
  protected GraphQuery getQueryForResources(RepositoryConnection conn) {
    String query = "CONSTRUCT { ?s ?p ?o } WHERE { ?s ?p ?o . ?s a ?type . }";
    GraphQuery graphQuery = conn.prepareGraphQuery(query);
    graphQuery.setBinding("type", ELMO.ENDPOINT);

    return graphQuery;
  }

  @Override
  protected DirectEndPoint createResource(@NonNull Model model, @NonNull Resource identifier) {
    String pathPattern = getObjectString(model, identifier, ELMO.PATH_PATTERN).orElseThrow(
        () -> new ConfigurationException(
            String.format("No <%s> statement has been found for pathPattern <%s>.",
                ELMO.PATH_PATTERN, identifier)));

    final DirectEndPoint.Builder builder = new Builder(identifier, pathPattern);
    getObjectString(model, identifier, RDFS.LABEL).ifPresent(builder::label);
    getObjectResource(model, identifier, ELMO.STAGE_PROP).ifPresent(
        iri -> builder.stage(stageResourceProvider.get(iri)));

    getObjectResource(model, identifier, ELMO.GET_REPRESENTATION_PROP).ifPresent(
        resource -> builder.getRepresentation(representationResourceProvider.get(resource)));
    getObjectResource(model, identifier, ELMO.POST_REPRESENTATION_PROP).ifPresent(
        resource -> builder.postRepresentation(representationResourceProvider.get(resource)));
    getObjectResource(model, identifier, ELMO.SERVICE_POST_PROP).ifPresent(
        postService -> builder.postService(serviceResourceProvider.get(postService)));
    getObjectResource(model, identifier, ELMO.SERVICE_PUT_PROP).ifPresent(
        putService -> builder.putService(serviceResourceProvider.get(putService)));
    getObjectResource(model, identifier, ELMO.SERVICE_DELETE_PROP).ifPresent(
        deleteService -> builder.deleteService(serviceResourceProvider.get(deleteService)));

    return builder.build();
  }

}
