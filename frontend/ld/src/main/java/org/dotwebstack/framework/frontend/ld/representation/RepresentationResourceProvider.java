package org.dotwebstack.framework.frontend.ld.representation;

import lombok.NonNull;
import org.dotwebstack.framework.AbstractResourceProvider;
import org.dotwebstack.framework.ApplicationProperties;
import org.dotwebstack.framework.config.ConfigurationBackend;
import org.dotwebstack.framework.frontend.http.stage.StageResourceProvider;
import org.dotwebstack.framework.frontend.ld.appearance.AppearanceResourceProvider;
import org.dotwebstack.framework.informationproduct.InformationProductResourceProvider;
import org.dotwebstack.framework.vocabulary.ELMO;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RepresentationResourceProvider extends AbstractResourceProvider<Representation> {

  private final InformationProductResourceProvider informationProductResourceProvider;

  private final AppearanceResourceProvider appearanceResourceProvider;

  private final StageResourceProvider stageResourceProvider;

  @Autowired
  public RepresentationResourceProvider(ConfigurationBackend configurationBackend,
      @NonNull InformationProductResourceProvider informationProductResourceProvider,
      @NonNull AppearanceResourceProvider appearanceResourceProvider,
      @NonNull StageResourceProvider stageResourceProvider,
      ApplicationProperties applicationProperties) {
    super(configurationBackend, applicationProperties);
    this.informationProductResourceProvider = informationProductResourceProvider;
    this.appearanceResourceProvider = appearanceResourceProvider;
    this.stageResourceProvider = stageResourceProvider;
  }

  @Override
  protected GraphQuery getQueryForResources(RepositoryConnection conn) {
    final String query = "CONSTRUCT { ?s ?p ?o } WHERE { ?s ?p ?o . ?s a ?type . }";

    final GraphQuery graphQuery = conn.prepareGraphQuery(query);
    graphQuery.setBinding("type", ELMO.REPRESENTATION);

    return graphQuery;
  }

  @Override
  protected Representation createResource(Model model, IRI identifier) {
    final Representation.Builder builder = new Representation.Builder(identifier);

    getObjectIRI(model, identifier, ELMO.INFORMATION_PRODUCT_PROP).ifPresent(iri ->
        builder.informationProduct(informationProductResourceProvider.get(iri)));

    getObjectIRI(model, identifier, ELMO.APPEARANCE_PROP).ifPresent(iri ->
        builder.appearance(appearanceResourceProvider.get(iri)));

    getObjectString(model, identifier, ELMO.URL_PATTERN).ifPresent(builder::urlPatterns);

    getObjectIRI(model, identifier, ELMO.STAGE_PROP).ifPresent(iri ->
        builder.stage(stageResourceProvider.get(iri)));

    return builder.build();
  }
  
}
