package org.dotwebstack.framework.frontend.http.stage;

import lombok.NonNull;
import org.dotwebstack.framework.AbstractResourceProvider;
import org.dotwebstack.framework.ApplicationProperties;
import org.dotwebstack.framework.config.ConfigurationBackend;
import org.dotwebstack.framework.config.ConfigurationException;
import org.dotwebstack.framework.frontend.http.layout.LayoutResourceProvider;
import org.dotwebstack.framework.frontend.http.site.SiteResourceProvider;
import org.dotwebstack.framework.vocabulary.ELMO;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.vocabulary.DC;
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StageResourceProvider extends AbstractResourceProvider<Stage> {

  private SiteResourceProvider siteResourceProvider;

  private LayoutResourceProvider layoutResourceProvider;

  @Autowired
  public StageResourceProvider(ConfigurationBackend configurationBackend,
      @NonNull SiteResourceProvider siteResourceProvider,
      @NonNull LayoutResourceProvider layoutResourceProvider,
      ApplicationProperties applicationProperties) {
    super(configurationBackend, applicationProperties);
    this.siteResourceProvider = siteResourceProvider;
    this.layoutResourceProvider = layoutResourceProvider;
  }

  @Override
  protected GraphQuery getQueryForResources(RepositoryConnection conn) {
    String query = "CONSTRUCT { ?s ?p ?o } WHERE { ?s ?p ?o . ?s a ?type . }";
    GraphQuery graphQuery = conn.prepareGraphQuery(query);
    graphQuery.setBinding("type", ELMO.STAGE);

    return graphQuery;
  }

  @Override
  protected Stage createResource(Model model, Resource identifier) {
    Resource siteIRI = getObjectResource(model, identifier, ELMO.SITE_PROP).orElseThrow(
        () -> new ConfigurationException(String.format(
            "No <%s> statement has been found for stage <%s>.", ELMO.SITE_PROP, identifier)));

    Stage.Builder builder = new Stage.Builder(identifier, siteResourceProvider.get(siteIRI));
    getObjectString(model, identifier, ELMO.BASE_PATH).ifPresent(builder::basePath);
    getObjectResource(model, identifier, ELMO.LAYOUT_PROP).ifPresent(
        iri -> builder.layout(layoutResourceProvider.get(iri)));
    getObjectString(model, identifier, DC.TITLE).ifPresent(builder::title);

    return builder.build();
  }

}
