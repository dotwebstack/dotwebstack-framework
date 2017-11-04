package org.dotwebstack.framework.frontend.ld.representation;

import java.util.Optional;
import lombok.NonNull;
import org.dotwebstack.framework.AbstractResourceProvider;
import org.dotwebstack.framework.ApplicationProperties;
import org.dotwebstack.framework.config.ConfigurationBackend;
import org.dotwebstack.framework.frontend.http.stage.StageResourceProvider;
import org.dotwebstack.framework.frontend.ld.appearance.Appearance;
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

  private InformationProductResourceProvider informationProductResourceProvider;

  private AppearanceResourceProvider appearanceResourceProvider;

  private StageResourceProvider stageResourceProvider;

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
    String query = "CONSTRUCT { ?s ?p ?o } WHERE { ?s ?p ?o . ?s a ?type . }";
    GraphQuery graphQuery = conn.prepareGraphQuery(query);
    graphQuery.setBinding("type", ELMO.REPRESENTATION);
    return graphQuery;
  }

  @Override
  protected Representation createResource(Model model, IRI identifier) {
    final Optional<IRI> informationProductIri =
        getObjectIRI(model, identifier, ELMO.INFORMATION_PRODUCT_PROP);

    final Optional<IRI> appearanceIri =
        getObjectIRI(model, identifier, ELMO.APPEARANCE_PROP);

    final Optional<IRI> stageIri = getObjectIRI(model, identifier, ELMO.STAGE_PROP);

    final Optional<String> urlPattern = getObjectString(model, identifier, ELMO.URL_PATTERN);

    Representation.Builder builder = new Representation.Builder(identifier);

    urlPattern.ifPresent(builder::urlPatterns);
    informationProductIri.ifPresent(
        iri -> builder.informationProduct(informationProductResourceProvider.get(iri)));
    appearanceIri.ifPresent(
        iri -> builder.appearance(appearanceResourceProvider.get(iri)));
    if (!appearanceIri.isPresent()) {
      builder.appearance(new Appearance());
    }
    stageIri.ifPresent(iri -> builder.stage(stageResourceProvider.get(iri)));

    return builder.build();
  }
}
